import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { AntDesignVueResolver } from 'unplugin-vue-components/resolvers'
import { visualizer } from 'rollup-plugin-visualizer'

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      imports: ['vue', 'vue-router', 'pinia'],
      dts: true
    }),
    Components({
      resolvers: [
        AntDesignVueResolver({
          importStyle: false
        })
      ],
      dts: true
    }),
    // 打包分析插件
    process.env.ANALYZE && visualizer({
      open: true,
      gzipSize: true,
      brotliSize: true
    })
  ],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 3000,
    host: '0.0.0.0',
    proxy: {
      '/api': {
        target: process.env.VITE_API_BASE_URL || 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      },
      '/ws': {
        target: process.env.VITE_WS_URL || 'ws://localhost:8080',
        ws: true,
        changeOrigin: true
      }
    }
  },
  build: {
    target: 'es2015',
    outDir: 'dist',
    assetsDir: 'assets',
    sourcemap: process.env.NODE_ENV === 'development',
    // 启用 CSS 代码分割
    cssCodeSplit: true,
    // 减小打包体积
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true,
        drop_debugger: true
      }
    },
    rollupOptions: {
      output: {
        // 优化代码分割
        manualChunks(id) {
          // 将 node_modules 中的依赖分类打包（使用正则表达式精确匹配）
          if (/[\\/]node_modules[\\/]/.test(id)) {
            // Vue 核心
            if (/[\\/]node_modules[\\/](vue|vue-router|pinia)[\\/]/.test(id)) {
              return 'vue-vendor'
            }
            // Ant Design Vue
            if (/[\\/]node_modules[\\/]ant-design-vue[\\/]/.test(id)) {
              return 'antd-vendor'
            }
            // 工具库
            if (/[\\/]node_modules[\\/](lodash|axios|dayjs)/.test(id)) {
              return 'utils-vendor'
            }
            // 图表库
            if (/[\\/]node_modules[\\/](echarts|dhtmlx-gantt)/.test(id)) {
              return 'chart-vendor'
            }
            // 编辑器
            if (/[\\/]node_modules[\\/](@tinymce|md-editor)/.test(id)) {
              return 'editor-vendor'
            }
            // 其他依赖
            return 'vendor'
          }
        },
        // 优化文件名
        chunkFileNames: 'js/[name]-[hash].js',
        entryFileNames: 'js/[name]-[hash].js',
        assetFileNames: (assetInfo) => {
          const info = assetInfo.name?.split('.') || []
          let extType = info[info.length - 1]
          
          if (/\.(mp4|webm|ogg|mp3|wav|flac|aac)(\?.*)?$/i.test(assetInfo.name || '')) {
            extType = 'media'
          } else if (/\.(png|jpe?g|gif|svg)(\?.*)?$/i.test(assetInfo.name || '')) {
            extType = 'img'
          } else if (/\.(woff2?|eot|ttf|otf)(\?.*)?$/i.test(assetInfo.name || '')) {
            extType = 'fonts'
          }
          
          return `${extType}/[name]-[hash].[ext]`
        }
      }
    },
    // 设置 chunk 大小警告限制
    chunkSizeWarningLimit: 1000
  },
  // 优化依赖预构建
  optimizeDeps: {
    include: [
      'vue',
      'vue-router',
      'pinia',
      'ant-design-vue',
      'axios',
      'dayjs',
      'lodash-es'
    ],
    exclude: [
      '@tinymce/tinymce-vue',
      'md-editor-v3',
      'dhtmlx-gantt'
    ]
  },
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: ['./src/tests/setup.ts'],
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html'],
      thresholds: {
        global: {
          branches: 40,
          functions: 40,
          lines: 40,
          statements: 40
        }
      }
    }
  }
})
