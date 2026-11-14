package plat.clinimedik.app.ui.screens.medico.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import java.time.LocalDateTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import plat.clinimedik.app.CliniMedikApp
import plat.clinimedik.app.data.preferencias.ConfiguracionRepositorio
import plat.clinimedik.app.data.repository.CitaRepositorio
import plat.clinimedik.app.data.repository.MedicoRepositorio
import plat.clinimedik.app.ui.components.SeleccionDia

class AgendaMedicoViewModel(
    private val citaRepositorio: CitaRepositorio,
    private val configuracionRepositorio: ConfiguracionRepositorio,
    private val medicoRepositorio: MedicoRepositorio
) : ViewModel() {

    private val _state = MutableStateFlow(AgendaMedicoUiState())
    val state: StateFlow<AgendaMedicoUiState> = _state.asStateFlow()

    private val seleccionDia = SeleccionDia()
    private val reintentos = MutableStateFlow(0)

    init {
        observarAgenda()
        citaRepositorio.actualizarConfirmaciones()
    }

    fun onEvent(event: AgendaMedicoEvent) {
        when (event) {
            AgendaMedicoEvent.DiaAnterior -> seleccionDia.anterior()
            AgendaMedicoEvent.DiaSiguiente -> seleccionDia.siguiente()
            is AgendaMedicoEvent.ElegirDia -> seleccionDia.elegir(event.fecha)
            AgendaMedicoEvent.VolverAHoy -> seleccionDia.volverAHoy()
            AgendaMedicoEvent.Reintentar -> {
                _state.update { it.copy(cargando = true, mensajeError = null) }
                reintentos.update { it + 1 }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observarAgenda() {
        val medicoId = configuracionRepositorio.configuracion
            .filterNotNull()
            .map { it.medicoId }
            .distinctUntilChanged()
        viewModelScope.launch {
            reintentos
                .flatMapLatest {
                    combine(medicoId, seleccionDia.dia) { id, dia -> id to dia }
                        .flatMapLatest { (id, dia) ->
                            combine(
                                citaRepositorio.observarCitasDelDia(id, dia.fecha),
                                medicoRepositorio.observarMedico(id)
                            ) { citas, medico ->
                                calcularAgendaMedico(citas, medico?.nombre.orEmpty(), LocalDateTime.now(), dia)
                            }
                        }
                        .catch {
                            emit(_state.value.copy(cargando = false, mensajeError = MENSAJE_ERROR_CARGA))
                        }
                }
                .collect { agenda -> _state.value = agenda }
        }
    }

    companion object {
        private const val MENSAJE_ERROR_CARGA = "No se pudo cargar la agenda"

        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as CliniMedikApp
                AgendaMedicoViewModel(
                    app.container.citaRepositorio,
                    app.container.configuracionRepositorio,
                    app.container.medicoRepositorio
                )
            }
        }
    }
}
