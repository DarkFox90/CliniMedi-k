package plat.clinimedik.app.ui.screens.medico.agenda

import java.time.LocalDate
import java.time.LocalDateTime
import plat.clinimedik.app.data.model.Cita
import plat.clinimedik.app.data.model.EstadoCita
import plat.clinimedik.app.data.model.EstadoConfirmacion
import plat.clinimedik.app.ui.components.DiaVisible
import plat.clinimedik.app.ui.components.formatearFechaLarga
import plat.clinimedik.app.ui.components.formatearHora

data class ElementoCitaMedico(
    val citaId: String,
    val hora: String,
    val nombrePaciente: String,
    val motivo: String,
    val estadoConfirmacion: EstadoConfirmacion,
    val estado: EstadoCita
)

data class AgendaMedicoUiState(
    val cargando: Boolean = true,
    val subtitulo: String = "",
    val ultimaActualizacion: String = "",
    val fecha: LocalDate = LocalDate.now(),
    val textoFecha: String = "",
    val esHoy: Boolean = true,
    val citas: List<ElementoCitaMedico> = emptyList(),
    val mensajeError: String? = null
) {
    val mensajeSinCitas: String
        get() = if (esHoy) "No hay citas programadas para hoy" else "No hay citas programadas para este día"
}

fun calcularAgendaMedico(
    citas: List<Cita>,
    nombreMedico: String,
    ahora: LocalDateTime,
    dia: DiaVisible
): AgendaMedicoUiState = AgendaMedicoUiState(
    cargando = false,
    subtitulo = nombreMedico,
    ultimaActualizacion = "Última actualización: ${formatearHora(ahora)}",
    fecha = dia.fecha,
    textoFecha = formatearFechaLarga(dia.fecha),
    esHoy = dia.esHoy,
    citas = citas.map { cita ->
        ElementoCitaMedico(
            citaId = cita.id,
            hora = formatearHora(cita.fechaHora),
            nombrePaciente = cita.nombrePaciente,
            motivo = cita.motivo,
            estadoConfirmacion = cita.estadoConfirmacion,
            estado = cita.estado
        )
    }
)
