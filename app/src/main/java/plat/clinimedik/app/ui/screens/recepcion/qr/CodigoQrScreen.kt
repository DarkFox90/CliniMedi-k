package plat.clinimedik.app.ui.screens.recepcion.qr

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plat.clinimedik.app.ui.components.BarraSuperior
import plat.clinimedik.app.ui.components.CodigoQrImagen
import plat.clinimedik.app.ui.components.ColumnaDesplazable
import plat.clinimedik.app.ui.components.EstadoError
import plat.clinimedik.app.ui.components.coloresQrDelTema
import plat.clinimedik.app.ui.theme.CliniMedikTheme

@Composable
fun CodigoQrScreen(
    state: CodigoQrUiState,
    onEvent: (CodigoQrEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = "Código generado",
                onRegresar = { onEvent(CodigoQrEvent.Regresar) }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        when {
            state.cargando -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    CircularProgressIndicator()
                }
            }

            state.mensajeError != null -> {
                val reintentar: (() -> Unit)? = if (state.puedeReintentar) {
                    { onEvent(CodigoQrEvent.Reintentar) }
                } else null

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    EstadoError(
                        mensaje = state.mensajeError,
                        onReintentar = reintentar
                    )
                }
            }

            else -> {
                ContenidoCodigo(
                    state = state,
                    onEvent = onEvent,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun ContenidoCodigo(
    state: CodigoQrUiState,
    onEvent: (CodigoQrEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val colores = coloresQrDelTema()

    ColumnaDesplazable(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(24.dp)
    ) {
        Text(
            text = state.nombrePaciente,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.size(32.dp))

        CodigoQrImagen(
            texto = state.contenidoQr,
            colores = colores,
            descripcion = "Código QR de ${state.nombrePaciente}",
            modifier = Modifier
                .size(240.dp)
                .clip(MaterialTheme.shapes.medium)
        )

        Spacer(modifier = Modifier.size(24.dp))

        Text(
            text = "Muestra este código al paciente para que lo guarde. Servirá para agilizar su ingreso en futuras visitas.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.size(24.dp))

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = { onEvent(CodigoQrEvent.EnviarPorWhatsApp(colores)) },
                enabled = !state.guardando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Enviar por WhatsApp")
            }

            OutlinedButton(
                onClick = { onEvent(CodigoQrEvent.Imprimir(colores)) },
                enabled = !state.guardando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Imprimir código")
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CodigoQrPreview() {
    CliniMedikTheme {
        CodigoQrScreen(
            state = CodigoQrUiState(
                cargando = false,
                nombrePaciente = "Lucía Ajú",
                pacienteId = "pac-006"
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CodigoQrGuardandoPreview() {
    CliniMedikTheme {
        CodigoQrScreen(
            state = CodigoQrUiState(
                cargando = false,
                nombrePaciente = "Lucía Ajú",
                pacienteId = "pac-006",
                guardando = true
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CodigoQrCargandoPreview() {
    CliniMedikTheme {
        CodigoQrScreen(
            state = CodigoQrUiState(cargando = true),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CodigoQrErrorPreview() {
    CliniMedikTheme {
        CodigoQrScreen(
            state = CodigoQrUiState(
                cargando = false,
                mensajeError = "No se pudo cargar el código del paciente",
                puedeReintentar = true
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CodigoQrNoEncontradoPreview() {
    CliniMedikTheme {
        CodigoQrScreen(
            state = CodigoQrUiState(
                cargando = false,
                mensajeError = "No se encontró el paciente"
            ),
            onEvent = {}
        )
    }
}