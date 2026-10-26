
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
import kotlinx.coroutines.flow.collect
import plat.clinimedik.app.data.fake.FakeDataSource
import plat.clinimedik.app.ui.components.formatearFechaLarga
import plat.clinimedik.app.ui.components.formatearHora
import plat.clinimedik.app.ui.navigation.AgendaRecepcion
import plat.clinimedik.app.ui.navigation.Inicio
import plat.clinimedik.app.ui.navigation.NuevaCita
import plat.clinimedik.app.ui.navigation.irAPestana

fun agendaUiStateDePrueba(): AgendaRecepcionUiState {
    val medico = FakeDataSource.medicoActual
    val elementos = FakeDataSource.citasDelDia(medico.id).map { cita ->
        ElementoCita(
            citaId = cita.id,
            hora = formatearHora(cita.fechaHora),
            nombrePaciente = cita.nombrePaciente,
            motivo = cita.motivo,
            estadoConfirmacion = cita.estadoConfirmacion,
            recordatorioEnviado = cita.recordatorioEnviado
        )
    }

    return AgendaRecepcionUiState(
        cargando = false,
        subtitulo = formatearFechaLarga(FakeDataSource.hoy),
        elementos = elementos
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
                navController.navigate(NuevaCita)
            }
        )
    }
}
