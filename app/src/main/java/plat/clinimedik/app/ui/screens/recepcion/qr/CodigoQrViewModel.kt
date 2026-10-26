package plat.clinimedik.app.ui.screens.recepcion.qr

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import plat.clinimedik.app.CliniMedikApp
import plat.clinimedik.app.data.repository.PacienteRepositorio
import plat.clinimedik.app.ui.navigation.CodigoQr

class CodigoQrViewModel(
    savedStateHandle: SavedStateHandle,
    private val pacienteRepositorio: PacienteRepositorio
) : ViewModel() {

    private val pacienteId = savedStateHandle.toRoute<CodigoQr>().pacienteId

    private val _state = MutableStateFlow(CodigoQrUiState(pacienteId = pacienteId))
    val state: StateFlow<CodigoQrUiState> = _state.asStateFlow()

    private val _efectos = Channel<CodigoQrEfecto>()
    val efectos: Flow<CodigoQrEfecto> = _efectos.receiveAsFlow()

    init {
        observarPaciente()
    }

    private fun observarPaciente() {
        viewModelScope.launch {
            pacienteRepositorio.observarPaciente(pacienteId).collect { paciente ->
                _state.update {
                    it.copy(
                        cargando = false,
                        nombrePaciente = paciente?.nombreCompleto.orEmpty(),
                        mensajeError = if (paciente == null) {
                            "No se encontró el paciente"
                        } else null
                    )
                }
            }
        }
    }

    fun onEvent(event: CodigoQrEvent) {
        when (event) {
            CodigoQrEvent.Regresar -> enviar(CodigoQrEfecto.Regresar)

            CodigoQrEvent.Imprimir -> {
                enviar(
                    CodigoQrEfecto.MostrarMensaje(
                        "Disponible en la siguiente fase"
                    )
                )
            }

            CodigoQrEvent.EnviarPorWhatsApp -> {
                if (_state.value.nombrePaciente.isNotBlank()) {
                    enviar(
                        CodigoQrEfecto.CompartirCodigo(
                            "CliniMedi-k\n" +
                                    "Paciente: ${_state.value.nombrePaciente}\n" +
                                    "Código: clinimedik:$pacienteId"
                        )
                    )
                }
            }
        }
    }

    private fun enviar(efecto: CodigoQrEfecto) {
        viewModelScope.launch {
            _efectos.send(efecto)
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[
                    ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY
                ] as CliniMedikApp

                CodigoQrViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    pacienteRepositorio = app.container.pacienteRepositorio
                )
            }
        }
    }
}

sealed interface CodigoQrEfecto {
    data object Regresar : CodigoQrEfecto
    data class CompartirCodigo(val texto: String) : CodigoQrEfecto
    data class MostrarMensaje(val texto: String) : CodigoQrEfecto
}