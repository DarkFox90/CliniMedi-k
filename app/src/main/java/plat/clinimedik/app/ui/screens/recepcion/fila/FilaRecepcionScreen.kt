package plat.clinimedik.app.ui.screens.recepcion.fila

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plat.clinimedik.app.ui.components.BotonNuevoPaciente
import plat.clinimedik.app.ui.components.ContenedorRecepcion
import plat.clinimedik.app.ui.components.EncabezadoPantalla
import plat.clinimedik.app.ui.components.MarcarUrgenteSheet
import plat.clinimedik.app.ui.components.PacienteItem
import plat.clinimedik.app.ui.components.PestanaRecepcion
import plat.clinimedik.app.ui.theme.CliniMedikTheme

@Composable
fun FilaRecepcionScreen(
    state: FilaRecepcionUiState,
    onIntent: (FilaRecepcionIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        floatingActionButton = {
            BotonNuevoPaciente(onClick = { onIntent(FilaRecepcionIntent.NuevoPaciente) })
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            EncabezadoPantalla(
                titulo = "Fila de hoy",
                subtitulo = state.subtitulo,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp)
            )
            FiltrosFila(
                filtroActual = state.filtro,
                onSeleccionar = { onIntent(FilaRecepcionIntent.SeleccionarFiltro(it)) }
            )
            if (state.pacientes.isEmpty()) {
                FilaVacia(
                    mensaje = state.filtro.mensajeVacio,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
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
                            onClick = { onIntent(FilaRecepcionIntent.AbrirPaciente(paciente.visitaId)) },
                            accion = if (paciente.puedeMarcarseUrgente) {
                                {
                                    BotonMarcarUrgente(
                                        onClick = { onIntent(FilaRecepcionIntent.SolicitarUrgencia(paciente.visitaId)) }
                                    )
                                }
                            } else {
                                null
                            }
                        )
                    }
                }
            }
        }

        state.urgencia?.let { urgencia ->
            MarcarUrgenteSheet(
                nombrePaciente = urgencia.nombrePaciente,
                motivo = urgencia.motivo,
                onMotivoChange = { onIntent(FilaRecepcionIntent.CambiarMotivoUrgencia(it)) },
                onConfirmar = { onIntent(FilaRecepcionIntent.ConfirmarUrgencia) },
                onDescartar = { onIntent(FilaRecepcionIntent.CancelarUrgencia) },
                mensajeError = urgencia.mensajeError
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FiltrosFila(
    filtroActual: FiltroFila,
    onSeleccionar: (FiltroFila) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(FiltroFila.entries) { filtro ->
            FilterChip(
                selected = filtro == filtroActual,
                onClick = { onSeleccionar(filtro) },
                label = { Text(filtro.etiqueta) }
            )
        }
    }
}

@Composable
private fun BotonMarcarUrgente(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = "Marcar como urgente",
            tint = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun FilaVacia(
    mensaje: String,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.padding(32.dp)
    ) {
        Text(
            text = mensaje,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun FilaConPacientesPreview() {
    CliniMedikTheme {
        ContenedorRecepcion(seleccion = PestanaRecepcion.FILA) {
            FilaRecepcionScreen(
                state = filaRecepcionUiStateDePrueba(),
                onIntent = {}
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun FilaConFiltroPreview() {
    CliniMedikTheme {
        ContenedorRecepcion(seleccion = PestanaRecepcion.FILA) {
            FilaRecepcionScreen(
                state = filaRecepcionUiStateDePrueba(filtro = FiltroFila.ESPERANDO),
                onIntent = {}
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun FilaSinResultadosPreview() {
    CliniMedikTheme {
        ContenedorRecepcion(seleccion = PestanaRecepcion.FILA) {
            FilaRecepcionScreen(
                state = filaRecepcionUiStateDePrueba().copy(
                    filtro = FiltroFila.URGENTES,
                    pacientes = emptyList()
                ),
                onIntent = {}
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun FilaMarcarUrgenteVacioPreview() {
    CliniMedikTheme {
        ContenedorRecepcion(seleccion = PestanaRecepcion.FILA) {
            FilaRecepcionScreen(
                state = filaRecepcionUiStateDePrueba().copy(
                    urgencia = SolicitudUrgencia(
                        visitaId = "vis-102",
                        nombrePaciente = "Marta Ríos"
                    )
                ),
                onIntent = {}
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun FilaMarcarUrgenteConMotivoPreview() {
    CliniMedikTheme {
        ContenedorRecepcion(seleccion = PestanaRecepcion.FILA) {
            FilaRecepcionScreen(
                state = filaRecepcionUiStateDePrueba().copy(
                    urgencia = SolicitudUrgencia(
                        visitaId = "vis-102",
                        nombrePaciente = "Marta Ríos",
                        motivo = "Presión arterial de 180/110 con dolor de cabeza intenso"
                    )
                ),
                onIntent = {}
            )
        }
    }
}