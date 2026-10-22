package plat.clinimedik.app.ui.screens.recepcion.detalle

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import plat.clinimedik.app.data.fake.FakeDataSource
import plat.clinimedik.app.data.model.VisitaConPaciente
import plat.clinimedik.app.ui.navigation.Detalle

fun detalleUiStateDePrueba(
    visitaId: String = "vis-102",
    panel: PanelDetalle? = null
): DetalleUiState {
    val visita = FakeDataSource.visitas.firstOrNull { it.id == visitaId } ?: return DetalleUiState(cargando = false)
    val paciente = FakeDataSource.paciente(visita.pacienteId) ?: return DetalleUiState(cargando = false)
    return VisitaConPaciente(visita, paciente)
        .aDetalleUiState(
            documentos = FakeDataSource.documentosDe(paciente.id),
            historial = FakeDataSource.historialDe(paciente.id),
            hoy = FakeDataSource.hoy
        )
        .copy(panel = panel)
}

@Composable
fun DetalleRoute(
    onRegresar: () -> Unit,
    viewModel: DetalleViewModel = viewModel(factory = DetalleViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.efectos.collect { efecto ->
            when (efecto) {
                DetalleEfecto.Regresar -> onRegresar()
                is DetalleEfecto.MostrarMensaje -> Toast.makeText(context, efecto.texto, Toast.LENGTH_SHORT).show()
            }
        }
    }
    DetalleScreen(state = state, onEvent = viewModel::onEvent)
}

fun NavGraphBuilder.detalleDestino(navController: NavController) {
    composable<Detalle> {
        DetalleRoute(onRegresar = { navController.popBackStack() })
    }
}
