package plat.clinimedik.app.ui.screens.recepcion.fila

sealed interface FilaRecepcionEvent {
    data class SeleccionarFiltro(val filtro: FiltroFila) : FilaRecepcionEvent
    data class AbrirPaciente(val visitaId: String) : FilaRecepcionEvent
    data class SolicitarUrgencia(val visitaId: String) : FilaRecepcionEvent
    data class CambiarMotivoUrgencia(val motivo: String) : FilaRecepcionEvent
    data object ConfirmarUrgencia : FilaRecepcionEvent
    data object CancelarUrgencia : FilaRecepcionEvent
    data object NuevoPaciente : FilaRecepcionEvent
}