package plat.clinimedik.app.ui.screens.recepcion.inicio

sealed interface InicioRecepcionEvent {
    data class AbrirPaciente(val visitaId: String) : InicioRecepcionEvent
    data object NuevoPaciente : InicioRecepcionEvent
    data object EscanearQr : InicioRecepcionEvent
    data object VerResumen : InicioRecepcionEvent
    data class AbrirCita(val citaId: String) : InicioRecepcionEvent
    data object AbrirMenu : InicioRecepcionEvent
    data object CerrarMenu : InicioRecepcionEvent
    data object CambiarConfiguracion : InicioRecepcionEvent
    data object SolicitarCierreSesion : InicioRecepcionEvent
    data object CancelarCierreSesion : InicioRecepcionEvent
    data object ConfirmarCierreSesion : InicioRecepcionEvent
    data object Reintentar : InicioRecepcionEvent
}