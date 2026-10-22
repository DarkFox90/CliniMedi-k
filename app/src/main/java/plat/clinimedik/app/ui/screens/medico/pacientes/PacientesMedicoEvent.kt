package plat.clinimedik.app.ui.screens.medico.pacientes

sealed interface PacientesMedicoEvent {
    data class CambiarTexto(val texto: String) : PacientesMedicoEvent
    data class AbrirHistorial(val pacienteId: String) : PacientesMedicoEvent
}
