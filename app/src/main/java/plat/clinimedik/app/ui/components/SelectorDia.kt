package plat.clinimedik.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import plat.clinimedik.app.ui.theme.CliniMedikTheme

/** El día que muestra una agenda y el día de hoy. */
data class DiaVisible(
    val fecha: LocalDate,
    val hoy: LocalDate
) {
    val esHoy: Boolean
        get() = fecha == hoy
}

/**
 * Guarda el día elegido en una agenda. Se crea dentro del ViewModel.
 * Mientras no se elige otro día, sigue al día de hoy y cambia solo a medianoche.
 */
class SeleccionDia {

    private val elegido = MutableStateFlow<LocalDate?>(null)

    val dia: Flow<DiaVisible> = combine(
        elegido,
        relojPorMinuto().map { it.toLocalDate() }.distinctUntilChanged()
    ) { fecha, hoy ->
        DiaVisible(fecha = fecha ?: hoy, hoy = hoy)
    }.distinctUntilChanged()

    fun anterior() = mover(-1L)

    fun siguiente() = mover(1L)

    fun elegir(fecha: LocalDate) {
        elegido.value = fecha.takeIf { it != LocalDate.now() }
    }

    fun volverAHoy() {
        elegido.value = null
    }

    private fun mover(dias: Long) {
        elegido.update { actual ->
            (actual ?: LocalDate.now()).plusDays(dias).takeIf { it != LocalDate.now() }
        }
    }
}

/**
 * Fila para cambiar el día de una agenda: flechas para el día anterior y el siguiente,
 * y la fecha, que abre el calendario para saltar a cualquier día. "Hoy" aparece cuando se mira otro día.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorDia(
    fecha: LocalDate,
    textoFecha: String,
    esHoy: Boolean,
    onDiaAnterior: () -> Unit,
    onDiaSiguiente: () -> Unit,
    onElegirDia: (LocalDate) -> Unit,
    onVolverAHoy: () -> Unit,
    modifier: Modifier = Modifier
) {
    var calendarioAbierto by rememberSaveable { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        IconButton(onClick = onDiaAnterior) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Día anterior"
            )
        }
        Surface(
            onClick = { calendarioAbierto = true },
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.weight(1f)
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = textoFecha,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        IconButton(onClick = onDiaSiguiente) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Día siguiente"
            )
        }
        if (!esHoy) {
            TextButton(onClick = onVolverAHoy) {
                Text(text = "Hoy")
            }
        }
    }

    if (calendarioAbierto) {
        val estado = rememberDatePickerState(initialSelectedDateMillis = fecha.aMilisUtc())
        DatePickerDialog(
            onDismissRequest = { calendarioAbierto = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        estado.selectedDateMillis?.let { milis -> onElegirDia(milisUtcAFecha(milis)) }
                        calendarioAbierto = false
                    },
                    enabled = estado.selectedDateMillis != null
                ) {
                    Text(text = "Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { calendarioAbierto = false }) {
                    Text(text = "Cancelar")
                }
            }
        ) {
            DatePicker(state = estado)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectorDiaPreview() {
    CliniMedikTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            SelectorDia(
                fecha = LocalDate.of(2026, 11, 12),
                textoFecha = "Jueves, 12 de noviembre",
                esHoy = true,
                onDiaAnterior = {},
                onDiaSiguiente = {},
                onElegirDia = {},
                onVolverAHoy = {}
            )
            SelectorDia(
                fecha = LocalDate.of(2026, 11, 13),
                textoFecha = "Viernes, 13 de noviembre",
                esHoy = false,
                onDiaAnterior = {},
                onDiaSiguiente = {},
                onElegirDia = {},
                onVolverAHoy = {}
            )
        }
    }
}
