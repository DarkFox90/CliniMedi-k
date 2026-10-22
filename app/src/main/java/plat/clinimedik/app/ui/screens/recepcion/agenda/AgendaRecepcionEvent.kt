package plat.clinimedik.app.ui.screens.recepcion.agenda

sealed interface AgendaRecepcionEvent {
    data class AbrirCita(val citaId: String) : AgendaRecepcionEvent
    data object NuevaCita : AgendaRecepcionEvent
}