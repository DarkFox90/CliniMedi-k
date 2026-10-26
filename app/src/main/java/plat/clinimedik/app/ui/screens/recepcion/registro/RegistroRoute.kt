package plat.clinimedik.app.ui.screens.recepcion.registro

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import plat.clinimedik.app.ui.navigation.Cuestionario
import plat.clinimedik.app.ui.navigation.Escaneo
import plat.clinimedik.app.ui.navigation.Registro

@Composable
fun RegistroRoute(
    onCuestionario: () -> Unit,
    onEscaneo: () -> Unit,
    onRegresar: () -> Unit,
    viewModel: RegistroViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.efectos.collect { efecto ->
            when (efecto) {
                RegistroEfecto.IrACuestionario -> onCuestionario()
                RegistroEfecto.IrAEscaneo -> onEscaneo()
                RegistroEfecto.Regresar -> onRegresar()
            }
        }
    }

    RegistroScreen(
        state = state,
        onEvent = viewModel::onEvent
    )
}

fun NavGraphBuilder.registroDestino(navController: NavController) {
    composable<Registro> {
        RegistroRoute(
            onCuestionario = {
                navController.navigate(Cuestionario)
            },
            onEscaneo = {
                navController.navigate(Escaneo)
            },
            onRegresar = {
                navController.popBackStack()
            }
        )
    }
}
