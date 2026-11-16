package plat.clinimedik.app.ui.screens.recepcion.resultado

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
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
import plat.clinimedik.app.data.model.EstadoCita
import plat.clinimedik.app.data.model.Paciente
import plat.clinimedik.app.data.model.Referencia
import plat.clinimedik.app.data.model.Visita
import plat.clinimedik.app.data.preferencias.ConfiguracionRepositorio
import plat.clinimedik.app.data.repository.CitaRepositorio
import plat.clinimedik.app.data.repository.PacienteRepositorio
import plat.clinimedik.app.data.repository.VisitaRepositorio
import plat.clinimedik.app.ui.components.calcularEdad
import plat.clinimedik.app.ui.components.formatearFecha
import plat.clinimedik.app.ui.components.formatearHora
import plat.clinimedik.app.ui.navigation.Resultado

class ResultadoViewModel(
    savedStateHandle: SavedStateHandle,
    private val pacienteRepositorio: PacienteRepositorio,
    private val visitaRepositorio: VisitaRepositorio,
    private val citaRepositorio: CitaRepositorio,
    private val configuracionRepositorio: ConfiguracionRepositorio
) : ViewModel() {

    private val pacienteId = savedStateHandle.toRoute<Resultado>().pacienteId

    private val _state = MutableStateFlow(ResultadoUiState())
    val state: StateFlow<ResultadoUiState> = _state.asStateFlow()

    private val _efectos = Channel<ResultadoEfecto>()
    val efectos: Flow<ResultadoEfecto> = _efectos.receiveAsFlow()

    private var ultimaVisitaId: String? = null
    private var observacion: Job? = null

    init {
        observarPaciente()
    }

    fun onEvent(event: ResultadoEvent) {
        when (event) {
            ResultadoEvent.VerHistorial -> {
                ultimaVisitaId?.let { enviar(ResultadoEfecto.AbrirDetalle(it)) }
            }

            ResultadoEvent.AgregarAFila -> abrirFormulario()

            is ResultadoEvent.CambiarMotivo ->
                _state.update {
                    it.copy(motivo = event.texto, errorMotivo = null, mensajeError = null)
                }

            ResultadoEvent.CancelarAgregarAFila -> cerrarFormulario()

            ResultadoEvent.ConfirmarAgregarAFila -> agregarAFila()

            ResultadoEvent.Reintentar -> observarPaciente()

            ResultadoEvent.EscanearOtro,
            ResultadoEvent.Regresar -> enviar(ResultadoEfecto.Regresar)
        }
    }

    private fun observarPaciente() {
        observacion?.cancel()
        _state.update {
            it.copy(cargando = true, mensajeErrorCarga = null, cargaReintentable = false)
        }
        observacion = viewModelScope.launch {
            combine(
                pacienteRepositorio.observarPaciente(pacienteId),
                visitaRepositorio.observarHistorial(pacienteId),
                citaRepositorio.observarProximaCita(pacienteId),
                visitaRepositorio.observarUltimaReferencia(pacienteId),
                observarCitaDeHoy()
            ) { paciente, historial, proxima, referencia, deHoy ->
                DatosResultado(paciente, historial, proxima, referencia, deHoy)
            }
                .catch { mostrarErrorCarga(MENSAJE_ERROR_CARGA, reintentable = true) }
                .collect { datos -> aplicar(datos) }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observarCitaDeHoy(): Flow<Cita?> =
        configuracionRepositorio.configuracion
            .map { configuracion -> configuracion?.medicoId }
            .distinctUntilChanged()
            .flatMapLatest { medicoId ->
                if (medicoId == null) {
                    flowOf<Cita?>(null)
                } else {
                    citaRepositorio.observarCitasDelDia(medicoId).map { citas ->
                        citas
                            .filter { it.pacienteId == pacienteId && it.estado == EstadoCita.PROGRAMADA }
                            .minByOrNull { it.fechaHora }
                    }
                }
            }

    private fun aplicar(datos: DatosResultado) {
        val paciente = datos.paciente
        if (paciente == null) {
            mostrarErrorCarga(MENSAJE_PACIENTE_NO_ENCONTRADO, reintentable = false)
            return
        }

        ultimaVisitaId = datos.historial.firstOrNull()?.id

        val resumen = if (datos.historial.isEmpty()) {
            "Paciente nuevo"
        } else {
            val fecha = datos.historial.first().horaLlegada.toLocalDate()
            "${datos.historial.size} visitas · última: ${formatearFecha(fecha)}"
        }

        _state.update { actual ->
            actual.copy(
                cargando = false,
                mensajeErrorCarga = null,
                cargaReintentable = false,
                nombre = paciente.nombreCompleto,
                edad = calcularEdad(paciente.fechaNacimiento, LocalDate.now()).toString(),
                telefono = paciente.telefono,
                aseguradora = paciente.aseguradora?.takeIf { texto -> texto.isNotBlank() },
                resumenVisitas = resumen,
                tieneHistorial = datos.historial.isNotEmpty(),
                proximaCita = datos.proximaCita?.let { cita ->
                    "${formatearFecha(cita.fechaHora.toLocalDate())} · " +
                            "${formatearHora(cita.fechaHora)} · ${cita.motivo}"
                },
                ultimaReferencia = datos.referencia?.let { referencia ->
                    textoReferencia(referencia, datos.historial)
                },
                citaDeHoy = datos.citaDeHoy?.let { cita ->
                    CitaDeHoy(
                        citaId = cita.id,
                        hora = formatearHora(cita.fechaHora),
                        motivo = cita.motivo
                    )
                }
            )
        }
    }

    private fun textoReferencia(referencia: Referencia, historial: List<Visita>): String {
        val fecha = historial
            .firstOrNull { it.id == referencia.visitaId }
            ?.horaLlegada
            ?.toLocalDate()
        val base = "${referencia.especialidad} · ${referencia.destino}"
        return if (fecha == null) base else "$base · ${formatearFecha(fecha)}"
    }

    private fun mostrarErrorCarga(mensaje: String, reintentable: Boolean) {
        _state.update {
            it.copy(cargando = false, mensajeErrorCarga = mensaje, cargaReintentable = reintentable)
        }
    }

    private fun abrirFormulario() {
        if (_state.value.guardando) return
        _state.update {
            it.copy(
                mostrarFormulario = true,
                citaDelDialogo = it.citaDeHoy,
                motivo = "",
                errorMotivo = null,
                mensajeError = null
            )
        }
    }

    private fun cerrarFormulario() {
        if (_state.value.guardando) return
        _state.update {
            it.copy(
                mostrarFormulario = false,
                citaDelDialogo = null,
                motivo = "",
                errorMotivo = null,
                mensajeError = null
            )
        }
    }

    private fun agregarAFila() {
        val datos = _state.value
        if (datos.guardando) return

        val cita = datos.citaDelDialogo
        if (cita == null && datos.motivo.isBlank()) {
            _state.update { it.copy(errorMotivo = MENSAJE_MOTIVO_OBLIGATORIO) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(guardando = true, errorMotivo = null, mensajeError = null) }

            try {
                val visitaId = if (cita != null) {
                    registrarLlegadaDeCita(cita)
                } else {
                    ingresarSinCita(datos.motivo)
                }
                if (visitaId != null) {
                    _efectos.send(ResultadoEfecto.AbrirDetalle(visitaId))
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _state.update { it.copy(mensajeError = MENSAJE_ERROR_AGREGAR) }
            } finally {
                _state.update { it.copy(guardando = false) }
            }
        }
    }

    private suspend fun registrarLlegadaDeCita(cita: CitaDeHoy): String? {
        val vigente = citaRepositorio.obtenerCita(cita.citaId)
        val valida = vigente != null &&
                vigente.estado == EstadoCita.PROGRAMADA &&
                vigente.pacienteId == pacienteId &&
                vigente.fechaHora.toLocalDate() == LocalDate.now()
        if (!valida) {
            _state.update {
                it.copy(
                    mostrarFormulario = false,
                    citaDelDialogo = null,
                    motivo = "",
                    mensajeError = MENSAJE_CITA_CAMBIO
                )
            }
            return null
        }
        val visitaId = citaRepositorio.registrarLlegada(cita.citaId)
        if (visitaId == null) {
            _state.update { it.copy(mensajeError = MENSAJE_ERROR_AGREGAR) }
        }
        return visitaId
    }

    private suspend fun ingresarSinCita(motivo: String): String {
        val configuracion = configuracionRepositorio.configuracion.first()
            ?: throw IllegalStateException("No hay médico configurado")
        val activa = visitaRepositorio.visitaActivaDeHoy(pacienteId, configuracion.medicoId)
        return activa?.id
            ?: visitaRepositorio.agregarAFila(pacienteId, configuracion.medicoId, motivo)
    }

    private fun enviar(efecto: ResultadoEfecto) {
        viewModelScope.launch { _efectos.send(efecto) }
    }

    companion object {
        private const val MENSAJE_ERROR_CARGA = "No se pudo cargar el paciente"
        private const val MENSAJE_PACIENTE_NO_ENCONTRADO = "No se encontró el paciente"
        private const val MENSAJE_MOTIVO_OBLIGATORIO = "Ingresa el motivo de consulta"
        private const val MENSAJE_ERROR_AGREGAR = "No se pudo agregar el paciente a la fila"
        private const val MENSAJE_CITA_CAMBIO = "La cita cambió. Vuelve a intentarlo."

        val Factory = viewModelFactory {
            initializer {
                val app = this[
                    ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY
                ] as CliniMedikApp

                ResultadoViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    pacienteRepositorio = app.container.pacienteRepositorio,
                    visitaRepositorio = app.container.visitaRepositorio,
                    citaRepositorio = app.container.citaRepositorio,
                    configuracionRepositorio = app.container.configuracionRepositorio
                )
            }
        }
    }
}

private data class DatosResultado(
    val paciente: Paciente?,
    val historial: List<Visita>,
    val proximaCita: Cita?,
    val referencia: Referencia?,
    val citaDeHoy: Cita?
)

sealed interface ResultadoEfecto {
    data object Regresar : ResultadoEfecto
    data class AbrirDetalle(val visitaId: String) : ResultadoEfecto
}