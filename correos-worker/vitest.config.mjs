import { cloudflareTest } from '@cloudflare/vitest-plugin';
import { defineConfig } from 'vitest/config';

// Las pruebas usan un KV local y valores falsos. Los secretos reales solo existen en Cloudflare.
export default defineConfig({
	plugins: [
		cloudflareTest({
			wrangler: { configPath: './wrangler.jsonc' },
			miniflare: {
				bindings: {
					CLAVE_APP: 'clave-de-prueba',
					CORREO_CLINICA: 'clinica@example.com',
					RESEND_API_KEY: 'sin-llave',
				},
			},
		}),
	],
});
