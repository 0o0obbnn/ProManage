/**
 * ProManage - 主应用入口
 */
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { setupRouterGuards } from './router/guards'
import pinia from './stores'

// Ant Design Vue
import Ant from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'

// 全局样式
import '@/assets/styles/global.scss'

// 懒加载指令
import { setupLazyLoad } from './directives/lazyLoad'
// 安全输入指令
import { setupSafeInput } from './directives/safeInput'

// 创建应用实例
const app = createApp(App)

// 配置路由守卫
setupRouterGuards(router)

// 挂载插件
app.use(router)
app.use(pinia)
app.use(Ant)

// 注册懒加载指令
setupLazyLoad(app)
// 注册安全输入指令
setupSafeInput(app)

// 注册性能优化插件
import performancePlugin from './plugins/performance'
app.use(performancePlugin)

// 注册 Service Worker
import { registerServiceWorker } from './utils/serviceWorker'

// 只在生产环境注册 Service Worker
if (import.meta.env.PROD) {
  registerServiceWorker()
}

// 全局错误处理
app.config.errorHandler = (err, instance, info) => {
  console.error('Global error:', err, info)
  
  // 上报到监控系统
  if (import.meta.env.PROD) {
    // TODO: 集成 Sentry 或其他监控服务
    // Sentry.captureException(err, {
    //   contexts: {
    //     vue: {
    //       componentName: instance?.$options.name,
    //       propsData: instance?.$props,
    //       info
    //     }
    //   }
    // })
  }
  
  // 显示用户友好的错误提示
  import('ant-design-vue').then(({ message }) => {
    message.error('应用发生错误，请刷新页面重试')
  })
}

// 全局警告处理
app.config.warnHandler = (msg, instance, trace) => {
  if (import.meta.env.DEV) {
    console.warn('Vue warning:', msg, trace)
  }
}

// 生产环境禁用console
if (import.meta.env.PROD) {
  console.log = () => {}
  console.debug = () => {}
  console.info = () => {}
  // 保留 console.warn 和 console.error 用于错误追踪
}

// 挂载应用
app.mount('#app')
