package plat.clinimedik.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlinx.coroutines.launch
import plat.clinimedik.app.ui.theme.CliniMedikTheme

private val altoFila = 40.dp
private const val FILAS_VISIBLES = 5

/**
 * Rueda vertical para elegir un número, como la de las alarmas del teléfono.
 * El valor elegido es el que queda en la franja central; también se puede tocar un número para llevarlo al centro.
 * [seleccionado] solo define la posición inicial. [valores] no puede estar vacío.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RuedaNumeros(
    valores: List<Int>,
    seleccionado: Int,
    onSeleccionChange: (Int) -> Unit,
    descripcion: String,
    modifier: Modifier = Modifier,
    formato: (Int) -> String = { it.toString().padStart(2, '0') }
) {
    val estado = rememberLazyListState(
        initialFirstVisibleItemIndex = valores.indexOf(seleccionado).coerceAtLeast(0)
    )
    val ajuste = rememberSnapFlingBehavior(lazyListState = estado)
    val alcance = rememberCoroutineScope()
    val cambio by rememberUpdatedState(onSeleccionChange)
    val indiceCentral by remember(estado) { derivedStateOf { estado.indiceCentral() } }

    LaunchedEffect(estado, valores) {
        snapshotFlow { estado.indiceCentral() }
            .collect { indice -> valores.getOrNull(indice)?.let { cambio(it) } }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .width(72.dp)
            .height(altoFila * FILAS_VISIBLES)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(altoFila)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
        )
        LazyColumn(
            state = estado,
            flingBehavior = ajuste,
            contentPadding = PaddingValues(vertical = altoFila * (FILAS_VISIBLES / 2)),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .semantics { contentDescription = descripcion }
        ) {
            items(count = valores.size) { indice ->
                val esCentral = indice == indiceCentral
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(altoFila)
                        .clickable { alcance.launch { estado.animateScrollToItem(indice) } }
                ) {
                    Text(
                        text = formato(valores[indice]),
                        style = if (esCentral) {
                            MaterialTheme.typography.headlineSmall
                        } else {
                            MaterialTheme.typography.titleMedium
                        },
                        color = if (esCentral) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

/** Índice del elemento más cercano al centro visible de la lista. */
private fun LazyListState.indiceCentral(): Int {
    val info = layoutInfo
    val visibles = info.visibleItemsInfo
    if (visibles.isEmpty()) return firstVisibleItemIndex
    val centro = (info.viewportStartOffset + info.viewportEndOffset) / 2
    return visibles.minBy { abs(it.offset + it.size / 2 - centro) }.index
}

@Preview(showBackground = true)
@Composable
private fun RuedaNumerosPreview() {
    CliniMedikTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            RuedaNumeros(
                valores = (0..23).toList(),
                seleccionado = 9,
                onSeleccionChange = {},
                descripcion = "Hora"
            )
            Text(text = ":", style = MaterialTheme.typography.headlineSmall)
            RuedaNumeros(
                valores = (0..59).toList(),
                seleccionado = 30,
                onSeleccionChange = {},
                descripcion = "Minutos"
            )
        }
    }
}
