package plat.clinimedik.app.ui.screens.recepcion.inicio

import java.time.LocalDateTime
import plat.clinimedik.app.data.model.Cita
import plat.clinimedik.app.data.model.EstadoConfirmacion
import plat.clinimedik.app.data.model.EstadoVisita
import plat.clinimedik.app.data.model.VisitaConPaciente
import plat.clinimedik.app.ui.components.formatearFechaLarga
import plat.clinimedik.app.ui.components.formatearHora
import plat.clinimedik.app.ui.screens.recepcion.fila.PacienteEnFila
import plat.clinimedik.app.ui.screens.recepcion.fila.aPacienteEnFila

data class ProximaCita(
    val citaId: String,
    val hora: String,
    val nombrePaciente: String,
    val motivo: String,
    val estadoConfirmacion: EstadoConfirmacion
)

data class InicioRecepcionUiState(
    val cargando: Boolean = true,
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

fun calcularInicio(
    fila: List<VisitaConPaciente>,
    citas: List<Cita>,
    nombreMedico: String,
    ahora: LocalDateTime
): InicioRecepcionUiState = InicioRecepcionUiState(
    cargando = false,
    fecha = formatearFechaLarga(ahora.toLocalDate()),
    saludo = saludoPara(ahora),
    medicoYRol = "$nombreMedico · Recepción",
    pacientes = fila.map { it.aPacienteEnFila(ahora) },
    proximaCita = citas.firstOrNull { it.fechaHora.isAfter(ahora) }?.aProximaCita()
)

private fun Cita.aProximaCita(): ProximaCita = ProximaCita(
    citaId = id,
    hora = formatearHora(fechaHora),
    nombrePaciente = nombrePaciente,
    motivo = motivo,
    estadoConfirmacion = estadoConfirmacion
)

private fun saludoPara(momento: LocalDateTime): String = when (momento.hour) {
    in 0..11 -> "Buenos días"
    in 12..18 -> "Buenas tardes"
    else -> "Buenas noches"
}