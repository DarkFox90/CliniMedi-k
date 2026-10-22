package plat.clinimedik.app.ui.screens.recepcion.registro

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import plat.clinimedik.app.ui.navigation.Registro

@Composable
fun RegistroRoute() {
    val state = remember { RegistroUiState() }
    RegistroScreen(state = state, onEvent = {})
}

fun NavGraphBuilder.registroDestino(navController: NavController) {
    composable<Registro> {
        RegistroRoute()
    }
}
