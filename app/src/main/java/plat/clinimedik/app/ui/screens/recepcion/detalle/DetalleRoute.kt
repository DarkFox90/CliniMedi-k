package plat.clinimedik.app.ui.screens.recepcion.detalle

import android.content.ActivityNotFoundException
import android.content.Intent
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
import plat.clinimedik.app.ui.navigation.FichaPaciente
import plat.clinimedik.app.ui.navigation.NuevaCita

fun detalleUiStateDePrueba(
    visitaId: String = "vis-102",
    panel: PanelDetalle? = null
): DetalleUiState {
    val visita = FakeDataSource.visitas.firstOrNull { it.id == visitaId } ?: return DetalleUiState(cargando = false)
    val paciente = FakeDataSource.paciente(visita.pacienteId) ?: return DetalleUiState(cargando = false)
    val historial = FakeDataSource.historialDe(paciente.id)
    return VisitaConPaciente(visita, paciente)
        .aDetalleUiState(
            documentos = FakeDataSource.documentosDe(paciente.id),
            historial = historial,
            recetas = historial.flatMap { FakeDataSource.recetasDe(it.id) },
            referencias = historial.mapNotNull { FakeDataSource.referenciaDe(it.id) },
            cobro = FakeDataSource.cobroDe(visita.id),
            proximaCita = FakeDataSource.proximaCitaDe(paciente.id),
            nombreMedico = FakeDataSource.medico(visita.medicoId)?.nombre.orEmpty(),
            hoy = FakeDataSource.hoy
        )
        .copy(panel = panel)
}

@Composable
fun DetalleRoute(
    onRegresar: () -> Unit,
    onAgendarSeguimiento: (pacienteId: String, motivo: String) -> Unit,
    onAbrirFicha: (pacienteId: String) -> Unit,
    viewModel: DetalleViewModel = viewModel(factory = DetalleViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.efectos.collect { efecto ->
            when (efecto) {
                DetalleEfecto.Regresar -> onRegresar()
                is DetalleEfecto.MostrarMensaje -> Toast.makeText(context, efecto.texto, Toast.LENGTH_SHORT).show()
                is DetalleEfecto.CompartirTexto -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, efecto.texto)
                    }
                    try {
                        context.startActivity(Intent.createChooser(intent, efecto.titulo))
                    } catch (_: ActivityNotFoundException) {
                        Toast.makeText(context, "No hay una aplicación para compartir", Toast.LENGTH_SHORT).show()
                    }
                }
                is DetalleEfecto.IrANuevaCita -> onAgendarSeguimiento(efecto.pacienteId, efecto.motivo)
                is DetalleEfecto.IrAFicha -> onAbrirFicha(efecto.pacienteId)
            }
        }
    }
    DetalleScreen(state = state, onEvent = viewModel::onEvent)
}

fun NavGraphBuilder.detalleDestino(navController: NavController) {
    composable<Detalle> {
        DetalleRoute(
            onRegresar = { navController.popBackStack() },
            onAgendarSeguimiento = { pacienteId, motivo ->
                navController.navigate(NuevaCita(pacienteId = pacienteId, motivo = motivo))
            },
            onAbrirFicha = { pacienteId ->
                navController.navigate(FichaPaciente(pacienteId))
            }
        )
    }
}
