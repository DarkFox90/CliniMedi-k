package plat.clinimedik.app.ui.screens.medico.historial

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import java.time.LocalDateTime
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import plat.clinimedik.app.CliniMedikApp
import plat.clinimedik.app.data.repository.PacienteRepositorio
import plat.clinimedik.app.data.repository.VisitaRepositorio
import plat.clinimedik.app.ui.navigation.HistorialMedico

class HistorialMedicoViewModel(
    savedStateHandle: SavedStateHandle,
    private val pacienteRepositorio: PacienteRepositorio,
    private val visitaRepositorio: VisitaRepositorio
) : ViewModel() {

    private val pacienteId: String = savedStateHandle.toRoute<HistorialMedico>().pacienteId

    private val _state = MutableStateFlow(HistorialMedicoUiState())
    val state: StateFlow<HistorialMedicoUiState> = _state.asStateFlow()

    private val _efectos = Channel<HistorialMedicoEfecto>()
    val efectos: Flow<HistorialMedicoEfecto> = _efectos.receiveAsFlow()

    init {
        observarHistorial()
    }

    fun onEvent(event: HistorialMedicoEvent) {
        when (event) {
            HistorialMedicoEvent.Regresar -> viewModelScope.launch { _efectos.send(HistorialMedicoEfecto.Regresar) }
            is HistorialMedicoEvent.AlternarVisita -> _state.update { actual ->
                val expandidas = if (event.visitaId in actual.visitasExpandidas) {
                    actual.visitasExpandidas - event.visitaId
                } else {
                    actual.visitasExpandidas + event.visitaId
                }
                actual.copy(visitasExpandidas = expandidas)
            }
        }
    }

    private fun observarHistorial() {
        viewModelScope.launch {
            combine(
                pacienteRepositorio.observarPaciente(pacienteId),
                visitaRepositorio.observarHistorial(pacienteId),
                visitaRepositorio.observarRecetasDePaciente(pacienteId),
                visitaRepositorio.observarReferenciasDePaciente(pacienteId)
            ) { paciente, historial, recetas, referencias ->
                paciente?.let { calcularHistorial(it, historial, recetas, referencias, LocalDateTime.now()) }
                    ?: HistorialMedicoUiState(cargando = false)
            }.collect { historial ->
                _state.update { actual -> historial.copy(visitasExpandidas = actual.visitasExpandidas) }
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as CliniMedikApp
                HistorialMedicoViewModel(
                    createSavedStateHandle(),
                    app.container.pacienteRepositorio,
                    app.container.visitaRepositorio
                )
            }
        }
    }
}

sealed interface HistorialMedicoEfecto {
    data object Regresar : HistorialMedicoEfecto
}