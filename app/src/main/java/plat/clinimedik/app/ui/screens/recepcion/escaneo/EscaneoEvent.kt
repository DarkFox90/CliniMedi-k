package plat.clinimedik.app.ui.screens.recepcion.escaneo

sealed interface EscaneoEvent {
    data object Regresar : EscaneoEvent
    data object SimularLectura : EscaneoEvent
}