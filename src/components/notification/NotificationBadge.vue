<template>
  <a-dropdown
    v-model:open="dropdownVisible"
    :trigger="['click']"
    placement="bottomRight"
    overlayClassName="notification-badge-dropdown"
  >
    <a-badge :count="unreadCount" :overflow-count="99" :offset="[-5, 5]">
      <a-button type="text" class="notification-trigger" @click="handleOpen">
        <BellOutlined :style="{ fontSize: '18px' }" />
      </a-button>
    </a-badge>

    <template #overlay>
      <div class="notification-dropdown">
        <div class="notification-dropdown-header">
          <a-tabs v-model:activeKey="activeTab" size="small">
            <a-tab-pane key="all" tab="全部" />
            <a-tab-pane key="mentioned" tab="@我的">
              <template #tab>
                @我的
                <a-badge
                  v-if="mentionedCount > 0"
                  :count="mentionedCount"
                  :number-style="{ backgroundColor: '#722ed1', marginLeft: '4px' }"
                />
              </template>
            </a-tab-pane>
            <a-tab-pane key="actionRequired" tab="待处理">
              <template #tab>
                待处理
                <a-badge
                  v-if="actionRequiredCount > 0"
                  :count="actionRequiredCount"
                  :number-style="{ backgroundColor: '#fa8c16', marginLeft: '4px' }"
                />
              </template>
            </a-tab-pane>
          </a-tabs>
          <a-space>
            <a-button type="link" size="small" @click="handleMarkAllRead">
              全部已读
            </a-button>
          </a-space>
        </div>

        <a-divider style="margin: 0" />

        <a-spin :spinning="loading">
          <div class="notification-dropdown-list">
            <template v-if="displayedNotifications.length > 0">
              <div
                v-for="notification in displayedNotifications"
                :key="notification.id"
                :class="['notification-item', { unread: !notification.read }]"
                @click="handleNotificationClick(notification)"
              >
                <div class="notification-item-icon">
                  <component :is="getNotificationIcon(notification.type)" />
                </div>
                <div class="notification-item-content">
                  <div class="notification-item-title">{{ notification.title }}</div>
                  <div class="notification-item-desc">{{ notification.content }}</div>
                  <div class="notification-item-meta">
                    <span>{{ formatTime(notification.createdAt) }}</span>
                    <span v-if="notification.projectName" class="project-tag">
                      {{ notification.projectName }}
                    </span>
                  </div>
                </div>
                <div v-if="!notification.read" class="notification-item-badge"></div>
              </div>
            </template>
            <a-empty
              v-else
              :image="Empty.PRESENTED_IMAGE_SIMPLE"
              description="暂无通知"
              style="padding: 40px 0"
            />
          </div>
        </a-spin>

        <a-divider style="margin: 0" />

        <div class="notification-dropdown-footer">
          <a-button type="link" block @click="handleViewAll">
            查看全部通知
          </a-button>
        </div>
      </div>
    </template>
  </a-dropdown>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { Empty, message } from 'ant-design-vue';
import { useNotificationStore } from '@/stores/modules/notification';
import {
  BellOutlined,
  UserAddOutlined,
  MessageOutlined,
  FileTextOutlined,
  EyeOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  SwapOutlined,
  ArrowUpOutlined
} from '@ant-design/icons-vue';
import { formatNotificationTime } from '@/utils/notification';
import type { Notification, NotificationType } from '@/types/notification';

const router = useRouter();
const notificationStore = useNotificationStore();

const dropdownVisible = ref(false);
const activeTab = ref<'all' | 'mentioned' | 'actionRequired'>('all');
const loading = ref(false);

const unreadCount = computed(() => notificationStore.unreadCount);
const mentionedCount = computed(() => notificationStore.mentionedNotifications.length);
const actionRequiredCount = computed(() => notificationStore.actionRequiredNotifications.length);

const displayedNotifications = computed(() => {
  let notifications = notificationStore.notifications;

  if (activeTab.value === 'mentioned') {
    notifications = notificationStore.mentionedNotifications;
  } else if (activeTab.value === 'actionRequired') {
    notifications = notificationStore.actionRequiredNotifications;
  }

  return notifications.slice(0, 5);
});

