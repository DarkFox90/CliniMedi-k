package plat.clinimedik.app.ui.screens.recepcion.nuevacita

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plat.clinimedik.app.ui.components.BarraSuperior
import plat.clinimedik.app.ui.components.CampoTexto
import plat.clinimedik.app.ui.theme.CliniMedikTheme

@Composable
fun NuevaCitaScreen(
    state: NuevaCitaUiState,
    onIntent: (NuevaCitaIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = "Nueva cita",
                onRegresar = { onIntent(NuevaCitaIntent.Regresar) }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            CampoTexto(
                valor = state.paciente,
                onValorChange = { onIntent(NuevaCitaIntent.CambiarPaciente(it)) },
                etiqueta = "Paciente",
                mensajeError = state.errorPaciente,
                modifier = Modifier.fillMaxWidth()
            )

            CampoTexto(
                valor = state.correo,
                onValorChange = { onIntent(NuevaCitaIntent.CambiarCorreo(it)) },
                etiqueta = "Correo electrónico",
                placeholder = "Obligatorio si no está registrado",
                mensajeError = state.errorCorreo,
                tipoTeclado = KeyboardType.Email,
                modifier = Modifier.fillMaxWidth()
            )

            CampoTexto(
                valor = state.fecha,
                onValorChange = { onIntent(NuevaCitaIntent.CambiarFecha(it)) },
                etiqueta = "Fecha",
                placeholder = "dd/mm/aaaa",
                mensajeError = state.errorFecha,
                modifier = Modifier.fillMaxWidth()
            )

            CampoTexto(
                valor = state.hora,
                onValorChange = { onIntent(NuevaCitaIntent.CambiarHora(it)) },
                etiqueta = "Hora",
                placeholder = "9:30 a. m.",
                mensajeError = state.errorHora,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "La duración de la cita es de 30 minutos.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            CampoTexto(
                valor = state.motivo,
                onValorChange = { onIntent(NuevaCitaIntent.CambiarMotivo(it)) },
                etiqueta = "Motivo de la cita",
                mensajeError = state.errorMotivo,
                lineas = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { onIntent(NuevaCitaIntent.Guardar) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Guardar cita")
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun NuevaCitaVaciaPreview() {
    CliniMedikTheme {
        NuevaCitaScreen(state = NuevaCitaUiState(), onIntent = {})
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun NuevaCitaErrorCamposPreview() {
    CliniMedikTheme {
        NuevaCitaScreen(
            state = NuevaCitaUiState(
                errorPaciente = "Debe ingresar el nombre del paciente",
                errorFecha = "La fecha es obligatoria",
                errorHora = "La hora es obligatoria"
            ),
            onIntent = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun NuevaCitaConflictoPreview() {
    CliniMedikTheme {
        NuevaCitaScreen(
            state = NuevaCitaUiState(
                paciente = "Álvaro Chacón",
                fecha = "24/09/2026",
                hora = "9:30 a. m.",
                motivo = "Control de rutina",
                errorHora = "El Dr. Roberto Sical ya tiene una cita a las 9:30 a. m."
            ),
            onIntent = {}
        )
    }
}