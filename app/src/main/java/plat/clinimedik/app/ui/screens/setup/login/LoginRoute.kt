package plat.clinimedik.app.ui.screens.setup.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import plat.clinimedik.app.ui.navigation.Login

@Composable
fun LoginRoute() {
    val state = remember { LoginUiState() }
    LoginScreen(state = state, onEvent = {})
}

fun NavGraphBuilder.loginDestino(navController: NavController) {
    composable<Login> {
        LoginRoute()
    }
}
