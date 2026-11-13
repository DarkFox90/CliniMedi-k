package plat.clinimedik.app.data.repository

import java.time.LocalDateTime
import plat.clinimedik.app.data.model.Cita
import plat.clinimedik.app.data.model.EstadoCita

/**
 * Qué hay que hacer con una cita en el Worker de correos.
 * En una cita, `recordatorioEnviado` indica que el Worker ya la tiene registrada.
 */
enum class AccionCorreo {
    /** Registrar la cita (nueva o reprogramada): el paciente recibe el correo de confirmación. */
    REGISTRAR,

    /** La cita se canceló y el Worker la tiene: hay que borrarla para que no reciba recordatorio. */
    RETIRAR,

    NINGUNA
}

fun accionCorreo(cita: Cita, ahora: LocalDateTime): AccionCorreo = when {
    cita.estado == EstadoCita.PROGRAMADA &&
        !cita.recordatorioEnviado &&
        !cita.correoPaciente.isNullOrBlank() &&
        cita.fechaHora.isAfter(ahora) -> AccionCorreo.REGISTRAR
    cita.estado == EstadoCita.CANCELADA && cita.recordatorioEnviado -> AccionCorreo.RETIRAR
    else -> AccionCorreo.NINGUNA
}
