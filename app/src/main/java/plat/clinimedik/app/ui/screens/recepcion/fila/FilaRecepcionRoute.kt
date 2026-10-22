package plat.clinimedik.app.ui.screens.recepcion.fila

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
import plat.clinimedik.app.data.model.VisitaConPaciente
import plat.clinimedik.app.ui.navigation.Detalle
import plat.clinimedik.app.ui.navigation.Fila
import plat.clinimedik.app.ui.navigation.Inicio
import plat.clinimedik.app.ui.navigation.Registro
import plat.clinimedik.app.ui.navigation.irAPestana

fun filaRecepcionUiStateDePrueba(filtro: FiltroFila = FiltroFila.TODOS): FilaRecepcionUiState {
    val medico = FakeDataSource.medicoActual
    val pacientes = FakeDataSource.filaDeHoy(medico.id).mapNotNull { visita ->
        FakeDataSource.paciente(visita.pacienteId)?.let { paciente ->
            VisitaConPaciente(visita, paciente).aPacienteEnFila(FakeDataSource.ahora)
        }
    }
    return FilaRecepcionUiState(
        cargando = false,
        subtitulo = "${medico.nombre} · Recepción",
        filtro = filtro,
        pacientes = pacientes
    )
}

@Composable
fun FilaRecepcionRoute(
    onAbrirDetalle: (String) -> Unit,
    onNuevoPaciente: () -> Unit,
    viewModel: FilaRecepcionViewModel = viewModel(factory = FilaRecepcionViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.efectos.collect { efecto ->
            when (efecto) {
                is FilaRecepcionEfecto.AbrirDetalle -> onAbrirDetalle(efecto.visitaId)
                FilaRecepcionEfecto.IrARegistro -> onNuevoPaciente()
            }
        }
    }
    FilaRecepcionScreen(state = state, onEvent = viewModel::onEvent)
}

fun NavGraphBuilder.filaRecepcionDestino(navController: NavController) {
    composable<Fila> {
        BackHandler { navController.irAPestana(Inicio) }
        FilaRecepcionRoute(
            onAbrirDetalle = { visitaId -> navController.navigate(Detalle(visitaId)) },
            onNuevoPaciente = { navController.navigate(Registro) }
        )
    }
}
