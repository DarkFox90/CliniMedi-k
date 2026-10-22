package plat.clinimedik.app.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow
import plat.clinimedik.app.data.local.entity.RecetaEntity

@Dao
interface RecetaDao {

    @Query("SELECT * FROM receta WHERE visitaId IN (:visitaIds)")
    fun observarDeVisitas(visitaIds: List<String>): Flow<List<RecetaEntity>>

    @Query("SELECT receta.* FROM receta INNER JOIN visita ON receta.visitaId = visita.id WHERE visita.pacienteId = :pacienteId ORDER BY visita.horaLlegada DESC")
    fun observarDePaciente(pacienteId: String): Flow<List<RecetaEntity>>

    @Insert
    suspend fun insertar(receta: RecetaEntity)

    @Insert
    suspend fun insertarTodos(recetas: List<RecetaEntity>)

    @Query("DELETE FROM receta")
    suspend fun borrarTodos()
}
