package plat.clinimedik.app.data.repository

import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Test
import plat.clinimedik.app.data.model.Cita
import plat.clinimedik.app.data.model.EstadoCita
import plat.clinimedik.app.data.model.EstadoConfirmacion

class AccionCorreoTest {

    private val ahora = LocalDateTime.of(2026, 11, 12, 10, 0)

    private val cita = Cita(
        id = "cit-1",
        pacienteId = null,
        nombrePaciente = "Ana López",
        correoPaciente = "ana@example.com",
        medicoId = "med-001",
        fechaHora = ahora.plusDays(2),
        duracionMinutos = 30,
        motivo = "Control",
        estadoConfirmacion = EstadoConfirmacion.PENDIENTE,
        recordatorioEnviado = false
    )

    @Test
    fun citaNuevaConCorreoSeRegistra() {
        assertEquals(AccionCorreo.REGISTRAR, accionCorreo(cita, ahora))
    }

    @Test
    fun citaYaRegistradaNoSeRepite() {
        assertEquals(AccionCorreo.NINGUNA, accionCorreo(cita.copy(recordatorioEnviado = true), ahora))
    }

    @Test
    fun citaSinCorreoNoSeRegistra() {
        assertEquals(AccionCorreo.NINGUNA, accionCorreo(cita.copy(correoPaciente = null), ahora))
        assertEquals(AccionCorreo.NINGUNA, accionCorreo(cita.copy(correoPaciente = "  "), ahora))
    }

    @Test
    fun citaQueYaPasoNoSeRegistra() {
        assertEquals(AccionCorreo.NINGUNA, accionCorreo(cita.copy(fechaHora = ahora.minusHours(1)), ahora))
    }

    @Test
    fun citaCanceladaYRegistradaSeRetira() {
        val cancelada = cita.copy(estado = EstadoCita.CANCELADA, recordatorioEnviado = true)
        assertEquals(AccionCorreo.RETIRAR, accionCorreo(cancelada, ahora))
    }

    @Test
    fun citaCanceladaSinRegistrarNoHaceNada() {
        assertEquals(AccionCorreo.NINGUNA, accionCorreo(cita.copy(estado = EstadoCita.CANCELADA), ahora))
    }

    @Test
    fun citaCompletadaNoHaceNada() {
        assertEquals(AccionCorreo.NINGUNA, accionCorreo(cita.copy(estado = EstadoCita.COMPLETADA), ahora))
    }
}
