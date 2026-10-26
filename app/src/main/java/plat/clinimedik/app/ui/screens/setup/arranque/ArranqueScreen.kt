package plat.clinimedik.app.ui.screens.setup.arranque

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plat.clinimedik.app.ui.theme.CliniMedikTheme

@Composable
fun ArranqueScreen(
    state: ArranqueUiState,
    onEvent: (ArranqueEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "CliniMedi-k",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary
            )
            if (state.cargando) {
                CircularProgressIndicator()
            } else {
                Text(
                    text = state.mensajeError.orEmpty(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Button(onClick = { onEvent(ArranqueEvent.Reintentar) }) {
                    Text(text = "Reintentar")
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ArranqueCargandoPreview() {
    CliniMedikTheme {
        ArranqueScreen(state = ArranqueUiState(), onEvent = {})
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ArranqueErrorPreview() {
    CliniMedikTheme {
        ArranqueScreen(
            state = ArranqueUiState(
                cargando = false,
                mensajeError = "No se pudo leer la configuración del dispositivo"
            ),
            onEvent = {}
        )
    }
}