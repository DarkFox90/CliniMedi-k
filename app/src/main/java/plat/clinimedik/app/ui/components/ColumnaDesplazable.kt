package plat.clinimedik.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Columna que ocupa al menos el alto disponible y se desplaza cuando el contenido no cabe,
 * por ejemplo con el teléfono en horizontal. Con espacio de sobra se comporta como una columna normal:
 * `Arrangement.Center` centra el contenido y un `Spacer(Modifier.weight(1f))` empuja los botones hacia abajo.
 */
@Composable
fun ColumnaDesplazable(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    contenido: @Composable ColumnScope.() -> Unit
) {
    BoxWithConstraints(modifier = modifier) {
        val altoMinimo = if (maxHeight == Dp.Infinity) 0.dp else maxHeight
        Column(
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = altoMinimo),
            content = contenido
        )
    }
}
