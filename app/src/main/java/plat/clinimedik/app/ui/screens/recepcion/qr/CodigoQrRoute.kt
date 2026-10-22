package plat.clinimedik.app.ui.screens.recepcion.qr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import plat.clinimedik.app.data.fake.FakeDataSource
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import plat.clinimedik.app.ui.navigation.CodigoQr

@Composable
fun CodigoQrRoute() {
    val paciente = FakeDataSource.paciente("pac-006")
    val state = remember {
        CodigoQrUiState(
            nombrePaciente = paciente?.nombreCompleto.orEmpty()
        )
    }

    CodigoQrScreen(state = state, onEvent = {})
}

fun NavGraphBuilder.codigoQrDestino(navController: NavController) {
    composable<CodigoQr> {
        CodigoQrRoute()
    }
}
