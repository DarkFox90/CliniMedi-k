package plat.clinimedik.app.data.repository

import java.time.LocalDate
import java.time.LocalDateTime

internal fun inicioDelDia(fecha: LocalDate): LocalDateTime = fecha.atStartOfDay()

internal fun finDelDia(fecha: LocalDate): LocalDateTime = fecha.plusDays(1).atStartOfDay()
