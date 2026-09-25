package plat.clinimedik.app.ui.components

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.Locale

private val localeEspanol: Locale = Locale.forLanguageTag("es-GT")
private val formatoHora = DateTimeFormatter.ofPattern("h:mm a", localeEspanol)
private val formatoFechaLarga = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", localeEspanol)
private val formatoFecha = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", localeEspanol)

fun formatearHora(fechaHora: LocalDateTime): String = fechaHora.format(formatoHora)

fun formatearFechaLarga(fecha: LocalDate): String =
    fecha.format(formatoFechaLarga).replaceFirstChar { it.titlecase(localeEspanol) }

fun formatearFecha(fecha: LocalDate): String = fecha.format(formatoFecha)

fun formatearEspera(desde: LocalDateTime, hasta: LocalDateTime): String {
    val minutos = Duration.between(desde, hasta).toMinutes().coerceAtLeast(0)
    return if (minutos < 60) "$minutos min" else "${minutos / 60} h ${minutos % 60} min"
}

fun calcularEdad(fechaNacimiento: LocalDate, hoy: LocalDate): Int =
    Period.between(fechaNacimiento, hoy).years

fun formatearMonto(monto: Double): String = String.format(Locale.US, "Q%,.2f", monto)