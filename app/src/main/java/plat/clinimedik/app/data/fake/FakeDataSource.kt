package plat.clinimedik.app.data.fake

import plat.clinimedik.app.data.model.Cita
import plat.clinimedik.app.data.model.Cobro
import plat.clinimedik.app.data.model.Documento
import plat.clinimedik.app.data.model.EstadoCivil
import plat.clinimedik.app.data.model.EstadoConfirmacion
import plat.clinimedik.app.data.model.EstadoVisita
import plat.clinimedik.app.data.model.Medico
import plat.clinimedik.app.data.model.Paciente
import plat.clinimedik.app.data.model.Receta
import plat.clinimedik.app.data.model.Referencia
import plat.clinimedik.app.data.model.TipoDocumento
import plat.clinimedik.app.data.model.TipoPago
import plat.clinimedik.app.data.model.Visita
import java.time.LocalDate
import java.time.LocalDateTime
import java.text.Normalizer

object FakeDataSource {

    val ahora: LocalDateTime = LocalDateTime.now().withSecond(0).withNano(0)
    val hoy: LocalDate = ahora.toLocalDate()

    val medicos = listOf(
        Medico(id = "med-001", nombre = "Dr. Roberto Sical", especialidad = "Medicina interna"),
        Medico(id = "med-002", nombre = "Dra. Ana Lucía Paz", especialidad = "Cardiología")
    )

    val medicoActual: Medico = medicos.first()

    val pacientes = listOf(
        Paciente(
            id = "pac-001",
            nombreCompleto = "Álvaro Chacón",
            fechaNacimiento = LocalDate.of(1984, 5, 12),
            telefono = "5512-7788",
            correo = "alvaro.chacon@gmail.com",
            estadoCivil = EstadoCivil.CASADO,
            ocupacion = "Contador",
            alergias = "Penicilina",
            enfermedadesPrevias = "Hipertensión arterial",
            tratamientosPrevios = "Losartán 50 mg diario",
            cirugias = "Ninguna",
            fechaRegistro = LocalDateTime.of(2025, 10, 18, 8, 30)
        ),
        Paciente(
            id = "pac-002",
            nombreCompleto = "Marta Ríos",
            fechaNacimiento = LocalDate.of(1992, 2, 20),
            telefono = "5599-8877",
            correo = "marta.rios@gmail.com",
            estadoCivil = EstadoCivil.SOLTERO,
            ocupacion = "Maestra",
            alergias = "Ninguna conocida",
            enfermedadesPrevias = "Ninguna",
            tratamientosPrevios = "Ninguno",
            cirugias = "Apendicectomía en 2015",
            fechaRegistro = LocalDateTime.of(2026, 1, 2, 9, 15)
        ),
        Paciente(
            id = "pac-003",
            nombreCompleto = "Julio Estrada",
            fechaNacimiento = LocalDate.of(1969, 7, 3),
            telefono = "4477-1122",
            correo = "julio.estrada@yahoo.com",
            estadoCivil = EstadoCivil.CASADO,
            ocupacion = "Comerciante",
            alergias = "Ninguna conocida",
            enfermedadesPrevias = "Diabetes tipo 2",
            tratamientosPrevios = "Metformina 850 mg",
            cirugias = "Ninguna",
            fechaRegistro = LocalDateTime.of(2025, 6, 10, 10, 0)
        ),
        Paciente(
            id = "pac-004",
            nombreCompleto = "Carla Méndez",
            fechaNacimiento = LocalDate.of(1997, 3, 15),
            telefono = "4433-2211",
            correo = "carla.mendez@gmail.com",
            estadoCivil = EstadoCivil.UNION_DE_HECHO,
            ocupacion = "Diseñadora gráfica",
            alergias = "Sulfas",
            enfermedadesPrevias = "Ninguna",
            tratamientosPrevios = "Ninguno",
            cirugias = "Ninguna",
            fechaRegistro = LocalDateTime.of(2025, 12, 5, 11, 20)
        ),
        Paciente(
            id = "pac-005",
            nombreCompleto = "Roberto Ixchop",
            fechaNacimiento = LocalDate.of(1965, 1, 22),
            telefono = "5512-3344",
            correo = "roberto.ixchop@gmail.com",
            estadoCivil = EstadoCivil.VIUDO,
            ocupacion = "Jubilado",
            alergias = "Ninguna conocida",
            enfermedadesPrevias = "Artritis",
            tratamientosPrevios = "Ibuprofeno ocasional",
            cirugias = "Cirugía de rodilla en 2019",
            fechaRegistro = LocalDateTime.of(2025, 11, 20, 8, 45)
        ),
        Paciente(
            id = "pac-006",
            nombreCompleto = "Lucía Ajú",
            fechaNacimiento = LocalDate.of(2001, 9, 2),
            telefono = "3021-5566",
            correo = "lucia.aju@gmail.com",
            estadoCivil = EstadoCivil.SOLTERO,
            ocupacion = "Estudiante",
            alergias = "Ninguna conocida",
            enfermedadesPrevias = "Migraña",
            tratamientosPrevios = "Ninguno",
            cirugias = "Ninguna",
            fechaRegistro = ahora.minusMinutes(8)
        ),
        Paciente(
            id = "pac-007",
            nombreCompleto = "Pedro Coc",
            fechaNacimiento = LocalDate.of(1978, 11, 10),
            telefono = "5877-9900",
            correo = "pedro.coc@gmail.com",
            estadoCivil = EstadoCivil.DIVORCIADO,
            ocupacion = "Ingeniero civil",
            alergias = "Mariscos",
            enfermedadesPrevias = "Ninguna",
            tratamientosPrevios = "Ninguno",
            cirugias = "Ninguna",
            fechaRegistro = LocalDateTime.of(2026, 4, 8, 14, 0)
        ),
        Paciente(
            id = "pac-008",
            nombreCompleto = "Sergio Ramírez",
            fechaNacimiento = LocalDate.of(1988, 8, 14),
            telefono = "5234-6677",
            correo = "sergio.ramirez@gmail.com",
            estadoCivil = EstadoCivil.CASADO,
            ocupacion = "Mecánico",
            alergias = "Ninguna conocida",
            enfermedadesPrevias = "Ninguna",
            tratamientosPrevios = "Ninguno",
            cirugias = "Ninguna",
            fechaRegistro = LocalDateTime.of(2026, 5, 3, 10, 30)
        )
    )

