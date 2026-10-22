package plat.clinimedik.app.ui.screens.recepcion.resumen

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
import plat.clinimedik.app.ui.navigation.Resumen

fun resumenUiStateDePrueba(): ResumenUiState {
    val medico = FakeDataSource.medicoActual
    val fila = FakeDataSource.filaDeHoy(medico.id).mapNotNull { visita ->
        FakeDataSource.paciente(visita.pacienteId)?.let { VisitaConPaciente(visita, it) }
    }
    val cobros = fila.mapNotNull { FakeDataSource.cobroDe(it.visita.id) }
    return calcularResumen(fila, cobros, medico.nombre, FakeDataSource.hoy)
}

@Composable
fun ResumenRoute(
    onRegresar: () -> Unit,
    viewModel: ResumenViewModel = viewModel(factory = ResumenViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.efectos.collect { efecto ->
            when (efecto) {
                ResumenEfecto.Regresar -> onRegresar()
            }
        }
    }
    ResumenScreen(state = state, onEvent = viewModel::onEvent)
}

fun NavGraphBuilder.resumenDestino(navController: NavController) {
    composable<Resumen> {
        ResumenRoute(onRegresar = { navController.popBackStack() })
    }
}
