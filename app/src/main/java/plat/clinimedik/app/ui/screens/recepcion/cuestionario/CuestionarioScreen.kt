package plat.clinimedik.app.ui.screens.recepcion.cuestionario

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plat.clinimedik.app.data.model.EstadoCivil
import plat.clinimedik.app.ui.components.BarraSuperior
import plat.clinimedik.app.ui.components.CampoTexto
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
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
            CampoTexto(
                valor = state.fechaNacimiento,
                onValorChange = { onEvent(CuestionarioEvent.CambiarFechaNacimiento(it)) },
                etiqueta = "Fecha de nacimiento",
                placeholder = "dd/mm/aaaa",
                mensajeError = state.errorFechaNacimiento,
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
                modifier = Modifier.fillMaxWidth()
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

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onEvent(CuestionarioEvent.Guardar) },
                modifier = Modifier.fillMaxWidth()
            ) {
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