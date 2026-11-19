package plat.clinimedik.app.ui.screens.medico.fila

sealed interface FilaMedicoEvent {
    data class AbrirHistorial(val pacienteId: String) : FilaMedicoEvent
    data object AbrirMenu : FilaMedicoEvent
    data object CerrarMenu : FilaMedicoEvent
    data object CambiarConfiguracion : FilaMedicoEvent
    data object SolicitarCierreSesion : FilaMedicoEvent
    data object CancelarCierreSesion : FilaMedicoEvent
    data object ConfirmarCierreSesion : FilaMedicoEvent
}