package plat.clinimedik.app.ui.screens.recepcion.escaneo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import plat.clinimedik.app.CliniMedikApp
import plat.clinimedik.app.data.repository.PacienteRepositorio
import plat.clinimedik.app.ui.screens.recepcion.qr.FormatoQr

class EscaneoViewModel(
    private val pacienteRepositorio: PacienteRepositorio
) : ViewModel() {

    private val _state = MutableStateFlow(EscaneoUiState())
    val state: StateFlow<EscaneoUiState> = _state.asStateFlow()

    private val _efectos = Channel<EscaneoEfecto>()
    val efectos: Flow<EscaneoEfecto> = _efectos.receiveAsFlow()

    fun onEvent(event: EscaneoEvent) {
        when (event) {
            EscaneoEvent.Regresar -> enviar(EscaneoEfecto.Regresar)
            EscaneoEvent.EscanearConCamara -> iniciarLectura()
            EscaneoEvent.SimularLectura -> simularLectura()
            is EscaneoEvent.CodigoLeido -> resolverCodigo(event.texto)
            EscaneoEvent.LecturaCancelada -> _state.update { it.copy(leyendo = false) }
            EscaneoEvent.LectorNoDisponible -> {
                _state.update { it.copy(leyendo = false) }
                enviar(EscaneoEfecto.MostrarMensaje(MENSAJE_SIN_LECTOR))
            }
        }
    }

    private fun iniciarLectura() {
        if (_state.value.leyendo) return
        _state.update { it.copy(leyendo = true) }
        enviar(EscaneoEfecto.AbrirLector)
    }

    private fun simularLectura() {
        if (_state.value.leyendo) return
        _state.update { it.copy(leyendo = true) }
        resolverCodigo(FormatoQr.contenidoDe(PACIENTE_DE_PRUEBA))
    }

    private fun resolverCodigo(texto: String) {
        viewModelScope.launch {
            try {
                val pacienteId = FormatoQr.pacienteIdDe(texto)
                if (pacienteId == null) {
                    _efectos.send(EscaneoEfecto.MostrarMensaje(MENSAJE_CODIGO_NO_RECONOCIDO))
                    return@launch
                }
                val paciente = pacienteRepositorio.observarPaciente(pacienteId).first()
                if (paciente == null) {
                    _efectos.send(EscaneoEfecto.MostrarMensaje(MENSAJE_PACIENTE_NO_ENCONTRADO))
                } else {
                    _efectos.send(EscaneoEfecto.PacienteEncontrado(pacienteId))
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _efectos.send(EscaneoEfecto.MostrarMensaje(MENSAJE_ERROR_BUSCAR))
            } finally {
                _state.update { it.copy(leyendo = false) }
            }
        }
    }

    private fun enviar(efecto: EscaneoEfecto) {
        viewModelScope.launch {
            _efectos.send(efecto)
        }
    }

    companion object {
        private const val PACIENTE_DE_PRUEBA = "pac-001"
        private const val MENSAJE_CODIGO_NO_RECONOCIDO = "Código no reconocido"
        private const val MENSAJE_PACIENTE_NO_ENCONTRADO = "Paciente no encontrado"
        private const val MENSAJE_SIN_LECTOR = "Lector no disponible en este dispositivo"
        private const val MENSAJE_ERROR_BUSCAR = "No se pudo buscar al paciente"

        val Factory = viewModelFactory {
            initializer {
                val app = this[
                    ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY
                ] as CliniMedikApp

                EscaneoViewModel(
                    pacienteRepositorio = app.container.pacienteRepositorio
                )
            }
        }
    }
}

sealed interface EscaneoEfecto {
    data object Regresar : EscaneoEfecto
    data object AbrirLector : EscaneoEfecto
    data class PacienteEncontrado(val pacienteId: String) : EscaneoEfecto
    data class MostrarMensaje(val texto: String) : EscaneoEfecto
}