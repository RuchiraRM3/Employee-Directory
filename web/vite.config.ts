import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // The browser calls /api/... on the same address it loaded the app from; the dev server forwards
    // it to the Spring Boot API on this machine. So the app works from any address (another device,
    // port forwarding, a tunnel) with only port 5173 forwarded, and the API itself is never exposed.
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        rewrite: (path) => path.replace(/^\/api/, ''),
        configure: (proxy) => {
          // To the browser this is a same-origin request. Drop its Origin header so the API's CORS
          // check (which only allows http://localhost:5173 for direct calls) doesn't reject it.
          proxy.on('proxyReq', (proxyReq) => proxyReq.removeHeader('origin'))
        },
      },
    },
  },
})
