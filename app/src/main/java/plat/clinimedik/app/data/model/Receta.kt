package plat.clinimedik.app.data.model

data class Receta (
    val id: String,
    val visitaId: String,
    val medicamento: String,
    val dosis: String,
    val frecuencia: String,
    val duracion: String
)