/**
 * Service Worker 注册和管理
 */

const SW_VERSION = '1.0.0'
const SW_URL = `/sw.js?v=${SW_VERSION}`

export function registerServiceWorker() {
  if (!('serviceWorker' in navigator)) {
    console.warn('Service Worker not supported')
    return Promise.resolve()
  }

  return navigator.serviceWorker.register(SW_URL)
    .then((registration) => {
      console.log('Service Worker registered:', registration)

      // 检查更新
      registration.addEventListener('updatefound', () => {
        const newWorker = registration.installing
        if (!newWorker) return

        newWorker.addEventListener('statechange', () => {
          if (newWorker.state === 'installed' && navigator.serviceWorker.controller) {
            // 新版本可用，提示用户刷新
            showUpdatePrompt()
          }
        })
      })

      // 定期检查更新
      setInterval(() => {
        registration.update()
      }, 60 * 60 * 1000) // 每小时检查一次

      return registration
    })
    .catch((error) => {
      console.error('Service Worker registration failed:', error)
    })
}

// 显示更新提示
function showUpdatePrompt() {
  // 这里可以使用 Ant Design Vue 的 Modal 或其他 UI 组件
  if (confirm('发现新版本，是否立即更新？')) {
    window.location.reload()
  }
}

// 取消注册 Service Worker
export function unregisterServiceWorker() {
  if (!('serviceWorker' in navigator)) {
    return Promise.resolve()
  }

  return navigator.serviceWorker.ready
    .then((registration) => {
      return registration.unregister()
    })
    .then(() => {
      console.log('Service Worker unregistered')
    })
    .catch((error) => {
      console.error('Service Worker unregistration failed:', error)
    })
}

// 获取 Service Worker 状态
export function getServiceWorkerStatus() {
  if (!('serviceWorker' in navigator)) {
    return 'unsupported'
  }

  if (navigator.serviceWorker.controller) {
    return 'controlled'
  }

  return 'not-controlled'
}

// 手动触发更新
export function triggerServiceWorkerUpdate() {
  if (!('serviceWorker' in navigator)) {
    return Promise.resolve()
  }

  return navigator.serviceWorker.ready
    .then((registration) => {
      return registration.update()
    })
    .then(() => {
      console.log('Service Worker update triggered')
    })
    .catch((error) => {
      console.error('Service Worker update failed:', error)
    })
}

// 发送消息给 Service Worker
export function sendMessageToSW(message: any) {
  if (!('serviceWorker' in navigator) || !navigator.serviceWorker.controller) {
    return Promise.reject(new Error('Service Worker not controlled'))
  }

  return new Promise((resolve, reject) => {
    const messageChannel = new MessageChannel()
    
    messageChannel.port1.onmessage = (event) => {
      if (event.data.error) {
        reject(event.data.error)
      } else {
        resolve(event.data)
      }
    }
    
    navigator.serviceWorker.controller.postMessage(message, [messageChannel.port2])
  })
}

// 监听 Service Worker 消息
export function onServiceWorkerMessage(callback: (event: MessageEvent) => void) {
  if (!('serviceWorker' in navigator)) {
    return
  }

  navigator.serviceWorker.addEventListener('message', callback)
}

// 清除所有缓存
export function clearAllCaches() {
  if (!('serviceWorker' in navigator)) {
    return Promise.resolve()
  }

  return sendMessageToSW({ type: 'CLEAR_CACHES' })
    .then(() => {
      console.log('All caches cleared')
    })
    .catch((error) => {
      console.error('Failed to clear caches:', error)
    })
}