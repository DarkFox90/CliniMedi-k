package plat.clinimedik.app.ui.screens.medico.agenda

import java.time.LocalDate

sealed interface AgendaMedicoEvent {
    data object DiaAnterior : AgendaMedicoEvent
    data object DiaSiguiente : AgendaMedicoEvent
    data class ElegirDia(val fecha: LocalDate) : AgendaMedicoEvent
    data object VolverAHoy : AgendaMedicoEvent
    data object Reintentar : AgendaMedicoEvent
}
