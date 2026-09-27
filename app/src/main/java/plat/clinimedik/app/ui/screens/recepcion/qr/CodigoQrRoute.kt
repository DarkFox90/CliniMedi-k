package plat.clinimedik.app.ui.screens.recepcion.qr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import plat.clinimedik.app.data.fake.FakeDataSource

@Composable
fun CodigoQrRoute() {
    val paciente = FakeDataSource.paciente("pac-006")
    val state = remember {
        CodigoQrUiState(
            nombrePaciente = paciente?.nombreCompleto.orEmpty()
        )
    }

    CodigoQrScreen(state = state, onIntent = {})
}