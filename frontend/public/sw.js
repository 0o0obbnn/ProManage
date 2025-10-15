/**
 * Service Worker for performance optimization
 */

const CACHE_NAME = 'promanage-v1'
const STATIC_CACHE = 'promanage-static-v1'
const RUNTIME_CACHE = 'promanage-runtime-v1'

// 需要预缓存的静态资源
const STATIC_ASSETS = [
  '/',
  '/index.html',
  '/favicon.ico',
  '/logo.svg',
  '/images/placeholder.png',
  '/images/placeholder-error.png'
]

// 安装事件 - 预缓存静态资源
self.addEventListener('install', (event) => {
  console.log('Service Worker installing...')
  event.waitUntil(
    caches.open(STATIC_CACHE)
      .then((cache) => {
        console.log('Caching static assets')
        return cache.addAll(STATIC_ASSETS)
      })
      .then(() => self.skipWaiting())
  )
})

// 激活事件 - 清理旧缓存
self.addEventListener('activate', (event) => {
  console.log('Service Worker activating...')
  event.waitUntil(
    caches.keys()
      .then((cacheNames) => {
        return Promise.all(
          cacheNames.map((cacheName) => {
            if (cacheName !== STATIC_CACHE && cacheName !== RUNTIME_CACHE) {
              console.log('Deleting old cache:', cacheName)
              return caches.delete(cacheName)
            }
          })
        )
      })
      .then(() => self.clients.claim())
  )
})

// 缓存大小限制 (50MB)
const MAX_CACHE_SIZE = 50 * 1024 * 1024
const MAX_CACHE_ITEMS = 100

// 网络请求拦截
self.addEventListener('fetch', (event) => {
  const { request } = event
  const url = new URL(request.url)

  // 跳过非 HTTP 请求
  if (!request.url.startsWith('http')) {
    return
  }

  // 静态资源 - 缓存优先策略
  if (isStaticAsset(request.url)) {
    event.respondWith(
      caches.match(request)
        .then((response) => {
          if (response) {
            return response
          }
          return fetch(request)
            .then((response) => {
              // 缓存成功的响应
              if (response.ok) {
                const responseClone = response.clone()
                caches.open(STATIC_CACHE)
                  .then((cache) => {
                    limitCacheSize(cache, MAX_CACHE_ITEMS)
                    return cache.put(request, responseClone)
                  })
              }
              return response
            })
        })
    )
    return
  }

  // API 请求 - 网络优先策略
  if (url.pathname.startsWith('/api')) {
    event.respondWith(
      fetch(request)
        .catch(() => {
          // 网络失败时尝试从缓存获取
          return caches.match(request)
        })
    )
    return
  }

  // 其他请求 - 网络优先，失败时使用缓存
  event.respondWith(
    fetch(request)
      .then((response) => {
        // 缓存成功的响应
        if (response.ok) {
          const responseClone = response.clone()
          caches.open(RUNTIME_CACHE)
            .then((cache) => {
              limitCacheSize(cache, MAX_CACHE_ITEMS)
              return cache.put(request, responseClone)
            })
        }
        return response
      })
      .catch(() => {
        // 网络失败时尝试从缓存获取
        return caches.match(request)
      })
  )
})

// 判断是否为静态资源
function isStaticAsset(url) {
  return /\.(js|css|png|jpg|jpeg|gif|svg|ico|woff|woff2|ttf|eot)$/i.test(url)
}

// 限制缓存大小 (LRU)
async function limitCacheSize(cache, maxItems) {
  const keys = await cache.keys()
  if (keys.length > maxItems) {
    // 删除最旧的缓存
    await cache.delete(keys[0])
    await limitCacheSize(cache, maxItems)
  }
}

// 后台同步事件
self.addEventListener('sync', (event) => {
  if (event.tag === 'background-sync') {
    event.waitUntil(doBackgroundSync())
  }
})

// 后台同步处理
async function doBackgroundSync() {
  // 处理离线时的请求队列
  const queue = await getOfflineQueue()
  for (const request of queue) {
    try {
      await fetch(request)
      await removeFromQueue(request)
    } catch (error) {
      console.error('Background sync failed:', error)
    }
  }
}

// 获取离线请求队列
async function getOfflineQueue() {
  // 这里应该从 IndexedDB 获取离线请求
  return []
}

// 从队列中移除请求
async function removeFromQueue(request) {
  // 这里应该从 IndexedDB 移除请求
}

// 推送通知事件
self.addEventListener('push', (event) => {
  if (event.data) {
    const data = event.data.json()
    const options = {
      body: data.body,
      icon: '/logo.svg',
      badge: '/favicon.ico',
      vibrate: [100, 50, 100],
      data: {
        dateOfArrival: Date.now(),
        primaryKey: 1
      }
    }
    
    event.waitUntil(
      self.registration.showNotification(data.title, options)
    )
  }
})

// 通知点击事件
self.addEventListener('notificationclick', (event) => {
  event.notification.close()
  
  event.waitUntil(
    clients.openWindow('/')
  )
})