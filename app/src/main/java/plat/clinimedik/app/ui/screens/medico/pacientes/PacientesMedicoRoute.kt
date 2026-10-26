package plat.clinimedik.app.ui.screens.medico.pacientes

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
import plat.clinimedik.app.ui.navigation.FilaMedico
import plat.clinimedik.app.ui.navigation.HistorialMedico
import plat.clinimedik.app.ui.navigation.PacientesMedico
import plat.clinimedik.app.ui.navigation.irAPestana

fun pacientesMedicoUiStateDePrueba(texto: String = ""): PacientesMedicoUiState {
    val medico = FakeDataSource.medicoActual
    val todos = FakeDataSource.visitas
        .filter { it.medicoId == medico.id }
        .map { it.pacienteId }
        .distinct()
        .mapNotNull { FakeDataSource.paciente(it) }
        .map { it.aPacienteDelMedico(FakeDataSource.historialDe(it.id)) }
        .ordenadosPorNombre()
    val pacientes = if (texto.isBlank()) {
        todos
    } else {
        val encontrados = FakeDataSource.buscarPacientes(texto).map { it.id }.toSet()
        todos.filter { it.pacienteId in encontrados }
    }
    return PacientesMedicoUiState(
        cargando = false,
        subtitulo = "${medico.nombre} · Todos los pacientes",
        texto = texto,
        pacientes = pacientes
    )
}

@Composable
fun PacientesMedicoRoute(
    onAbrirHistorial: (String) -> Unit,
    viewModel: PacientesMedicoViewModel = viewModel(factory = PacientesMedicoViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.efectos.collect { efecto ->
            when (efecto) {
                is PacientesMedicoEfecto.AbrirHistorial -> onAbrirHistorial(efecto.pacienteId)
            }
        }
    }
    PacientesMedicoScreen(state = state, onEvent = viewModel::onEvent)
}

fun NavGraphBuilder.pacientesMedicoDestino(navController: NavController) {
    composable<PacientesMedico> {
        BackHandler { navController.irAPestana(FilaMedico) }
        PacientesMedicoRoute(
            onAbrirHistorial = { pacienteId -> navController.navigate(HistorialMedico(pacienteId)) }
        )
    }
}