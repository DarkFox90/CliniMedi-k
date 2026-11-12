package plat.clinimedik.app.ui.screens.recepcion.agenda

import java.time.LocalDate

sealed interface AgendaRecepcionEvent {
    data object NuevaCita : AgendaRecepcionEvent
    data object DiaAnterior : AgendaRecepcionEvent
    data object DiaSiguiente : AgendaRecepcionEvent
    data class ElegirDia(val fecha: LocalDate) : AgendaRecepcionEvent
    data object VolverAHoy : AgendaRecepcionEvent
    data object Reintentar : AgendaRecepcionEvent
}
