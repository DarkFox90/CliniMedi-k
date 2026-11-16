package plat.clinimedik.app.ui.screens.recepcion.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import plat.clinimedik.app.CliniMedikApp
import plat.clinimedik.app.data.model.EstadoCita
import plat.clinimedik.app.data.model.EstadoConfirmacion
import plat.clinimedik.app.data.preferencias.ConfiguracionRepositorio
import plat.clinimedik.app.data.repository.CitaRepositorio
import plat.clinimedik.app.ui.components.SeleccionDia

private val formatoFechaCampo: DateTimeFormatter = DateTimeFormatter
    .ofPattern("dd/MM/uuuu")
    .withResolverStyle(ResolverStyle.STRICT)

private val formatoHoraCampo: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

class AgendaRecepcionViewModel(
    private val citaRepositorio: CitaRepositorio,
    private val configuracionRepositorio: ConfiguracionRepositorio
) : ViewModel() {

    private val _state = MutableStateFlow(AgendaRecepcionUiState())
    val state: StateFlow<AgendaRecepcionUiState> = _state.asStateFlow()

    private val _efectos = Channel<AgendaRecepcionEfecto>()
    val efectos: Flow<AgendaRecepcionEfecto> = _efectos.receiveAsFlow()

    private val seleccionDia = SeleccionDia()
    private val reintentos = MutableStateFlow(0)

    init {
        observarCitas()
        citaRepositorio.actualizarConfirmaciones()
    }

    fun onEvent(event: AgendaRecepcionEvent) {
        when (event) {
            AgendaRecepcionEvent.NuevaCita -> enviar(AgendaRecepcionEfecto.IrANuevaCita)
            AgendaRecepcionEvent.DiaAnterior -> seleccionDia.anterior()
            AgendaRecepcionEvent.DiaSiguiente -> seleccionDia.siguiente()
            is AgendaRecepcionEvent.ElegirDia -> seleccionDia.elegir(event.fecha)
            AgendaRecepcionEvent.VolverAHoy -> seleccionDia.volverAHoy()
            AgendaRecepcionEvent.Reintentar -> {
                _state.update { it.copy(cargando = true, mensajeError = null) }
                reintentos.update { it + 1 }
            }

            is AgendaRecepcionEvent.AbrirCita -> abrirCita(event.citaId)
            AgendaRecepcionEvent.CerrarPanel -> cerrarPanel()

            is AgendaRecepcionEvent.MarcarConfirmacion -> marcarConfirmacion(event.estado)

            AgendaRecepcionEvent.AbrirReprogramar -> abrirReprogramar()
            is AgendaRecepcionEvent.CambiarFechaReprogramar ->
                actualizarReprogramar { it.copy(fecha = event.texto, errorFecha = null, errorHora = null) }
            is AgendaRecepcionEvent.CambiarHoraReprogramar ->
                actualizarReprogramar { it.copy(hora = event.texto, errorHora = null) }
            AgendaRecepcionEvent.ConfirmarReprogramar -> reprogramar()

            AgendaRecepcionEvent.SolicitarCancelacion -> solicitarCancelacion()
            AgendaRecepcionEvent.ConfirmarCancelacion -> cancelar()

            AgendaRecepcionEvent.RegistrarLlegada -> registrarLlegada()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observarCitas() {
        val medico = configuracionRepositorio.configuracion
            .filterNotNull()
            .map { it.medicoId }
            .distinctUntilChanged()
        viewModelScope.launch {
            reintentos
                .flatMapLatest {
                    combine(medico, seleccionDia.dia) { medicoId, dia -> medicoId to dia }
                        .flatMapLatest { (medicoId, dia) ->
                            citaRepositorio.observarCitasDelDia(medicoId, dia.fecha)
                                .map { citas -> calcularAgendaRecepcion(citas, dia) }
                        }
                        .catch {
                            emit(_state.value.copy(cargando = false, mensajeError = MENSAJE_ERROR_CARGA))
                        }
                }
                .collect { nuevo ->
                    _state.update { actual -> conservarPanel(nuevo, actual) }
                }
        }
    }

    private fun conservarPanel(
        nuevo: AgendaRecepcionUiState,
        actual: AgendaRecepcionUiState
    ): AgendaRecepcionUiState {
        val panel = actual.panel?.takeIf { abierto ->
            nuevo.elementos.any { it.citaId == abierto.citaId && it.admiteAcciones }
        }
        return nuevo.copy(panel = panel, guardando = actual.guardando)
    }

    private fun elementoDe(citaId: String): ElementoCita? =
        _state.value.elementos.firstOrNull { it.citaId == citaId }

    private fun abrirCita(citaId: String) {
        val actual = _state.value
        if (actual.guardando || actual.panel != null) return
        val elemento = elementoDe(citaId) ?: return
        if (!elemento.admiteAcciones) return
        _state.update { it.copy(panel = PanelAgenda.Acciones(citaId)) }
    }

    private fun cerrarPanel() {
        if (_state.value.guardando) return
        _state.update { it.copy(panel = null) }
    }

    private fun marcarConfirmacion(estado: EstadoConfirmacion) {
        val panel = _state.value.panel as? PanelAgenda.Acciones ?: return
        val mensaje = if (estado == EstadoConfirmacion.CONFIRMADA) {
            "Cita marcada como confirmada"
        } else {
            "Cita marcada como no confirmada"
        }
        ejecutar {
            citaRepositorio.marcarConfirmacion(panel.citaId, estado)
            cerrarPanelYAvisar(mensaje)
        }
    }

    private fun abrirReprogramar() {
        val panel = _state.value.panel as? PanelAgenda.Acciones ?: return
        val elemento = elementoDe(panel.citaId) ?: return
        val fechaActual = elemento.fechaHora.toLocalDate()
        val formulario = FormularioReprogramar(
            fecha = if (fechaActual.isBefore(LocalDate.now())) "" else fechaActual.format(formatoFechaCampo),
            hora = elemento.fechaHora.format(formatoHoraCampo)
        )
        _state.update {
            it.copy(panel = PanelAgenda.Reprogramar(citaId = panel.citaId, formulario = formulario))
        }
    }

    private fun actualizarReprogramar(cambio: (FormularioReprogramar) -> FormularioReprogramar) {
        _state.update { actual ->
            val panel = actual.panel as? PanelAgenda.Reprogramar ?: return@update actual
            actual.copy(panel = panel.copy(formulario = cambio(panel.formulario)))
        }
    }

    private fun reprogramar() {
        val actual = _state.value
        if (actual.guardando) return
        val panel = actual.panel as? PanelAgenda.Reprogramar ?: return
        val elemento = elementoDe(panel.citaId) ?: return
        val formulario = panel.formulario

        val fecha = leerFecha(formulario.fecha)
        val hora = leerHora(formulario.hora)
        val nueva = if (fecha != null && hora != null) LocalDateTime.of(fecha, hora) else null
        val ahora = LocalDateTime.now()

        val errorFecha = when {
            formulario.fecha.isBlank() -> "Elige la nueva fecha"
            fecha == null -> "Ingresa una fecha válida"
            fecha.isBefore(ahora.toLocalDate()) -> "La fecha no puede ser anterior a hoy"
            else -> null
        }
        val errorHora = when {
            formulario.hora.isBlank() -> "Elige la nueva hora"
            hora == null -> "Ingresa una hora válida"
            errorFecha == null && nueva != null && nueva.isBefore(ahora) -> "La hora ya pasó"
            nueva != null && nueva == elemento.fechaHora -> "Elige un horario distinto al actual"
            else -> null
        }

        if (errorFecha != null || errorHora != null || nueva == null) {
            actualizarReprogramar { it.copy(errorFecha = errorFecha, errorHora = errorHora) }
            return
        }

        ejecutar {
            val conflicto = citaRepositorio.reprogramar(panel.citaId, nueva)
            if (conflicto != null) {
                actualizarReprogramar {
                    it.copy(errorHora = "Horario ocupado por ${conflicto.nombrePaciente}")
                }
            } else {
                val guardada = citaRepositorio.obtenerCita(panel.citaId)
                val reprogramada = guardada != null &&
                        guardada.estado == EstadoCita.PROGRAMADA &&
                        guardada.fechaHora == nueva
                if (reprogramada) {
                    cerrarPanelYAvisar("Cita reprogramada")
                } else {
                    _efectos.send(AgendaRecepcionEfecto.MostrarMensaje(MENSAJE_NO_REPROGRAMADA))
                }
            }
        }
    }

    private fun solicitarCancelacion() {
        val panel = _state.value.panel as? PanelAgenda.Acciones ?: return
        _state.update { it.copy(panel = PanelAgenda.ConfirmarCancelacion(panel.citaId)) }
    }

    private fun cancelar() {
        val panel = _state.value.panel as? PanelAgenda.ConfirmarCancelacion ?: return
        ejecutar {
            citaRepositorio.cancelar(panel.citaId)
            cerrarPanelYAvisar("Cita cancelada")
        }
    }

    private fun registrarLlegada() {
        val actual = _state.value
        if (actual.guardando) return
        val panel = actual.panel as? PanelAgenda.Acciones ?: return
        val elemento = elementoDe(panel.citaId) ?: return

        if (elemento.fechaHora.toLocalDate() != LocalDate.now()) {
            enviar(AgendaRecepcionEfecto.MostrarMensaje(MENSAJE_LLEGADA_OTRO_DIA))
            return
        }

        if (!elemento.tienePaciente) {
            _state.update { it.copy(panel = null) }
            enviar(AgendaRecepcionEfecto.AbrirCuestionario(elemento.citaId))
            return
        }

        ejecutar {
            val visitaId = citaRepositorio.registrarLlegada(panel.citaId)
            if (visitaId == null) {
                _efectos.send(AgendaRecepcionEfecto.MostrarMensaje(MENSAJE_ERROR_LLEGADA))
            } else {
                _state.update { it.copy(panel = null) }
                _efectos.send(AgendaRecepcionEfecto.AbrirDetalle(visitaId))
            }
        }
    }

    private fun ejecutar(accion: suspend () -> Unit) {
        if (_state.value.guardando) return
        _state.update { it.copy(guardando = true) }
        viewModelScope.launch {
            try {
                accion()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _efectos.send(AgendaRecepcionEfecto.MostrarMensaje(MENSAJE_ERROR_GUARDAR))
            } finally {
                _state.update { it.copy(guardando = false) }
            }
        }
    }

    private suspend fun cerrarPanelYAvisar(texto: String) {
        _state.update { it.copy(panel = null) }
        _efectos.send(AgendaRecepcionEfecto.MostrarMensaje(texto))
    }

    private fun enviar(efecto: AgendaRecepcionEfecto) {
        viewModelScope.launch { _efectos.send(efecto) }
    }

    private fun leerFecha(texto: String): LocalDate? = try {
        LocalDate.parse(texto.trim(), formatoFechaCampo)
    } catch (_: DateTimeParseException) {
        null
    }

    private fun leerHora(texto: String): LocalTime? = try {
        LocalTime.parse(texto.trim(), formatoHoraCampo)
    } catch (_: DateTimeParseException) {
        null
    }

    companion object {
        private const val MENSAJE_ERROR_CARGA = "No se pudieron cargar las citas"
        private const val MENSAJE_ERROR_GUARDAR = "No se pudo guardar. Intenta de nuevo."
        private const val MENSAJE_ERROR_LLEGADA = "No se pudo registrar la llegada"
        private const val MENSAJE_NO_REPROGRAMADA = "No se pudo reprogramar la cita"
        private const val MENSAJE_LLEGADA_OTRO_DIA = "La llegada solo se registra el día de la cita"

        val Factory = viewModelFactory {
            initializer {
                val app = this[
                    ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY
                ] as CliniMedikApp

                AgendaRecepcionViewModel(
                    app.container.citaRepositorio,
                    app.container.configuracionRepositorio
                )
            }
        }
    }
}

sealed interface AgendaRecepcionEfecto {
    data object IrANuevaCita : AgendaRecepcionEfecto
    data class MostrarMensaje(val texto: String) : AgendaRecepcionEfecto
    data class AbrirDetalle(val visitaId: String) : AgendaRecepcionEfecto
    data class AbrirCuestionario(val citaId: String) : AgendaRecepcionEfecto
}
