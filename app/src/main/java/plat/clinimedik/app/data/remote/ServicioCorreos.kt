package plat.clinimedik.app.data.remote

/**
 * Llamadas al Worker de correos (carpeta `correos-worker/` del repositorio).
 * Solo los trabajos de `data/trabajos` lo usan; las pantallas nunca llaman a internet directamente.
 */
interface ServicioCorreos {

    /** `false` cuando el teléfono no tiene la URL o la clave del Worker en `local.properties`. */
    val activo: Boolean

    /** Crea o reemplaza la cita en el Worker. Si es nueva o cambió de hora, el paciente recibe el correo de confirmación. */
    suspend fun registrarCita(cita: CitaCorreo)

    /** Borra la cita del Worker: ya no recibe recordatorio. */
    suspend fun cancelarCita(citaId: String)

    /** Avisa al Worker que Recepción confirmó la cita a mano, para que no aparezca en el aviso de citas sin confirmar. */
    suspend fun marcarConfirmada(citaId: String)

    /** Ids de las citas que el paciente confirmó con el enlace del correo. */
    suspend fun consultarConfirmaciones(): List<String>

    suspend fun avisarUrgente(aviso: AvisoUrgenteCorreo)

    suspend fun enviarResumen(resumen: ResumenCorreo)
}

/**
 * Error al hablar con el Worker. [reintentable] indica si vale la pena intentarlo más tarde
 * (sin conexión, error del servidor) o si el pedido está mal y no se repite.
 */
class ErrorCorreos(
    mensaje: String,
    val reintentable: Boolean,
    causa: Throwable? = null
) : Exception(mensaje, causa)

/** Se usa cuando `local.properties` no tiene `CORREOS_URL` o `CORREOS_CLAVE`: la app funciona sin enviar correos. */
object ServicioCorreosDesactivado : ServicioCorreos {

    override val activo: Boolean = false

    private fun desactivado(): Nothing =
        throw ErrorCorreos("El envío de correos no está configurado en este teléfono", reintentable = false)

    override suspend fun registrarCita(cita: CitaCorreo) = desactivado()
    override suspend fun cancelarCita(citaId: String) = desactivado()
    override suspend fun marcarConfirmada(citaId: String) = desactivado()
    override suspend fun consultarConfirmaciones(): List<String> = desactivado()
    override suspend fun avisarUrgente(aviso: AvisoUrgenteCorreo) = desactivado()
    override suspend fun enviarResumen(resumen: ResumenCorreo) = desactivado()
}
