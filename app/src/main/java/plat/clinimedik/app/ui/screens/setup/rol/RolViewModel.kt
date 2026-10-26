package plat.clinimedik.app.ui.screens.setup.rol

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import plat.clinimedik.app.data.model.RolDispositivo

class RolViewModel : ViewModel() {

    private val _state = MutableStateFlow(
        RolUiState(opciones = RolDispositivo.entries.map { it.aOpcionRol() })
    )
    val state: StateFlow<RolUiState> = _state.asStateFlow()

    private val _efectos = Channel<RolEfecto>()
    val efectos: Flow<RolEfecto> = _efectos.receiveAsFlow()

    fun onEvent(event: RolEvent) {
        when (event) {
            is RolEvent.SeleccionarRol -> _state.update { it.copy(rolSeleccionado = event.rol) }
            RolEvent.Continuar -> continuar()
        }
    }

    private fun continuar() {
        val rol = _state.value.rolSeleccionado ?: return
        viewModelScope.launch { _efectos.send(RolEfecto.IrASeleccionMedico(rol)) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                RolViewModel()
            }
        }
    }
}

sealed interface RolEfecto {
    data class IrASeleccionMedico(val rol: RolDispositivo) : RolEfecto
}