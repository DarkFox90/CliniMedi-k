package plat.clinimedik.app.ui.screens.recepcion.cuestionario

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plat.clinimedik.app.data.model.EstadoCivil
import plat.clinimedik.app.ui.components.BarraSuperior
import plat.clinimedik.app.ui.components.CampoFecha
import plat.clinimedik.app.ui.components.CampoTexto
import plat.clinimedik.app.ui.components.EstadoError
import plat.clinimedik.app.ui.components.etiqueta
import plat.clinimedik.app.ui.theme.CliniMedikTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuestionarioScreen(
    state: CuestionarioUiState,
    onEvent: (CuestionarioEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = "Nuevo paciente",
                onRegresar = { onEvent(CuestionarioEvent.Regresar) }
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
                    { onEvent(CuestionarioEvent.Reintentar) }
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
    state: CuestionarioUiState,
    onEvent: (CuestionarioEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(text = "Datos generales", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

        CampoTexto(
            valor = state.nombre,
            onValorChange = { onEvent(CuestionarioEvent.CambiarNombre(it)) },
            etiqueta = "Nombre completo",
            mensajeError = state.errorNombre,
            modifier = Modifier.fillMaxWidth()
        )
        CampoFecha(
            valor = state.fechaNacimiento,
            onValorChange = { onEvent(CuestionarioEvent.CambiarFechaNacimiento(it)) },
            etiqueta = "Fecha de nacimiento",
            mensajeError = state.errorFechaNacimiento,
            permiteFuturo = false,
            modifier = Modifier.fillMaxWidth()
        )

        Text(text = "Estado civil", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            EstadoCivil.entries.forEach { estado ->
                FilterChip(
                    selected = state.estadoCivil == estado,
                    onClick = { onEvent(CuestionarioEvent.SeleccionarEstadoCivil(estado)) },
                    label = { Text(estado.etiqueta()) }
                )
            }
        }

        CampoTexto(
            valor = state.ocupacion,
            onValorChange = { onEvent(CuestionarioEvent.CambiarOcupacion(it)) },
            etiqueta = "Ocupación",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Contacto", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

        CampoTexto(
            valor = state.telefono,
            onValorChange = { onEvent(CuestionarioEvent.CambiarTelefono(it)) },
            etiqueta = "Teléfono",
            mensajeError = state.errorTelefono,
            tipoTeclado = KeyboardType.Phone,
            modifier = Modifier.fillMaxWidth()
        )
        CampoTexto(
            valor = state.correo,
            onValorChange = { onEvent(CuestionarioEvent.CambiarCorreo(it)) },
            etiqueta = "Correo electrónico",
            mensajeError = state.errorCorreo,
            tipoTeclado = KeyboardType.Email,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Consulta", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

        CampoTexto(
            valor = state.motivo,
            onValorChange = { onEvent(CuestionarioEvent.CambiarMotivo(it)) },
            etiqueta = "Motivo de consulta",
            lineas = 3,
            modifier = Modifier.fillMaxWidth(),
            mensajeError = state.errorMotivo,
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Antecedentes", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

        CampoTexto(
            valor = state.alergias,
            onValorChange = { onEvent(CuestionarioEvent.CambiarAlergias(it)) },
            etiqueta = "Alergias",
            lineas = 2,
            modifier = Modifier.fillMaxWidth()
        )
        CampoTexto(
            valor = state.enfermedades,
            onValorChange = { onEvent(CuestionarioEvent.CambiarEnfermedades(it)) },
            etiqueta = "Enfermedades previas",
            lineas = 2,
            modifier = Modifier.fillMaxWidth()
        )
        CampoTexto(
            valor = state.tratamientos,
            onValorChange = { onEvent(CuestionarioEvent.CambiarTratamientos(it)) },
            etiqueta = "Tratamientos previos",
            lineas = 2,
            modifier = Modifier.fillMaxWidth()
        )
        CampoTexto(
            valor = state.cirugias,
            onValorChange = { onEvent(CuestionarioEvent.CambiarCirugias(it)) },
            etiqueta = "Cirugías",
            lineas = 2,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Seguro médico", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Tiene seguro",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Switch(
                checked = state.tieneSeguro,
                onCheckedChange = { onEvent(CuestionarioEvent.CambiarTieneSeguro(it)) },
                enabled = !state.guardando
            )
        }
        if (state.tieneSeguro) {
            CampoTexto(
                valor = state.aseguradora,
                onValorChange = { onEvent(CuestionarioEvent.CambiarAseguradora(it)) },
                etiqueta = "Aseguradora",
                mensajeError = state.errorAseguradora,
                modifier = Modifier.fillMaxWidth()
            )
            CampoTexto(
                valor = state.numeroPoliza,
                onValorChange = { onEvent(CuestionarioEvent.CambiarPoliza(it)) },
                etiqueta = "Número de póliza o afiliado (opcional)",
                modifier = Modifier.fillMaxWidth()
            )
            CampoTexto(
                valor = state.titularPoliza,
                onValorChange = { onEvent(CuestionarioEvent.CambiarTitular(it)) },
                etiqueta = "Titular de la póliza (opcional)",
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.mensajeError != null) {
            Text(
                text = state.mensajeError,
                color = MaterialTheme.colorScheme.error
            )
        }

        Button(
            onClick = { onEvent(CuestionarioEvent.Guardar) },
            enabled = !state.guardando,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.guardando) {
                CircularProgressIndicator(
                    modifier = Modifier.height(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(text = "Guardar y generar QR")
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CuestionarioVacioPreview() {
    CliniMedikTheme {
        CuestionarioScreen(state = CuestionarioUiState(), onEvent = {})
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CuestionarioErrorFaltantesPreview() {
    CliniMedikTheme {
        CuestionarioScreen(
            state = CuestionarioUiState(
                errorNombre = "El nombre es obligatorio",
                errorFechaNacimiento = "La fecha de nacimiento es obligatoria",
                errorTelefono = "El teléfono es obligatorio"
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CuestionarioErrorInvalidoPreview() {
    CliniMedikTheme {
        CuestionarioScreen(
            state = CuestionarioUiState(
                nombre = "Lucía Ajú",
                fechaNacimiento = "02/09/2001",
                telefono = "55551234",
                correo = "correo-invalido",
                errorCorreo = "El formato del correo no es válido"
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CuestionarioConSeguroPreview() {
    CliniMedikTheme {
        CuestionarioScreen(
            state = CuestionarioUiState(
                nombre = "Lucía Ajú",
                tieneSeguro = true,
                aseguradora = "Seguros G&T",
                numeroPoliza = "POL-48210"
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CuestionarioSeguroSinAseguradoraPreview() {
    CliniMedikTheme {
        CuestionarioScreen(
            state = CuestionarioUiState(
                tieneSeguro = true,
                errorAseguradora = "Ingresa la aseguradora"
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CuestionarioDesdeCitaPreview() {
    CliniMedikTheme {
        CuestionarioScreen(
            state = CuestionarioUiState(
                nombre = "Sofía Hernández",
                correo = "sofia.hernandez@example.com",
                motivo = "Primera consulta"
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CuestionarioCargandoPreview() {
    CliniMedikTheme {
        CuestionarioScreen(state = CuestionarioUiState(cargando = true), onEvent = {})
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CuestionarioErrorCargaPreview() {
    CliniMedikTheme {
        CuestionarioScreen(
            state = CuestionarioUiState(
                mensajeErrorCarga = "No se pudo cargar la cita",
                cargaReintentable = true
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CuestionarioCitaNoDisponiblePreview() {
    CliniMedikTheme {
        CuestionarioScreen(
            state = CuestionarioUiState(
                mensajeErrorCarga = "Esta cita ya no está disponible",
                cargaReintentable = false
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CuestionarioGuardandoPreview() {
    CliniMedikTheme {
        CuestionarioScreen(
            state = CuestionarioUiState(nombre = "Lucía Ajú", guardando = true),
            onEvent = {}
        )
    }
}