    val visitasDeHoy = listOf(
        Visita(
            id = "vis-101",
            pacienteId = "pac-001",
            medicoId = "med-001",
            motivoConsulta = "Dolor torácico",
            horaLlegada = ahora.minusMinutes(20),
            estado = EstadoVisita.ESPERANDO,
            esUrgente = true,
            motivoPrioridad = "Dolor torácico con dificultad para respirar"
        ),
        Visita(
            id = "vis-102",
            pacienteId = "pac-002",
            medicoId = "med-001",
            motivoConsulta = "Control de presión",
            horaLlegada = ahora.minusMinutes(12),
            estado = EstadoVisita.ESPERANDO
        ),
        Visita(
            id = "vis-103",
            pacienteId = "pac-006",
            medicoId = "med-001",
            motivoConsulta = "Primera consulta por migraña",
            horaLlegada = ahora.minusMinutes(5),
            estado = EstadoVisita.ESPERANDO
        ),
        Visita(
            id = "vis-104",
            pacienteId = "pac-003",
            medicoId = "med-001",
            motivoConsulta = "Chequeo general",
            horaLlegada = ahora.minusMinutes(45),
            estado = EstadoVisita.EN_CONSULTA,
            horaInicioConsulta = ahora.minusMinutes(10)
        ),
        Visita(
            id = "vis-105",
            pacienteId = "pac-004",
            medicoId = "med-001",
            motivoConsulta = "Palpitaciones",
            horaLlegada = ahora.minusMinutes(95),
            estado = EstadoVisita.REFERIDO,
            horaInicioConsulta = ahora.minusMinutes(75),
            horaFin = ahora.minusMinutes(60),
            diagnostico = "Soplo cardíaco en estudio",
            notas = "Se refiere a cardiología para ecocardiograma."
        ),
        Visita(
            id = "vis-106",
            pacienteId = "pac-005",
            medicoId = "med-001",
            motivoConsulta = "Dolor de garganta",
            horaLlegada = ahora.minusMinutes(130),
            estado = EstadoVisita.ATENDIDO,
            horaInicioConsulta = ahora.minusMinutes(110),
            horaFin = ahora.minusMinutes(95),
            diagnostico = "Faringitis bacteriana",
            notas = "Control en una semana si persiste la fiebre."
        ),
        Visita(
            id = "vis-107",
            pacienteId = "pac-007",
            medicoId = "med-002",
            motivoConsulta = "Evaluación cardiológica",
            horaLlegada = ahora.minusMinutes(15),
            estado = EstadoVisita.ESPERANDO
        ),
        Visita(
            id = "vis-108",
            pacienteId = "pac-008",
            medicoId = "med-001",
            motivoConsulta = "Dolor lumbar",
            horaLlegada = ahora.minusMinutes(150),
            estado = EstadoVisita.ATENDIDO,
            horaInicioConsulta = ahora.minusMinutes(140),
            horaFin = ahora.minusMinutes(125),
            diagnostico = "Contractura muscular lumbar",
            notas = "Aplicar calor local y evitar cargar peso por una semana."
        )
    )

