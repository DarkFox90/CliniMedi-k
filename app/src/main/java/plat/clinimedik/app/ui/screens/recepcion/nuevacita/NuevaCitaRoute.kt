package plat.clinimedik.app.ui.screens.recepcion.nuevacita

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun NuevaCitaRoute() {
    val state = remember { NuevaCitaUiState() }
    NuevaCitaScreen(state = state, onIntent = {})
}