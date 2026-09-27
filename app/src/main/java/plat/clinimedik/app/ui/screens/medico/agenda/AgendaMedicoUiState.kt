package plat.clinimedik.app.ui.screens.medico.agenda

data class Cita(
    val id: String,
    val paciente: String,
    val hora: String,
    val motivo: String
)

data class AgendaMedicoUiState(
    val titulo: String = "Mi Agenda",
    val citas: List<Cita> = emptyList()
)