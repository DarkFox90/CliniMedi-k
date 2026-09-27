package plat.clinimedik.app.ui.screens.recepcion.escaneo

sealed interface EscaneoIntent {
    data object Regresar : EscaneoIntent
}