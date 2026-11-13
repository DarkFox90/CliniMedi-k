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

private val formatoMontoLeido = Regex("""\d+(\.\d{1,2})?""")

/**
 * Convierte en número un monto escrito por el usuario, o devuelve `null` si no es válido.
 * Acepta comas de miles (1,500.50) y hasta dos decimales. Si solo hay una coma seguida de
 * uno o dos dígitos (150,5 o 150,50), la toma como punto decimal, porque algunos teclados la usan así.
 */
fun leerMonto(texto: String): Double? {
    val sinEspacios = texto.trim().replace(" ", "")
    if (sinEspacios.isEmpty()) return null
    val normalizado = when {
        sinEspacios.contains('.') -> sinEspacios.replace(",", "")
        sinEspacios.count { it == ',' } == 1 && sinEspacios.substringAfter(',').length in 1..2 ->
            sinEspacios.replace(',', '.')
        else -> sinEspacios.replace(",", "")
    }
    if (!formatoMontoLeido.matches(normalizado)) return null
    return normalizado.toDoubleOrNull()
}