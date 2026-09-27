package plat.clinimedik.app.ui.screens.medico.agenda

sealed interface AgendaMedicoIntent {
    data object Regresar : AgendaMedicoIntent
}