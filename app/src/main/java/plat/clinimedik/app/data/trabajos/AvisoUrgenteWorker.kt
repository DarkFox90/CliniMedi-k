package plat.clinimedik.app.data.trabajos

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.flow.first
import plat.clinimedik.app.data.remote.AvisoUrgenteCorreo

/**
 * Avisa por correo a la clínica que un paciente se marcó como urgente.
 * Lleva el nombre, el médico y la hora; el motivo de la urgencia no sale del teléfono.
 */
class AvisoUrgenteWorker(
    contexto: Context,
    parametros: WorkerParameters
) : CoroutineWorker(contexto, parametros) {

    override suspend fun doWork(): Result {
        val visitaId = inputData.getString(DATO_VISITA) ?: return Result.failure()
        val hora = inputData.getString(DATO_HORA) ?: return Result.failure()
        return ejecutarCorreo("Aviso de urgente") { container ->
            val visita = container.visitaRepositorio.observarVisita(visitaId).first() ?: return@ejecutarCorreo
            if (!visita.visita.esUrgente) return@ejecutarCorreo
            val medico = container.medicoRepositorio.observarMedico(visita.visita.medicoId).first()
            container.servicioCorreos.avisarUrgente(
                AvisoUrgenteCorreo(
                    pacienteNombre = visita.paciente.nombreCompleto,
                    medicoNombre = medico?.nombre ?: MEDICO_SIN_NOMBRE,
                    hora = hora
                )
            )
        }
    }

    companion object {
        private const val DATO_VISITA = "visitaId"
        private const val DATO_HORA = "hora"
        private const val MEDICO_SIN_NOMBRE = "Sin médico asignado"

        fun solicitud(visitaId: String, hora: String): OneTimeWorkRequest =
            solicitudDeCorreo<AvisoUrgenteWorker>(workDataOf(DATO_VISITA to visitaId, DATO_HORA to hora))
    }
}
