package plat.clinimedik.app.ui.screens.recepcion.detalle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import plat.clinimedik.app.data.fake.FakeDataSource
import plat.clinimedik.app.ui.components.calcularEdad
import plat.clinimedik.app.ui.components.formatearFecha

fun detalleUiStateDePrueba(
    visitaId: String = "vis-102",
    panel: PanelDetalle? = null
): DetalleUiState {
    val visita = FakeDataSource.visitas.firstOrNull { it.id == visitaId } ?: return DetalleUiState()
    val paciente = FakeDataSource.paciente(visita.pacienteId) ?: return DetalleUiState()
    val edad = calcularEdad(paciente.fechaNacimiento, FakeDataSource.hoy)

    val documentos = FakeDataSource.documentosDe(paciente.id).map { documento ->
        DocumentoDetalle(
            id = documento.id,
            tipo = documento.tipo,
            fecha = formatearFecha(documento.fecha.toLocalDate())
        )
    }

    val visitasAnteriores = FakeDataSource.historialDe(paciente.id)
        .filter { it.horaLlegada.toLocalDate().isBefore(FakeDataSource.hoy) }
        .map { anterior ->
            VisitaAnterior(
                id = anterior.id,
                fecha = formatearFecha(anterior.horaLlegada.toLocalDate()),
                motivo = anterior.motivoConsulta,
                diagnostico = anterior.diagnostico ?: "Sin diagnóstico registrado"
            )
        }

    return DetalleUiState(
        visitaId = visita.id,
        nombre = paciente.nombreCompleto,
        datosContacto = "$edad años · ${paciente.telefono}",
        motivoConsulta = visita.motivoConsulta,
        estado = visita.estado,
        esUrgente = visita.esUrgente,
        motivoPrioridad = visita.motivoPrioridad,
        alergias = paciente.alergias,
        enfermedadesPrevias = paciente.enfermedadesPrevias,
        documentos = documentos,
        visitasAnteriores = visitasAnteriores,
        panel = panel
    )
}

@Composable
fun DetalleRoute(visitaId: String = "vis-102") {
    val state = remember(visitaId) { detalleUiStateDePrueba(visitaId) }
    DetalleScreen(state = state, onIntent = {})
}