import { borrarCita, borrarToken, guardarCita, listarCitas, obtenerCita, obtenerPorToken } from './almacen.js';
import {
	correoAvisoUrgente,
	correoConfirmacion,
	correoRecordatorio,
	correoResumen,
	correoSinConfirmar,
	enviarCorreo,
	ErrorEnvio,
	formularioConfirmar,
	pagina,
	textoDetalleCita,
} from './correos.js';
import { mananaEnGuatemala } from './fechas.js';
import { esCorreoDePrueba, esIdValido, validarAvisoUrgente, validarCita, validarResumen } from './validacion.js';

/**
 * Worker de correos de CliniMedi-k.
 *
 * Rutas para la app (encabezado X-Clave-App obligatorio):
 *   POST /citas                  crea o reemplaza una cita y envía el correo de confirmación
 *   POST /citas/{id}/cancelar    borra la cita (ya no recibe recordatorio)
 *   POST /citas/{id}/confirmada  Recepción la confirmó a mano (por teléfono, en persona)
 *   GET  /confirmaciones         ids de las citas que el paciente confirmó
 *   POST /avisos/urgente         aviso a la clínica de un caso urgente
 *   POST /avisos/resumen         resumen del día a la clínica
 *
 * Rutas para el paciente (sin clave; las protege el token del enlace):
 *   GET  /confirmar?token=...    muestra la cita y el botón "Confirmar mi cita"
 *   POST /confirmar              guarda la confirmación
 *
 * Tareas programadas (hora UTC; Guatemala es UTC-6):
 *   0 14 * * *   08:00 de Guatemala: recordatorio a los pacientes con cita mañana
 *   0 0 * * *    18:00 de Guatemala: aviso a la clínica con las citas de mañana sin confirmar
 */

export const CRON_RECORDATORIOS = '0 14 * * *';
export const CRON_SIN_CONFIRMAR = '0 0 * * *';

const RUTA_CANCELAR = /^\/citas\/([^/]+)\/cancelar$/;
const RUTA_CONFIRMADA = /^\/citas\/([^/]+)\/confirmada$/;
const TOKEN_VALIDO = /^[0-9a-f-]{36}$/;
const PAUSA_ENTRE_CORREOS_MS = 600;

export default {
	async fetch(request, env) {
		try {
			return await atender(request, env);
		} catch (error) {
			console.error('Error al atender la solicitud', error);
			if (error instanceof ErrorEnvio) {
				return json({ error: 'No se pudo enviar el correo' }, 502);
			}
			return json({ error: 'Error interno' }, 500);
		}
	},

	async scheduled(controlador, env, ctx) {
		if (controlador.cron === CRON_RECORDATORIOS) {
			ctx.waitUntil(enviarRecordatorios(env));
		} else if (controlador.cron === CRON_SIN_CONFIRMAR) {
			ctx.waitUntil(avisarSinConfirmar(env));
		}
	},
};

async function atender(request, env) {
	const url = new URL(request.url);
	const ruta = url.pathname;
	const metodo = request.method;

	if (ruta === '/confirmar') {
		if (metodo === 'GET') return mostrarConfirmacion(env, url.searchParams.get('token'));
		if (metodo === 'POST') return confirmar(request, env);
		return json({ error: 'Método no permitido' }, 405);
	}

	const protegida = ruta === '/citas' || RUTA_CANCELAR.test(ruta) || RUTA_CONFIRMADA.test(ruta) || ruta === '/confirmaciones' || ruta.startsWith('/avisos/');
	if (!protegida) {
		return json({ error: 'Ruta no encontrada' }, 404);
	}
	if (!(await claveValida(request, env))) {
		return json({ error: 'No autorizado' }, 401);
	}

	if (ruta === '/citas' && metodo === 'POST') return registrarCita(request, env, url.origin);
	const cancelar = ruta.match(RUTA_CANCELAR);
	if (cancelar && metodo === 'POST') return cancelarCita(env, decodeURIComponent(cancelar[1]));
	const confirmada = ruta.match(RUTA_CONFIRMADA);
	if (confirmada && metodo === 'POST') return marcarConfirmada(env, decodeURIComponent(confirmada[1]));
	if (ruta === '/confirmaciones' && metodo === 'GET') return listarConfirmaciones(env);
	if (ruta === '/avisos/urgente' && metodo === 'POST') return avisoUrgente(request, env);
	if (ruta === '/avisos/resumen' && metodo === 'POST') return avisoResumen(request, env);
	return json({ error: 'Ruta no encontrada' }, 404);
}

/** Compara la clave en tiempo constante para no dar pistas por el tiempo de respuesta. */
async function claveValida(request, env) {
	const esperada = new TextEncoder().encode(env.CLAVE_APP ?? '');
	const recibida = new TextEncoder().encode(request.headers.get('X-Clave-App') ?? '');
	if (esperada.byteLength === 0 || esperada.byteLength !== recibida.byteLength) {
		return false;
	}
	return crypto.subtle.timingSafeEqual(esperada, recibida);
}

async function leerJson(request) {
	try {
		return await request.json();
	} catch {
		return null;
	}
}

function json(cuerpo, estado = 200) {
	return Response.json(cuerpo, { status: estado, headers: { 'Cache-Control': 'no-store' } });
}

