/**
 * 性能优化工具
 */
import { ref, onMounted, onUnmounted } from 'vue'

// 防抖函数
export function debounce<T extends (...args: any[]) => any>(
  func: T,
  wait: number
): (...args: Parameters<T>) => void {
  let timeout: NodeJS.Timeout | null = null
  
  return function (...args: Parameters<T>) {
    if (timeout) {
      clearTimeout(timeout)
    }
    timeout = setTimeout(() => func(...args), wait)
  }
}

// 节流函数
export function throttle<T extends (...args: any[]) => any>(
  func: T,
  limit: number
): (...args: Parameters<T>) => void {
  let inThrottle: boolean
  
  return function (...args: Parameters<T>) {
    if (!inThrottle) {
      func(...args)
      inThrottle = true
      setTimeout(() => inThrottle = false, limit)
    }
  }
}

// 预加载图片
export function preloadImage(src: string): Promise<HTMLImageElement> {
  return new Promise((resolve, reject) => {
    const img = new Image()
    img.onload = () => resolve(img)
    img.onerror = reject
    img.src = src
  })
}

// 预加载多个图片
export async function preloadImages(srcs: string[]): Promise<HTMLImageElement[]> {
  const promises = srcs.map(src => preloadImage(src))
  return Promise.all(promises)
}

// 检测网络状态
export function useNetworkStatus() {
  const isOnline = ref<boolean>(navigator.onLine)
  const connectionType = ref<string>('unknown')
  
  const updateOnlineStatus = () => {
    isOnline.value = navigator.onLine
  }
  
  const updateConnectionType = () => {
    const connection = (navigator as any).connection || (navigator as any).mozConnection || (navigator as any).webkitConnection
    if (connection) {
      connectionType.value = connection.effectiveType || 'unknown'
    }
  }
  
  onMounted(() => {
    window.addEventListener('online', updateOnlineStatus)
    window.addEventListener('offline', updateOnlineStatus)
    
    if ((navigator as any).connection) {
      (navigator as any).connection.addEventListener('change', updateConnectionType)
      updateConnectionType()
    }
  })
  
  onUnmounted(() => {
    window.removeEventListener('online', updateOnlineStatus)
    window.removeEventListener('offline', updateOnlineStatus)
    
    if ((navigator as any).connection) {
      (navigator as any).connection.removeEventListener('change', updateConnectionType)
    }
  })
  
  return {
    isOnline,
    connectionType
  }
}

// 检测设备性能
export function getDevicePerformance() {
  const connection = (navigator as any).connection || (navigator as any).mozConnection || (navigator as any).webkitConnection
  const hardwareConcurrency = navigator.hardwareConcurrency || 4
  const deviceMemory = (navigator as any).deviceMemory || 4
  
  return {
    connectionType: connection?.effectiveType || 'unknown',
    saveData: connection?.saveData || false,
    hardwareConcurrency,
    deviceMemory,
    isLowEndDevice: hardwareConcurrency < 4 || deviceMemory < 4
  }
}

// 根据设备性能调整配置
export function getPerformanceConfig() {
  const device = getDevicePerformance()
  
  // 低端设备配置
  if (device.isLowEndDevice || device.connectionType === 'slow-2g' || device.connectionType === '2g' || device.saveData) {
    return {
      enableAnimations: false,
      enableLazyLoad: true,
      imageQuality: 'low',
      chunkSize: 10,
      debounceTime: 500,
      throttleTime: 300
    }
  }
  
  // 中端设备配置
  if (device.connectionType === '3g') {
    return {
      enableAnimations: true,
      enableLazyLoad: true,
      imageQuality: 'medium',
      chunkSize: 20,
      debounceTime: 300,
      throttleTime: 200
    }
  }
  
  // 高端设备配置
  return {
    enableAnimations: true,
    enableLazyLoad: false,
    imageQuality: 'high',
    chunkSize: 50,
    debounceTime: 200,
    throttleTime: 100
  }
}

// 性能监控
export class PerformanceMonitor {
  private static instance: PerformanceMonitor
  private metrics: Record<string, number[]> = {}
  
  static getInstance() {
    if (!this.instance) {
      this.instance = new PerformanceMonitor()
    }
    return this.instance
  }
  
  // 记录性能指标
  record(name: string, value: number) {
    if (!this.metrics[name]) {
      this.metrics[name] = []
    }
    this.metrics[name].push(value)
  }
  
  // 获取平均性能指标
  getAverage(name: string): number {
    const values = this.metrics[name] || []
    if (values.length === 0) return 0
    return values.reduce((sum, val) => sum + val, 0) / values.length
  }
  
  // 获取性能报告
  getReport() {
    const report: Record<string, { avg: number; min: number; max: number; count: number }> = {}
    
    for (const [name, values] of Object.entries(this.metrics)) {
      if (values.length === 0) continue
      
      report[name] = {
        avg: values.reduce((sum, val) => sum + val, 0) / values.length,
        min: Math.min(...values),
        max: Math.max(...values),
        count: values.length
      }
    }
    
    return report
  }
  
  // 清除指标
  clear() {
    this.metrics = {}
  }
}

// 监控页面加载性能
export function monitorPagePerformance() {
  const monitor = PerformanceMonitor.getInstance()
  
  // 监控页面加载时间
  window.addEventListener('load', () => {
    const navigation = performance.getEntriesByType('navigation')[0] as PerformanceNavigationTiming
    
    if (navigation) {
      monitor.record('domContentLoaded', navigation.domContentLoadedEventEnd - navigation.domContentLoadedEventStart)
      monitor.record('pageLoad', navigation.loadEventEnd - navigation.loadEventStart)
      monitor.record('firstPaint', performance.getEntriesByName('first-paint')[0]?.startTime || 0)
      monitor.record('firstContentfulPaint', performance.getEntriesByName('first-contentful-paint')[0]?.startTime || 0)
    }
  })
}

// 监控组件渲染性能
export function measureRenderPerformance(componentName: string) {
  return (target: any, propertyKey: string, descriptor: PropertyDescriptor) => {
    const originalMethod = descriptor.value
    
    descriptor.value = async function (...args: any[]) {
      const start = performance.now()
      const result = await originalMethod.apply(this, args)
      const end = performance.now()
      
      const monitor = PerformanceMonitor.getInstance()
      monitor.record(`${componentName}-${propertyKey}`, end - start)
      
      return result
    }
    
    return descriptor
  }
}

// 资源预加载
export function preloadResources(resources: string[]) {
  resources.forEach(resource => {
    const link = document.createElement('link')
    link.rel = 'preload'
    
    if (resource.endsWith('.js')) {
      link.as = 'script'
    } else if (resource.endsWith('.css')) {
      link.as = 'style'
    } else if (resource.match(/\.(png|jpg|jpeg|webp|svg)$/)) {
      link.as = 'image'
    } else if (resource.match(/\.(woff|woff2|ttf)$/)) {
      link.as = 'font'
      link.crossOrigin = 'anonymous'
    }
    
    link.href = resource
    document.head.appendChild(link)
  })
}