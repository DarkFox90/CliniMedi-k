package plat.clinimedik.app.ui.screens.recepcion.agenda

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plat.clinimedik.app.ui.components.CampoFecha
import plat.clinimedik.app.ui.components.CampoHora
import plat.clinimedik.app.ui.components.PanelInferior
import plat.clinimedik.app.ui.screens.recepcion.detalle.BotonesPanel
import plat.clinimedik.app.ui.screens.recepcion.detalle.EncabezadoPanel
import plat.clinimedik.app.ui.screens.recepcion.detalle.SuperficiePanelPreview

@Composable
fun ReprogramarContenido(
    elemento: ElementoCita,
    formulario: FormularioReprogramar,
    guardando: Boolean,
    onEvent: (AgendaRecepcionEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
    ) {
        EncabezadoPanel(
            titulo = "Reprogramar cita",
            descripcion = "${elemento.nombrePaciente}. Elige el nuevo día y la nueva hora; la confirmación se reinicia."
        )
        CampoFecha(
            valor = formulario.fecha,
            onValorChange = { onEvent(AgendaRecepcionEvent.CambiarFechaReprogramar(it)) },
            etiqueta = "Nueva fecha",
            mensajeError = formulario.errorFecha,
            permitePasado = false,
            habilitado = !guardando
        )
        CampoHora(
            valor = formulario.hora,
            onValorChange = { onEvent(AgendaRecepcionEvent.CambiarHoraReprogramar(it)) },
            etiqueta = "Nueva hora",
            mensajeError = formulario.errorHora,
            habilitado = !guardando
        )
        BotonesPanel(
            textoConfirmar = "Reprogramar",
            onConfirmar = { onEvent(AgendaRecepcionEvent.ConfirmarReprogramar) },
            onCancelar = { onEvent(AgendaRecepcionEvent.CerrarPanel) },
            habilitado = !guardando
        )
    }
}

@Composable
fun ReprogramarSheet(
    elemento: ElementoCita,
    formulario: FormularioReprogramar,
    guardando: Boolean,
    onEvent: (AgendaRecepcionEvent) -> Unit
) {
    PanelInferior(onDescartar = { onEvent(AgendaRecepcionEvent.CerrarPanel) }) {
        ReprogramarContenido(
            elemento = elemento,
            formulario = formulario,
            guardando = guardando,
            onEvent = onEvent
        )
    }
}

private fun elementoDePruebaReprogramar(): ElementoCita =
    agendaUiStateDePrueba(diasDesdeHoy = 1).elementos.first { it.citaId == "cit-005" }

@Preview(showBackground = true)
@Composable
private fun ReprogramarVacioPreview() {
    SuperficiePanelPreview {
        ReprogramarContenido(
            elemento = elementoDePruebaReprogramar(),
            formulario = FormularioReprogramar(),
            guardando = false,
            onEvent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReprogramarLlenoPreview() {
    SuperficiePanelPreview {
        ReprogramarContenido(
            elemento = elementoDePruebaReprogramar(),
            formulario = FormularioReprogramar(fecha = "20/11/2026", hora = "10:30"),
            guardando = false,
            onEvent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReprogramarOcupadoPreview() {
    SuperficiePanelPreview {
        ReprogramarContenido(
            elemento = elementoDePruebaReprogramar(),
            formulario = FormularioReprogramar(
                fecha = "20/11/2026",
                hora = "10:30",
                errorHora = "Horario ocupado por Marta Ríos"
            ),
            guardando = false,
            onEvent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReprogramarGuardandoPreview() {
    SuperficiePanelPreview {
        ReprogramarContenido(
            elemento = elementoDePruebaReprogramar(),
            formulario = FormularioReprogramar(fecha = "20/11/2026", hora = "10:30"),
            guardando = true,
            onEvent = {}
        )
    }
}