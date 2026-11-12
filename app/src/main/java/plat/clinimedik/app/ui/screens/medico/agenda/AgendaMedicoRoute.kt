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
import plat.clinimedik.app.ui.components.DiaVisible
import plat.clinimedik.app.ui.navigation.AgendaMedico
import plat.clinimedik.app.ui.navigation.FilaMedico
import plat.clinimedik.app.ui.navigation.irAPestana

fun agendaMedicoUiStateDePrueba(diasDesdeHoy: Long = 0): AgendaMedicoUiState {
    val medico = FakeDataSource.medicoActual
    val fecha = FakeDataSource.hoy.plusDays(diasDesdeHoy)
    return calcularAgendaMedico(
        citas = FakeDataSource.citasDelDia(medico.id, fecha),
        nombreMedico = medico.nombre,
        ahora = FakeDataSource.ahora,
        dia = DiaVisible(fecha = fecha, hoy = FakeDataSource.hoy)
    )
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
