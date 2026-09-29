package plat.clinimedik.app.ui.screens.medico.pacientes

sealed interface PacientesMedicoIntent {
    data class CambiarTexto(val texto: String) : PacientesMedicoIntent
    data class AbrirHistorial(val pacienteId: String) : PacientesMedicoIntent
}
