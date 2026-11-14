package plat.clinimedik.app.data.sincronizacion

import plat.clinimedik.app.data.local.entity.CitaEntity
import plat.clinimedik.app.data.local.entity.CobroEntity
import plat.clinimedik.app.data.local.entity.MedicoEntity
import plat.clinimedik.app.data.local.entity.PacienteEntity
import plat.clinimedik.app.data.local.entity.RecetaEntity
import plat.clinimedik.app.data.local.entity.ReferenciaEntity
import plat.clinimedik.app.data.local.entity.VisitaEntity

/**
 * Copia hacia otros teléfonos lo que se guarda en Room.
 * Los repositorios llaman a `publicar...` después de cada escritura.
 * Las funciones no son `suspend`: no deben esperar la respuesta del servidor.
 */
interface Sincronizador {
    fun iniciar()
    fun detener()
    fun publicarMedico(medico: MedicoEntity)
    fun publicarPaciente(paciente: PacienteEntity)
    fun publicarVisita(visita: VisitaEntity)
    fun publicarCita(cita: CitaEntity)
    fun publicarReceta(receta: RecetaEntity)
    fun publicarReferencia(referencia: ReferenciaEntity)
    fun publicarCobro(cobro: CobroEntity)
}
