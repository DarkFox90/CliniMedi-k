package plat.clinimedik.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import plat.clinimedik.app.ui.theme.CliniMedikTheme

@Composable
fun TarjetaMetrica(
    titulo: String,
    valor: String,
    modifier: Modifier = Modifier,
    fondo: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    contenido: Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        color = fondo,
        contentColor = contenido,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = valor,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TarjetaMetricaPreview() {
    CliniMedikTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            TarjetaMetrica(
                titulo = "Esperando",
                valor = "3",
                modifier = Modifier.weight(1f)
            )
            TarjetaMetrica(
                titulo = "Urgentes",
                valor = "1",
                fondo = MaterialTheme.colorScheme.errorContainer,
                contenido = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            TarjetaMetrica(
                titulo = "Atendidos",
                valor = "2",
                modifier = Modifier.weight(1f)
            )
        }
    }
}