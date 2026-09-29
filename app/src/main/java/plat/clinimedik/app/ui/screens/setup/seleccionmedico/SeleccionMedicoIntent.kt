package plat.clinimedik.app.ui.screens.setup.seleccionmedico

sealed interface SeleccionMedicoIntent {
    data class SeleccionarMedico(val id: String) : SeleccionMedicoIntent
    data object MostrarFormulario : SeleccionMedicoIntent
    data class CambiarNombre(val texto: String) : SeleccionMedicoIntent
    data class CambiarEspecialidad(val texto: String) : SeleccionMedicoIntent
    data object Continuar : SeleccionMedicoIntent
}
