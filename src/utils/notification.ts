import type { ToastNotificationOptions, BrowserNotificationOptions } from '@/types/notification';

/**
 * 请求浏览器通知权限
 */
export async function requestNotificationPermission(): Promise<NotificationPermission> {
  if (!('Notification' in window)) {
    console.warn('This browser does not support desktop notification');
    return 'denied';
  }

  if (Notification.permission === 'granted') {
    return 'granted';
  }

  if (Notification.permission !== 'denied') {
    const permission = await Notification.requestPermission();
    return permission;
  }

  return Notification.permission;
}

/**
 * 显示浏览器原生通知
 */
export function showBrowserNotification(
  title: string,
  options?: BrowserNotificationOptions
): Notification | null {
  if (!('Notification' in window)) {
    console.warn('This browser does not support desktop notification');
    return null;
  }

  if (Notification.permission !== 'granted') {
    console.warn('Notification permission not granted');
    return null;
  }

  try {
    const notification = new Notification(title, {
      body: options?.body,
      icon: options?.icon || '/logo.png',
      badge: options?.badge,
      tag: options?.tag,
      data: options?.data,
      requireInteraction: options?.requireInteraction || false
    });

    return notification;
  } catch (error) {
    console.error('Failed to show browser notification:', error);
    return null;
  }
}

/**
 * 播放通知音效
 */
export function playNotificationSound(soundUrl: string = '/sounds/notification.mp3'): void {
  try {
    const audio = new Audio(soundUrl);
    audio.volume = 0.5;
    audio.play().catch(err => {
      console.warn('Failed to play notification sound:', err);
    });
  } catch (error) {
    console.error('Failed to play notification sound:', error);
  }
}

/**
 * 通知类型图标映射
 */
export const notificationTypeIcons: Record<string, string> = {
  task_assigned: 'user-add',
  task_mentioned: 'at',
  change_request: 'file-text',
  review_request: 'eye',
  approval_pending: 'check-circle',
  comment_reply: 'message',
  system: 'bell',
  deadline_reminder: 'clock-circle',
  status_changed: 'swap',
  priority_changed: 'arrow-up'
};

/**
 * 通知类型颜色映射
 */
export const notificationTypeColors: Record<string, string> = {
  task_assigned: '#1890ff',
  task_mentioned: '#722ed1',
  change_request: '#13c2c2',
  review_request: '#52c41a',
  approval_pending: '#fa8c16',
  comment_reply: '#eb2f96',
  system: '#8c8c8c',
  deadline_reminder: '#f5222d',
  status_changed: '#1890ff',
  priority_changed: '#fa541c'
};

/**
 * 通知类型文本映射
 */
export const notificationTypeTexts: Record<string, string> = {
  task_assigned: '任务分配',
  task_mentioned: '提到我的',
  change_request: '变更请求',
  review_request: '评审请求',
  approval_pending: '待审批',
  comment_reply: '评论回复',
  system: '系统通知',
  deadline_reminder: '截止提醒',
  status_changed: '状态变更',
  priority_changed: '优先级变更'
};

/**
 * 格式化通知时间
 */
export function formatNotificationTime(dateString: string): string {
  const date = new Date(dateString);
  const now = new Date();
  const diff = now.getTime() - date.getTime();

  const seconds = Math.floor(diff / 1000);
  const minutes = Math.floor(seconds / 60);
  const hours = Math.floor(minutes / 60);
  const days = Math.floor(hours / 24);

  if (seconds < 60) {
    return '刚刚';
  } else if (minutes < 60) {
    return `${minutes}分钟前`;
  } else if (hours < 24) {
    return `${hours}小时前`;
  } else if (days < 7) {
    return `${days}天前`;
  } else {
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    });
  }
}

/**
 * 检查是否在免打扰时段
 */
export function isInDoNotDisturbPeriod(settings: {
  enabled: boolean;
  workdayStart?: string;
  workdayEnd?: string;
  weekendEnabled?: boolean;
}): boolean {
  if (!settings.enabled) {
    return false;
  }

  const now = new Date();
  const day = now.getDay();
  const isWeekend = day === 0 || day === 6;

  // 周末免打扰
  if (isWeekend && settings.weekendEnabled) {
    return true;
  }

  // 工作日时段免打扰
  if (!isWeekend && settings.workdayStart && settings.workdayEnd) {
    const [startHour, startMinute] = settings.workdayStart.split(':').map(Number);
    const [endHour, endMinute] = settings.workdayEnd.split(':').map(Number);

    const currentMinutes = now.getHours() * 60 + now.getMinutes();
    const startMinutes = startHour * 60 + startMinute;
    const endMinutes = endHour * 60 + endMinute;

    if (startMinutes <= endMinutes) {
      return currentMinutes >= startMinutes && currentMinutes <= endMinutes;
    } else {
      // 跨越午夜的情况
      return currentMinutes >= startMinutes || currentMinutes <= endMinutes;
    }
  }

  return false;
}

/**
 * 生成通知摘要
 */
export function generateNotificationSummary(content: string, maxLength: number = 50): string {
  if (content.length <= maxLength) {
    return content;
  }
  return content.substring(0, maxLength) + '...';
}

/**
 * 获取优先级标签配置
 */
export function getPriorityConfig(priority?: string): {
  color: string;
  text: string;
} {
  const configs: Record<string, { color: string; text: string }> = {
    urgent: { color: 'red', text: '紧急' },
    high: { color: 'orange', text: '高' },
    normal: { color: 'blue', text: '中' },
    low: { color: 'default', text: '低' }
  };

  return configs[priority || 'normal'] || configs.normal;
}

/**
 * 分组通知
 */
export function groupNotificationsByDate(notifications: any[]): {
  today: any[];
  yesterday: any[];
  earlier: any[];
} {
  const now = new Date();
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  const yesterday = new Date(today.getTime() - 24 * 60 * 60 * 1000);

  const result = {
    today: [] as any[],
    yesterday: [] as any[],
    earlier: [] as any[]
  };

  notifications.forEach(notification => {
    const date = new Date(notification.createdAt);
    if (date >= today) {
      result.today.push(notification);
    } else if (date >= yesterday) {
      result.yesterday.push(notification);
    } else {
      result.earlier.push(notification);
    }
  });

  return result;
}
