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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plat.clinimedik.app.data.model.EstadoCita
import plat.clinimedik.app.data.model.EstadoConfirmacion
import plat.clinimedik.app.ui.theme.CliniMedikTheme

@Composable
fun CitaItem(
    hora: String,
    nombrePaciente: String,
    motivo: String,
    estadoConfirmacion: EstadoConfirmacion,
    modifier: Modifier = Modifier,
    recordatorioEnviado: Boolean? = null,
    estado: EstadoCita = EstadoCita.PROGRAMADA,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .alpha(if (estado == EstadoCita.CANCELADA) 0.6f else 1f)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                text = hora,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(76.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nombrePaciente,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = motivo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (recordatorioEnviado != null && estado == EstadoCita.PROGRAMADA) {
                    Text(
                        text = if (recordatorioEnviado) "Correo enviado" else "Correo pendiente de envío",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            if (estado == EstadoCita.PROGRAMADA) {
                ConfirmacionChip(estado = estadoConfirmacion)
            } else {
                EstadoCitaChip(estado = estado)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CitaItemPreview() {
    CliniMedikTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            CitaItem(
                hora = "9:30 a. m.",
                nombrePaciente = "Marta Ríos",
                motivo = "Control de presión",
                estadoConfirmacion = EstadoConfirmacion.CONFIRMADA,
                recordatorioEnviado = true
            )
            CitaItem(
                hora = "11:00 a. m.",
                nombrePaciente = "Sofía Hernández",
                motivo = "Primera consulta",
                estadoConfirmacion = EstadoConfirmacion.PENDIENTE
            )
            CitaItem(
                hora = "12:00 p. m.",
                nombrePaciente = "Pedro Gómez",
                motivo = "Control",
                estadoConfirmacion = EstadoConfirmacion.CONFIRMADA,
                recordatorioEnviado = true,
                estado = EstadoCita.CANCELADA
            )
            CitaItem(
                hora = "8:30 a. m.",
                nombrePaciente = "Lucía Morales",
                motivo = "Chequeo general",
                estadoConfirmacion = EstadoConfirmacion.CONFIRMADA,
                estado = EstadoCita.COMPLETADA
            )
        }
    }
}