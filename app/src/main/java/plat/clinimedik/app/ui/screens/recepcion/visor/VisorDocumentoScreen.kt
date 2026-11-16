package plat.clinimedik.app.ui.screens.recepcion.visor

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plat.clinimedik.app.ui.components.BarraSuperior
import plat.clinimedik.app.ui.components.EstadoError
import plat.clinimedik.app.ui.theme.CliniMedikTheme

@Composable
fun VisorDocumentoScreen(
    state: VisorDocumentoUiState,
    onEvent: (VisorDocumentoEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = state.tipo.ifBlank { "Documento" },
                onRegresar = { onEvent(VisorDocumentoEvent.Regresar) }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val imagen = state.imagen
            val mensajeError = state.mensajeError

            when {
                state.cargando -> CircularProgressIndicator()

                mensajeError != null -> {
                    val reintentar: (() -> Unit)? = if (state.puedeReintentar) {
                        { onEvent(VisorDocumentoEvent.Reintentar) }
                    } else null

                    EstadoError(
                        mensaje = mensajeError,
                        onReintentar = reintentar
                    )
                }

                imagen != null -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Image(
                            bitmap = imagen,
                            contentDescription = "Foto de ${state.tipo}",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        )
                        Text(
                            text = state.fecha,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun VisorConImagenPreview() {
    CliniMedikTheme {
        val fondo = MaterialTheme.colorScheme.primaryContainer.toArgb()
        val imagen = remember {
            Bitmap.createBitmap(400, 300, Bitmap.Config.ARGB_8888)
                .apply { eraseColor(fondo) }
                .asImageBitmap()
        }
        VisorDocumentoScreen(
            state = VisorDocumentoUiState(
                cargando = false,
                tipo = "Carnet de seguro",
                fecha = "14 de noviembre de 2026 · 9:30 a. m.",
                imagen = imagen
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun VisorCargandoPreview() {
    CliniMedikTheme {
        VisorDocumentoScreen(state = VisorDocumentoUiState(), onEvent = {})
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun VisorSinImagenPreview() {
    CliniMedikTheme {
        VisorDocumentoScreen(
            state = VisorDocumentoUiState(
                cargando = false,
                tipo = "Carnet de seguro",
                mensajeError = "La imagen de este documento no está disponible"
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun VisorNoEncontradoPreview() {
    CliniMedikTheme {
        VisorDocumentoScreen(
            state = VisorDocumentoUiState(
                cargando = false,
                mensajeError = "No se encontró el documento"
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun VisorErrorPreview() {
    CliniMedikTheme {
        VisorDocumentoScreen(
            state = VisorDocumentoUiState(
                cargando = false,
                mensajeError = "No se pudo cargar el documento",
                puedeReintentar = true
            ),
            onEvent = {}
        )
    }
}