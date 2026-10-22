package plat.clinimedik.app.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow
import plat.clinimedik.app.data.local.entity.MedicoEntity

@Dao
interface MedicoDao {

    @Query("SELECT * FROM medico ORDER BY nombre")
    fun observarTodos(): Flow<List<MedicoEntity>>

    @Query("SELECT * FROM medico WHERE id = :id")
    fun observarPorId(id: String): Flow<MedicoEntity?>

    @Insert
    suspend fun insertar(medico: MedicoEntity)

    @Upsert
    suspend fun guardarTodos(medicos: List<MedicoEntity>)
}
