package plat.clinimedik.app.ui.screens.recepcion.buscar

sealed interface BuscarEvent {
    data class CambiarTexto(val texto: String) : BuscarEvent
    data class AbrirPaciente(val pacienteId: String) : BuscarEvent
    data object NuevoPaciente : BuscarEvent
}
