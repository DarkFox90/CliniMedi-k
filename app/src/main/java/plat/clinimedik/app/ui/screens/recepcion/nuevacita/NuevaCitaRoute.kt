package plat.clinimedik.app.ui.screens.recepcion.nuevacita

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import plat.clinimedik.app.ui.navigation.NuevaCita

@Composable
fun NuevaCitaRoute(
    onRegresar: () -> Unit,
    viewModel: NuevaCitaViewModel = viewModel(
        factory = NuevaCitaViewModel.Factory
    )
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.efectos.collect { efecto ->
            when (efecto) {
                NuevaCitaEfecto.Regresar -> onRegresar()
            }
        }
    }

    NuevaCitaScreen(
        state = state,
        onEvent = viewModel::onEvent
    )
}

fun NavGraphBuilder.nuevaCitaDestino(navController: NavController) {
    composable<NuevaCita> {
        NuevaCitaRoute(
            onRegresar = { navController.popBackStack() }
        )
    }
}
