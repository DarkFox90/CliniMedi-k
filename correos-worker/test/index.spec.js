import { env, SELF } from 'cloudflare:test';
import { describe, expect, it } from 'vitest';
import { escaparHtml } from '../src/correos.js';
import { diaEnGuatemala, leerFechaHora, mananaEnGuatemala, textoFecha, textoHora } from '../src/fechas.js';
import { esCorreoDePrueba, validarAvisoUrgente, validarCita, validarResumen } from '../src/validacion.js';

const AHORA = new Date('2026-11-12T18:00:00Z');

const citaValida = {
	idCita: 'cit-123',
	pacienteNombre: 'Marta Ríos',
	pacienteCorreo: 'Marta@Example.com',
	medicoNombre: 'Dr. Roberto Sical',
	fechaHora: '2026-11-14T10:30-06:00',
};

describe('rutas', () => {
	it('responde 404 a una ruta que no existe', async () => {
		const respuesta = await SELF.fetch('https://worker.test/no-existe');
		expect(respuesta.status).toBe(404);
	});

	it('rechaza POST /citas sin la clave', async () => {
		const respuesta = await SELF.fetch('https://worker.test/citas', { method: 'POST', body: JSON.stringify(citaValida) });
		expect(respuesta.status).toBe(401);
	});

	it('rechaza GET /confirmaciones con una clave equivocada', async () => {
		const respuesta = await SELF.fetch('https://worker.test/confirmaciones', { headers: { 'X-Clave-App': 'equivocada' } });
		expect(respuesta.status).toBe(401);
	});

	it('muestra "Enlace no válido" con un token que no existe', async () => {
		const respuesta = await SELF.fetch('https://worker.test/confirmar?token=00000000-0000-0000-0000-000000000000');
		expect(respuesta.status).toBe(404);
		expect(await respuesta.text()).toContain('Enlace no válido');
	});

	it('muestra "Enlace no válido" sin token', async () => {
		const respuesta = await SELF.fetch('https://worker.test/confirmar');
		expect(respuesta.status).toBe(404);
	});
});

describe('validarCita', () => {
	it('acepta una cita completa y normaliza la fecha y el correo', () => {
		const { cita, error } = validarCita(citaValida, AHORA);
		expect(error).toBeUndefined();
		expect(cita.fechaHora).toBe('2026-11-14T16:30:00.000Z');
		expect(cita.pacienteCorreo).toBe('marta@example.com');
	});

	it('rechaza una fecha sin zona horaria', () => {
		expect(validarCita({ ...citaValida, fechaHora: '2026-11-14T10:30' }, AHORA).error).toBeDefined();
	});

	it('rechaza una cita que ya pasó', () => {
		expect(validarCita({ ...citaValida, fechaHora: '2026-11-10T10:30-06:00' }, AHORA).error).toBe('La cita ya pasó');
	});

	it('rechaza un correo mal escrito y un id con caracteres raros', () => {
		expect(validarCita({ ...citaValida, pacienteCorreo: 'marta' }, AHORA).error).toBeDefined();
		expect(validarCita({ ...citaValida, idCita: 'cit/1' }, AHORA).error).toBeDefined();
	});
});

describe('avisos', () => {
	it('valida el aviso de urgente', () => {
		expect(validarAvisoUrgente({ pacienteNombre: 'Ana', medicoNombre: 'Dr. X', hora: '09:15' }).aviso).toBeDefined();
		expect(validarAvisoUrgente({ pacienteNombre: 'Ana', medicoNombre: 'Dr. X', hora: '9:15' }).error).toBeDefined();
	});

	it('valida el resumen', () => {
		const resumen = { fecha: '2026-11-12', medicoNombre: 'Dr. X', atendidos: [{ nombre: 'Ana', hora: '08:00' }], sinConfirmarManana: [] };
		expect(validarResumen(resumen).resumen).toBeDefined();
		expect(validarResumen({ ...resumen, atendidos: 'Ana' }).error).toBeDefined();
	});
});

