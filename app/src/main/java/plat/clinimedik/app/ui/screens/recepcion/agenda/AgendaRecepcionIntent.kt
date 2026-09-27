package plat.clinimedik.app.ui.screens.recepcion.agenda

sealed interface AgendaRecepcionIntent {
    data class AbrirCita(val citaId: String) : AgendaRecepcionIntent
    data object NuevaCita : AgendaRecepcionIntent
}