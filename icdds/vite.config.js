import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue(), vueDevTools()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  // 1. 端口号设置为 80，这是 HTTP 默认端口，用于 Web 应用程序。
  // 2. host 设置为 true，允许从任何网络接口访问该服务器。
  // 3. open 设置为 true，启动服务器后自动打开默认浏览器。
  // 4. 代理 /api 到后端服务，将所有以 /api 开头的请求转发到 http://localhost:8080。
  // 5. changeOrigin 设置为 true，修改请求头中的 Host 字段为目标服务器地址。
  // 6. rewrite 函数将请求路径中的 /api 前缀移除，确保后端接口能够正常接收请求。
  // 7. 代理 /api/v1 到后端服务，将所有以 /api/v1 开头的请求转发到 http://localhost:8080。

  server: {
    port: 5173, // 明确指定前端开发服务器端口
    host: true,
    open: true,
    proxy: {
      // 代理 /api 到后端服务
      '/api': {
        // 目标地址：后端服务运行在 8080 端口，使用 127.0.0.1 避免 DNS 解析问题
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
        // 如果后端接口不包含 /api 前缀，才需要 rewrite
        // rewrite: (path) => path.replace(/^\/api/, '')
      },
      // 代理 /uploads 静态资源到后端服务
      '/uploads': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
      },
    },
  },
})
