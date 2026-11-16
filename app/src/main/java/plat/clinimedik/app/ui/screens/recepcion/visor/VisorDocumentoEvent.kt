package plat.clinimedik.app.ui.screens.recepcion.visor

sealed interface VisorDocumentoEvent {
    data object Regresar : VisorDocumentoEvent
    data object Reintentar : VisorDocumentoEvent
}