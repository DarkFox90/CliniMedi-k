package plat.clinimedik.app.ui.screens.recepcion.fila

sealed interface FilaRecepcionIntent {
    data class SeleccionarFiltro(val filtro: FiltroFila) : FilaRecepcionIntent
    data class AbrirPaciente(val visitaId: String) : FilaRecepcionIntent
    data class SolicitarUrgencia(val visitaId: String) : FilaRecepcionIntent
    data class CambiarMotivoUrgencia(val motivo: String) : FilaRecepcionIntent
    data object ConfirmarUrgencia : FilaRecepcionIntent
    data object CancelarUrgencia : FilaRecepcionIntent
    data object NuevoPaciente : FilaRecepcionIntent
}