package plat.clinimedik.app.ui.screens.recepcion.agenda

import java.time.LocalDate
import plat.clinimedik.app.data.model.EstadoConfirmacion

sealed interface AgendaRecepcionEvent {
    data object NuevaCita : AgendaRecepcionEvent
    data object DiaAnterior : AgendaRecepcionEvent
    data object DiaSiguiente : AgendaRecepcionEvent
    data class ElegirDia(val fecha: LocalDate) : AgendaRecepcionEvent
    data object VolverAHoy : AgendaRecepcionEvent
    data object Reintentar : AgendaRecepcionEvent

    data class AbrirCita(val citaId: String) : AgendaRecepcionEvent
    data object CerrarPanel : AgendaRecepcionEvent

    data class MarcarConfirmacion(val estado: EstadoConfirmacion) : AgendaRecepcionEvent

    data object AbrirReprogramar : AgendaRecepcionEvent
    data class CambiarFechaReprogramar(val texto: String) : AgendaRecepcionEvent
    data class CambiarHoraReprogramar(val texto: String) : AgendaRecepcionEvent
    data object ConfirmarReprogramar : AgendaRecepcionEvent

    data object SolicitarCancelacion : AgendaRecepcionEvent
    data object ConfirmarCancelacion : AgendaRecepcionEvent

    data object RegistrarLlegada : AgendaRecepcionEvent
}
