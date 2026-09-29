package plat.clinimedik.app.ui.screens.recepcion.inicio

import plat.clinimedik.app.data.model.EstadoConfirmacion
import plat.clinimedik.app.data.model.EstadoVisita
import plat.clinimedik.app.ui.screens.recepcion.fila.PacienteEnFila

data class ProximaCita(
    val citaId: String,
    val hora: String,
    val nombrePaciente: String,
    val motivo: String,
    val estadoConfirmacion: EstadoConfirmacion
)

data class InicioRecepcionUiState(
    val fecha: String = "",
    val saludo: String = "",
    val medicoYRol: String = "",
    val pacientes: List<PacienteEnFila> = emptyList(),
    val proximaCita: ProximaCita? = null
) {
    val esperando: Int
        get() = pacientes.count { it.estado == EstadoVisita.ESPERANDO }

    val urgentes: Int
        get() = pacientes.count { it.esUrgente && it.estado == EstadoVisita.ESPERANDO }

    val atendidos: Int
        get() = pacientes.count { it.estado == EstadoVisita.ATENDIDO || it.estado == EstadoVisita.REFERIDO }

    val siguientePaciente: PacienteEnFila?
        get() = pacientes.firstOrNull { it.estado == EstadoVisita.ESPERANDO }

    val mensajeSinSiguiente: String
        get() = if (pacientes.isEmpty()) "No hay pacientes en la fila" else "No hay pacientes esperando"
}
