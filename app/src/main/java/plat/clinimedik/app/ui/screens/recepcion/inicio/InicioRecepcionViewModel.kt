package plat.clinimedik.app.ui.screens.recepcion.inicio

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
import plat.clinimedik.app.data.repository.CitaRepositorio
import plat.clinimedik.app.data.repository.MedicoRepositorio
import plat.clinimedik.app.data.repository.SesionRepositorio
import plat.clinimedik.app.data.repository.VisitaRepositorio
import plat.clinimedik.app.ui.components.MenuSesionUiState

class InicioRecepcionViewModel(
    private val visitaRepositorio: VisitaRepositorio,
    private val citaRepositorio: CitaRepositorio,
    private val medicoRepositorio: MedicoRepositorio,
    private val configuracionRepositorio: ConfiguracionRepositorio,
    private val sesionRepositorio: SesionRepositorio
) : ViewModel() {

    private val _state = MutableStateFlow(InicioRecepcionUiState())
    val state: StateFlow<InicioRecepcionUiState> = _state.asStateFlow()

    private val _efectos = Channel<InicioRecepcionEfecto>()
    val efectos: Flow<InicioRecepcionEfecto> = _efectos.receiveAsFlow()

    init {
        observarInicio()
    }

    fun onEvent(event: InicioRecepcionEvent) {
        when (event) {
            is InicioRecepcionEvent.AbrirPaciente -> enviar(InicioRecepcionEfecto.AbrirDetalle(event.visitaId))
            InicioRecepcionEvent.NuevoPaciente -> enviar(InicioRecepcionEfecto.IrARegistro)
            InicioRecepcionEvent.EscanearQr -> enviar(InicioRecepcionEfecto.IrAEscaneo)
            InicioRecepcionEvent.VerResumen -> enviar(InicioRecepcionEfecto.IrAResumen)
            is InicioRecepcionEvent.AbrirCita -> enviar(InicioRecepcionEfecto.IrAAgenda)
            InicioRecepcionEvent.AbrirMenu -> actualizarMenu { it.copy(menuAbierto = true) }
            InicioRecepcionEvent.CerrarMenu -> actualizarMenu { it.copy(menuAbierto = false) }
            InicioRecepcionEvent.CambiarConfiguracion -> {
                actualizarMenu { it.copy(menuAbierto = false) }
                enviar(InicioRecepcionEfecto.IrARol)
            }
            InicioRecepcionEvent.SolicitarCierreSesion -> actualizarMenu {
                it.copy(menuAbierto = false, confirmandoCierre = true)
            }
            InicioRecepcionEvent.CancelarCierreSesion -> actualizarMenu { it.copy(confirmandoCierre = false) }
            InicioRecepcionEvent.ConfirmarCierreSesion -> cerrarSesion()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observarInicio() {
        viewModelScope.launch {
            configuracionRepositorio.configuracion
                .filterNotNull()
                .flatMapLatest { configuracion ->
                    combine(
                        visitaRepositorio.observarFilaDelDia(configuracion.medicoId),
                        citaRepositorio.observarCitasDelDia(configuracion.medicoId),
                        medicoRepositorio.observarMedico(configuracion.medicoId)
                    ) { fila, citas, medico ->
                        calcularInicio(fila, citas, medico?.nombre.orEmpty(), LocalDateTime.now())
                    }
                }
                .collect { inicio ->
                    // Los datos nuevos no deben cerrar el menú ni el diálogo que el usuario tiene abiertos.
                    _state.update { actual -> inicio.copy(menuSesion = actual.menuSesion) }
                }
        }
    }

    private fun cerrarSesion() {
        if (_state.value.menuSesion.cerrandoSesion) return
        actualizarMenu { it.copy(cerrandoSesion = true) }
        viewModelScope.launch {
            try {
                sesionRepositorio.cerrarSesion()
                _efectos.send(InicioRecepcionEfecto.IrALogin)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                actualizarMenu { it.copy(confirmandoCierre = false, cerrandoSesion = false) }
                _efectos.send(InicioRecepcionEfecto.MostrarMensaje(MENSAJE_ERROR_CIERRE))
            }
        }
    }

    private fun actualizarMenu(cambio: (MenuSesionUiState) -> MenuSesionUiState) {
        _state.update { it.copy(menuSesion = cambio(it.menuSesion)) }
    }

    private fun enviar(efecto: InicioRecepcionEfecto) {
        viewModelScope.launch { _efectos.send(efecto) }
    }

    companion object {
        private const val MENSAJE_ERROR_CIERRE = "No se pudo cerrar la sesión. Intenta de nuevo."

        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as CliniMedikApp
                InicioRecepcionViewModel(
                    app.container.visitaRepositorio,
                    app.container.citaRepositorio,
                    app.container.medicoRepositorio,
                    app.container.configuracionRepositorio,
                    app.container.sesionRepositorio
                )
            }
        }
    }
}

sealed interface InicioRecepcionEfecto {
    data class AbrirDetalle(val visitaId: String) : InicioRecepcionEfecto
    data object IrARegistro : InicioRecepcionEfecto
    data object IrAEscaneo : InicioRecepcionEfecto
    data object IrAResumen : InicioRecepcionEfecto
    data object IrAAgenda : InicioRecepcionEfecto
    data object IrARol : InicioRecepcionEfecto
    data object IrALogin : InicioRecepcionEfecto
    data class MostrarMensaje(val texto: String) : InicioRecepcionEfecto
}