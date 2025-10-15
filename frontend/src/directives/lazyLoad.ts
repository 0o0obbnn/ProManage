/**
 * 图片懒加载指令
 */
import type { App, DirectiveBinding } from 'vue'

interface LazyLoadElement extends HTMLElement {
  _lazyLoadObserver?: IntersectionObserver
  _lazyLoadSrc?: string
}

const createObserver = (
  el: LazyLoadElement,
  binding: DirectiveBinding<string>
) => {
  const observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting && !el._lazyLoadSrc) {
          const img = entry.target as HTMLImageElement
          const src = binding.value
          
          // 防止重复加载 - 立即标记
          if (!src || el.src === src || el._lazyLoadSrc) {
            observer.unobserve(img)
            return
          }
          el._lazyLoadSrc = src // 立即标记为加载中
          
          // 创建新图片对象预加载
          const newImg = new Image()
          const timeoutId = setTimeout(() => {
            // 加载超时处理
            img.src = '/images/placeholder-error.png'
            img.classList.remove('lazy-loading')
            img.classList.add('lazy-error')
            observer.unobserve(img)
          }, 5000) // 5秒超时
          
          newImg.onload = () => {
            clearTimeout(timeoutId)
            img.src = src
            img.classList.remove('lazy-loading')
            img.classList.add('lazy-loaded')
            observer.unobserve(img)
          }
          
          newImg.onerror = () => {
            clearTimeout(timeoutId)
            // 加载失败，显示默认图片
            img.src = '/images/placeholder-error.png'
            img.classList.remove('lazy-loading')
            img.classList.add('lazy-error')
            observer.unobserve(img)
          }
          
          // 开始加载
          newImg.src = src
          el._lazyLoadSrc = src
        }
      })
    },
    {
      rootMargin: '50px', // 提前50px开始加载
      threshold: 0.1
    }
  )
  
  observer.observe(el)
  el._lazyLoadObserver = observer
}

export const lazyLoad = {
  mounted(el: LazyLoadElement, binding: DirectiveBinding<string>) {
    // 验证绑定值
    if (!binding.value || typeof binding.value !== 'string') {
      console.warn('[v-lazy] Invalid image URL:', binding.value)
      return
    }
    
    // URL 安全验证
    if (!isValidImageUrl(binding.value)) {
      console.warn('[v-lazy] Unsafe image URL:', binding.value)
      el.src = '/images/placeholder-error.png'
      return
    }
    
    // 检查是否支持 IntersectionObserver
    if (!window.IntersectionObserver) {
      // 不支持则直接加载
      el.src = binding.value
      el.classList.add('lazy-loaded')
      return
    }
    
    // 设置占位图
    el.src = '/images/placeholder.png'
    el._lazyLoadSrc = undefined // 初始化为undefined
    
    // 添加加载中样式
    el.classList.add('lazy-loading')
    
    // 创建观察器
    createObserver(el, binding)
  },
  
  updated(el: LazyLoadElement, binding: DirectiveBinding<string>) {
    // 验证新值
    if (!binding.value || typeof binding.value !== 'string') {
      console.warn('[v-lazy] Invalid image URL:', binding.value)
      return
    }
    
    if (el._lazyLoadSrc !== binding.value) {
      // 如果图片地址变化，重置状态
      el._lazyLoadSrc = undefined
      el.classList.remove('lazy-loaded', 'lazy-error')
      el.classList.add('lazy-loading')
      
      // 断开旧观察器
      if (el._lazyLoadObserver) {
        el._lazyLoadObserver.disconnect()
      }
      
      // 创建新观察器
      createObserver(el, binding)
    }
  },
  
  unmounted(el: LazyLoadElement) {
    // 清理观察器
    if (el._lazyLoadObserver) {
      el._lazyLoadObserver.disconnect()
      delete el._lazyLoadObserver
    }
    el._lazyLoadSrc = undefined
  }
}

// URL 安全验证
function isValidImageUrl(url: string): boolean {
  try {
    const urlObj = new URL(url, window.location.origin)
    // 只允许 http/https 协议
    if (!['http:', 'https:', 'data:'].includes(urlObj.protocol)) {
      return false
    }
    // 检查文件扩展名
    const validExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.webp', '.svg']
    const hasValidExt = validExtensions.some(ext => urlObj.pathname.toLowerCase().endsWith(ext))
    return hasValidExt || urlObj.protocol === 'data:'
  } catch {
    return false
  }
}

export const setupLazyLoad = (app: App) => {
  app.directive('lazy', lazyLoad)
  
  // 添加全局样式（只添加一次）
  const styleId = 'lazy-load-styles'
  if (!document.getElementById(styleId)) {
    const style = document.createElement('style')
    style.id = styleId
    style.textContent = `
      img.lazy-loading {
        opacity: 0.6;
        filter: blur(2px);
        transition: opacity 0.3s, filter 0.3s;
      }
      
      img.lazy-loaded {
        opacity: 1;
        filter: blur(0);
      }
      
      img.lazy-error {
        opacity: 0.8;
        filter: grayscale(100%);
      }
    `
    document.head.appendChild(style)
  }
}