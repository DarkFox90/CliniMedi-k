package plat.clinimedik.app.data.local.entity

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import java.time.LocalDateTime
import plat.clinimedik.app.data.model.Documento
import plat.clinimedik.app.data.model.TipoDocumento

@Entity(
    tableName = "documento",
    indices = [Index("pacienteId")]
)
data class DocumentoEntity(
    @PrimaryKey val id: String,
    val pacienteId: String,
    val tipo: TipoDocumento,
    val rutaArchivo: String,
    val fecha: LocalDateTime
)

fun DocumentoEntity.aModelo(): Documento = Documento(
    id = id,
    pacienteId = pacienteId,
    tipo = tipo,
    rutaArchivo = rutaArchivo,
    fecha = fecha
)

fun Documento.aEntity(): DocumentoEntity = DocumentoEntity(
    id = id,
    pacienteId = pacienteId,
    tipo = tipo,
    rutaArchivo = rutaArchivo,
    fecha = fecha
)
