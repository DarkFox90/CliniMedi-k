package plat.clinimedik.app.ui.screens.medico.pacientes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plat.clinimedik.app.ui.components.CampoTexto
import plat.clinimedik.app.ui.components.ContenedorMedico
import plat.clinimedik.app.ui.components.EncabezadoPantalla
import plat.clinimedik.app.ui.components.PacienteItem
import plat.clinimedik.app.ui.components.PestanaMedico
import plat.clinimedik.app.ui.theme.CliniMedikTheme

@Composable
fun PacientesMedicoScreen(
    state: PacientesMedicoUiState,
    onIntent: (PacientesMedicoIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            EncabezadoPantalla(
                titulo = "Pacientes",
                subtitulo = state.subtitulo,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp)
            )
            CampoTexto(
                valor = state.texto,
                onValorChange = { onIntent(PacientesMedicoIntent.CambiarTexto(it)) },
                etiqueta = "Buscar",
                placeholder = "Nombre o teléfono",
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            if (state.pacientes.isEmpty()) {
                ListaVacia(
                    mensaje = state.mensajeVacio,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Text(
                            text = state.tituloLista,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    items(state.pacientes, key = { it.pacienteId }) { paciente ->
                        PacienteItem(
                            nombre = paciente.nombre,
                            detalle = paciente.detalle,
                            onClick = { onIntent(PacientesMedicoIntent.AbrirHistorial(paciente.pacienteId)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ListaVacia(
    mensaje: String,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.padding(32.dp)
    ) {
        Text(
            text = mensaje,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PacientesMedicoListaPreview() {
    CliniMedikTheme {
        ContenedorMedico(seleccion = PestanaMedico.PACIENTES) {
            PacientesMedicoScreen(
                state = pacientesMedicoUiStateDePrueba(),
                onIntent = {}
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PacientesMedicoSinResultadosPreview() {
    CliniMedikTheme {
        ContenedorMedico(seleccion = PestanaMedico.PACIENTES) {
            PacientesMedicoScreen(
                state = pacientesMedicoUiStateDePrueba(texto = "zzz"),
                onIntent = {}
            )
        }
    }
}
