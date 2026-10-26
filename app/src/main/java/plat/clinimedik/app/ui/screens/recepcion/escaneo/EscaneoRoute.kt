package plat.clinimedik.app.ui.screens.recepcion.escaneo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import plat.clinimedik.app.ui.navigation.Escaneo
import plat.clinimedik.app.ui.navigation.Resultado

@Composable
fun EscaneoRoute(
    onRegresar: () -> Unit,
    onPacienteEncontrado: (String) -> Unit,
    viewModel: EscaneoViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.efectos.collect { efecto ->
            when (efecto) {
                EscaneoEfecto.Regresar -> onRegresar()

                is EscaneoEfecto.PacienteEncontrado -> {
                    onPacienteEncontrado(efecto.pacienteId)
                }
            }
        }
    }

    EscaneoScreen(
        state = state,
        onEvent = viewModel::onEvent
    )
}

fun NavGraphBuilder.escaneoDestino(navController: NavController) {
    composable<Escaneo> {
        EscaneoRoute(
            onRegresar = {
                navController.popBackStack()
            },
            onPacienteEncontrado = { pacienteId ->
                navController.navigate(Resultado(pacienteId))
            }
        )
    }
}
