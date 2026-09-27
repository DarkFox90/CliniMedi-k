package plat.clinimedik.app.ui.screens.recepcion.registro

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun RegistroRoute() {
    val state = remember { RegistroUiState() }
    RegistroScreen(state = state, onIntent = {})
}