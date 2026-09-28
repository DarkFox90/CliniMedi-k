package plat.clinimedik.app.ui.screens.setup.rol

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plat.clinimedik.app.data.model.RolDispositivo
import plat.clinimedik.app.ui.components.EncabezadoPantalla
import plat.clinimedik.app.ui.theme.CliniMedikTheme

@Composable
fun RolScreen(
    state: RolUiState,
    onIntent: (RolIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            EncabezadoPantalla(
                titulo = "Configura este dispositivo",
                subtitulo = "Configuración inicial"
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Selecciona cómo se usará este teléfono en la clínica",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.selectableGroup()
            ) {
                state.opciones.forEach { opcion ->
                    TarjetaRol(
                        opcion = opcion,
                        seleccionada = opcion.rol == state.rolSeleccionado,
                        onClick = { onIntent(RolIntent.SeleccionarRol(opcion.rol)) }
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onIntent(RolIntent.Continuar) },
                enabled = state.puedeContinuar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Continuar")
            }
        }
    }
}

@Composable
private fun TarjetaRol(
    opcion: OpcionRol,
    seleccionada: Boolean,
    onClick: () -> Unit
) {
    val colorFondo = if (seleccionada) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerLowest
    }
    val colorTitulo = if (seleccionada) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val colorDescripcion = if (seleccionada) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        selected = seleccionada,
        onClick = onClick,
        color = colorFondo,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = opcion.rol.icono(),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = opcion.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    color = colorTitulo
                )
                Text(
                    text = opcion.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorDescripcion
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            RadioButton(
                selected = seleccionada,
                onClick = null
            )
        }
    }
}

private fun RolDispositivo.icono(): ImageVector = when (this) {
    RolDispositivo.RECEPCION -> Icons.Filled.Edit
    RolDispositivo.MEDICO -> Icons.Filled.Person
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RolRecepcionSeleccionadaPreview() {
    CliniMedikTheme {
        RolScreen(
            state = rolUiStateDePrueba(rolSeleccionado = RolDispositivo.RECEPCION),
            onIntent = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RolMedicoSeleccionadoPreview() {
    CliniMedikTheme {
        RolScreen(
            state = rolUiStateDePrueba(rolSeleccionado = RolDispositivo.MEDICO),
            onIntent = {}
        )
    }
}
