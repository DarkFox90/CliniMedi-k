package plat.clinimedik.app.ui.screens.medico.agenda

sealed interface AgendaMedicoEvent {
    data object Regresar : AgendaMedicoEvent
}