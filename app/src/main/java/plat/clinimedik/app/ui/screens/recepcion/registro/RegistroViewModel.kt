
package plat.clinimedik.app.ui.screens.recepcion.registro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class RegistroViewModel : ViewModel() {

    private val _state = MutableStateFlow(RegistroUiState())
    val state: StateFlow<RegistroUiState> = _state.asStateFlow()

    private val _efectos = Channel<RegistroEfecto>()
    val efectos: Flow<RegistroEfecto> = _efectos.receiveAsFlow()

    fun onEvent(event: RegistroEvent) {
        when (event) {
            RegistroEvent.RegistrarNuevo -> enviar(RegistroEfecto.IrACuestionario)
            RegistroEvent.EscanearQr -> enviar(RegistroEfecto.IrAEscaneo)
            RegistroEvent.Regresar -> enviar(RegistroEfecto.Regresar)
        }
    }

    private fun enviar(efecto: RegistroEfecto) {
        viewModelScope.launch {
            _efectos.send(efecto)
        }
    }
}

sealed interface RegistroEfecto {
    data object IrACuestionario : RegistroEfecto
    data object IrAEscaneo : RegistroEfecto
    data object Regresar : RegistroEfecto
}

