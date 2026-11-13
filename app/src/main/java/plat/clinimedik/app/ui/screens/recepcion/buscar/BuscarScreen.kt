package plat.clinimedik.app.ui.screens.recepcion.buscar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
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
import plat.clinimedik.app.ui.components.BotonNuevoPaciente
import plat.clinimedik.app.ui.components.CampoTexto
import plat.clinimedik.app.ui.components.ContenedorRecepcion
import plat.clinimedik.app.ui.components.EncabezadoPantalla
import plat.clinimedik.app.ui.components.EstadoError
import plat.clinimedik.app.ui.components.PacienteItem
import plat.clinimedik.app.ui.components.PestanaRecepcion
import plat.clinimedik.app.ui.theme.CliniMedikTheme

@Composable
fun BuscarScreen(
    state: BuscarUiState,
    onEvent: (BuscarEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        floatingActionButton = {
            BotonNuevoPaciente(onClick = { onEvent(BuscarEvent.NuevoPaciente) })
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            EncabezadoPantalla(
                titulo = "Buscar paciente",
                subtitulo = "Pacientes de la clínica",
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp)
            )
            CampoTexto(
                valor = state.texto,
                onValorChange = { onEvent(BuscarEvent.CambiarTexto(it)) },
                etiqueta = "Buscar",
                placeholder = "Nombre o teléfono",
                modifier = Modifier.padding(horizontal = 16.dp)
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
                    onReintentar = null,
                    modifier = Modifier.fillMaxSize()
                )
                state.sinPacientes -> ListaVacia(
                    mensaje = state.mensajeVacio,
                    modifier = Modifier.fillMaxSize()
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (state.buscando) {
                        seccionPacientes(
                            titulo = state.tituloResultados,
                            pacientes = state.resultados,
                            prefijoClave = "resultado",
                            onEvent = onEvent
                        )
                    } else {
                        if (state.recientes.isNotEmpty()) {
                            seccionPacientes(
                                titulo = "Recientes",
                                pacientes = state.recientes,
                                prefijoClave = "reciente",
                                onEvent = onEvent
                            )
                        }
                        seccionPacientes(
                            titulo = state.tituloTodos,
                            pacientes = state.todos,
                            prefijoClave = "todos",
                            onEvent = onEvent
                        )
                    }
                }
            }
        }
    }
}

/**
 * Título y lista de una sección. [prefijoClave] evita claves repetidas en la lista,
 * porque un mismo paciente puede estar en "Recientes" y en "Todos los pacientes".
 */
private fun LazyListScope.seccionPacientes(
    titulo: String,
    pacientes: List<PacienteEncontrado>,
    prefijoClave: String,
    onEvent: (BuscarEvent) -> Unit
) {
    item(key = "titulo-$prefijoClave") {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
    items(pacientes, key = { "$prefijoClave-${it.pacienteId}" }) { paciente ->
        PacienteItem(
            nombre = paciente.nombre,
            detalle = paciente.telefono,
            onClick = { onEvent(BuscarEvent.AbrirPaciente(paciente.pacienteId)) }
        )
    }
}

@Composable
private fun ListaVacia(
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun BuscarCargandoPreview() {
    CliniMedikTheme {
        ContenedorRecepcion(seleccion = PestanaRecepcion.BUSCAR) {
            BuscarScreen(
                state = BuscarUiState(),
                onEvent = {}
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun BuscarRecientesYTodosPreview() {
    CliniMedikTheme {
        ContenedorRecepcion(seleccion = PestanaRecepcion.BUSCAR) {
            BuscarScreen(
                state = buscarUiStateDePrueba(),
                onEvent = {}
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun BuscarConResultadosPreview() {
    CliniMedikTheme {
        ContenedorRecepcion(seleccion = PestanaRecepcion.BUSCAR) {
            BuscarScreen(
                state = buscarUiStateDePrueba(texto = "mar"),
                onEvent = {}
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun BuscarSinResultadosPreview() {
    CliniMedikTheme {
        ContenedorRecepcion(seleccion = PestanaRecepcion.BUSCAR) {
            BuscarScreen(
                state = buscarUiStateDePrueba(texto = "zzz"),
                onEvent = {}
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun BuscarErrorPreview() {
    CliniMedikTheme {
        ContenedorRecepcion(seleccion = PestanaRecepcion.BUSCAR) {
            BuscarScreen(
                state = BuscarUiState(cargando = false, mensajeError = "No se pudieron cargar los pacientes"),
                onEvent = {}
            )
        }
    }
}
