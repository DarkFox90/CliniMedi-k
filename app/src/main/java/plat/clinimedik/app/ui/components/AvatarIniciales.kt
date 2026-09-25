package plat.clinimedik.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import plat.clinimedik.app.ui.theme.CliniMedikTheme

@Composable
fun AvatarIniciales(
    nombre: String,
    modifier: Modifier = Modifier,
    destacado: Boolean = false,
    tamano: Dp = 44.dp
) {
    val iniciales = nombre
        .split(" ")
        .filter { it.isNotBlank() && !it.endsWith(".") }
        .take(2)
        .joinToString("") { it.first().uppercase() }
    val fondo = if (destacado) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
    val contenido = if (destacado) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(tamano)
            .clip(CircleShape)
            .background(fondo)
    ) {
        Text(
            text = iniciales,
            style = MaterialTheme.typography.titleSmall,
            color = contenido
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AvatarInicialesPreview() {
    CliniMedikTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            AvatarIniciales(nombre = "Marta Ríos")
            AvatarIniciales(nombre = "Álvaro Chacón", destacado = true)
            AvatarIniciales(nombre = "Dr. Roberto Sical")
        }
    }
}