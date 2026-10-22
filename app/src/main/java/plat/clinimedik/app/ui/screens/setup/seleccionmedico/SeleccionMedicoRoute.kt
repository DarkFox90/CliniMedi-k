package plat.clinimedik.app.ui.screens.setup.seleccionmedico

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import plat.clinimedik.app.data.fake.FakeDataSource
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import plat.clinimedik.app.ui.navigation.SeleccionMedico

fun seleccionMedicoUiStateDePrueba(eleccion: MedicoElegido? = null): SeleccionMedicoUiState =
    SeleccionMedicoUiState(
        medicos = FakeDataSource.medicos,
        eleccion = eleccion
    )

@Composable
fun SeleccionMedicoRoute() {
    val state = remember { seleccionMedicoUiStateDePrueba() }
    SeleccionMedicoScreen(state = state, onEvent = {})
}

fun NavGraphBuilder.seleccionMedicoDestino(navController: NavController) {
    composable<SeleccionMedico> {
        SeleccionMedicoRoute()
    }
}
