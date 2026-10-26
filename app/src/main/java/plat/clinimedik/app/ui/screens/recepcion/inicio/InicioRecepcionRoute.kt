package plat.clinimedik.app.ui.screens.recepcion.inicio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import plat.clinimedik.app.data.fake.FakeDataSource
import plat.clinimedik.app.data.model.VisitaConPaciente
import plat.clinimedik.app.ui.navigation.AgendaRecepcion
import plat.clinimedik.app.ui.navigation.Detalle
import plat.clinimedik.app.ui.navigation.Escaneo
import plat.clinimedik.app.ui.navigation.Inicio
import plat.clinimedik.app.ui.navigation.Registro
import plat.clinimedik.app.ui.navigation.Resumen
import plat.clinimedik.app.ui.navigation.Rol
import plat.clinimedik.app.ui.navigation.irAPestana

fun inicioRecepcionUiStateDePrueba(): InicioRecepcionUiState {
    val medico = FakeDataSource.medicoActual
    val fila = FakeDataSource.filaDeHoy(medico.id).mapNotNull { visita ->
        FakeDataSource.paciente(visita.pacienteId)?.let { paciente -> VisitaConPaciente(visita, paciente) }
    }
    return calcularInicio(fila, FakeDataSource.citasDelDia(medico.id), medico.nombre, FakeDataSource.ahora)
}

@Composable
fun InicioRecepcionRoute(
    onAbrirDetalle: (String) -> Unit,
    onNuevoPaciente: () -> Unit,
    onEscanearQr: () -> Unit,
    onVerResumen: () -> Unit,
    onVerAgenda: () -> Unit,
    onCambiarConfiguracion: () -> Unit,
    viewModel: InicioRecepcionViewModel = viewModel(factory = InicioRecepcionViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.efectos.collect { efecto ->
            when (efecto) {
                is InicioRecepcionEfecto.AbrirDetalle -> onAbrirDetalle(efecto.visitaId)
                InicioRecepcionEfecto.IrARegistro -> onNuevoPaciente()
                InicioRecepcionEfecto.IrAEscaneo -> onEscanearQr()
                InicioRecepcionEfecto.IrAResumen -> onVerResumen()
                InicioRecepcionEfecto.IrAAgenda -> onVerAgenda()
                InicioRecepcionEfecto.IrARol -> onCambiarConfiguracion()
            }
        }
    }
    InicioRecepcionScreen(state = state, onEvent = viewModel::onEvent)
}

fun NavGraphBuilder.inicioRecepcionDestino(navController: NavController) {
    composable<Inicio> {
        InicioRecepcionRoute(
            onAbrirDetalle = { visitaId -> navController.navigate(Detalle(visitaId)) },
            onNuevoPaciente = { navController.navigate(Registro) },
            onEscanearQr = { navController.navigate(Escaneo) },
            onVerResumen = { navController.navigate(Resumen) },
            onVerAgenda = { navController.irAPestana(AgendaRecepcion) },
            onCambiarConfiguracion = { navController.navigate(Rol) }
        )
    }
}