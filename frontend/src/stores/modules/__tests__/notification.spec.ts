
import { setActivePinia, createPinia } from 'pinia';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { useNotificationStore } from '../notification';
import * as notificationApi from '@/api/modules/notification';
import { createWebSocketClient } from '@/utils/websocket';

// Mock the API module
vi.mock('@/api/modules/notification', () => ({
  notificationApi: {
    getNotifications: vi.fn(),
    getUnreadCount: vi.fn(),
    markAsRead: vi.fn(),
    markAllAsRead: vi.fn(),
    deleteNotification: vi.fn(),
  },
}));

// Mock the WebSocket client
const mockWsClient = {
  on: vi.fn(),
  connect: vi.fn(),
  disconnect: vi.fn(),
  isConnected: vi.fn(() => true),
};
vi.mock('@/utils/websocket', () => ({
  createWebSocketClient: vi.fn(() => mockWsClient),
  WebSocketMessageType: {
    NOTIFICATION: 'notification',
  },
}));

// Mock browser APIs
const NotificationMock = vi.fn(() => ({
    onclick: vi.fn(),
    close: vi.fn(),
}));
const AudioMock = vi.fn(() => ({
    play: vi.fn(() => Promise.resolve()),
}));
vi.stubGlobal('Notification', NotificationMock);
vi.stubGlobal('Audio', AudioMock);

// Mock Ant Design message
vi.mock('ant-design-vue', () => ({
    message: {
        info: vi.fn(),
        warning: vi.fn(),
    }
}));

describe('Notification Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
    localStorage.clear();
    // Reset static properties on mocks
    vi.spyOn(window, 'Notification', 'get').mockReturnValue(Object.assign(NotificationMock, { permission: 'granted' }));
  });

  it('initializes with default values', () => {
    const store = useNotificationStore();
    expect(store.notifications).toEqual([]);
    expect(store.unreadCount).toBe(0);
    expect(store.isConnected).toBe(false);
    expect(store.audioEnabled).toBe(true); // Default
    expect(store.desktopNotificationEnabled).toBe(true); // Default based on granted permission
  });

  describe('WebSocket actions', () => {
    it('connects to WebSocket', () => {
      const store = useNotificationStore();
      store.connectWebSocket('fake-token');
      expect(createWebSocketClient).toHaveBeenCalled();
      expect(mockWsClient.connect).toHaveBeenCalled();
      expect(store.isConnected).toBe(true);
    });

    it('disconnects from WebSocket', () => {
      const store = useNotificationStore();
      store.connectWebSocket('fake-token'); // First connect
      store.disconnectWebSocket();
      expect(mockWsClient.disconnect).toHaveBeenCalled();
      expect(store.isConnected).toBe(false);
      expect(store.wsClient).toBeNull();
    });
  });

  describe('Notification handling', () => {
    it('adds a new notification and updates state', () => {
      const store = useNotificationStore();
      store.desktopNotificationEnabled = true; // Ensure desktop notifications are on
      store.audioEnabled = true; // Ensure audio is on

      const newNotification = { id: 1, title: 'New', isRead: false };
      store.addNotification(newNotification as any);

      expect(store.notifications).toEqual([newNotification]);
      expect(store.unreadCount).toBe(1);
      expect(NotificationMock).toHaveBeenCalledWith('New', expect.any(Object));
      expect(AudioMock).toHaveBeenCalled();
      expect(vi.mocked(AudioMock).mock.instances[0].play).toHaveBeenCalled();
    });
  });

  describe('API actions', () => {
    const mockNotifs = { data: { list: [{ id: 1, title: 'Test' }] } };
    const mockCount = { data: { total: 5 } };

    it('fetches notifications', async () => {
      const store = useNotificationStore();
      vi.mocked(notificationApi.getNotifications).mockResolvedValue(mockNotifs as any);
      await store.fetchNotifications();
      expect(notificationApi.getNotifications).toHaveBeenCalled();
      expect(store.notifications).toEqual(mockNotifs.data.list);
    });

    it('fetches unread count', async () => {
      const store = useNotificationStore();
      vi.mocked(notificationApi.getUnreadCount).mockResolvedValue(mockCount as any);
      await store.fetchUnreadCount();
      expect(notificationApi.getUnreadCount).toHaveBeenCalled();
      expect(store.unreadCount).toBe(5);
    });

    it('marks a notification as read', async () => {
      const store = useNotificationStore();
      store.notifications = [{ id: 1, title: 'Unread', isRead: false }] as any;
      store.unreadCount = 1;
      vi.mocked(notificationApi.markAsRead).mockResolvedValue(undefined);

      await store.markAsRead(1);

      expect(notificationApi.markAsRead).toHaveBeenCalledWith(1);
      expect(store.notifications[0].isRead).toBe(true);
      expect(store.unreadCount).toBe(0);
    });

    it('marks all notifications as read', async () => {
        const store = useNotificationStore();
        store.notifications = [{ id: 1, isRead: false }, { id: 2, isRead: false }] as any;
        store.unreadCount = 2;
        vi.mocked(notificationApi.markAllAsRead).mockResolvedValue(undefined);

        await store.markAllAsRead();

        expect(notificationApi.markAllAsRead).toHaveBeenCalled();
        expect(store.notifications.every(n => n.isRead)).toBe(true);
        expect(store.unreadCount).toBe(0);
    });

    it('deletes a notification', async () => {
        const store = useNotificationStore();
        store.notifications = [{ id: 1, isRead: false }, { id: 2, isRead: true }] as any;
        store.unreadCount = 1;
        vi.mocked(notificationApi.deleteNotification).mockResolvedValue(undefined);

        await store.deleteNotification(1);

        expect(notificationApi.deleteNotification).toHaveBeenCalledWith(1);
        expect(store.notifications.length).toBe(1);
        expect(store.notifications[0].id).toBe(2);
        expect(store.unreadCount).toBe(0);
    });
  });

  describe('User preferences', () => {
    it('toggles audio and saves to localStorage', () => {
        const store = useNotificationStore();
        const initialValue = store.audioEnabled;
        store.toggleAudio();
        expect(store.audioEnabled).toBe(!initialValue);
        expect(localStorage.setItem).toHaveBeenCalledWith('notification-audio-enabled', String(!initialValue));
    });

    it('requests permission when enabling desktop notifications', async () => {
        vi.spyOn(window, 'Notification', 'get').mockReturnValue(Object.assign(NotificationMock, { permission: 'default' }));
        const store = useNotificationStore();
        store.desktopNotificationEnabled = false;

        vi.mocked(Notification).requestPermission = vi.fn().mockResolvedValue('granted');

        await store.toggleDesktopNotification();

        expect(Notification.requestPermission).toHaveBeenCalled();
        expect(store.desktopNotificationEnabled).toBe(true);
    });
  });
});
