import { diaEnGuatemala } from './fechas.js';

/**
 * Citas en Workers KV.
 *
 * - `cita:<idCita>` guarda la cita completa. Su metadata (`dia`, `confirmada`) permite
 *   filtrar con una sola llamada a `list`, sin leer cada cita.
 * - `token:<token>` apunta a la cita; es lo que lleva el enlace del correo.
 *
 * Las dos claves vencen solas dos días después de la cita, así KV no acumula datos de pacientes.
 */

const PREFIJO_CITA = 'cita:';
const PREFIJO_TOKEN = 'token:';
const DOS_DIAS_S = 2 * 24 * 60 * 60;

function vencimiento(cita) {
	return Math.floor(new Date(cita.fechaHora).getTime() / 1000) + DOS_DIAS_S;
}

export async function guardarCita(env, cita) {
	const expiration = vencimiento(cita);
	await env.CITAS.put(PREFIJO_CITA + cita.idCita, JSON.stringify(cita), {
		expiration,
		metadata: { dia: diaEnGuatemala(new Date(cita.fechaHora)), confirmada: Boolean(cita.confirmadaEn) },
	});
	await env.CITAS.put(PREFIJO_TOKEN + cita.token, cita.idCita, { expiration });
}

export function obtenerCita(env, idCita) {
	return env.CITAS.get(PREFIJO_CITA + idCita, 'json');
}

export async function obtenerPorToken(env, token) {
	const idCita = await env.CITAS.get(PREFIJO_TOKEN + token);
	return idCita ? obtenerCita(env, idCita) : null;
}

export async function borrarToken(env, token) {
	await env.CITAS.delete(PREFIJO_TOKEN + token);
}

export async function borrarCita(env, cita) {
	await env.CITAS.delete(PREFIJO_CITA + cita.idCita);
	await borrarToken(env, cita.token);
}

/** Todas las citas guardadas, solo con su id y su metadata. */
export async function listarCitas(env) {
	const citas = [];
	let cursor;
	do {
		const pagina = await env.CITAS.list({ prefix: PREFIJO_CITA, cursor });
		for (const clave of pagina.keys) {
			citas.push({ idCita: clave.name.slice(PREFIJO_CITA.length), ...(clave.metadata ?? {}) });
		}
		cursor = pagina.list_complete ? undefined : pagina.cursor;
	} while (cursor);
	return citas;
}
