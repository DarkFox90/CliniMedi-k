package plat.clinimedik.app.ui.screens.recepcion.resumen

sealed interface ResumenEvent {
    data object Regresar : ResumenEvent
    data object Reintentar : ResumenEvent
}
