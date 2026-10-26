package plat.clinimedik.app.ui.screens.setup.arranque

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import plat.clinimedik.app.ui.navigation.Arranque
import plat.clinimedik.app.ui.navigation.FilaMedico
import plat.clinimedik.app.ui.navigation.Inicio
import plat.clinimedik.app.ui.navigation.Login
import plat.clinimedik.app.ui.navigation.Rol

@Composable
fun ArranqueRoute(
    onIrALogin: () -> Unit,
    onIrARol: () -> Unit,
    onIrARecepcion: () -> Unit,
    onIrAMedico: () -> Unit,
    viewModel: ArranqueViewModel = viewModel(factory = ArranqueViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.efectos.collect { efecto ->
            when (efecto) {
                ArranqueEfecto.IrALogin -> onIrALogin()
                ArranqueEfecto.IrARol -> onIrARol()
                ArranqueEfecto.IrARecepcion -> onIrARecepcion()
                ArranqueEfecto.IrAMedico -> onIrAMedico()
            }
        }
    }
    ArranqueScreen(state = state, onEvent = viewModel::onEvent)
}

fun NavGraphBuilder.arranqueDestino(navController: NavController) {
    composable<Arranque> {
        val irSinRegreso: (Any) -> Unit = { destino ->
            navController.navigate(destino) {
                popUpTo<Arranque> { inclusive = true }
            }
        }
        ArranqueRoute(
            onIrALogin = { irSinRegreso(Login) },
            onIrARol = { irSinRegreso(Rol) },
            onIrARecepcion = { irSinRegreso(Inicio) },
            onIrAMedico = { irSinRegreso(FilaMedico) }
        )
    }
}