package plat.clinimedik.app.ui.screens.recepcion.nuevacita

import android.util.Patterns
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import java.text.Normalizer
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import plat.clinimedik.app.CliniMedikApp
import plat.clinimedik.app.data.model.Cita
import plat.clinimedik.app.data.model.EstadoConfirmacion
import plat.clinimedik.app.data.model.Paciente
import plat.clinimedik.app.data.preferencias.ConfiguracionRepositorio
import plat.clinimedik.app.data.repository.CitaRepositorio
import plat.clinimedik.app.data.repository.PacienteRepositorio
import plat.clinimedik.app.ui.navigation.NuevaCita

class NuevaCitaViewModel(
    savedStateHandle: SavedStateHandle,
    private val citaRepositorio: CitaRepositorio,
    private val pacienteRepositorio: PacienteRepositorio,
    private val configuracionRepositorio: ConfiguracionRepositorio
) : ViewModel() {

    private val ruta = savedStateHandle.toRoute<NuevaCita>()
    private val pacienteIdInicial: String? = ruta.pacienteId

    private val _state = MutableStateFlow(
        NuevaCitaUiState(
            cargando = pacienteIdInicial != null,
            motivo = ruta.motivo.orEmpty()
        )
    )
    val state: StateFlow<NuevaCitaUiState> = _state.asStateFlow()

    private val _efectos = Channel<NuevaCitaEfecto>()
    val efectos: Flow<NuevaCitaEfecto> = _efectos.receiveAsFlow()

    private val consulta = MutableStateFlow("")

    private val formatoFecha = DateTimeFormatter
        .ofPattern("dd/MM/uuuu")
        .withResolverStyle(ResolverStyle.STRICT)

    private val formatoHora = DateTimeFormatter.ofPattern("H:mm")

    init {
        observarBusqueda()
        pacienteIdInicial?.let { cargarPaciente(it) }
    }

    fun onEvent(event: NuevaCitaEvent) {
        when (event) {
            is NuevaCitaEvent.CambiarPaciente -> cambiarPaciente(event.texto)

            is NuevaCitaEvent.ElegirPaciente -> elegirPaciente(event.pacienteId)

            NuevaCitaEvent.QuitarPaciente -> quitarPaciente()

            is NuevaCitaEvent.CambiarCorreo ->
                _state.update { it.copy(correo = event.texto, errorCorreo = null) }

            is NuevaCitaEvent.CambiarFecha ->
                _state.update { it.copy(fecha = event.texto, errorFecha = null) }

            is NuevaCitaEvent.CambiarHora ->
                _state.update { it.copy(hora = event.texto, errorHora = null) }

            is NuevaCitaEvent.CambiarMotivo ->
                _state.update { it.copy(motivo = event.texto, errorMotivo = null) }

            NuevaCitaEvent.Guardar -> guardarCita()

            NuevaCitaEvent.Reintentar -> pacienteIdInicial?.let { cargarPaciente(it) }

            NuevaCitaEvent.Regresar -> {
                if (!_state.value.guardando) {
                    enviar(NuevaCitaEfecto.Regresar)
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private fun observarBusqueda() {
        viewModelScope.launch {
            consulta
                .map { it.trim() }
                .distinctUntilChanged()
                .debounce { texto ->
                    if (texto.length < NuevaCitaUiState.MINIMO_BUSQUEDA) 0L else RETARDO_BUSQUEDA_MS
                }
                .flatMapLatest { texto -> buscarSugerencias(texto) }
                .collect { resultado ->
                    _state.update {
                        it.copy(
                            sugerencias = resultado.sugerencias,
                            busquedaResuelta = resultado.texto,
                            errorBusqueda = if (resultado.fallo) MENSAJE_ERROR_BUSQUEDA else null
                        )
                    }
                }
        }
    }

    private fun buscarSugerencias(texto: String): Flow<ResultadoBusqueda> {
        if (texto.length < NuevaCitaUiState.MINIMO_BUSQUEDA) {
            return flowOf(ResultadoBusqueda(texto, emptyList(), false))
        }
        return pacienteRepositorio.buscar(texto)
            .map { lista ->
                ResultadoBusqueda(
                    texto = texto,
                    sugerencias = lista.take(MAXIMO_SUGERENCIAS).map { it.aSugerido() },
                    fallo = false
                )
            }
            .catch { emit(ResultadoBusqueda(texto, emptyList(), true)) }
    }

    private fun cambiarPaciente(texto: String) {
        if (_state.value.pacienteElegido) return
        _state.update {
            it.copy(paciente = texto, errorPaciente = null, errorBusqueda = null)
        }
        consulta.value = texto
    }

    private fun elegirPaciente(pacienteId: String) {
        val actual = _state.value
        if (actual.guardando) return
        val sugerido = actual.sugerencias.firstOrNull { it.pacienteId == pacienteId } ?: return
        aplicarPaciente(sugerido)
    }

    private fun quitarPaciente() {
        if (_state.value.guardando) return
        consulta.value = ""
        _state.update {
            it.copy(
                pacienteId = null,
                paciente = "",
                pacienteTelefono = "",
                correo = "",
                sugerencias = emptyList(),
                busquedaResuelta = "",
                errorPaciente = null,
                errorBusqueda = null,
                errorCorreo = null
            )
        }
    }

    private fun aplicarPaciente(sugerido: PacienteSugerido) {
        consulta.value = ""
        _state.update {
            it.copy(
                pacienteId = sugerido.pacienteId,
                paciente = sugerido.nombre,
                pacienteTelefono = sugerido.telefono,
                correo = sugerido.correo,
                sugerencias = emptyList(),
                busquedaResuelta = "",
                errorPaciente = null,
                errorBusqueda = null,
                errorCorreo = null
            )
        }
    }

    private fun cargarPaciente(id: String) {
        _state.update {
            it.copy(cargando = true, mensajeErrorCarga = null, cargaReintentable = false)
        }
        viewModelScope.launch {
            try {
                val paciente = pacienteRepositorio.observarPaciente(id).first()
                if (paciente == null) {
                    mostrarErrorCarga(MENSAJE_PACIENTE_NO_ENCONTRADO, reintentable = false)
                } else {
                    aplicarPaciente(paciente.aSugerido())
                    _state.update { it.copy(cargando = false) }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mostrarErrorCarga(MENSAJE_ERROR_CARGA, reintentable = true)
            }
        }
    }

    private fun mostrarErrorCarga(mensaje: String, reintentable: Boolean) {
        _state.update {
            it.copy(cargando = false, mensajeErrorCarga = mensaje, cargaReintentable = reintentable)
        }
    }

    private fun guardarCita() {
        val datos = _state.value
        if (datos.guardando || datos.cargando || datos.mensajeErrorCarga != null) return

        val fecha = leerFecha(datos.fecha)
        val hora = leerHora(datos.hora)
        val ahora = LocalDateTime.now()
        val fechaHora = if (fecha != null && hora != null) LocalDateTime.of(fecha, hora) else null

        val errorPaciente = if (datos.pacienteId == null && datos.paciente.isBlank()) {
            "Ingresa el nombre del paciente"
        } else null

        val errorFecha = when {
            fecha == null -> "Selecciona la fecha de la cita"
            fecha.isBefore(ahora.toLocalDate()) -> "La fecha no puede ser anterior a hoy"
            else -> null
        }

        val errorHora = when {
            hora == null -> "Selecciona la hora de la cita"
            errorFecha == null && fechaHora != null && fechaHora.isBefore(ahora) ->
                "La hora de la cita ya pasó"
            else -> null
        }

        val errorMotivo = if (datos.motivo.isBlank()) {
            "Ingresa el motivo de la cita"
        } else null

        val errorCorreo = if (
            datos.correo.isNotBlank() &&
            !Patterns.EMAIL_ADDRESS.matcher(datos.correo.trim()).matches()
        ) {
            "Ingresa un correo válido"
        } else null

        _state.update {
            it.copy(
                errorPaciente = errorPaciente,
                errorCorreo = errorCorreo,
                errorFecha = errorFecha,
                errorHora = errorHora,
                errorMotivo = errorMotivo,
                mensajeError = null
            )
        }

        if (listOf(errorPaciente, errorCorreo, errorFecha, errorHora, errorMotivo).any { it != null }) {
            return
        }

        val horario = fechaHora ?: return

        viewModelScope.launch {
            _state.update { it.copy(guardando = true) }

            try {
                val configuracion = configuracionRepositorio.configuracion.first()
                if (configuracion == null) {
                    _state.update { it.copy(mensajeError = MENSAJE_SIN_MEDICO) }
                    return@launch
                }

                val coincidencias = if (datos.pacienteId == null) {
                    val buscado = normalizar(datos.paciente.trim())
                    pacienteRepositorio.buscar(datos.paciente.trim())
                        .first()
                        .filter { normalizar(it.nombreCompleto) == buscado }
                } else {
                    emptyList()
                }

                if (coincidencias.size > 1) {
                    _state.update { it.copy(errorPaciente = MENSAJE_HOMONIMOS) }
                    return@launch
                }

                val vinculado: Paciente? = coincidencias.singleOrNull()
                val pacienteId = datos.pacienteId ?: vinculado?.id
                val nombre = vinculado?.nombreCompleto ?: datos.paciente.trim()
                val correo: String? = datos.correo.trim()
                    .ifBlank { vinculado?.correo.orEmpty() }
                    .trim()
                    .ifBlank { null }

                if (pacienteId == null && correo == null) {
                    _state.update { it.copy(errorCorreo = MENSAJE_CORREO_OBLIGATORIO) }
                    return@launch
                }

                val conflicto = citaRepositorio.buscarConflicto(configuracion.medicoId, horario)
                if (conflicto != null) {
                    _state.update {
                        it.copy(errorHora = "Horario ocupado por ${conflicto.nombrePaciente}")
                    }
                    return@launch
                }

                val cita = Cita(
                    id = "",
                    pacienteId = pacienteId,
                    nombrePaciente = nombre,
                    correoPaciente = correo,
                    medicoId = configuracion.medicoId,
                    fechaHora = horario,
                    duracionMinutos = 30,
                    motivo = datos.motivo.trim(),
                    estadoConfirmacion = EstadoConfirmacion.PENDIENTE,
                    recordatorioEnviado = false
                )

                citaRepositorio.crear(cita)
                _efectos.send(NuevaCitaEfecto.CitaGuardada)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _state.update { it.copy(mensajeError = MENSAJE_ERROR_GUARDAR) }
            } finally {
                _state.update { it.copy(guardando = false) }
            }
        }
    }

    private fun leerFecha(texto: String): LocalDate? = try {
        LocalDate.parse(texto.trim(), formatoFecha)
    } catch (_: DateTimeParseException) {
        null
    }

    private fun leerHora(texto: String): LocalTime? = try {
        LocalTime.parse(texto.trim(), formatoHora)
    } catch (_: DateTimeParseException) {
        null
    }

    private fun normalizar(texto: String): String =
        Normalizer.normalize(texto, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .lowercase()

    private fun enviar(efecto: NuevaCitaEfecto) {
        viewModelScope.launch { _efectos.send(efecto) }
    }

    companion object {
        private const val RETARDO_BUSQUEDA_MS = 250L
        private const val MAXIMO_SUGERENCIAS = 5
        private const val MENSAJE_ERROR_BUSQUEDA = "No se pudo buscar pacientes"
        private const val MENSAJE_ERROR_CARGA = "No se pudo cargar el paciente"
        private const val MENSAJE_PACIENTE_NO_ENCONTRADO = "No se encontró el paciente"
        private const val MENSAJE_SIN_MEDICO = "Configura primero el médico"
        private const val MENSAJE_HOMONIMOS = "Hay varios pacientes con ese nombre. Elige uno de la lista."
        private const val MENSAJE_CORREO_OBLIGATORIO = "El correo es obligatorio para pacientes nuevos"
        private const val MENSAJE_ERROR_GUARDAR = "No se pudo guardar la cita. Inténtalo de nuevo."

        val Factory = viewModelFactory {
            initializer {
                val app = this[
                    ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY
                ] as CliniMedikApp

                NuevaCitaViewModel(
                    createSavedStateHandle(),
                    app.container.citaRepositorio,
                    app.container.pacienteRepositorio,
                    app.container.configuracionRepositorio
                )
            }
        }
    }
}

private data class ResultadoBusqueda(
    val texto: String,
    val sugerencias: List<PacienteSugerido>,
    val fallo: Boolean
)

sealed interface NuevaCitaEfecto {
    data object Regresar : NuevaCitaEfecto
    data object CitaGuardada : NuevaCitaEfecto
}