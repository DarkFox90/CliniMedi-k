package plat.clinimedik.app.ui.screens.recepcion.qr

sealed interface CodigoQrEvent {
    data object Imprimir : CodigoQrEvent
    data object EnviarPorWhatsApp : CodigoQrEvent
    data object Regresar : CodigoQrEvent
}