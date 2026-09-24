package plat.clinimedik.app.data.model

import java.time.LocalDate
import java.time.LocalDateTime

data class Paciente (
    val id: String,
    val nombreCompleto: String,
    val fechaNacimiento: LocalDate,
    val telefono: String,
    val correo: String,
    val estadoCivil: EstadoCivil,
    val ocupacion: String,
    val alergias: String,
    val enfermedadesPrevias: String,
    val tratamientosPrevios: String,
    val cirugias: String,
    val fechaRegistro: LocalDateTime
)