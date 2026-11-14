package plat.clinimedik.app.ui.screens.recepcion.fila

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import java.time.LocalDateTime
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import plat.clinimedik.app.CliniMedikApp
import plat.clinimedik.app.data.model.VisitaConPaciente
import plat.clinimedik.app.data.preferencias.ConfiguracionRepositorio
import plat.clinimedik.app.data.repository.MedicoRepositorio
import plat.clinimedik.app.data.repository.VisitaRepositorio
import plat.clinimedik.app.ui.components.DiaVisible
import plat.clinimedik.app.ui.components.SeleccionDia
import plat.clinimedik.app.ui.components.formatearFechaLarga
import plat.clinimedik.app.ui.components.relojPorMinuto

class FilaRecepcionViewModel(
    private val visitaRepositorio: VisitaRepositorio,
    private val medicoRepositorio: MedicoRepositorio,
    private val configuracionRepositorio: ConfiguracionRepositorio
) : ViewModel() {

    private val _state = MutableStateFlow(FilaRecepcionUiState())
    val state: StateFlow<FilaRecepcionUiState> = _state.asStateFlow()

    private val _efectos = Channel<FilaRecepcionEfecto>()
    val efectos: Flow<FilaRecepcionEfecto> = _efectos.receiveAsFlow()

    /** La fila solo muestra hoy o días anteriores. */
    private val seleccionDia = SeleccionDia(permiteFuturo = false)

    private val reintentos = MutableStateFlow(0)

    init {
        observarFila()
    }

    fun onEvent(event: FilaRecepcionEvent) {
        when (event) {
            is FilaRecepcionEvent.SeleccionarFiltro -> _state.update { it.copy(filtro = event.filtro) }
            is FilaRecepcionEvent.AbrirPaciente -> enviar(FilaRecepcionEfecto.AbrirDetalle(event.visitaId))
            is FilaRecepcionEvent.SolicitarUrgencia -> solicitarUrgencia(event.visitaId)
            is FilaRecepcionEvent.CambiarMotivoUrgencia -> _state.update { actual ->
                actual.copy(urgencia = actual.urgencia?.copy(motivo = event.motivo, mensajeError = null))
            }
            FilaRecepcionEvent.ConfirmarUrgencia -> confirmarUrgencia()
            FilaRecepcionEvent.CancelarUrgencia -> _state.update { it.copy(urgencia = null) }
            FilaRecepcionEvent.NuevoPaciente -> enviar(FilaRecepcionEfecto.IrARegistro)
            FilaRecepcionEvent.DiaAnterior -> seleccionDia.anterior()
            FilaRecepcionEvent.DiaSiguiente -> seleccionDia.siguiente()
            is FilaRecepcionEvent.ElegirDia -> seleccionDia.elegir(event.fecha)
            FilaRecepcionEvent.VolverAHoy -> seleccionDia.volverAHoy()
            FilaRecepcionEvent.Reintentar -> {
                _state.update { it.copy(cargando = true, mensajeError = null) }
                reintentos.update { it + 1 }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observarFila() {
        viewModelScope.launch {
            reintentos
                .flatMapLatest {
                    configuracionRepositorio.configuracion
                        .filterNotNull()
                        .flatMapLatest { configuracion ->
                            seleccionDia.dia.flatMapLatest { dia ->
                                combine(
                                    visitaRepositorio.observarFilaDelDia(configuracion.medicoId, dia.fecha),
                                    medicoRepositorio.observarMedico(configuracion.medicoId),
                                    relojPorMinuto()
                                ) { fila, medico, ahora -> DatosFila(dia, fila, medico?.nombre, ahora) }
                            }
                        }
                        .catch {
                            _state.update { it.copy(cargando = false, mensajeError = MENSAJE_ERROR_CARGA) }
                        }
                }
                .collect { datos -> _state.update { actual -> actual.conDatos(datos) } }
        }
    }

    private fun FilaRecepcionUiState.conDatos(datos: DatosFila): FilaRecepcionUiState = copy(
        cargando = false,
        mensajeError = null,
        subtitulo = datos.nombreMedico?.let { "$it · Recepción" }.orEmpty(),
        fecha = datos.dia.fecha,
        textoFecha = formatearFechaLarga(datos.dia.fecha),
        esHoy = datos.dia.esHoy,
        pacientes = datos.fila.map { it.aPacienteEnFila(datos.ahora, esHoy = datos.dia.esHoy) },
        // Un día anterior es de solo consulta: si había un panel de urgencia abierto, se cierra.
        urgencia = urgencia.takeIf { datos.dia.esHoy }
    )

    private fun solicitarUrgencia(visitaId: String) {
        val paciente = _state.value.pacientes.firstOrNull { it.visitaId == visitaId } ?: return
        if (!paciente.puedeMarcarseUrgente) return
        _state.update {
            it.copy(urgencia = SolicitudUrgencia(visitaId = visitaId, nombrePaciente = paciente.nombre))
        }
    }

    private fun confirmarUrgencia() {
        val urgencia = _state.value.urgencia ?: return
        if (urgencia.guardando) return
        if (urgencia.motivo.isBlank()) {
            _state.update {
                it.copy(urgencia = urgencia.copy(mensajeError = "Escribe el motivo de la prioridad"))
            }
            return
        }
        _state.update { it.copy(urgencia = urgencia.copy(guardando = true, mensajeError = null)) }
        viewModelScope.launch {
            try {
                visitaRepositorio.marcarUrgente(urgencia.visitaId, urgencia.motivo)
                _state.update { it.copy(urgencia = null) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                // El panel queda abierto con lo escrito para intentarlo otra vez.
                _state.update {
                    it.copy(urgencia = urgencia.copy(guardando = false, mensajeError = MENSAJE_ERROR_URGENCIA))
                }
            }
        }
    }

    private fun enviar(efecto: FilaRecepcionEfecto) {
        viewModelScope.launch { _efectos.send(efecto) }
    }

    companion object {
        private const val MENSAJE_ERROR_CARGA = "No se pudo cargar la fila"
        private const val MENSAJE_ERROR_URGENCIA = "No se pudo marcar como urgente. Intenta de nuevo."

        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as CliniMedikApp
                FilaRecepcionViewModel(
                    app.container.visitaRepositorio,
                    app.container.medicoRepositorio,
                    app.container.configuracionRepositorio
                )
            }
        }
    }
}

private data class DatosFila(
    val dia: DiaVisible,
    val fila: List<VisitaConPaciente>,
    val nombreMedico: String?,
    val ahora: LocalDateTime
)

sealed interface FilaRecepcionEfecto {
    data class AbrirDetalle(val visitaId: String) : FilaRecepcionEfecto
    data object IrARegistro : FilaRecepcionEfecto
}
