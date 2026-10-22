package plat.clinimedik.app.ui.screens.setup.seleccionmedico

sealed interface SeleccionMedicoEvent {
    data class SeleccionarMedico(val id: String) : SeleccionMedicoEvent
    data object MostrarFormulario : SeleccionMedicoEvent
    data class CambiarNombre(val texto: String) : SeleccionMedicoEvent
    data class CambiarEspecialidad(val texto: String) : SeleccionMedicoEvent
    data object Continuar : SeleccionMedicoEvent
}
