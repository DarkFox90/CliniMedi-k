package plat.clinimedik.app.ui.screens.recepcion.resultado

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import plat.clinimedik.app.data.fake.FakeDataSource
import plat.clinimedik.app.ui.components.calcularEdad
import plat.clinimedik.app.ui.components.formatearFecha

fun resultadoUiStateDePrueba(): ResultadoUiState {
    val paciente = FakeDataSource.paciente("pac-001")
    val historial = FakeDataSource.historialDe("pac-001")

    val edadStr = paciente?.let { calcularEdad(it.fechaNacimiento, FakeDataSource.hoy).toString() } ?: ""
    val resumen = if (historial.isNotEmpty()) {
        "${historial.size} visitas anteriores · última: ${historial.firstOrNull()?.let { formatearFecha(it.horaLlegada.toLocalDate()) }}"
    } else {
        "Paciente nuevo"
    }

    return ResultadoUiState(
        nombre = paciente?.nombreCompleto.orEmpty(),
        edad = edadStr,
        telefono = paciente?.telefono.orEmpty(),
        resumenVisitas = resumen
    )
}

@Composable
fun ResultadoRoute() {
    val state = remember { resultadoUiStateDePrueba() }
    ResultadoScreen(state = state, onIntent = {})
}