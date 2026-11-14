package plat.clinimedik.app.ui.screens.recepcion.fila

import java.time.LocalDate
import java.time.LocalDateTime
import plat.clinimedik.app.data.model.EstadoVisita
import plat.clinimedik.app.data.model.VisitaConPaciente
import plat.clinimedik.app.ui.components.formatearEspera
import plat.clinimedik.app.ui.components.formatearHora

enum class FiltroFila(val etiqueta: String, val mensajeVacio: String) {
    TODOS("Todos", "No hay pacientes en la fila"),
    URGENTES("Urgentes", "No hay pacientes urgentes"),
    ESPERANDO("Esperando", "No hay pacientes esperando"),
    EN_CONSULTA("En consulta", "No hay pacientes en consulta"),
    ATENDIDOS("Atendidos", "Aún no hay pacientes atendidos");

    fun incluye(paciente: PacienteEnFila): Boolean = when (this) {
        TODOS -> true
        URGENTES -> paciente.esUrgente && paciente.estado == EstadoVisita.ESPERANDO
        ESPERANDO -> paciente.estado == EstadoVisita.ESPERANDO
        EN_CONSULTA -> paciente.estado == EstadoVisita.EN_CONSULTA
        ATENDIDOS -> paciente.estado == EstadoVisita.ATENDIDO || paciente.estado == EstadoVisita.REFERIDO
    }
}

data class PacienteEnFila(
    val visitaId: String,
    val nombre: String,
    val detalle: String,
    val tiempo: String,
    val estado: EstadoVisita,
    val esUrgente: Boolean,
    /** `false` en la fila de un día anterior: solo se consulta. */
    val editable: Boolean = true
) {
    val puedeMarcarseUrgente: Boolean
        get() = editable && estado == EstadoVisita.ESPERANDO && !esUrgente
}

data class SolicitudUrgencia(
    val visitaId: String,
    val nombrePaciente: String,
    val motivo: String = "",
    val mensajeError: String? = null,
    val guardando: Boolean = false
)

/**
 * Estado de la Fila de Recepción. Muestra hoy o un día anterior (nunca días futuros:
 * una visita se crea cuando el paciente llega, y lo que viene ya lo muestra la agenda).
 * En un día anterior la fila es de solo consulta: no se marca urgente ni se registra a nadie.
 */
data class FilaRecepcionUiState(
    val cargando: Boolean = true,
    val subtitulo: String = "",
    val fecha: LocalDate = LocalDate.now(),
    val textoFecha: String = "",
    val esHoy: Boolean = true,
    val filtro: FiltroFila = FiltroFila.TODOS,
    val pacientes: List<PacienteEnFila> = emptyList(),
    val urgencia: SolicitudUrgencia? = null,
    val mensajeError: String? = null
) {
    val titulo: String
        get() = if (esHoy) "Fila de hoy" else "Fila del día"

    val pacientesVisibles: List<PacienteEnFila>
        get() = pacientes.filter { filtro.incluye(it) }

    val mensajeVacio: String
        get() = if (!esHoy && filtro == FiltroFila.TODOS) "No hubo pacientes en este día" else filtro.mensajeVacio
}

/**
 * Convierte una visita en una fila de la lista. En la fila de hoy, quien espera muestra cuánto lleva
 * esperando; en un día anterior se muestra la hora de llegada, porque la espera ya no tiene sentido.
 */
fun VisitaConPaciente.aPacienteEnFila(ahora: LocalDateTime, esHoy: Boolean = true): PacienteEnFila {
    val detalle = if (visita.esUrgente && visita.estado == EstadoVisita.ESPERANDO) {
        visita.motivoPrioridad ?: visita.motivoConsulta
    } else {
        visita.motivoConsulta
    }
    val tiempo = when (visita.estado) {
        EstadoVisita.ESPERANDO -> if (esHoy) {
            formatearEspera(visita.horaLlegada, ahora)
        } else {
            "Llegó ${formatearHora(visita.horaLlegada)}"
        }
        EstadoVisita.EN_CONSULTA -> "Desde ${formatearHora(visita.horaInicioConsulta ?: visita.horaLlegada)}"
        EstadoVisita.ATENDIDO, EstadoVisita.REFERIDO -> "Salió ${formatearHora(visita.horaFin ?: visita.horaLlegada)}"
    }
    return PacienteEnFila(
        visitaId = visita.id,
        nombre = paciente.nombreCompleto,
        detalle = detalle,
        tiempo = tiempo,
        estado = visita.estado,
        esUrgente = visita.esUrgente,
        editable = esHoy
    )
}
