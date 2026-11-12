package plat.clinimedik.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import plat.clinimedik.app.ui.theme.CliniMedikTheme

private val formatoLecturaHora: DateTimeFormatter = DateTimeFormatter.ofPattern("H:mm")
private val formatoCampoHora: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val horaInicial: LocalTime = LocalTime.of(9, 0)

/**
 * Campo de hora que se llena con el reloj de Material 3, no con el teclado.
 * [valor] y [onValorChange] usan texto en 24 horas (por ejemplo 14:30), el mismo que ya validan los ViewModels.
 * En pantalla se muestra como el resto de la app (2:30 p. m.).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoHora(
    valor: String,
    onValorChange: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    mensajeError: String? = null,
    habilitado: Boolean = true
) {
    var abierto by rememberSaveable { mutableStateOf(false) }
    val hora = textoAHora(valor)

    CampoDeSeleccion(
        valor = hora?.let { formatearHora(it) } ?: valor,
        etiqueta = etiqueta,
        placeholder = "Toca para elegir la hora",
        icono = Icons.Filled.KeyboardArrowDown,
        descripcionIcono = "Elegir hora",
        onAbrir = { abierto = true },
        mensajeError = mensajeError,
        habilitado = habilitado,
        modifier = modifier
    )

    if (abierto) {
        val inicial = hora ?: horaInicial
        val estado = rememberTimePickerState(
            initialHour = inicial.hour,
            initialMinute = inicial.minute,
            is24Hour = false
        )
        TimePickerDialog(
            onDismissRequest = { abierto = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onValorChange(LocalTime.of(estado.hour, estado.minute).format(formatoCampoHora))
                        abierto = false
                    }
                ) {
                    Text(text = "Aceptar")
                }
            },
            title = { Text(text = "Selecciona la hora") },
            dismissButton = {
                TextButton(onClick = { abierto = false }) {
                    Text(text = "Cancelar")
                }
            }
        ) {
            TimePicker(state = estado)
        }
    }
}

private fun textoAHora(texto: String): LocalTime? = try {
    LocalTime.parse(texto.trim(), formatoLecturaHora)
} catch (_: DateTimeParseException) {
    null
}

@Preview(showBackground = true)
@Composable
private fun CampoHoraPreview() {
    CliniMedikTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            CampoHora(
                valor = "",
                onValorChange = {},
                etiqueta = "Hora"
            )
            CampoHora(
                valor = "14:30",
                onValorChange = {},
                etiqueta = "Hora",
                mensajeError = "Ya existe una cita en ese horario"
            )
        }
    }
}
