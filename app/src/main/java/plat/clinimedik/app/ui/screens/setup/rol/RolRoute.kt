package plat.clinimedik.app.ui.screens.setup.rol

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import plat.clinimedik.app.data.model.RolDispositivo
import plat.clinimedik.app.ui.navigation.Rol
import plat.clinimedik.app.ui.navigation.SeleccionMedico

fun rolUiStateDePrueba(rolSeleccionado: RolDispositivo? = null): RolUiState = RolUiState(
    opciones = RolDispositivo.entries.map { it.aOpcionRol() },
    rolSeleccionado = rolSeleccionado
)

@Composable
fun RolRoute(
    onContinuar: (RolDispositivo) -> Unit,
    viewModel: RolViewModel = viewModel(factory = RolViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.efectos.collect { efecto ->
            when (efecto) {
                is RolEfecto.IrASeleccionMedico -> onContinuar(efecto.rol)
            }
        }
    }
    RolScreen(state = state, onEvent = viewModel::onEvent)
}

fun NavGraphBuilder.rolDestino(navController: NavController) {
    composable<Rol> {
        RolRoute(
            onContinuar = { rol -> navController.navigate(SeleccionMedico(rol.name)) }
        )
    }
}