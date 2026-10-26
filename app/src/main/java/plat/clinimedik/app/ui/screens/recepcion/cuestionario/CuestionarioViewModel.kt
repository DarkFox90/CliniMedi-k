package plat.clinimedik.app.ui.screens.recepcion.cuestionario

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
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
import plat.clinimedik.app.data.model.EstadoCivil
import plat.clinimedik.app.data.model.Paciente
import plat.clinimedik.app.data.preferencias.ConfiguracionRepositorio
import plat.clinimedik.app.data.repository.PacienteRepositorio

class CuestionarioViewModel(
    private val pacienteRepositorio: PacienteRepositorio,
    private val configuracionRepositorio: ConfiguracionRepositorio
) : ViewModel() {

    private val _state = MutableStateFlow(CuestionarioUiState())
    val state: StateFlow<CuestionarioUiState> = _state.asStateFlow()

    private val _efectos = Channel<CuestionarioEfecto>()
    val efectos: Flow<CuestionarioEfecto> = _efectos.receiveAsFlow()

    private val formatoFecha = DateTimeFormatter
        .ofPattern("dd/MM/uuuu")
        .withResolverStyle(ResolverStyle.STRICT)

    fun onEvent(event: CuestionarioEvent) {
        when (event) {
            is CuestionarioEvent.CambiarNombre ->
                _state.update { it.copy(nombre = event.texto, errorNombre = null) }

            is CuestionarioEvent.CambiarFechaNacimiento ->
                _state.update {
                    it.copy(fechaNacimiento = event.texto, errorFechaNacimiento = null)
                }

            is CuestionarioEvent.SeleccionarEstadoCivil ->
                _state.update { it.copy(estadoCivil = event.estado) }

            is CuestionarioEvent.CambiarOcupacion ->
                _state.update { it.copy(ocupacion = event.texto) }

            is CuestionarioEvent.CambiarTelefono ->
                _state.update { it.copy(telefono = event.texto, errorTelefono = null) }

            is CuestionarioEvent.CambiarCorreo ->
                _state.update { it.copy(correo = event.texto, errorCorreo = null) }

            is CuestionarioEvent.CambiarMotivo ->
                _state.update { it.copy(motivo = event.texto, errorMotivo = null) }

            is CuestionarioEvent.CambiarAlergias ->
                _state.update { it.copy(alergias = event.texto) }

            is CuestionarioEvent.CambiarEnfermedades ->
                _state.update { it.copy(enfermedades = event.texto) }

            is CuestionarioEvent.CambiarTratamientos ->
                _state.update { it.copy(tratamientos = event.texto) }

            is CuestionarioEvent.CambiarCirugias ->
                _state.update { it.copy(cirugias = event.texto) }

            CuestionarioEvent.Guardar -> guardarPaciente()

            CuestionarioEvent.Regresar -> {
                if (!_state.value.guardando) {
                    enviar(CuestionarioEfecto.Regresar)
                }
            }
        }
    }

    private fun guardarPaciente() {
        val datos = _state.value
        if (datos.guardando) return

        val fecha = try {
            LocalDate.parse(datos.fechaNacimiento.trim(), formatoFecha)
        } catch (_: DateTimeParseException) {
            null
        }

        val errorNombre = if (datos.nombre.isBlank()) {
            "El nombre es obligatorio"
        } else null

        val errorFecha = when {
            datos.fechaNacimiento.isBlank() ->
                "La fecha de nacimiento es obligatoria"
            fecha == null ->
                "Ingresa una fecha válida (dd/mm/aaaa)"
            fecha.isAfter(LocalDate.now()) ->
                "La fecha no puede ser futura"
            else -> null
        }

        val telefono = datos.telefono.filter { it.isDigit() }
        val errorTelefono = if (telefono.length != 8) {
            "Ingresa un teléfono de 8 dígitos"
        } else null

        val errorCorreo = if (
            datos.correo.isNotBlank() &&
            !Patterns.EMAIL_ADDRESS.matcher(datos.correo.trim()).matches()
        ) {
            "El formato del correo no es válido"
        } else null

        val errorMotivo = if (datos.motivo.isBlank()) {
            "El motivo de consulta es obligatorio"
        } else null

        _state.update {
            it.copy(
                errorNombre = errorNombre,
                errorFechaNacimiento = errorFecha,
                errorTelefono = errorTelefono,
                errorCorreo = errorCorreo,
                errorMotivo = errorMotivo,
                mensajeError = null
            )
        }

        if (listOf(
                errorNombre, errorFecha, errorTelefono,
                errorCorreo, errorMotivo
            ).any { it != null }
        ) return

        viewModelScope.launch {
            _state.update { it.copy(guardando = true) }

            try {
                val configuracion = configuracionRepositorio.configuracion.first()
                    ?: throw IllegalStateException("No hay médico configurado")

                val paciente = Paciente(
                    id = "",
                    nombreCompleto = datos.nombre.trim(),
                    fechaNacimiento = requireNotNull(fecha),
                    telefono = telefono,
                    correo = datos.correo.trim(),
                    estadoCivil = datos.estadoCivil ?: EstadoCivil.SOLTERO,
                    ocupacion = datos.ocupacion.trim(),
                    alergias = datos.alergias.trim(),
                    enfermedadesPrevias = datos.enfermedades.trim(),
                    tratamientosPrevios = datos.tratamientos.trim(),
                    cirugias = datos.cirugias.trim(),
                    fechaRegistro = LocalDateTime.now()
                )

                val pacienteId = pacienteRepositorio.registrarConVisita(
                    paciente = paciente,
                    motivo = datos.motivo.trim(),
                    medicoId = configuracion.medicoId
                )

                _efectos.send(CuestionarioEfecto.IrACodigoQr(pacienteId))

            } catch (_: Exception) {
                _state.update {
                    it.copy(mensajeError = "No se pudo registrar el paciente")
                }
            } finally {
                _state.update { it.copy(guardando = false) }
            }
        }
    }

    private fun enviar(efecto: CuestionarioEfecto) {
        viewModelScope.launch { _efectos.send(efecto) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[
                    ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY
                ] as CliniMedikApp

                CuestionarioViewModel(
                    app.container.pacienteRepositorio,
                    app.container.configuracionRepositorio
                )
            }
        }
    }
}

sealed interface CuestionarioEfecto {
    data object Regresar : CuestionarioEfecto
    data class IrACodigoQr(val pacienteId: String) : CuestionarioEfecto
}
