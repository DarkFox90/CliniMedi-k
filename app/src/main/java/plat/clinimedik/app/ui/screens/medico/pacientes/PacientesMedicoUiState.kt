package plat.clinimedik.app.ui.screens.medico.pacientes

data class PacienteDelMedico(
    val pacienteId: String,
    val nombre: String,
    val detalle: String
)

data class PacientesMedicoUiState(
    val subtitulo: String = "",
    val texto: String = "",
    val pacientes: List<PacienteDelMedico> = emptyList()
) {
    val tituloLista: String
        get() = if (pacientes.size == 1) "1 paciente" else "${pacientes.size} pacientes"

    val mensajeVacio: String
        get() = if (texto.isNotBlank()) {
            "No se encontraron pacientes con “${texto.trim()}”"
        } else {
            "Aún no hay pacientes con visitas"
        }
}
