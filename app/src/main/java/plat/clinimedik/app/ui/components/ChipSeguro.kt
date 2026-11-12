package plat.clinimedik.app.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plat.clinimedik.app.ui.theme.CliniMedikTheme

@Composable
fun ChipSeguro(
    aseguradora: String,
    modifier: Modifier = Modifier
) {
    EtiquetaChip(
        texto = "Asegurado · $aseguradora",
        fondo = MaterialTheme.colorScheme.secondaryContainer,
        contenido = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun ChipSeguroPreview() {
    CliniMedikTheme {
        ChipSeguro(
            aseguradora = "Seguros G&T",
            modifier = Modifier.padding(16.dp)
        )
    }
}
