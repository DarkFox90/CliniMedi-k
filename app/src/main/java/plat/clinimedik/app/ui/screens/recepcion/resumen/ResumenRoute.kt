package plat.clinimedik.app.ui.screens.recepcion.resumen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.time.Duration
import java.time.temporal.ChronoUnit
import kotlin.math.roundToLong
import plat.clinimedik.app.data.fake.FakeDataSource
import plat.clinimedik.app.data.model.EstadoVisita
import plat.clinimedik.app.data.model.TipoPago
import plat.clinimedik.app.data.model.Visita
import plat.clinimedik.app.ui.components.etiqueta
import plat.clinimedik.app.ui.components.formatearEspera
import plat.clinimedik.app.ui.components.formatearFechaLarga
import plat.clinimedik.app.ui.components.formatearHora
import plat.clinimedik.app.ui.components.formatearMonto

private fun Visita.terminoConsulta(): Boolean =
    estado == EstadoVisita.ATENDIDO || estado == EstadoVisita.REFERIDO

private fun formatearDuracion(minutos: Long): String =
    formatearEspera(FakeDataSource.ahora, FakeDataSource.ahora.plusMinutes(minutos))

private fun tiempoPromedio(terminadas: List<Visita>): String {
    val duraciones = terminadas.mapNotNull { visita ->
        val inicio = visita.horaInicioConsulta
        val fin = visita.horaFin
        if (inicio != null && fin != null) Duration.between(inicio, fin).toMinutes() else null
    }
    return if (duraciones.isEmpty()) "Sin datos" else formatearDuracion(duraciones.average().roundToLong())
}

private fun barrasDeAfluencia(visitas: List<Visita>): List<BarraAfluencia> {
    if (visitas.isEmpty()) return emptyList()
    val llegadasPorHora = visitas
        .groupingBy { it.horaLlegada.truncatedTo(ChronoUnit.HOURS) }
        .eachCount()
    val primera = llegadasPorHora.keys.min()
    val ultima = llegadasPorHora.keys.max()
    val maximo = llegadasPorHora.values.max()
    return generateSequence(primera) { it.plusHours(1) }
        .takeWhile { !it.isAfter(ultima) }
        .map { hora ->
            val llegadas = llegadasPorHora[hora] ?: 0
            BarraAfluencia(
                hora = formatearHora(hora),
                llegadas = llegadas,
                proporcion = llegadas.toFloat() / maximo
            )
        }
        .toList()
}

fun resumenUiStateDePrueba(): ResumenUiState {
    val medico = FakeDataSource.medicoActual
    val visitas = FakeDataSource.filaDeHoy(medico.id)
    val terminadas = visitas.filter { it.terminoConsulta() }
    val pacientesDelDia = visitas.map { it.pacienteId }.distinct()
    val nuevos = pacientesDelDia.count { FakeDataSource.esPacienteNuevo(it) }
    val cobros = visitas.mapNotNull { FakeDataSource.cobroDe(it.id) }
    return ResumenUiState(
        subtitulo = "${formatearFechaLarga(FakeDataSource.hoy)} · ${medico.nombre}",
        atendidos = terminadas.size,
        tiempoPromedio = tiempoPromedio(terminadas),
        nuevos = nuevos,
        recurrentes = pacientesDelDia.size - nuevos,
        cobrosPorTipo = TipoPago.entries.map { tipo ->
            CobroPorTipo(
                tipo = tipo.etiqueta(),
                monto = formatearMonto(cobros.filter { it.tipoPago == tipo }.sumOf { it.monto })
            )
        },
        totalDelDia = formatearMonto(cobros.sumOf { it.monto }),
        pendientesDeCobro = terminadas
            .filter { FakeDataSource.cobroDe(it.id) == null }
            .map { FakeDataSource.paciente(it.pacienteId)?.nombreCompleto.orEmpty() },
        afluencia = barrasDeAfluencia(visitas)
    )
}

@Composable
fun ResumenRoute() {
    val state = remember { resumenUiStateDePrueba() }
    ResumenScreen(state = state, onIntent = {})
}
