package plat.clinimedik.app.ui.screens.recepcion.visor

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import java.io.File
import java.io.IOException
import kotlin.math.max
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import plat.clinimedik.app.CliniMedikApp
import plat.clinimedik.app.data.model.Documento
import plat.clinimedik.app.data.repository.PacienteRepositorio
import plat.clinimedik.app.ui.components.etiqueta
import plat.clinimedik.app.ui.components.formatearFecha
import plat.clinimedik.app.ui.components.formatearHora
import plat.clinimedik.app.ui.navigation.VisorDocumento

private const val LADO_MAXIMO_PX = 2048

private class DocumentoCargado(
    val tipo: String,
    val fecha: String,
    val imagen: ImageBitmap?
)

class VisorDocumentoViewModel(
    savedStateHandle: SavedStateHandle,
    private val pacienteRepositorio: PacienteRepositorio
) : ViewModel() {

    private val documentoId = savedStateHandle.toRoute<VisorDocumento>().documentoId

    private val _state = MutableStateFlow(VisorDocumentoUiState())
    val state: StateFlow<VisorDocumentoUiState> = _state.asStateFlow()

    private val _efectos = Channel<VisorDocumentoEfecto>()
    val efectos: Flow<VisorDocumentoEfecto> = _efectos.receiveAsFlow()

    private var trabajoCarga: Job? = null

    init {
        cargar()
    }

    fun onEvent(event: VisorDocumentoEvent) {
        when (event) {
            VisorDocumentoEvent.Regresar -> {
                viewModelScope.launch { _efectos.send(VisorDocumentoEfecto.Regresar) }
            }
            VisorDocumentoEvent.Reintentar -> cargar()
        }
    }

    private fun cargar() {
        trabajoCarga?.cancel()
        _state.update {
            it.copy(cargando = true, mensajeError = null, puedeReintentar = false, imagen = null)
        }
        trabajoCarga = viewModelScope.launch {
            pacienteRepositorio.observarDocumento(documentoId)
                .map { documento -> documento?.let { cargarDocumento(it) } }
                .catch {
                    _state.update {
                        it.copy(
                            cargando = false,
                            mensajeError = "No se pudo cargar el documento",
                            puedeReintentar = true
                        )
                    }
                }
                .collect { cargado ->
                    _state.update { actual ->
                        when {
                            cargado == null -> actual.copy(
                                cargando = false,
                                mensajeError = "No se encontró el documento",
                                puedeReintentar = false,
                                imagen = null
                            )
                            cargado.imagen == null -> actual.copy(
                                cargando = false,
                                tipo = cargado.tipo,
                                fecha = cargado.fecha,
                                mensajeError = "La imagen de este documento no está disponible",
                                puedeReintentar = false,
                                imagen = null
                            )
                            else -> actual.copy(
                                cargando = false,
                                tipo = cargado.tipo,
                                fecha = cargado.fecha,
                                mensajeError = null,
                                puedeReintentar = false,
                                imagen = cargado.imagen
                            )
                        }
                    }
                }
        }
    }

    private suspend fun cargarDocumento(documento: Documento): DocumentoCargado =
        withContext(Dispatchers.IO) {
            DocumentoCargado(
                tipo = documento.tipo.etiqueta(),
                fecha = "${formatearFecha(documento.fecha.toLocalDate())} · ${formatearHora(documento.fecha)}",
                imagen = decodificarImagen(documento.rutaArchivo)
            )
        }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[
                    ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY
                ] as CliniMedikApp

                VisorDocumentoViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    pacienteRepositorio = app.container.pacienteRepositorio
                )
            }
        }
    }
}

sealed interface VisorDocumentoEfecto {
    data object Regresar : VisorDocumentoEfecto
}

private fun decodificarImagen(ruta: String): ImageBitmap? {
    if (ruta.isBlank()) return null
    val archivo = File(ruta)
    if (!archivo.isFile || archivo.length() == 0L) return null

    val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(ruta, limites)
    if (limites.outWidth <= 0 || limites.outHeight <= 0) return null

    var muestreo = 1
    while (max(limites.outWidth, limites.outHeight) / muestreo > LADO_MAXIMO_PX) {
        muestreo *= 2
    }

    val opciones = BitmapFactory.Options().apply { inSampleSize = muestreo }
    val original = BitmapFactory.decodeFile(ruta, opciones) ?: return null

    val grados = leerRotacion(ruta)
    if (grados == 0f) return original.asImageBitmap()

    val matriz = Matrix().apply { postRotate(grados) }
    val rotada = Bitmap.createBitmap(original, 0, 0, original.width, original.height, matriz, true)
    if (rotada !== original) original.recycle()
    return rotada.asImageBitmap()
}

private fun leerRotacion(ruta: String): Float {
    return try {
        when (
            ExifInterface(ruta).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        ) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
    } catch (error: IOException) {
        0f
    }
}