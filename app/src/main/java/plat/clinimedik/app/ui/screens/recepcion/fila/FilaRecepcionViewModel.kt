package plat.clinimedik.app.ui.screens.recepcion.fila

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import java.time.LocalDateTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import plat.clinimedik.app.CliniMedikApp
import plat.clinimedik.app.data.preferencias.ConfiguracionRepositorio
import plat.clinimedik.app.data.repository.MedicoRepositorio
import plat.clinimedik.app.data.repository.VisitaRepositorio

class FilaRecepcionViewModel(
    private val visitaRepositorio: VisitaRepositorio,
    private val medicoRepositorio: MedicoRepositorio,
    private val configuracionRepositorio: ConfiguracionRepositorio
) : ViewModel() {

    private val _state = MutableStateFlow(FilaRecepcionUiState())
    val state: StateFlow<FilaRecepcionUiState> = _state.asStateFlow()

    private val _efectos = Channel<FilaRecepcionEfecto>()
    val efectos: Flow<FilaRecepcionEfecto> = _efectos.receiveAsFlow()

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
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observarFila() {
        viewModelScope.launch {
            configuracionRepositorio.configuracion
                .filterNotNull()
                .flatMapLatest { configuracion ->
                    combine(
                        visitaRepositorio.observarFilaDelDia(configuracion.medicoId),
                        medicoRepositorio.observarMedico(configuracion.medicoId)
                    ) { fila, medico -> fila to medico }
                }
                .collect { (fila, medico) ->
                    val ahora = LocalDateTime.now()
                    _state.update { actual ->
                        actual.copy(
                            cargando = false,
                            subtitulo = medico?.let { "${it.nombre} · Recepción" }.orEmpty(),
                            pacientes = fila.map { it.aPacienteEnFila(ahora) }
                        )
                    }
                }
        }
    }

    private fun solicitarUrgencia(visitaId: String) {
        val paciente = _state.value.pacientes.firstOrNull { it.visitaId == visitaId } ?: return
        _state.update {
            it.copy(urgencia = SolicitudUrgencia(visitaId = visitaId, nombrePaciente = paciente.nombre))
        }
    }

    private fun confirmarUrgencia() {
        val urgencia = _state.value.urgencia ?: return
        if (urgencia.motivo.isBlank()) {
            _state.update {
                it.copy(urgencia = urgencia.copy(mensajeError = "Escribe el motivo de la prioridad"))
            }
            return
        }
        _state.update { it.copy(urgencia = null) }
        viewModelScope.launch {
            visitaRepositorio.marcarUrgente(urgencia.visitaId, urgencia.motivo)
        }
    }

    private fun enviar(efecto: FilaRecepcionEfecto) {
        viewModelScope.launch { _efectos.send(efecto) }
    }

    companion object {
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

sealed interface FilaRecepcionEfecto {
    data class AbrirDetalle(val visitaId: String) : FilaRecepcionEfecto
    data object IrARegistro : FilaRecepcionEfecto
}
