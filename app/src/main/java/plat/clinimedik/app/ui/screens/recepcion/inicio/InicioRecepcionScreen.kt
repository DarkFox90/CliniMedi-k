package plat.clinimedik.app.ui.screens.recepcion.inicio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plat.clinimedik.app.ui.components.BotonNuevoPaciente
import plat.clinimedik.app.ui.components.CitaItem
import plat.clinimedik.app.ui.components.ContenedorRecepcion
import plat.clinimedik.app.ui.components.EncabezadoPantalla
import plat.clinimedik.app.ui.components.PacienteItem
import plat.clinimedik.app.ui.components.PestanaRecepcion
import plat.clinimedik.app.ui.components.TarjetaMetrica
import plat.clinimedik.app.ui.theme.CliniMedikTheme

@Composable
fun InicioRecepcionScreen(
    state: InicioRecepcionUiState,
    onIntent: (InicioRecepcionIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        floatingActionButton = {
            BotonNuevoPaciente(onClick = { onIntent(InicioRecepcionIntent.NuevoPaciente) })
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                EncabezadoPantalla(
                    titulo = state.saludo,
                    subtitulo = state.fecha
                )
                Text(
                    text = state.medicoYRol,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            MetricasFila(state = state)
            SeccionSiguientePaciente(state = state, onIntent = onIntent)
            SeccionProximaCita(proximaCita = state.proximaCita, onIntent = onIntent)
            SeccionAccesosRapidos(onIntent = onIntent)
        }
    }
}

@Composable
private fun MetricasFila(state: InicioRecepcionUiState) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TarjetaMetrica(
            titulo = "Esperando",
            valor = state.esperando.toString(),
            modifier = Modifier.weight(1f)
        )
        TarjetaMetrica(
            titulo = "Urgentes",
            valor = state.urgentes.toString(),
            fondo = MaterialTheme.colorScheme.errorContainer,
            contenido = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.weight(1f)
        )
        TarjetaMetrica(
            titulo = "Atendidos",
            valor = state.atendidos.toString(),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SeccionSiguientePaciente(
    state: InicioRecepcionUiState,
    onIntent: (InicioRecepcionIntent) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TituloSeccion(texto = "Siguiente en la fila")
        val paciente = state.siguientePaciente
        if (paciente == null) {
            TextoVacio(texto = state.mensajeSinSiguiente)
        } else {
            PacienteItem(
                nombre = paciente.nombre,
                detalle = paciente.detalle,
                tiempo = paciente.tiempo,
                estado = paciente.estado,
                esUrgente = paciente.esUrgente,
                onClick = { onIntent(InicioRecepcionIntent.AbrirPaciente(paciente.visitaId)) }
            )
        }
    }
}

@Composable
private fun SeccionProximaCita(
    proximaCita: ProximaCita?,
    onIntent: (InicioRecepcionIntent) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TituloSeccion(texto = "Próxima cita")
        if (proximaCita == null) {
            TextoVacio(texto = "No hay más citas hoy")
        } else {
            CitaItem(
                hora = proximaCita.hora,
                nombrePaciente = proximaCita.nombrePaciente,
                motivo = proximaCita.motivo,
                estadoConfirmacion = proximaCita.estadoConfirmacion,
                onClick = { onIntent(InicioRecepcionIntent.AbrirCita(proximaCita.citaId)) }
            )
        }
    }
}

@Composable
private fun SeccionAccesosRapidos(onIntent: (InicioRecepcionIntent) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TituloSeccion(texto = "Accesos rápidos")
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(IntrinsicSize.Min)
        ) {
            AccesoRapido(
                icono = Icons.Filled.Person,
                texto = "Nuevo paciente",
                onClick = { onIntent(InicioRecepcionIntent.NuevoPaciente) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            AccesoRapido(
                icono = Icons.Filled.Search,
                texto = "Escanear QR",
                onClick = { onIntent(InicioRecepcionIntent.EscanearQr) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            AccesoRapido(
                icono = Icons.Filled.Info,
                texto = "Ver resumen",
                onClick = { onIntent(InicioRecepcionIntent.VerResumen) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
private fun AccesoRapido(
    icono: ImageVector,
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp)
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = texto,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TituloSeccion(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun TextoVacio(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun InicioConPacientesPreview() {
    CliniMedikTheme {
        ContenedorRecepcion(seleccion = PestanaRecepcion.INICIO) {
            InicioRecepcionScreen(
                state = inicioRecepcionUiStateDePrueba(),
                onIntent = {}
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun InicioSinPacientesPreview() {
    CliniMedikTheme {
        ContenedorRecepcion(seleccion = PestanaRecepcion.INICIO) {
            InicioRecepcionScreen(
                state = inicioRecepcionUiStateDePrueba().copy(pacientes = emptyList()),
                onIntent = {}
            )
        }
    }
}
