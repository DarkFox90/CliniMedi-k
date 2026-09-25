package plat.clinimedik.app.ui.screens.recepcion.fila

import plat.clinimedik.app.data.model.EstadoVisita

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
    val esUrgente: Boolean
) {
    val puedeMarcarseUrgente: Boolean
        get() = estado == EstadoVisita.ESPERANDO && !esUrgente
}

data class SolicitudUrgencia(
    val visitaId: String,
    val nombrePaciente: String,
    val motivo: String = "",
    val mensajeError: String? = null
)

data class FilaRecepcionUiState(
    val subtitulo: String = "",
    val filtro: FiltroFila = FiltroFila.TODOS,
    val pacientes: List<PacienteEnFila> = emptyList(),
    val urgencia: SolicitudUrgencia? = null
)