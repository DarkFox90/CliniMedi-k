package plat.clinimedik.app.data.local.entity

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import java.time.LocalDateTime
import plat.clinimedik.app.data.model.Cita
import plat.clinimedik.app.data.model.EstadoConfirmacion

@Entity(
    tableName = "cita",
    indices = [Index("medicoId")]
)
data class CitaEntity(
    @PrimaryKey val id: String,
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

fun CitaEntity.aModelo(): Cita = Cita(
    id = id,
    pacienteId = pacienteId,
    nombrePaciente = nombrePaciente,
    correoPaciente = correoPaciente,
    medicoId = medicoId,
    fechaHora = fechaHora,
    duracionMinutos = duracionMinutos,
    motivo = motivo,
    estadoConfirmacion = estadoConfirmacion,
    recordatorioEnviado = recordatorioEnviado
)

fun Cita.aEntity(): CitaEntity = CitaEntity(
    id = id,
    pacienteId = pacienteId,
    nombrePaciente = nombrePaciente,
    correoPaciente = correoPaciente,
    medicoId = medicoId,
    fechaHora = fechaHora,
    duracionMinutos = duracionMinutos,
    motivo = motivo,
    estadoConfirmacion = estadoConfirmacion,
    recordatorioEnviado = recordatorioEnviado
)
