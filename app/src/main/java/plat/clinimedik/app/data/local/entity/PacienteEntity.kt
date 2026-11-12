package plat.clinimedik.app.data.local.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime
import plat.clinimedik.app.data.model.EstadoCivil
import plat.clinimedik.app.data.model.Paciente

@Entity(
    tableName = "paciente"
)
data class PacienteEntity(
    @PrimaryKey val id: String,
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
    val fechaRegistro: LocalDateTime,
    val aseguradora: String?,
    val numeroPoliza: String?,
    val titularPoliza: String?
)

fun PacienteEntity.aModelo(): Paciente = Paciente(
    id = id,
    nombreCompleto = nombreCompleto,
    fechaNacimiento = fechaNacimiento,
    telefono = telefono,
    correo = correo,
    estadoCivil = estadoCivil,
    ocupacion = ocupacion,
    alergias = alergias,
    enfermedadesPrevias = enfermedadesPrevias,
    tratamientosPrevios = tratamientosPrevios,
    cirugias = cirugias,
    fechaRegistro = fechaRegistro,
    aseguradora = aseguradora,
    numeroPoliza = numeroPoliza,
    titularPoliza = titularPoliza
)

fun Paciente.aEntity(): PacienteEntity = PacienteEntity(
    id = id,
    nombreCompleto = nombreCompleto,
    fechaNacimiento = fechaNacimiento,
    telefono = telefono,
    correo = correo,
    estadoCivil = estadoCivil,
    ocupacion = ocupacion,
    alergias = alergias,
    enfermedadesPrevias = enfermedadesPrevias,
    tratamientosPrevios = tratamientosPrevios,
    cirugias = cirugias,
    fechaRegistro = fechaRegistro,
    aseguradora = aseguradora,
    numeroPoliza = numeroPoliza,
    titularPoliza = titularPoliza
)
