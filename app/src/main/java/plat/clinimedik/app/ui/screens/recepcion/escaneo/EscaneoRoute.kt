package plat.clinimedik.app.ui.screens.recepcion.escaneo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun EscaneoRoute() {
    val state = remember { EscaneoUiState() }
    EscaneoScreen(state = state, onIntent = {})
}