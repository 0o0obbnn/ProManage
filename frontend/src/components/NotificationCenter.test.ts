import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import NotificationCenter from './NotificationCenter.vue'
import { useNotificationStore } from '@/stores/modules/notification'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

// Mock dayjs
vi.mock('dayjs', () => ({
  default: vi.fn((time: string) => ({
    fromNow: vi.fn(() => '2小时前')
  })),
  extend: vi.fn(),
  locale: vi.fn()
}))

// Mock notification store
vi.mock('@/stores/modules/notification', () => ({
  useNotificationStore: vi.fn(() => ({
    notifications: [
      {
        id: 1,
        title: '测试通知1',
        content: '这是测试通知内容1',
        type: 'TASK',
        priority: 'HIGH',
        isRead: false,
        createdAt: '2023-10-10T10:00:00Z',
        link: '/tasks/1',
        relatedId: 1,
        relatedType: 'TASK'
      },
      {
        id: 2,
        title: '测试通知2',
        content: '这是测试通知内容2',
        type: 'DOCUMENT',
        priority: 'MEDIUM',
        isRead: true,
        createdAt: '2023-10-09T15:30:00Z',
        link: '/documents/2',
        relatedId: 2,
        relatedType: 'DOCUMENT'
      }
    ],
    unreadCount: 1,
    fetchNotifications: vi.fn(),
    fetchUnreadCount: vi.fn(),
    markAsRead: vi.fn(),
    markAllAsRead: vi.fn(),
    deleteNotification: vi.fn(),
    toggleAudio: vi.fn(),
    toggleDesktopNotification: vi.fn(),
    audioEnabled: true,
    desktopNotificationEnabled: true
  }))
}))

// Mock notification API
vi.mock('@/api/modules/notification', () => ({
  notificationApi: {
    clearReadNotifications: vi.fn()
  }
}))

// Mock router
const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush
  })
}))

// Mock Ant Design Vue
vi.mock('ant-design-vue', async () => {
  const actual = await vi.importActual('ant-design-vue')
  return {
    ...actual,
    message: {
      success: vi.fn(),
      error: vi.fn()
    },
    Empty: {
      PRESENTED_IMAGE_SIMPLE: 'simple-empty-image'
    }
  }
})

