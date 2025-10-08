/**
 * 用户管理 API
 */
import { get, post, put, del } from '../request'
import type { PageResult } from '@/types/global'

/**
 * 用户接口
 */
export interface User {
  id: number
  username: string
  realName?: string
  email?: string
  avatar?: string
  phone?: string
  department?: string
  position?: string
  status?: string
  createdAt?: string
  updatedAt?: string
}

/**
 * 用户查询参数
 */
export interface UserQueryParams {
  page: number
  pageSize: number
  keyword?: string
  status?: string
  department?: string
}

/**
 * 获取用户列表
 */
export function getUserList(params?: UserQueryParams) {
  return get<PageResult<User>>('/api/v1/users', { params })
}

/**
 * 获取用户详情
 */
export function getUserDetail(id: number) {
  return get<User>(`/api/v1/users/${id}`)
}

/**
 * 创建用户
 */
export function createUser(data: Partial<User>) {
  return post<User>('/api/v1/users', data)
}

/**
 * 更新用户
 */
export function updateUser(id: number, data: Partial<User>) {
  return put<User>(`/api/v1/users/${id}`, data)
}

/**
 * 删除用户
 */
export function deleteUser(id: number) {
  return del(`/api/v1/users/${id}`)
}

/**
 * 获取当前用户信息
 */
export function getCurrentUser() {
  return get<User>('/api/v1/users/current')
}

/**
 * 更新当前用户信息
 */
export function updateCurrentUser(data: Partial<User>) {
  return put<User>('/api/v1/users/current', data)
}

/**
 * 修改密码
 */
export function changePassword(data: { oldPassword: string; newPassword: string }) {
  return post('/api/v1/users/change-password', data)
}

/**
 * 重置密码
 */
export function resetPassword(userId: number) {
  return post(`/api/v1/users/${userId}/reset-password`)
}

/**
 * 搜索用户
 */
export function searchUsers(keyword: string) {
  return get<User[]>('/api/v1/users/search', { params: { keyword } })
}

