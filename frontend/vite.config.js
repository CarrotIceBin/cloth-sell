import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import { fileURLToPath, URL } from 'node:url'

const backend = 'http://127.0.0.1:8080'

function gatewayPrefix(url, side) {
  const path = url.split('?')[0]
  if (side === 'admin') return '/admin-api'
  if (side === 'client') return '/client-api'
  if (path.startsWith('/mall/client') || path === '/mall/auth/login' || path === '/mall/auth/register') return '/client-api'
  return '/admin-api'
}

function routeMall(req) {
  if (!req.url || !req.url.startsWith('/mall')) return
  const side = String(req.headers['x-mall-side'] || '')
  req.url = gatewayPrefix(req.url, side) + req.url
}

function apiGateway() {
  const attach = (server) => {
    server.middlewares.use((req, res, next) => {
      routeMall(req)
      next()
    })
  }
  return {
    name: 'api-gateway',
    configureServer: attach,
    configurePreviewServer: attach
  }
}

const proxy = {
  '/admin-api': { target: backend, changeOrigin: true },
  '/client-api': { target: backend, changeOrigin: true },
  '/files': backend
}

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({ imports: ['vue'], dts: false }),
    apiGateway()
  ],
  resolve: {
    alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) }
  },
  server: {
    host: true,
    allowedHosts: ['.trycloudflare.com', '.mylyx.store'],
    port: 5173,
    proxy
  },
  preview: { proxy }
})
