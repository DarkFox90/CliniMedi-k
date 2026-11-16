package plat.clinimedik.app.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import plat.clinimedik.app.ui.theme.CliniMedikTheme

private const val LADO_PANTALLA_PX = 512

data class ColoresQr(val modulos: Int, val fondo: Int)

@Composable
fun coloresQrDelTema(): ColoresQr {
    val esquema = MaterialTheme.colorScheme
    val surfaceEsMasClaro = esquema.surface.luminance() > esquema.onSurface.luminance()
    val fondo = if (surfaceEsMasClaro) esquema.surface else esquema.onSurface
    val modulos = if (surfaceEsMasClaro) esquema.onSurface else esquema.surface
    return ColoresQr(modulos = modulos.toArgb(), fondo = fondo.toArgb())
}

fun generarBitmapQr(texto: String, lado: Int, colores: ColoresQr): Bitmap {
    val sugerencias = mapOf(
        EncodeHintType.CHARACTER_SET to "UTF-8",
        EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
        EncodeHintType.MARGIN to 4
    )
    val matriz = QRCodeWriter().encode(texto, BarcodeFormat.QR_CODE, lado, lado, sugerencias)
    val ancho = matriz.width
    val alto = matriz.height
    val pixeles = IntArray(ancho * alto)
    for (y in 0 until alto) {
        val inicioFila = y * ancho
        for (x in 0 until ancho) {
            pixeles[inicioFila + x] = if (matriz[x, y]) colores.modulos else colores.fondo
        }
    }
    return Bitmap.createBitmap(pixeles, ancho, alto, Bitmap.Config.ARGB_8888)
}

@Composable
fun CodigoQrImagen(
    texto: String,
    colores: ColoresQr,
    descripcion: String,
    modifier: Modifier = Modifier
) {
    val imagen = remember(texto, colores) {
        try {
            generarBitmapQr(texto, LADO_PANTALLA_PX, colores).asImageBitmap()
        } catch (error: Exception) {
            null
        }
    }

    if (imagen == null) {
        Text(
            text = "No se pudo generar el código",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            modifier = modifier
        )
    } else {
        Image(
            bitmap = imagen,
            contentDescription = descripcion,
            filterQuality = FilterQuality.None,
            modifier = modifier
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CodigoQrImagenPreview() {
    CliniMedikTheme {
        CodigoQrImagen(
            texto = "clinimedik:pac-001",
            colores = coloresQrDelTema(),
            descripcion = "Código QR de muestra",
            modifier = Modifier.size(240.dp)
        )
    }
}