package plat.clinimedik.app.ui.screens.recepcion.nuevacita

data class NuevaCitaUiState(
    val paciente: String = "",
    val errorPaciente: String? = null,
    val correo: String = "",
    val errorCorreo: String? = null,
    val fecha: String = "",
    val errorFecha: String? = null,
    val hora: String = "",
    val errorHora: String? = null,
    val motivo: String = "",
    val errorMotivo: String? = null
)