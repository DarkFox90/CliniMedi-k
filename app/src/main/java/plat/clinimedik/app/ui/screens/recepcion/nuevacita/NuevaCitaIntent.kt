package plat.clinimedik.app.ui.screens.recepcion.nuevacita

sealed interface NuevaCitaIntent {
    data class CambiarPaciente(val texto: String) : NuevaCitaIntent
    data class CambiarCorreo(val texto: String) : NuevaCitaIntent
    data class CambiarFecha(val texto: String) : NuevaCitaIntent
    data class CambiarHora(val texto: String) : NuevaCitaIntent
    data class CambiarMotivo(val texto: String) : NuevaCitaIntent
    data object Guardar : NuevaCitaIntent
    data object Regresar : NuevaCitaIntent
}