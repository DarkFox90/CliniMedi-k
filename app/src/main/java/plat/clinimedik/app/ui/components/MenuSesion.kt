package plat.clinimedik.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plat.clinimedik.app.ui.theme.CliniMedikTheme

/** Estado del menú de tres puntos. Lo guarda el ViewModel de cada pantalla que lo usa. */
data class MenuSesionUiState(
    val menuAbierto: Boolean = false,
    val confirmandoCierre: Boolean = false,
    val cerrandoSesion: Boolean = false
)

/**
 * Botón de tres puntos con "Cambiar configuración" y "Cerrar sesión",
 * y el diálogo que confirma el cierre. No guarda estado propio.
 */
@Composable
fun MenuSesion(
    state: MenuSesionUiState,
    onAbrirMenu: () -> Unit,
    onCerrarMenu: () -> Unit,
    onCambiarConfiguracion: () -> Unit,
    onSolicitarCierre: () -> Unit,
    onCancelarCierre: () -> Unit,
    onConfirmarCierre: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        IconButton(onClick = onAbrirMenu) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "Más opciones",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        DropdownMenu(
            expanded = state.menuAbierto,
            onDismissRequest = onCerrarMenu
        ) {
            DropdownMenuItem(
                text = { Text(text = "Cambiar configuración") },
                leadingIcon = { Icon(imageVector = Icons.Filled.Settings, contentDescription = null) },
                onClick = onCambiarConfiguracion
            )
            DropdownMenuItem(
                text = { Text(text = "Cerrar sesión") },
                leadingIcon = { Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null) },
                onClick = onSolicitarCierre
            )
        }
    }
    if (state.confirmandoCierre) {
        DialogoCerrarSesion(
            cerrandoSesion = state.cerrandoSesion,
            onCancelar = onCancelarCierre,
            onConfirmar = onConfirmarCierre
        )
    }
}

@Composable
private fun DialogoCerrarSesion(
    cerrandoSesion: Boolean,
    onCancelar: () -> Unit,
    onConfirmar: () -> Unit
) {
    AlertDialog(
        // Mientras se cierra la sesión, tocar fuera del diálogo no lo cierra.
        onDismissRequest = { if (!cerrandoSesion) onCancelar() },
        title = { Text(text = "¿Cerrar sesión?") },
        text = {
            Text(
                text = "Tendrás que volver a iniciar sesión y configurar el dispositivo. " +
                        "Los datos de la clínica se conservan."
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirmar, enabled = !cerrandoSesion) {
                Text(text = if (cerrandoSesion) "Cerrando sesión…" else "Cerrar sesión")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar, enabled = !cerrandoSesion) {
                Text(text = "Cancelar")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun MenuSesionAbiertoPreview() {
    CliniMedikTheme {
        MenuSesion(
            state = MenuSesionUiState(menuAbierto = true),
            onAbrirMenu = {},
            onCerrarMenu = {},
            onCambiarConfiguracion = {},
            onSolicitarCierre = {},
            onCancelarCierre = {},
            onConfirmarCierre = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MenuSesionConfirmandoPreview() {
    CliniMedikTheme {
        MenuSesion(
            state = MenuSesionUiState(confirmandoCierre = true),
            onAbrirMenu = {},
            onCerrarMenu = {},
            onCambiarConfiguracion = {},
            onSolicitarCierre = {},
            onCancelarCierre = {},
            onConfirmarCierre = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MenuSesionCerrandoPreview() {
    CliniMedikTheme {
        MenuSesion(
            state = MenuSesionUiState(confirmandoCierre = true, cerrandoSesion = true),
            onAbrirMenu = {},
            onCerrarMenu = {},
            onCambiarConfiguracion = {},
            onSolicitarCierre = {},
            onCancelarCierre = {},
            onConfirmarCierre = {}
        )
    }
}