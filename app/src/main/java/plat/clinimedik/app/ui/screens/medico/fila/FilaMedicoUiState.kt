package plat.clinimedik.app.ui.screens.medico.fila

import java.time.LocalDateTime
import plat.clinimedik.app.data.model.EstadoVisita
import plat.clinimedik.app.data.model.VisitaConPaciente
import plat.clinimedik.app.ui.components.formatearHora
import plat.clinimedik.app.ui.screens.recepcion.fila.aPacienteEnFila

data class PacienteEnFilaMedico(
    val visitaId: String,
    val pacienteId: String,
    val nombre: String,
    val detalle: String,
    val tiempo: String,
    val estado: EstadoVisita,
    val esUrgente: Boolean
)

data class FilaMedicoUiState(
    val cargando: Boolean = true,
    val subtitulo: String = "",
    val ultimaActualizacion: String = "",
    val pacientes: List<PacienteEnFilaMedico> = emptyList()
)

fun calcularFilaMedico(
    fila: List<VisitaConPaciente>,
    nombreMedico: String,
    ahora: LocalDateTime
): FilaMedicoUiState = FilaMedicoUiState(
    cargando = false,
    subtitulo = "$nombreMedico · Fila de hoy",
    ultimaActualizacion = "Última actualización: ${formatearHora(ahora)}",
    pacientes = fila.map { it.aPacienteEnFilaMedico(ahora) }
)

private fun VisitaConPaciente.aPacienteEnFilaMedico(ahora: LocalDateTime): PacienteEnFilaMedico {
    val enFila = aPacienteEnFila(ahora)
    return PacienteEnFilaMedico(
        visitaId = enFila.visitaId,
        pacienteId = paciente.id,
        nombre = enFila.nombre,
        detalle = enFila.detalle,
        tiempo = enFila.tiempo,
        estado = enFila.estado,
        esUrgente = enFila.esUrgente
    )
}