package plat.clinimedik.app.ui.screens.setup.login

sealed interface LoginEvent {
    data object ContinuarConGoogle : LoginEvent
    data class TokenRecibido(val idToken: String) : LoginEvent
    data class ErrorGoogle(val mensaje: String) : LoginEvent
    data object ContinuarSinCuenta : LoginEvent
}