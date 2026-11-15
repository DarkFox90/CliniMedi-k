package plat.clinimedik.app.ui.screens.recepcion.qr

object FormatoQr {
    const val PREFIJO = "clinimedik:"

    fun contenidoDe(pacienteId: String): String = PREFIJO + pacienteId
}