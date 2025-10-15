import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useNotificationStore } from './notification'
import { notificationApi } from '@/api/modules/notification'
import { createWebSocketClient, WebSocketMessageType } from '@/utils/websocket'
import { message } from 'ant-design-vue'

// Mock notification API
vi.mock('@/api/modules/notification', () => ({
  notificationApi: {
    getNotifications: vi.fn(),
    getUnreadCount: vi.fn(),
    markAsRead: vi.fn(),
    markAllAsRead: vi.fn(),
    deleteNotification: vi.fn()
  }
}))

// Mock WebSocket utilities
vi.mock('@/utils/websocket', () => ({
  WebSocketMessageType: {
    NOTIFICATION: 'notification',
    TASK_UPDATE: 'task_update',
    DOCUMENT_UPDATE: 'document_update',
    CHANGE_UPDATE: 'change_update',
    COMMENT: 'comment',
    MENTION: 'mention'
  },
  createWebSocketClient: vi.fn(() => ({
    isConnected: vi.fn(() => false),
    connect: vi.fn(),
    disconnect: vi.fn(),
    on: vi.fn()
  }))
}))

// Mock Ant Design Vue
vi.mock('ant-design-vue', () => ({
  message: {
    info: vi.fn(),
    warning: vi.fn()
  }
}))

// Mock localStorage
const localStorageMock = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn()
}
Object.defineProperty(window, 'localStorage', {
  value: localStorageMock
})

// Mock Notification API
const mockNotification = {
  requestPermission: vi.fn(),
  permission: 'default'
}
Object.defineProperty(window, 'Notification', {
  value: mockNotification,
  writable: true
})

// Mock Audio
const mockAudio = {
  play: vi.fn().mockResolvedValue(undefined),
  volume: 1
}
global.Audio = vi.fn(() => mockAudio) as any

