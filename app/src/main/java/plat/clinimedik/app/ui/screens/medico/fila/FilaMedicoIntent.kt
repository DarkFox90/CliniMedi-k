package plat.clinimedik.app.ui.screens.medico.fila

sealed interface FilaMedicoIntent {
    data class AbrirHistorial(val pacienteId: String) : FilaMedicoIntent
}
