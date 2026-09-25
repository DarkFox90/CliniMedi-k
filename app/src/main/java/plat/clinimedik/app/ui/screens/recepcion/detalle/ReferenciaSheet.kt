package plat.clinimedik.app.ui.screens.recepcion.detalle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plat.clinimedik.app.ui.components.CampoTexto
import plat.clinimedik.app.ui.components.PanelInferior

@Composable
fun ReferenciaContenido(
    nombrePaciente: String,
    formulario: FormularioReferencia,
    onIntent: (DetalleIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        EncabezadoPanel(
            titulo = "Registrar referencia",
            descripcion = "Al guardar, el estado de $nombrePaciente cambiará a Referido."
        )
        CampoTexto(
            valor = formulario.especialidad,
            onValorChange = { onIntent(DetalleIntent.CambiarEspecialidad(it)) },
            etiqueta = "Especialidad",
            placeholder = "Ej. Cardiología",
            mensajeError = formulario.errorEspecialidad
        )
        CampoTexto(
            valor = formulario.destino,
            onValorChange = { onIntent(DetalleIntent.CambiarDestino(it)) },
            etiqueta = "Médico o servicio de destino",
            placeholder = "Ej. Dra. Ana Lucía Paz",
            mensajeError = formulario.errorDestino
        )
        CampoTexto(
            valor = formulario.motivo,
            onValorChange = { onIntent(DetalleIntent.CambiarMotivoReferencia(it)) },
            etiqueta = "Motivo de la referencia",
            placeholder = "Ej. Soplo cardíaco, requiere ecocardiograma",
            mensajeError = formulario.errorMotivo,
            lineas = 3
        )
        BotonesPanel(
            textoConfirmar = "Guardar referencia",
            onConfirmar = { onIntent(DetalleIntent.GuardarReferencia) },
            onCancelar = { onIntent(DetalleIntent.CerrarPanel) }
        )
    }
}

@Composable
fun ReferenciaSheet(
    nombrePaciente: String,
    formulario: FormularioReferencia,
    onIntent: (DetalleIntent) -> Unit
) {
    PanelInferior(onDescartar = { onIntent(DetalleIntent.CerrarPanel) }) {
        ReferenciaContenido(
            nombrePaciente = nombrePaciente,
            formulario = formulario,
            onIntent = onIntent
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReferenciaVaciaPreview() {
    SuperficiePanelPreview {
        ReferenciaContenido(
            nombrePaciente = "Julio Estrada",
            formulario = FormularioReferencia(),
            onIntent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReferenciaErrorPreview() {
    SuperficiePanelPreview {
        ReferenciaContenido(
            nombrePaciente = "Julio Estrada",
            formulario = FormularioReferencia(
                especialidad = "Endocrinología",
                errorDestino = "Este campo es obligatorio",
                errorMotivo = "Este campo es obligatorio"
            ),
            onIntent = {}
        )
    }
}