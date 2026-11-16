package plat.clinimedik.app.ui.screens.recepcion.detalle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plat.clinimedik.app.data.model.TipoDocumento
import plat.clinimedik.app.ui.components.PanelInferior
import plat.clinimedik.app.ui.components.etiqueta

@Composable
fun DocumentoContenido(
    onEvent: (DetalleEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
    ) {
        EncabezadoPanel(
            titulo = "Adjuntar documento",
            descripcion = "Elige qué documento vas a fotografiar."
        )
        TipoDocumento.entries.forEach { tipo ->
            OutlinedButton(
                onClick = { onEvent(DetalleEvent.ElegirTipoDocumento(tipo)) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(tipo.etiqueta())
            }
        }
        TextButton(
            onClick = { onEvent(DetalleEvent.CerrarPanel) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancelar")
        }
    }
}

@Composable
fun DocumentoSheet(
    onEvent: (DetalleEvent) -> Unit
) {
    PanelInferior(onDescartar = { onEvent(DetalleEvent.CerrarPanel) }) {
        DocumentoContenido(onEvent = onEvent)
    }
}

@Preview(showBackground = true)
@Composable
private fun DocumentoPreview() {
    SuperficiePanelPreview {
        DocumentoContenido(onEvent = {})
    }
}