package plat.clinimedik.app.ui.screens.recepcion.agenda

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import plat.clinimedik.app.data.fake.FakeDataSource
import plat.clinimedik.app.ui.components.DiaVisible
import plat.clinimedik.app.ui.navigation.AgendaRecepcion
import plat.clinimedik.app.ui.navigation.Inicio
import plat.clinimedik.app.ui.navigation.NuevaCita
import plat.clinimedik.app.ui.navigation.irAPestana

fun agendaUiStateDePrueba(diasDesdeHoy: Long = 0): AgendaRecepcionUiState {
    val medico = FakeDataSource.medicoActual
    val fecha = FakeDataSource.hoy.plusDays(diasDesdeHoy)
    return calcularAgendaRecepcion(
        citas = FakeDataSource.citasDelDia(medico.id, fecha),
        dia = DiaVisible(fecha = fecha, hoy = FakeDataSource.hoy)
    )
}

@Composable
fun AgendaRecepcionRoute(
    onNuevaCita: () -> Unit,
    viewModel: AgendaRecepcionViewModel = viewModel(
        factory = AgendaRecepcionViewModel.Factory
    )
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.efectos.collect { efecto ->
            when (efecto) {
                AgendaRecepcionEfecto.IrANuevaCita -> onNuevaCita()
            }
        }
    }

    AgendaRecepcionScreen(
        state = state,
        onEvent = viewModel::onEvent
    )
}

fun NavGraphBuilder.agendaRecepcionDestino(navController: NavController) {
    composable<AgendaRecepcion> {
        BackHandler {
            navController.irAPestana(Inicio)
        }

        AgendaRecepcionRoute(
            onNuevaCita = {
                navController.navigate(NuevaCita())
            }
        )
    }
}
