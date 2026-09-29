package plat.clinimedik.app.ui.screens.medico.fila

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import plat.clinimedik.app.data.fake.FakeDataSource
import plat.clinimedik.app.data.model.EstadoVisita
import plat.clinimedik.app.data.model.Visita
import plat.clinimedik.app.ui.components.ContenedorMedico
import plat.clinimedik.app.ui.components.PestanaMedico
import plat.clinimedik.app.ui.components.formatearEspera
import plat.clinimedik.app.ui.components.formatearHora

private fun Visita.aPacienteEnFilaMedico(): PacienteEnFilaMedico {
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
    return PacienteEnFilaMedico(
        visitaId = id,
        pacienteId = pacienteId,
        nombre = nombre,
        detalle = detalle,
        tiempo = tiempo,
        estado = estado,
        esUrgente = esUrgente
    )
}

fun filaMedicoUiStateDePrueba(): FilaMedicoUiState {
    val medico = FakeDataSource.medicoActual
    return FilaMedicoUiState(
        subtitulo = "${medico.nombre} · Fila de hoy",
        ultimaActualizacion = "Última actualización: ${formatearHora(FakeDataSource.ahora)}",
        pacientes = FakeDataSource.filaDeHoy(medico.id).map { it.aPacienteEnFilaMedico() }
    )
}

@Composable
fun FilaMedicoRoute() {
    val state = remember { filaMedicoUiStateDePrueba() }
    ContenedorMedico(seleccion = PestanaMedico.FILA) {
        FilaMedicoScreen(state = state, onIntent = {})
    }
}
