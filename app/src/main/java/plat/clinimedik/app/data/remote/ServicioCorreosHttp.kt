package plat.clinimedik.app.data.remote

import java.io.IOException
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Cliente HTTP del Worker de correos. Cada pedido lleva la clave compartida en `X-Clave-App`.
 * Las respuestas 5xx, 408 y 429 y la falta de conexión se pueden reintentar; las demás no.
 */
class ServicioCorreosHttp(
    urlBase: String,
    private val clave: String,
    private val cliente: OkHttpClient = clientePredeterminado()
) : ServicioCorreos {

    private val base = urlBase.trimEnd('/')
    private val json = Json { ignoreUnknownKeys = true }

    override val activo: Boolean = true

    override suspend fun registrarCita(cita: CitaCorreo) {
        enviar("/citas", json.encodeToString(CitaCorreo.serializer(), cita))
    }

    override suspend fun cancelarCita(citaId: String) {
        enviar("/citas/${codificar(citaId)}/cancelar", CUERPO_VACIO)
    }

    override suspend fun marcarConfirmada(citaId: String) {
        enviar("/citas/${codificar(citaId)}/confirmada", CUERPO_VACIO)
    }

    override suspend fun consultarConfirmaciones(): List<String> {
        val texto = ejecutar(Request.Builder().url(base + "/confirmaciones").get())
        return try {
            json.decodeFromString(RespuestaConfirmaciones.serializer(), texto).confirmadas
        } catch (error: IllegalArgumentException) {
            throw ErrorCorreos("Respuesta inesperada del servicio de correos", reintentable = false, causa = error)
        }
    }

    override suspend fun avisarUrgente(aviso: AvisoUrgenteCorreo) {
        enviar("/avisos/urgente", json.encodeToString(AvisoUrgenteCorreo.serializer(), aviso))
    }

    override suspend fun enviarResumen(resumen: ResumenCorreo) {
        enviar("/avisos/resumen", json.encodeToString(ResumenCorreo.serializer(), resumen))
    }

    private suspend fun enviar(ruta: String, cuerpo: String) {
        ejecutar(Request.Builder().url(base + ruta).post(cuerpo.toRequestBody(TIPO_JSON)))
    }

    private suspend fun ejecutar(solicitud: Request.Builder): String = withContext(Dispatchers.IO) {
        val pedido = solicitud.header(ENCABEZADO_CLAVE, clave).build()
        try {
            cliente.newCall(pedido).execute().use { respuesta ->
                val cuerpo = respuesta.body?.string().orEmpty()
                if (!respuesta.isSuccessful) {
                    val codigo = respuesta.code
                    throw ErrorCorreos(
                        "El servicio de correos respondió $codigo",
                        reintentable = codigo >= 500 || codigo == 408 || codigo == 429
                    )
                }
                cuerpo
            }
        } catch (error: IOException) {
            throw ErrorCorreos("Sin conexión con el servicio de correos", reintentable = true, causa = error)
        }
    }

    private fun codificar(texto: String): String = URLEncoder.encode(texto, "UTF-8")

    private companion object {
        const val ENCABEZADO_CLAVE = "X-Clave-App"
        const val CUERPO_VACIO = "{}"
        val TIPO_JSON = "application/json; charset=utf-8".toMediaType()

        fun clientePredeterminado(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .build()
    }
}
