package plat.clinimedik.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import plat.clinimedik.app.ui.theme.CliniMedikTheme

/**
 * Logo de CliniMedi-k: una cruz dentro de un cuadrado redondeado.
 * Toma los colores del tema, por eso se ve bien en modo claro y oscuro.
 * Es decorativo: la pantalla que lo usa ya muestra el nombre de la app.
 */
@Composable
fun LogoCliniMedik(
    modifier: Modifier = Modifier,
    tamano: Dp = 64.dp
) {
    val fondo = MaterialTheme.colorScheme.primary
    val cruz = MaterialTheme.colorScheme.onPrimary
    Canvas(modifier = modifier.size(tamano)) {
        val lado = size.minDimension
        drawRoundRect(color = fondo, cornerRadius = CornerRadius(lado * 0.22f))
        val largo = lado * 0.5f
        val grosor = lado * 0.16f
        val esquina = CornerRadius(grosor / 2)
        drawRoundRect(
            color = cruz,
            topLeft = Offset(center.x - largo / 2, center.y - grosor / 2),
            size = Size(largo, grosor),
            cornerRadius = esquina
        )
        drawRoundRect(
            color = cruz,
            topLeft = Offset(center.x - grosor / 2, center.y - largo / 2),
            size = Size(grosor, largo),
            cornerRadius = esquina
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LogoCliniMedikPreview() {
    CliniMedikTheme {
        Row {
            LogoCliniMedik(modifier = Modifier.padding(16.dp))
            LogoCliniMedik(modifier = Modifier.padding(16.dp), tamano = 40.dp)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF101413)
@Composable
private fun LogoCliniMedikOscuroPreview() {
    CliniMedikTheme(darkTheme = true) {
        LogoCliniMedik(modifier = Modifier.padding(16.dp))
    }
}
