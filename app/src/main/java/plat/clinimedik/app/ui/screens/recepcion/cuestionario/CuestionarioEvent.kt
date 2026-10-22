package plat.clinimedik.app.ui.screens.recepcion.cuestionario

import plat.clinimedik.app.data.model.EstadoCivil

sealed interface CuestionarioEvent {
    data class CambiarNombre(val texto: String) : CuestionarioEvent
    data class CambiarFechaNacimiento(val texto: String) : CuestionarioEvent
    data class SeleccionarEstadoCivil(val estado: EstadoCivil) : CuestionarioEvent
    data class CambiarOcupacion(val texto: String) : CuestionarioEvent
    data class CambiarTelefono(val texto: String) : CuestionarioEvent
    data class CambiarCorreo(val texto: String) : CuestionarioEvent
    data class CambiarMotivo(val texto: String) : CuestionarioEvent
    data class CambiarAlergias(val texto: String) : CuestionarioEvent
    data class CambiarEnfermedades(val texto: String) : CuestionarioEvent
    data class CambiarTratamientos(val texto: String) : CuestionarioEvent
    data class CambiarCirugias(val texto: String) : CuestionarioEvent
    data object Guardar : CuestionarioEvent
    data object Regresar : CuestionarioEvent
}