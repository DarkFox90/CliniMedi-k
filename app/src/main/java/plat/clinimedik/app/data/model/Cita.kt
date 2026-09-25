package plat.clinimedik.app.data.model

import java.time.LocalDateTime

data class Cita (
    val id: String,
    val pacienteId: String?,
    val nombrePaciente: String,
    val correoPaciente: String?,
    val medicoId: String,
    val fechaHora: LocalDateTime,
    val duracionMinutos: Int,
    val motivo: String,
    val estadoConfirmacion: EstadoConfirmacion,
    val recordatorioEnviado: Boolean
)