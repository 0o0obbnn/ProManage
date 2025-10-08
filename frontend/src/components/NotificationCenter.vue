<template>
  <a-dropdown
    v-model:open="dropdownVisible"
    placement="bottomRight"
    :trigger="['click']"
    overlay-class-name="notification-dropdown-overlay"
  >
    <!-- 通知图标和角标 -->
    <a-badge :count="unreadCount" :overflow-count="99" :offset="[-5, 5]">
      <BellOutlined
        class="notification-icon"
        :style="{ fontSize: '20px', cursor: 'pointer' }"
      />
    </a-badge>

    <!-- 下拉面板 -->
    <template #overlay>
      <div class="notification-dropdown">
        <!-- 头部 -->
        <div class="notification-header">
          <span class="title">通知</span>
          <a-space>
            <a-tooltip title="音效提示">
              <a-button
                type="text"
                size="small"
                @click="notificationStore.toggleAudio()"
              >
                <template #icon>
                  <SoundOutlined v-if="notificationStore.audioEnabled" />
                  <SoundOutlined v-else style="opacity: 0.3" />
                </template>
              </a-button>
            </a-tooltip>
            <a-tooltip title="桌面通知">
              <a-button
                type="text"
                size="small"
                @click="notificationStore.toggleDesktopNotification()"
              >
                <template #icon>
                  <BellOutlined v-if="notificationStore.desktopNotificationEnabled" />
                  <BellOutlined v-else style="opacity: 0.3" />
                </template>
              </a-button>
            </a-tooltip>
            <a-button
              type="link"
              size="small"
              :disabled="unreadCount === 0"
              @click="handleMarkAllAsRead"
            >
              全部已读
            </a-button>
            <a-button
              type="link"
              size="small"
              danger
              :disabled="notifications.length === 0"
              @click="handleClearRead"
            >
              清空已读
            </a-button>
          </a-space>
        </div>

        <!-- 筛选标签 -->
        <div class="notification-tabs">
          <a-radio-group v-model:value="filterType" button-style="solid" size="small">
            <a-radio-button value="all">全部</a-radio-button>
            <a-radio-button value="unread">未读</a-radio-button>
          </a-radio-group>
        </div>

        <!-- 通知列表 -->
        <div
          ref="listContainerRef"
          class="notification-list"
          @scroll="handleScroll"
        >
          <a-spin :spinning="loading">
            <a-list
              v-if="filteredNotifications.length > 0"
              :data-source="filteredNotifications"
              :split="false"
            >
              <template #renderItem="{ item }">
                <a-list-item
                  :class="['notification-item', { unread: !item.isRead }]"
                  @click="handleNotificationClick(item)"
                >
                  <a-list-item-meta>
                    <template #avatar>
                      <NotificationIcon :type="item.type" :priority="item.priority" />
                    </template>
                    <template #title>
                      <div class="notification-title">
                        {{ item.title }}
                        <a-tag
                          v-if="item.priority === 'URGENT'"
                          color="red"
                          size="small"
                        >
                          紧急
                        </a-tag>
                        <a-tag
                          v-else-if="item.priority === 'HIGH'"
                          color="orange"
                          size="small"
                        >
                          重要
                        </a-tag>
                      </div>
                    </template>
                    <template #description>
                      <div class="notification-content">{{ item.content }}</div>
                      <div class="notification-meta">
                        <span class="time">{{ formatTime(item.createdAt) }}</span>
                        <a-button
                          type="link"
                          size="small"
                          danger
                          @click.stop="handleDelete(item.id)"
                        >
                          删除
                        </a-button>
                      </div>
                    </template>
                  </a-list-item-meta>
                </a-list-item>
              </template>
            </a-list>

            <!-- 空状态 -->
            <a-empty
              v-else
              :image="Empty.PRESENTED_IMAGE_SIMPLE"
              description="暂无通知"
              style="padding: 40px 0"
            />
          </a-spin>

          <!-- 加载更多 -->
          <div v-if="hasMore && !loading" class="load-more">
            <a-button type="link" size="small" @click="loadMore">
              加载更多
            </a-button>
          </div>
        </div>

        <!-- 底部 -->
        <div class="notification-footer">
          <a-button type="link" block @click="viewAll">
            查看全部通知
          </a-button>
        </div>
      </div>
    </template>
  </a-dropdown>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { message, Empty } from 'ant-design-vue'
import { BellOutlined, SoundOutlined } from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import NotificationIcon from './NotificationIcon.vue'
import { useNotificationStore } from '@/stores/modules/notification'
import type { Notification, NotificationType } from '@/types/notification'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

/**
 * 使用notification store
 */
const notificationStore = useNotificationStore()

/**
 * 组件状态
 */
const router = useRouter()
const dropdownVisible = ref(false)
const loading = ref(false)
const filterType = ref<'all' | 'unread'>('all')
const listContainerRef = ref<HTMLElement>()

// 从store获取通知数据
const notifications = computed(() => notificationStore.notifications)
const unreadCount = computed(() => notificationStore.unreadCount)

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const hasMore = computed(() => notifications.value.length < total.value)

/**
 * 过滤后的通知列表
 */
const filteredNotifications = computed(() => {
  if (filterType.value === 'unread') {
    return notifications.value.filter(n => !n.isRead)
  }
  return notifications.value
})

