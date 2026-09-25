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
    onIntent: (DetalleIntent) -> Unit,
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
            onValorChange = { onIntent(DetalleIntent.CambiarMedicamento(it)) },
            etiqueta = "Medicamento",
            placeholder = "Ej. Amoxicilina 500 mg",
            mensajeError = formulario.errorMedicamento
        )
        CampoTexto(
            valor = formulario.dosis,
            onValorChange = { onIntent(DetalleIntent.CambiarDosis(it)) },
            etiqueta = "Dosis",
            placeholder = "Ej. 1 cápsula",
            mensajeError = formulario.errorDosis
        )
        CampoTexto(
            valor = formulario.frecuencia,
            onValorChange = { onIntent(DetalleIntent.CambiarFrecuencia(it)) },
            etiqueta = "Frecuencia",
            placeholder = "Ej. Cada 8 horas",
            mensajeError = formulario.errorFrecuencia
        )
        CampoTexto(
            valor = formulario.duracion,
            onValorChange = { onIntent(DetalleIntent.CambiarDuracion(it)) },
            etiqueta = "Duración",
            placeholder = "Ej. 7 días",
            mensajeError = formulario.errorDuracion
        )
        BotonesPanel(
            textoConfirmar = "Guardar receta",
            onConfirmar = { onIntent(DetalleIntent.GuardarReceta) },
            onCancelar = { onIntent(DetalleIntent.CerrarPanel) }
        )
    }
}

@Composable
fun RecetaSheet(
    nombrePaciente: String,
    formulario: FormularioReceta,
    onIntent: (DetalleIntent) -> Unit
) {
    PanelInferior(onDescartar = { onIntent(DetalleIntent.CerrarPanel) }) {
        RecetaContenido(
            nombrePaciente = nombrePaciente,
            formulario = formulario,
            onIntent = onIntent
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
            onIntent = {}
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
            onIntent = {}
        )
    }
}