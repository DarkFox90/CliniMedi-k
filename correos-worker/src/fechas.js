/**
 * Fechas en la hora de Guatemala (UTC-6, sin horario de verano).
 * Cloudflare ejecuta el Worker en UTC, por eso cada cálculo de "día" pasa por aquí.
 */

const DESFASE_GUATEMALA_MS = 6 * 60 * 60 * 1000;

const DIAS = ['domingo', 'lunes', 'martes', 'miércoles', 'jueves', 'viernes', 'sábado'];
const MESES = [
	'enero',
	'febrero',
	'marzo',
	'abril',
	'mayo',
	'junio',
	'julio',
	'agosto',
	'septiembre',
	'octubre',
	'noviembre',
	'diciembre',
];

/** Fecha y hora con zona, como la envía la app: 2026-11-14T10:30-06:00 (los segundos son opcionales). */
const FORMATO_FECHA_HORA = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}(:\d{2}(\.\d+)?)?(Z|[+-]\d{2}:\d{2})$/;

/** Devuelve la fecha leída o `null` si el texto no tiene el formato esperado. */
export function leerFechaHora(texto) {
	if (typeof texto !== 'string' || !FORMATO_FECHA_HORA.test(texto)) {
		return null;
	}
	const fecha = new Date(texto);
	return Number.isNaN(fecha.getTime()) ? null : fecha;
}

/** La misma fecha corrida a la hora de Guatemala; se lee con los métodos getUTC*. */
function enGuatemala(fecha) {
	return new Date(fecha.getTime() - DESFASE_GUATEMALA_MS);
}

/** Día en Guatemala con formato AAAA-MM-DD. */
export function diaEnGuatemala(fecha) {
	return enGuatemala(fecha).toISOString().slice(0, 10);
}

/** El día siguiente en Guatemala, con formato AAAA-MM-DD. */
export function mananaEnGuatemala(ahora = new Date()) {
	const local = enGuatemala(ahora);
	local.setUTCDate(local.getUTCDate() + 1);
	return local.toISOString().slice(0, 10);
}

/** "sábado 14 de noviembre de 2026" */
export function textoFecha(fecha) {
	const local = enGuatemala(fecha);
	return `${DIAS[local.getUTCDay()]} ${local.getUTCDate()} de ${MESES[local.getUTCMonth()]} de ${local.getUTCFullYear()}`;
}

/** "10:30", en 24 horas. */
export function textoHora(fecha) {
	const local = enGuatemala(fecha);
	return `${dosDigitos(local.getUTCHours())}:${dosDigitos(local.getUTCMinutes())}`;
}

/** "14 de noviembre de 2026" a partir de AAAA-MM-DD. */
export function textoDia(dia) {
	const [anio, mes, numero] = dia.split('-').map(Number);
	return `${numero} de ${MESES[mes - 1]} de ${anio}`;
}

function dosDigitos(numero) {
	return String(numero).padStart(2, '0');
}
