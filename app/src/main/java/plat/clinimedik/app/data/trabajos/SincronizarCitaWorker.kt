package plat.clinimedik.app.data.trabajos

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import java.time.LocalDateTime
import kotlinx.coroutines.flow.first
import plat.clinimedik.app.data.model.EstadoCita
import plat.clinimedik.app.data.model.EstadoConfirmacion
import plat.clinimedik.app.data.remote.aCitaCorreo
import plat.clinimedik.app.data.repository.AccionCorreo
import plat.clinimedik.app.data.repository.accionCorreo

/**
 * Pone al día una cita en el Worker de correos. Lee el estado actual de la cita en Room,
 * por eso da igual cuántas veces se ejecute o cuánto tarde en haber internet.
 */
class SincronizarCitaWorker(
    contexto: Context,
    parametros: WorkerParameters
) : CoroutineWorker(contexto, parametros) {

    override suspend fun doWork(): Result {
        val citaId = inputData.getString(DATO_CITA) ?: return Result.failure()
        return ejecutarCorreo("Sincronizar cita") { container ->
            val citas = container.citaRepositorio
            val cita = citas.obtenerCita(citaId) ?: return@ejecutarCorreo
            when (accionCorreo(cita, LocalDateTime.now())) {
                AccionCorreo.REGISTRAR -> {
                    val medico = container.medicoRepositorio.observarMedico(cita.medicoId).first()
                    container.servicioCorreos.registrarCita(cita.aCitaCorreo(medico?.nombre ?: MEDICO_SIN_NOMBRE))
                    citas.marcarCorreoRegistrado(citaId, cita.fechaHora)
                }
                AccionCorreo.RETIRAR -> {
                    container.servicioCorreos.cancelarCita(citaId)
                    citas.marcarCorreoRetirado(citaId)
                }
                AccionCorreo.NINGUNA -> Unit
            }
            val actual = citas.obtenerCita(citaId)
            if (actual != null &&
                actual.estado == EstadoCita.PROGRAMADA &&
                actual.recordatorioEnviado &&
                actual.estadoConfirmacion == EstadoConfirmacion.CONFIRMADA
            ) {
                container.servicioCorreos.marcarConfirmada(citaId)
            }
        }
    }

    companion object {
        private const val DATO_CITA = "citaId"
        private const val MEDICO_SIN_NOMBRE = "tu médico"

        fun solicitud(citaId: String): OneTimeWorkRequest =
            solicitudDeCorreo<SincronizarCitaWorker>(workDataOf(DATO_CITA to citaId))
    }
}
