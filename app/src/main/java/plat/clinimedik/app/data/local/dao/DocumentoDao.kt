package plat.clinimedik.app.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow
import plat.clinimedik.app.data.local.entity.DocumentoEntity

@Dao
interface DocumentoDao {

    @Query("SELECT * FROM documento WHERE pacienteId = :pacienteId ORDER BY fecha DESC")
    fun observarDePaciente(pacienteId: String): Flow<List<DocumentoEntity>>

    @Query("SELECT * FROM documento WHERE id = :id")
    fun observarPorId(id: String): Flow<DocumentoEntity?>

    @Insert
    suspend fun insertar(documento: DocumentoEntity)

    @Upsert
    suspend fun guardar(documento: DocumentoEntity)

    @Upsert
    suspend fun guardarTodos(documentos: List<DocumentoEntity>)
}
