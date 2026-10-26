package plat.clinimedik.app.ui.screens.recepcion.qr

data class CodigoQrUiState(
    val cargando: Boolean = true,
    val nombrePaciente: String = "",
    val pacienteId: String = "",
    val mensajeError: String? = null
)