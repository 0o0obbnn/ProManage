import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type {
  Notification,
  NotificationQueryParams,
  NotificationStats,
  NotificationSettings,
  NotificationType,
  ToastNotificationOptions
} from '@/types/notification';
import * as notificationApi from '@/api/modules/notification';

export const useNotificationStore = defineStore('notification', () => {
  // State
  const notifications = ref<Notification[]>([]);
  const total = ref(0);
  const loading = ref(false);
  const unreadCount = ref(0);
  const stats = ref<NotificationStats | null>(null);
  const settings = ref<NotificationSettings | null>(null);
  const queryParams = ref<NotificationQueryParams>({
    page: 1,
    pageSize: 20
  });
  const toastQueue = ref<ToastNotificationOptions[]>([]);
  const wsConnected = ref(false);
  let wsInstance: WebSocket | null = null;
  let pollTimer: number | null = null;

  // Getters
  const unreadNotifications = computed(() => notifications.value.filter(n => !n.read));
  const actionRequiredNotifications = computed(() => notifications.value.filter(n => n.actionRequired && !n.read));
  const mentionedNotifications = computed(() =>
    notifications.value.filter(n => n.type === 'task_mentioned' && !n.read)
  );

  // Actions
  /**
   * 获取通知列表
   */
  async function fetchNotifications(params?: NotificationQueryParams) {
    loading.value = true;
    try {
      const response = await notificationApi.getNotificationList({
        ...queryParams.value,
        ...params
      });
      notifications.value = response.list;
      total.value = response.total;
      queryParams.value = {
        ...queryParams.value,
        ...params
      };
    } catch (error) {
      console.error('Failed to fetch notifications:', error);
      throw error;
    } finally {
      loading.value = false;
    }
  }

  /**
   * 获取通知详情
   */
  async function fetchNotificationDetail(id: string): Promise<Notification> {
    try {
      return await notificationApi.getNotificationDetail(id);
    } catch (error) {
      console.error('Failed to fetch notification detail:', error);
      throw error;
    }
  }

  /**
   * 标记为已读
   */
  async function markAsRead(id: string) {
    try {
      await notificationApi.markAsRead(id);
      const notification = notifications.value.find(n => n.id === id);
      if (notification && !notification.read) {
        notification.read = true;
        unreadCount.value = Math.max(0, unreadCount.value - 1);
      }
    } catch (error) {
      console.error('Failed to mark as read:', error);
      throw error;
    }
  }

  /**
   * 全部标记为已读
   */
  async function markAllAsRead() {
    try {
      await notificationApi.markAllAsRead();
      notifications.value.forEach(n => {
        n.read = true;
      });
      unreadCount.value = 0;
    } catch (error) {
      console.error('Failed to mark all as read:', error);
      throw error;
    }
  }

  /**
   * 删除通知
   */
  async function deleteNotification(id: string) {
    try {
      await notificationApi.deleteNotification(id);
      const index = notifications.value.findIndex(n => n.id === id);
      if (index > -1) {
        const notification = notifications.value[index];
        if (!notification.read) {
          unreadCount.value = Math.max(0, unreadCount.value - 1);
        }
        notifications.value.splice(index, 1);
        total.value--;
      }
    } catch (error) {
      console.error('Failed to delete notification:', error);
      throw error;
    }
  }

  /**
   * 批量删除通知
   */
  async function batchDelete(ids: string[]) {
    try {
      await notificationApi.batchDeleteNotifications(ids);
      ids.forEach(id => {
        const index = notifications.value.findIndex(n => n.id === id);
        if (index > -1) {
          const notification = notifications.value[index];
          if (!notification.read) {
            unreadCount.value = Math.max(0, unreadCount.value - 1);
          }
          notifications.value.splice(index, 1);
          total.value--;
        }
      });
    } catch (error) {
      console.error('Failed to batch delete notifications:', error);
      throw error;
    }
  }

  /**
   * 获取未读数量
   */
  async function fetchUnreadCount() {
    try {
      unreadCount.value = await notificationApi.getUnreadCount();
    } catch (error) {
      console.error('Failed to fetch unread count:', error);
    }
  }

  /**
   * 获取统计信息
   */
  async function fetchStats() {
    try {
      stats.value = await notificationApi.getNotificationStats();
      unreadCount.value = stats.value.unreadCount;
    } catch (error) {
      console.error('Failed to fetch stats:', error);
      throw error;
    }
  }

  /**
   * 获取通知设置
   */
  async function fetchSettings() {
    try {
      settings.value = await notificationApi.getNotificationSettings();
    } catch (error) {
      console.error('Failed to fetch settings:', error);
      throw error;
    }
  }

  /**
   * 更新通知设置
   */
  async function updateSettings(newSettings: NotificationSettings) {
    try {
      await notificationApi.updateNotificationSettings(newSettings);
      settings.value = newSettings;
    } catch (error) {
      console.error('Failed to update settings:', error);
      throw error;
    }
  }

  /**
   * 添加Toast通知
   */
  function addToast(options: ToastNotificationOptions) {
    const id = options.id || `toast-${Date.now()}-${Math.random()}`;
    toastQueue.value.push({
      ...options,
      id
    });

    // 自动移除
    const duration = options.duration || 5000;
    setTimeout(() => {
      removeToast(id);
    }, duration);
  }

  /**
   * 移除Toast通知
   */
  function removeToast(id: string) {
    const index = toastQueue.value.findIndex(t => t.id === id);
    if (index > -1) {
      toastQueue.value.splice(index, 1);
    }
  }

  /**
   * 连接WebSocket
   */
  function connectWebSocket() {
    if (wsConnected.value || wsInstance) {
      return;
    }

    try {
      // 模拟WebSocket连接
      // 实际项目中应该连接到真实的WebSocket服务器
      const wsUrl = import.meta.env.VITE_WS_URL || 'ws://localhost:3000/ws';
      wsInstance = new WebSocket(wsUrl);

      wsInstance.onopen = () => {
        wsConnected.value = true;
        console.log('WebSocket connected');
      };

      wsInstance.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data);
          handleWebSocketMessage(data);
        } catch (error) {
          console.error('Failed to parse WebSocket message:', error);
        }
      };

      wsInstance.onerror = (error) => {
        console.error('WebSocket error:', error);
        wsConnected.value = false;
      };

      wsInstance.onclose = () => {
        wsConnected.value = false;
        wsInstance = null;
        console.log('WebSocket disconnected');

        // 5秒后重连
        setTimeout(() => {
          connectWebSocket();
        }, 5000);
      };
    } catch (error) {
      console.error('Failed to connect WebSocket:', error);
      // 如果WebSocket失败，使用轮询
      startPolling();
    }
  }

  /**
   * 断开WebSocket
   */
  function disconnectWebSocket() {
    if (wsInstance) {
      wsInstance.close();
      wsInstance = null;
      wsConnected.value = false;
    }
  }

  /**
   * 处理WebSocket消息
   */
  function handleWebSocketMessage(data: any) {
    if (data.type === 'notification') {
      const notification: Notification = data.payload;

      // 添加到列表
      notifications.value.unshift(notification);
      total.value++;
      unreadCount.value++;

      // 显示Toast
      addToast({
        type: 'info',
        title: notification.title,
        content: notification.content,
        link: getNotificationLink(notification)
      });

      // 播放音效
      if (settings.value?.sound) {
        playNotificationSound();
      }

      // 显示浏览器通知
      if (settings.value?.browserNotification) {
        showBrowserNotification(notification);
      }
    } else if (data.type === 'unread_count') {
      unreadCount.value = data.payload.count;
    }
  }

  /**
   * 开始轮询
   */
  function startPolling(interval = 30000) {
    stopPolling();
    pollTimer = window.setInterval(() => {
      fetchUnreadCount();
    }, interval);
  }

  /**
   * 停止轮询
   */
  function stopPolling() {
    if (pollTimer) {
      clearInterval(pollTimer);
      pollTimer = null;
    }
  }

  /**
   * 获取通知链接
   */
  function getNotificationLink(notification: Notification): string {
    if (!notification.relatedType || !notification.relatedId) {
      return '/notifications';
    }

    switch (notification.relatedType) {
      case 'task':
        return `/tasks/${notification.relatedId}`;
      case 'document':
        return `/documents/${notification.relatedId}`;
      case 'change':
        return `/changes/${notification.relatedId}`;
      case 'test':
        return `/tests/${notification.relatedId}`;
      default:
        return '/notifications';
    }
  }

  /**
   * 播放通知音效
   */
  function playNotificationSound() {
    try {
      const audio = new Audio('/sounds/notification.mp3');
      audio.volume = 0.5;
      audio.play().catch(err => {
        console.warn('Failed to play notification sound:', err);
      });
    } catch (error) {
      console.error('Failed to play notification sound:', error);
    }
  }

  /**
   * 显示浏览器通知
   */
  function showBrowserNotification(notification: Notification) {
    if (!('Notification' in window)) {
      return;
    }

    if (Notification.permission === 'granted') {
      new Notification(notification.title, {
        body: notification.content,
        icon: '/logo.png',
        tag: notification.id,
        requireInteraction: notification.actionRequired
      });
    } else if (Notification.permission !== 'denied') {
      Notification.requestPermission().then(permission => {
        if (permission === 'granted') {
          new Notification(notification.title, {
            body: notification.content,
            icon: '/logo.png',
            tag: notification.id,
            requireInteraction: notification.actionRequired
          });
        }
      });
    }
  }

  /**
   * 重置状态
   */
  function reset() {
    notifications.value = [];
    total.value = 0;
    loading.value = false;
    unreadCount.value = 0;
    stats.value = null;
    settings.value = null;
    queryParams.value = {
      page: 1,
      pageSize: 20
    };
    toastQueue.value = [];
    disconnectWebSocket();
    stopPolling();
  }

  return {
    // State
    notifications,
    total,
    loading,
    unreadCount,
    stats,
    settings,
    queryParams,
    toastQueue,
    wsConnected,

    // Getters
    unreadNotifications,
    actionRequiredNotifications,
    mentionedNotifications,

    // Actions
    fetchNotifications,
    fetchNotificationDetail,
    markAsRead,
    markAllAsRead,
    deleteNotification,
    batchDelete,
    fetchUnreadCount,
    fetchStats,
    fetchSettings,
    updateSettings,
    addToast,
    removeToast,
    connectWebSocket,
    disconnectWebSocket,
    startPolling,
    stopPolling,
    reset
  };
});
