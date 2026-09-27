package plat.clinimedik.app.ui.screens.recepcion.cuestionario

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun CuestionarioRoute() {
    val state = remember { CuestionarioUiState() }
    CuestionarioScreen(state = state, onIntent = {})
}