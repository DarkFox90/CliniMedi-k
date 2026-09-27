package plat.clinimedik.app.ui.screens.recepcion.cuestionario

import plat.clinimedik.app.data.model.EstadoCivil

sealed interface CuestionarioIntent {
    data class CambiarNombre(val texto: String) : CuestionarioIntent
    data class CambiarFechaNacimiento(val texto: String) : CuestionarioIntent
    data class SeleccionarEstadoCivil(val estado: EstadoCivil) : CuestionarioIntent
    data class CambiarOcupacion(val texto: String) : CuestionarioIntent
    data class CambiarTelefono(val texto: String) : CuestionarioIntent
    data class CambiarCorreo(val texto: String) : CuestionarioIntent
    data class CambiarMotivo(val texto: String) : CuestionarioIntent
    data class CambiarAlergias(val texto: String) : CuestionarioIntent
    data class CambiarEnfermedades(val texto: String) : CuestionarioIntent
    data class CambiarTratamientos(val texto: String) : CuestionarioIntent
    data class CambiarCirugias(val texto: String) : CuestionarioIntent
    data object Guardar : CuestionarioIntent
    data object Regresar : CuestionarioIntent
}