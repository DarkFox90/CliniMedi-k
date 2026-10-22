package plat.clinimedik.app.data.local.entity

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import java.time.LocalDateTime
import plat.clinimedik.app.data.model.EstadoVisita
import plat.clinimedik.app.data.model.Visita

@Entity(
    tableName = "visita",
    indices = [Index("pacienteId"), Index("medicoId")]
)
data class VisitaEntity(
    @PrimaryKey val id: String,
    val pacienteId: String,
    val medicoId: String,
    val motivoConsulta: String,
    val horaLlegada: LocalDateTime,
    val estado: EstadoVisita,
    val esUrgente: Boolean,
    val motivoPrioridad: String?,
    val horaInicioConsulta: LocalDateTime?,
    val horaFin: LocalDateTime?,
    val diagnostico: String?,
    val notas: String?
)

fun VisitaEntity.aModelo(): Visita = Visita(
    id = id,
    pacienteId = pacienteId,
    medicoId = medicoId,
    motivoConsulta = motivoConsulta,
    horaLlegada = horaLlegada,
    estado = estado,
    esUrgente = esUrgente,
    motivoPrioridad = motivoPrioridad,
    horaInicioConsulta = horaInicioConsulta,
    horaFin = horaFin,
    diagnostico = diagnostico,
    notas = notas
)

fun Visita.aEntity(): VisitaEntity = VisitaEntity(
    id = id,
    pacienteId = pacienteId,
    medicoId = medicoId,
    motivoConsulta = motivoConsulta,
    horaLlegada = horaLlegada,
    estado = estado,
    esUrgente = esUrgente,
    motivoPrioridad = motivoPrioridad,
    horaInicioConsulta = horaInicioConsulta,
    horaFin = horaFin,
    diagnostico = diagnostico,
    notas = notas
)
