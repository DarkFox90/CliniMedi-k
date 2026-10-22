package plat.clinimedik.app.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import java.time.LocalDateTime
import kotlinx.coroutines.flow.Flow
import plat.clinimedik.app.data.local.entity.CobroEntity

@Dao
interface CobroDao {

    @Query("SELECT * FROM cobro WHERE visitaId IN (:visitaIds)")
    fun observarDeVisitas(visitaIds: List<String>): Flow<List<CobroEntity>>

    @Query("SELECT cobro.* FROM cobro INNER JOIN visita ON cobro.visitaId = visita.id WHERE visita.medicoId = :medicoId AND visita.horaLlegada >= :inicio AND visita.horaLlegada < :fin")
    fun observarDelDia(medicoId: String, inicio: LocalDateTime, fin: LocalDateTime): Flow<List<CobroEntity>>

    @Query("SELECT * FROM cobro WHERE visitaId = :visitaId LIMIT 1")
    suspend fun obtenerDeVisita(visitaId: String): CobroEntity?

    @Insert
    suspend fun insertar(cobro: CobroEntity)

    @Insert
    suspend fun insertarTodos(cobros: List<CobroEntity>)

    @Query("DELETE FROM cobro")
    suspend fun borrarTodos()
}
