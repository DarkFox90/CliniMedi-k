package plat.clinimedik.app.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import java.time.LocalDateTime
import kotlinx.coroutines.flow.Flow
import plat.clinimedik.app.data.local.entity.VisitaEntity

@Dao
interface VisitaDao {

    @Query("SELECT * FROM visita WHERE medicoId = :medicoId AND horaLlegada >= :inicio AND horaLlegada < :fin")
    fun observarDelDia(medicoId: String, inicio: LocalDateTime, fin: LocalDateTime): Flow<List<VisitaEntity>>

    @Query("SELECT * FROM visita WHERE id = :id")
    fun observarPorId(id: String): Flow<VisitaEntity?>

    @Query("SELECT * FROM visita WHERE pacienteId = :pacienteId ORDER BY horaLlegada DESC")
    fun observarDePaciente(pacienteId: String): Flow<List<VisitaEntity>>

    @Query("SELECT * FROM visita WHERE medicoId = :medicoId ORDER BY horaLlegada DESC")
    fun observarDeMedico(medicoId: String): Flow<List<VisitaEntity>>

    @Query("SELECT * FROM visita WHERE id = :id")
    suspend fun obtenerPorId(id: String): VisitaEntity?

    @Query("SELECT * FROM visita WHERE pacienteId = :pacienteId AND medicoId = :medicoId AND estado IN ('ESPERANDO', 'EN_CONSULTA') AND horaLlegada >= :inicio AND horaLlegada < :fin LIMIT 1")
    suspend fun obtenerActivaDelDia(pacienteId: String, medicoId: String, inicio: LocalDateTime, fin: LocalDateTime): VisitaEntity?

    @Insert
    suspend fun insertar(visita: VisitaEntity)

    @Update
    suspend fun actualizar(visita: VisitaEntity)

    @Insert
    suspend fun insertarTodos(visitas: List<VisitaEntity>)

    @Query("DELETE FROM visita")
    suspend fun borrarTodos()
}
