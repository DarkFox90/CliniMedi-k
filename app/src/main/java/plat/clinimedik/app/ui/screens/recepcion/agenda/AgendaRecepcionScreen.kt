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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
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
import plat.clinimedik.app.ui.components.ContenedorRecepcion
import plat.clinimedik.app.ui.components.EncabezadoPantalla
import plat.clinimedik.app.ui.components.EstadoError
import plat.clinimedik.app.ui.components.PestanaRecepcion
import plat.clinimedik.app.ui.components.SelectorDia
import plat.clinimedik.app.ui.theme.CliniMedikTheme

@Composable
fun AgendaRecepcionScreen(
    state: AgendaRecepcionUiState,
    onEvent: (AgendaRecepcionEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onEvent(AgendaRecepcionEvent.NuevaCita) },
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
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)
            )
            SelectorDia(
                fecha = state.fecha,
                textoFecha = state.textoFecha,
                esHoy = state.esHoy,
                onDiaAnterior = { onEvent(AgendaRecepcionEvent.DiaAnterior) },
                onDiaSiguiente = { onEvent(AgendaRecepcionEvent.DiaSiguiente) },
                onElegirDia = { onEvent(AgendaRecepcionEvent.ElegirDia(it)) },
                onVolverAHoy = { onEvent(AgendaRecepcionEvent.VolverAHoy) },
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
            )
            when {
                state.cargando -> Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator()
                }
                state.mensajeError != null -> EstadoError(
                    mensaje = state.mensajeError,
                    onReintentar = { onEvent(AgendaRecepcionEvent.Reintentar) },
                    modifier = Modifier.fillMaxSize()
                )
                state.elementos.isEmpty() -> Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                ) {
                    Text(
                        text = state.mensajeSinCitas,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
                else -> LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 96.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.elementos, key = { it.citaId }) { elemento ->
                        CitaItem(
                            hora = elemento.hora,
                            nombrePaciente = elemento.nombrePaciente,
                            motivo = elemento.motivo,
                            estadoConfirmacion = elemento.estadoConfirmacion,
                            recordatorioEnviado = elemento.recordatorioEnviado,
                            estado = elemento.estado,
                            onClick = { onEvent(AgendaRecepcionEvent.AbrirCita(elemento.citaId)) }
                        )
                    }
                }
            }
        }
    }

    val panel = state.panel
    val elementoDelPanel = panel?.let { abierto ->
        state.elementos.firstOrNull { it.citaId == abierto.citaId }
    }
    if (panel != null && elementoDelPanel != null) {
        when (panel) {
            is PanelAgenda.Acciones -> CitaAccionesSheet(
                elemento = elementoDelPanel,
                puedeRegistrarLlegada = state.esHoy,
                guardando = state.guardando,
                onEvent = onEvent
            )
            is PanelAgenda.Reprogramar -> ReprogramarSheet(
                elemento = elementoDelPanel,
                formulario = panel.formulario,
                guardando = state.guardando,
                onEvent = onEvent
            )
            is PanelAgenda.ConfirmarCancelacion -> CancelarCitaDialogo(
                elemento = elementoDelPanel,
                guardando = state.guardando,
                onEvent = onEvent
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AgendaConDatosPreview() {
    CliniMedikTheme {
        ContenedorRecepcion(seleccion = PestanaRecepcion.AGENDA) {
            AgendaRecepcionScreen(state = agendaUiStateDePrueba(), onEvent = {})
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AgendaOtroDiaPreview() {
    CliniMedikTheme {
        ContenedorRecepcion(seleccion = PestanaRecepcion.AGENDA) {
            AgendaRecepcionScreen(state = agendaUiStateDePrueba(diasDesdeHoy = 1), onEvent = {})
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AgendaVaciaPreview() {
    CliniMedikTheme {
        ContenedorRecepcion(seleccion = PestanaRecepcion.AGENDA) {
            AgendaRecepcionScreen(
                state = agendaUiStateDePrueba().copy(elementos = emptyList()),
                onEvent = {}
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AgendaErrorPreview() {
    CliniMedikTheme {
        ContenedorRecepcion(seleccion = PestanaRecepcion.AGENDA) {
            AgendaRecepcionScreen(
                state = agendaUiStateDePrueba().copy(
                    elementos = emptyList(),
                    mensajeError = "No se pudieron cargar las citas"
                ),
                onEvent = {}
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AgendaConPanelPreview() {
    CliniMedikTheme {
        ContenedorRecepcion(seleccion = PestanaRecepcion.AGENDA) {
            AgendaRecepcionScreen(
                state = agendaUiStateDePrueba().copy(panel = PanelAgenda.Acciones("cit-004")),
                onEvent = {}
            )
        }
    }
}
