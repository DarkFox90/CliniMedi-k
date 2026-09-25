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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CobroContenido(
    nombrePaciente: String,
    formulario: FormularioCobro,
    onIntent: (DetalleIntent) -> Unit,
    modifier: Modifier = Modifier
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
                        onClick = { onIntent(DetalleIntent.SeleccionarTipoPago(tipo)) },
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
        CampoTexto(
            valor = formulario.monto,
            onValorChange = { onIntent(DetalleIntent.CambiarMonto(it)) },
            etiqueta = "Monto (Q)",
            placeholder = "0.00",
            mensajeError = formulario.errorMonto,
            tipoTeclado = KeyboardType.Decimal,
            habilitado = formulario.tipoPago != TipoPago.CORTESIA
        )
        BotonesPanel(
            textoConfirmar = "Guardar cobro",
            onConfirmar = { onIntent(DetalleIntent.GuardarCobro) },
            onCancelar = { onIntent(DetalleIntent.CerrarPanel) }
        )
    }
}

@Composable
fun CobroSheet(
    nombrePaciente: String,
    formulario: FormularioCobro,
    onIntent: (DetalleIntent) -> Unit
) {
    PanelInferior(onDescartar = { onIntent(DetalleIntent.CerrarPanel) }) {
        CobroContenido(
            nombrePaciente = nombrePaciente,
            formulario = formulario,
            onIntent = onIntent
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
            onIntent = {}
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
                monto = "300.00"
            ),
            onIntent = {}
        )
    }
}