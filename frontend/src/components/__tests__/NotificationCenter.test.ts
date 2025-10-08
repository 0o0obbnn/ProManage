import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import NotificationCenter from '../NotificationCenter.vue'
import { notificationApi } from '@/api/modules/notification'

// Mock dayjs
vi.mock('dayjs', () => {
  const dayjs = (date?: any) => ({
    fromNow: () => '1小时前',
    format: (fmt: string) => '2024-01-01 00:00:00'
  })
  dayjs.extend = vi.fn()
  dayjs.locale = vi.fn()
  return { default: dayjs }
})

// Mock dayjs plugins
vi.mock('dayjs/plugin/relativeTime', () => ({ default: {} }))
vi.mock('dayjs/locale/zh-cn', () => ({ default: {} }))

// Mock API
vi.mock('@/api/modules/notification', () => ({
  notificationApi: {
    getNotifications: vi.fn(() =>
      Promise.resolve({
        data: {
          list: [
            {
              id: 1,
              type: 'TASK',
              priority: 'NORMAL',
              title: 'Test Notification',
              content: 'Test content',
              isRead: false,
              createdAt: '2024-01-01T00:00:00Z'
            }
          ],
          total: 1,
          page: 1,
          pageSize: 10
        }
      })
    ),
    getUnreadCount: vi.fn(() =>
      Promise.resolve({
        data: {
          total: 1,
          byType: {}
        }
      })
    ),
    markAsRead: vi.fn(() => Promise.resolve()),
    markAllAsRead: vi.fn(() => Promise.resolve()),
    deleteNotification: vi.fn(() => Promise.resolve()),
    clearReadNotifications: vi.fn(() => Promise.resolve())
  }
}))

// Mock router
const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush
  })
}))

