/**
 * Axios HTTP 客户端配置
 */
import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosError } from 'axios'
import { message } from 'ant-design-vue'
import type { ApiResponse } from '@/types/global'
import * as authApi from './modules/auth'

// 是否正在刷新 token 的标志
let isRefreshing = false
// 存储待重试的请求队列
let refreshSubscribers: ((token: string) => void)[] = []

/**
 * 订阅 token 刷新
 */
function subscribeTokenRefresh(callback: (token: string) => void) {
  refreshSubscribers.push(callback)
}

/**
 * 通知所有订阅者 token 已刷新
 */
function onRefreshed(token: string) {
  refreshSubscribers.forEach((callback) => callback(token))
  refreshSubscribers = []
}

/**
 * 错误报告函数
 */
function reportError(error: any, context: string) {
  // 在开发环境下输出详细错误信息
  if (import.meta.env.DEV) {
    console.error(`[API Error] ${context}:`, error)
  }
  
  // 可以在这里集成错误监控服务，如Sentry
  // Sentry.captureException(error, {
  //   contexts: {
  //     api: {
  //       context,
  //       url: error.config?.url,
  //       method: error.config?.method,
  //       timestamp: new Date().toISOString()
  //     }
  //   }
  // })
}

/**
 * 格式化错误消息
 */
function formatErrorMessage(error: any): string {
  if (error.response?.data?.message) {
    return error.response.data.message
  }
  
  if (error.message) {
    return error.message
  }
  
  return '请求失败'
}

// 创建 Axios 实例
const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    // 添加 token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    // 在开发环境下记录请求
    if (import.meta.env.DEV) {
      console.log('[API Request]', config.method?.toUpperCase(), config.url, config.data)
    }

    return config
  },
  (error: AxiosError) => {
    reportError(error, 'Request Interceptor')
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    const { code, message: msg, data } = response.data as ApiResponse

    if (code === 200) {
      // 在开发环境下记录成功响应
      if (import.meta.env.DEV) {
        console.log('[API Success]', response.config.method?.toUpperCase(), response.config.url, data)
      }
      return data
    } else {
      const errorMessage = msg || '请求失败'
      message.error(errorMessage)
      reportError(new Error(errorMessage), `API Response Error: ${response.config.url}`)
      return Promise.reject(new Error(errorMessage))
    }
  },
  async (error: AxiosError) => {
    const originalRequest = error.config as AxiosRequestConfig & { _retry?: boolean }

    // 记录错误
    reportError(error, `API Response Error: ${originalRequest?.url || 'Unknown URL'}`)

    if (error.response) {
      const { status } = error.response
      const data = error.response.data as ApiResponse
      const errorMessage = data?.message || formatErrorMessage(error)

      // 处理 401 未授权错误
      if (status === 401 && !originalRequest._retry) {
        if (isRefreshing) {
          // 如果正在刷新 token，将请求加入队列
          return new Promise((resolve) => {
            subscribeTokenRefresh((newToken: string) => {
              if (originalRequest.headers) {
                originalRequest.headers.Authorization = `Bearer ${newToken}`
              }
              resolve(service(originalRequest))
            })
          })
        }

        originalRequest._retry = true
        isRefreshing = true

        try {
          const refreshToken = localStorage.getItem('refreshToken')
          if (!refreshToken) {
            throw new Error('No refresh token')
          }

          // 刷新 token
          const response = await authApi.refreshToken(refreshToken)
          const { token, refreshToken: newRefreshToken } = response

          // 更新本地存储
          localStorage.setItem('token', token)
          localStorage.setItem('refreshToken', newRefreshToken)

          // 更新请求头
          if (originalRequest.headers) {
            originalRequest.headers.Authorization = `Bearer ${token}`
          }

          // 通知所有等待的请求
          onRefreshed(token)

          // 重试原始请求
          return service(originalRequest)
        } catch (refreshError) {
          // 刷新失败，清除 token 并跳转到登录页
          message.error('登录已过期，请重新登录')
          localStorage.removeItem('token')
          localStorage.removeItem('refreshToken')
          window.location.href = '/login'
          reportError(refreshError, 'Token Refresh Failed')
          return Promise.reject(refreshError)
        } finally {
          isRefreshing = false
        }
      } else if (status === 401) {
        // 刷新 token 后仍然 401，说明 refresh token 也过期了
        message.error('登录已过期，请重新登录')
        localStorage.removeItem('token')
        localStorage.removeItem('refreshToken')
        window.location.href = '/login'
      } else {
        // 处理其他错误
        switch (status) {
          case 403:
            message.error('权限不足')
            break
          case 404:
            message.error('请求资源不存在')
            break
          case 500:
            message.error('服务器错误')
            break
          default:
            message.error(errorMessage)
        }
      }
    } else if (error.request) {
      message.error('网络错误，请检查网络连接')
    } else {
      message.error('请求配置错误')
    }

    return Promise.reject(error)
  }
)

/**
 * GET 请求
 */
export function get<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
  return service.get(url, config)
}

/**
 * POST 请求
 */
export function post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
  return service.post(url, data, config)
}

/**
 * PUT 请求
 */
export function put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
  return service.put(url, data, config)
}

/**
 * DELETE 请求
 */
export function del<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
  return service.delete(url, config)
}

export default service