package plat.clinimedik.app.ui.screens.medico.historial

sealed interface HistorialMedicoIntent {
    data object Regresar : HistorialMedicoIntent
    data class AlternarVisita(val visitaId: String) : HistorialMedicoIntent
}