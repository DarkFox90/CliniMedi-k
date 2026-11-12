package plat.clinimedik.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plat.clinimedik.app.ui.theme.CliniMedikTheme

/**
 * Contenido de una pantalla cuando no pudo cargar sus datos.
 * Con [onReintentar] en `null` no se muestra el botón.
 */
@Composable
fun EstadoError(
    mensaje: String,
    onReintentar: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error
        )
        Text(
            text = mensaje,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (onReintentar != null) {
            Button(onClick = onReintentar) {
                Text(text = "Reintentar")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EstadoErrorPreview() {
    CliniMedikTheme {
        EstadoError(
            mensaje = "No se pudo cargar la fila de pacientes",
            onReintentar = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EstadoErrorSinReintentoPreview() {
    CliniMedikTheme {
        EstadoError(
            mensaje = "No se encontró el paciente",
            onReintentar = null
        )
    }
}
