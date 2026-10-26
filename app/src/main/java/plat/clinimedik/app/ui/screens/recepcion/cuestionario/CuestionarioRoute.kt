package plat.clinimedik.app.ui.screens.recepcion.cuestionario

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import plat.clinimedik.app.ui.navigation.CodigoQr
import plat.clinimedik.app.ui.navigation.Cuestionario
import plat.clinimedik.app.ui.navigation.Registro

@Composable
fun CuestionarioRoute(
    onRegresar: () -> Unit,
    onCodigoQr: (String) -> Unit,
    viewModel: CuestionarioViewModel = viewModel(
        factory = CuestionarioViewModel.Factory
    )
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.efectos.collect { efecto ->
            when (efecto) {
                CuestionarioEfecto.Regresar -> onRegresar()
                is CuestionarioEfecto.IrACodigoQr ->
                    onCodigoQr(efecto.pacienteId)
            }
        }
    }

    CuestionarioScreen(
        state = state,
        onEvent = viewModel::onEvent
    )
}

fun NavGraphBuilder.cuestionarioDestino(navController: NavController) {
    composable<Cuestionario> {
        CuestionarioRoute(
            onRegresar = { navController.popBackStack() },
            onCodigoQr = { pacienteId ->
                navController.navigate(CodigoQr(pacienteId)) {
                    popUpTo<Registro> {
                        inclusive = true
                    }
                }
            }
        )
    }
}
