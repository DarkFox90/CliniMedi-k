package plat.clinimedik.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
private val horas: List<Int> = (0..23).toList()
private val minutos: List<Int> = (0..59).toList()

/**
 * Campo de hora que se llena con dos ruedas (horas y minutos, en 24 horas), no con el teclado.
 * [valor] y [onValorChange] usan texto en 24 horas (por ejemplo 14:30), el mismo que ya validan los ViewModels.
 */
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
        valor = hora?.format(formatoCampoHora) ?: valor,
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
        DialogoHora(
            inicial = hora ?: horaInicial,
            onAceptar = { elegida ->
                onValorChange(elegida.format(formatoCampoHora))
                abierto = false
            },
            onCancelar = { abierto = false }
        )
    }
}

@Composable
private fun DialogoHora(
    inicial: LocalTime,
    onAceptar: (LocalTime) -> Unit,
    onCancelar: () -> Unit
) {
    var horaElegida by rememberSaveable { mutableIntStateOf(inicial.hour) }
    var minutoElegido by rememberSaveable { mutableIntStateOf(inicial.minute) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(text = "Selecciona la hora") },
        text = {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                RuedaNumeros(
                    valores = horas,
                    seleccionado = horaElegida,
                    onSeleccionChange = { horaElegida = it },
                    descripcion = "Hora"
                )
                Text(
                    text = ":",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                RuedaNumeros(
                    valores = minutos,
                    seleccionado = minutoElegido,
                    onSeleccionChange = { minutoElegido = it },
                    descripcion = "Minutos"
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onAceptar(LocalTime.of(horaElegida, minutoElegido)) }) {
                Text(text = "Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text(text = "Cancelar")
            }
        }
    )
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

@Preview(showBackground = true)
@Composable
private fun DialogoHoraPreview() {
    CliniMedikTheme {
        DialogoHora(
            inicial = LocalTime.of(14, 30),
            onAceptar = {},
            onCancelar = {}
        )
    }
}
