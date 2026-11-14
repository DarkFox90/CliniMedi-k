package plat.clinimedik.app.data.trabajos

import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException
import plat.clinimedik.app.CliniMedikApp
import plat.clinimedik.app.data.remote.ErrorCorreos
import plat.clinimedik.app.dependencyinjection.AppContainer

private const val ETIQUETA_LOG = "Correos"
private const val MAXIMO_INTENTOS = 8

/** Solicitud de un trabajo de correo: solo corre con internet y, si falla, se reintenta cada vez más espaciado. */
internal inline fun <reified T : CoroutineWorker> solicitudDeCorreo(datos: Data = Data.EMPTY): OneTimeWorkRequest =
    OneTimeWorkRequestBuilder<T>()
        .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
        .setInputData(datos)
        .build()

/**
 * Ejecuta un trabajo de correo con las dependencias de [AppContainer].
 * Sin conexión o con un error del servidor pide un reintento; ante un pedido rechazado o
 * después de [MAXIMO_INTENTOS] lo da por fallido. Nunca cierra la app.
 */
internal suspend fun CoroutineWorker.ejecutarCorreo(
    nombre: String,
    accion: suspend (AppContainer) -> Unit
): ListenableWorker.Result {
    val container = (applicationContext as CliniMedikApp).container
    return try {
        accion(container)
        ListenableWorker.Result.success()
    } catch (error: CancellationException) {
        throw error
    } catch (error: ErrorCorreos) {
        Log.w(ETIQUETA_LOG, "$nombre: ${error.message}", error)
        if (error.reintentable && runAttemptCount < MAXIMO_INTENTOS) {
            ListenableWorker.Result.retry()
        } else {
            ListenableWorker.Result.failure()
        }
    } catch (error: Exception) {
        Log.e(ETIQUETA_LOG, "$nombre falló", error)
        ListenableWorker.Result.failure()
    }
}