/**
 * 获取通知列表
 */
const fetchNotifications = async (append = false) => {
  loading.value = true
  try {
    await notificationStore.fetchNotifications()
  } catch (error) {
    message.error('获取通知列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 获取未读数量
 */
const fetchUnreadCount = async () => {
  try {
    await notificationStore.fetchUnreadCount()
  } catch (error) {
    console.error('获取未读数量失败:', error)
  }
}

/**
 * 格式化时间
 */
const formatTime = (time: string): string => {
  return dayjs(time).fromNow()
}

/**
 * 处理通知点击
 */
const handleNotificationClick = async (notification: Notification) => {
  // 标记为已读
  if (!notification.isRead) {
    await notificationStore.markAsRead(notification.id)
  }

  // 跳转到相关页面
  if (notification.link) {
    dropdownVisible.value = false
    router.push(notification.link)
  } else if (notification.relatedId && notification.relatedType) {
    dropdownVisible.value = false
    navigateToRelated(notification.relatedType, notification.relatedId)
  }
}

/**
 * 根据类型跳转到相关页面
 */
const navigateToRelated = (type: string, id: number) => {
  const routeMap: Record<string, string> = {
    TASK: `/tasks/${id}`,
    DOCUMENT: `/documents/${id}`,
    CHANGE: `/changes/${id}`,
    TEST: `/tests/${id}`
  }

  const route = routeMap[type]
  if (route) {
    router.push(route)
  }
}

/**
 * 标记所有为已读
 */
const handleMarkAllAsRead = async () => {
  try {
    await notificationStore.markAllAsRead()
    message.success('已全部标记为已读')
  } catch (error) {
    message.error('操作失败')
  }
}

/**
 * 删除通知
 */
const handleDelete = async (id: number) => {
  try {
    await notificationStore.deleteNotification(id)
    message.success('删除成功')
  } catch (error) {
    message.error('删除失败')
  }
}

/**
 * 清空已读通知
 */
const handleClearRead = async () => {
  try {
    // 使用store的API
    const { notificationApi } = await import('@/api/modules/notification')
    await notificationApi.clearReadNotifications()
    // 刷新通知列表
    await fetchNotifications()
    message.success('已清空已读通知')
  } catch (error) {
    message.error('操作失败')
  }
}

/**
 * 加载更多
 */
const loadMore = () => {
  if (!hasMore.value || loading.value) return
  currentPage.value++
  fetchNotifications(true)
}

/**
 * 滚动加载
 */
const handleScroll = (e: Event) => {
  const target = e.target as HTMLElement
  const scrollTop = target.scrollTop
  const scrollHeight = target.scrollHeight
  const clientHeight = target.clientHeight

  // 滚动到底部时加载更多
  if (scrollHeight - scrollTop - clientHeight < 50) {
    loadMore()
  }
}

/**
 * 查看全部
 */
const viewAll = () => {
  dropdownVisible.value = false
  router.push('/notifications')
}

/**
 * 监听筛选类型变化
 */
watch(filterType, () => {
  currentPage.value = 1
  fetchNotifications()
})

/**
 * 监听下拉框显示
 */
watch(dropdownVisible, (visible) => {
  if (visible) {
    currentPage.value = 1
    fetchNotifications()
  }
})

/**
 * 组件挂载
 */
onMounted(() => {
  // 初始加载未读数
  fetchUnreadCount()
  // WebSocket会实时更新通知,不需要定时刷新
})
</script>

<style scoped lang="scss">
.notification-icon {
  color: rgba(0, 0, 0, 0.65);
  transition: color 0.3s;

  &:hover {
    color: #1890ff;
  }
}

:deep(.notification-dropdown-overlay) {
  .ant-dropdown-menu {
    padding: 0;
  }
}

.notification-dropdown {
  width: 380px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);

  .notification-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    border-bottom: 1px solid #f0f0f0;

    .title {
      font-size: 16px;
      font-weight: 500;
    }
  }

  .notification-tabs {
    padding: 8px 16px;
    border-bottom: 1px solid #f0f0f0;
  }

  .notification-list {
    max-height: 400px;
    overflow-y: auto;

    &::-webkit-scrollbar {
      width: 6px;
    }

    &::-webkit-scrollbar-thumb {
      background: #d9d9d9;
      border-radius: 3px;

      &:hover {
        background: #bfbfbf;
      }
    }

    .notification-item {
      padding: 12px 16px;
      cursor: pointer;
      transition: background-color 0.3s;

      &:hover {
        background-color: #f5f5f5;
      }

      &.unread {
        background-color: #e6f7ff;

        &:hover {
          background-color: #bae7ff;
        }
      }

      .notification-title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-weight: 500;
      }

      .notification-content {
        color: rgba(0, 0, 0, 0.65);
        margin-bottom: 4px;
      }

      .notification-meta {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .time {
          font-size: 12px;
          color: rgba(0, 0, 0, 0.45);
        }
      }
    }

    .load-more {
      text-align: center;
      padding: 8px 0;
      border-top: 1px solid #f0f0f0;
    }
  }

  .notification-footer {
    border-top: 1px solid #f0f0f0;
  }
}
</style>

