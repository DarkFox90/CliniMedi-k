package plat.clinimedik.app.ui.screens.recepcion.detalle

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plat.clinimedik.app.data.model.TipoPago
import plat.clinimedik.app.ui.components.CampoTexto
import plat.clinimedik.app.ui.components.PanelInferior
import plat.clinimedik.app.ui.components.etiqueta
import plat.clinimedik.app.ui.components.formatearMonto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CobroContenido(
    nombrePaciente: String,
    formulario: FormularioCobro,
    onEvent: (DetalleEvent) -> Unit,
    modifier: Modifier = Modifier,
    guardando: Boolean = false
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        EncabezadoPanel(
            titulo = "Registrar cobro",
            descripcion = "Cobro de la visita de $nombrePaciente."
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Tipo de pago",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                TipoPago.entries.forEach { tipo ->
                    FilterChip(
                        selected = formulario.tipoPago == tipo,
                        onClick = { onEvent(DetalleEvent.SeleccionarTipoPago(tipo)) },
                        label = { Text(tipo.etiqueta()) }
                    )
                }
            }
            if (formulario.errorTipoPago != null) {
                Text(
                    text = formulario.errorTipoPago,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        if (formulario.esSeguro) {
            CamposSeguro(formulario = formulario, onEvent = onEvent)
        } else {
            CampoTexto(
                valor = formulario.monto,
                onValorChange = { onEvent(DetalleEvent.CambiarMonto(it)) },
                etiqueta = "Monto (Q)",
                placeholder = "0.00",
                mensajeError = formulario.errorMonto,
                tipoTeclado = KeyboardType.Decimal,
                habilitado = formulario.tipoPago != TipoPago.CORTESIA
            )
        }
        BotonesPanel(
            textoConfirmar = "Guardar cobro",
            onConfirmar = { onEvent(DetalleEvent.GuardarCobro) },
            onCancelar = { onEvent(DetalleEvent.CerrarPanel) },
            habilitado = !guardando
        )
    }
}

@Composable
private fun CamposSeguro(
    formulario: FormularioCobro,
    onEvent: (DetalleEvent) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CampoTexto(
            valor = formulario.aseguradora,
            onValorChange = { onEvent(DetalleEvent.CambiarAseguradora(it)) },
            etiqueta = "Aseguradora",
            placeholder = "Ej. Seguros G&T",
            mensajeError = formulario.errorAseguradora
        )
        CampoTexto(
            valor = formulario.numeroAutorizacion,
            onValorChange = { onEvent(DetalleEvent.CambiarAutorizacion(it)) },
            etiqueta = "Número de autorización (opcional)"
        )
        CampoTexto(
            valor = formulario.copago,
            onValorChange = { onEvent(DetalleEvent.CambiarCopago(it)) },
            etiqueta = "Lo que paga el paciente (Q)",
            placeholder = "0.00",
            mensajeError = formulario.errorCopago,
            tipoTeclado = KeyboardType.Decimal
        )
        CampoTexto(
            valor = formulario.montoAseguradora,
            onValorChange = { onEvent(DetalleEvent.CambiarMontoAseguradora(it)) },
            etiqueta = "Lo que paga el seguro (Q)",
            placeholder = "0.00",
            mensajeError = formulario.errorMontoAseguradora,
            tipoTeclado = KeyboardType.Decimal
        )
        Text(
            text = formulario.totalSeguro?.let { "Total del cobro: ${formatearMonto(it)}" }
                ?: "El total es lo que paga el paciente más lo que paga el seguro",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun CobroSheet(
    nombrePaciente: String,
    formulario: FormularioCobro,
    onEvent: (DetalleEvent) -> Unit,
    guardando: Boolean = false
) {
    PanelInferior(onDescartar = { onEvent(DetalleEvent.CerrarPanel) }) {
        CobroContenido(
            nombrePaciente = nombrePaciente,
            formulario = formulario,
            onEvent = onEvent,
            guardando = guardando
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CobroVacioPreview() {
    SuperficiePanelPreview {
        CobroContenido(
            nombrePaciente = "Julio Estrada",
            formulario = FormularioCobro(),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CobroParticularErrorPreview() {
    SuperficiePanelPreview {
        CobroContenido(
            nombrePaciente = "Julio Estrada",
            formulario = FormularioCobro(
                tipoPago = TipoPago.PARTICULAR,
                monto = "15.505",
                errorMonto = "Usa solo números, con hasta dos decimales"
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CobroConTipoPreview() {
    SuperficiePanelPreview {
        CobroContenido(
            nombrePaciente = "Julio Estrada",
            formulario = FormularioCobro(
                tipoPago = TipoPago.SEGURO,
                aseguradora = "Seguros G&T",
                copago = "50",
                montoAseguradora = "250.00"
            ),
            onEvent = {}
        )
    }
}