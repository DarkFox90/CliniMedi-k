package plat.clinimedik.app.ui.screens.recepcion.resultado

data class CitaDeHoy(
    val citaId: String,
    val hora: String,
    val motivo: String
)

data class ResultadoUiState(
    val cargando: Boolean = true,
    val mensajeErrorCarga: String? = null,
    val cargaReintentable: Boolean = false,
    val nombre: String = "",
    val edad: String = "",
    val telefono: String = "",
    val aseguradora: String? = null,
    val resumenVisitas: String = "",
    val tieneHistorial: Boolean = false,
    val proximaCita: String? = null,
    val ultimaReferencia: String? = null,
    val citaDeHoy: CitaDeHoy? = null,
    val mostrarFormulario: Boolean = false,
    val citaDelDialogo: CitaDeHoy? = null,
    val motivo: String = "",
    val errorMotivo: String? = null,
    val guardando: Boolean = false,
    val mensajeError: String? = null
)