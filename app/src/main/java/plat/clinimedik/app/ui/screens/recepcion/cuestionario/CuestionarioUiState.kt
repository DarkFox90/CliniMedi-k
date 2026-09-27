package plat.clinimedik.app.ui.screens.recepcion.cuestionario

import plat.clinimedik.app.data.model.EstadoCivil

data class CuestionarioUiState(
    val nombre: String = "",
    val errorNombre: String? = null,
    val fechaNacimiento: String = "",
    val errorFechaNacimiento: String? = null,
    val estadoCivil: EstadoCivil? = null,
    val ocupacion: String = "",
    val telefono: String = "",
    val errorTelefono: String? = null,
    val correo: String = "",
    val errorCorreo: String? = null,
    val motivo: String = "",
    val alergias: String = "",
    val enfermedades: String = "",
    val tratamientos: String = "",
    val cirugias: String = ""
)