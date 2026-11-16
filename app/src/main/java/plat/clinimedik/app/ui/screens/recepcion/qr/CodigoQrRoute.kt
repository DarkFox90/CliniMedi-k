package plat.clinimedik.app.ui.screens.recepcion.qr

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.print.PrintHelper
import plat.clinimedik.app.ui.components.LocalMensajes
import plat.clinimedik.app.ui.navigation.CodigoQr

@Composable
fun CodigoQrRoute(
    onRegresar: () -> Unit,
    viewModel: CodigoQrViewModel = viewModel(
        factory = CodigoQrViewModel.Factory
    )
) {
    val context = LocalContext.current
    val mensajes = LocalMensajes.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.efectos.collect { efecto ->
            when (efecto) {
                CodigoQrEfecto.Regresar -> onRegresar()

                is CodigoQrEfecto.MostrarMensaje -> mensajes.mostrar(efecto.texto)

                is CodigoQrEfecto.CompartirImagen -> {
                    try {
                        compartirImagen(context, efecto)
                    } catch (error: Exception) {
                        mensajes.mostrar("No se pudo compartir el código")
                    }
                }

                is CodigoQrEfecto.ImprimirImagen -> {
                    try {
                        PrintHelper(context).apply {
                            scaleMode = PrintHelper.SCALE_MODE_FIT
                        }.printBitmap(efecto.trabajo, efecto.imagen)
                    } catch (error: Exception) {
                        mensajes.mostrar("No se pudo abrir la impresión")
                    }
                }
            }
        }
    }

    CodigoQrScreen(
        state = state,
        onEvent = viewModel::onEvent
    )
}

private fun compartirImagen(context: Context, efecto: CodigoQrEfecto.CompartirImagen) {
    val envio = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, efecto.uri)
        putExtra(Intent.EXTRA_TEXT, efecto.texto)
        clipData = ClipData.newRawUri(null, efecto.uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    try {
        context.startActivity(Intent(envio).setPackage("com.whatsapp"))
    } catch (error: ActivityNotFoundException) {
        context.startActivity(Intent.createChooser(envio, "Compartir código"))
    }
}

fun NavGraphBuilder.codigoQrDestino(navController: NavController) {
    composable<CodigoQr> {
        CodigoQrRoute(
            onRegresar = { navController.popBackStack() }
        )
    }
}