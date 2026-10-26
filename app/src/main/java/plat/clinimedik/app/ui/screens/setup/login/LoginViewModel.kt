package plat.clinimedik.app.ui.screens.setup.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    private val _efectos = Channel<LoginEfecto>()
    val efectos: Flow<LoginEfecto> = _efectos.receiveAsFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            LoginEvent.ContinuarConGoogle -> {
                _state.update { it.copy(cargando = true, mensajeError = null) }
                enviar(LoginEfecto.AbrirSelectorGoogle)
            }
            is LoginEvent.TokenRecibido -> iniciarSesion(event.idToken)
            is LoginEvent.ErrorGoogle -> mostrarError(event.mensaje)
            LoginEvent.ContinuarSinCuenta -> enviar(LoginEfecto.IrARol)
        }
    }

    private fun iniciarSesion(idToken: String) {
        val credencial = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credencial)
            .addOnSuccessListener { enviar(LoginEfecto.IrARol) }
            .addOnFailureListener { mostrarError(MENSAJE_ERROR_LOGIN) }
    }

    private fun mostrarError(mensaje: String) {
        _state.update { it.copy(cargando = false, mensajeError = mensaje) }
    }

    private fun enviar(efecto: LoginEfecto) {
        viewModelScope.launch { _efectos.send(efecto) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                LoginViewModel(FirebaseAuth.getInstance())
            }
        }
    }
}

sealed interface LoginEfecto {
    data object AbrirSelectorGoogle : LoginEfecto
    data object IrARol : LoginEfecto
}