describe('NotificationStore', () => {
  let notificationStore: any
  let mockWsClient: any

  beforeEach(() => {
    // Create a fresh pinia instance
    setActivePinia(createPinia())
    notificationStore = useNotificationStore()
    
    // Mock WebSocket client
    mockWsClient = {
      isConnected: vi.fn(() => false),
      connect: vi.fn(),
      disconnect: vi.fn(),
      on: vi.fn()
    }
    vi.mocked(createWebSocketClient).mockReturnValue(mockWsClient)
    
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.resetAllMocks()
  })

  describe('Initial State', () => {
    it('has correct initial state', () => {
      expect(notificationStore.wsClient).toBeNull()
      expect(notificationStore.notifications).toEqual([])
      expect(notificationStore.unreadCount).toBe(0)
      expect(notificationStore.isConnected).toBe(false)
      expect(notificationStore.audioEnabled).toBe(true)
      expect(notificationStore.desktopNotificationEnabled).toBe(false)
    })

    it('initializes settings from localStorage', () => {
      localStorageMock.getItem.mockImplementation((key: string) => {
        if (key === 'notification-audio-enabled') return 'false'
        return null
      })
      
      // Create new store instance to test initialization
      setActivePinia(createPinia())
      const newStore = useNotificationStore()
      
      expect(newStore.audioEnabled).toBe(false)
    })

    it('checks desktop notification permission on init', () => {
      mockNotification.permission = 'granted'
      
      // Create new store instance to test initialization
      setActivePinia(createPinia())
      const newStore = useNotificationStore()
      
      expect(newStore.desktopNotificationEnabled).toBe(true)
    })
  })

  describe('Getters', () => {
    it('computes hasUnread correctly', () => {
      expect(notificationStore.hasUnread).toBe(false)
      
      notificationStore.unreadCount = 1
      expect(notificationStore.hasUnread).toBe(true)
      
      notificationStore.unreadCount = 0
      expect(notificationStore.hasUnread).toBe(false)
    })
  })

  describe('WebSocket Connection', () => {
    it('connects to WebSocket with token', () => {
      const token = 'test-token'
      notificationStore.connectWebSocket(token)
      
      expect(createWebSocketClient).toHaveBeenCalledWith({
        url: 'ws://localhost:8080/ws/notifications',
        token,
        reconnectInterval: 5000,
        maxReconnectAttempts: 10,
        heartbeatInterval: 30000
      })
      
      expect(mockWsClient.on).toHaveBeenCalledWith(WebSocketMessageType.NOTIFICATION, expect.any(Function))
      expect(mockWsClient.on).toHaveBeenCalledWith(WebSocketMessageType.TASK_UPDATE, expect.any(Function))
      expect(mockWsClient.on).toHaveBeenCalledWith(WebSocketMessageType.DOCUMENT_UPDATE, expect.any(Function))
      expect(mockWsClient.on).toHaveBeenCalledWith(WebSocketMessageType.CHANGE_UPDATE, expect.any(Function))
      expect(mockWsClient.on).toHaveBeenCalledWith(WebSocketMessageType.COMMENT, expect.any(Function))
      expect(mockWsClient.on).toHaveBeenCalledWith(WebSocketMessageType.MENTION, expect.any(Function))
      
      expect(mockWsClient.connect).toHaveBeenCalled()
      expect(notificationStore.isConnected).toBe(true)
    })

    it('does not connect if already connected', () => {
      mockWsClient.isConnected.mockReturnValue(true)
      
      const token = 'test-token'
      notificationStore.connectWebSocket(token)
      
      expect(createWebSocketClient).not.toHaveBeenCalled()
    })

    it('disconnects WebSocket', () => {
      notificationStore.connectWebSocket('test-token')
      notificationStore.disconnectWebSocket()
      
      expect(mockWsClient.disconnect).toHaveBeenCalled()
      expect(notificationStore.wsClient).toBeNull()
      expect(notificationStore.isConnected).toBe(false)
    })
  })

  describe('Message Handlers', () => {
    beforeEach(() => {
      notificationStore.connectWebSocket('test-token')
    })

    it('handles notification message', () => {
      const mockNotification = {
        id: 1,
        title: 'Test Notification',
        content: 'Test content',
        type: 'TASK',
        priority: 'HIGH',
        isRead: false,
        createdAt: '2023-10-10T10:00:00Z'
      }
      
      const messageHandler = mockWsClient.on.mock.calls.find(
        call => call[0] === WebSocketMessageType.NOTIFICATION
      )[1]
      
      messageHandler({ data: mockNotification })
      
      expect(notificationStore.notifications).toHaveLength(1)
      expect(notificationStore.notifications[0]).toEqual(mockNotification)
      expect(notificationStore.unreadCount).toBe(1)
    })

    it('handles task update message', () => {
      const consoleSpy = vi.spyOn(console, 'log').mockImplementation(() => {})
      
      const messageHandler = mockWsClient.on.mock.calls.find(
        call => call[0] === WebSocketMessageType.TASK_UPDATE
      )[1]
      
      messageHandler({ data: { id: 1, title: 'Task Update' } })
      
      expect(consoleSpy).toHaveBeenCalledWith('任务更新:', { id: 1, title: 'Task Update' })
      
      consoleSpy.mockRestore()
    })

    it('handles document update message', () => {
      const consoleSpy = vi.spyOn(console, 'log').mockImplementation(() => {})
      
      const messageHandler = mockWsClient.on.mock.calls.find(
        call => call[0] === WebSocketMessageType.DOCUMENT_UPDATE
      )[1]
      
      messageHandler({ data: { id: 1, title: 'Document Update' } })
      
      expect(consoleSpy).toHaveBeenCalledWith('文档更新:', { id: 1, title: 'Document Update' })
      
      consoleSpy.mockRestore()
    })

    it('handles change update message', () => {
      const consoleSpy = vi.spyOn(console, 'log').mockImplementation(() => {})
      
      const messageHandler = mockWsClient.on.mock.calls.find(
        call => call[0] === WebSocketMessageType.CHANGE_UPDATE
      )[1]
      
      messageHandler({ data: { id: 1, title: 'Change Update' } })
      
      expect(consoleSpy).toHaveBeenCalledWith('变更更新:', { id: 1, title: 'Change Update' })
      
      consoleSpy.mockRestore()
    })

    it('handles comment message', () => {
      const mockNotification = {
        id: 2,
        title: 'New Comment',
        content: 'Comment content',
        type: 'COMMENT',
        priority: 'MEDIUM',
        isRead: false,
        createdAt: '2023-10-10T10:00:00Z'
      }
      
      const messageHandler = mockWsClient.on.mock.calls.find(
        call => call[0] === WebSocketMessageType.COMMENT
      )[1]
      
      messageHandler({ data: mockNotification })
      
      expect(notificationStore.notifications).toHaveLength(1)
      expect(notificationStore.notifications[0]).toEqual(mockNotification)
      expect(notificationStore.unreadCount).toBe(1)
    })

    it('handles mention message', () => {
      const mockNotification = {
        id: 3,
        title: 'You were mentioned',
        content: 'Mention content',
        type: 'MENTION',
        priority: 'HIGH',
        isRead: false,
        createdAt: '2023-10-10T10:00:00Z'
      }
      
      const messageHandler = mockWsClient.on.mock.calls.find(
        call => call[0] === WebSocketMessageType.MENTION
      )[1]
      
      messageHandler({ data: mockNotification })
      
      expect(notificationStore.notifications).toHaveLength(1)
      expect(notificationStore.notifications[0]).toEqual(mockNotification)
      expect(notificationStore.unreadCount).toBe(1)
    })
  })

  describe('Notification Management', () => {
    it('adds notification correctly', () => {
      const mockNotification = {
        id: 1,
        title: 'Test Notification',
        content: 'Test content',
        type: 'TASK',
        priority: 'HIGH',
        isRead: false,
        createdAt: '2023-10-10T10:00:00Z'
      }
      
      notificationStore.addNotification(mockNotification)
      
      expect(notificationStore.notifications).toHaveLength(1)
      expect(notificationStore.notifications[0]).toEqual(mockNotification)
      expect(notificationStore.unreadCount).toBe(1)
    })

    it('does not increment unread count for read notification', () => {
      const mockNotification = {
        id: 1,
        title: 'Test Notification',
        content: 'Test content',
        type: 'TASK',
        priority: 'HIGH',
        isRead: true,
        createdAt: '2023-10-10T10:00:00Z'
      }
      
      notificationStore.addNotification(mockNotification)
      
      expect(notificationStore.notifications).toHaveLength(1)
      expect(notificationStore.notifications[0]).toEqual(mockNotification)
      expect(notificationStore.unreadCount).toBe(0)
    })

    it('shows message info when adding notification', () => {
      const mockNotification = {
        id: 1,
        title: 'Test Notification',
        content: 'Test content',
        type: 'TASK',
        priority: 'HIGH',
        isRead: false,
        createdAt: '2023-10-10T10:00:00Z'
      }
      
      notificationStore.addNotification(mockNotification)
      
      expect(message.info).toHaveBeenCalledWith({
        content: 'Test Notification',
        duration: 3
      })
    })

    it('fetches notifications successfully', async () => {
      const mockNotifications = [
        { id: 1, title: 'Notification 1' },
        { id: 2, title: 'Notification 2' }
      ]
      
      vi.mocked(notificationApi.getNotifications).mockResolvedValue({
        data: { list: mockNotifications }
      })
      
      await notificationStore.fetchNotifications()
      
      expect(notificationApi.getNotifications).toHaveBeenCalledWith({
        page: 1,
        pageSize: 20
      })
      expect(notificationStore.notifications).toEqual(mockNotifications)
    })

    it('handles fetch notifications error', async () => {
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      vi.mocked(notificationApi.getNotifications).mockRejectedValue(new Error('Failed to fetch'))
      
      await notificationStore.fetchNotifications()
      
      expect(consoleSpy).toHaveBeenCalledWith('获取通知列表失败:', expect.any(Error))
      
      consoleSpy.mockRestore()
    })

    it('fetches unread count successfully', async () => {
      vi.mocked(notificationApi.getUnreadCount).mockResolvedValue({
        data: { total: 5 }
      })
      
      await notificationStore.fetchUnreadCount()
      
      expect(notificationApi.getUnreadCount).toHaveBeenCalled()
      expect(notificationStore.unreadCount).toBe(5)
    })

    it('handles fetch unread count error', async () => {
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      vi.mocked(notificationApi.getUnreadCount).mockRejectedValue(new Error('Failed to fetch'))
      
      await notificationStore.fetchUnreadCount()
      
      expect(consoleSpy).toHaveBeenCalledWith('获取未读数量失败:', expect.any(Error))
      
      consoleSpy.mockRestore()
    })

    it('marks notification as read successfully', async () => {
      const mockNotification = {
        id: 1,
        title: 'Test Notification',
        isRead: false
      }
      
      notificationStore.notifications = [mockNotification]
      notificationStore.unreadCount = 1
      
      vi.mocked(notificationApi.markAsRead).mockResolvedValue(undefined)
      
      await notificationStore.markAsRead(1)
      
      expect(notificationApi.markAsRead).toHaveBeenCalledWith(1)
      expect(mockNotification.isRead).toBe(true)
      expect(notificationStore.unreadCount).toBe(0)
    })

    it('marks all notifications as read successfully', async () => {
      const mockNotifications = [
        { id: 1, title: 'Notification 1', isRead: false },
        { id: 2, title: 'Notification 2', isRead: false }
      ]
      
      notificationStore.notifications = mockNotifications
      notificationStore.unreadCount = 2
      
      vi.mocked(notificationApi.markAllAsRead).mockResolvedValue(undefined)
      
      await notificationStore.markAllAsRead()
      
      expect(notificationApi.markAllAsRead).toHaveBeenCalled()
      expect(mockNotifications[0].isRead).toBe(true)
      expect(mockNotifications[1].isRead).toBe(true)
      expect(notificationStore.unreadCount).toBe(0)
    })

    it('deletes notification successfully', async () => {
      const mockNotification = {
        id: 1,
        title: 'Notification 1',
        isRead: false
      }
      
      notificationStore.notifications = [mockNotification]
      notificationStore.unreadCount = 1
      
      vi.mocked(notificationApi.deleteNotification).mockResolvedValue(undefined)
      
      await notificationStore.deleteNotification(1)
      
      expect(notificationApi.deleteNotification).toHaveBeenCalledWith(1)
      expect(notificationStore.notifications).toHaveLength(0)
      expect(notificationStore.unreadCount).toBe(0)
    })

    it('deletes read notification successfully', async () => {
      const mockNotification = {
        id: 1,
        title: 'Notification 1',
        isRead: true
      }
      
      notificationStore.notifications = [mockNotification]
      notificationStore.unreadCount = 0
      
      vi.mocked(notificationApi.deleteNotification).mockResolvedValue(undefined)
      
      await notificationStore.deleteNotification(1)
      
      expect(notificationApi.deleteNotification).toHaveBeenCalledWith(1)
      expect(notificationStore.notifications).toHaveLength(0)
      expect(notificationStore.unreadCount).toBe(0)
    })

    it('handles mark as read error', async () => {
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      vi.mocked(notificationApi.markAsRead).mockRejectedValue(new Error('Failed to mark'))
      
      await notificationStore.markAsRead(1)
      
      expect(consoleSpy).toHaveBeenCalledWith('标记已读失败:', expect.any(Error))
      
      consoleSpy.mockRestore()
    })
  })

  describe('Settings', () => {
    it('toggles audio setting', () => {
      const initialState = notificationStore.audioEnabled
      
      notificationStore.toggleAudio()
      
      expect(notificationStore.audioEnabled).toBe(!initialState)
      expect(localStorage.setItem).toHaveBeenCalledWith(
        'notification-audio-enabled',
        String(!initialState)
      )
    })

    it('toggles desktop notification when enabling', async () => {
      mockNotification.requestPermission.mockResolvedValue('granted')
      
      notificationStore.desktopNotificationEnabled = false
      
      await notificationStore.toggleDesktopNotification()
      
      expect(mockNotification.requestPermission).toHaveBeenCalled()
      expect(notificationStore.desktopNotificationEnabled).toBe(true)
    })

    it('shows warning when desktop notification permission denied', async () => {
      mockNotification.requestPermission.mockResolvedValue('denied')
      
      notificationStore.desktopNotificationEnabled = false
      
      await notificationStore.toggleDesktopNotification()
      
      expect(mockNotification.requestPermission).toHaveBeenCalled()
      expect(notificationStore.desktopNotificationEnabled).toBe(false)
      expect(message.warning).toHaveBeenCalledWith('桌面通知权限被拒绝')
    })

    it('toggles desktop notification when disabling', async () => {
      notificationStore.desktopNotificationEnabled = true
      
      await notificationStore.toggleDesktopNotification()
      
      expect(notificationStore.desktopNotificationEnabled).toBe(false)
    })

    it('requests notification permission', async () => {
      mockNotification.requestPermission.mockResolvedValue('granted')
      
      const result = await notificationStore.requestNotificationPermission()
      
      expect(mockNotification.requestPermission).toHaveBeenCalled()
      expect(result).toBe(true)
      expect(notificationStore.desktopNotificationEnabled).toBe(true)
    })

    it('handles notification not supported', async () => {
      Object.defineProperty(window, 'Notification', {
        value: undefined,
        writable: true
      })
      
      const result = await notificationStore.requestNotificationPermission()
      
      expect(result).toBe(false)
    })
  })

  describe('Desktop Notifications', () => {
    beforeEach(() => {
      mockNotification.permission = 'granted'
      notificationStore.desktopNotificationEnabled = true
    })

    it('shows desktop notification when enabled', () => {
      const mockNotificationObj = {
        close: vi.fn()
      }
      
      const OriginalNotification = global.Notification
      global.Notification = vi.fn(() => mockNotificationObj) as any
      
      const mockNotification = {
        id: 1,
        title: 'Test Notification',
        content: 'Test content',
        priority: 'HIGH'
      }
      
      notificationStore.showDesktopNotification(mockNotification)
      
      expect(global.Notification).toHaveBeenCalledWith('Test Notification', {
        body: 'Test content',
        icon: '/logo.svg',
        tag: 'notification-1',
        requireInteraction: true
      })
      
      // Restore original Notification
      global.Notification = OriginalNotification
    })

    it('does not show desktop notification when disabled', () => {
      notificationStore.desktopNotificationEnabled = false
      
      const OriginalNotification = global.Notification
      global.Notification = vi.fn() as any
      
      const mockNotification = {
        id: 1,
        title: 'Test Notification',
        content: 'Test content'
      }
      
      notificationStore.showDesktopNotification(mockNotification)
      
      expect(global.Notification).not.toHaveBeenCalled()
      
      // Restore original Notification
      global.Notification = OriginalNotification
    })
  })

  describe('Audio Notifications', () => {
    it('plays notification sound when enabled', () => {
      notificationStore.audioEnabled = true
      
      notificationStore.playNotificationSound()
      
      expect(global.Audio).toHaveBeenCalledWith('/sounds/notification.mp3')
      expect(mockAudio.play).toHaveBeenCalled()
      expect(mockAudio.volume).toBe(0.5)
    })

    it('does not play sound when disabled', () => {
      notificationStore.audioEnabled = false
      
      notificationStore.playNotificationSound()
      
      expect(global.Audio).not.toHaveBeenCalled()
    })

    it('handles audio play error', () => {
      const consoleSpy = vi.spyOn(console, 'warn').mockImplementation(() => {})
      mockAudio.play.mockRejectedValue(new Error('Audio play failed'))
      
      notificationStore.audioEnabled = true
      notificationStore.playNotificationSound()
      
      expect(consoleSpy).toHaveBeenCalledWith('播放音效失败:', expect.any(Error))
      
      consoleSpy.mockRestore()
    })

    it('handles audio creation error', () => {
      const consoleSpy = vi.spyOn(console, 'warn').mockImplementation(() => {})
      global.Audio = vi.fn(() => {
        throw new Error('Audio creation failed')
      }) as any
      
      notificationStore.audioEnabled = true
      notificationStore.playNotificationSound()
      
      expect(consoleSpy).toHaveBeenCalledWith('播放音效失败:', expect.any(Error))
      
      consoleSpy.mockRestore()
    })
  })
})