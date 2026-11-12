package plat.clinimedik.app.data.repository

import androidx.room3.withWriteTransaction
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import plat.clinimedik.app.data.local.CliniMedikDatabase
import plat.clinimedik.app.data.local.dao.CitaDao
import plat.clinimedik.app.data.local.dao.VisitaDao
import plat.clinimedik.app.data.local.entity.CitaEntity
import plat.clinimedik.app.data.local.entity.VisitaEntity
import plat.clinimedik.app.data.local.entity.aEntity
import plat.clinimedik.app.data.local.entity.aModelo
import plat.clinimedik.app.data.model.Cita
import plat.clinimedik.app.data.model.EstadoCita
import plat.clinimedik.app.data.model.EstadoConfirmacion
import plat.clinimedik.app.data.model.EstadoVisita
import plat.clinimedik.app.data.model.Visita
import plat.clinimedik.app.data.sincronizacion.Sincronizador

class CitaRepositorio(
    private val database: CliniMedikDatabase,
    private val citaDao: CitaDao,
    private val visitaDao: VisitaDao,
    private val sincronizador: Sincronizador
) {

    /**
     * Todas las citas de un día, incluidas las canceladas y las completadas.
     * Sin [fecha] devuelve las de hoy y cambia sola de día a medianoche.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observarCitasDelDia(medicoId: String, fecha: LocalDate? = null): Flow<List<Cita>> =
        if (fecha != null) {
            citasDe(medicoId, fecha)
        } else {
            diaActual().flatMapLatest { hoy -> citasDe(medicoId, hoy) }
        }

    private fun citasDe(medicoId: String, fecha: LocalDate): Flow<List<Cita>> =
        citaDao.observarDelDia(medicoId, inicioDelDia(fecha), finDelDia(fecha))
            .map { lista -> lista.map { it.aModelo() } }

    fun observarCita(citaId: String): Flow<Cita?> =
        citaDao.observarPorId(citaId).map { it?.aModelo() }

    /** La siguiente cita PROGRAMADA del paciente, contada desde el momento en que se empieza a observar. */
    fun observarProximaCita(pacienteId: String): Flow<Cita?> = flow {
        emitAll(
            citaDao.observarProximaDePaciente(pacienteId, LocalDateTime.now()).map { it?.aModelo() }
        )
    }

    /** Citas PROGRAMADA de ese día que el paciente todavía no confirma (pendientes o no confirmadas). */
    fun observarSinConfirmar(medicoId: String, fecha: LocalDate): Flow<List<Cita>> =
        citaDao.observarSinConfirmar(medicoId, inicioDelDia(fecha), finDelDia(fecha))
            .map { lista -> lista.map { it.aModelo() } }

    /**
     * Devuelve la cita que ocupa ese horario, o `null` si está libre.
     * Las citas canceladas no ocupan horario. [citaIgnoradaId] excluye a la cita que se está reprogramando.
     */
    suspend fun buscarConflicto(
        medicoId: String,
        fechaHora: LocalDateTime,
        duracionMinutos: Int = 30,
        citaIgnoradaId: String? = null
    ): Cita? {
        val fecha = fechaHora.toLocalDate()
        val finNueva = fechaHora.plusMinutes(duracionMinutos.toLong())
        return citaDao.obtenerDelDia(medicoId, inicioDelDia(fecha), finDelDia(fecha))
            .map { it.aModelo() }
            .firstOrNull { cita ->
                val finExistente = cita.fechaHora.plusMinutes(cita.duracionMinutos.toLong())
                cita.estado != EstadoCita.CANCELADA &&
                    cita.id != citaIgnoradaId &&
                    cita.fechaHora < finNueva &&
                    fechaHora < finExistente
            }
    }

    suspend fun crear(cita: Cita): String {
        val entidad = cita.copy(id = UUID.randomUUID().toString()).aEntity()
        citaDao.insertar(entidad)
        publicar { sincronizador.publicarCita(entidad) }
        return entidad.id
    }

    suspend fun marcarConfirmacion(citaId: String, estado: EstadoConfirmacion) {
        modificarCita(citaId) { it.copy(estadoConfirmacion = estado) }
    }

    /** Cancela una cita PROGRAMADA. En cualquier otro estado no hace nada. */
    suspend fun cancelar(citaId: String) {
        modificarCita(citaId) { cita ->
            if (cita.estado == EstadoCita.PROGRAMADA) cita.copy(estado = EstadoCita.CANCELADA) else null
        }
    }

    /**
     * Mueve una cita PROGRAMADA a otro horario y reinicia su confirmación.
     * Devuelve la cita que ocupa el horario nuevo, o `null` si se reprogramó.
     * También devuelve `null`, sin cambiar nada, si la cita no existe o no está PROGRAMADA.
     */
    suspend fun reprogramar(citaId: String, nuevaFechaHora: LocalDateTime): Cita? {
        val (conflicto, reprogramada) = database.withWriteTransaction<Pair<Cita?, CitaEntity?>> {
            val actual = citaDao.obtenerPorId(citaId)
            if (actual == null || actual.estado != EstadoCita.PROGRAMADA) {
                null to null
            } else {
                val ocupada = buscarConflicto(
                    medicoId = actual.medicoId,
                    fechaHora = nuevaFechaHora,
                    duracionMinutos = actual.duracionMinutos,
                    citaIgnoradaId = citaId
                )
                if (ocupada != null) {
                    ocupada to null
                } else {
                    val nueva = actual.copy(
                        fechaHora = nuevaFechaHora,
                        estadoConfirmacion = EstadoConfirmacion.PENDIENTE,
                        recordatorioEnviado = false,
                        idCorreoRecordatorio = null
                    )
                    citaDao.actualizar(nueva)
                    null to nueva
                }
            }
        }
        if (reprogramada != null) {
            publicar { sincronizador.publicarCita(reprogramada) }
        }
        return conflicto
    }

    /**
     * El paciente de la cita llegó: lo agrega a la fila de hoy, marca la cita COMPLETADA
     * y devuelve el id de la visita. Devuelve `null` si la cita no tiene paciente registrado.
     * Si el paciente ya tiene una visita activa hoy con ese médico, se usa esa y no se crea otra.
     * Si la cita ya estaba COMPLETADA, devuelve la visita que ya tenía.
     */
    suspend fun registrarLlegada(citaId: String): String? {
        val ahora = LocalDateTime.now()
        val llegada = database.withWriteTransaction<Llegada?> {
            val cita = citaDao.obtenerPorId(citaId)
            val pacienteId = cita?.pacienteId
            val visitaPrevia = cita?.visitaId
            if (cita == null || pacienteId == null) {
                null
            } else if (cita.estado == EstadoCita.COMPLETADA && visitaPrevia != null) {
                Llegada(visitaId = visitaPrevia)
            } else {
                val hoy = ahora.toLocalDate()
                val activa = visitaDao.obtenerActivaDelDia(
                    pacienteId,
                    cita.medicoId,
                    inicioDelDia(hoy),
                    finDelDia(hoy)
                )
                val creada = if (activa == null) {
                    Visita(
                        id = UUID.randomUUID().toString(),
                        pacienteId = pacienteId,
                        medicoId = cita.medicoId,
                        motivoConsulta = cita.motivo,
                        horaLlegada = ahora,
                        estado = EstadoVisita.ESPERANDO
                    ).aEntity().also { visitaDao.insertar(it) }
                } else {
                    null
                }
                val visitaId = activa?.id ?: creada?.id
                if (visitaId == null) {
                    null
                } else {
                    val completada = cita.copy(estado = EstadoCita.COMPLETADA, visitaId = visitaId)
                    citaDao.actualizar(completada)
                    Llegada(visitaId = visitaId, visitaCreada = creada, citaActualizada = completada)
                }
            }
        }
        llegada?.visitaCreada?.let { visita -> publicar { sincronizador.publicarVisita(visita) } }
        llegada?.citaActualizada?.let { cita -> publicar { sincronizador.publicarCita(cita) } }
        return llegada?.visitaId
    }

    /** Une la cita con el paciente recién registrado desde ella y la marca COMPLETADA. */
    suspend fun enlazarPaciente(citaId: String, pacienteId: String, visitaId: String) {
        modificarCita(citaId) {
            it.copy(pacienteId = pacienteId, visitaId = visitaId, estado = EstadoCita.COMPLETADA)
        }
    }

    private suspend fun modificarCita(citaId: String, cambio: (CitaEntity) -> CitaEntity?) {
        val modificada = database.withWriteTransaction<CitaEntity?> {
            val nueva = citaDao.obtenerPorId(citaId)?.let(cambio)
            if (nueva != null) {
                citaDao.actualizar(nueva)
            }
            nueva
        }
        if (modificada != null) {
            publicar { sincronizador.publicarCita(modificada) }
        }
    }
}

private data class Llegada(
    val visitaId: String,
    val visitaCreada: VisitaEntity? = null,
    val citaActualizada: CitaEntity? = null
)
