package plat.clinimedik.app.ui.screens.recepcion.detalle

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plat.clinimedik.app.data.model.EstadoVisita
import plat.clinimedik.app.data.model.TipoDocumento
import plat.clinimedik.app.ui.components.AvatarIniciales
import plat.clinimedik.app.ui.components.BarraSuperior
import plat.clinimedik.app.ui.components.EstadoChip
import plat.clinimedik.app.ui.components.MarcarUrgenteSheet
import plat.clinimedik.app.ui.components.etiqueta
import plat.clinimedik.app.ui.theme.CliniMedikTheme

@Composable
fun DetalleScreen(
    state: DetalleUiState,
    onIntent: (DetalleIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = "Expediente",
                onRegresar = { onIntent(DetalleIntent.Regresar) }
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
            EncabezadoPaciente(state = state)
            if (state.tieneAccionDeEstado) {
                AccionesDeEstado(state = state, onIntent = onIntent)
            }
            SeccionRegistro(state = state, onIntent = onIntent)
            SeccionDocumentos(documentos = state.documentos, onIntent = onIntent)
            SeccionVisitasAnteriores(visitas = state.visitasAnteriores)
        }

        when (val panel = state.panel) {
            is PanelDetalle.Urgencia -> MarcarUrgenteSheet(
                nombrePaciente = state.nombre,
                motivo = panel.motivo,
                onMotivoChange = { onIntent(DetalleIntent.CambiarMotivoUrgencia(it)) },
                onConfirmar = { onIntent(DetalleIntent.ConfirmarUrgencia) },
                onDescartar = { onIntent(DetalleIntent.CerrarPanel) },
                mensajeError = panel.mensajeError
            )
            is PanelDetalle.Receta -> RecetaSheet(
                nombrePaciente = state.nombre,
                formulario = panel.formulario,
                onIntent = onIntent
            )
            is PanelDetalle.Referencia -> ReferenciaSheet(
                nombrePaciente = state.nombre,
                formulario = panel.formulario,
                onIntent = onIntent
            )
            is PanelDetalle.Cobro -> CobroSheet(
                nombrePaciente = state.nombre,
                formulario = panel.formulario,
                onIntent = onIntent
            )
            null -> Unit
        }
    }
}

