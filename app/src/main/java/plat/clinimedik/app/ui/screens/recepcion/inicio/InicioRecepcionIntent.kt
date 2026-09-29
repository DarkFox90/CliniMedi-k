package plat.clinimedik.app.ui.screens.recepcion.inicio

sealed interface InicioRecepcionIntent {
    data class AbrirPaciente(val visitaId: String) : InicioRecepcionIntent
    data object NuevoPaciente : InicioRecepcionIntent
    data object EscanearQr : InicioRecepcionIntent
    data object VerResumen : InicioRecepcionIntent
    data class AbrirCita(val citaId: String) : InicioRecepcionIntent
}
