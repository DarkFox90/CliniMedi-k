package plat.clinimedik.app.ui.screens.recepcion.agenda

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plat.clinimedik.app.data.model.EstadoConfirmacion
import plat.clinimedik.app.ui.components.ConfirmacionChip
import plat.clinimedik.app.ui.components.PanelInferior
import plat.clinimedik.app.ui.screens.recepcion.detalle.EncabezadoPanel
import plat.clinimedik.app.ui.screens.recepcion.detalle.SuperficiePanelPreview
import plat.clinimedik.app.ui.theme.CliniMedikTheme

@Composable
fun CitaAccionesContenido(
    elemento: ElementoCita,
    puedeRegistrarLlegada: Boolean,
    guardando: Boolean,
    onEvent: (AgendaRecepcionEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
    ) {
        EncabezadoPanel(
            titulo = elemento.nombrePaciente,
            descripcion = "${elemento.hora} · ${elemento.motivo}"
        )
        ConfirmacionChip(estado = elemento.estadoConfirmacion)

        Button(
            onClick = { onEvent(AgendaRecepcionEvent.RegistrarLlegada) },
            enabled = puedeRegistrarLlegada && !guardando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar llegada")
        }
        if (!puedeRegistrarLlegada) {
            Text(
                text = "La llegada solo se registra el día de la cita",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else if (!elemento.tienePaciente) {
            Text(
                text = "Esta cita no tiene paciente registrado: se abrirá el registro",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (elemento.estadoConfirmacion != EstadoConfirmacion.CONFIRMADA) {
            OutlinedButton(
                onClick = {
                    onEvent(AgendaRecepcionEvent.MarcarConfirmacion(EstadoConfirmacion.CONFIRMADA))
                },
                enabled = !guardando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Marcar como confirmada")
            }
        }
        if (elemento.estadoConfirmacion != EstadoConfirmacion.NO_CONFIRMADA) {
            OutlinedButton(
                onClick = {
                    onEvent(AgendaRecepcionEvent.MarcarConfirmacion(EstadoConfirmacion.NO_CONFIRMADA))
                },
                enabled = !guardando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Marcar como no confirmada")
            }
        }
        OutlinedButton(
            onClick = { onEvent(AgendaRecepcionEvent.AbrirReprogramar) },
            enabled = !guardando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reprogramar")
        }
        TextButton(
            onClick = { onEvent(AgendaRecepcionEvent.SolicitarCancelacion) },
            enabled = !guardando,
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancelar cita")
        }
        TextButton(
            onClick = { onEvent(AgendaRecepcionEvent.CerrarPanel) },
            enabled = !guardando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar")
        }
    }
}

@Composable
fun CitaAccionesSheet(
    elemento: ElementoCita,
    puedeRegistrarLlegada: Boolean,
    guardando: Boolean,
    onEvent: (AgendaRecepcionEvent) -> Unit
) {
    PanelInferior(onDescartar = { onEvent(AgendaRecepcionEvent.CerrarPanel) }) {
        CitaAccionesContenido(
            elemento = elemento,
            puedeRegistrarLlegada = puedeRegistrarLlegada,
            guardando = guardando,
            onEvent = onEvent
        )
    }
}

@Composable
fun CancelarCitaDialogo(
    elemento: ElementoCita,
    guardando: Boolean,
    onEvent: (AgendaRecepcionEvent) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onEvent(AgendaRecepcionEvent.CerrarPanel) },
        title = { Text("¿Cancelar la cita?") },
        text = { Text("${elemento.nombrePaciente} · ${elemento.hora}. Se liberará ese horario.") },
        confirmButton = {
            TextButton(
                onClick = { onEvent(AgendaRecepcionEvent.ConfirmarCancelacion) },
                enabled = !guardando,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Cancelar cita")
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onEvent(AgendaRecepcionEvent.CerrarPanel) },
                enabled = !guardando
            ) {
                Text("Volver")
            }
        }
    )
}

private fun elementoDePrueba(citaId: String): ElementoCita =
    agendaUiStateDePrueba().elementos.first { it.citaId == citaId }

@Preview(showBackground = true)
@Composable
private fun AccionesConPacientePreview() {
    SuperficiePanelPreview {
        CitaAccionesContenido(
            elemento = elementoDePrueba("cit-004"),
            puedeRegistrarLlegada = true,
            guardando = false,
            onEvent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AccionesSinPacientePreview() {
    SuperficiePanelPreview {
        CitaAccionesContenido(
            elemento = elementoDePrueba("cit-003"),
            puedeRegistrarLlegada = true,
            guardando = false,
            onEvent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AccionesOtroDiaPreview() {
    SuperficiePanelPreview {
        CitaAccionesContenido(
            elemento = agendaUiStateDePrueba(diasDesdeHoy = 1).elementos.first { it.citaId == "cit-005" },
            puedeRegistrarLlegada = false,
            guardando = false,
            onEvent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AccionesGuardandoPreview() {
    SuperficiePanelPreview {
        CitaAccionesContenido(
            elemento = elementoDePrueba("cit-004"),
            puedeRegistrarLlegada = true,
            guardando = true,
            onEvent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CancelarCitaDialogoPreview() {
    CliniMedikTheme {
        CancelarCitaDialogo(
            elemento = elementoDePrueba("cit-004"),
            guardando = false,
            onEvent = {}
        )
    }
}