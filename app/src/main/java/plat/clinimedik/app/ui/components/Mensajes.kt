package plat.clinimedik.app.ui.components

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

interface Mensajes {
    fun mostrar(texto: String)
}

class MensajesSnackbar(
    private val estado: SnackbarHostState,
    private val scope: CoroutineScope
) : Mensajes {

    override fun mostrar(texto: String) {
        scope.launch {
            estado.currentSnackbarData?.dismiss()
            estado.showSnackbar(
                message = texto,
                duration = SnackbarDuration.Short
            )
        }
    }
}

private object MensajesVacios : Mensajes {
    override fun mostrar(texto: String) {}
}

val LocalMensajes = staticCompositionLocalOf<Mensajes> { MensajesVacios }