package plat.clinimedik.app.ui.screens.recepcion.resultado

data class ResultadoUiState(
    val cargando: Boolean = true,
    val nombre: String = "",
    val edad: String = "",
    val telefono: String = "",
    val resumenVisitas: String = "",
    val tieneHistorial: Boolean = false,
    val mostrarFormulario: Boolean = false,
    val motivo: String = "",
    val guardando: Boolean = false,
    val mensajeError: String? = null
)