@Composable
private fun EncabezadoPaciente(state: DetalleUiState) {
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
                AvatarIniciales(
                    nombre = state.nombre,
                    destacado = state.esUrgenteEnEspera,
                    tamano = 64.dp
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = state.nombre,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = state.datosContacto,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                EstadoChip(estado = state.estado, esUrgente = state.esUrgente)
                Text(
                    text = state.motivoConsulta,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (state.esUrgenteEnEspera && state.motivoPrioridad != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Prioridad: ${state.motivoPrioridad}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            DatoClinico(etiqueta = "Alergias", valor = state.alergias)
            DatoClinico(etiqueta = "Enfermedades previas", valor = state.enfermedadesPrevias)
        }
    }
}

@Composable
private fun DatoClinico(
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
private fun AccionesDeEstado(
    state: DetalleUiState,
    onIntent: (DetalleIntent) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        when (state.estado) {
            EstadoVisita.ESPERANDO -> Button(
                onClick = { onIntent(DetalleIntent.PasarAConsulta) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Pasar a consulta")
            }
            EstadoVisita.EN_CONSULTA -> Button(
                onClick = { onIntent(DetalleIntent.FinalizarConsulta) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Finalizar consulta")
            }
            else -> Unit
        }
        if (state.puedeMarcarseUrgente) {
            OutlinedButton(
                onClick = { onIntent(DetalleIntent.SolicitarUrgencia) },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Marcar urgente")
            }
        }
    }
}

@Composable
private fun SeccionRegistro(
    state: DetalleUiState,
    onIntent: (DetalleIntent) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TituloSeccion(texto = "Registro de la visita")
        FilaAccion(
            icono = Icons.Filled.Create,
            texto = "Registrar receta",
            habilitada = state.puedeRegistrarConsulta,
            onClick = { onIntent(DetalleIntent.AbrirReceta) }
        )
        FilaAccion(
            icono = Icons.AutoMirrored.Filled.Send,
            texto = "Registrar referencia",
            habilitada = state.puedeRegistrarConsulta,
            onClick = { onIntent(DetalleIntent.AbrirReferencia) }
        )
        FilaAccion(
            icono = Icons.Filled.ShoppingCart,
            texto = "Registrar cobro",
            habilitada = state.puedeRegistrarConsulta,
            onClick = { onIntent(DetalleIntent.AbrirCobro) }
        )
        FilaAccion(
            icono = Icons.Filled.Add,
            texto = "Adjuntar documento",
            habilitada = true,
            onClick = { onIntent(DetalleIntent.AdjuntarDocumento) }
        )
        if (!state.puedeRegistrarConsulta) {
            Text(
                text = "La receta, la referencia y el cobro se habilitan cuando el paciente pasa a consulta.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FilaAccion(
    icono: ImageVector,
    texto: String,
    habilitada: Boolean,
    onClick: () -> Unit
) {
    val colorContenido = if (habilitada) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    }
    val colorIcono = if (habilitada) MaterialTheme.colorScheme.primary else colorContenido

    Surface(
        onClick = onClick,
        enabled = habilitada,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = colorIcono
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = texto,
                style = MaterialTheme.typography.bodyLarge,
                color = colorContenido,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = colorContenido
            )
        }
    }
}

@Composable
private fun SeccionDocumentos(
    documentos: List<DocumentoDetalle>,
    onIntent: (DetalleIntent) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TituloSeccion(texto = "Documentos adjuntos")
        if (documentos.isEmpty()) {
            Text(
                text = "Sin documentos adjuntos",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                documentos.forEach { documento ->
                    TarjetaDocumento(
                        documento = documento,
                        onClick = { onIntent(DetalleIntent.AbrirDocumento(documento.id)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaDocumento(
    documento: DocumentoDetalle,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.width(140.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = iconoDocumento(documento.tipo),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = documento.tipo.etiqueta(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = documento.fecha,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun iconoDocumento(tipo: TipoDocumento): ImageVector = when (tipo) {
    TipoDocumento.CARNET_SEGURO -> Icons.Filled.AccountBox
    TipoDocumento.RECETA_ANTERIOR -> Icons.Filled.Create
    TipoDocumento.ORDEN_REFERENCIA -> Icons.AutoMirrored.Filled.Send
    TipoDocumento.OTRO -> Icons.Filled.Info
}

@Composable
private fun SeccionVisitasAnteriores(visitas: List<VisitaAnterior>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TituloSeccion(texto = "Visitas anteriores")
        if (visitas.isEmpty()) {
            Text(
                text = "Es la primera visita registrada del paciente",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            visitas.forEach { visita ->
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(16.dp)
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
                        Text(
                            text = visita.diagnostico,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
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
private fun DetalleEsperandoPreview() {
    CliniMedikTheme {
        DetalleScreen(
            state = detalleUiStateDePrueba(visitaId = "vis-102"),
            onIntent = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun DetalleUrgenteConDocumentosPreview() {
    CliniMedikTheme {
        DetalleScreen(
            state = detalleUiStateDePrueba(visitaId = "vis-101"),
            onIntent = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun DetalleEnConsultaPreview() {
    CliniMedikTheme {
        DetalleScreen(
            state = detalleUiStateDePrueba(visitaId = "vis-104"),
            onIntent = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun DetalleRecetaAbiertaPreview() {
    CliniMedikTheme {
        DetalleScreen(
            state = detalleUiStateDePrueba(visitaId = "vis-104", panel = PanelDetalle.Receta()),
            onIntent = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun DetalleReferenciaAbiertaPreview() {
    CliniMedikTheme {
        DetalleScreen(
            state = detalleUiStateDePrueba(visitaId = "vis-104", panel = PanelDetalle.Referencia()),
            onIntent = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun DetalleCobroAbiertoPreview() {
    CliniMedikTheme {
        DetalleScreen(
            state = detalleUiStateDePrueba(visitaId = "vis-104", panel = PanelDetalle.Cobro()),
            onIntent = {}
        )
    }
}