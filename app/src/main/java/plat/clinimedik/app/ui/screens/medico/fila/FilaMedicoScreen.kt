package plat.clinimedik.app.ui.screens.medico.fila

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plat.clinimedik.app.ui.components.ContenedorMedico
import plat.clinimedik.app.ui.components.EncabezadoPantalla
import plat.clinimedik.app.ui.components.EtiquetaChip
import plat.clinimedik.app.ui.components.PacienteItem
import plat.clinimedik.app.ui.components.PestanaMedico
import plat.clinimedik.app.ui.theme.CliniMedikTheme

@Composable
fun FilaMedicoScreen(
    state: FilaMedicoUiState,
    onIntent: (FilaMedicoIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            EncabezadoPantalla(
                titulo = "Mis pacientes",
                subtitulo = state.subtitulo,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp)
            )
            AvisoSoloLectura(
                ultimaActualizacion = state.ultimaActualizacion,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            if (state.pacientes.isEmpty()) {
                FilaVacia(modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.pacientes, key = { it.visitaId }) { paciente ->
                        PacienteItem(
                            nombre = paciente.nombre,
                            detalle = paciente.detalle,
                            tiempo = paciente.tiempo,
                            estado = paciente.estado,
                            esUrgente = paciente.esUrgente,
                            onClick = { onIntent(FilaMedicoIntent.AbrirHistorial(paciente.pacienteId)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AvisoSoloLectura(
    ultimaActualizacion: String,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            EtiquetaChip(
                texto = "Solo lectura",
                fondo = MaterialTheme.colorScheme.surfaceVariant,
                contenido = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = ultimaActualizacion,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "Los cambios en la fila los realiza Recepción.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FilaVacia(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.padding(32.dp)
    ) {
        Text(
            text = "No hay pacientes en la fila",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun FilaMedicoConPacientesPreview() {
    CliniMedikTheme {
        ContenedorMedico(seleccion = PestanaMedico.FILA) {
            FilaMedicoScreen(
                state = filaMedicoUiStateDePrueba(),
                onIntent = {}
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun FilaMedicoSinPacientesPreview() {
    CliniMedikTheme {
        ContenedorMedico(seleccion = PestanaMedico.FILA) {
            FilaMedicoScreen(
                state = filaMedicoUiStateDePrueba().copy(pacientes = emptyList()),
                onIntent = {}
            )
        }
    }
}
