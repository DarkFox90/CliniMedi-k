package plat.clinimedik.app.ui.screens.medico.fila

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
import plat.clinimedik.app.ui.navigation.FilaMedico
import plat.clinimedik.app.ui.navigation.HistorialMedico
import plat.clinimedik.app.ui.navigation.Rol

fun filaMedicoUiStateDePrueba(): FilaMedicoUiState {
    val medico = FakeDataSource.medicoActual
    val fila = FakeDataSource.filaDeHoy(medico.id).mapNotNull { visita ->
        FakeDataSource.paciente(visita.pacienteId)?.let { paciente -> VisitaConPaciente(visita, paciente) }
    }
    return calcularFilaMedico(fila, medico.nombre, FakeDataSource.ahora)
}

@Composable
fun FilaMedicoRoute(
    onAbrirHistorial: (String) -> Unit,
    onCambiarConfiguracion: () -> Unit,
    viewModel: FilaMedicoViewModel = viewModel(factory = FilaMedicoViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.efectos.collect { efecto ->
            when (efecto) {
                is FilaMedicoEfecto.AbrirHistorial -> onAbrirHistorial(efecto.pacienteId)
                FilaMedicoEfecto.IrARol -> onCambiarConfiguracion()
            }
        }
    }
    FilaMedicoScreen(state = state, onEvent = viewModel::onEvent)
}

fun NavGraphBuilder.filaMedicoDestino(navController: NavController) {
    composable<FilaMedico> {
        FilaMedicoRoute(
            onAbrirHistorial = { pacienteId -> navController.navigate(HistorialMedico(pacienteId)) },
            onCambiarConfiguracion = { navController.navigate(Rol) }
        )
    }
}