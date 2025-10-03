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

// 创建应用实例
const app = createApp(App)

// 配置路由守卫
setupRouterGuards(router)

// 挂载插件
app.use(router)
app.use(pinia)
app.use(Ant)

// 挂载应用
app.mount('#app')
