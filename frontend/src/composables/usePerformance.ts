/**
 * 性能优化组合式函数
 */
import { ref, computed, watch, onMounted, onUnmounted, nextTick, type Ref } from 'vue'
import { debounce, throttle, useNetworkStatus, getPerformanceConfig, PerformanceMonitor } from '@/utils/performance'

// 虚拟滚动
export function useVirtualScroll(options: {
  containerHeight: number
  itemHeight: number
  items: any[]
  bufferSize?: number
}) {
  const scrollTop = ref(0)
  const containerRef = ref<HTMLElement>()
  
  const config = getPerformanceConfig()
  const bufferSize = options.bufferSize || config.chunkSize
  
  const startIndex = computed(() => {
    const index = Math.floor(scrollTop.value / options.itemHeight)
    return Math.max(0, index - bufferSize)
  })
  
  const endIndex = computed(() => {
    const visibleCount = Math.ceil(options.containerHeight / options.itemHeight)
    const index = startIndex.value + visibleCount + bufferSize * 2
    return Math.min(options.items.length - 1, index)
  })
  
  const visibleItems = computed(() => {
    return options.items.slice(startIndex.value, endIndex.value + 1).map((item, index) => ({
      ...item,
      index: startIndex.value + index
    }))
  })
  
  const offsetY = computed(() => startIndex.value * options.itemHeight)
  const totalHeight = computed(() => options.items.length * options.itemHeight)
  
  const handleScroll = throttle((e: Event) => {
    scrollTop.value = (e.target as HTMLElement).scrollTop
  }, config.throttleTime)
  
  const scrollToItem = (index: number) => {
    if (!containerRef.value) return
    const targetScrollTop = index * options.itemHeight
    containerRef.value.scrollTop = targetScrollTop
  }
  
  return {
    containerRef,
    scrollTop,
    visibleItems,
    offsetY,
    totalHeight,
    handleScroll,
    scrollToItem
  }
}

// 无限滚动
export function useInfiniteScroll(options: {
  loadMore: () => Promise<void>
  hasMore: boolean
  threshold?: number
}) {
  const loading = ref(false)
  const containerRef = ref<HTMLElement>()
  
  const config = getPerformanceConfig()
  const threshold = options.threshold || 200
  
  const handleScroll = debounce(async () => {
    if (!containerRef.value || loading.value || !options.hasMore) return
    
    const { scrollTop, scrollHeight, clientHeight } = containerRef.value
    const distanceToBottom = scrollHeight - scrollTop - clientHeight
    
    if (distanceToBottom <= threshold) {
      loading.value = true
      try {
        await options.loadMore()
      } finally {
        loading.value = false
      }
    }
  }, config.debounceTime)
  
  onMounted(() => {
    if (containerRef.value) {
      containerRef.value.addEventListener('scroll', handleScroll)
    }
  })
  
  onUnmounted(() => {
    if (containerRef.value) {
      containerRef.value.removeEventListener('scroll', handleScroll)
    }
  })
  
  return {
    containerRef,
    loading,
    handleScroll
  }
}

// 懒加载组件
export function useLazyComponent<T extends () => Promise<any>>(
  loader: T,
  options: {
    loadingComponent?: any
    errorComponent?: any
    delay?: number
    timeout?: number
  } = {}
) {
  const component = ref<any>(null)
  const loading = ref(false)
  const error = ref<Error | null>(null)
  
  const config = getPerformanceConfig()
  const delay = options.delay || 200
  const timeout = options.timeout || 5000
  
  let loadTimer: NodeJS.Timeout | null = null
  let timeoutTimer: NodeJS.Timeout | null = null
  
  const load = async () => {
    if (loading.value) return
    
    loading.value = true
    error.value = null
    
    try {
      // 添加延迟以避免闪烁
      if (delay > 0) {
        await new Promise(resolve => {
          loadTimer = setTimeout(resolve, delay)
        })
      }
      
      // 设置超时
      if (timeout > 0) {
        timeoutTimer = setTimeout(() => {
          error.value = new Error('Component load timeout')
        }, timeout)
      }
      
      const module = await loader()
      component.value = module.default || module
      
    } catch (e) {
      error.value = e as Error
    } finally {
      loading.value = false
      if (loadTimer) clearTimeout(loadTimer)
      if (timeoutTimer) clearTimeout(timeoutTimer)
    }
  }
  
  return {
    component,
    loading,
    error,
    load
  }
}

