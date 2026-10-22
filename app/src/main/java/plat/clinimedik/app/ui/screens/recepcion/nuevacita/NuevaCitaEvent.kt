package plat.clinimedik.app.ui.screens.recepcion.nuevacita

sealed interface NuevaCitaEvent {
    data class CambiarPaciente(val texto: String) : NuevaCitaEvent
    data class CambiarCorreo(val texto: String) : NuevaCitaEvent
    data class CambiarFecha(val texto: String) : NuevaCitaEvent
    data class CambiarHora(val texto: String) : NuevaCitaEvent
    data class CambiarMotivo(val texto: String) : NuevaCitaEvent
    data object Guardar : NuevaCitaEvent
    data object Regresar : NuevaCitaEvent
}