package plat.clinimedik.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import plat.clinimedik.app.ui.theme.CliniMedikTheme

enum class PestanaRecepcion(val etiqueta: String, val icono: ImageVector) {
    INICIO("Inicio", Icons.Filled.Home),
    FILA("Fila", Icons.AutoMirrored.Filled.List),
    AGENDA("Agenda", Icons.Filled.DateRange),
    BUSCAR("Buscar", Icons.Filled.Search)
}

enum class PestanaMedico(val etiqueta: String, val icono: ImageVector) {
    FILA("Fila", Icons.AutoMirrored.Filled.List),
    AGENDA("Agenda", Icons.Filled.DateRange),
    PACIENTES("Pacientes", Icons.Filled.Person)
}

@Composable
fun BarraNavegacionRecepcion(
    seleccion: PestanaRecepcion,
    onSeleccionar: (PestanaRecepcion) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier
    ) {
        PestanaRecepcion.entries.forEach { pestana ->
            NavigationBarItem(
                selected = pestana == seleccion,
                onClick = { onSeleccionar(pestana) },
                icon = { Icon(imageVector = pestana.icono, contentDescription = null) },
                label = { Text(pestana.etiqueta) }
            )
        }
    }
}

@Composable
fun BarraNavegacionMedico(
    seleccion: PestanaMedico,
    onSeleccionar: (PestanaMedico) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier
    ) {
        PestanaMedico.entries.forEach { pestana ->
            NavigationBarItem(
                selected = pestana == seleccion,
                onClick = { onSeleccionar(pestana) },
                icon = { Icon(imageVector = pestana.icono, contentDescription = null) },
                label = { Text(pestana.etiqueta) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BarraNavegacionRecepcionPreview() {
    CliniMedikTheme {
        BarraNavegacionRecepcion(seleccion = PestanaRecepcion.FILA, onSeleccionar = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun BarraNavegacionMedicoPreview() {
    CliniMedikTheme {
        BarraNavegacionMedico(seleccion = PestanaMedico.AGENDA, onSeleccionar = {})
    }
}