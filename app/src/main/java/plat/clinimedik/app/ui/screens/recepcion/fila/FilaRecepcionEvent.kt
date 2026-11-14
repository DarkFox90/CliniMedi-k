package plat.clinimedik.app.ui.screens.recepcion.fila

import java.time.LocalDate

sealed interface FilaRecepcionEvent {
    data class SeleccionarFiltro(val filtro: FiltroFila) : FilaRecepcionEvent
    data class AbrirPaciente(val visitaId: String) : FilaRecepcionEvent
    data class SolicitarUrgencia(val visitaId: String) : FilaRecepcionEvent
    data class CambiarMotivoUrgencia(val motivo: String) : FilaRecepcionEvent
    data object ConfirmarUrgencia : FilaRecepcionEvent
    data object CancelarUrgencia : FilaRecepcionEvent
    data object NuevoPaciente : FilaRecepcionEvent
    data object DiaAnterior : FilaRecepcionEvent
    data object DiaSiguiente : FilaRecepcionEvent
    data class ElegirDia(val fecha: LocalDate) : FilaRecepcionEvent
    data object VolverAHoy : FilaRecepcionEvent
    data object Reintentar : FilaRecepcionEvent
}
