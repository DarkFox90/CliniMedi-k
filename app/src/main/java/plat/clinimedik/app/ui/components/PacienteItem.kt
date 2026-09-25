package plat.clinimedik.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plat.clinimedik.app.data.model.EstadoVisita
import plat.clinimedik.app.ui.theme.CliniMedikTheme

@Composable
fun PacienteItem(
    nombre: String,
    detalle: String,
    modifier: Modifier = Modifier,
    tiempo: String? = null,
    estado: EstadoVisita? = null,
    esUrgente: Boolean = false,
    onClick: () -> Unit = {}
) {
    val destacado = esUrgente && estado == EstadoVisita.ESPERANDO
    val fondo = if (destacado) {
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerLowest
    }

    Surface(
        onClick = onClick,
        color = fondo,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            AvatarIniciales(nombre = nombre, destacado = destacado)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nombre,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = detalle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (estado != null || tiempo != null) {
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (estado != null) {
                        EstadoChip(estado = estado, esUrgente = esUrgente)
                    }
                    if (tiempo != null) {
                        Text(
                            text = tiempo,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PacienteItemPreview() {
    CliniMedikTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            PacienteItem(
                nombre = "Álvaro Chacón",
                detalle = "Dolor torácico",
                tiempo = "20 min",
                estado = EstadoVisita.ESPERANDO,
                esUrgente = true
            )
            PacienteItem(
                nombre = "Julio Estrada",
                detalle = "Chequeo general",
                tiempo = "45 min",
                estado = EstadoVisita.EN_CONSULTA
            )
            PacienteItem(
                nombre = "Pedro Coc",
                detalle = "5877-9900"
            )
        }
    }
}