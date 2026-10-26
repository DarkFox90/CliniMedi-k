package plat.clinimedik.app.ui.screens.recepcion.resultado

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import java.time.LocalDate
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import plat.clinimedik.app.CliniMedikApp
import plat.clinimedik.app.data.preferencias.ConfiguracionRepositorio
import plat.clinimedik.app.data.repository.PacienteRepositorio
import plat.clinimedik.app.data.repository.VisitaRepositorio
import plat.clinimedik.app.ui.components.calcularEdad
import plat.clinimedik.app.ui.components.formatearFecha
import plat.clinimedik.app.ui.navigation.Resultado

class ResultadoViewModel(
    savedStateHandle: SavedStateHandle,
    private val pacienteRepositorio: PacienteRepositorio,
    private val visitaRepositorio: VisitaRepositorio,
    private val configuracionRepositorio: ConfiguracionRepositorio
) : ViewModel() {

    private val pacienteId = savedStateHandle.toRoute<Resultado>().pacienteId

    private val _state = MutableStateFlow(ResultadoUiState())
    val state: StateFlow<ResultadoUiState> = _state.asStateFlow()

    private val _efectos = Channel<ResultadoEfecto>()
    val efectos: Flow<ResultadoEfecto> = _efectos.receiveAsFlow()

    private var ultimaVisitaId: String? = null

    init {
        observarPaciente()
    }

    private fun observarPaciente() {
        viewModelScope.launch {
            combine(
                pacienteRepositorio.observarPaciente(pacienteId),
                visitaRepositorio.observarHistorial(pacienteId)
            ) { paciente, historial -> paciente to historial }
                .collect { (paciente, historial) ->
                    ultimaVisitaId = historial.firstOrNull()?.id

                    val resumen = if (historial.isEmpty()) {
                        "Paciente nuevo"
                    } else {
                        val fecha = historial.first().horaLlegada.toLocalDate()
                        "${historial.size} visitas · última: ${formatearFecha(fecha)}"
                    }

                    _state.update {
                        it.copy(
                            cargando = false,
                            nombre = paciente?.nombreCompleto.orEmpty(),
                            edad = paciente?.let { p ->
                                calcularEdad(p.fechaNacimiento, LocalDate.now()).toString()
                            }.orEmpty(),
                            telefono = paciente?.telefono.orEmpty(),
                            resumenVisitas = resumen,
                            tieneHistorial = historial.isNotEmpty(),
                            mensajeError = if (paciente == null) {
                                "No se encontró el paciente"
                            } else null
                        )
                    }
                }
        }
    }

    fun onEvent(event: ResultadoEvent) {
        when (event) {
            ResultadoEvent.VerHistorial -> {
                ultimaVisitaId?.let {
                    enviar(ResultadoEfecto.AbrirDetalle(it))
                }
            }

            ResultadoEvent.AgregarAFila ->
                _state.update {
                    it.copy(mostrarFormulario = true, mensajeError = null)
                }

            is ResultadoEvent.CambiarMotivo ->
                _state.update {
                    it.copy(motivo = event.texto, mensajeError = null)
                }

            ResultadoEvent.CancelarAgregarAFila ->
                _state.update {
                    it.copy(mostrarFormulario = false, motivo = "", mensajeError = null)
                }

            ResultadoEvent.ConfirmarAgregarAFila -> agregarAFila()

            ResultadoEvent.EscanearOtro,
            ResultadoEvent.Regresar -> enviar(ResultadoEfecto.Regresar)
        }
    }

    private fun agregarAFila() {
        val datos = _state.value
        if (datos.guardando) return

        if (datos.motivo.isBlank()) {
            _state.update {
                it.copy(mensajeError = "Ingresa el motivo de consulta")
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(guardando = true, mensajeError = null) }

            try {
                val configuracion = configuracionRepositorio.configuracion.first()
                    ?: throw IllegalStateException("No hay médico configurado")

                val visitaExistente = visitaRepositorio.visitaActivaDeHoy(
                    pacienteId,
                    configuracion.medicoId
                )

                val visitaId = visitaExistente?.id
                    ?: visitaRepositorio.agregarAFila(
                        pacienteId,
                        configuracion.medicoId,
                        datos.motivo
                    )

                _efectos.send(ResultadoEfecto.AbrirDetalle(visitaId))
            } catch (_: Exception) {
                _state.update {
                    it.copy(mensajeError = "No se pudo agregar el paciente a la fila")
                }
            } finally {
                _state.update { it.copy(guardando = false) }
            }
        }
    }

    private fun enviar(efecto: ResultadoEfecto) {
        viewModelScope.launch { _efectos.send(efecto) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[
                    ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY
                ] as CliniMedikApp

                ResultadoViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    pacienteRepositorio = app.container.pacienteRepositorio,
                    visitaRepositorio = app.container.visitaRepositorio,
                    configuracionRepositorio = app.container.configuracionRepositorio
                )
            }
        }
    }
}

sealed interface ResultadoEfecto {
    data object Regresar : ResultadoEfecto
    data class AbrirDetalle(val visitaId: String) : ResultadoEfecto
}
