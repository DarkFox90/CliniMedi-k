package plat.clinimedik.app.ui.screens.recepcion.visor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import plat.clinimedik.app.ui.navigation.VisorDocumento

@Composable
fun VisorDocumentoRoute(
    onRegresar: () -> Unit,
    viewModel: VisorDocumentoViewModel = viewModel(
        factory = VisorDocumentoViewModel.Factory
    )
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.efectos.collect { efecto ->
            when (efecto) {
                VisorDocumentoEfecto.Regresar -> onRegresar()
            }
        }
    }

    VisorDocumentoScreen(
        state = state,
        onEvent = viewModel::onEvent
    )
}

fun NavGraphBuilder.visorDocumentoDestino(navController: NavController) {
    composable<VisorDocumento> {
        VisorDocumentoRoute(
            onRegresar = { navController.popBackStack() }
        )
    }
}