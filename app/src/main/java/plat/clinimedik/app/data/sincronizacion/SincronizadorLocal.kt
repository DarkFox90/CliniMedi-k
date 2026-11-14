package plat.clinimedik.app.data.sincronizacion

import plat.clinimedik.app.data.local.entity.CitaEntity
import plat.clinimedik.app.data.local.entity.CobroEntity
import plat.clinimedik.app.data.local.entity.MedicoEntity
import plat.clinimedik.app.data.local.entity.PacienteEntity
import plat.clinimedik.app.data.local.entity.RecetaEntity
import plat.clinimedik.app.data.local.entity.ReferenciaEntity
import plat.clinimedik.app.data.local.entity.VisitaEntity

/** Implementación vacía: los datos se quedan solo en este teléfono. */
class SincronizadorLocal : Sincronizador {
    override fun iniciar() = Unit
    override fun detener() = Unit
    override fun publicarMedico(medico: MedicoEntity) = Unit
    override fun publicarPaciente(paciente: PacienteEntity) = Unit
    override fun publicarVisita(visita: VisitaEntity) = Unit
    override fun publicarCita(cita: CitaEntity) = Unit
    override fun publicarReceta(receta: RecetaEntity) = Unit
    override fun publicarReferencia(referencia: ReferenciaEntity) = Unit
    override fun publicarCobro(cobro: CobroEntity) = Unit
}
