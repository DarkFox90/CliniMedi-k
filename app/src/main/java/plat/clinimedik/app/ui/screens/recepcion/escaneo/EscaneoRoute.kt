package plat.clinimedik.app.ui.screens.recepcion.escaneo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import plat.clinimedik.app.ui.navigation.Escaneo

@Composable
fun EscaneoRoute() {
    val state = remember { EscaneoUiState() }
    EscaneoScreen(state = state, onEvent = {})
}

fun NavGraphBuilder.escaneoDestino(navController: NavController) {
    composable<Escaneo> {
        EscaneoRoute()
    }
}
