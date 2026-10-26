package plat.clinimedik.app.ui.screens.medico.historial

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import plat.clinimedik.app.data.fake.FakeDataSource
import plat.clinimedik.app.ui.navigation.HistorialMedico

fun historialMedicoUiStateDePrueba(
    pacienteId: String = "pac-001",
    visitasExpandidas: Set<String> = emptySet()
): HistorialMedicoUiState {
    val paciente = FakeDataSource.paciente(pacienteId) ?: return HistorialMedicoUiState(cargando = false)
    return calcularHistorial(
        paciente = paciente,
        historial = FakeDataSource.historialDe(pacienteId),
        recetas = FakeDataSource.recetas,
        referencias = FakeDataSource.referencias,
        ahora = FakeDataSource.ahora
    ).copy(visitasExpandidas = visitasExpandidas)
}

@Composable
fun HistorialMedicoRoute(
    onRegresar: () -> Unit,
    viewModel: HistorialMedicoViewModel = viewModel(factory = HistorialMedicoViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.efectos.collect { efecto ->
            when (efecto) {
                HistorialMedicoEfecto.Regresar -> onRegresar()
            }
        }
    }
    HistorialMedicoScreen(state = state, onEvent = viewModel::onEvent)
}

fun NavGraphBuilder.historialMedicoDestino(navController: NavController) {
    composable<HistorialMedico> {
        HistorialMedicoRoute(onRegresar = { navController.popBackStack() })
    }
}