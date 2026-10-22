package plat.clinimedik.app.ui.screens.recepcion.nuevacita

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import plat.clinimedik.app.ui.navigation.NuevaCita

@Composable
fun NuevaCitaRoute() {
    val state = remember { NuevaCitaUiState() }
    NuevaCitaScreen(state = state, onEvent = {})
}

fun NavGraphBuilder.nuevaCitaDestino(navController: NavController) {
    composable<NuevaCita> {
        NuevaCitaRoute()
    }
}
