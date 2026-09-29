package plat.clinimedik.app.ui.screens.recepcion.resumen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plat.clinimedik.app.ui.components.BarraSuperior
import plat.clinimedik.app.ui.components.TarjetaMetrica
import plat.clinimedik.app.ui.theme.CliniMedikTheme

@Composable
fun ResumenScreen(
    state: ResumenUiState,
    onIntent: (ResumenIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = "Resumen del día",
                onRegresar = { onIntent(ResumenIntent.Regresar) }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = state.subtitulo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            SeccionPacientes(state = state)
            SeccionCobros(state = state)
            SeccionAfluencia(state = state)
        }
    }
}

@Composable
private fun SeccionPacientes(state: ResumenUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TituloSeccion(texto = "Pacientes")
        FilaMetricas {
            TarjetaMetrica(
                titulo = "Atendidos",
                valor = state.atendidos.toString(),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            TarjetaMetrica(
                titulo = "Promedio por paciente",
                valor = state.tiempoPromedio,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
        FilaMetricas {
            TarjetaMetrica(
                titulo = "Nuevos",
                valor = state.nuevos.toString(),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            TarjetaMetrica(
                titulo = "Recurrentes",
                valor = state.recurrentes.toString(),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
private fun FilaMetricas(contenido: @Composable RowScope.() -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.height(IntrinsicSize.Min),
        content = contenido
    )
}

@Composable
private fun SeccionCobros(state: ResumenUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TituloSeccion(texto = "Cobros del día")
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                state.cobrosPorTipo.forEach { cobro ->
                    FilaValor(etiqueta = cobro.tipo, valor = cobro.monto)
                }
                HorizontalDivider()
                FilaValor(
                    etiqueta = "Total del día",
                    valor = state.totalDelDia,
                    destacado = true
                )
                HorizontalDivider()
                PendientesDeCobro(nombres = state.pendientesDeCobro)
            }
        }
    }
}

@Composable
private fun FilaValor(
    etiqueta: String,
    valor: String,
    destacado: Boolean = false
) {
    val estilo = if (destacado) {
        MaterialTheme.typography.titleMedium
    } else {
        MaterialTheme.typography.bodyLarge
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = etiqueta,
            style = estilo,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = valor,
            style = estilo,
            color = if (destacado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun PendientesDeCobro(nombres: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Pendientes de cobro",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = nombres.size.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = if (nombres.isEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
            )
        }
        if (nombres.isNotEmpty()) {
            Text(
                text = nombres.joinToString(", "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SeccionAfluencia(state: ResumenUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TituloSeccion(texto = "Horas de mayor afluencia")
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = state.textoAfluencia,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (state.afluencia.isNotEmpty()) {
                    GraficaAfluencia(barras = state.afluencia)
                }
            }
        }
    }
}

@Composable
private fun GraficaAfluencia(barras: List<BarraAfluencia>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            barras.forEach { barra ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Text(
                        text = barra.llegadas.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .fillMaxHeight(barra.proporcion)
                            .clip(MaterialTheme.shapes.extraSmall)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            barras.forEach { barra ->
                Text(
                    text = barra.hora,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ResumenNormalPreview() {
    CliniMedikTheme {
        ResumenScreen(
            state = resumenUiStateDePrueba(),
            onIntent = {}
        )
    }
}