describe('fechas en Guatemala', () => {
	it('escribe la fecha y la hora en español y en 24 horas', () => {
		const fecha = leerFechaHora('2026-11-14T10:30-06:00');
		expect(textoFecha(fecha)).toBe('sábado 14 de noviembre de 2026');
		expect(textoHora(fecha)).toBe('10:30');
	});

	it('calcula el día con la hora de Guatemala y no con la de UTC', () => {
		// 03:00 UTC del 13 son las 21:00 del 12 en Guatemala.
		expect(diaEnGuatemala(new Date('2026-11-13T03:00:00Z'))).toBe('2026-11-12');
		expect(mananaEnGuatemala(new Date('2026-11-13T03:00:00Z'))).toBe('2026-11-13');
	});
});

describe('otros', () => {
	it('reconoce los dominios de prueba', () => {
		expect(esCorreoDePrueba('alguien@example.com')).toBe(true);
		expect(esCorreoDePrueba('alguien@clinica.test')).toBe(true);
		expect(esCorreoDePrueba('alguien@gmail.com')).toBe(false);
	});

	it('escapa el HTML de los nombres', () => {
		expect(escaparHtml('<b>"Ana" & Luis</b>')).toBe('&lt;b&gt;&quot;Ana&quot; &amp; Luis&lt;/b&gt;');
	});
});

describe('flujo de una cita (correo de prueba, sin llamar a Resend)', () => {
	const encabezados = { 'X-Clave-App': 'clave-de-prueba', 'Content-Type': 'application/json' };
	const enDias = (dias) => new Date(Date.now() + dias * 24 * 60 * 60 * 1000).toISOString();
	const cita = (fechaHora) => JSON.stringify({ ...citaValida, idCita: 'cit-flujo', pacienteCorreo: 'ana@example.com', fechaHora });
	const confirmaciones = async () => (await SELF.fetch('https://worker.test/confirmaciones', { headers: encabezados })).json();

	it('registra, confirma, reprograma y cancela', async () => {
		let respuesta = await SELF.fetch('https://worker.test/citas', { method: 'POST', headers: encabezados, body: cita(enDias(3)) });
		expect(await respuesta.json()).toEqual({ correoEnviado: false });
		expect(await confirmaciones()).toEqual({ confirmadas: [] });

		const guardada = await env.CITAS.get('cita:cit-flujo', 'json');
		respuesta = await SELF.fetch(`https://worker.test/confirmar?token=${guardada.token}`);
		expect(await respuesta.text()).toContain('Confirmar mi cita');

		const formulario = new FormData();
		formulario.set('token', guardada.token);
		respuesta = await SELF.fetch('https://worker.test/confirmar', { method: 'POST', body: formulario });
		expect(await respuesta.text()).toContain('Cita confirmada');
		expect(await confirmaciones()).toEqual({ confirmadas: ['cit-flujo'] });

		// Reprogramar invalida el enlace anterior y reinicia la confirmación.
		await SELF.fetch('https://worker.test/citas', { method: 'POST', headers: encabezados, body: cita(enDias(4)) });
		respuesta = await SELF.fetch(`https://worker.test/confirmar?token=${guardada.token}`);
		expect(respuesta.status).toBe(404);
		expect(await confirmaciones()).toEqual({ confirmadas: [] });

		// Recepción la confirma a mano.
		respuesta = await SELF.fetch('https://worker.test/citas/cit-flujo/confirmada', { method: 'POST', headers: encabezados });
		expect(await respuesta.json()).toEqual({ confirmada: true });
		expect(await confirmaciones()).toEqual({ confirmadas: ['cit-flujo'] });

		respuesta = await SELF.fetch('https://worker.test/citas/cit-flujo/cancelar', { method: 'POST', headers: encabezados });
		expect(await respuesta.json()).toEqual({ cancelada: true });
	});

	it('rechaza un cuerpo que no es JSON y un aviso incompleto', async () => {
		let respuesta = await SELF.fetch('https://worker.test/citas', { method: 'POST', headers: encabezados, body: '{malo' });
		expect(respuesta.status).toBe(400);
		respuesta = await SELF.fetch('https://worker.test/avisos/urgente', { method: 'POST', headers: encabezados, body: '{}' });
		expect(respuesta.status).toBe(400);
	});
});
