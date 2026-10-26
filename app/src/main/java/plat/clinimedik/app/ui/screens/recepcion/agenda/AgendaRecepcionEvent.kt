package plat.clinimedik.app.ui.screens.recepcion.agenda

sealed interface AgendaRecepcionEvent {
    data object NuevaCita : AgendaRecepcionEvent
}