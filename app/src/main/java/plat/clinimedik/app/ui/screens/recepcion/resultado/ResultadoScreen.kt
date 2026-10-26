package plat.clinimedik.app.ui.screens.recepcion.resultado

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plat.clinimedik.app.ui.components.AvatarIniciales
import plat.clinimedik.app.ui.components.BarraSuperior
import plat.clinimedik.app.ui.theme.CliniMedikTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton

@Composable
fun ResultadoScreen(
    state: ResultadoUiState,
    onEvent: (ResultadoEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = "Paciente encontrado",
                onRegresar = { onEvent(ResultadoEvent.Regresar) }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            if (state.cargando) {
                CircularProgressIndicator()
                return@Column
            }

            if (state.mensajeError != null && state.nombre.isBlank()) {
                Text(
                    text = state.mensajeError,
                    color = MaterialTheme.colorScheme.error
                )
                return@Column
            }

            Spacer(modifier = Modifier.height(32.dp))

            AvatarIniciales(
                nombre = state.nombre,
                tamano = 72.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = state.nombre,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${state.edad} años · ${state.telefono}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = state.resumenVisitas,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { onEvent(ResultadoEvent.AgregarAFila) },
                    enabled = !state.guardando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Agregar a la fila")
                }

                OutlinedButton(
                    onClick = { onEvent(ResultadoEvent.VerHistorial) },
                    enabled = state.tieneHistorial,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ver historial completo")
                }

                OutlinedButton(
                    onClick = { onEvent(ResultadoEvent.EscanearOtro) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Escanear otro")
                }
            }
        }
    }

    if (state.mostrarFormulario) {
        AlertDialog(
            onDismissRequest = {
                if (!state.guardando) {
                    onEvent(ResultadoEvent.CancelarAgregarAFila)
                }
            },
            title = { Text("Agregar a la fila") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.motivo,
                        onValueChange = {
                            onEvent(ResultadoEvent.CambiarMotivo(it))
                        },
                        label = { Text("Motivo de consulta") },
                        enabled = !state.guardando,
                        isError = state.mensajeError != null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (state.mensajeError != null) {
                        Text(
                            text = state.mensajeError,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEvent(ResultadoEvent.ConfirmarAgregarAFila)
                    },
                    enabled = !state.guardando
                ) {
                    Text(if (state.guardando) "Guardando..." else "Confirmar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onEvent(ResultadoEvent.CancelarAgregarAFila)
                    },
                    enabled = !state.guardando
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ResultadoPreview() {
    CliniMedikTheme {
        ResultadoScreen(
            state = ResultadoUiState(
                cargando = false,
                nombre = "Álvaro Chacón",
                edad = "42",
                telefono = "5512-7788",
                resumenVisitas = "3 visitas anteriores · última: 14 de marzo de 2026",
                tieneHistorial = true
            ),
            onEvent = {}
        )
    }
}