async function registrarCita(request, env, origen) {
	const { cita: datos, error } = validarCita(await leerJson(request));
	if (error) {
		return json({ error }, 400);
	}
	const anterior = await obtenerCita(env, datos.idCita);
	// La app puede reenviar la misma cita (reintento o reenvío al abrir la app): no se repite el correo.
	if (anterior && anterior.correoEnviado && anterior.fechaHora === datos.fechaHora && anterior.pacienteCorreo === datos.pacienteCorreo) {
		return json({ correoEnviado: true, repetida: true });
	}
	// Cita nueva o reprogramada: confirmación y recordatorio empiezan de cero, con un enlace nuevo.
	const cita = {
		...datos,
		origen,
		token: crypto.randomUUID(),
		confirmadaEn: null,
		correoEnviado: false,
		recordatorioEnviado: false,
	};
	if (!esCorreoDePrueba(cita.pacienteCorreo)) {
		await enviarCorreo(env, correoConfirmacion(cita));
		cita.correoEnviado = true;
	}
	if (anterior) {
		await borrarToken(env, anterior.token);
	}
	await guardarCita(env, cita);
	return json({ correoEnviado: cita.correoEnviado });
}

async function cancelarCita(env, idCita) {
	if (!esIdValido(idCita)) {
		return json({ error: 'idCita no es válido' }, 400);
	}
	const cita = await obtenerCita(env, idCita);
	if (cita) {
		await borrarCita(env, cita);
	}
	return json({ cancelada: Boolean(cita) });
}

/** Recepción confirmó la cita por otro medio: deja de aparecer en el aviso de citas sin confirmar. */
async function marcarConfirmada(env, idCita) {
	if (!esIdValido(idCita)) {
		return json({ error: 'idCita no es válido' }, 400);
	}
	const cita = await obtenerCita(env, idCita);
	if (cita && !cita.confirmadaEn) {
		cita.confirmadaEn = new Date().toISOString();
		await guardarCita(env, cita);
	}
	return json({ confirmada: Boolean(cita) });
}

async function listarConfirmaciones(env) {
	const citas = await listarCitas(env);
	return json({ confirmadas: citas.filter((cita) => cita.confirmada).map((cita) => cita.idCita) });
}

async function citaDelEnlace(env, token) {
	if (typeof token !== 'string' || !TOKEN_VALIDO.test(token)) {
		return null;
	}
	return obtenerPorToken(env, token);
}

function enlaceNoValido() {
	return pagina(
		'Enlace no válido',
		'<p>Este enlace ya no es válido: la cita pudo cambiar de horario o ya pasó. Si tienes dudas, responde al correo de tu cita.</p>',
		404,
	);
}

async function mostrarConfirmacion(env, token) {
	const cita = await citaDelEnlace(env, token);
	if (!cita) {
		return enlaceNoValido();
	}
	if (new Date(cita.fechaHora).getTime() < Date.now()) {
		return pagina('Esta cita ya pasó', textoDetalleCita(cita));
	}
	if (cita.confirmadaEn) {
		return pagina('Tu cita ya está confirmada', `${textoDetalleCita(cita)}<p>¡Te esperamos!</p>`);
	}
	return pagina('Confirma tu cita', formularioConfirmar(cita));
}

async function confirmar(request, env) {
	let token = null;
	try {
		token = (await request.formData()).get('token');
	} catch {
		token = null;
	}
	const cita = await citaDelEnlace(env, token);
	if (!cita) {
		return enlaceNoValido();
	}
	if (new Date(cita.fechaHora).getTime() < Date.now()) {
		return pagina('Esta cita ya pasó', textoDetalleCita(cita));
	}
	if (!cita.confirmadaEn) {
		cita.confirmadaEn = new Date().toISOString();
		await guardarCita(env, cita);
	}
	return pagina('Cita confirmada', `${textoDetalleCita(cita)}<p>Gracias por confirmar. ¡Te esperamos!</p>`);
}

async function avisoUrgente(request, env) {
	const { aviso, error } = validarAvisoUrgente(await leerJson(request));
	if (error) {
		return json({ error }, 400);
	}
	await enviarCorreo(env, correoAvisoUrgente(env, aviso));
	return json({ enviado: true });
}

async function avisoResumen(request, env) {
	const { resumen, error } = validarResumen(await leerJson(request));
	if (error) {
		return json({ error }, 400);
	}
	await enviarCorreo(env, correoResumen(env, resumen));
	return json({ enviado: true });
}

function pausa(ms) {
	return new Promise((resolver) => setTimeout(resolver, ms));
}

/** Citas de mañana (hora de Guatemala) con sus datos completos. */
async function citasDeManana(env, filtro = () => true) {
	const manana = mananaEnGuatemala();
	const resumenes = (await listarCitas(env)).filter((cita) => cita.dia === manana && filtro(cita));
	const citas = [];
	for (const { idCita } of resumenes) {
		const cita = await obtenerCita(env, idCita);
		if (cita) {
			citas.push(cita);
		}
	}
	citas.sort((a, b) => a.fechaHora.localeCompare(b.fechaHora));
	return { manana, citas };
}

/** Un error con un paciente no detiene a los demás. Resend acepta pocas solicitudes por segundo, por eso la pausa. */
export async function enviarRecordatorios(env) {
	const { citas } = await citasDeManana(env);
	for (const cita of citas) {
		if (cita.recordatorioEnviado || esCorreoDePrueba(cita.pacienteCorreo)) {
			continue;
		}
		try {
			await enviarCorreo(env, correoRecordatorio(cita));
			cita.recordatorioEnviado = true;
			await guardarCita(env, cita);
		} catch (error) {
			console.error(`No se pudo enviar el recordatorio de la cita ${cita.idCita}`, error);
		}
		await pausa(PAUSA_ENTRE_CORREOS_MS);
	}
}

export async function avisarSinConfirmar(env) {
	const { manana, citas } = await citasDeManana(env, (cita) => !cita.confirmada);
	if (citas.length === 0) {
		return;
	}
	await enviarCorreo(env, correoSinConfirmar(env, manana, citas));
}
