/**
 * 通知状态管理
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { message } from 'ant-design-vue'
import { WebSocketClient, WebSocketMessageType, createWebSocketClient } from '@/utils/websocket-client'
import { notificationApi } from '@/api/modules/notification'
import type { Notification } from '@/types/notification'

/**
 * 通知Store
 */
export const useNotificationStore = defineStore('notification', () => {
  /**
   * 状态
   */
  const wsClient = ref<WebSocketClient | null>(null)
  const notifications = ref<Notification[]>([])
  const unreadCount = ref(0)
  const isConnected = ref(false)
  const audioEnabled = ref(true)
  const desktopNotificationEnabled = ref(false)

  /**
   * 计算属性
   */
  const hasUnread = computed(() => unreadCount.value > 0)

  /**
   * 连接WebSocket
   */
  const connectWebSocket = (token: string) => {
    if (wsClient.value?.isConnected()) {
      console.log('WebSocket已连接')
      return
    }

    // 获取WebSocket URL
    const wsUrl = import.meta.env.VITE_WS_URL || 'ws://localhost:8080/ws'
    const fullUrl = `${wsUrl}/notifications`

    // 创建WebSocket客户端
    wsClient.value = createWebSocketClient({
      url: fullUrl,
      token,
      reconnectInterval: 5000,
      maxReconnectAttempts: 10,
      heartbeatInterval: 30000
    })

    // 注册消息处理器
    wsClient.value.on(WebSocketMessageType.NOTIFICATION, handleNotificationMessage)
    wsClient.value.on(WebSocketMessageType.TASK_UPDATE, handleTaskUpdateMessage)
    wsClient.value.on(WebSocketMessageType.DOCUMENT_UPDATE, handleDocumentUpdateMessage)
    wsClient.value.on(WebSocketMessageType.CHANGE_UPDATE, handleChangeUpdateMessage)
    wsClient.value.on(WebSocketMessageType.COMMENT, handleCommentMessage)
    wsClient.value.on(WebSocketMessageType.MENTION, handleMentionMessage)

    // 连接
    wsClient.value.connect()
    isConnected.value = true
  }

  /**
   * 断开WebSocket
   */
  const disconnectWebSocket = () => {
    if (wsClient.value) {
      wsClient.value.disconnect()
      wsClient.value = null
      isConnected.value = false
    }
  }

  /**
   * 处理通知消息
   */
  const handleNotificationMessage = (message: any) => {
    const notification: Notification = message.data
    addNotification(notification)
  }

  /**
   * 处理任务更新消息
   */
  const handleTaskUpdateMessage = (message: any) => {
    console.log('任务更新:', message.data)
    // 可以触发任务列表刷新等操作
  }

  /**
   * 处理文档更新消息
   */
  const handleDocumentUpdateMessage = (message: any) => {
    console.log('文档更新:', message.data)
    // 可以触发文档列表刷新等操作
  }

  /**
   * 处理变更更新消息
   */
  const handleChangeUpdateMessage = (message: any) => {
    console.log('变更更新:', message.data)
    // 可以触发变更列表刷新等操作
  }

  /**
   * 处理评论消息
   */
  const handleCommentMessage = (message: any) => {
    const notification: Notification = message.data
    addNotification(notification)
  }

  /**
   * 处理@提及消息
   */
  const handleMentionMessage = (message: any) => {
    const notification: Notification = message.data
    addNotification(notification)
  }

  /**
   * 添加通知
   */
  const addNotification = (notification: Notification) => {
    // 添加到列表开头
    notifications.value.unshift(notification)
    
    // 更新未读数
    if (!notification.isRead) {
      unreadCount.value++
    }

    // 显示桌面通知
    if (desktopNotificationEnabled.value) {
      showDesktopNotification(notification)
    }

    // 播放音效
    if (audioEnabled.value) {
      playNotificationSound()
    }

    // 显示消息提示
    message.info({
      content: notification.title,
      duration: 3
    })
  }

  /**
   * 显示桌面通知
   */
  const showDesktopNotification = (notification: Notification) => {
    if ('Notification' in window && Notification.permission === 'granted') {
      const n = new Notification(notification.title, {
        body: notification.content,
        icon: '/logo.svg',
        tag: `notification-${notification.id}`,
        requireInteraction: notification.priority === 'URGENT'
      })

      // 点击通知时聚焦窗口
      n.onclick = () => {
        window.focus()
        n.close()
      }
    }
  }

  /**
   * 播放通知音效
   */
  const playNotificationSound = () => {
    try {
      const audio = new Audio('/sounds/notification.mp3')
      audio.volume = 0.5
      audio.play().catch(error => {
        console.warn('播放音效失败:', error)
      })
    } catch (error) {
      console.warn('播放音效失败:', error)
    }
  }

  /**
   * 请求桌面通知权限
   */
  const requestNotificationPermission = async () => {
    if ('Notification' in window) {
      const permission = await Notification.requestPermission()
      desktopNotificationEnabled.value = permission === 'granted'
      return permission === 'granted'
    }
    return false
  }

  /**
   * 获取通知列表
   */
  const fetchNotifications = async () => {
    try {
      const res = await notificationApi.getNotifications({
        page: 1,
        pageSize: 20
      })
      notifications.value = res.data.list
    } catch (error) {
      console.error('获取通知列表失败:', error)
    }
  }

  /**
   * 获取未读数量
   */
  const fetchUnreadCount = async () => {
    try {
      const res = await notificationApi.getUnreadCount()
      unreadCount.value = res.data.total
    } catch (error) {
      console.error('获取未读数量失败:', error)
    }
  }

  /**
   * 标记为已读
   */
  const markAsRead = async (id: number) => {
    try {
      await notificationApi.markAsRead(id)
      const notification = notifications.value.find(n => n.id === id)
      if (notification && !notification.isRead) {
        notification.isRead = true
        unreadCount.value = Math.max(0, unreadCount.value - 1)
      }
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }

  /**
   * 全部已读
   */
  const markAllAsRead = async () => {
    try {
      await notificationApi.markAllAsRead()
      notifications.value.forEach(n => (n.isRead = true))
      unreadCount.value = 0
    } catch (error) {
      console.error('全部已读失败:', error)
    }
  }

  /**
   * 删除通知
   */
  const deleteNotification = async (id: number) => {
    try {
      await notificationApi.deleteNotification(id)
      const index = notifications.value.findIndex(n => n.id === id)
      if (index > -1) {
        const notification = notifications.value[index]
        if (!notification.isRead) {
          unreadCount.value = Math.max(0, unreadCount.value - 1)
        }
        notifications.value.splice(index, 1)
      }
    } catch (error) {
      console.error('删除通知失败:', error)
    }
  }

  /**
   * 切换音效
   */
  const toggleAudio = () => {
    audioEnabled.value = !audioEnabled.value
    localStorage.setItem('notification-audio-enabled', String(audioEnabled.value))
  }

  /**
   * 切换桌面通知
   */
  const toggleDesktopNotification = async () => {
    if (!desktopNotificationEnabled.value) {
      const granted = await requestNotificationPermission()
      if (!granted) {
        message.warning('桌面通知权限被拒绝')
      }
    } else {
      desktopNotificationEnabled.value = false
    }
  }

  /**
   * 初始化
   */
  const init = () => {
    // 从localStorage读取设置
    const audioSetting = localStorage.getItem('notification-audio-enabled')
    if (audioSetting !== null) {
      audioEnabled.value = audioSetting === 'true'
    }

    // 检查桌面通知权限
    if ('Notification' in window) {
      desktopNotificationEnabled.value = Notification.permission === 'granted'
    }
  }

  // 初始化
  init()

  return {
    // 状态
    wsClient,
    notifications,
    unreadCount,
    isConnected,
    audioEnabled,
    desktopNotificationEnabled,
    hasUnread,

    // 方法
    connectWebSocket,
    disconnectWebSocket,
    addNotification,
    fetchNotifications,
    fetchUnreadCount,
    markAsRead,
    markAllAsRead,
    deleteNotification,
    toggleAudio,
    toggleDesktopNotification,
    requestNotificationPermission
  }
})

