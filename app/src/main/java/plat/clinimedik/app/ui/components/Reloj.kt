package plat.clinimedik.app.ui.components

import java.time.LocalDateTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

private const val MILISEGUNDOS_POR_MINUTO = 60_000L

/**
 * Emite la hora actual al empezar a observarlo y otra vez al inicio de cada minuto.
 * Se combina con los datos de una pantalla para que los tiempos de espera avancen solos:
 *
 *     combine(visitaRepositorio.observarFilaDelDia(medicoId), relojPorMinuto()) { fila, ahora -> ... }
 */
fun relojPorMinuto(): Flow<LocalDateTime> = flow {
    while (true) {
        val ahora = LocalDateTime.now()
        emit(ahora)
        val transcurrido = ahora.second * 1_000L + ahora.nano / 1_000_000L
        delay(MILISEGUNDOS_POR_MINUTO - transcurrido)
    }
}
