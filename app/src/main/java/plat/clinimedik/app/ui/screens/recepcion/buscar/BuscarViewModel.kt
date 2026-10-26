package plat.clinimedik.app.ui.screens.recepcion.buscar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import plat.clinimedik.app.CliniMedikApp
import plat.clinimedik.app.data.repository.PacienteRepositorio

class BuscarViewModel(
    private val pacienteRepositorio: PacienteRepositorio
) : ViewModel() {

    private val _state = MutableStateFlow(BuscarUiState())
    val state: StateFlow<BuscarUiState> = _state.asStateFlow()

    private val _efectos = Channel<BuscarEfecto>()
    val efectos: Flow<BuscarEfecto> = _efectos.receiveAsFlow()

    init {
        observarRecientes()
        observarResultados()
    }

    fun onEvent(event: BuscarEvent) {
        when (event) {
            is BuscarEvent.CambiarTexto -> _state.update { it.copy(texto = event.texto) }
            is BuscarEvent.AbrirPaciente -> enviar(BuscarEfecto.AbrirResultado(event.pacienteId))
            BuscarEvent.NuevoPaciente -> enviar(BuscarEfecto.IrARegistro)
        }
    }

    private fun observarRecientes() {
        viewModelScope.launch {
            pacienteRepositorio.observarPorIds(idsRecientes).collect { pacientes ->
                _state.update { actual ->
                    actual.copy(cargando = false, recientes = pacientes.map { it.aPacienteEncontrado() })
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observarResultados() {
        viewModelScope.launch {
            _state
                .map { it.texto }
                .distinctUntilChanged()
                .flatMapLatest { texto -> pacienteRepositorio.buscar(texto) }
                .collect { pacientes ->
                    _state.update { actual ->
                        actual.copy(resultados = pacientes.map { it.aPacienteEncontrado() })
                    }
                }
        }
    }

    private fun enviar(efecto: BuscarEfecto) {
        viewModelScope.launch { _efectos.send(efecto) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as CliniMedikApp
                BuscarViewModel(app.container.pacienteRepositorio)
            }
        }
    }
}

sealed interface BuscarEfecto {
    data class AbrirResultado(val pacienteId: String) : BuscarEfecto
    data object IrARegistro : BuscarEfecto
}