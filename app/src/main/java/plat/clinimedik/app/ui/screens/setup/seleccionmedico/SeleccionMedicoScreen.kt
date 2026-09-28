package plat.clinimedik.app.ui.screens.setup.seleccionmedico

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plat.clinimedik.app.data.model.Medico
import plat.clinimedik.app.ui.components.AvatarIniciales
import plat.clinimedik.app.ui.components.CampoTexto
import plat.clinimedik.app.ui.components.EncabezadoPantalla
import plat.clinimedik.app.ui.theme.CliniMedikTheme

@Composable
fun SeleccionMedicoScreen(
    state: SeleccionMedicoUiState,
    onIntent: (SeleccionMedicoIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                EncabezadoPantalla(
                    titulo = "Médico asignado",
                    subtitulo = "Configuración inicial"
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Selecciona el médico con el que trabajará este dispositivo o agrega uno nuevo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.selectableGroup()
                ) {
                    state.medicos.forEach { medico ->
                        TarjetaMedico(
                            medico = medico,
                            seleccionado = medico.id == state.medicoSeleccionadoId,
                            onClick = { onIntent(SeleccionMedicoIntent.SeleccionarMedico(medico.id)) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                val formulario = state.formulario
                if (formulario == null) {
                    OutlinedButton(
                        onClick = { onIntent(SeleccionMedicoIntent.MostrarFormulario) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                        Text(text = "Agregar médico nuevo")
                    }
                } else {
                    FormularioMedicoNuevo(
                        formulario = formulario,
                        onIntent = onIntent
                    )
                }
            }
            Button(
                onClick = { onIntent(SeleccionMedicoIntent.Continuar) },
                enabled = state.puedeContinuar,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(text = "Continuar")
            }
        }
    }
}

@Composable
private fun TarjetaMedico(
    medico: Medico,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    val colorFondo = if (seleccionado) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerLowest
    }
    val colorNombre = if (seleccionado) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val colorEspecialidad = if (seleccionado) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val bordeAvatar = if (seleccionado) {
        Modifier.border(width = 2.dp, color = MaterialTheme.colorScheme.primary, shape = CircleShape)
    } else {
        Modifier
    }

    Surface(
        selected = seleccionado,
        onClick = onClick,
        color = colorFondo,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            AvatarIniciales(
                nombre = medico.nombre,
                modifier = bordeAvatar
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = medico.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    color = colorNombre
                )
                Text(
                    text = medico.especialidad,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorEspecialidad
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            RadioButton(
                selected = seleccionado,
                onClick = null
            )
        }
    }
}

@Composable
private fun FormularioMedicoNuevo(
    formulario: FormularioMedico,
    onIntent: (SeleccionMedicoIntent) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Médico nuevo",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            CampoTexto(
                valor = formulario.nombre,
                onValorChange = { onIntent(SeleccionMedicoIntent.CambiarNombre(it)) },
                etiqueta = "Nombre del médico",
                placeholder = "Ej. Dra. María López",
                mensajeError = formulario.errorNombre
            )
            CampoTexto(
                valor = formulario.especialidad,
                onValorChange = { onIntent(SeleccionMedicoIntent.CambiarEspecialidad(it)) },
                etiqueta = "Especialidad",
                placeholder = "Ej. Pediatría",
                mensajeError = formulario.errorEspecialidad
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SeleccionMedicoListaPreview() {
    CliniMedikTheme {
        SeleccionMedicoScreen(
            state = seleccionMedicoUiStateDePrueba(
                eleccion = MedicoElegido.Existente(medicoId = "med-001")
            ),
            onIntent = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SeleccionMedicoFormularioPreview() {
    CliniMedikTheme {
        SeleccionMedicoScreen(
            state = seleccionMedicoUiStateDePrueba(
                eleccion = MedicoElegido.Nuevo()
            ),
            onIntent = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SeleccionMedicoErrorPreview() {
    CliniMedikTheme {
        SeleccionMedicoScreen(
            state = seleccionMedicoUiStateDePrueba(
                eleccion = MedicoElegido.Nuevo(
                    formulario = FormularioMedico(
                        especialidad = "Pediatría",
                        errorNombre = "El nombre es obligatorio"
                    )
                )
            ),
            onIntent = {}
        )
    }
}
