import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  base: "/",
  build: {
    outDir: "build",
    sourcemap: false,
  },
  plugins: [
    vue(),
  ],
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url)),
    },
  },
  css: {
  },
  define: {
    'process.env': {}
  },
  server: {
    port: 9527,
    host:'0.0.0.0',
    proxy: {
      '/management/': {
        target: 'http://localhost:9527/',
        rewrite: () => '/management'
      },
      '/api': {
        // target: 'http://10.11.32.179:8080', //
        // target: 'http://ops-test-03.lab.zjvis.net:18080', //
        // target:'https://data-pipeline-dev.lab.zjvis.net/api/',
        // target: 'https://data-pipeline-dev.lab.zjvis.net/api', // demo server
        // target: 'http://ops-test-02.lab.zjvis.net:8080',
        target: 'http://10.105.20.64:30865', // dev 环境
        ws: true,
        changeOrigin: true,
        rewrite: (path) => path.replace('/api', ''),
      }
    },
  },
})
