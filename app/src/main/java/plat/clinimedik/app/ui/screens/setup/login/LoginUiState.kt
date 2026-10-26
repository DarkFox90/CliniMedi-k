package plat.clinimedik.app.ui.screens.setup.login

const val MENSAJE_ERROR_LOGIN = "No se pudo iniciar sesión con Google. Revisa tu conexión e intenta de nuevo."

data class LoginUiState(
    val cargando: Boolean = false,
    val mensajeError: String? = null
)