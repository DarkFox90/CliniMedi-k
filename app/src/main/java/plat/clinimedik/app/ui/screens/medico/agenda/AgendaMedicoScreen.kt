package plat.clinimedik.app.ui.screens.medico.agenda

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
import plat.clinimedik.app.ui.components.CitaItem
import plat.clinimedik.app.ui.components.ContenedorMedico
import plat.clinimedik.app.ui.components.EncabezadoPantalla
import plat.clinimedik.app.ui.components.EtiquetaChip
import plat.clinimedik.app.ui.components.PestanaMedico
import plat.clinimedik.app.ui.theme.CliniMedikTheme

@Composable
fun AgendaMedicoScreen(
    state: AgendaMedicoUiState,
    onEvent: (AgendaMedicoEvent) -> Unit,
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
                titulo = "Mi agenda",
                subtitulo = state.subtitulo,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp)
            )
            AvisoSoloLectura(
                ultimaActualizacion = state.ultimaActualizacion,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            if (state.cargando) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.citas.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                ) {
                    Text(
                        text = "No hay citas programadas para hoy",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.citas, key = { it.citaId }) { cita ->
                        CitaItem(
                            hora = cita.hora,
                            nombrePaciente = cita.nombrePaciente,
                            motivo = cita.motivo,
                            estadoConfirmacion = cita.estadoConfirmacion
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
            text = "Los cambios en la agenda los realiza Recepción.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AgendaMedicoCargandoPreview() {
    CliniMedikTheme {
        ContenedorMedico(seleccion = PestanaMedico.AGENDA) {
            AgendaMedicoScreen(
                state = AgendaMedicoUiState(),
                onEvent = {}
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AgendaMedicoConCitasPreview() {
    CliniMedikTheme {
        ContenedorMedico(seleccion = PestanaMedico.AGENDA) {
            AgendaMedicoScreen(
                state = agendaMedicoUiStateDePrueba(),
                onEvent = {}
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AgendaMedicoSinCitasPreview() {
    CliniMedikTheme {
        ContenedorMedico(seleccion = PestanaMedico.AGENDA) {
            AgendaMedicoScreen(
                state = agendaMedicoUiStateDePrueba().copy(citas = emptyList()),
                onEvent = {}
            )
        }
    }
}