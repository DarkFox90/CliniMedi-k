package plat.clinimedik.app.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import plat.clinimedik.app.ui.theme.CliniMedikTheme

private val formatoCampoFecha: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/uuuu")

/**
 * Campo de fecha que se llena con el calendario de Material 3, no con el teclado.
 * [valor] y [onValorChange] usan texto con el formato dd/mm/aaaa, el mismo que ya validan los ViewModels.
 * Con [permitePasado] o [permiteFuturo] en `false`, esos días aparecen deshabilitados en el calendario.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoFecha(
    valor: String,
    onValorChange: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    mensajeError: String? = null,
    permitePasado: Boolean = true,
    permiteFuturo: Boolean = true,
    habilitado: Boolean = true
) {
    var abierto by rememberSaveable { mutableStateOf(false) }

    CampoDeSeleccion(
        valor = valor,
        etiqueta = etiqueta,
        placeholder = "Toca para elegir la fecha",
        icono = Icons.Filled.DateRange,
        descripcionIcono = "Elegir fecha",
        onAbrir = { abierto = true },
        mensajeError = mensajeError,
        habilitado = habilitado,
        modifier = modifier
    )

    if (abierto) {
        val fechas = remember(permitePasado, permiteFuturo) {
            FechasPermitidas(LocalDate.now(), permitePasado, permiteFuturo)
        }
        val estado = rememberDatePickerState(
            initialSelectedDateMillis = textoAFecha(valor)?.takeIf { fechas.permite(it) }?.aMilisUtc(),
            yearRange = fechas.anios,
            selectableDates = fechas
        )
        DatePickerDialog(
            onDismissRequest = { abierto = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        estado.selectedDateMillis?.let { milis ->
                            onValorChange(milisUtcAFecha(milis).format(formatoCampoFecha))
                        }
                        abierto = false
                    },
                    enabled = estado.selectedDateMillis != null
                ) {
                    Text(text = "Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { abierto = false }) {
                    Text(text = "Cancelar")
                }
            }
        ) {
            DatePicker(state = estado)
        }
    }
}

/**
 * Campo de solo lectura que abre un selector al tocarlo. Lo comparten [CampoFecha] y [CampoHora].
 */
@Composable
internal fun CampoDeSeleccion(
    valor: String,
    etiqueta: String,
    placeholder: String,
    icono: ImageVector,
    descripcionIcono: String,
    onAbrir: () -> Unit,
    modifier: Modifier = Modifier,
    mensajeError: String? = null,
    habilitado: Boolean = true
) {
    val abrir by rememberUpdatedState(onAbrir)
    val interacciones = remember { MutableInteractionSource() }
    LaunchedEffect(interacciones) {
        interacciones.interactions.collect { interaccion ->
            if (interaccion is PressInteraction.Release) {
                abrir()
            }
        }
    }
    OutlinedTextField(
        value = valor,
        onValueChange = {},
        readOnly = true,
        enabled = habilitado,
        label = { Text(etiqueta) },
        placeholder = { Text(placeholder) },
        trailingIcon = {
            IconButton(onClick = onAbrir, enabled = habilitado) {
                Icon(imageVector = icono, contentDescription = descripcionIcono)
            }
        },
        isError = mensajeError != null,
        supportingText = if (mensajeError != null) {
            { Text(mensajeError) }
        } else {
            null
        },
        singleLine = true,
        interactionSource = interacciones,
        shape = MaterialTheme.shapes.small,
        modifier = modifier.fillMaxWidth()
    )
}

/** Días que se pueden elegir en un calendario. La usan `CampoFecha` y `SelectorDia`. */
internal class FechasPermitidas(
    private val hoy: LocalDate,
    private val permitePasado: Boolean,
    private val permiteFuturo: Boolean
) : SelectableDates {

    val anios: IntRange = IntRange(
        if (permitePasado) DatePickerDefaults.YearRange.first else hoy.year,
        if (permiteFuturo) DatePickerDefaults.YearRange.last else hoy.year
    )

    fun permite(fecha: LocalDate): Boolean =
        fecha.year in anios &&
            (permitePasado || !fecha.isBefore(hoy)) &&
            (permiteFuturo || !fecha.isAfter(hoy))

    override fun isSelectableDate(utcTimeMillis: Long): Boolean = permite(milisUtcAFecha(utcTimeMillis))

    override fun isSelectableYear(year: Int): Boolean = year in anios
}

private fun textoAFecha(texto: String): LocalDate? = try {
    LocalDate.parse(texto.trim(), formatoCampoFecha)
} catch (_: DateTimeParseException) {
    null
}

internal fun LocalDate.aMilisUtc(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

internal fun milisUtcAFecha(milis: Long): LocalDate =
    Instant.ofEpochMilli(milis).atZone(ZoneOffset.UTC).toLocalDate()

@Preview(showBackground = true)
@Composable
private fun CampoFechaPreview() {
    CliniMedikTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            CampoFecha(
                valor = "",
                onValorChange = {},
                etiqueta = "Fecha"
            )
            CampoFecha(
                valor = "24/09/2026",
                onValorChange = {},
                etiqueta = "Fecha de la cita",
                mensajeError = "La fecha no puede ser anterior a hoy"
            )
        }
    }
}
