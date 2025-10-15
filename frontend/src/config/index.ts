/**
 * 应用配置管理
 */

export interface AppConfig {
  apiBaseUrl: string
  wsUrl: string
  enableMock: boolean
  logLevel: 'debug' | 'info' | 'warn' | 'error'
  timeout: number
  enableDevTools: boolean
  enablePerformanceMonitor: boolean
  maxFileSize: number
  allowedFileTypes: string[]
}

const development: AppConfig = {
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1',
  wsUrl: import.meta.env.VITE_WS_URL || 'ws://localhost:8080/ws',
  enableMock: false,
  logLevel: 'debug',
  timeout: 30000,
  enableDevTools: true,
  enablePerformanceMonitor: true,
  maxFileSize: 50 * 1024 * 1024, // 50MB
  allowedFileTypes: [
    'image/jpeg',
    'image/png',
    'image/gif',
    'image/webp',
    'application/pdf',
    'application/msword',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    'application/vnd.ms-excel',
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    'text/plain',
    'text/markdown'
  ]
}

const production: AppConfig = {
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL || '/api/v1',
  wsUrl: import.meta.env.VITE_WS_URL || `wss://${window.location.host}/ws`,
  enableMock: false,
  logLevel: 'error',
  timeout: 30000,
  enableDevTools: false,
  enablePerformanceMonitor: false,
  maxFileSize: 50 * 1024 * 1024,
  allowedFileTypes: [
    'image/jpeg',
    'image/png',
    'image/gif',
    'image/webp',
    'application/pdf',
    'application/msword',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    'application/vnd.ms-excel',
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    'text/plain',
    'text/markdown'
  ]
}

const test: AppConfig = {
  ...development,
  enableMock: true,
  logLevel: 'info'
}

const configs: Record<string, AppConfig> = {
  development,
  production,
  test
}

export const config: AppConfig = configs[import.meta.env.MODE] || development

// 常量配置
export const CONSTANTS = {
  // Token相关
  TOKEN_REFRESH_BUFFER: 300, // 5分钟
  TOKEN_STORAGE_KEY: 'token',
  REFRESH_TOKEN_STORAGE_KEY: 'refreshToken',
  
  // 请求相关
  REQUEST_TIMEOUT: 30000,
  REQUEST_RETRY_COUNT: 3,
  REQUEST_RETRY_DELAY: 1000,
  
  // 文件上传
  MAX_FILE_SIZE: 50 * 1024 * 1024,
  CHUNK_SIZE: 1024 * 1024, // 1MB
  
  // 分页
  DEFAULT_PAGE_SIZE: 20,
  MAX_PAGE_SIZE: 100,
  
  // 防抖节流
  DEBOUNCE_DELAY: 300,
  THROTTLE_DELAY: 200,
  
  // WebSocket
  WS_HEARTBEAT_INTERVAL: 30000,
  WS_RECONNECT_INTERVAL: 5000,
  WS_MAX_RECONNECT_ATTEMPTS: 5,
  
  // 缓存
  CACHE_EXPIRE_TIME: 300000, // 5分钟
  
  // 性能
  VIRTUAL_LIST_BUFFER: 5,
  VIRTUAL_LIST_ITEM_HEIGHT: 50
}

export default config