describe('NotificationCenter', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should render correctly', () => {
    const wrapper = mount(NotificationCenter, {
      global: {
        stubs: {
          'a-dropdown': true,
          'a-badge': true,
          'a-button': true,
          'a-space': true,
          'a-radio-group': true,
          'a-radio-button': true,
          'a-spin': true,
          'a-list': true,
          'a-list-item': true,
          'a-list-item-meta': true,
          'a-tag': true,
          'a-empty': true,
          'BellOutlined': true,
          'NotificationIcon': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
  })

  it('should fetch unread count on mount', async () => {
    mount(NotificationCenter, {
      global: {
        stubs: {
          'a-dropdown': true,
          'a-badge': true,
          'a-button': true,
          'a-space': true,
          'a-radio-group': true,
          'a-radio-button': true,
          'a-spin': true,
          'a-list': true,
          'a-list-item': true,
          'a-list-item-meta': true,
          'a-tag': true,
          'a-empty': true,
          'BellOutlined': true,
          'NotificationIcon': true
        }
      }
    })

    await new Promise(resolve => setTimeout(resolve, 100))

    expect(notificationApi.getUnreadCount).toHaveBeenCalled()
  })

  it('should display unread count badge', async () => {
    const wrapper = mount(NotificationCenter, {
      global: {
        stubs: {
          'a-dropdown': {
            template: '<div><slot /></div>'
          },
          'a-badge': {
            template: '<div><slot /></div>',
            props: ['count']
          },
          'a-button': true,
          'a-space': true,
          'a-radio-group': true,
          'a-radio-button': true,
          'a-spin': true,
          'a-list': true,
          'a-list-item': true,
          'a-list-item-meta': true,
          'a-tag': true,
          'a-empty': true,
          'BellOutlined': true,
          'NotificationIcon': true
        }
      }
    })

    await new Promise(resolve => setTimeout(resolve, 100))

    const vm = wrapper.vm as any
    expect(vm.unreadCount).toBeGreaterThanOrEqual(0)
  })

  it('should fetch notifications when dropdown opens', async () => {
    const wrapper = mount(NotificationCenter, {
      global: {
        stubs: {
          'a-dropdown': {
            template: '<div><slot /><slot name="overlay" /></div>',
            props: ['open']
          },
          'a-badge': true,
          'a-button': true,
          'a-space': true,
          'a-radio-group': true,
          'a-radio-button': true,
          'a-spin': true,
          'a-list': true,
          'a-list-item': true,
          'a-list-item-meta': true,
          'a-tag': true,
          'a-empty': true,
          'BellOutlined': true,
          'NotificationIcon': true
        }
      }
    })

    const vm = wrapper.vm as any
    vm.dropdownVisible = true
    await wrapper.vm.$nextTick()

    await new Promise(resolve => setTimeout(resolve, 100))

    expect(notificationApi.getNotifications).toHaveBeenCalled()
  })

  it('should mark notification as read when clicked', async () => {
    const wrapper = mount(NotificationCenter, {
      global: {
        stubs: {
          'a-dropdown': true,
          'a-badge': true,
          'a-button': true,
          'a-space': true,
          'a-radio-group': true,
          'a-radio-button': true,
          'a-spin': true,
          'a-list': true,
          'a-list-item': true,
          'a-list-item-meta': true,
          'a-tag': true,
          'a-empty': true,
          'BellOutlined': true,
          'NotificationIcon': true
        }
      }
    })

    const vm = wrapper.vm as any
    const notification = {
      id: 1,
      type: 'TASK',
      priority: 'NORMAL',
      title: 'Test',
      content: 'Test',
      isRead: false,
      createdAt: '2024-01-01'
    }

    await vm.handleNotificationClick(notification)

    expect(notificationApi.markAsRead).toHaveBeenCalledWith(1)
  })

  it('should mark all as read', async () => {
    const wrapper = mount(NotificationCenter, {
      global: {
        stubs: {
          'a-dropdown': true,
          'a-badge': true,
          'a-button': true,
          'a-space': true,
          'a-radio-group': true,
          'a-radio-button': true,
          'a-spin': true,
          'a-list': true,
          'a-list-item': true,
          'a-list-item-meta': true,
          'a-tag': true,
          'a-empty': true,
          'BellOutlined': true,
          'NotificationIcon': true
        }
      }
    })

    const vm = wrapper.vm as any
    await vm.handleMarkAllAsRead()

    expect(notificationApi.markAllAsRead).toHaveBeenCalled()
  })

  it('should delete notification', async () => {
    const wrapper = mount(NotificationCenter, {
      global: {
        stubs: {
          'a-dropdown': true,
          'a-badge': true,
          'a-button': true,
          'a-space': true,
          'a-radio-group': true,
          'a-radio-button': true,
          'a-spin': true,
          'a-list': true,
          'a-list-item': true,
          'a-list-item-meta': true,
          'a-tag': true,
          'a-empty': true,
          'BellOutlined': true,
          'NotificationIcon': true
        }
      }
    })

    const vm = wrapper.vm as any
    await vm.handleDelete(1)

    expect(notificationApi.deleteNotification).toHaveBeenCalledWith(1)
  })

  it('should filter notifications by read status', async () => {
    const wrapper = mount(NotificationCenter, {
      global: {
        stubs: {
          'a-dropdown': true,
          'a-badge': true,
          'a-button': true,
          'a-space': true,
          'a-radio-group': true,
          'a-radio-button': true,
          'a-spin': true,
          'a-list': true,
          'a-list-item': true,
          'a-list-item-meta': true,
          'a-tag': true,
          'a-empty': true,
          'BellOutlined': true,
          'NotificationIcon': true
        }
      }
    })

    const vm = wrapper.vm as any
    vm.notifications = [
      { id: 1, isRead: false, type: 'TASK', priority: 'NORMAL', title: 'Test 1', content: 'Test', createdAt: '2024-01-01' },
      { id: 2, isRead: true, type: 'TASK', priority: 'NORMAL', title: 'Test 2', content: 'Test', createdAt: '2024-01-01' }
    ]

    vm.filterType = 'unread'
    await wrapper.vm.$nextTick()

    expect(vm.filteredNotifications.length).toBe(1)
    expect(vm.filteredNotifications[0].id).toBe(1)
  })

  it('should navigate to related page when notification is clicked', async () => {
    const wrapper = mount(NotificationCenter, {
      global: {
        stubs: {
          'a-dropdown': true,
          'a-badge': true,
          'a-button': true,
          'a-space': true,
          'a-radio-group': true,
          'a-radio-button': true,
          'a-spin': true,
          'a-list': true,
          'a-list-item': true,
          'a-list-item-meta': true,
          'a-tag': true,
          'a-empty': true,
          'BellOutlined': true,
          'NotificationIcon': true
        }
      }
    })

    const vm = wrapper.vm as any
    const notification = {
      id: 1,
      type: 'TASK',
      priority: 'NORMAL',
      title: 'Test',
      content: 'Test',
      isRead: true,
      relatedId: 123,
      relatedType: 'TASK',
      createdAt: '2024-01-01'
    }

    await vm.handleNotificationClick(notification)

    expect(mockPush).toHaveBeenCalledWith('/tasks/123')
  })
})

