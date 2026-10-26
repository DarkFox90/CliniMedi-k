package plat.clinimedik.app.ui.screens.setup.arranque

sealed interface ArranqueEvent {
    data object Reintentar : ArranqueEvent
}