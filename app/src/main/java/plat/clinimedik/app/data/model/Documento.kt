package plat.clinimedik.app.data.model

import java.time.LocalDateTime

data class Documento (
    val id: String,
    val pacienteId: String,
    val tipo: TipoDocumento,
    val rutaArchivo: String,
    val fecha: LocalDateTime
)