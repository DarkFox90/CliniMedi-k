package plat.clinimedik.app.data.repository

import androidx.room3.withWriteTransaction
import java.text.Normalizer
import java.time.LocalDateTime
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import plat.clinimedik.app.data.local.CliniMedikDatabase
import plat.clinimedik.app.data.local.dao.DocumentoDao
import plat.clinimedik.app.data.local.dao.PacienteDao
import plat.clinimedik.app.data.local.dao.VisitaDao
import plat.clinimedik.app.data.local.entity.DocumentoEntity
import plat.clinimedik.app.data.local.entity.aEntity
import plat.clinimedik.app.data.local.entity.aModelo
import plat.clinimedik.app.data.model.Documento
import plat.clinimedik.app.data.model.EstadoVisita
import plat.clinimedik.app.data.model.Paciente
import plat.clinimedik.app.data.model.TipoDocumento
import plat.clinimedik.app.data.model.Visita
import plat.clinimedik.app.data.sincronizacion.Sincronizador

class PacienteRepositorio(
    private val database: CliniMedikDatabase,
    private val pacienteDao: PacienteDao,
    private val visitaDao: VisitaDao,
    private val documentoDao: DocumentoDao,
    private val sincronizador: Sincronizador
) {

    fun observarPaciente(id: String): Flow<Paciente?> =
        pacienteDao.observarPorId(id).map { it?.aModelo() }

    fun buscar(texto: String): Flow<List<Paciente>> {
        val busqueda = normalizar(texto.trim())
        if (busqueda.isEmpty()) return flowOf(emptyList())
        val digitos = busqueda.filter { it.isDigit() }
        return pacienteDao.observarTodos().map { lista ->
            lista.map { it.aModelo() }.filter { paciente ->
                normalizar(paciente.nombreCompleto).contains(busqueda) ||
                    (digitos.isNotEmpty() && paciente.telefono.filter { it.isDigit() }.contains(digitos))
            }
        }
    }

    fun observarPorIds(ids: List<String>): Flow<List<Paciente>> =
        pacienteDao.observarPorIds(ids).map { lista ->
            lista.map { it.aModelo() }.sortedBy { ids.indexOf(it.id) }
        }

    fun observarPacientesDelMedico(medicoId: String): Flow<List<Paciente>> =
        pacienteDao.observarDeMedico(medicoId).map { lista -> lista.map { it.aModelo() } }

    fun observarDocumentos(pacienteId: String): Flow<List<Documento>> =
        documentoDao.observarDePaciente(pacienteId).map { lista -> lista.map { it.aModelo() } }

    fun observarDocumento(documentoId: String): Flow<Documento?> =
        documentoDao.observarPorId(documentoId).map { it?.aModelo() }

    suspend fun registrarConVisita(paciente: Paciente, motivo: String, medicoId: String): String {
        val ahora = LocalDateTime.now()
        val pacienteId = UUID.randomUUID().toString()
        val pacienteNuevo = paciente.copy(id = pacienteId, fechaRegistro = ahora).aEntity()
        val visita = Visita(
            id = UUID.randomUUID().toString(),
            pacienteId = pacienteId,
            medicoId = medicoId,
            motivoConsulta = motivo.trim(),
            horaLlegada = ahora,
            estado = EstadoVisita.ESPERANDO
        ).aEntity()
        database.withWriteTransaction {
            pacienteDao.insertar(pacienteNuevo)
            visitaDao.insertar(visita)
        }
        publicar { sincronizador.publicarPaciente(pacienteNuevo) }
        publicar { sincronizador.publicarVisita(visita) }
        return pacienteId
    }

    /** Guarda los cambios de un paciente que ya existe. Si el id no existe, no hace nada. */
    suspend fun actualizar(paciente: Paciente) {
        val entidad = paciente.aEntity()
        if (pacienteDao.actualizar(entidad) > 0) {
            publicar { sincronizador.publicarPaciente(entidad) }
        }
    }

    /**
     * Registra la foto de un documento y devuelve su id.
     * Los documentos no se sincronizan: la foto vive solo en este teléfono.
     */
    suspend fun guardarDocumento(pacienteId: String, tipo: TipoDocumento, rutaArchivo: String): String {
        val documento = DocumentoEntity(
            id = UUID.randomUUID().toString(),
            pacienteId = pacienteId,
            tipo = tipo,
            rutaArchivo = rutaArchivo,
            fecha = LocalDateTime.now()
        )
        documentoDao.insertar(documento)
        return documento.id
    }

    private fun normalizar(texto: String): String =
        Normalizer.normalize(texto, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .lowercase()
}
