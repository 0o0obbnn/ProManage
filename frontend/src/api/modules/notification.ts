/**
 * 通知管理 API
 */
import { get, post, put, del } from '../request'
import type { PageResult } from '@/types/global'
import type {
  Notification,
  NotificationQueryParams,
  UnreadCount,
  NotificationSettings
} from '@/types/notification'

/**
 * 获取通知列表
 */
export function getNotifications(params?: NotificationQueryParams) {
  return get<PageResult<Notification>>('/api/v1/notifications', { params })
}

/**
 * 获取通知详情
 */
export function getNotificationDetail(id: number) {
  return get<Notification>(`/api/v1/notifications/${id}`)
}

/**
 * 获取未读通知数量
 */
export function getUnreadCount() {
  return get<UnreadCount>('/api/v1/notifications/unread-count')
}

/**
 * 标记通知为已读
 */
export function markAsRead(id: number) {
  return put(`/api/v1/notifications/${id}/read`)
}

/**
 * 标记所有通知为已读
 */
export function markAllAsRead() {
  return put('/api/v1/notifications/read-all')
}

/**
 * 删除通知
 */
export function deleteNotification(id: number) {
  return del(`/api/v1/notifications/${id}`)
}

/**
 * 批量删除通知
 */
export function batchDeleteNotifications(ids: number[]) {
  return post('/api/v1/notifications/batch-delete', { ids })
}

/**
 * 清空已读通知
 */
export function clearReadNotifications() {
  return del('/api/v1/notifications/clear-read')
}

/**
 * 获取通知设置
 */
export function getNotificationSettings() {
  return get<NotificationSettings>('/api/v1/notifications/settings')
}

/**
 * 更新通知设置
 */
export function updateNotificationSettings(settings: Partial<NotificationSettings>) {
  return put<NotificationSettings>('/api/v1/notifications/settings', settings)
}

/**
 * 导出通知API
 */
export const notificationApi = {
  getNotifications,
  getNotificationDetail,
  getUnreadCount,
  markAsRead,
  markAllAsRead,
  deleteNotification,
  batchDeleteNotifications,
  clearReadNotifications,
  getNotificationSettings,
  updateNotificationSettings
}

export default notificationApi

