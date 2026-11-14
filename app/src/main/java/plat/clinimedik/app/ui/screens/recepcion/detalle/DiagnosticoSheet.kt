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
fun DiagnosticoContenido(
    nombrePaciente: String,
    formulario: FormularioDiagnostico,
    finalizaConsulta: Boolean,
    onEvent: (DetalleEvent) -> Unit,
    modifier: Modifier = Modifier,
    guardando: Boolean = false
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        EncabezadoPanel(
            titulo = if (finalizaConsulta) "Finalizar consulta" else "Diagnóstico de la visita",
            descripcion = "Diagnóstico e indicaciones que dicta el médico para $nombrePaciente."
        )
        CampoTexto(
            valor = formulario.diagnostico,
            onValorChange = { onEvent(DetalleEvent.CambiarDiagnostico(it)) },
            etiqueta = "Diagnóstico",
            placeholder = "Ej. Faringitis aguda",
            mensajeError = formulario.errorDiagnostico,
            lineas = 2
        )
        CampoTexto(
            valor = formulario.notas,
            onValorChange = { onEvent(DetalleEvent.CambiarNotas(it)) },
            etiqueta = "Indicaciones o notas (opcional)",
            placeholder = "Ej. Reposo por 3 días y control en una semana",
            lineas = 3
        )
        BotonesPanel(
            textoConfirmar = if (finalizaConsulta) "Finalizar" else "Guardar",
            onConfirmar = { onEvent(DetalleEvent.GuardarDiagnostico) },
            onCancelar = { onEvent(DetalleEvent.CerrarPanel) },
            habilitado = !guardando
        )
    }
}

@Composable
fun DiagnosticoSheet(
    nombrePaciente: String,
    formulario: FormularioDiagnostico,
    finalizaConsulta: Boolean,
    onEvent: (DetalleEvent) -> Unit,
    guardando: Boolean = false
) {
    PanelInferior(onDescartar = { onEvent(DetalleEvent.CerrarPanel) }) {
        DiagnosticoContenido(
            nombrePaciente = nombrePaciente,
            formulario = formulario,
            finalizaConsulta = finalizaConsulta,
            onEvent = onEvent,
            guardando = guardando
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DiagnosticoVacioPreview() {
    SuperficiePanelPreview {
        DiagnosticoContenido(
            nombrePaciente = "Julio Estrada",
            formulario = FormularioDiagnostico(),
            finalizaConsulta = true,
            onEvent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DiagnosticoErrorPreview() {
    SuperficiePanelPreview {
        DiagnosticoContenido(
            nombrePaciente = "Julio Estrada",
            formulario = FormularioDiagnostico(
                notas = "Control en una semana",
                errorDiagnostico = "Escribe el diagnóstico"
            ),
            finalizaConsulta = false,
            onEvent = {}
        )
    }
}
