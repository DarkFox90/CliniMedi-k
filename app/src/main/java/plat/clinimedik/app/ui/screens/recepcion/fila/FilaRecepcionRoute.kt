package plat.clinimedik.app.ui.screens.recepcion.fila

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import plat.clinimedik.app.data.fake.FakeDataSource
import plat.clinimedik.app.data.model.EstadoVisita
import plat.clinimedik.app.data.model.Visita
import plat.clinimedik.app.ui.components.ContenedorRecepcion
import plat.clinimedik.app.ui.components.PestanaRecepcion
import plat.clinimedik.app.ui.components.formatearEspera
import plat.clinimedik.app.ui.components.formatearHora

private fun Visita.aPacienteEnFila(): PacienteEnFila {
    val nombre = FakeDataSource.paciente(pacienteId)?.nombreCompleto.orEmpty()
    val detalle = if (esUrgente && estado == EstadoVisita.ESPERANDO) {
        motivoPrioridad ?: motivoConsulta
    } else {
        motivoConsulta
    }
    val tiempo = when (estado) {
        EstadoVisita.ESPERANDO -> formatearEspera(horaLlegada, FakeDataSource.ahora)
        EstadoVisita.EN_CONSULTA -> "Desde ${formatearHora(horaInicioConsulta ?: horaLlegada)}"
        EstadoVisita.ATENDIDO, EstadoVisita.REFERIDO -> "Salió ${formatearHora(horaFin ?: horaLlegada)}"
    }
    return PacienteEnFila(
        visitaId = id,
        nombre = nombre,
        detalle = detalle,
        tiempo = tiempo,
        estado = estado,
        esUrgente = esUrgente
    )
}

fun filaRecepcionUiStateDePrueba(filtro: FiltroFila = FiltroFila.TODOS): FilaRecepcionUiState {
    val medico = FakeDataSource.medicoActual
    val pacientes = FakeDataSource.filaDeHoy(medico.id)
        .map { it.aPacienteEnFila() }
        .filter { filtro.incluye(it) }
    return FilaRecepcionUiState(
        subtitulo = "${medico.nombre} · Recepción",
        filtro = filtro,
        pacientes = pacientes
    )
}

@Composable
fun FilaRecepcionRoute() {
    val state = remember { filaRecepcionUiStateDePrueba() }
    ContenedorRecepcion(seleccion = PestanaRecepcion.FILA) {
        FilaRecepcionScreen(state = state, onIntent = {})
    }
}