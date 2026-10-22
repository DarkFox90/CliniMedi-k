package plat.clinimedik.app.data.local.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import plat.clinimedik.app.data.local.entity.MetadatoEntity

@Dao
interface MetadatoDao {

    @Query("SELECT * FROM metadato WHERE id = 1")
    suspend fun obtener(): MetadatoEntity?

    @Upsert
    suspend fun guardar(metadato: MetadatoEntity)
}
