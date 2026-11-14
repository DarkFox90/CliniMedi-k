package plat.clinimedik.app.data.remote

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.serialization.Serializable
import plat.clinimedik.app.data.model.Cita

/**
 * Datos que viajan al Worker de correos. Solo llevan nombre, correo, médico, fecha y hora:
 * nunca diagnósticos, tratamientos ni motivos de consulta.
 */

@Serializable
data class CitaCorreo(
    val idCita: String,
    val pacienteNombre: String,
    val pacienteCorreo: String,
    val medicoNombre: String,
    /** Fecha y hora con la zona del teléfono, por ejemplo `2026-11-14T10:30-06:00`. */
    val fechaHora: String
)

@Serializable
data class RespuestaConfirmaciones(
    val confirmadas: List<String> = emptyList()
)

@Serializable
data class AvisoUrgenteCorreo(
    val pacienteNombre: String,
    val medicoNombre: String,
    val hora: String
)

@Serializable
data class PacienteCorreo(
    val nombre: String,
    val hora: String
)

@Serializable
data class ResumenCorreo(
    val fecha: String,
    val medicoNombre: String,
    val atendidos: List<PacienteCorreo>,
    val sinConfirmarManana: List<PacienteCorreo>
)

private val formatoHoraCorreo: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

fun horaCorreo(fechaHora: LocalDateTime): String = fechaHora.format(formatoHoraCorreo)

fun Cita.aCitaCorreo(medicoNombre: String): CitaCorreo = CitaCorreo(
    idCita = id,
    pacienteNombre = nombrePaciente,
    pacienteCorreo = correoPaciente.orEmpty(),
    medicoNombre = medicoNombre,
    fechaHora = fechaHora.atZone(ZoneId.systemDefault()).toOffsetDateTime().toString()
)
