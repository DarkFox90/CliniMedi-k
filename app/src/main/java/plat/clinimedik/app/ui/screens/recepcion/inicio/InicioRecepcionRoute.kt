package plat.clinimedik.app.ui.screens.recepcion.inicio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.time.LocalDateTime
import plat.clinimedik.app.data.fake.FakeDataSource
import plat.clinimedik.app.data.model.Cita
import plat.clinimedik.app.data.model.EstadoVisita
import plat.clinimedik.app.data.model.Visita
import plat.clinimedik.app.ui.components.ContenedorRecepcion
import plat.clinimedik.app.ui.components.PestanaRecepcion
import plat.clinimedik.app.ui.components.formatearEspera
import plat.clinimedik.app.ui.components.formatearFechaLarga
import plat.clinimedik.app.ui.components.formatearHora
import plat.clinimedik.app.ui.screens.recepcion.fila.PacienteEnFila

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

private fun Cita.aProximaCita(): ProximaCita = ProximaCita(
    citaId = id,
    hora = formatearHora(fechaHora),
    nombrePaciente = nombrePaciente,
    motivo = motivo,
    estadoConfirmacion = estadoConfirmacion
)

private fun saludoPara(momento: LocalDateTime): String = when (momento.hour) {
    in 0..11 -> "Buenos días"
    in 12..18 -> "Buenas tardes"
    else -> "Buenas noches"
}

fun inicioRecepcionUiStateDePrueba(): InicioRecepcionUiState {
    val medico = FakeDataSource.medicoActual
    val ahora = FakeDataSource.ahora
    return InicioRecepcionUiState(
        fecha = formatearFechaLarga(FakeDataSource.hoy),
        saludo = saludoPara(ahora),
        medicoYRol = "${medico.nombre} · Recepción",
        pacientes = FakeDataSource.filaDeHoy(medico.id).map { it.aPacienteEnFila() },
        proximaCita = FakeDataSource.citasDelDia(medico.id)
            .firstOrNull { it.fechaHora.isAfter(ahora) }
            ?.aProximaCita()
    )
}

@Composable
fun InicioRecepcionRoute() {
    val state = remember { inicioRecepcionUiStateDePrueba() }
    ContenedorRecepcion(seleccion = PestanaRecepcion.INICIO) {
        InicioRecepcionScreen(state = state, onIntent = {})
    }
}
