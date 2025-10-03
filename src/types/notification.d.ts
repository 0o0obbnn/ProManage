/**
 * 通知类型枚举
 */
export enum NotificationType {
  TASK_ASSIGNED = 'task_assigned',
  TASK_MENTIONED = 'task_mentioned',
  CHANGE_REQUEST = 'change_request',
  REVIEW_REQUEST = 'review_request',
  APPROVAL_PENDING = 'approval_pending',
  COMMENT_REPLY = 'comment_reply',
  SYSTEM = 'system',
  DEADLINE_REMINDER = 'deadline_reminder',
  STATUS_CHANGED = 'status_changed',
  PRIORITY_CHANGED = 'priority_changed'
}

/**
 * 通知相关类型
 */
export enum NotificationRelatedType {
  TASK = 'task',
  DOCUMENT = 'document',
  CHANGE = 'change',
  TEST = 'test',
  COMMENT = 'comment',
  PROJECT = 'project'
}

/**
 * 通知接口
 */
export interface Notification {
  id: string;
  type: NotificationType;
  title: string;
  content: string;
  read: boolean;
  createdAt: string;
  from?: {
    id: string;
    name: string;
    avatar?: string;
  };
  relatedId?: string;
  relatedType?: NotificationRelatedType;
  projectId?: string;
  projectName?: string;
  priority?: 'low' | 'normal' | 'high' | 'urgent';
  actionRequired?: boolean;
  expiresAt?: string;
  metadata?: Record<string, any>;
}

/**
 * 通知查询参数
 */
export interface NotificationQueryParams {
  page?: number;
  pageSize?: number;
  read?: boolean;
  type?: NotificationType[];
  projectId?: string;
  startDate?: string;
  endDate?: string;
  keyword?: string;
  actionRequired?: boolean;
}

/**
 * 通知统计信息
 */
export interface NotificationStats {
  unreadCount: number;
  todayCount: number;
  actionRequiredCount: number;
  byType: {
    [key in NotificationType]?: number;
  };
  byProject: {
    projectId: string;
    projectName: string;
    count: number;
  }[];
}

/**
 * 通知设置接口
 */
export interface NotificationSettings {
  channels: {
    inApp: boolean;
    email: boolean;
    push: boolean;
  };
  types: {
    [key in NotificationType]: {
      enabled: boolean;
      channels: {
        inApp: boolean;
        email: boolean;
        push: boolean;
      };
    };
  };
  doNotDisturb: {
    enabled: boolean;
    workdayStart?: string;
    workdayEnd?: string;
    weekendEnabled?: boolean;
  };
  sound: boolean;
  browserNotification: boolean;
}

/**
 * 通知列表响应
 */
export interface NotificationListResponse {
  list: Notification[];
  total: number;
  page: number;
  pageSize: number;
}

/**
 * Toast通知选项
 */
export interface ToastNotificationOptions {
  id?: string;
  type: 'info' | 'success' | 'warning' | 'error';
  title: string;
  content?: string;
  duration?: number;
  link?: string;
  onClose?: () => void;
  onClick?: () => void;
}

/**
 * 浏览器通知选项
 */
export interface BrowserNotificationOptions {
  body?: string;
  icon?: string;
  badge?: string;
  tag?: string;
  data?: any;
  requireInteraction?: boolean;
}