    val visitasAnteriores = listOf(
        Visita(
            id = "vis-201",
            pacienteId = "pac-001",
            medicoId = "med-001",
            motivoConsulta = "Control de presión",
            horaLlegada = LocalDateTime.of(2026, 3, 14, 9, 0),
            estado = EstadoVisita.ATENDIDO,
            horaInicioConsulta = LocalDateTime.of(2026, 3, 14, 9, 20),
            horaFin = LocalDateTime.of(2026, 3, 14, 9, 40),
            diagnostico = "Hipertensión arterial controlada",
            notas = "Presión 138/86. Se indica reducir consumo de sal y control en 4 semanas."
        ),
        Visita(
            id = "vis-202",
            pacienteId = "pac-001",
            medicoId = "med-001",
            motivoConsulta = "Consulta general",
            horaLlegada = LocalDateTime.of(2026, 1, 2, 10, 0),
            estado = EstadoVisita.ATENDIDO,
            horaInicioConsulta = LocalDateTime.of(2026, 1, 2, 10, 15),
            horaFin = LocalDateTime.of(2026, 1, 2, 10, 30),
            diagnostico = "Cuadro viral leve, sin datos de alarma",
            notas = "Reposo e hidratación. Acudir si hay fiebre persistente mayor a 3 días."
        ),
        Visita(
            id = "vis-203",
            pacienteId = "pac-001",
            medicoId = "med-001",
            motivoConsulta = "Chequeo anual",
            horaLlegada = LocalDateTime.of(2025, 10, 18, 8, 30),
            estado = EstadoVisita.ATENDIDO,
            horaInicioConsulta = LocalDateTime.of(2025, 10, 18, 8, 45),
            horaFin = LocalDateTime.of(2025, 10, 18, 9, 10),
            diagnostico = "Paciente sano, laboratorios dentro de rango",
            notas = "Perfil lipídico y glucosa en ayunas normales. Próximo chequeo en 12 meses."
        ),
        Visita(
            id = "vis-204",
            pacienteId = "pac-002",
            medicoId = "med-001",
            motivoConsulta = "Dolor de cabeza frecuente",
            horaLlegada = LocalDateTime.of(2026, 1, 2, 9, 15),
            estado = EstadoVisita.ATENDIDO,
            horaInicioConsulta = LocalDateTime.of(2026, 1, 2, 9, 30),
            horaFin = LocalDateTime.of(2026, 1, 2, 9, 50),
            diagnostico = "Presión arterial elevada",
            notas = "Iniciar control de presión cada mes."
        ),
        Visita(
            id = "vis-205",
            pacienteId = "pac-005",
            medicoId = "med-001",
            motivoConsulta = "Dolor de rodilla",
            horaLlegada = LocalDateTime.of(2026, 2, 2, 11, 0),
            estado = EstadoVisita.ATENDIDO,
            horaInicioConsulta = LocalDateTime.of(2026, 2, 2, 11, 10),
            horaFin = LocalDateTime.of(2026, 2, 2, 11, 30),
            diagnostico = "Artritis en rodilla derecha",
            notas = "Fisioterapia dos veces por semana."
        )
    )

