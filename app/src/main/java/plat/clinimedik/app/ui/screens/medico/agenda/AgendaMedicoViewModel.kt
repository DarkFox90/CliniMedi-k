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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import plat.clinimedik.app.CliniMedikApp
import plat.clinimedik.app.data.preferencias.ConfiguracionRepositorio
import plat.clinimedik.app.data.repository.CitaRepositorio
import plat.clinimedik.app.data.repository.MedicoRepositorio

class AgendaMedicoViewModel(
    private val citaRepositorio: CitaRepositorio,
    private val configuracionRepositorio: ConfiguracionRepositorio,
    private val medicoRepositorio: MedicoRepositorio
) : ViewModel() {

    private val _state = MutableStateFlow(AgendaMedicoUiState())
    val state: StateFlow<AgendaMedicoUiState> = _state.asStateFlow()

    init {
        observarAgenda()
    }

    fun onEvent(event: AgendaMedicoEvent) = Unit

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observarAgenda() {
        viewModelScope.launch {
            configuracionRepositorio.configuracion
                .filterNotNull()
                .flatMapLatest { configuracion ->
                    combine(
                        citaRepositorio.observarCitasDelDia(configuracion.medicoId),
                        medicoRepositorio.observarMedico(configuracion.medicoId)
                    ) { citas, medico ->
                        calcularAgendaMedico(citas, medico?.nombre.orEmpty(), LocalDateTime.now())
                    }
                }
                .collect { agenda -> _state.value = agenda }
        }
    }

    companion object {
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