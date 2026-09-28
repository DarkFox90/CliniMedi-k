package plat.clinimedik.app.ui.screens.setup.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun LoginRoute() {
    val state = remember { LoginUiState() }
    LoginScreen(state = state, onIntent = {})
}
