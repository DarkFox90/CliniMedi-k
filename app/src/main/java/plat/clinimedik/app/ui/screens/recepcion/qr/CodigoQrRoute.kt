package plat.clinimedik.app.ui.screens.recepcion.qr

import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import plat.clinimedik.app.data.fake.FakeDataSource
import plat.clinimedik.app.ui.navigation.CodigoQr

@Composable
fun CodigoQrRoute(
    onRegresar: () -> Unit,
    viewModel: CodigoQrViewModel = viewModel(
        factory = CodigoQrViewModel.Factory
    )
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.efectos.collect { efecto ->
            when (efecto) {
                CodigoQrEfecto.Regresar -> onRegresar()

                is CodigoQrEfecto.MostrarMensaje -> {
                    Toast.makeText(
                        context,
                        efecto.texto,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is CodigoQrEfecto.CompartirCodigo -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, efecto.texto)
                    }

                    context.startActivity(
                        Intent.createChooser(intent, "Compartir código")
                    )
                }
            }
        }
    }

    CodigoQrScreen(
        state = state,
        onEvent = viewModel::onEvent
    )
}

fun NavGraphBuilder.codigoQrDestino(navController: NavController) {
    composable<CodigoQr> {
        CodigoQrRoute(
            onRegresar = { navController.popBackStack() }
        )
    }
}