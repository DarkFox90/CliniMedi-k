package plat.clinimedik.app.ui.screens.recepcion.buscar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import plat.clinimedik.app.data.fake.FakeDataSource
import plat.clinimedik.app.data.model.Paciente
import plat.clinimedik.app.ui.components.ContenedorRecepcion
import plat.clinimedik.app.ui.components.PestanaRecepcion
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import plat.clinimedik.app.ui.navigation.Buscar

private val idsRecientes = listOf("pac-005", "pac-004", "pac-002")

private fun Paciente.aPacienteEncontrado(): PacienteEncontrado = PacienteEncontrado(
    pacienteId = id,
    nombre = nombreCompleto,
    telefono = telefono
)

fun buscarUiStateDePrueba(texto: String = ""): BuscarUiState {
    val recientes = idsRecientes
        .mapNotNull { FakeDataSource.paciente(it) }
        .map { it.aPacienteEncontrado() }
    val resultados = if (texto.isBlank()) {
        emptyList()
    } else {
        FakeDataSource.buscarPacientes(texto).map { it.aPacienteEncontrado() }
    }
    return BuscarUiState(
        texto = texto,
        recientes = recientes,
        resultados = resultados
    )
}

@Composable
fun BuscarRoute() {
    val state = remember { buscarUiStateDePrueba() }
    BuscarScreen(state = state, onEvent = {})
}

fun NavGraphBuilder.buscarDestino(navController: NavController) {
    composable<Buscar> {
        BuscarRoute()
    }
}
