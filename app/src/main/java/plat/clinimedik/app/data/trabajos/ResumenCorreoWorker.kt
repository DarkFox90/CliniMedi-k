package plat.clinimedik.app.data.trabajos

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import plat.clinimedik.app.data.model.EstadoVisita
import plat.clinimedik.app.data.remote.PacienteCorreo
import plat.clinimedik.app.data.remote.ResumenCorreo
import plat.clinimedik.app.data.remote.horaCorreo

/** Envía a la clínica el resumen del día: pacientes atendidos hoy y citas de mañana sin confirmar. */
class ResumenCorreoWorker(
    contexto: Context,
    parametros: WorkerParameters
) : CoroutineWorker(contexto, parametros) {

    override suspend fun doWork(): Result {
        val medicoId = inputData.getString(DATO_MEDICO) ?: return Result.failure()
        return ejecutarCorreo("Resumen del día") { container ->
            val hoy = LocalDate.now()
            val atendidos = container.visitaRepositorio.observarFilaDelDia(medicoId).first()
                .filter { it.visita.estado == EstadoVisita.ATENDIDO || it.visita.estado == EstadoVisita.REFERIDO }
                .sortedBy { it.visita.horaLlegada }
                .map { PacienteCorreo(it.paciente.nombreCompleto, horaCorreo(it.visita.horaLlegada)) }
            val sinConfirmar = container.citaRepositorio.observarSinConfirmar(medicoId, hoy.plusDays(1)).first()
                .map { PacienteCorreo(it.nombrePaciente, horaCorreo(it.fechaHora)) }
            val medico = container.medicoRepositorio.observarMedico(medicoId).first()
            container.servicioCorreos.enviarResumen(
                ResumenCorreo(
                    fecha = hoy.toString(),
                    medicoNombre = medico?.nombre ?: MEDICO_SIN_NOMBRE,
                    atendidos = atendidos.take(MAXIMO_POR_LISTA),
                    sinConfirmarManana = sinConfirmar.take(MAXIMO_POR_LISTA)
                )
            )
        }
    }

    companion object {
        private const val DATO_MEDICO = "medicoId"
        private const val MEDICO_SIN_NOMBRE = "Sin médico asignado"
        private const val MAXIMO_POR_LISTA = 200

        fun solicitud(medicoId: String): OneTimeWorkRequest =
            solicitudDeCorreo<ResumenCorreoWorker>(workDataOf(DATO_MEDICO to medicoId))
    }
}
