package plat.clinimedik.app.data.repository

import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow

private const val MILISEGUNDOS_ENTRE_REVISIONES = 60_000L

internal fun inicioDelDia(fecha: LocalDate): LocalDateTime = fecha.atStartOfDay()

internal fun finDelDia(fecha: LocalDate): LocalDateTime = fecha.plusDays(1).atStartOfDay()

/**
 * Emite la fecha de hoy y vuelve a emitir cuando cambia el día.
 * Así las consultas "del día" no se quedan en el día anterior si la app sigue abierta a medianoche.
 */
internal fun diaActual(): Flow<LocalDate> = flow {
    while (true) {
        emit(LocalDate.now())
        delay(MILISEGUNDOS_ENTRE_REVISIONES)
    }
}.distinctUntilChanged()
