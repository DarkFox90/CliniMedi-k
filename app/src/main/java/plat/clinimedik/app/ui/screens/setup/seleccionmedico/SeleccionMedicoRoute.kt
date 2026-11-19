package plat.clinimedik.app.ui.screens.setup.seleccionmedico

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import plat.clinimedik.app.data.fake.FakeDataSource
import plat.clinimedik.app.data.model.RolDispositivo
import plat.clinimedik.app.ui.components.LocalMensajes
import plat.clinimedik.app.ui.navigation.FilaMedico
import plat.clinimedik.app.ui.navigation.Inicio
import plat.clinimedik.app.ui.navigation.SeleccionMedico
import plat.clinimedik.app.ui.navigation.borrarHistorialDePestanas

fun seleccionMedicoUiStateDePrueba(eleccion: MedicoElegido? = null): SeleccionMedicoUiState =
    SeleccionMedicoUiState(
        cargando = false,
        medicos = FakeDataSource.medicos,
        eleccion = eleccion
    )

@Composable
fun SeleccionMedicoRoute(
    onConfiguracionGuardada: (RolDispositivo) -> Unit,
    viewModel: SeleccionMedicoViewModel = viewModel(factory = SeleccionMedicoViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val mensajes = LocalMensajes.current
    LaunchedEffect(viewModel) {
        viewModel.efectos.collect { efecto ->
            when (efecto) {
                is SeleccionMedicoEfecto.ConfiguracionGuardada -> onConfiguracionGuardada(efecto.rol)
                is SeleccionMedicoEfecto.MostrarMensaje -> mensajes.mostrar(efecto.texto)
            }
        }
    }
    SeleccionMedicoScreen(state = state, onEvent = viewModel::onEvent)
}

fun NavGraphBuilder.seleccionMedicoDestino(navController: NavController) {
    composable<SeleccionMedico> {
        SeleccionMedicoRoute(
            onConfiguracionGuardada = { rol ->
                navController.borrarHistorialDePestanas()
                val destino = if (rol == RolDispositivo.RECEPCION) Inicio else FilaMedico
                navController.navigate(destino) {
                    popUpTo(navController.graph.id) { inclusive = true }
                }
            }
        )
    }
}