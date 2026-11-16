package plat.clinimedik.app.ui.screens.recepcion.nuevacita

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plat.clinimedik.app.ui.components.BarraSuperior
import plat.clinimedik.app.ui.components.CampoFecha
import plat.clinimedik.app.ui.components.CampoHora
import plat.clinimedik.app.ui.components.CampoTexto
import plat.clinimedik.app.ui.components.EstadoError
import plat.clinimedik.app.ui.components.PacienteItem
import plat.clinimedik.app.ui.theme.CliniMedikTheme

@Composable
fun NuevaCitaScreen(
    state: NuevaCitaUiState,
    onEvent: (NuevaCitaEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = "Nueva cita",
                onRegresar = { onEvent(NuevaCitaEvent.Regresar) }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        when {
            state.cargando -> Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                CircularProgressIndicator()
            }

            state.mensajeErrorCarga != null -> EstadoError(
                mensaje = state.mensajeErrorCarga,
                onReintentar = if (state.cargaReintentable) {
                    { onEvent(NuevaCitaEvent.Reintentar) }
                } else {
                    null
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )

            else -> Formulario(
                state = state,
                onEvent = onEvent,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }
}

@Composable
private fun Formulario(
    state: NuevaCitaUiState,
    onEvent: (NuevaCitaEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        SeccionPaciente(state = state, onEvent = onEvent)

        CampoTexto(
            valor = state.correo,
            onValorChange = { onEvent(NuevaCitaEvent.CambiarCorreo(it)) },
            etiqueta = "Correo electrónico",
            placeholder = if (state.pacienteElegido) "Opcional" else "Obligatorio si no está registrado",
            mensajeError = state.errorCorreo,
            tipoTeclado = KeyboardType.Email,
            habilitado = !state.guardando,
            modifier = Modifier.fillMaxWidth()
        )

        CampoFecha(
            valor = state.fecha,
            onValorChange = { onEvent(NuevaCitaEvent.CambiarFecha(it)) },
            etiqueta = "Fecha",
            mensajeError = state.errorFecha,
            permitePasado = false,
            habilitado = !state.guardando,
            modifier = Modifier.fillMaxWidth()
        )

        CampoHora(
            valor = state.hora,
            onValorChange = { onEvent(NuevaCitaEvent.CambiarHora(it)) },
            etiqueta = "Hora",
            mensajeError = state.errorHora,
            habilitado = !state.guardando,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "La duración de la cita es de 30 minutos.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        CampoTexto(
            valor = state.motivo,
            onValorChange = { onEvent(NuevaCitaEvent.CambiarMotivo(it)) },
            etiqueta = "Motivo de la cita",
            mensajeError = state.errorMotivo,
            lineas = 3,
            habilitado = !state.guardando,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (state.mensajeError != null) {
            Text(
                text = state.mensajeError,
                color = MaterialTheme.colorScheme.error
            )
        }

        Button(
            onClick = { onEvent(NuevaCitaEvent.Guardar) },
            enabled = !state.guardando,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.guardando) {
                CircularProgressIndicator(
                    modifier = Modifier.height(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(text = "Guardar cita")
            }
        }
    }
}

@Composable
private fun SeccionPaciente(
    state: NuevaCitaUiState,
    onEvent: (NuevaCitaEvent) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (state.pacienteElegido) {
            PacienteItem(
                nombre = state.paciente,
                detalle = state.pacienteTelefono,
                accion = {
                    TextButton(
                        onClick = { onEvent(NuevaCitaEvent.QuitarPaciente) },
                        enabled = !state.guardando
                    ) {
                        Text("Cambiar")
                    }
                }
            )
        } else {
            CampoTexto(
                valor = state.paciente,
                onValorChange = { onEvent(NuevaCitaEvent.CambiarPaciente(it)) },
                etiqueta = "Paciente",
                placeholder = "Escribe el nombre para buscar",
                mensajeError = state.errorPaciente ?: state.errorBusqueda,
                habilitado = !state.guardando,
                modifier = Modifier.fillMaxWidth()
            )
            state.sugerenciasVisibles.forEach { sugerido ->
                PacienteItem(
                    nombre = sugerido.nombre,
                    detalle = sugerido.telefono,
                    onClick = { onEvent(NuevaCitaEvent.ElegirPaciente(sugerido.pacienteId)) }
                )
            }
            if (state.sinCoincidencias) {
                Text(
                    text = "No hay pacientes con ese nombre. Se agendará como paciente nuevo; escribe su correo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun pacienteDePrueba(): NuevaCitaUiState = NuevaCitaUiState(
    pacienteId = "pac-001",
    paciente = "Álvaro Chacón",
    pacienteTelefono = "5512-7788",
    correo = "alvaro.chacon@example.com",
    motivo = "Seguimiento: Dolor torácico"
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun NuevaCitaVaciaPreview() {
    CliniMedikTheme {
        NuevaCitaScreen(state = NuevaCitaUiState(), onEvent = {})
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun NuevaCitaConSugerenciasPreview() {
    CliniMedikTheme {
        NuevaCitaScreen(
            state = NuevaCitaUiState(
                paciente = "Mar",
                busquedaResuelta = "Mar",
                sugerencias = listOf(
                    PacienteSugerido("pac-002", "Marta Ríos", "5599-8877", "marta.rios@example.com"),
                    PacienteSugerido("pac-009", "Mario Gómez", "4455-6677", "mario.gomez@example.com")
                )
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun NuevaCitaSinCoincidenciasPreview() {
    CliniMedikTheme {
        NuevaCitaScreen(
            state = NuevaCitaUiState(
                paciente = "Zoila Pérez",
                busquedaResuelta = "Zoila Pérez"
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun NuevaCitaPacienteElegidoPreview() {
    CliniMedikTheme {
        NuevaCitaScreen(state = pacienteDePrueba(), onEvent = {})
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun NuevaCitaCargandoPreview() {
    CliniMedikTheme {
        NuevaCitaScreen(state = NuevaCitaUiState(cargando = true), onEvent = {})
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun NuevaCitaErrorCargaPreview() {
    CliniMedikTheme {
        NuevaCitaScreen(
            state = NuevaCitaUiState(
                mensajeErrorCarga = "No se pudo cargar el paciente",
                cargaReintentable = true
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun NuevaCitaErrorCamposPreview() {
    CliniMedikTheme {
        NuevaCitaScreen(
            state = NuevaCitaUiState(
                errorPaciente = "Ingresa el nombre del paciente",
                errorFecha = "Selecciona la fecha de la cita",
                errorHora = "Selecciona la hora de la cita",
                errorMotivo = "Ingresa el motivo de la cita"
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun NuevaCitaConflictoPreview() {
    CliniMedikTheme {
        NuevaCitaScreen(
            state = pacienteDePrueba().copy(
                fecha = "24/11/2026",
                hora = "14:30",
                errorHora = "Horario ocupado por Marta Ríos"
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun NuevaCitaErrorGuardarPreview() {
    CliniMedikTheme {
        NuevaCitaScreen(
            state = pacienteDePrueba().copy(
                fecha = "24/11/2026",
                hora = "14:30",
                mensajeError = "No se pudo guardar la cita. Inténtalo de nuevo."
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun NuevaCitaGuardandoPreview() {
    CliniMedikTheme {
        NuevaCitaScreen(
            state = pacienteDePrueba().copy(
                fecha = "24/11/2026",
                hora = "14:30",
                guardando = true
            ),
            onEvent = {}
        )
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun NuevaCitaOscuroPreview() {
    CliniMedikTheme {
        NuevaCitaScreen(
            state = pacienteDePrueba().copy(fecha = "24/11/2026", hora = "14:30"),
            onEvent = {}
        )
    }
}