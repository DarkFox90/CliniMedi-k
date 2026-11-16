package plat.clinimedik.app.ui.screens.recepcion.visor

import androidx.compose.ui.graphics.ImageBitmap

data class VisorDocumentoUiState(
    val cargando: Boolean = true,
    val mensajeError: String? = null,
    val puedeReintentar: Boolean = false,
    val tipo: String = "",
    val fecha: String = "",
    val imagen: ImageBitmap? = null
)