package plat.clinimedik.app.ui.screens.recepcion.qr

data class CodigoQrUiState(
    val cargando: Boolean = true,
    val nombrePaciente: String = "",
    val pacienteId: String = "",
    val guardando: Boolean = false,
    val mensajeError: String? = null,
    val puedeReintentar: Boolean = false
) {
    val contenidoQr: String
        get() = FormatoQr.contenidoDe(pacienteId)
}