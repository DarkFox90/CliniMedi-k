package plat.clinimedik.app.ui.screens.recepcion.qr

import plat.clinimedik.app.ui.components.ColoresQr

sealed interface CodigoQrEvent {
    data class Imprimir(val colores: ColoresQr) : CodigoQrEvent
    data class EnviarPorWhatsApp(val colores: ColoresQr) : CodigoQrEvent
    data object Reintentar : CodigoQrEvent
    data object Regresar : CodigoQrEvent
}