package plat.clinimedik.app.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow
import plat.clinimedik.app.data.local.entity.DocumentoEntity

@Dao
interface DocumentoDao {

    @Query("SELECT * FROM documento WHERE pacienteId = :pacienteId ORDER BY fecha DESC")
    fun observarDePaciente(pacienteId: String): Flow<List<DocumentoEntity>>

    @Insert
    suspend fun insertarTodos(documentos: List<DocumentoEntity>)

    @Query("DELETE FROM documento")
    suspend fun borrarTodos()
}
