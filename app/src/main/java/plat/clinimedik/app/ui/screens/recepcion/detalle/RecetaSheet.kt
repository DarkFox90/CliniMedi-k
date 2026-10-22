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
fun RecetaContenido(
    nombrePaciente: String,
    formulario: FormularioReceta,
    onEvent: (DetalleEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        EncabezadoPanel(
            titulo = "Registrar receta",
            descripcion = "Receta indicada por el médico para $nombrePaciente."
        )
        CampoTexto(
            valor = formulario.medicamento,
            onValorChange = { onEvent(DetalleEvent.CambiarMedicamento(it)) },
            etiqueta = "Medicamento",
            placeholder = "Ej. Amoxicilina 500 mg",
            mensajeError = formulario.errorMedicamento
        )
        CampoTexto(
            valor = formulario.dosis,
            onValorChange = { onEvent(DetalleEvent.CambiarDosis(it)) },
            etiqueta = "Dosis",
            placeholder = "Ej. 1 cápsula",
            mensajeError = formulario.errorDosis
        )
        CampoTexto(
            valor = formulario.frecuencia,
            onValorChange = { onEvent(DetalleEvent.CambiarFrecuencia(it)) },
            etiqueta = "Frecuencia",
            placeholder = "Ej. Cada 8 horas",
            mensajeError = formulario.errorFrecuencia
        )
        CampoTexto(
            valor = formulario.duracion,
            onValorChange = { onEvent(DetalleEvent.CambiarDuracion(it)) },
            etiqueta = "Duración",
            placeholder = "Ej. 7 días",
            mensajeError = formulario.errorDuracion
        )
        BotonesPanel(
            textoConfirmar = "Guardar receta",
            onConfirmar = { onEvent(DetalleEvent.GuardarReceta) },
            onCancelar = { onEvent(DetalleEvent.CerrarPanel) }
        )
    }
}

@Composable
fun RecetaSheet(
    nombrePaciente: String,
    formulario: FormularioReceta,
    onEvent: (DetalleEvent) -> Unit
) {
    PanelInferior(onDescartar = { onEvent(DetalleEvent.CerrarPanel) }) {
        RecetaContenido(
            nombrePaciente = nombrePaciente,
            formulario = formulario,
            onEvent = onEvent
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RecetaVaciaPreview() {
    SuperficiePanelPreview {
        RecetaContenido(
            nombrePaciente = "Julio Estrada",
            formulario = FormularioReceta(),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RecetaErrorPreview() {
    SuperficiePanelPreview {
        RecetaContenido(
            nombrePaciente = "Julio Estrada",
            formulario = FormularioReceta(
                medicamento = "Metformina 850 mg",
                errorDosis = "Este campo es obligatorio",
                errorFrecuencia = "Este campo es obligatorio",
                errorDuracion = "Este campo es obligatorio"
            ),
            onEvent = {}
        )
    }
}