package plat.clinimedik.app.ui.screens.medico.historial

sealed interface HistorialMedicoEvent {
    data object Regresar : HistorialMedicoEvent
    data class AlternarVisita(val visitaId: String) : HistorialMedicoEvent
}