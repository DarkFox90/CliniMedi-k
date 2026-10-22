package plat.clinimedik.app.data.local

import android.content.Context
import androidx.room3.ColumnTypeConverters
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import plat.clinimedik.app.data.local.dao.CitaDao
import plat.clinimedik.app.data.local.dao.CobroDao
import plat.clinimedik.app.data.local.dao.DocumentoDao
import plat.clinimedik.app.data.local.dao.MedicoDao
import plat.clinimedik.app.data.local.dao.MetadatoDao
import plat.clinimedik.app.data.local.dao.PacienteDao
import plat.clinimedik.app.data.local.dao.RecetaDao
import plat.clinimedik.app.data.local.dao.ReferenciaDao
import plat.clinimedik.app.data.local.dao.VisitaDao
import plat.clinimedik.app.data.local.entity.CitaEntity
import plat.clinimedik.app.data.local.entity.CobroEntity
import plat.clinimedik.app.data.local.entity.DocumentoEntity
import plat.clinimedik.app.data.local.entity.MedicoEntity
import plat.clinimedik.app.data.local.entity.MetadatoEntity
import plat.clinimedik.app.data.local.entity.PacienteEntity
import plat.clinimedik.app.data.local.entity.RecetaEntity
import plat.clinimedik.app.data.local.entity.ReferenciaEntity
import plat.clinimedik.app.data.local.entity.VisitaEntity

@Database(
    entities = [
        MedicoEntity::class, PacienteEntity::class, VisitaEntity::class,
        RecetaEntity::class, ReferenciaEntity::class, CobroEntity::class,
        DocumentoEntity::class, CitaEntity::class, MetadatoEntity::class
    ],
    version = 1,
    exportSchema = true
)
@ColumnTypeConverters(Convertidores::class)
abstract class CliniMedikDatabase : RoomDatabase() {
    abstract fun medicoDao(): MedicoDao
    abstract fun pacienteDao(): PacienteDao
    abstract fun visitaDao(): VisitaDao
    abstract fun recetaDao(): RecetaDao
    abstract fun referenciaDao(): ReferenciaDao
    abstract fun cobroDao(): CobroDao
    abstract fun documentoDao(): DocumentoDao
    abstract fun citaDao(): CitaDao
    abstract fun metadatoDao(): MetadatoDao

    companion object {
        fun crear(context: Context): CliniMedikDatabase =
            Room.databaseBuilder<CliniMedikDatabase>(context, "clinimedik.db")
                .setQueryCoroutineContext(Dispatchers.IO)
                .setDriver(BundledSQLiteDriver())
                .build()
    }
}