// 自适应图片质量
export function useAdaptiveImage() {
  const networkStatus = useNetworkStatus()
  const config = getPerformanceConfig()
  
  const getImageUrl = (baseUrl: string, quality?: string) => {
    const imageQuality = quality || config.imageQuality
    
    // 根据网络状态和设备性能调整图片质量
    if (!networkStatus.isOnline.value) {
      return `${baseUrl}?quality=low&format=webp`
    }
    
    if (networkStatus.connectionType.value === 'slow-2g' || networkStatus.connectionType.value === '2g') {
      return `${baseUrl}?quality=low&format=webp`
    }
    
    if (networkStatus.connectionType.value === '3g') {
      return `${baseUrl}?quality=medium&format=webp`
    }
    
    return `${baseUrl}?quality=${imageQuality}`
  }
  
  return {
    getImageUrl
  }
}

// 性能监控
export function usePerformanceMonitor(componentName: string) {
  const monitor = PerformanceMonitor.getInstance()
  
  const startMeasure = (name: string) => {
    return performance.now()
  }
  
  const endMeasure = (name: string, startTime: number) => {
    const duration = performance.now() - startTime
    monitor.record(`${componentName}-${name}`, duration)
    return duration
  }
  
  const measureAsync = async <T>(name: string, fn: () => Promise<T>): Promise<T> => {
    const start = startMeasure(name)
    try {
      const result = await fn()
      endMeasure(name, start)
      return result
    } catch (error) {
      endMeasure(`${name}-error`, start)
      // 记录错误到监控系统
      console.error(`[Performance] ${componentName}-${name} failed:`, error)
      // TODO: 集成到监控平台 (Sentry, DataDog 等)
      throw error
    }
  }
  
  return {
    startMeasure,
    endMeasure,
    measureAsync
  }
}

// 防抖搜索
export function useDebounceSearch<T>(
  searchFn: (query: string) => Promise<T[]>,
  options: {
    delay?: number
    minLength?: number
  } = {}
) {
  const config = getPerformanceConfig()
  const delay = options.delay || config.debounceTime
  const minLength = options.minLength || 2
  
  const query = ref('')
  const results = ref<T[]>([])
  const loading = ref(false)
  const error = ref<Error | null>(null)
  
  const debouncedSearch = debounce(async (searchQuery: string) => {
    if (searchQuery.length < minLength) {
      results.value = []
      return
    }
    
    loading.value = true
    error.value = null
    
    try {
      const searchResults = await searchFn(searchQuery)
      results.value = searchResults
    } catch (e) {
      error.value = e as Error
    } finally {
      loading.value = false
    }
  }, delay)
  
  watch(query, (newQuery) => {
    debouncedSearch(newQuery)
  })
  
  return {
    query,
    results,
    loading,
    error
  }
}

// 优化的列表渲染
export function useOptimizedList<T>(
  items: Ref<T[]>,
  options: {
    itemKey?: keyof T | ((item: T) => string | number)
    threshold?: number
  } = {}
) {
  const config = getPerformanceConfig()
  const threshold = options.threshold || config.chunkSize
  
  const visibleRange = ref({ start: 0, end: threshold })
  const containerRef = ref<HTMLElement>()
  
  const visibleItems = computed(() => {
    return items.value.slice(visibleRange.value.start, visibleRange.value.end)
  })
  
  const updateVisibleRange = () => {
    if (!containerRef.value) return
    
    const { scrollTop, clientHeight } = containerRef.value
    const itemHeight = 50 // 假设每个项目高度为50px
    
    const start = Math.floor(scrollTop / itemHeight)
    const end = start + Math.ceil(clientHeight / itemHeight) + threshold
    
    visibleRange.value = {
      start: Math.max(0, start - threshold),
      end: Math.min(items.value.length, end + threshold)
    }
  }
  
  const handleScroll = throttle(updateVisibleRange, config.throttleTime)
  
  onMounted(() => {
    updateVisibleRange()
    if (containerRef.value) {
      containerRef.value.addEventListener('scroll', handleScroll)
    }
  })
  
  onUnmounted(() => {
    if (containerRef.value) {
      containerRef.value.removeEventListener('scroll', handleScroll)
    }
  })
  
  return {
    containerRef,
    visibleItems,
    updateVisibleRange,
    handleScroll
  }
}