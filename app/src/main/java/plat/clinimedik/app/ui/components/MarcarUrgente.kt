package plat.clinimedik.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plat.clinimedik.app.ui.theme.CliniMedikTheme

@Composable
fun MarcarUrgenteContenido(
    nombrePaciente: String,
    motivo: String,
    onMotivoChange: (String) -> Unit,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
    modifier: Modifier = Modifier,
    mensajeError: String? = null
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        Text(
            text = "Marcar como urgente",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "$nombrePaciente pasará al inicio de la fila de espera.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        CampoTexto(
            valor = motivo,
            onValorChange = onMotivoChange,
            etiqueta = "Motivo de la prioridad",
            placeholder = "Ej. dolor torácico con dificultad para respirar",
            mensajeError = mensajeError,
            lineas = 3
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = onCancelar,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancelar")
            }
            Button(
                onClick = onConfirmar,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("Marcar urgente")
            }
        }
    }
}

@Composable
fun MarcarUrgenteSheet(
    nombrePaciente: String,
    motivo: String,
    onMotivoChange: (String) -> Unit,
    onConfirmar: () -> Unit,
    onDescartar: () -> Unit,
    mensajeError: String? = null
) {
    PanelInferior(onDescartar = onDescartar) {
        MarcarUrgenteContenido(
            nombrePaciente = nombrePaciente,
            motivo = motivo,
            onMotivoChange = onMotivoChange,
            onConfirmar = onConfirmar,
            onCancelar = onDescartar,
            mensajeError = mensajeError
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MarcarUrgenteVacioPreview() {
    CliniMedikTheme {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = MaterialTheme.shapes.extraLarge
        ) {
            MarcarUrgenteContenido(
                nombrePaciente = "Marta Ríos",
                motivo = "",
                onMotivoChange = {},
                onConfirmar = {},
                onCancelar = {},
                modifier = Modifier.padding(24.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MarcarUrgenteConMotivoPreview() {
    CliniMedikTheme {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = MaterialTheme.shapes.extraLarge
        ) {
            MarcarUrgenteContenido(
                nombrePaciente = "Marta Ríos",
                motivo = "Presión arterial de 180/110 con dolor de cabeza intenso",
                onMotivoChange = {},
                onConfirmar = {},
                onCancelar = {},
                modifier = Modifier.padding(24.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MarcarUrgenteErrorPreview() {
    CliniMedikTheme {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = MaterialTheme.shapes.extraLarge
        ) {
            MarcarUrgenteContenido(
                nombrePaciente = "Marta Ríos",
                motivo = "",
                onMotivoChange = {},
                onConfirmar = {},
                onCancelar = {},
                mensajeError = "Escribe el motivo de la prioridad",
                modifier = Modifier.padding(24.dp)
            )
        }
    }
}