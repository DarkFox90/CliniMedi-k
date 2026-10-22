package plat.clinimedik.app.data.local

import androidx.room3.ColumnTypeConverter
import java.time.LocalDate
import java.time.LocalDateTime

class Convertidores {

    @ColumnTypeConverter
    fun fechaATexto(valor: LocalDate?): String? = valor?.toString()

    @ColumnTypeConverter
    fun textoAFecha(valor: String?): LocalDate? = valor?.let { LocalDate.parse(it) }

    @ColumnTypeConverter
    fun fechaHoraATexto(valor: LocalDateTime?): String? = valor?.toString()

    @ColumnTypeConverter
    fun textoAFechaHora(valor: String?): LocalDateTime? = valor?.let { LocalDateTime.parse(it) }
}
