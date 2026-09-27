package plat.clinimedik.app.ui.screens.recepcion.agenda

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import plat.clinimedik.app.data.fake.FakeDataSource
import plat.clinimedik.app.ui.components.ContenedorRecepcion
import plat.clinimedik.app.ui.components.PestanaRecepcion
import plat.clinimedik.app.ui.components.formatearFechaLarga
import plat.clinimedik.app.ui.components.formatearHora

fun agendaUiStateDePrueba(): AgendaRecepcionUiState {
    val medico = FakeDataSource.medicoActual
    val elementos = FakeDataSource.citasDelDia(medico.id).map { cita ->
        ElementoCita(
            citaId = cita.id,
            hora = formatearHora(cita.fechaHora),
            nombrePaciente = cita.nombrePaciente,
            motivo = cita.motivo,
            estadoConfirmacion = cita.estadoConfirmacion,
            recordatorioEnviado = cita.recordatorioEnviado
        )
    }
    return AgendaRecepcionUiState(
        subtitulo = formatearFechaLarga(FakeDataSource.hoy),
        elementos = elementos
    )
}

@Composable
fun AgendaRecepcionRoute() {
    val state = remember { agendaUiStateDePrueba() }
    ContenedorRecepcion(seleccion = PestanaRecepcion.AGENDA) {
        AgendaRecepcionScreen(state = state, onIntent = {})
    }
}