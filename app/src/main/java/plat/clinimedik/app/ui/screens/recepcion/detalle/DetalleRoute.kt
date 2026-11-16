package plat.clinimedik.app.ui.screens.recepcion.detalle

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import java.io.File
import java.util.UUID
import plat.clinimedik.app.data.fake.FakeDataSource
import plat.clinimedik.app.data.model.TipoDocumento
import plat.clinimedik.app.data.model.VisitaConPaciente
import plat.clinimedik.app.ui.navigation.CodigoQr
import plat.clinimedik.app.ui.navigation.Detalle
import plat.clinimedik.app.ui.navigation.FichaPaciente
import plat.clinimedik.app.ui.navigation.NuevaCita
import plat.clinimedik.app.ui.navigation.VisorDocumento

fun detalleUiStateDePrueba(
    visitaId: String = "vis-102",
    panel: PanelDetalle? = null
): DetalleUiState {
    val visita = FakeDataSource.visitas.firstOrNull { it.id == visitaId } ?: return DetalleUiState(cargando = false)
    val paciente = FakeDataSource.paciente(visita.pacienteId) ?: return DetalleUiState(cargando = false)
    val historial = FakeDataSource.historialDe(paciente.id)
    return VisitaConPaciente(visita, paciente)
        .aDetalleUiState(
            documentos = FakeDataSource.documentosDe(paciente.id),
            historial = historial,
            recetas = historial.flatMap { FakeDataSource.recetasDe(it.id) },
            referencias = historial.mapNotNull { FakeDataSource.referenciaDe(it.id) },
            cobro = FakeDataSource.cobroDe(visita.id),
            proximaCita = FakeDataSource.proximaCitaDe(paciente.id),
            nombreMedico = FakeDataSource.medico(visita.medicoId)?.nombre.orEmpty(),
            hoy = FakeDataSource.hoy
        )
        .copy(panel = panel)
}

private fun crearArchivoDocumento(context: Context): File {
    val carpeta = File(context.filesDir, "documentos").apply { mkdirs() }
    return File(carpeta, "${UUID.randomUUID()}.jpg").apply { createNewFile() }
}

@Composable
fun DetalleRoute(
    onRegresar: () -> Unit,
    onAgendarSeguimiento: (pacienteId: String, motivo: String) -> Unit,
    onAbrirFicha: (pacienteId: String) -> Unit,
    onAbrirDocumento: (documentoId: String) -> Unit,
    onVerCodigoQr: (pacienteId: String) -> Unit,
    viewModel: DetalleViewModel = viewModel(factory = DetalleViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var tipoPendiente by rememberSaveable { mutableStateOf<String?>(null) }
    var rutaPendiente by rememberSaveable { mutableStateOf<String?>(null) }

    val lanzadorCamara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { tomada ->
        val ruta = rutaPendiente
        val tipo = TipoDocumento.entries.firstOrNull { it.name == tipoPendiente }
        tipoPendiente = null
        rutaPendiente = null
        if (ruta != null) {
            val archivo = File(ruta)
            if (tomada && tipo != null && archivo.length() > 0L) {
                viewModel.onEvent(DetalleEvent.DocumentoCapturado(tipo, ruta))
            } else {
                archivo.delete()
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.efectos.collect { efecto ->
            when (efecto) {
                DetalleEfecto.Regresar -> onRegresar()
                is DetalleEfecto.MostrarMensaje -> Toast.makeText(context, efecto.texto, Toast.LENGTH_SHORT).show()
                is DetalleEfecto.CompartirTexto -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, efecto.texto)
                    }
                    try {
                        context.startActivity(Intent.createChooser(intent, efecto.titulo))
                    } catch (_: ActivityNotFoundException) {
                        Toast.makeText(context, "No hay una aplicación para compartir", Toast.LENGTH_SHORT).show()
                    }
                }
                is DetalleEfecto.IrANuevaCita -> onAgendarSeguimiento(efecto.pacienteId, efecto.motivo)
                is DetalleEfecto.IrAFicha -> onAbrirFicha(efecto.pacienteId)
                is DetalleEfecto.IrAlVisor -> onAbrirDocumento(efecto.documentoId)
                is DetalleEfecto.IrACodigoQr -> onVerCodigoQr(efecto.pacienteId)
                is DetalleEfecto.AbrirCamara -> {
                    var archivo: File? = null
                    try {
                        val nuevo = crearArchivoDocumento(context)
                        archivo = nuevo
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            nuevo
                        )
                        tipoPendiente = efecto.tipo.name
                        rutaPendiente = nuevo.absolutePath
                        lanzadorCamara.launch(uri)
                    } catch (error: Exception) {
                        tipoPendiente = null
                        rutaPendiente = null
                        archivo?.delete()
                        viewModel.onEvent(DetalleEvent.CamaraNoDisponible)
                    }
                }
            }
        }
    }
    DetalleScreen(state = state, onEvent = viewModel::onEvent)
}

fun NavGraphBuilder.detalleDestino(navController: NavController) {
    composable<Detalle> {
        DetalleRoute(
            onRegresar = { navController.popBackStack() },
            onAgendarSeguimiento = { pacienteId, motivo ->
                navController.navigate(NuevaCita(pacienteId = pacienteId, motivo = motivo))
            },
            onAbrirFicha = { pacienteId ->
                navController.navigate(FichaPaciente(pacienteId))
            },
            onAbrirDocumento = { documentoId ->
                navController.navigate(VisorDocumento(documentoId))
            },
            onVerCodigoQr = { pacienteId ->
                navController.navigate(CodigoQr(pacienteId = pacienteId, desdeRegistro = false))
            }
        )
    }
}