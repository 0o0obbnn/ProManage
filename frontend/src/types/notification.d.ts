/**
 * 通知类型定义
 */

/**
 * 通知类型枚举
 */
export enum NotificationType {
  SYSTEM = 'SYSTEM',           // 系统通知
  TASK = 'TASK',               // 任务通知
  DOCUMENT = 'DOCUMENT',       // 文档通知
  CHANGE = 'CHANGE',           // 变更通知
  TEST = 'TEST',               // 测试通知
  COMMENT = 'COMMENT',         // 评论通知
  MENTION = 'MENTION',         // @提及通知
  APPROVAL = 'APPROVAL'        // 审批通知
}

/**
 * 通知优先级
 */
export enum NotificationPriority {
  LOW = 'LOW',
  NORMAL = 'NORMAL',
  HIGH = 'HIGH',
  URGENT = 'URGENT'
}

/**
 * 通知接口
 */
export interface Notification {
  id: number
  type: NotificationType
  priority: NotificationPriority
  title: string
  content: string
  isRead: boolean
  relatedId?: number          // 关联对象ID
  relatedType?: string        // 关联对象类型
  link?: string               // 跳转链接
  sender?: {
    id: number
    name: string
    avatar?: string
  }
  createdAt: string
  readAt?: string
}

/**
 * 通知查询参数
 */
export interface NotificationQueryParams {
  page?: number
  pageSize?: number
  type?: NotificationType
  isRead?: boolean
  startDate?: string
  endDate?: string
}

/**
 * 未读数统计
 */
export interface UnreadCount {
  total: number
  byType: {
    [key in NotificationType]?: number
  }
}

/**
 * 通知设置
 */
export interface NotificationSettings {
  emailEnabled: boolean
  pushEnabled: boolean
  types: {
    [key in NotificationType]?: boolean
  }
}

