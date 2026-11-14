import { textoDia, textoFecha, textoHora } from './fechas.js';

/**
 * Plantillas de los correos y envío con Resend.
 * Los correos al paciente llevan solo nombre, médico, fecha y hora: nunca datos clínicos.
 */

const REMITENTE = 'CliniMedi-k <citas@clinimedik.dev>';
const URL_RESEND = 'https://api.resend.com/emails';
const COLOR = '#00695c';

export class ErrorEnvio extends Error {
	constructor(estado, detalle) {
		super(`Resend respondió ${estado}: ${detalle}`);
		this.estado = estado;
	}
}

export function escaparHtml(texto) {
	return String(texto)
		.replaceAll('&', '&amp;')
		.replaceAll('<', '&lt;')
		.replaceAll('>', '&gt;')
		.replaceAll('"', '&quot;')
		.replaceAll("'", '&#39;');
}

/** Envía un correo. `reply_to` es el correo de la clínica, porque citas@clinimedik.dev no tiene buzón. */
export async function enviarCorreo(env, { para, asunto, html, texto }) {
	const respuesta = await fetch(URL_RESEND, {
		method: 'POST',
		headers: {
			Authorization: `Bearer ${env.RESEND_API_KEY}`,
			'Content-Type': 'application/json',
		},
		body: JSON.stringify({
			from: REMITENTE,
			to: [para],
			reply_to: env.CORREO_CLINICA,
			subject: asunto,
			html,
			text: texto,
		}),
	});
	if (!respuesta.ok) {
		throw new ErrorEnvio(respuesta.status, await respuesta.text());
	}
}

function marco(titulo, cuerpoHtml) {
	return `<!DOCTYPE html>
<html lang="es"><body style="margin:0;padding:24px;background:#f4f7f6;font-family:Arial,Helvetica,sans-serif;color:#1b1b1b">
<div style="max-width:520px;margin:0 auto;background:#ffffff;border-radius:12px;padding:24px">
<p style="margin:0 0 16px;font-weight:bold;color:${COLOR}">CliniMedi-k</p>
<h1 style="margin:0 0 16px;font-size:20px">${escaparHtml(titulo)}</h1>
${cuerpoHtml}
</div></body></html>`;
}

function boton(enlace, texto) {
	return `<p style="margin:24px 0"><a href="${escaparHtml(enlace)}" style="background:${COLOR};color:#ffffff;padding:12px 20px;border-radius:24px;text-decoration:none;font-weight:bold">${escaparHtml(texto)}</a></p>`;
}

function detallesCita(cita) {
	const fecha = new Date(cita.fechaHora);
	return {
		fecha: textoFecha(fecha),
		hora: textoHora(fecha),
	};
}

export function enlaceConfirmacion(cita) {
	return `${cita.origen}/confirmar?token=${encodeURIComponent(cita.token)}`;
}

export function correoConfirmacion(cita) {
	const { fecha, hora } = detallesCita(cita);
	const enlace = enlaceConfirmacion(cita);
	const cuerpo = `<p>Hola, ${escaparHtml(cita.pacienteNombre)}:</p>
<p>Tienes una cita con <strong>${escaparHtml(cita.medicoNombre)}</strong> el <strong>${fecha}</strong> a las <strong>${hora}</strong>.</p>
<p>Por favor, confirma tu asistencia.</p>
${boton(enlace, 'Confirmar mi cita')}
<p style="color:#555555;font-size:14px">Si necesitas cambiar o cancelar la cita, responde a este correo.</p>`;
	return {
		para: cita.pacienteCorreo,
		asunto: `Confirma tu cita del ${fecha}`,
		html: marco('Confirma tu cita', cuerpo),
		texto: `Hola, ${cita.pacienteNombre}:\n\nTienes una cita con ${cita.medicoNombre} el ${fecha} a las ${hora}.\nConfirma tu asistencia en este enlace: ${enlace}\n\nSi necesitas cambiar o cancelar la cita, responde a este correo.`,
	};
}

export function correoRecordatorio(cita) {
	const { fecha, hora } = detallesCita(cita);
	const enlace = enlaceConfirmacion(cita);
	const pedido = cita.confirmadaEn
		? '<p>Tu asistencia ya está confirmada. ¡Te esperamos!</p>'
		: `<p>Todavía no has confirmado tu asistencia.</p>${boton(enlace, 'Confirmar mi cita')}`;
	const pedidoTexto = cita.confirmadaEn
		? 'Tu asistencia ya está confirmada.'
		: `Todavía no has confirmado tu asistencia. Confírmala en este enlace: ${enlace}`;
	const cuerpo = `<p>Hola, ${escaparHtml(cita.pacienteNombre)}:</p>
<p>Te recordamos tu cita de mañana con <strong>${escaparHtml(cita.medicoNombre)}</strong>, el <strong>${fecha}</strong> a las <strong>${hora}</strong>.</p>
${pedido}
<p style="color:#555555;font-size:14px">Si necesitas cambiar o cancelar la cita, responde a este correo.</p>`;
	return {
		para: cita.pacienteCorreo,
		asunto: `Recordatorio: tu cita de mañana a las ${hora}`,
		html: marco('Recordatorio de tu cita', cuerpo),
		texto: `Hola, ${cita.pacienteNombre}:\n\nTe recordamos tu cita de mañana con ${cita.medicoNombre}, el ${fecha} a las ${hora}.\n${pedidoTexto}\n\nSi necesitas cambiar o cancelar la cita, responde a este correo.`,
	};
}

