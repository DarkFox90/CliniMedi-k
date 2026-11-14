package plat.clinimedik.app.ui.screens.recepcion.resumen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import plat.clinimedik.app.CliniMedikApp
import plat.clinimedik.app.data.preferencias.ConfiguracionRepositorio
import plat.clinimedik.app.data.repository.MedicoRepositorio
import plat.clinimedik.app.data.repository.NotificacionRepositorio
import plat.clinimedik.app.data.repository.VisitaRepositorio

class ResumenViewModel(
    private val visitaRepositorio: VisitaRepositorio,
    private val configuracionRepositorio: ConfiguracionRepositorio,
    private val medicoRepositorio: MedicoRepositorio,
    private val notificacionRepositorio: NotificacionRepositorio
) : ViewModel() {

    private val _state = MutableStateFlow(ResumenUiState())
    val state: StateFlow<ResumenUiState> = _state.asStateFlow()

    private val _efectos = Channel<ResumenEfecto>()
    val efectos: Flow<ResumenEfecto> = _efectos.receiveAsFlow()

    private val reintentos = MutableStateFlow(0)

    init {
        observarResumen()
    }

    fun onEvent(event: ResumenEvent) {
        when (event) {
            ResumenEvent.Regresar -> viewModelScope.launch { _efectos.send(ResumenEfecto.Regresar) }
            ResumenEvent.Reintentar -> {
                _state.update { it.copy(cargando = true, mensajeError = null) }
                reintentos.update { it + 1 }
            }
            ResumenEvent.EnviarResumen -> enviarResumen()
        }
    }

    /** El correo sale en segundo plano en cuanto haya internet; aquí solo se programa. */
    private fun enviarResumen() {
        viewModelScope.launch {
            val mensaje = try {
                val medicoId = configuracionRepositorio.configuracion.first()?.medicoId
                when {
                    medicoId == null -> "Configura el médico antes de enviar el resumen"
                    notificacionRepositorio.enviarResumen(medicoId) -> "El resumen se enviará al correo de la clínica"
                    else -> "Este teléfono no tiene configurado el envío de correos"
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                "No se pudo preparar el resumen. Intenta de nuevo."
            }
            _efectos.send(ResumenEfecto.MostrarMensaje(mensaje))
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observarResumen() {
        viewModelScope.launch {
            reintentos
                .flatMapLatest {
                    configuracionRepositorio.configuracion
                        .filterNotNull()
                        .flatMapLatest { configuracion ->
                            combine(
                                visitaRepositorio.observarFilaDelDia(configuracion.medicoId),
                                visitaRepositorio.observarCobrosDelDia(configuracion.medicoId),
                                medicoRepositorio.observarMedico(configuracion.medicoId)
                            ) { fila, cobros, medico ->
                                calcularResumen(fila, cobros, medico?.nombre.orEmpty(), LocalDate.now())
                            }
                        }
                        .catch {
                            emit(_state.value.copy(cargando = false, mensajeError = "No se pudo cargar el resumen"))
                        }
                }
                .collect { resumen -> _state.value = resumen }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as CliniMedikApp
                ResumenViewModel(
                    app.container.visitaRepositorio,
                    app.container.configuracionRepositorio,
                    app.container.medicoRepositorio,
                    app.container.notificacionRepositorio
                )
            }
        }
    }
}

sealed interface ResumenEfecto {
    data object Regresar : ResumenEfecto
    data class MostrarMensaje(val texto: String) : ResumenEfecto
}
