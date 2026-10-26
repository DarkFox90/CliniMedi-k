package plat.clinimedik.app.ui.screens.medico.agenda

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import plat.clinimedik.app.data.fake.FakeDataSource
import plat.clinimedik.app.ui.navigation.AgendaMedico
import plat.clinimedik.app.ui.navigation.FilaMedico
import plat.clinimedik.app.ui.navigation.irAPestana

fun agendaMedicoUiStateDePrueba(): AgendaMedicoUiState {
    val medico = FakeDataSource.medicoActual
    return calcularAgendaMedico(FakeDataSource.citasDelDia(medico.id), medico.nombre, FakeDataSource.ahora)
}

@Composable
fun AgendaMedicoRoute(
    viewModel: AgendaMedicoViewModel = viewModel(factory = AgendaMedicoViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AgendaMedicoScreen(state = state, onEvent = viewModel::onEvent)
}

fun NavGraphBuilder.agendaMedicoDestino(navController: NavController) {
    composable<AgendaMedico> {
        BackHandler { navController.irAPestana(FilaMedico) }
        AgendaMedicoRoute()
    }
}