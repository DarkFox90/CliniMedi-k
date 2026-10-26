package plat.clinimedik.app.ui.screens.recepcion.buscar

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
import plat.clinimedik.app.ui.navigation.Buscar
import plat.clinimedik.app.ui.navigation.Inicio
import plat.clinimedik.app.ui.navigation.Registro
import plat.clinimedik.app.ui.navigation.Resultado
import plat.clinimedik.app.ui.navigation.irAPestana

fun buscarUiStateDePrueba(texto: String = ""): BuscarUiState {
    val recientes = idsRecientes
        .mapNotNull { FakeDataSource.paciente(it) }
        .map { it.aPacienteEncontrado() }
    val resultados = if (texto.isBlank()) {
        emptyList()
    } else {
        FakeDataSource.buscarPacientes(texto).map { it.aPacienteEncontrado() }
    }
    return BuscarUiState(
        cargando = false,
        texto = texto,
        recientes = recientes,
        resultados = resultados
    )
}

@Composable
fun BuscarRoute(
    onAbrirResultado: (String) -> Unit,
    onNuevoPaciente: () -> Unit,
    viewModel: BuscarViewModel = viewModel(factory = BuscarViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.efectos.collect { efecto ->
            when (efecto) {
                is BuscarEfecto.AbrirResultado -> onAbrirResultado(efecto.pacienteId)
                BuscarEfecto.IrARegistro -> onNuevoPaciente()
            }
        }
    }
    BuscarScreen(state = state, onEvent = viewModel::onEvent)
}

fun NavGraphBuilder.buscarDestino(navController: NavController) {
    composable<Buscar> {
        BackHandler { navController.irAPestana(Inicio) }
        BuscarRoute(
            onAbrirResultado = { pacienteId -> navController.navigate(Resultado(pacienteId)) },
            onNuevoPaciente = { navController.navigate(Registro) }
        )
    }
}