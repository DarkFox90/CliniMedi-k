package plat.clinimedik.app.ui.screens.recepcion.escaneo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import plat.clinimedik.app.ui.components.BarraSuperior
import plat.clinimedik.app.ui.components.ColumnaDesplazable
import plat.clinimedik.app.ui.theme.CliniMedikTheme

@Composable
fun EscaneoScreen(
    state: EscaneoUiState,
    onEvent: (EscaneoEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = state.titulo,
                onRegresar = { onEvent(EscaneoEvent.Regresar) }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.inverseSurface)
                .padding(innerPadding)
        ) {
            ColumnaDesplazable(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .border(
                            width = 4.dp,
                            color = MaterialTheme.colorScheme.inversePrimary,
                            shape = MaterialTheme.shapes.extraLarge
                        )
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Apunta la cámara al código del paciente",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { onEvent(EscaneoEvent.EscanearConCamara) },
                    enabled = !state.leyendo,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                ) {
                    Text("Escanear con la cámara")
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { onEvent(EscaneoEvent.SimularLectura) },
                    enabled = !state.leyendo,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.inverseOnSurface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.inverseOnSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                ) {
                    Text("Simular lectura")
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun EscaneoPreview() {
    CliniMedikTheme {
        EscaneoScreen(state = EscaneoUiState(), onEvent = {})
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun EscaneoLeyendoPreview() {
    CliniMedikTheme {
        EscaneoScreen(state = EscaneoUiState(leyendo = true), onEvent = {})
    }
}