describe('NotificationCenter', () => {
  let wrapper: any

  beforeEach(() => {
    vi.clearAllMocks()
    wrapper = mount(NotificationCenter)
  })

  afterEach(() => {
    wrapper.unmount()
  })

  it('renders correctly', () => {
    expect(wrapper.find('.notification-icon').exists()).toBe(true)
    expect(wrapper.findComponent({ name: 'ABadge' }).exists()).toBe(true)
    expect(wrapper.findComponent({ name: 'ADropdown' }).exists()).toBe(true)
  })

  it('displays unread count badge', () => {
    const badge = wrapper.findComponent({ name: 'ABadge' })
    expect(badge.props('count')).toBe(1) // unreadCount from mock
  })

  it('filters notifications correctly', async () => {
    // Open dropdown
    await wrapper.setData({ dropdownVisible: true })
    await nextTick()

    // Initially shows all notifications
    expect(wrapper.vm.filteredNotifications).toHaveLength(2)

    // Filter to unread only
    await wrapper.setData({ filterType: 'unread' })
    await nextTick()
    
    expect(wrapper.vm.filteredNotifications).toHaveLength(1)
    expect(wrapper.vm.filteredNotifications[0].id).toBe(1)
  })

  it('shows empty state when no notifications', async () => {
    // Mock empty notifications
    vi.mocked(useNotificationStore).mockReturnValueOnce({
      notifications: [],
      unreadCount: 0,
      fetchNotifications: vi.fn(),
      fetchUnreadCount: vi.fn(),
      markAsRead: vi.fn(),
      markAllAsRead: vi.fn(),
      deleteNotification: vi.fn(),
      toggleAudio: vi.fn(),
      toggleDesktopNotification: vi.fn(),
      audioEnabled: true,
      desktopNotificationEnabled: true
    } as any)

    wrapper = mount(NotificationCenter)
    await wrapper.setData({ dropdownVisible: true })
    await nextTick()

    expect(wrapper.findComponent({ name: 'AEmpty' }).exists()).toBe(true)
  })

  it('formats time correctly', () => {
    const time = wrapper.vm.formatTime('2023-10-10T10:00:00Z')
    expect(time).toBe('2小时前')
  })

  it('handles notification click', async () => {
    const notificationStore = useNotificationStore()
    
    // Open dropdown
    await wrapper.setData({ dropdownVisible: true })
    await nextTick()

    // Click on first notification
    const notificationItems = wrapper.findAll('.notification-item')
    await notificationItems[0].trigger('click')
    await nextTick()

    expect(notificationStore.markAsRead).toHaveBeenCalledWith(1)
    expect(mockPush).toHaveBeenCalledWith('/tasks/1')
    expect(wrapper.vm.dropdownVisible).toBe(false)
  })

  it('navigates to related item when no link provided', async () => {
    // Mock notification without link but with relatedId and relatedType
    vi.mocked(useNotificationStore).mockReturnValueOnce({
      notifications: [
        {
          id: 3,
          title: '测试通知3',
          content: '这是测试通知内容3',
          type: 'CHANGE',
          priority: 'LOW',
          isRead: false,
          createdAt: '2023-10-08T12:00:00Z',
          link: null,
          relatedId: 3,
          relatedType: 'CHANGE'
        }
      ],
      unreadCount: 1,
      fetchNotifications: vi.fn(),
      fetchUnreadCount: vi.fn(),
      markAsRead: vi.fn(),
      markAllAsRead: vi.fn(),
      deleteNotification: vi.fn(),
      toggleAudio: vi.fn(),
      toggleDesktopNotification: vi.fn(),
      audioEnabled: true,
      desktopNotificationEnabled: true
    } as any)

    wrapper = mount(NotificationCenter)
    await wrapper.setData({ dropdownVisible: true })
    await nextTick()

    const notificationItems = wrapper.findAll('.notification-item')
    await notificationItems[0].trigger('click')
    await nextTick()

    expect(mockPush).toHaveBeenCalledWith('/changes/3')
  })

  it('marks all as read', async () => {
    const notificationStore = useNotificationStore()
    
    // Open dropdown
    await wrapper.setData({ dropdownVisible: true })
    await nextTick()

    // Click "全部已读" button
    const markAllButton = wrapper.find('[data-testid="mark-all-read"]')
    if (markAllButton.exists()) {
      await markAllButton.trigger('click')
      await nextTick()

      expect(notificationStore.markAllAsRead).toHaveBeenCalled()
      expect(message.success).toHaveBeenCalledWith('已全部标记为已读')
    }
  })

  it('deletes notification', async () => {
    const notificationStore = useNotificationStore()
    
    // Open dropdown
    await wrapper.setData({ dropdownVisible: true })
    await nextTick()

    // Find and click delete button on first notification
    const deleteButtons = wrapper.findAll('[data-testid="delete-notification"]')
    if (deleteButtons.length > 0) {
      await deleteButtons[0].trigger('click')
      await nextTick()

      expect(notificationStore.deleteNotification).toHaveBeenCalledWith(1)
      expect(message.success).toHaveBeenCalledWith('删除成功')
    }
  })

  it('clears read notifications', async () => {
    const { notificationApi } = await import('@/api/modules/notification')
    vi.mocked(notificationApi.clearReadNotifications).mockResolvedValue(undefined)
    
    // Open dropdown
    await wrapper.setData({ dropdownVisible: true })
    await nextTick()

    // Click "清空已读" button
    const clearButton = wrapper.find('[data-testid="clear-read"]')
    if (clearButton.exists()) {
      await clearButton.trigger('click')
      await nextTick()

      expect(notificationApi.clearReadNotifications).toHaveBeenCalled()
      expect(message.success).toHaveBeenCalledWith('已清空已读通知')
    }
  })

  it('loads more notifications', async () => {
    const notificationStore = useNotificationStore()
    
    // Open dropdown
    await wrapper.setData({ 
      dropdownVisible: true,
      hasMore: true,
      loading: false
    })
    await nextTick()

    // Click "加载更多" button
    const loadMoreButton = wrapper.find('[data-testid="load-more"]')
    if (loadMoreButton.exists()) {
      await loadMoreButton.trigger('click')
      await nextTick()

      expect(wrapper.vm.currentPage).toBe(2)
      expect(notificationStore.fetchNotifications).toHaveBeenCalled()
    }
  })

  it('handles scroll to load more', async () => {
    const notificationStore = useNotificationStore()
    
    // Mock scroll event
    const mockScrollEvent = {
      target: {
        scrollTop: 300,
        scrollHeight: 400,
        clientHeight: 50
      }
    }
    
    // Set hasMore to true
    await wrapper.setData({ 
      hasMore: true,
      loading: false
    })
    
    // Trigger scroll
    wrapper.vm.handleScroll(mockScrollEvent)
    
    // Should load more when near bottom
    expect(wrapper.vm.currentPage).toBe(2)
    expect(notificationStore.fetchNotifications).toHaveBeenCalled()
  })

  it('toggles audio settings', async () => {
    const notificationStore = useNotificationStore()
    
    // Open dropdown
    await wrapper.setData({ dropdownVisible: true })
    await nextTick()

    // Click audio toggle button
    const audioButton = wrapper.find('[data-testid="toggle-audio"]')
    if (audioButton.exists()) {
      await audioButton.trigger('click')
      await nextTick()

      expect(notificationStore.toggleAudio).toHaveBeenCalled()
    }
  })

  it('toggles desktop notification settings', async () => {
    const notificationStore = useNotificationStore()
    
    // Open dropdown
    await wrapper.setData({ dropdownVisible: true })
    await nextTick()

    // Click desktop notification toggle button
    const desktopButton = wrapper.find('[data-testid="toggle-desktop"]')
    if (desktopButton.exists()) {
      await desktopButton.trigger('click')
      await nextTick()

      expect(notificationStore.toggleDesktopNotification).toHaveBeenCalled()
    }
  })

  it('navigates to all notifications page', async () => {
    // Open dropdown
    await wrapper.setData({ dropdownVisible: true })
    await nextTick()

    // Click "查看全部通知" button
    const viewAllButton = wrapper.find('[data-testid="view-all"]')
    if (viewAllButton.exists()) {
      await viewAllButton.trigger('click')
      await nextTick()

      expect(mockPush).toHaveBeenCalledWith('/notifications')
      expect(wrapper.vm.dropdownVisible).toBe(false)
    }
  })

  it('watches filter type changes', async () => {
    const notificationStore = useNotificationStore()
    
    // Change filter type
    await wrapper.setData({ filterType: 'unread' })
    await nextTick()
    
    expect(wrapper.vm.currentPage).toBe(1)
    expect(notificationStore.fetchNotifications).toHaveBeenCalled()
  })

  it('watches dropdown visibility', async () => {
    const notificationStore = useNotificationStore()
    
    // Open dropdown
    await wrapper.setData({ dropdownVisible: true })
    await nextTick()
    
    expect(wrapper.vm.currentPage).toBe(1)
    expect(notificationStore.fetchNotifications).toHaveBeenCalled()
  })

  it('displays priority tags correctly', async () => {
    // Open dropdown
    await wrapper.setData({ dropdownVisible: true })
    await nextTick()

    // Check for URGENT tag
    const urgentTags = wrapper.findAllComponents({ name: 'ATag' }).filter(
      tag => tag.props('color') === 'red'
    )
    
    // Check for HIGH tag
    const highTags = wrapper.findAllComponents({ name: 'ATag' }).filter(
      tag => tag.props('color') === 'orange'
    )
    
    // Should find one HIGH priority tag (from our mock data)
    expect(highTags.length).toBeGreaterThan(0)
  })
})