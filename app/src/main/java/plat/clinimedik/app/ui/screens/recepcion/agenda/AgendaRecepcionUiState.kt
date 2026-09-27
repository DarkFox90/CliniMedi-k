package plat.clinimedik.app.ui.screens.recepcion.agenda

import plat.clinimedik.app.data.model.EstadoConfirmacion

data class ElementoCita(
    val citaId: String,
    val hora: String,
    val nombrePaciente: String,
    val motivo: String,
    val estadoConfirmacion: EstadoConfirmacion,
    val recordatorioEnviado: Boolean
)

data class AgendaRecepcionUiState(
    val subtitulo: String = "",
    val elementos: List<ElementoCita> = emptyList()
)