package plat.clinimedik.app.ui.screens.recepcion.resumen

import java.time.Duration
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.roundToLong
import plat.clinimedik.app.data.model.Cobro
import plat.clinimedik.app.data.model.EstadoVisita
import plat.clinimedik.app.data.model.TipoPago
import plat.clinimedik.app.data.model.Visita
import plat.clinimedik.app.data.model.VisitaConPaciente
import plat.clinimedik.app.ui.components.etiqueta
import plat.clinimedik.app.ui.components.formatearEspera
import plat.clinimedik.app.ui.components.formatearFechaLarga
import plat.clinimedik.app.ui.components.formatearHora
import plat.clinimedik.app.ui.components.formatearMonto

data class CobroPorTipo(
    val tipo: String,
    val monto: String
)

data class BarraAfluencia(
    val hora: String,
    val llegadas: Int,
    val proporcion: Float
)

data class ResumenUiState(
    val cargando: Boolean = true,
    val subtitulo: String = "",
    val atendidos: Int = 0,
    val tiempoPromedio: String = "",
    val nuevos: Int = 0,
    val recurrentes: Int = 0,
    val cobrosPorTipo: List<CobroPorTipo> = emptyList(),
    val totalDelDia: String = "",
    val pendientesDeCobro: List<String> = emptyList(),
    val afluencia: List<BarraAfluencia> = emptyList()
) {
    val textoAfluencia: String
        get() {
            val pico = afluencia.maxByOrNull { it.llegadas }
            return if (pico == null || pico.llegadas == 0) {
                "Sin llegadas registradas hoy"
            } else {
                val llegadas = if (pico.llegadas == 1) "1 llegada" else "${pico.llegadas} llegadas"
                "Mayor afluencia: ${pico.hora} · $llegadas"
            }
        }
}


fun calcularResumen(
    fila: List<VisitaConPaciente>,
    cobros: List<Cobro>,
    nombreMedico: String,
    hoy: LocalDate
): ResumenUiState {
    val visitas = fila.map { it.visita }
    val terminadas = fila.filter { it.visita.terminoConsulta() }
    val pacientesDelDia = fila.map { it.paciente }.distinctBy { it.id }
    val nuevos = pacientesDelDia.count { it.fechaRegistro.toLocalDate() == hoy }
    val visitasConCobro = cobros.map { it.visitaId }.toSet()
    return ResumenUiState(
        cargando = false,
        subtitulo = "${formatearFechaLarga(hoy)} · $nombreMedico",
        atendidos = terminadas.size,
        tiempoPromedio = tiempoPromedio(terminadas.map { it.visita }, hoy),
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
            .filter { it.visita.id !in visitasConCobro }
            .map { it.paciente.nombreCompleto },
        afluencia = barrasDeAfluencia(visitas)
    )
}

private fun Visita.terminoConsulta(): Boolean =
    estado == EstadoVisita.ATENDIDO || estado == EstadoVisita.REFERIDO

private fun tiempoPromedio(terminadas: List<Visita>, hoy: LocalDate): String {
    val duraciones = terminadas.mapNotNull { visita ->
        val inicio = visita.horaInicioConsulta
        val fin = visita.horaFin
        if (inicio != null && fin != null) Duration.between(inicio, fin).toMinutes() else null
    }
    if (duraciones.isEmpty()) return "Sin datos"
    val base = hoy.atStartOfDay()
    return formatearEspera(base, base.plusMinutes(duraciones.average().roundToLong()))
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
