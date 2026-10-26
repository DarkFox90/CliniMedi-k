package plat.clinimedik.app.ui.screens.recepcion.escaneo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class EscaneoViewModel : ViewModel() {

    private val _state = MutableStateFlow(EscaneoUiState())
    val state: StateFlow<EscaneoUiState> = _state.asStateFlow()

    private val _efectos = Channel<EscaneoEfecto>()
    val efectos: Flow<EscaneoEfecto> = _efectos.receiveAsFlow()

    fun onEvent(event: EscaneoEvent) {
        when (event) {
            EscaneoEvent.Regresar -> enviar(EscaneoEfecto.Regresar)

            EscaneoEvent.SimularLectura -> {
                enviar(EscaneoEfecto.PacienteEncontrado("pac-001"))
            }
        }
    }

    private fun enviar(efecto: EscaneoEfecto) {
        viewModelScope.launch {
            _efectos.send(efecto)
        }
    }
}

sealed interface EscaneoEfecto {
    data object Regresar : EscaneoEfecto
    data class PacienteEncontrado(val pacienteId: String) : EscaneoEfecto
}