package plat.clinimedik.app.ui.screens.recepcion.qr

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import plat.clinimedik.app.CliniMedikApp
import plat.clinimedik.app.data.repository.PacienteRepositorio
import plat.clinimedik.app.ui.components.ColoresQr
import plat.clinimedik.app.ui.components.generarBitmapQr
import plat.clinimedik.app.ui.navigation.CodigoQr

class CodigoQrViewModel(
    savedStateHandle: SavedStateHandle,
    private val application: Application,
    private val pacienteRepositorio: PacienteRepositorio
) : ViewModel() {

    private val pacienteId = savedStateHandle.toRoute<CodigoQr>().pacienteId

    private val _state = MutableStateFlow(CodigoQrUiState(pacienteId = pacienteId))
    val state: StateFlow<CodigoQrUiState> = _state.asStateFlow()

    private val _efectos = Channel<CodigoQrEfecto>()
    val efectos: Flow<CodigoQrEfecto> = _efectos.receiveAsFlow()

    private var trabajoCarga: Job? = null

    init {
        observarPaciente()
    }

    private fun observarPaciente() {
        trabajoCarga?.cancel()
        _state.update {
            it.copy(cargando = true, mensajeError = null, puedeReintentar = false)
        }
        trabajoCarga = viewModelScope.launch {
            pacienteRepositorio.observarPaciente(pacienteId)
                .catch {
                    _state.update {
                        it.copy(
                            cargando = false,
                            mensajeError = "No se pudo cargar el código del paciente",
                            puedeReintentar = true
                        )
                    }
                }
                .collect { paciente ->
                    _state.update {
                        it.copy(
                            cargando = false,
                            nombrePaciente = paciente?.nombreCompleto.orEmpty(),
                            mensajeError = if (paciente == null) {
                                "No se encontró el paciente"
                            } else null,
                            puedeReintentar = false
                        )
                    }
                }
        }
    }

    fun onEvent(event: CodigoQrEvent) {
        when (event) {
            CodigoQrEvent.Regresar -> enviar(CodigoQrEfecto.Regresar)

            CodigoQrEvent.Reintentar -> observarPaciente()

            is CodigoQrEvent.Imprimir -> ejecutarAccion(
                mensajeError = "No se pudo preparar el código para imprimir"
            ) { estado ->
                val imagen = withContext(Dispatchers.IO) {
                    generarBitmapQr(estado.contenidoQr, LADO_ARCHIVO_PX, event.colores)
                }
                CodigoQrEfecto.ImprimirImagen(
                    imagen = imagen,
                    trabajo = "Código QR - ${estado.nombrePaciente}"
                )
            }

            is CodigoQrEvent.EnviarPorWhatsApp -> ejecutarAccion(
                mensajeError = "No se pudo preparar el código para compartir"
            ) { estado ->
                val uri = withContext(Dispatchers.IO) {
                    guardarPng(estado.contenidoQr, event.colores)
                }
                CodigoQrEfecto.CompartirImagen(
                    uri = uri,
                    texto = "CliniMedi-k\n" +
                            "Paciente: ${estado.nombrePaciente}\n" +
                            "Código: ${estado.contenidoQr}"
                )
            }
        }
    }

    private fun ejecutarAccion(
        mensajeError: String,
        preparar: suspend (CodigoQrUiState) -> CodigoQrEfecto
    ) {
        val actual = _state.value
        if (actual.guardando || actual.nombrePaciente.isBlank()) return

        _state.update { it.copy(guardando = true) }
        viewModelScope.launch {
            try {
                _efectos.send(preparar(actual))
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _efectos.send(CodigoQrEfecto.MostrarMensaje(mensajeError))
            } finally {
                _state.update { it.copy(guardando = false) }
            }
        }
    }

    private fun guardarPng(contenido: String, colores: ColoresQr): Uri {
        val imagen = generarBitmapQr(contenido, LADO_ARCHIVO_PX, colores)
        val carpeta = File(application.cacheDir, "qr").apply { mkdirs() }
        val archivo = File(carpeta, "$pacienteId.png")
        val guardado = archivo.outputStream().use { salida ->
            imagen.compress(Bitmap.CompressFormat.PNG, 100, salida)
        }
        if (!guardado) throw IOException("No se pudo guardar la imagen del código")
        return FileProvider.getUriForFile(
            application,
            "${application.packageName}.fileprovider",
            archivo
        )
    }

    private fun enviar(efecto: CodigoQrEfecto) {
        viewModelScope.launch {
            _efectos.send(efecto)
        }
    }

    companion object {
        private const val LADO_ARCHIVO_PX = 1024

        val Factory = viewModelFactory {
            initializer {
                val app = this[
                    ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY
                ] as CliniMedikApp

                CodigoQrViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    application = app,
                    pacienteRepositorio = app.container.pacienteRepositorio
                )
            }
        }
    }
}

sealed interface CodigoQrEfecto {
    data object Regresar : CodigoQrEfecto
    data class CompartirImagen(val uri: Uri, val texto: String) : CodigoQrEfecto
    data class ImprimirImagen(val imagen: Bitmap, val trabajo: String) : CodigoQrEfecto
    data class MostrarMensaje(val texto: String) : CodigoQrEfecto
}