package plat.clinimedik.app.ui.screens.recepcion.escaneo

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import plat.clinimedik.app.ui.components.LocalMensajes
import plat.clinimedik.app.ui.navigation.Escaneo
import plat.clinimedik.app.ui.navigation.Resultado

@Composable
fun EscaneoRoute(
    onRegresar: () -> Unit,
    onPacienteEncontrado: (String) -> Unit,
    viewModel: EscaneoViewModel = viewModel(
        factory = EscaneoViewModel.Factory
    )
) {
    val context = LocalContext.current
    val mensajes = LocalMensajes.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.efectos.collect { efecto ->
            when (efecto) {
                EscaneoEfecto.Regresar -> onRegresar()

                EscaneoEfecto.AbrirLector -> abrirLector(context, viewModel::onEvent)

                is EscaneoEfecto.PacienteEncontrado -> {
                    onPacienteEncontrado(efecto.pacienteId)
                }

                is EscaneoEfecto.MostrarMensaje -> mensajes.mostrar(efecto.texto)
            }
        }
    }

    EscaneoScreen(
        state = state,
        onEvent = viewModel::onEvent
    )
}

private fun abrirLector(context: Context, onEvent: (EscaneoEvent) -> Unit) {
    try {
        val opciones = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()

        GmsBarcodeScanning.getClient(context, opciones)
            .startScan()
            .addOnSuccessListener { codigo ->
                onEvent(EscaneoEvent.CodigoLeido(codigo.rawValue.orEmpty()))
            }
            .addOnCanceledListener {
                onEvent(EscaneoEvent.LecturaCancelada)
            }
            .addOnFailureListener {
                onEvent(EscaneoEvent.LectorNoDisponible)
            }
    } catch (error: Exception) {
        onEvent(EscaneoEvent.LectorNoDisponible)
    }
}

fun NavGraphBuilder.escaneoDestino(navController: NavController) {
    composable<Escaneo> {
        EscaneoRoute(
            onRegresar = {
                navController.popBackStack()
            },
            onPacienteEncontrado = { pacienteId ->
                navController.navigate(Resultado(pacienteId))
            }
        )
    }
}