    val visitas: List<Visita> = visitasDeHoy + visitasAnteriores

    val recetas = listOf(
        Receta(
            id = "rec-001",
            visitaId = "vis-201",
            medicamento = "Losartán 50 mg",
            dosis = "1 tableta",
            frecuencia = "Cada 24 horas",
            duracion = "30 días"
        ),
        Receta(
            id = "rec-002",
            visitaId = "vis-106",
            medicamento = "Amoxicilina 500 mg",
            dosis = "1 cápsula",
            frecuencia = "Cada 8 horas",
            duracion = "7 días"
        ),
        Receta(
            id = "rec-003",
            visitaId = "vis-106",
            medicamento = "Ibuprofeno 400 mg",
            dosis = "1 tableta",
            frecuencia = "Cada 8 horas si hay dolor",
            duracion = "3 días"
        ),
        Receta(
            id = "rec-004",
            visitaId = "vis-205",
            medicamento = "Diclofenaco 50 mg",
            dosis = "1 tableta",
            frecuencia = "Cada 12 horas",
            duracion = "10 días"
        )
    )

    val referencias = listOf(
        Referencia(
            id = "ref-001",
            visitaId = "vis-105",
            especialidad = "Cardiología",
            destino = "Dra. Ana Lucía Paz",
            motivo = "Soplo cardíaco, requiere ecocardiograma"
        )
    )

    val cobros = listOf(
        Cobro(id = "cob-001", visitaId = "vis-106", tipoPago = TipoPago.PARTICULAR, monto = 350.0),
        Cobro(id = "cob-002", visitaId = "vis-105", tipoPago = TipoPago.SEGURO, monto = 300.0),
        Cobro(id = "cob-003", visitaId = "vis-201", tipoPago = TipoPago.PARTICULAR, monto = 300.0),
        Cobro(id = "cob-004", visitaId = "vis-202", tipoPago = TipoPago.CORTESIA, monto = 0.0)
    )

    val documentos = listOf(
        Documento(
            id = "doc-001",
            pacienteId = "pac-001",
            tipo = TipoDocumento.CARNET_SEGURO,
            rutaArchivo = "",
            fecha = LocalDateTime.of(2025, 10, 18, 8, 35)
        ),
        Documento(
            id = "doc-002",
            pacienteId = "pac-001",
            tipo = TipoDocumento.RECETA_ANTERIOR,
            rutaArchivo = "",
            fecha = LocalDateTime.of(2026, 3, 14, 9, 5)
        ),
        Documento(
            id = "doc-003",
            pacienteId = "pac-004",
            tipo = TipoDocumento.ORDEN_REFERENCIA,
            rutaArchivo = "",
            fecha = ahora.minusMinutes(58)
        )
    )

