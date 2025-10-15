/**
 * 性能优化插件
 */
import type { App } from 'vue'
import { monitorPagePerformance, getPerformanceConfig, preloadResources } from '@/utils/performance'

// 预加载关键资源
const criticalResources = [
  '/favicon.ico',
  '/logo.svg',
  // 添加其他关键资源
]

export default {
  install(app: App) {
    // 启动性能监控
    monitorPagePerformance()
    
    // 获取性能配置
    const config = getPerformanceConfig()
    
    // 预加载关键资源
    if (typeof window !== 'undefined') {
      preloadResources(criticalResources)
    }
    
    // 根据设备性能调整全局配置
    app.config.globalProperties.$performanceConfig = config
    
    // 添加性能指令
    app.directive('throttle', {
      mounted(el, binding) {
        const [fn, delay = config.throttleTime] = binding.value
        let lastCall = 0
        
        el.addEventListener('click', () => {
          const now = Date.now()
          if (now - lastCall >= delay) {
            fn()
            lastCall = now
          }
        })
      }
    })
    
    app.directive('debounce', {
      mounted(el, binding) {
        const [fn, delay = config.debounceTime] = binding.value
        let timeoutId: NodeJS.Timeout
        
        el.addEventListener('click', () => {
          clearTimeout(timeoutId)
          timeoutId = setTimeout(fn, delay)
        })
      }
    })
    
    // 添加性能方法到全局
    app.config.globalProperties.$perf = {
      config,
      monitor: () => {
        const { PerformanceMonitor } = require('@/utils/performance')
        return PerformanceMonitor.getInstance()
      }
    }
  }
}