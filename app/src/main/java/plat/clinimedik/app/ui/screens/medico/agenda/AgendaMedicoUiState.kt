package plat.clinimedik.app.ui.screens.medico.agenda

import java.time.LocalDateTime
import plat.clinimedik.app.data.model.Cita
import plat.clinimedik.app.data.model.EstadoConfirmacion
import plat.clinimedik.app.ui.components.formatearHora

data class ElementoCitaMedico(
    val citaId: String,
    val hora: String,
    val nombrePaciente: String,
    val motivo: String,
    val estadoConfirmacion: EstadoConfirmacion
)

data class AgendaMedicoUiState(
    val cargando: Boolean = true,
    val subtitulo: String = "",
    val ultimaActualizacion: String = "",
    val citas: List<ElementoCitaMedico> = emptyList()
)

fun calcularAgendaMedico(
    citas: List<Cita>,
    nombreMedico: String,
    ahora: LocalDateTime
): AgendaMedicoUiState = AgendaMedicoUiState(
    cargando = false,
    subtitulo = "$nombreMedico · Citas de hoy",
    ultimaActualizacion = "Última actualización: ${formatearHora(ahora)}",
    citas = citas.map { cita ->
        ElementoCitaMedico(
            citaId = cita.id,
            hora = formatearHora(cita.fechaHora),
            nombrePaciente = cita.nombrePaciente,
            motivo = cita.motivo,
            estadoConfirmacion = cita.estadoConfirmacion
        )
    }
)