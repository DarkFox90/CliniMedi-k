package plat.clinimedik.app.ui.screens.recepcion.resultado

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plat.clinimedik.app.ui.components.AvatarIniciales
import plat.clinimedik.app.ui.components.BarraSuperior
import plat.clinimedik.app.ui.components.CampoTexto
import plat.clinimedik.app.ui.components.ChipSeguro
import plat.clinimedik.app.ui.components.ColumnaDesplazable
import plat.clinimedik.app.ui.components.EstadoError
import plat.clinimedik.app.ui.theme.CliniMedikTheme

@Composable
fun ResultadoScreen(
    state: ResultadoUiState,
    onEvent: (ResultadoEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = "Paciente encontrado",
                onRegresar = { onEvent(ResultadoEvent.Regresar) }
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
                    { onEvent(ResultadoEvent.Reintentar) }
                } else {
                    null
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )

            else -> Contenido(
                state = state,
                onEvent = onEvent,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }

    if (state.mostrarFormulario) {
        DialogoAgregarAFila(state = state, onEvent = onEvent)
    }
}

@Composable
private fun Contenido(
    state: ResultadoUiState,
    onEvent: (ResultadoEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    ColumnaDesplazable(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        AvatarIniciales(
            nombre = state.nombre,
            tamano = 72.dp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = state.nombre,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${state.edad} años · ${state.telefono}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (state.aseguradora != null) {
            Spacer(modifier = Modifier.height(12.dp))
            ChipSeguro(aseguradora = state.aseguradora)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = state.resumenVisitas,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        DatoDelPaciente(
            titulo = "Próxima cita",
            valor = state.proximaCita ?: "Sin citas programadas"
        )

        Spacer(modifier = Modifier.height(12.dp))

        DatoDelPaciente(
            titulo = "Última referencia",
            valor = state.ultimaReferencia ?: "Sin referencias anteriores"
        )

        if (state.citaDeHoy != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Tiene cita hoy a las ${state.citaDeHoy.hora}. " +
                        "Al agregarlo a la fila se registra su llegada.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        if (!state.mostrarFormulario && state.mensajeError != null) {
            Text(
                text = state.mensajeError,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = { onEvent(ResultadoEvent.AgregarAFila) },
                enabled = !state.guardando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Agregar a la fila")
            }

            OutlinedButton(
                onClick = { onEvent(ResultadoEvent.VerHistorial) },
                enabled = state.tieneHistorial,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver historial completo")
            }

            OutlinedButton(
                onClick = { onEvent(ResultadoEvent.EscanearOtro) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Escanear otro")
            }
        }
    }
}

@Composable
private fun DatoDelPaciente(
    titulo: String,
    valor: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DialogoAgregarAFila(
    state: ResultadoUiState,
    onEvent: (ResultadoEvent) -> Unit
) {
    val cita = state.citaDelDialogo

    AlertDialog(
        onDismissRequest = {
            if (!state.guardando) {
                onEvent(ResultadoEvent.CancelarAgregarAFila)
            }
        },
        title = {
            Text(if (cita != null) "Registrar llegada" else "Agregar a la fila")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (cita != null) {
                    Text(
                        text = "Cita de hoy a las ${cita.hora} · ${cita.motivo}. " +
                                "El paciente pasará a la fila con ese motivo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    CampoTexto(
                        valor = state.motivo,
                        onValorChange = { onEvent(ResultadoEvent.CambiarMotivo(it)) },
                        etiqueta = "Motivo de consulta",
                        mensajeError = state.errorMotivo,
                        habilitado = !state.guardando
                    )
                }

                if (state.mensajeError != null) {
                    Text(
                        text = state.mensajeError,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onEvent(ResultadoEvent.ConfirmarAgregarAFila) },
                enabled = !state.guardando
            ) {
                Text(if (state.guardando) "Guardando..." else "Confirmar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onEvent(ResultadoEvent.CancelarAgregarAFila) },
                enabled = !state.guardando
            ) {
                Text("Cancelar")
            }
        }
    )
}

private fun resultadoDePrueba(): ResultadoUiState = ResultadoUiState(
    cargando = false,
    nombre = "Álvaro Chacón",
    edad = "42",
    telefono = "5512-7788",
    aseguradora = "Seguros G&T",
    resumenVisitas = "3 visitas · última: 14 de marzo de 2026",
    tieneHistorial = true,
    proximaCita = "25 de noviembre de 2026 · 9:00 a. m. · Control de presión",
    ultimaReferencia = "Cardiología · Dra. Ana Lucía Paz · 14 de marzo de 2026"
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ResultadoConSeguroPreview() {
    CliniMedikTheme {
        ResultadoScreen(state = resultadoDePrueba(), onEvent = {})
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ResultadoPacienteNuevoPreview() {
    CliniMedikTheme {
        ResultadoScreen(
            state = ResultadoUiState(
                cargando = false,
                nombre = "Lucía Ajú",
                edad = "24",
                telefono = "3021-5566",
                resumenVisitas = "Paciente nuevo"
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ResultadoConCitaDeHoyPreview() {
    CliniMedikTheme {
        ResultadoScreen(
            state = resultadoDePrueba().copy(
                citaDeHoy = CitaDeHoy(
                    citaId = "cit-004",
                    hora = "3:00 p. m.",
                    motivo = "Control de presión"
                )
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ResultadoDialogoMotivoPreview() {
    CliniMedikTheme {
        ResultadoScreen(
            state = resultadoDePrueba().copy(
                mostrarFormulario = true,
                errorMotivo = "Ingresa el motivo de consulta"
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ResultadoDialogoCitaPreview() {
    val cita = CitaDeHoy(
        citaId = "cit-004",
        hora = "3:00 p. m.",
        motivo = "Control de presión"
    )
    CliniMedikTheme {
        ResultadoScreen(
            state = resultadoDePrueba().copy(
                citaDeHoy = cita,
                mostrarFormulario = true,
                citaDelDialogo = cita
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ResultadoGuardandoPreview() {
    CliniMedikTheme {
        ResultadoScreen(
            state = resultadoDePrueba().copy(
                mostrarFormulario = true,
                motivo = "Dolor de cabeza",
                guardando = true
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ResultadoErrorAgregarPreview() {
    CliniMedikTheme {
        ResultadoScreen(
            state = resultadoDePrueba().copy(
                mensajeError = "La cita cambió. Vuelve a intentarlo."
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ResultadoCargandoPreview() {
    CliniMedikTheme {
        ResultadoScreen(state = ResultadoUiState(cargando = true), onEvent = {})
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ResultadoErrorCargaPreview() {
    CliniMedikTheme {
        ResultadoScreen(
            state = ResultadoUiState(
                cargando = false,
                mensajeErrorCarga = "No se pudo cargar el paciente",
                cargaReintentable = true
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ResultadoNoEncontradoPreview() {
    CliniMedikTheme {
        ResultadoScreen(
            state = ResultadoUiState(
                cargando = false,
                mensajeErrorCarga = "No se encontró el paciente"
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
private fun ResultadoOscuroPreview() {
    CliniMedikTheme {
        ResultadoScreen(
            state = resultadoDePrueba().copy(
                citaDeHoy = CitaDeHoy(
                    citaId = "cit-004",
                    hora = "3:00 p. m.",
                    motivo = "Control de presión"
                )
            ),
            onEvent = {}
        )
    }
}