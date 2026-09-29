package plat.clinimedik.app.ui.screens.recepcion.buscar

sealed interface BuscarIntent {
    data class CambiarTexto(val texto: String) : BuscarIntent
    data class AbrirPaciente(val pacienteId: String) : BuscarIntent
    data object NuevoPaciente : BuscarIntent
}
