package plat.clinimedik.app.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow
import plat.clinimedik.app.data.local.entity.PacienteEntity

@Dao
interface PacienteDao {

    @Query("SELECT * FROM paciente ORDER BY nombreCompleto")
    fun observarTodos(): Flow<List<PacienteEntity>>

    @Query("SELECT * FROM paciente WHERE id = :id")
    fun observarPorId(id: String): Flow<PacienteEntity?>

    @Query("SELECT * FROM paciente WHERE id IN (:ids)")
    fun observarPorIds(ids: List<String>): Flow<List<PacienteEntity>>

    @Query("SELECT * FROM paciente WHERE id IN (SELECT pacienteId FROM visita WHERE medicoId = :medicoId) ORDER BY nombreCompleto")
    fun observarDeMedico(medicoId: String): Flow<List<PacienteEntity>>

    @Insert
    suspend fun insertar(paciente: PacienteEntity)

    @Insert
    suspend fun insertarTodos(pacientes: List<PacienteEntity>)

    @Query("DELETE FROM paciente")
    suspend fun borrarTodos()
}
