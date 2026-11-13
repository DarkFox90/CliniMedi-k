import { leerFechaHora } from './fechas.js';

const ID_VALIDO = /^[A-Za-z0-9-]{1,100}$/;
const CORREO_VALIDO = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const HORA_VALIDA = /^([01]\d|2[0-3]):[0-5]\d$/;
const DIA_VALIDO = /^\d{4}-\d{2}-\d{2}$/;
const UN_ANIO_MS = 366 * 24 * 60 * 60 * 1000;
const MAXIMO_EN_LISTA = 200;

/** Dominios reservados para pruebas (RFC 2606 y RFC 6761). A ellos no se envía nada. */
const DOMINIOS_DE_PRUEBA = ['example.com', 'example.net', 'example.org'];
const TERMINACIONES_DE_PRUEBA = ['.test', '.example', '.invalid', '.localhost'];

export function esIdValido(id) {
	return typeof id === 'string' && ID_VALIDO.test(id);
}

export function esCorreoDePrueba(correo) {
	const dominio = correo.split('@').pop().toLowerCase();
	return DOMINIOS_DE_PRUEBA.includes(dominio) || TERMINACIONES_DE_PRUEBA.some((terminacion) => dominio.endsWith(terminacion));
}

function texto(valor, maximo) {
	if (typeof valor !== 'string') {
		return null;
	}
	const limpio = valor.trim();
	return limpio.length > 0 && limpio.length <= maximo ? limpio : null;
}

/**
 * Revisa el cuerpo de POST /citas. Devuelve `{ cita }` con los datos limpios
 * o `{ error }` con el motivo del rechazo.
 */
export function validarCita(datos, ahora = new Date()) {
	if (datos === null || typeof datos !== 'object') {
		return { error: 'El cuerpo debe ser un objeto JSON' };
	}
	if (!esIdValido(datos.idCita)) {
		return { error: 'idCita no es válido' };
	}
	const pacienteNombre = texto(datos.pacienteNombre, 120);
	if (!pacienteNombre) {
		return { error: 'pacienteNombre es obligatorio' };
	}
	const pacienteCorreo = texto(datos.pacienteCorreo, 254);
	if (!pacienteCorreo || !CORREO_VALIDO.test(pacienteCorreo)) {
		return { error: 'pacienteCorreo no es válido' };
	}
	const medicoNombre = texto(datos.medicoNombre, 120);
	if (!medicoNombre) {
		return { error: 'medicoNombre es obligatorio' };
	}
	const fecha = leerFechaHora(datos.fechaHora);
	if (!fecha) {
		return { error: 'fechaHora debe incluir la zona horaria, por ejemplo 2026-11-14T10:30-06:00' };
	}
	if (fecha.getTime() <= ahora.getTime()) {
		return { error: 'La cita ya pasó' };
	}
	if (fecha.getTime() - ahora.getTime() > UN_ANIO_MS) {
		return { error: 'La cita es a más de un año' };
	}
	return {
		cita: {
			idCita: datos.idCita,
			pacienteNombre,
			pacienteCorreo: pacienteCorreo.toLowerCase(),
			medicoNombre,
			fechaHora: fecha.toISOString(),
		},
	};
}

/** Revisa el cuerpo de POST /avisos/urgente. */
export function validarAvisoUrgente(datos) {
	const pacienteNombre = texto(datos?.pacienteNombre, 120);
	const medicoNombre = texto(datos?.medicoNombre, 120);
	const hora = texto(datos?.hora, 5);
	if (!pacienteNombre || !medicoNombre || !hora || !HORA_VALIDA.test(hora)) {
		return { error: 'Se necesitan pacienteNombre, medicoNombre y hora (HH:mm)' };
	}
	return { aviso: { pacienteNombre, medicoNombre, hora } };
}

function listaDePacientes(lista) {
	if (!Array.isArray(lista) || lista.length > MAXIMO_EN_LISTA) {
		return null;
	}
	const limpia = [];
	for (const elemento of lista) {
		const nombre = texto(elemento?.nombre, 120);
		const hora = texto(elemento?.hora, 5);
		if (!nombre || !hora || !HORA_VALIDA.test(hora)) {
			return null;
		}
		limpia.push({ nombre, hora });
	}
	return limpia;
}

/** Revisa el cuerpo de POST /avisos/resumen. */
export function validarResumen(datos) {
	const fecha = texto(datos?.fecha, 10);
	const medicoNombre = texto(datos?.medicoNombre, 120);
	const atendidos = listaDePacientes(datos?.atendidos);
	const sinConfirmarManana = listaDePacientes(datos?.sinConfirmarManana);
	if (!fecha || !DIA_VALIDO.test(fecha) || !medicoNombre || !atendidos || !sinConfirmarManana) {
		return { error: 'Se necesitan fecha (AAAA-MM-DD), medicoNombre, atendidos y sinConfirmarManana' };
	}
	return { resumen: { fecha, medicoNombre, atendidos, sinConfirmarManana } };
}
