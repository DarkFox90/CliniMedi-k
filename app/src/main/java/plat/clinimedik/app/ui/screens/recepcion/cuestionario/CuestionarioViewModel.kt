package plat.clinimedik.app.ui.screens.recepcion.cuestionario

import android.util.Patterns
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import plat.clinimedik.app.CliniMedikApp
import plat.clinimedik.app.data.model.Cita
import plat.clinimedik.app.data.model.EstadoCita
import plat.clinimedik.app.data.model.EstadoCivil
import plat.clinimedik.app.data.model.Paciente
import plat.clinimedik.app.data.preferencias.ConfiguracionRepositorio
import plat.clinimedik.app.data.repository.CitaRepositorio
import plat.clinimedik.app.data.repository.PacienteRepositorio
import plat.clinimedik.app.data.repository.VisitaRepositorio
import plat.clinimedik.app.ui.navigation.Cuestionario

class CuestionarioViewModel(
    savedStateHandle: SavedStateHandle,
    private val pacienteRepositorio: PacienteRepositorio,
    private val citaRepositorio: CitaRepositorio,
    private val visitaRepositorio: VisitaRepositorio,
    private val configuracionRepositorio: ConfiguracionRepositorio
) : ViewModel() {

    private val citaId: String? = savedStateHandle.toRoute<Cuestionario>().citaId

    private val _state = MutableStateFlow(CuestionarioUiState(cargando = citaId != null))
    val state: StateFlow<CuestionarioUiState> = _state.asStateFlow()

    private val _efectos = Channel<CuestionarioEfecto>()
    val efectos: Flow<CuestionarioEfecto> = _efectos.receiveAsFlow()

    private val formatoFecha = DateTimeFormatter
        .ofPattern("dd/MM/uuuu")
        .withResolverStyle(ResolverStyle.STRICT)

    private var cita: Cita? = null
    private var pacienteRegistradoId: String? = null

    init {
        citaId?.let { cargarCita(it) }
    }

    fun onEvent(event: CuestionarioEvent) {
        when (event) {
            is CuestionarioEvent.CambiarNombre ->
                _state.update { it.copy(nombre = event.texto, errorNombre = null) }

            is CuestionarioEvent.CambiarFechaNacimiento ->
                _state.update {
                    it.copy(fechaNacimiento = event.texto, errorFechaNacimiento = null)
                }

            is CuestionarioEvent.SeleccionarEstadoCivil ->
                _state.update { it.copy(estadoCivil = event.estado) }

            is CuestionarioEvent.CambiarOcupacion ->
                _state.update { it.copy(ocupacion = event.texto) }

            is CuestionarioEvent.CambiarTelefono ->
                _state.update { it.copy(telefono = event.texto, errorTelefono = null) }

            is CuestionarioEvent.CambiarCorreo ->
                _state.update { it.copy(correo = event.texto, errorCorreo = null) }

            is CuestionarioEvent.CambiarMotivo ->
                _state.update { it.copy(motivo = event.texto, errorMotivo = null) }

            is CuestionarioEvent.CambiarAlergias ->
                _state.update { it.copy(alergias = event.texto) }

            is CuestionarioEvent.CambiarEnfermedades ->
                _state.update { it.copy(enfermedades = event.texto) }

            is CuestionarioEvent.CambiarTratamientos ->
                _state.update { it.copy(tratamientos = event.texto) }

            is CuestionarioEvent.CambiarCirugias ->
                _state.update { it.copy(cirugias = event.texto) }

            is CuestionarioEvent.CambiarTieneSeguro ->
                _state.update { it.copy(tieneSeguro = event.activo, errorAseguradora = null) }

            is CuestionarioEvent.CambiarAseguradora ->
                _state.update { it.copy(aseguradora = event.texto, errorAseguradora = null) }

            is CuestionarioEvent.CambiarPoliza ->
                _state.update { it.copy(numeroPoliza = event.texto) }

            is CuestionarioEvent.CambiarTitular ->
                _state.update { it.copy(titularPoliza = event.texto) }

            CuestionarioEvent.Guardar -> guardarPaciente()

            CuestionarioEvent.Reintentar -> citaId?.let { cargarCita(it) }

            CuestionarioEvent.Regresar -> solicitarSalida()

            CuestionarioEvent.ConfirmarSalida -> {
                _state.update { it.copy(confirmandoSalida = false) }
                enviar(CuestionarioEfecto.Regresar)
            }

            CuestionarioEvent.CancelarSalida ->
                _state.update { it.copy(confirmandoSalida = false) }
        }
    }

    private fun solicitarSalida() {
        val actual = _state.value
        when {
            actual.guardando -> Unit
            actual.registroParcial -> _state.update { it.copy(confirmandoSalida = true) }
            else -> enviar(CuestionarioEfecto.Regresar)
        }
    }

    private fun cargarCita(id: String) {
        _state.update {
            it.copy(cargando = true, mensajeErrorCarga = null, cargaReintentable = false)
        }
        viewModelScope.launch {
            try {
                val encontrada = citaRepositorio.obtenerCita(id)
                if (encontrada == null || encontrada.estado != EstadoCita.PROGRAMADA) {
                    mostrarErrorCarga(MENSAJE_CITA_NO_DISPONIBLE, reintentable = false)
                    return@launch
                }
                if (encontrada.pacienteId != null) {
                    mostrarErrorCarga(MENSAJE_CITA_CON_PACIENTE, reintentable = false)
                    return@launch
                }
                cita = encontrada
                _state.update {
                    it.copy(
                        cargando = false,
                        nombre = encontrada.nombrePaciente,
                        correo = encontrada.correoPaciente.orEmpty(),
                        motivo = encontrada.motivo
                    )
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

    private fun guardarPaciente() {
        val datos = _state.value
        if (datos.guardando || datos.cargando || datos.mensajeErrorCarga != null) return

        val fecha = try {
            LocalDate.parse(datos.fechaNacimiento.trim(), formatoFecha)
        } catch (_: DateTimeParseException) {
            null
        }

        val errorNombre = if (datos.nombre.isBlank()) {
            "El nombre es obligatorio"
        } else null

        val errorFecha = when {
            datos.fechaNacimiento.isBlank() ->
                "La fecha de nacimiento es obligatoria"
            fecha == null ->
                "Ingresa una fecha válida (dd/mm/aaaa)"
            fecha.isAfter(LocalDate.now()) ->
                "La fecha no puede ser futura"
            else -> null
        }

        val telefono = datos.telefono.filter { it.isDigit() }
        val errorTelefono = if (telefono.length != 8) {
            "Ingresa un teléfono de 8 dígitos"
        } else null

        val errorCorreo = if (
            datos.correo.isNotBlank() &&
            !Patterns.EMAIL_ADDRESS.matcher(datos.correo.trim()).matches()
        ) {
            "El formato del correo no es válido"
        } else null

        val errorMotivo = if (datos.motivo.isBlank()) {
            "El motivo de consulta es obligatorio"
        } else null

        val errorAseguradora = if (datos.tieneSeguro && datos.aseguradora.isBlank()) {
            "Ingresa la aseguradora"
        } else null

        _state.update {
            it.copy(
                errorNombre = errorNombre,
                errorFechaNacimiento = errorFecha,
                errorTelefono = errorTelefono,
                errorCorreo = errorCorreo,
                errorMotivo = errorMotivo,
                errorAseguradora = errorAseguradora,
                mensajeError = null
            )
        }

        if (listOf(
                errorNombre, errorFecha, errorTelefono,
                errorCorreo, errorMotivo, errorAseguradora
            ).any { it != null }
        ) return

        viewModelScope.launch {
            _state.update { it.copy(guardando = true) }

            try {
                val citaOrigen = cita
                val medicoId = citaOrigen?.medicoId
                    ?: configuracionRepositorio.configuracion.first()?.medicoId
                    ?: throw IllegalStateException("No hay médico configurado")

                val pacienteId = pacienteRegistradoId ?: run {
                    val paciente = Paciente(
                        id = "",
                        nombreCompleto = datos.nombre.trim(),
                        fechaNacimiento = requireNotNull(fecha),
                        telefono = telefono,
                        correo = datos.correo.trim(),
                        estadoCivil = datos.estadoCivil ?: EstadoCivil.SOLTERO,
                        ocupacion = datos.ocupacion.trim(),
                        alergias = datos.alergias.trim(),
                        enfermedadesPrevias = datos.enfermedades.trim(),
                        tratamientosPrevios = datos.tratamientos.trim(),
                        cirugias = datos.cirugias.trim(),
                        fechaRegistro = LocalDateTime.now(),
                        aseguradora = datos.aseguradora.trim().takeIf { datos.tieneSeguro },
                        numeroPoliza = datos.numeroPoliza.trim()
                            .takeIf { datos.tieneSeguro && it.isNotEmpty() },
                        titularPoliza = datos.titularPoliza.trim()
                            .takeIf { datos.tieneSeguro && it.isNotEmpty() }
                    )
                    pacienteRepositorio.registrarConVisita(
                        paciente = paciente,
                        motivo = datos.motivo.trim(),
                        medicoId = medicoId
                    ).also { pacienteRegistradoId = it }
                }

                if (citaOrigen != null) {
                    val vigente = citaRepositorio.obtenerCita(citaOrigen.id)
                    val sigueLibre = vigente != null &&
                            vigente.estado == EstadoCita.PROGRAMADA &&
                            vigente.pacienteId == null
                    if (sigueLibre) {
                        val visita = visitaRepositorio.visitaActivaDeHoy(pacienteId, medicoId)
                            ?: throw IllegalStateException("No se encontró la visita del paciente")
                        citaRepositorio.enlazarPaciente(citaOrigen.id, pacienteId, visita.id)
                    }
                }

                _state.update { it.copy(registroParcial = false) }
                _efectos.send(CuestionarioEfecto.IrACodigoQr(pacienteId))

            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                val parcial = pacienteRegistradoId != null
                _state.update {
                    it.copy(
                        mensajeError = if (parcial) MENSAJE_ERROR_ENLACE else MENSAJE_ERROR_REGISTRO,
                        registroParcial = parcial
                    )
                }
            } finally {
                _state.update { it.copy(guardando = false) }
            }
        }
    }

    private fun enviar(efecto: CuestionarioEfecto) {
        viewModelScope.launch { _efectos.send(efecto) }
    }

    companion object {
        private const val MENSAJE_ERROR_REGISTRO = "No se pudo registrar el paciente"
        private const val MENSAJE_ERROR_ENLACE =
            "El paciente se registró, pero no se pudo enlazar la cita. Toca guardar para reintentar."
        private const val MENSAJE_ERROR_CARGA = "No se pudo cargar la cita"
        private const val MENSAJE_CITA_NO_DISPONIBLE = "Esta cita ya no está disponible"
        private const val MENSAJE_CITA_CON_PACIENTE = "Esta cita ya tiene un paciente registrado"

        val Factory = viewModelFactory {
            initializer {
                val app = this[
                    ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY
                ] as CliniMedikApp

                CuestionarioViewModel(
                    createSavedStateHandle(),
                    app.container.pacienteRepositorio,
                    app.container.citaRepositorio,
                    app.container.visitaRepositorio,
                    app.container.configuracionRepositorio
                )
            }
        }
    }
}

sealed interface CuestionarioEfecto {
    data object Regresar : CuestionarioEfecto
    data class IrACodigoQr(val pacienteId: String) : CuestionarioEfecto
}