package plat.clinimedik.app.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plat.clinimedik.app.ui.theme.CliniMedikTheme

@Composable
fun BotonNuevoPaciente(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        icon = { Icon(imageVector = Icons.Filled.Add, contentDescription = null) },
        text = { Text("Nuevo paciente") },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun BotonNuevoPacientePreview() {
    CliniMedikTheme {
        BotonNuevoPaciente(onClick = {}, modifier = Modifier.padding(16.dp))
    }
}