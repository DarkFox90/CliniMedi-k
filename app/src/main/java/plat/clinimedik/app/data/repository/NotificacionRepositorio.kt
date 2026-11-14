package plat.clinimedik.app.data.repository

import android.content.Context
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import java.time.LocalDateTime
import kotlinx.coroutines.CancellationException
import plat.clinimedik.app.data.remote.ServicioCorreos
import plat.clinimedik.app.data.remote.horaCorreo
import plat.clinimedik.app.data.trabajos.AvisoUrgenteWorker
import plat.clinimedik.app.data.trabajos.ConsultarConfirmacionesWorker
import plat.clinimedik.app.data.trabajos.ResumenCorreoWorker
import plat.clinimedik.app.data.trabajos.SincronizarCitaWorker

/**
 * Programa los correos con WorkManager. Cada trabajo espera a que haya internet y se reintenta solo
 * si falla, así que guardar una cita nunca depende de la conexión.
 * Si el teléfono no tiene configurado el Worker de correos, no programa nada.
 */
class NotificacionRepositorio(
    context: Context,
    private val servicioCorreos: ServicioCorreos
) {

    private val workManager by lazy { WorkManager.getInstance(context) }

    val activo: Boolean
        get() = servicioCorreos.activo

    /**
     * Pone al día la cita en el Worker según su estado actual en Room. Los trabajos de una misma
     * cita se encadenan en orden, por eso crear y cancelar seguidos sin internet llegan bien.
     */
    fun sincronizarCita(citaId: String) {
        encolar("correo-cita-$citaId", ExistingWorkPolicy.APPEND_OR_REPLACE, SincronizarCitaWorker.solicitud(citaId))
    }

    fun consultarConfirmaciones() {
        encolar(TRABAJO_CONFIRMACIONES, ExistingWorkPolicy.KEEP, ConsultarConfirmacionesWorker.solicitud())
    }

    fun avisarUrgente(visitaId: String) {
        val hora = horaCorreo(LocalDateTime.now())
        encolar("aviso-urgente-$visitaId", ExistingWorkPolicy.KEEP, AvisoUrgenteWorker.solicitud(visitaId, hora))
    }

    /** Devuelve `false` si este teléfono no puede enviar correos. */
    fun enviarResumen(medicoId: String): Boolean {
        if (!activo) return false
        encolar("resumen-$medicoId", ExistingWorkPolicy.KEEP, ResumenCorreoWorker.solicitud(medicoId))
        return true
    }

    private fun encolar(nombre: String, politica: ExistingWorkPolicy, solicitud: OneTimeWorkRequest) {
        if (!activo) return
        try {
            workManager.enqueueUniqueWork(nombre, politica, solicitud)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Log.w("Correos", "No se pudo programar el trabajo $nombre", error)
        }
    }

    private companion object {
        const val TRABAJO_CONFIRMACIONES = "consultar-confirmaciones"
    }
}
