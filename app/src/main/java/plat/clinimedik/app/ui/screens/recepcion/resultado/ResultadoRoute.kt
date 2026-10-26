package plat.clinimedik.app.ui.screens.recepcion.resultado

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import plat.clinimedik.app.data.fake.FakeDataSource
import plat.clinimedik.app.ui.components.calcularEdad
import plat.clinimedik.app.ui.components.formatearFecha
import plat.clinimedik.app.ui.navigation.Detalle
import plat.clinimedik.app.ui.navigation.Resultado

fun resultadoUiStateDePrueba(): ResultadoUiState {
    val paciente = FakeDataSource.paciente("pac-001")
    val historial = FakeDataSource.historialDe("pac-001")

    val resumen = if (historial.isEmpty()) {
        "Paciente nuevo"
    } else {
        "${historial.size} visitas · última: " +
                formatearFecha(historial.first().horaLlegada.toLocalDate())
    }

    return ResultadoUiState(
        cargando = false,
        nombre = paciente?.nombreCompleto.orEmpty(),
        edad = paciente?.let {
            calcularEdad(it.fechaNacimiento, FakeDataSource.hoy).toString()
        }.orEmpty(),
        telefono = paciente?.telefono.orEmpty(),
        resumenVisitas = resumen,
        tieneHistorial = historial.isNotEmpty()
    )
}

@Composable
fun ResultadoRoute(
    onRegresar: () -> Unit,
    onAbrirDetalle: (String) -> Unit,
    viewModel: ResultadoViewModel = viewModel(
        factory = ResultadoViewModel.Factory
    )
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.efectos.collect { efecto ->
            when (efecto) {
                ResultadoEfecto.Regresar -> onRegresar()
                is ResultadoEfecto.AbrirDetalle ->
                    onAbrirDetalle(efecto.visitaId)
            }
        }
    }

    ResultadoScreen(
        state = state,
        onEvent = viewModel::onEvent
    )
}

fun NavGraphBuilder.resultadoDestino(navController: NavController) {
    composable<Resultado> {
        ResultadoRoute(
            onRegresar = { navController.popBackStack() },
            onAbrirDetalle = { visitaId ->
                navController.navigate(Detalle(visitaId)) {
                    popUpTo<Resultado> { inclusive = true }
                }
            }
        )
    }
}

