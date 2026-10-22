package plat.clinimedik.app.ui.screens.medico.fila

sealed interface FilaMedicoEvent {
    data class AbrirHistorial(val pacienteId: String) : FilaMedicoEvent
}
