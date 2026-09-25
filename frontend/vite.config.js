import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import { fileURLToPath, URL } from 'node:url'

const backend = 'http://127.0.0.1:8080'

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({ imports: ['vue'], dts: false })
  ],
  resolve: {
    alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) }
  },
  server: {
    host: true,
    allowedHosts: ['.trycloudflare.com', '.mylyx.store'],
    port: 5173,
    proxy: {
      '/mall': backend,
      '/files': backend
    }
  }
})
