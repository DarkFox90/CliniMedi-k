package plat.clinimedik.app.ui.screens.medico.fila

import plat.clinimedik.app.data.model.EstadoVisita

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
    val subtitulo: String = "",
    val ultimaActualizacion: String = "",
    val pacientes: List<PacienteEnFilaMedico> = emptyList()
)
