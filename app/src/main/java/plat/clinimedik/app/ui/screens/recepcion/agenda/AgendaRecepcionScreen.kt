package plat.clinimedik.app.ui.screens.recepcion.agenda

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plat.clinimedik.app.ui.components.CitaItem
import plat.clinimedik.app.ui.components.ContenedorRecepcion
import plat.clinimedik.app.ui.components.EncabezadoPantalla
import plat.clinimedik.app.ui.components.PestanaRecepcion
import plat.clinimedik.app.ui.theme.CliniMedikTheme

@Composable
fun AgendaRecepcionScreen(
    state: AgendaRecepcionUiState,
    onIntent: (AgendaRecepcionIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onIntent(AgendaRecepcionIntent.NuevaCita) },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nueva cita") }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            EncabezadoPantalla(
                titulo = "Agenda",
                subtitulo = state.subtitulo,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            )
            if (state.elementos.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "No hay citas programadas para hoy",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.elementos, key = { it.citaId }) { elemento ->
                        CitaItem(
                            hora = elemento.hora,
                            nombrePaciente = elemento.nombrePaciente,
                            motivo = elemento.motivo,
                            estadoConfirmacion = elemento.estadoConfirmacion,
                            recordatorioEnviado = elemento.recordatorioEnviado,
                            onClick = { onIntent(AgendaRecepcionIntent.AbrirCita(elemento.citaId)) }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AgendaConDatosPreview() {
    CliniMedikTheme {
        ContenedorRecepcion(seleccion = PestanaRecepcion.AGENDA) {
            AgendaRecepcionScreen(state = agendaUiStateDePrueba(), onIntent = {})
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AgendaVaciaPreview() {
    CliniMedikTheme {
        ContenedorRecepcion(seleccion = PestanaRecepcion.AGENDA) {
            AgendaRecepcionScreen(
                state = AgendaRecepcionUiState(
                    subtitulo = "Jueves, 24 de septiembre"
                ),
                onIntent = {}
            )
        }
    }
}