const iconMap: Record<string, any> = {
  task_assigned: UserAddOutlined,
  task_mentioned: MessageOutlined,
  change_request: FileTextOutlined,
  review_request: EyeOutlined,
  approval_pending: CheckCircleOutlined,
  comment_reply: MessageOutlined,
  deadline_reminder: ClockCircleOutlined,
  status_changed: SwapOutlined,
  priority_changed: ArrowUpOutlined,
  system: BellOutlined
};

const getNotificationIcon = (type: NotificationType) => {
  return iconMap[type] || BellOutlined;
};

const formatTime = (dateString: string) => {
  return formatNotificationTime(dateString);
};

const handleOpen = async () => {
  if (!dropdownVisible.value) {
    loading.value = true;
    try {
      await notificationStore.fetchNotifications({
        page: 1,
        pageSize: 20,
        read: false
      });
    } catch (error) {
      console.error('Failed to fetch notifications:', error);
    } finally {
      loading.value = false;
    }
  }
};

const handleNotificationClick = async (notification: Notification) => {
  try {
    if (!notification.read) {
      await notificationStore.markAsRead(notification.id);
    }

    dropdownVisible.value = false;

    if (notification.relatedType && notification.relatedId) {
      const routeMap: Record<string, string> = {
        task: '/tasks',
        document: '/documents',
        change: '/changes',
        test: '/tests'
      };

      const basePath = routeMap[notification.relatedType];
      if (basePath) {
        router.push(`${basePath}/${notification.relatedId}`);
      }
    }
  } catch (error) {
    message.error('操作失败');
  }
};

const handleMarkAllRead = async () => {
  try {
    loading.value = true;
    await notificationStore.markAllAsRead();
    message.success('已全部标记为已读');
  } catch (error) {
    message.error('操作失败');
  } finally {
    loading.value = false;
  }
};

const handleViewAll = () => {
  dropdownVisible.value = false;
  router.push('/notifications');
};

watch(dropdownVisible, (visible) => {
  if (visible) {
    handleOpen();
  }
});

onMounted(() => {
  notificationStore.fetchUnreadCount();

  // 尝试连接WebSocket，如果失败则使用轮询
  try {
    notificationStore.connectWebSocket();
  } catch {
    notificationStore.startPolling(30000);
  }
});
</script>

<style lang="scss">
.notification-badge-dropdown {
  .ant-dropdown-menu {
    padding: 0;
  }
}
</style>

<style scoped lang="scss">
.notification-trigger {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  color: var(--color-text-primary);
  transition: all 0.3s;

  &:hover {
    color: var(--color-primary);
    background-color: var(--color-fill-secondary);
  }
}

.notification-dropdown {
  width: 400px;
  background: var(--color-bg-elevated);
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-lg);
}

.notification-dropdown-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;

  :deep(.ant-tabs) {
    flex: 1;

    .ant-tabs-nav {
      margin: 0;

      &::before {
        border: none;
      }
    }

    .ant-tabs-tab {
      padding: 4px 0;
      margin: 0 12px 0 0;
    }
  }
}

.notification-dropdown-list {
  max-height: 400px;
  overflow-y: auto;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background-color: var(--color-border-secondary);
    border-radius: 3px;

    &:hover {
      background-color: var(--color-border);
    }
  }
}

.notification-item {
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  cursor: pointer;
  transition: background-color 0.2s;
  position: relative;

  &:hover {
    background-color: var(--color-fill-secondary);
  }

  &.unread {
    background-color: var(--color-fill-quaternary);

    &:hover {
      background-color: var(--color-fill-tertiary);
    }
  }
}

.notification-item-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-primary-bg);
  color: var(--color-primary);
  border-radius: var(--border-radius-base);
  flex-shrink: 0;
  font-size: 16px;
}

.notification-item-content {
  flex: 1;
  min-width: 0;
}

.notification-item-title {
  font-size: var(--font-size-base);
  font-weight: 500;
  color: var(--color-text-primary);
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notification-item-desc {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  line-height: 1.5;
  margin-bottom: 6px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.notification-item-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: var(--font-size-sm);
  color: var(--color-text-tertiary);

  .project-tag {
    padding: 0 6px;
    background: var(--color-fill-secondary);
    border-radius: var(--border-radius-sm);
    font-size: 12px;
  }
}

.notification-item-badge {
  width: 8px;
  height: 8px;
  background: var(--color-primary);
  border-radius: 50%;
  flex-shrink: 0;
  margin-top: 12px;
}

.notification-dropdown-footer {
  padding: 8px;
  text-align: center;
}
</style>
