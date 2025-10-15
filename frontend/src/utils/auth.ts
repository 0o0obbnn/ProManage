/**
 * 认证工具函数
 */
import { SecureStorage } from './storage'

/**
 * Token存储键
 */
export const TOKEN_KEY = 'token'
export const REFRESH_TOKEN_KEY = 'refreshToken'
export const USER_INFO_KEY = 'userInfo'

/**
 * 获取Token
 */
export function getToken(): string | null {
  return SecureStorage.getItem(TOKEN_KEY, false) // Token不加密，由后端验证
}

/**
 * 设置Token
 */
export function setToken(token: string): void {
  SecureStorage.setItem(TOKEN_KEY, token, false)
}

/**
 * 移除Token
 */
export function removeToken(): void {
  SecureStorage.removeItem(TOKEN_KEY)
}

/**
 * 获取RefreshToken
 */
export function getRefreshToken(): string | null {
  return SecureStorage.getItem(REFRESH_TOKEN_KEY, false)
}

/**
 * 设置RefreshToken
 */
export function setRefreshToken(refreshToken: string): void {
  SecureStorage.setItem(REFRESH_TOKEN_KEY, refreshToken, false)
}

/**
 * 移除RefreshToken
 */
export function removeRefreshToken(): void {
  SecureStorage.removeItem(REFRESH_TOKEN_KEY)
}

/**
 * 清除所有认证信息
 */
export function clearAuth(): void {
  SecureStorage.removeItem(TOKEN_KEY)
  SecureStorage.removeItem(REFRESH_TOKEN_KEY)
  SecureStorage.removeItem(USER_INFO_KEY)
}

/**
 * 解析JWT Token
 */
export function parseJWT(token: string): any {
  try {
    const base64Url = token.split('.')[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    )
    return JSON.parse(jsonPayload)
  } catch (error) {
    console.error('Failed to parse JWT:', error)
    return null
  }
}

/**
 * 检查Token是否过期
 * @param token JWT token
 * @param bufferTime 提前刷新时间（秒），默认5分钟
 */
export function isTokenExpired(token: string, bufferTime: number = 300): boolean {
  const payload = parseJWT(token)
  if (!payload || !payload.exp) {
    return true
  }

  const currentTime = Math.floor(Date.now() / 1000)
  // 如果距离过期时间小于bufferTime秒，认为token即将过期
  return payload.exp - currentTime < bufferTime
}

/**
 * 检查Token是否即将过期（5分钟内）
 */
export function isTokenExpiringSoon(token: string): boolean {
  return isTokenExpired(token, 300) // 5分钟
}

/**
 * 获取Token剩余有效时间（秒）
 */
export function getTokenRemainingTime(token: string): number {
  const payload = parseJWT(token)
  if (!payload || !payload.exp) {
    return 0
  }

  const currentTime = Math.floor(Date.now() / 1000)
  return Math.max(0, payload.exp - currentTime)
}

/**
 * 检查是否有有效的Token
 */
export function hasValidToken(): boolean {
  const token = getToken()
  if (!token) {
    return false
  }

  return !isTokenExpired(token, 0)
}
