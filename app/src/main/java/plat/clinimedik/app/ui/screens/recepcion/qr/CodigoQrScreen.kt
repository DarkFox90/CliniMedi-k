package plat.clinimedik.app.ui.screens.recepcion.qr

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plat.clinimedik.app.R
import plat.clinimedik.app.ui.components.BarraSuperior
import plat.clinimedik.app.ui.theme.CliniMedikTheme

@Composable
fun CodigoQrScreen(
    state: CodigoQrUiState,
    onIntent: (CodigoQrIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = "Código generado",
                onRegresar = { onIntent(CodigoQrIntent.Regresar) }
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
            Text(
                text = state.nombrePaciente,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.size(32.dp))


            Box(
                modifier = Modifier
                    .size(240.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest, MaterialTheme.shapes.medium)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {

                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "Código QR de muestra",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.size(24.dp))

            Text(
                text = "Muestra este código al paciente para que lo guarde. Servirá para agilizar su ingreso en futuras visitas.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { onIntent(CodigoQrIntent.EnviarPorWhatsApp) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Enviar por WhatsApp")
                }

                OutlinedButton(
                    onClick = { onIntent(CodigoQrIntent.Imprimir) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Imprimir código")
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CodigoQrPreview() {
    CliniMedikTheme {
        CodigoQrScreen(
            state = CodigoQrUiState(nombrePaciente = "Lucía Ajú"),
            onIntent = {}
        )
    }
}