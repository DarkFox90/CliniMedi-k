package plat.clinimedik.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plat.clinimedik.app.data.model.EstadoConfirmacion
import plat.clinimedik.app.data.model.EstadoVisita
import plat.clinimedik.app.ui.theme.CliniMedikTheme

@Composable
fun EtiquetaChip(
    texto: String,
    fondo: Color,
    contenido: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = fondo,
        contentColor = contenido,
        shape = MaterialTheme.shapes.extraSmall,
        modifier = modifier
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun EstadoChip(
    estado: EstadoVisita,
    esUrgente: Boolean,
    modifier: Modifier = Modifier
) {
    val colores = MaterialTheme.colorScheme
    if (esUrgente && estado == EstadoVisita.ESPERANDO) {
        EtiquetaChip(
            texto = "Urgente",
            fondo = colores.errorContainer,
            contenido = colores.onErrorContainer,
            modifier = modifier
        )
    } else {
        val (fondo, contenido) = when (estado) {
            EstadoVisita.ESPERANDO -> colores.surfaceVariant to colores.onSurfaceVariant
            EstadoVisita.EN_CONSULTA -> colores.primaryContainer to colores.onPrimaryContainer
            EstadoVisita.REFERIDO -> colores.tertiaryContainer to colores.onTertiaryContainer
            EstadoVisita.ATENDIDO -> colores.surfaceContainerHigh to colores.onSurfaceVariant
        }
        EtiquetaChip(
            texto = estado.etiqueta(),
            fondo = fondo,
            contenido = contenido,
            modifier = modifier
        )
    }
}

@Composable
fun ConfirmacionChip(
    estado: EstadoConfirmacion,
    modifier: Modifier = Modifier
) {
    val colores = MaterialTheme.colorScheme
    val (fondo, contenido) = when (estado) {
        EstadoConfirmacion.CONFIRMADA -> colores.primaryContainer to colores.onPrimaryContainer
        EstadoConfirmacion.PENDIENTE -> colores.tertiaryContainer to colores.onTertiaryContainer
        EstadoConfirmacion.NO_CONFIRMADA -> colores.errorContainer to colores.onErrorContainer
    }
    EtiquetaChip(
        texto = estado.etiqueta(),
        fondo = fondo,
        contenido = contenido,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun EstadoChipPreview() {
    CliniMedikTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            EstadoChip(estado = EstadoVisita.ESPERANDO, esUrgente = true)
            EstadoChip(estado = EstadoVisita.ESPERANDO, esUrgente = false)
            EstadoChip(estado = EstadoVisita.EN_CONSULTA, esUrgente = false)
            EstadoChip(estado = EstadoVisita.REFERIDO, esUrgente = false)
            EstadoChip(estado = EstadoVisita.ATENDIDO, esUrgente = false)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ConfirmacionChipPreview() {
    CliniMedikTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            ConfirmacionChip(estado = EstadoConfirmacion.CONFIRMADA)
            ConfirmacionChip(estado = EstadoConfirmacion.PENDIENTE)
            ConfirmacionChip(estado = EstadoConfirmacion.NO_CONFIRMADA)
        }
    }
}