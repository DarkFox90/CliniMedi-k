package plat.clinimedik.app.ui.screens.recepcion.qr

sealed interface CodigoQrIntent {
    data object Imprimir : CodigoQrIntent
    data object EnviarPorWhatsApp : CodigoQrIntent
    data object Regresar : CodigoQrIntent
}