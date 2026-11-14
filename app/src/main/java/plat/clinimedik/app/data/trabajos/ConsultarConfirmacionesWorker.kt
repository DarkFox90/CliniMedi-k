package plat.clinimedik.app.data.trabajos

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkerParameters

/** Trae del Worker las citas que el paciente confirmó con el enlace del correo y las marca en Room. */
class ConsultarConfirmacionesWorker(
    contexto: Context,
    parametros: WorkerParameters
) : CoroutineWorker(contexto, parametros) {

    override suspend fun doWork(): Result = ejecutarCorreo("Consultar confirmaciones") { container ->
        val confirmadas = container.servicioCorreos.consultarConfirmaciones()
        container.citaRepositorio.aplicarConfirmaciones(confirmadas)
    }

    companion object {
        fun solicitud(): OneTimeWorkRequest = solicitudDeCorreo<ConsultarConfirmacionesWorker>()
    }
}
