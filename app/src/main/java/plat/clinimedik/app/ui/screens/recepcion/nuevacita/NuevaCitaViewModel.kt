
package plat.clinimedik.app.ui.screens.recepcion.nuevacita

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
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
import plat.clinimedik.app.data.model.Cita
import plat.clinimedik.app.data.model.EstadoConfirmacion
import plat.clinimedik.app.data.preferencias.ConfiguracionRepositorio
import plat.clinimedik.app.data.repository.CitaRepositorio
import plat.clinimedik.app.data.repository.PacienteRepositorio

class NuevaCitaViewModel(
    private val citaRepositorio: CitaRepositorio,
    private val pacienteRepositorio: PacienteRepositorio,
    private val configuracionRepositorio: ConfiguracionRepositorio
) : ViewModel() {

    private val _state = MutableStateFlow(NuevaCitaUiState())
    val state: StateFlow<NuevaCitaUiState> = _state.asStateFlow()

    private val _efectos = Channel<NuevaCitaEfecto>()
    val efectos: Flow<NuevaCitaEfecto> = _efectos.receiveAsFlow()

    private val formatoFecha = DateTimeFormatter.ofPattern("dd/MM/uuuu")

    fun onEvent(event: NuevaCitaEvent) {
        when (event) {
            is NuevaCitaEvent.CambiarPaciente ->
                _state.update { it.copy(paciente = event.texto, errorPaciente = null) }

            is NuevaCitaEvent.CambiarCorreo ->
                _state.update { it.copy(correo = event.texto, errorCorreo = null) }

            is NuevaCitaEvent.CambiarFecha ->
                _state.update { it.copy(fecha = event.texto, errorFecha = null) }

            is NuevaCitaEvent.CambiarHora ->
                _state.update { it.copy(hora = event.texto, errorHora = null) }

            is NuevaCitaEvent.CambiarMotivo ->
                _state.update { it.copy(motivo = event.texto, errorMotivo = null) }

            NuevaCitaEvent.Guardar -> guardarCita()

            NuevaCitaEvent.Regresar -> {
                if (!_state.value.guardando) {
                    enviar(NuevaCitaEfecto.Regresar)
                }
            }
        }
    }

    private fun guardarCita() {
        val datos = _state.value
        if (datos.guardando) return

        val fecha = try {
            LocalDate.parse(datos.fecha.trim(), formatoFecha)
        } catch (_: DateTimeParseException) {
            null
        }

        val hora = try {
            LocalTime.parse(datos.hora.trim(), DateTimeFormatter.ofPattern("H:mm"))
        } catch (_: DateTimeParseException) {
            null
        }

        val fechaHora = if (fecha != null && hora != null) {
            LocalDateTime.of(fecha, hora)
        } else null

        val errorPaciente = if (datos.paciente.isBlank()) {
            "Ingresa el nombre del paciente"
        } else null

        val errorFecha = when {
            fecha == null -> "Usa el formato dd/mm/aaaa"
            fecha.isBefore(LocalDate.now()) -> "La fecha no puede ser anterior a hoy"
            else -> null
        }

        val errorHora = when {
            hora == null -> "Usa el formato de 24 horas, por ejemplo 09:30"
            fechaHora != null && fechaHora.isBefore(LocalDateTime.now()) ->
                "La hora de la cita ya pasó"
            else -> null
        }

        val errorMotivo = if (datos.motivo.isBlank()) {
            "Ingresa el motivo de la cita"
        } else null

        val errorCorreo = if (
            datos.correo.isNotBlank() &&
            !android.util.Patterns.EMAIL_ADDRESS.matcher(datos.correo.trim()).matches()
        ) {
            "Ingresa un correo válido"
        } else null

        _state.update {
            it.copy(
                errorPaciente = errorPaciente,
                errorCorreo = errorCorreo,
                errorFecha = errorFecha,
                errorHora = errorHora,
                errorMotivo = errorMotivo
            )
        }

        if (listOf(
                errorPaciente, errorCorreo, errorFecha,
                errorHora, errorMotivo
            ).any { it != null }
        ) return

        viewModelScope.launch {
            _state.update { it.copy(guardando = true) }

            try {
                val configuracion = configuracionRepositorio.configuracion.first()
                    ?: run {
                        _state.update {
                            it.copy(errorPaciente = "Configura primero el médico")
                        }
                        return@launch
                    }

                val nombre = datos.paciente.trim()
                val pacienteRegistrado = pacienteRepositorio.buscar(nombre)
                    .first()
                    .firstOrNull { it.nombreCompleto.equals(nombre, ignoreCase = true) }

                if (pacienteRegistrado == null && datos.correo.isBlank()) {
                    _state.update {
                        it.copy(errorCorreo = "El correo es obligatorio para pacientes nuevos")
                    }
                    return@launch
                }

                val horario = fechaHora ?: return@launch

                val conflicto = citaRepositorio.buscarConflicto(
                    configuracion.medicoId,
                    horario
                )

                if (conflicto != null) {
                    _state.update {
                        it.copy(errorHora = "Ya existe una cita en ese horario")
                    }
                    return@launch
                }

                val cita = Cita(
                    id = "",
                    pacienteId = pacienteRegistrado?.id,
                    nombrePaciente = nombre,
                    correoPaciente = datos.correo.trim().ifBlank {
                        pacienteRegistrado?.correo
                    },
                    medicoId = configuracion.medicoId,
                    fechaHora = horario,
                    duracionMinutos = 30,
                    motivo = datos.motivo.trim(),
                    estadoConfirmacion = EstadoConfirmacion.PENDIENTE,
                    recordatorioEnviado = false
                )

                citaRepositorio.crear(cita)
                _efectos.send(NuevaCitaEfecto.Regresar)

            } catch (_: Exception) {
                _state.update {
                    it.copy(errorMotivo = "No se pudo guardar la cita. Inténtalo de nuevo")
                }
            } finally {
                _state.update { it.copy(guardando = false) }
            }
        }
    }

    private fun enviar(efecto: NuevaCitaEfecto) {
        viewModelScope.launch { _efectos.send(efecto) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as CliniMedikApp

                NuevaCitaViewModel(
                    app.container.citaRepositorio,
                    app.container.pacienteRepositorio,
                    app.container.configuracionRepositorio
                )
            }
        }
    }
}

sealed interface NuevaCitaEfecto {
    data object Regresar : NuevaCitaEfecto
}
