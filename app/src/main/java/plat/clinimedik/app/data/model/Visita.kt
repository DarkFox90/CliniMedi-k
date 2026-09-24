package plat.clinimedik.app.data.model

import java.time.LocalDateTime

data class Visita (
    val id: String,
    val pacienteId: String,
    val medicoId: String,
    val motivoConsulta: String,
    val horaLlegada: LocalDateTime,
    val estado: EstadoVisita,
    val esUrgente: Boolean = false,
    val motivoPrioridad: String? = null,
    val horaInicioConsulta: LocalDateTime? = null,
    val horaFin: LocalDateTime? = null,
    val diagnostico: String? = null,
    val notas: String? = null
)