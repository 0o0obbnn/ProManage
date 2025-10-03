import request from '../request';
import type {
  Notification,
  NotificationQueryParams,
  NotificationListResponse,
  NotificationStats,
  NotificationSettings
} from '@/types/notification';

/**
 * 获取通知列表
 */
export function getNotificationList(params?: NotificationQueryParams): Promise<NotificationListResponse> {
  return request.get('/api/notifications', { params });
}

/**
 * 获取通知详情
 */
export function getNotificationDetail(id: string): Promise<Notification> {
  return request.get(`/api/notifications/${id}`);
}

/**
 * 标记为已读
 */
export function markAsRead(id: string): Promise<void> {
  return request.put(`/api/notifications/${id}/read`);
}

/**
 * 全部标记为已读
 */
export function markAllAsRead(): Promise<void> {
  return request.put('/api/notifications/read-all');
}

/**
 * 删除通知
 */
export function deleteNotification(id: string): Promise<void> {
  return request.delete(`/api/notifications/${id}`);
}

/**
 * 批量删除通知
 */
export function batchDeleteNotifications(ids: string[]): Promise<void> {
  return request.post('/api/notifications/batch-delete', { ids });
}

/**
 * 获取未读数量
 */
export function getUnreadCount(): Promise<number> {
  return request.get('/api/notifications/unread-count');
}

/**
 * 获取统计信息
 */
export function getNotificationStats(): Promise<NotificationStats> {
  return request.get('/api/notifications/stats');
}

/**
 * 更新通知设置
 */
export function updateNotificationSettings(settings: NotificationSettings): Promise<void> {
  return request.put('/api/notifications/settings', settings);
}

/**
 * 获取通知设置
 */
export function getNotificationSettings(): Promise<NotificationSettings> {
  return request.get('/api/notifications/settings');
}

/**
 * 测试通知
 */
export function testNotification(type: string): Promise<void> {
  return request.post('/api/notifications/test', { type });
}
