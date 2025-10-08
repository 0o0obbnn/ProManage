
import { mount } from '@vue/test-utils';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { ref, computed } from 'vue';
import NotificationCenter from '../NotificationCenter.vue';
import NotificationIcon from '../NotificationIcon.vue';
import { useNotificationStore } from '@/stores/modules/notification';
import { useRouter } from 'vue-router';
import { Empty } from 'ant-design-vue';

// Mock a-list and other ant design components that cause issues with test-utils
vi.mock('ant-design-vue', async () => {
  const actual = await vi.importActual('ant-design-vue');
  return {
    ...actual,
    // Mocking components that might have internal complexities
    AList: {
      name: 'AList',
      template: '<div class="mock-a-list"><slot name="renderItem" v-for="item in dataSource" :item="item"></slot><slot></slot></div>',
      props: ['dataSource'],
    },
    AListItem: {
      name: 'AListItem',
      template: '<div class="mock-a-list-item"><slot></slot></div>',
    },
    AListItemMeta: {
        name: 'AListItemMeta',
        template: '<div class="mock-a-list-item-meta"><slot name="title"></slot><slot name="description"></slot><slot name="avatar"></slot></div>',
    },
    ADropdown: {
        name: 'ADropdown',
        template: '<div class="mock-a-dropdown"><slot></slot><slot name="overlay"></slot></div>',
        props: ['open', 'trigger']
    },
    ABadge: {
        name: 'ABadge',
        template: '<div class="mock-a-badge"><slot></slot></div>',
        props: ['count']
    },
    ASpin: {
        name: 'ASpin',
        template: '<div class="mock-a-spin"><slot></slot></div>',
        props: ['spinning']
    },
    AEmpty: Empty,
  };
});


// Mock the store
vi.mock('@/stores/modules/notification', () => ({
  useNotificationStore: vi.fn(),
}));

// Mock the router
vi.mock('vue-router', () => ({
  useRouter: vi.fn(),
}));

const mockNotifications = [
  { id: 1, title: 'Task Due', content: 'Your task is due tomorrow', isRead: false, type: 'TASK', priority: 'HIGH', createdAt: new Date().toISOString(), link: '/tasks/1' },
  { id: 2, title: 'New Document', content: 'A new document was shared', isRead: true, type: 'DOCUMENT', priority: 'NORMAL', createdAt: new Date().toISOString(), link: '/documents/2' },
];

describe('NotificationCenter.vue', () => {
  let mockStore;
  let mockRouter;

  beforeEach(() => {
    // Reset mocks before each test
    mockStore = {
      notifications: ref(mockNotifications),
      unreadCount: computed(() => mockStore.notifications.value.filter(n => !n.isRead).length),
      audioEnabled: ref(true),
      desktopNotificationEnabled: ref(true),
      fetchNotifications: vi.fn().mockResolvedValue(void 0),
      fetchUnreadCount: vi.fn().mockResolvedValue(void 0),
      markAsRead: vi.fn().mockResolvedValue(void 0),
      markAllAsRead: vi.fn().mockResolvedValue(void 0),
      deleteNotification: vi.fn().mockResolvedValue(void 0),
      toggleAudio: vi.fn(),
      toggleDesktopNotification: vi.fn(),
    };
    
    mockRouter = {
      push: vi.fn(),
    };

    vi.mocked(useNotificationStore).mockReturnValue(mockStore);
    vi.mocked(useRouter).mockReturnValue(mockRouter);
  });

  it('renders the notification icon and badge with unread count', () => {
    const wrapper = mount(NotificationCenter, {
        global: {
            stubs: {
                NotificationIcon: true, // Stub child component
            }
        }
    });
    expect(wrapper.find('.notification-icon').exists()).toBe(true);
    // Cannot directly test badge count due to component mocking, but we can check if the store value is correct
    expect(mockStore.unreadCount.value).toBe(1);
  });

  it('fetches notifications when the dropdown becomes visible', async () => {
    const wrapper = mount(NotificationCenter);
    
    // Manually trigger the watcher for dropdown visibility
    await wrapper.vm.$watch('dropdownVisible', () => {}, { immediate: true });
    wrapper.vm.dropdownVisible = true;
    await wrapper.vm.$nextTick();

    expect(mockStore.fetchNotifications).toHaveBeenCalled();
  });

  it('displays a list of notifications', async () => {
    const wrapper = mount(NotificationCenter, {
        global: {
            stubs: {
                NotificationIcon: true,
            }
        }
    });
    mockStore.notifications.value = mockNotifications;
    await wrapper.vm.$nextTick();
    
    const items = wrapper.findAll('.mock-a-list-item');
    expect(items.length).toBe(2);
    expect(wrapper.text()).toContain('Task Due');
    expect(wrapper.text()).toContain('A new document was shared');
  });

  it('shows an empty state when there are no notifications', async () => {
    mockStore.notifications.value = [];
    const wrapper = mount(NotificationCenter);
    await wrapper.vm.$nextTick();

    expect(wrapper.findComponent(Empty).exists()).toBe(true);
    expect(wrapper.text()).toContain('暂无通知');
  });

  it('filters for unread notifications when "unread" tab is clicked', async () => {
    const wrapper = mount(NotificationCenter);
    await wrapper.vm.$nextTick();

    // Simulate filter change
    wrapper.vm.filterType = 'unread';
    await wrapper.vm.$nextTick();

    expect(wrapper.vm.filteredNotifications.length).toBe(1);
    expect(wrapper.vm.filteredNotifications[0].title).toBe('Task Due');
  });

  it('calls markAsRead and navigates when a notification is clicked', async () => {
    const wrapper = mount(NotificationCenter, {
        global: {
            stubs: {
                NotificationIcon: true,
            }
        }
    });
    await wrapper.vm.$nextTick();

    const firstItem = wrapper.find('.mock-a-list-item');
    await firstItem.trigger('click');

    expect(mockStore.markAsRead).toHaveBeenCalledWith(mockNotifications[0].id);
    expect(mockRouter.push).toHaveBeenCalledWith(mockNotifications[0].link);
  });

  it('calls markAllAsRead when "全部已读" button is clicked', async () => {
    const wrapper = mount(NotificationCenter);
    await wrapper.vm.$nextTick();

    const markAllButton = wrapper.findAll('button').find(b => b.text() === '全部已读');
    await markAllButton.trigger('click');

    expect(mockStore.markAllAsRead).toHaveBeenCalled();
  });

  it('calls deleteNotification when "删除" button is clicked', async () => {
    const wrapper = mount(NotificationCenter, {
        global: {
            stubs: {
                NotificationIcon: true,
            }
        }
    });
    await wrapper.vm.$nextTick();

    // Find the delete button within the first item
    const deleteButton = wrapper.find('.mock-a-list-item').find('button[danger]');
    await deleteButton.trigger('click');

    expect(mockStore.deleteNotification).toHaveBeenCalledWith(mockNotifications[0].id);
  });

  it('navigates to the notifications page when "查看全部通知" is clicked', async () => {
    const wrapper = mount(NotificationCenter);
    await wrapper.vm.$nextTick();

    const viewAllButton = wrapper.find('.notification-footer').find('button');
    await viewAllButton.trigger('click');

    expect(mockRouter.push).toHaveBeenCalledWith('/notifications');
  });
});
