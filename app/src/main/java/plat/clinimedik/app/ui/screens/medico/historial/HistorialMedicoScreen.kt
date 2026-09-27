package plat.clinimedik.app.ui.screens.medico.historial

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plat.clinimedik.app.ui.components.AvatarIniciales
import plat.clinimedik.app.ui.components.BarraSuperior
import plat.clinimedik.app.ui.components.EstadoChip
import plat.clinimedik.app.ui.components.EtiquetaChip
import plat.clinimedik.app.ui.theme.CliniMedikTheme

@Composable
fun HistorialMedicoScreen(
    state: HistorialMedicoUiState,
    onIntent: (HistorialMedicoIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = "Historial",
                onRegresar = { onIntent(HistorialMedicoIntent.Regresar) }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                EncabezadoHistorial(state = state)
            }
            item {
                AvisoSoloLectura(ultimaActualizacion = state.ultimaActualizacion)
            }
            item {
                Text(
                    text = "Visitas",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            if (state.visitas.isEmpty()) {
                item {
                    Text(
                        text = "El paciente no tiene visitas terminadas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(state.visitas, key = { it.id }) { visita ->
                    TarjetaVisita(
                        visita = visita,
                        expandida = visita.id in state.visitasExpandidas,
                        onAlternar = { onIntent(HistorialMedicoIntent.AlternarVisita(visita.id)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EncabezadoHistorial(state: HistorialMedicoUiState) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarIniciales(nombre = state.nombre, tamano = 56.dp)
                Spacer(modifier = Modifier.width(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = state.nombre,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = state.datosPaciente,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            DatoPaciente(etiqueta = "Alergias", valor = state.alergias)
            DatoPaciente(etiqueta = "Enfermedades previas", valor = state.enfermedadesPrevias)
        }
    }
}

@Composable
private fun DatoPaciente(
    etiqueta: String,
    valor: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun AvisoSoloLectura(ultimaActualizacion: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
            text = "Los cambios en el expediente los realiza Recepción.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TarjetaVisita(
    visita: VisitaHistorial,
    expandida: Boolean,
    onAlternar: () -> Unit
) {
    Surface(
        onClick = onAlternar,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = visita.fecha,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = visita.motivo,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                EstadoChip(estado = visita.estado, esUrgente = false)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = if (expandida) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (expandida) "Ocultar detalle" else "Mostrar detalle",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (expandida) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                SeccionVisita(titulo = "Diagnóstico") {
                    Text(
                        text = visita.diagnostico,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                SeccionVisita(titulo = "Receta") {
                    if (visita.recetas.isEmpty()) {
                        Text(
                            text = "Sin receta registrada",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        visita.recetas.forEach { receta ->
                            Column {
                                Text(
                                    text = receta.medicamento,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = receta.indicaciones,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                if (visita.referencia != null) {
                    SeccionVisita(titulo = "Referencia") {
                        Text(
                            text = visita.referencia,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                if (visita.notas != null) {
                    SeccionVisita(titulo = "Notas") {
                        Text(
                            text = visita.notas,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SeccionVisita(
    titulo: String,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        contenido()
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HistorialCerradoPreview() {
    CliniMedikTheme {
        HistorialMedicoScreen(
            state = historialMedicoUiStateDePrueba(),
            onIntent = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HistorialAbiertoPreview() {
    CliniMedikTheme {
        HistorialMedicoScreen(
            state = historialMedicoUiStateDePrueba(visitasExpandidas = setOf("vis-201")),
            onIntent = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HistorialConReferenciaPreview() {
    CliniMedikTheme {
        HistorialMedicoScreen(
            state = historialMedicoUiStateDePrueba(
                pacienteId = "pac-004",
                visitasExpandidas = setOf("vis-105")
            ),
            onIntent = {}
        )
    }
}