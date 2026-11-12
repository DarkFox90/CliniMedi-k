package plat.clinimedik.app.ui.screens.recepcion.agenda

import java.time.LocalDate
import plat.clinimedik.app.data.model.Cita
import plat.clinimedik.app.data.model.EstadoCita
import plat.clinimedik.app.data.model.EstadoConfirmacion
import plat.clinimedik.app.ui.components.DiaVisible
import plat.clinimedik.app.ui.components.formatearFechaLarga
import plat.clinimedik.app.ui.components.formatearHora

data class ElementoCita(
    val citaId: String,
    val hora: String,
    val nombrePaciente: String,
    val motivo: String,
    val estadoConfirmacion: EstadoConfirmacion,
    val recordatorioEnviado: Boolean,
    val estado: EstadoCita
)

data class AgendaRecepcionUiState(
    val cargando: Boolean = true,
    val fecha: LocalDate = LocalDate.now(),
    val textoFecha: String = "",
    val esHoy: Boolean = true,
    val elementos: List<ElementoCita> = emptyList(),
    val mensajeError: String? = null
) {
    val mensajeSinCitas: String
        get() = if (esHoy) "No hay citas programadas para hoy" else "No hay citas programadas para este día"
}

fun calcularAgendaRecepcion(
    citas: List<Cita>,
    dia: DiaVisible
): AgendaRecepcionUiState = AgendaRecepcionUiState(
    cargando = false,
    fecha = dia.fecha,
    textoFecha = formatearFechaLarga(dia.fecha),
    esHoy = dia.esHoy,
    elementos = citas.map { cita ->
        ElementoCita(
            citaId = cita.id,
            hora = formatearHora(cita.fechaHora),
            nombrePaciente = cita.nombrePaciente,
            motivo = cita.motivo,
            estadoConfirmacion = cita.estadoConfirmacion,
            recordatorioEnviado = cita.recordatorioEnviado,
            estado = cita.estado
        )
    }
)
