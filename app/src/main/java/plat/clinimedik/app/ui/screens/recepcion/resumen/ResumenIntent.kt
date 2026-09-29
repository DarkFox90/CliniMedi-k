package plat.clinimedik.app.ui.screens.recepcion.resumen

sealed interface ResumenIntent {
    data object Regresar : ResumenIntent
}
