package plat.clinimedik.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import plat.clinimedik.app.ui.theme.CliniMedikTheme

@Composable
fun ContenedorRecepcion(
    seleccion: PestanaRecepcion,
    modifier: Modifier = Modifier,
    onSeleccionar: (PestanaRecepcion) -> Unit = {},
    contenido: @Composable () -> Unit
) {
    Scaffold(
        bottomBar = {
            BarraNavegacionRecepcion(seleccion = seleccion, onSeleccionar = onSeleccionar)
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            contenido()
        }
    }
}

@Composable
fun ContenedorMedico(
    seleccion: PestanaMedico,
    modifier: Modifier = Modifier,
    onSeleccionar: (PestanaMedico) -> Unit = {},
    contenido: @Composable () -> Unit
) {
    Scaffold(
        bottomBar = {
            BarraNavegacionMedico(seleccion = seleccion, onSeleccionar = onSeleccionar)
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            contenido()
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ContenedorRecepcionPreview() {
    CliniMedikTheme {
        ContenedorRecepcion(seleccion = PestanaRecepcion.INICIO) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Contenido de la pantalla")
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ContenedorMedicoPreview() {
    CliniMedikTheme {
        ContenedorMedico(seleccion = PestanaMedico.FILA) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Contenido de la pantalla")
            }
        }
    }
}