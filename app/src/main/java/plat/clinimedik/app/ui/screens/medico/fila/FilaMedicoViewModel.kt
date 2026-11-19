package plat.clinimedik.app.ui.screens.medico.fila

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import java.time.LocalDateTime
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import plat.clinimedik.app.CliniMedikApp
import plat.clinimedik.app.data.preferencias.ConfiguracionRepositorio
import plat.clinimedik.app.data.repository.MedicoRepositorio
import plat.clinimedik.app.data.repository.SesionRepositorio
import plat.clinimedik.app.data.repository.VisitaRepositorio
import plat.clinimedik.app.ui.components.MenuSesionUiState

class FilaMedicoViewModel(
    private val visitaRepositorio: VisitaRepositorio,
    private val configuracionRepositorio: ConfiguracionRepositorio,
    private val medicoRepositorio: MedicoRepositorio,
    private val sesionRepositorio: SesionRepositorio
) : ViewModel() {

    private val _state = MutableStateFlow(FilaMedicoUiState())
    val state: StateFlow<FilaMedicoUiState> = _state.asStateFlow()

    private val _efectos = Channel<FilaMedicoEfecto>()
    val efectos: Flow<FilaMedicoEfecto> = _efectos.receiveAsFlow()

    init {
        observarFila()
    }

    fun onEvent(event: FilaMedicoEvent) {
        when (event) {
            is FilaMedicoEvent.AbrirHistorial -> enviar(FilaMedicoEfecto.AbrirHistorial(event.pacienteId))
            FilaMedicoEvent.AbrirMenu -> actualizarMenu { it.copy(menuAbierto = true) }
            FilaMedicoEvent.CerrarMenu -> actualizarMenu { it.copy(menuAbierto = false) }
            FilaMedicoEvent.CambiarConfiguracion -> {
                actualizarMenu { it.copy(menuAbierto = false) }
                enviar(FilaMedicoEfecto.IrARol)
            }
            FilaMedicoEvent.SolicitarCierreSesion -> actualizarMenu {
                it.copy(menuAbierto = false, confirmandoCierre = true)
            }
            FilaMedicoEvent.CancelarCierreSesion -> actualizarMenu { it.copy(confirmandoCierre = false) }
            FilaMedicoEvent.ConfirmarCierreSesion -> cerrarSesion()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observarFila() {
        viewModelScope.launch {
            configuracionRepositorio.configuracion
                .filterNotNull()
                .flatMapLatest { configuracion ->
                    combine(
                        visitaRepositorio.observarFilaDelDia(configuracion.medicoId),
                        medicoRepositorio.observarMedico(configuracion.medicoId)
                    ) { fila, medico ->
                        calcularFilaMedico(fila, medico?.nombre.orEmpty(), LocalDateTime.now())
                    }
                }
                .collect { fila ->
                    // Los datos nuevos no deben cerrar el menú ni el diálogo que el usuario tiene abiertos.
                    _state.update { actual -> fila.copy(menuSesion = actual.menuSesion) }
                }
        }
    }

    private fun cerrarSesion() {
        if (_state.value.menuSesion.cerrandoSesion) return
        actualizarMenu { it.copy(cerrandoSesion = true) }
        viewModelScope.launch {
            try {
                sesionRepositorio.cerrarSesion()
                _efectos.send(FilaMedicoEfecto.IrALogin)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                actualizarMenu { it.copy(confirmandoCierre = false, cerrandoSesion = false) }
                _efectos.send(FilaMedicoEfecto.MostrarMensaje(MENSAJE_ERROR_CIERRE))
            }
        }
    }

    private fun actualizarMenu(cambio: (MenuSesionUiState) -> MenuSesionUiState) {
        _state.update { it.copy(menuSesion = cambio(it.menuSesion)) }
    }

    private fun enviar(efecto: FilaMedicoEfecto) {
        viewModelScope.launch { _efectos.send(efecto) }
    }

    companion object {
        private const val MENSAJE_ERROR_CIERRE = "No se pudo cerrar la sesión. Intenta de nuevo."

        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as CliniMedikApp
                FilaMedicoViewModel(
                    app.container.visitaRepositorio,
                    app.container.configuracionRepositorio,
                    app.container.medicoRepositorio,
                    app.container.sesionRepositorio
                )
            }
        }
    }
}

sealed interface FilaMedicoEfecto {
    data class AbrirHistorial(val pacienteId: String) : FilaMedicoEfecto
    data object IrARol : FilaMedicoEfecto
    data object IrALogin : FilaMedicoEfecto
    data class MostrarMensaje(val texto: String) : FilaMedicoEfecto
}