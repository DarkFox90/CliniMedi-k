package plat.clinimedik.app.ui.screens.recepcion.qr

object FormatoQr {
    const val PREFIJO = "clinimedik:"

    fun contenidoDe(pacienteId: String): String = PREFIJO + pacienteId

    fun pacienteIdDe(texto: String): String? {
        val limpio = texto.trim()
        if (!limpio.startsWith(PREFIJO)) return null
        return limpio.removePrefix(PREFIJO).trim().ifBlank { null }
    }
}