    val citas = listOf(
        Cita(
            id = "cit-001",
            pacienteId = "pac-002",
            nombrePaciente = "Marta Ríos",
            correoPaciente = "marta.rios@gmail.com",
            medicoId = "med-001",
            fechaHora = hoy.atTime(9, 30),
            duracionMinutos = 30,
            motivo = "Control de presión",
            estadoConfirmacion = EstadoConfirmacion.CONFIRMADA,
            recordatorioEnviado = true
        ),
        Cita(
            id = "cit-002",
            pacienteId = "pac-003",
            nombrePaciente = "Julio Estrada",
            correoPaciente = "julio.estrada@yahoo.com",
            medicoId = "med-001",
            fechaHora = hoy.atTime(10, 15),
            duracionMinutos = 30,
            motivo = "Chequeo general",
            estadoConfirmacion = EstadoConfirmacion.CONFIRMADA,
            recordatorioEnviado = true
        ),
        Cita(
            id = "cit-003",
            pacienteId = null,
            nombrePaciente = "Sofía Hernández",
            correoPaciente = "sofia.hernandez@gmail.com",
            medicoId = "med-001",
            fechaHora = hoy.atTime(11, 0),
            duracionMinutos = 30,
            motivo = "Primera consulta",
            estadoConfirmacion = EstadoConfirmacion.PENDIENTE,
            recordatorioEnviado = true
        ),
        Cita(
            id = "cit-004",
            pacienteId = "pac-007",
            nombrePaciente = "Pedro Coc",
            correoPaciente = "pedro.coc@gmail.com",
            medicoId = "med-001",
            fechaHora = hoy.atTime(15, 0),
            duracionMinutos = 30,
            motivo = "Resultados de laboratorio",
            estadoConfirmacion = EstadoConfirmacion.NO_CONFIRMADA,
            recordatorioEnviado = true
        ),
        Cita(
            id = "cit-005",
            pacienteId = "pac-001",
            nombrePaciente = "Álvaro Chacón",
            correoPaciente = "alvaro.chacon@gmail.com",
            medicoId = "med-001",
            fechaHora = hoy.plusDays(1).atTime(9, 0),
            duracionMinutos = 30,
            motivo = "Control de presión",
            estadoConfirmacion = EstadoConfirmacion.PENDIENTE,
            recordatorioEnviado = false
        )
    )

    fun paciente(id: String): Paciente? = pacientes.find { it.id == id }

    fun medico(id: String): Medico? = medicos.find { it.id == id }

    private fun prioridadEnFila(visita: Visita): Int = when {
        visita.estado == EstadoVisita.ESPERANDO && visita.esUrgente -> 0
        visita.estado == EstadoVisita.ESPERANDO -> 1
        visita.estado == EstadoVisita.EN_CONSULTA -> 2
        visita.estado == EstadoVisita.REFERIDO -> 3
        else -> 4
    }

    fun filaDeHoy(medicoId: String): List<Visita> =
        visitasDeHoy
            .filter { it.medicoId == medicoId }
            .sortedWith(compareBy<Visita> { prioridadEnFila(it) }.thenBy { it.horaLlegada })

    fun historialDe(pacienteId: String): List<Visita> =
        visitas
            .filter { it.pacienteId == pacienteId }
            .sortedByDescending { it.horaLlegada }

    fun recetasDe(visitaId: String): List<Receta> = recetas.filter { it.visitaId == visitaId }

    fun referenciaDe(visitaId: String): Referencia? = referencias.find { it.visitaId == visitaId }

    fun cobroDe(visitaId: String): Cobro? = cobros.find { it.visitaId == visitaId }

    fun documentosDe(pacienteId: String): List<Documento> = documentos.filter { it.pacienteId == pacienteId }

    fun citasDelDia(medicoId: String, fecha: LocalDate = hoy): List<Cita> =
        citas
            .filter { it.medicoId == medicoId && it.fechaHora.toLocalDate() == fecha }
            .sortedBy { it.fechaHora }

    private fun normalizar(texto: String): String =
        Normalizer.normalize(texto, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .lowercase()

    fun buscarPacientes(texto: String): List<Paciente> {
        val busqueda = normalizar(texto.trim())
        val digitos = busqueda.filter { it.isDigit() }
        return pacientes.filter { paciente ->
            normalizar(paciente.nombreCompleto).contains(busqueda) ||
                    (digitos.isNotEmpty() && paciente.telefono.filter { it.isDigit() }.contains(digitos))
        }
    }

    fun esPacienteNuevo(pacienteId: String): Boolean =
        paciente(pacienteId)?.fechaRegistro?.toLocalDate() == hoy
}