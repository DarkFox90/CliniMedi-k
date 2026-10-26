package plat.clinimedik.app.ui.screens.setup.arranque

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.auth.FirebaseAuth
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
import plat.clinimedik.app.data.model.RolDispositivo
import plat.clinimedik.app.data.preferencias.ConfiguracionRepositorio
import plat.clinimedik.app.data.repository.MedicoRepositorio

class ArranqueViewModel(
    private val configuracionRepositorio: ConfiguracionRepositorio,
    private val medicoRepositorio: MedicoRepositorio,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow(ArranqueUiState())
    val state: StateFlow<ArranqueUiState> = _state.asStateFlow()

    private val _efectos = Channel<ArranqueEfecto>()
    val efectos: Flow<ArranqueEfecto> = _efectos.receiveAsFlow()

    init {
        decidirDestino()
    }

    fun onEvent(event: ArranqueEvent) {
        when (event) {
            ArranqueEvent.Reintentar -> decidirDestino()
        }
    }

    private fun decidirDestino() {
        _state.update { it.copy(cargando = true, mensajeError = null) }
        viewModelScope.launch {
            try {
                _efectos.send(buscarDestino())
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _state.update {
                    it.copy(cargando = false, mensajeError = "No se pudo leer la configuración del dispositivo")
                }
            }
        }
    }

    private suspend fun buscarDestino(): ArranqueEfecto {
        val configuracion = configuracionRepositorio.configuracion.first()
        if (configuracion != null) {
            val medico = medicoRepositorio.observarMedico(configuracion.medicoId).first()
            if (medico != null) {
                return when (configuracion.rol) {
                    RolDispositivo.RECEPCION -> ArranqueEfecto.IrARecepcion
                    RolDispositivo.MEDICO -> ArranqueEfecto.IrAMedico
                }
            }
        }
        return if (firebaseAuth.currentUser != null) ArranqueEfecto.IrARol else ArranqueEfecto.IrALogin
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as CliniMedikApp
                ArranqueViewModel(
                    app.container.configuracionRepositorio,
                    app.container.medicoRepositorio,
                    FirebaseAuth.getInstance()
                )
            }
        }
    }
}

sealed interface ArranqueEfecto {
    data object IrALogin : ArranqueEfecto
    data object IrARol : ArranqueEfecto
    data object IrARecepcion : ArranqueEfecto
    data object IrAMedico : ArranqueEfecto
}