function tablaPacientes(lista, vacio) {
	if (lista.length === 0) {
		return `<p style="color:#555555">${escaparHtml(vacio)}</p>`;
	}
	const filas = lista
		.map((p) => `<tr><td style="padding:4px 12px 4px 0">${escaparHtml(p.hora)}</td><td style="padding:4px 0">${escaparHtml(p.nombre)}</td></tr>`)
		.join('');
	return `<table style="border-collapse:collapse">${filas}</table>`;
}

function lineasPacientes(lista, vacio) {
	return lista.length === 0 ? vacio : lista.map((p) => `- ${p.hora} ${p.nombre}`).join('\n');
}

export function correoAvisoUrgente(env, aviso) {
	const cuerpo = `<p><strong>${escaparHtml(aviso.pacienteNombre)}</strong> se marcó como caso urgente a las <strong>${escaparHtml(aviso.hora)}</strong>.</p>
<p>Médico: ${escaparHtml(aviso.medicoNombre)}.</p>
<p style="color:#555555;font-size:14px">Aviso automático para el control de la clínica.</p>`;
	return {
		para: env.CORREO_CLINICA,
		asunto: `Caso urgente: ${aviso.pacienteNombre}`,
		html: marco('Caso urgente en la fila', cuerpo),
		texto: `${aviso.pacienteNombre} se marcó como caso urgente a las ${aviso.hora}.\nMédico: ${aviso.medicoNombre}.`,
	};
}

export function correoResumen(env, resumen) {
	const dia = textoDia(resumen.fecha);
	const cuerpo = `<p>Médico: ${escaparHtml(resumen.medicoNombre)}.</p>
<h2 style="font-size:16px;margin:20px 0 8px">Pacientes atendidos (${resumen.atendidos.length})</h2>
${tablaPacientes(resumen.atendidos, 'No hubo pacientes atendidos.')}
<h2 style="font-size:16px;margin:20px 0 8px">Citas de mañana sin confirmar (${resumen.sinConfirmarManana.length})</h2>
${tablaPacientes(resumen.sinConfirmarManana, 'Todas las citas de mañana están confirmadas.')}`;
	return {
		para: env.CORREO_CLINICA,
		asunto: `Resumen del día ${dia}`,
		html: marco(`Resumen del ${dia}`, cuerpo),
		texto: `Resumen del ${dia}\nMédico: ${resumen.medicoNombre}\n\nPacientes atendidos (${resumen.atendidos.length}):\n${lineasPacientes(resumen.atendidos, 'No hubo pacientes atendidos.')}\n\nCitas de mañana sin confirmar (${resumen.sinConfirmarManana.length}):\n${lineasPacientes(resumen.sinConfirmarManana, 'Todas las citas de mañana están confirmadas.')}`,
	};
}

export function correoSinConfirmar(env, dia, citas) {
	const lista = citas.map((cita) => ({
		hora: textoHora(new Date(cita.fechaHora)),
		nombre: `${cita.pacienteNombre} (${cita.medicoNombre})`,
	}));
	const cuerpo = `<p>Estas citas de mañana todavía no están confirmadas por el paciente:</p>
${tablaPacientes(lista, '')}
<p style="color:#555555;font-size:14px">Conviene llamar a estos pacientes para confirmar su asistencia.</p>`;
	return {
		para: env.CORREO_CLINICA,
		asunto: `Citas sin confirmar para el ${textoDia(dia)} (${citas.length})`,
		html: marco('Citas de mañana sin confirmar', cuerpo),
		texto: `Citas de mañana sin confirmar (${textoDia(dia)}):\n${lineasPacientes(lista, '')}`,
	};
}

/** Página que ve el paciente al abrir el enlace o al confirmar. */
export function pagina(titulo, cuerpoHtml, estado = 200) {
	const html = `<!DOCTYPE html>
<html lang="es"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="robots" content="noindex"><title>${escaparHtml(titulo)} · CliniMedi-k</title></head>
<body style="margin:0;padding:24px;background:#f4f7f6;font-family:Arial,Helvetica,sans-serif;color:#1b1b1b">
<div style="max-width:480px;margin:0 auto;background:#ffffff;border-radius:12px;padding:24px">
<p style="margin:0 0 16px;font-weight:bold;color:${COLOR}">CliniMedi-k</p>
<h1 style="margin:0 0 16px;font-size:22px">${escaparHtml(titulo)}</h1>
${cuerpoHtml}
</div></body></html>`;
	return new Response(html, {
		status: estado,
		headers: { 'Content-Type': 'text/html; charset=utf-8', 'X-Robots-Tag': 'noindex', 'Cache-Control': 'no-store' },
	});
}

export function textoDetalleCita(cita) {
	const { fecha, hora } = detallesCita(cita);
	return `<p>${escaparHtml(cita.pacienteNombre)}, tu cita con <strong>${escaparHtml(cita.medicoNombre)}</strong> es el <strong>${fecha}</strong> a las <strong>${hora}</strong>.</p>`;
}

/** Formulario de confirmación. Confirmar exige tocar el botón: abrir el enlace no basta, porque algunos
 * programas de correo abren los enlaces solos para revisarlos. */
export function formularioConfirmar(cita) {
	return `${textoDetalleCita(cita)}
<form method="POST" action="/confirmar" style="margin:24px 0">
<input type="hidden" name="token" value="${escaparHtml(cita.token)}">
<button type="submit" style="background:${COLOR};color:#ffffff;border:0;padding:12px 20px;border-radius:24px;font-size:16px;font-weight:bold">Confirmar mi cita</button>
</form>`;
}
