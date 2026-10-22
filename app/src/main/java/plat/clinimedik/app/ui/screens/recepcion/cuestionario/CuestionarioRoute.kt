package plat.clinimedik.app.ui.screens.recepcion.cuestionario

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import plat.clinimedik.app.ui.navigation.Cuestionario

@Composable
fun CuestionarioRoute() {
    val state = remember { CuestionarioUiState() }
    CuestionarioScreen(state = state, onEvent = {})
}

fun NavGraphBuilder.cuestionarioDestino(navController: NavController) {
    composable<Cuestionario> {
        CuestionarioRoute()
    }
}
