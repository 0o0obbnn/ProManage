/**
 * 认证相关 API
 */
import { post } from '../request'
import type { LoginRequest, LoginResponse, UserInfo } from '@/types/global'

/**
 * 注册请求参数
 */
export interface RegisterRequest {
  username: string
  email: string
  password: string
  confirmPassword: string
  phone?: string
  realName?: string
  verificationCode?: string
}

/**
 * 用户登录
 */
export function login(data: LoginRequest) {
  return post<LoginResponse>('/auth/login', data)
}

/**
 * 用户注册
 */
export function register(data: RegisterRequest) {
  return post<UserInfo>('/auth/register', data)
}

/**
 * 用户登出
 */
export function logout() {
  return post('/auth/logout')
}

/**
 * 刷新 Token
 */
export function refreshToken(refreshToken: string) {
  return post<LoginResponse>('/auth/refresh', { refreshToken })
}

/**
 * 获取当前用户信息
 */
export function getCurrentUser() {
  return get<UserInfo>('/auth/me')
}

/**
 * 获取当前用户权限列表
 */
export function getUserPermissions() {
  return get<string[]>('/auth/permissions')
}

/**
 * 发送密码重置验证码
 */
export function sendResetCode(email: string) {
  return post('/auth/forgot-password/send-code', { email })
}

/**
 * 重置密码
 */
export interface ResetPasswordRequest {
  email: string
  verificationCode: string
  newPassword: string
  confirmPassword: string
}

export function resetPassword(data: ResetPasswordRequest) {
  return post('/auth/forgot-password/reset', data)
}

/**
 * 修改密码（已登录用户）
 */
export interface ChangePasswordRequest {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}

export function changePassword(data: ChangePasswordRequest) {
  return post('/auth/change-password', data)
}