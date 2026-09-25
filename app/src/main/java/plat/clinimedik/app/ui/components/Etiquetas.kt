package plat.clinimedik.app.ui.components

import plat.clinimedik.app.data.model.EstadoCivil
import plat.clinimedik.app.data.model.EstadoConfirmacion
import plat.clinimedik.app.data.model.EstadoVisita
import plat.clinimedik.app.data.model.RolDispositivo
import plat.clinimedik.app.data.model.TipoDocumento
import plat.clinimedik.app.data.model.TipoPago

fun EstadoVisita.etiqueta(): String = when (this) {
    EstadoVisita.ESPERANDO -> "Esperando"
    EstadoVisita.EN_CONSULTA -> "En consulta"
    EstadoVisita.ATENDIDO -> "Atendido"
    EstadoVisita.REFERIDO -> "Referido"
}

fun TipoPago.etiqueta(): String = when (this) {
    TipoPago.PARTICULAR -> "Particular"
    TipoPago.SEGURO -> "Seguro médico"
    TipoPago.CORTESIA -> "Cortesía"
}

fun EstadoConfirmacion.etiqueta(): String = when (this) {
    EstadoConfirmacion.PENDIENTE -> "Pendiente"
    EstadoConfirmacion.CONFIRMADA -> "Confirmada"
    EstadoConfirmacion.NO_CONFIRMADA -> "No confirmada"
}

fun TipoDocumento.etiqueta(): String = when (this) {
    TipoDocumento.CARNET_SEGURO -> "Carnet de seguro"
    TipoDocumento.RECETA_ANTERIOR -> "Receta anterior"
    TipoDocumento.ORDEN_REFERENCIA -> "Orden de referencia"
    TipoDocumento.OTRO -> "Otro documento"
}

fun EstadoCivil.etiqueta(): String = when (this) {
    EstadoCivil.SOLTERO -> "Soltero(a)"
    EstadoCivil.CASADO -> "Casado(a)"
    EstadoCivil.UNION_DE_HECHO -> "Unión de hecho"
    EstadoCivil.DIVORCIADO -> "Divorciado(a)"
    EstadoCivil.VIUDO -> "Viudo(a)"
}

fun RolDispositivo.etiqueta(): String = when (this) {
    RolDispositivo.RECEPCION -> "Recepción"
    RolDispositivo.MEDICO -> "Médico"
}