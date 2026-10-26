package plat.clinimedik.app.ui.screens.medico.pacientes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import plat.clinimedik.app.CliniMedikApp
import plat.clinimedik.app.data.model.Paciente
import plat.clinimedik.app.data.preferencias.ConfiguracionRepositorio
import plat.clinimedik.app.data.repository.MedicoRepositorio
import plat.clinimedik.app.data.repository.PacienteRepositorio
import plat.clinimedik.app.data.repository.VisitaRepositorio

@OptIn(ExperimentalCoroutinesApi::class)
class PacientesMedicoViewModel(
    private val pacienteRepositorio: PacienteRepositorio,
    private val visitaRepositorio: VisitaRepositorio,
    private val configuracionRepositorio: ConfiguracionRepositorio,
    private val medicoRepositorio: MedicoRepositorio
) : ViewModel() {

    private val _state = MutableStateFlow(PacientesMedicoUiState())
    val state: StateFlow<PacientesMedicoUiState> = _state.asStateFlow()

    private val _efectos = Channel<PacientesMedicoEfecto>()
    val efectos: Flow<PacientesMedicoEfecto> = _efectos.receiveAsFlow()

    init {
        observarMedico()
        observarPacientes()
    }

    fun onEvent(event: PacientesMedicoEvent) {
        when (event) {
            is PacientesMedicoEvent.CambiarTexto -> _state.update { it.copy(texto = event.texto) }
            is PacientesMedicoEvent.AbrirHistorial -> viewModelScope.launch {
                _efectos.send(PacientesMedicoEfecto.AbrirHistorial(event.pacienteId))
            }
        }
    }

    private fun observarMedico() {
        viewModelScope.launch {
            configuracionRepositorio.configuracion
                .filterNotNull()
                .flatMapLatest { medicoRepositorio.observarMedico(it.medicoId) }
                .collect { medico ->
                    _state.update { it.copy(subtitulo = medico?.let { m -> "${m.nombre} · Todos los pacientes" }.orEmpty()) }
                }
        }
    }

    private fun observarPacientes() {
        val todos = configuracionRepositorio.configuracion
            .filterNotNull()
            .flatMapLatest { pacienteRepositorio.observarPacientesDelMedico(it.medicoId) }
            .flatMapLatest { pacientes -> conHistorial(pacientes) }
        val encontrados = _state
            .map { it.texto }
            .distinctUntilChanged()
            .flatMapLatest { texto ->
                if (texto.isBlank()) {
                    flowOf(null)
                } else {
                    pacienteRepositorio.buscar(texto).map { lista -> lista.map { it.id }.toSet() }
                }
            }
        viewModelScope.launch {
            combine(todos, encontrados) { lista, ids ->
                if (ids == null) lista else lista.filter { it.pacienteId in ids }
            }.collect { pacientes ->
                _state.update { it.copy(cargando = false, pacientes = pacientes) }
            }
        }
    }

    private fun conHistorial(pacientes: List<Paciente>): Flow<List<PacienteDelMedico>> {
        if (pacientes.isEmpty()) return flowOf(emptyList())
        val flujos = pacientes.map { paciente ->
            visitaRepositorio.observarHistorial(paciente.id).map { historial -> paciente.aPacienteDelMedico(historial) }
        }
        return combine(flujos) { lista -> lista.toList().ordenadosPorNombre() }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as CliniMedikApp
                PacientesMedicoViewModel(
                    app.container.pacienteRepositorio,
                    app.container.visitaRepositorio,
                    app.container.configuracionRepositorio,
                    app.container.medicoRepositorio
                )
            }
        }
    }
}

sealed interface PacientesMedicoEfecto {
    data class AbrirHistorial(val pacienteId: String) : PacientesMedicoEfecto
}