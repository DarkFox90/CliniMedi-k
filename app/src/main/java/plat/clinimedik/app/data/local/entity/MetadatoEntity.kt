package plat.clinimedik.app.data.local.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "metadato")
data class MetadatoEntity(
    @PrimaryKey val id: Int = 1,
    val fechaSemilla: LocalDate
)
