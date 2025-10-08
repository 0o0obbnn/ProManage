<template>
  <div class="notification-page">
    <a-card title="通知中心" :bordered="false">
      <!-- 操作栏 -->
      <template #extra>
        <a-space>
          <a-button
            type="primary"
            :disabled="unreadCount === 0"
            @click="handleMarkAllAsRead"
          >
            全部已读
          </a-button>
          <a-button
            danger
            :disabled="selectedRowKeys.length === 0"
            @click="handleBatchDelete"
          >
            批量删除
          </a-button>
          <a-button @click="handleClearRead">
            清空已读
          </a-button>
        </a-space>
      </template>

      <!-- 筛选栏 -->
      <div class="filter-bar">
        <a-space>
          <a-radio-group v-model:value="queryParams.isRead" button-style="solid">
            <a-radio-button :value="undefined">全部</a-radio-button>
            <a-radio-button :value="false">未读</a-radio-button>
            <a-radio-button :value="true">已读</a-radio-button>
          </a-radio-group>

          <a-select
            v-model:value="queryParams.type"
            placeholder="通知类型"
            style="width: 150px"
            allow-clear
          >
            <a-select-option value="SYSTEM">系统通知</a-select-option>
            <a-select-option value="TASK">任务通知</a-select-option>
            <a-select-option value="DOCUMENT">文档通知</a-select-option>
            <a-select-option value="CHANGE">变更通知</a-select-option>
            <a-select-option value="TEST">测试通知</a-select-option>
            <a-select-option value="COMMENT">评论通知</a-select-option>
            <a-select-option value="MENTION">@提及</a-select-option>
            <a-select-option value="APPROVAL">审批通知</a-select-option>
          </a-select>

          <a-range-picker
            v-model:value="dateRange"
            format="YYYY-MM-DD"
            @change="handleDateChange"
          />

          <a-button type="primary" @click="handleSearch">
            查询
          </a-button>
          <a-button @click="handleReset">
            重置
          </a-button>
        </a-space>
      </div>

      <!-- 通知列表 -->
      <a-table
        :columns="columns"
        :data-source="notifications"
        :loading="loading"
        :pagination="pagination"
        :row-selection="rowSelection"
        :row-key="(record) => record.id"
        @change="handleTableChange"
      >
        <!-- 标题列 -->
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'title'">
            <div class="title-cell">
              <NotificationIcon :type="record.type" :priority="record.priority" size="small" />
              <span :class="{ 'unread-title': !record.isRead }">
                {{ record.title }}
              </span>
              <a-tag v-if="record.priority === 'URGENT'" color="red" size="small">
                紧急
              </a-tag>
              <a-tag v-else-if="record.priority === 'HIGH'" color="orange" size="small">
                重要
              </a-tag>
            </div>
          </template>

          <!-- 类型列 -->
          <template v-else-if="column.key === 'type'">
            <a-tag :color="getTypeColor(record.type)">
              {{ getTypeName(record.type) }}
            </a-tag>
          </template>

          <!-- 状态列 -->
          <template v-else-if="column.key === 'isRead'">
            <a-tag :color="record.isRead ? 'default' : 'blue'">
              {{ record.isRead ? '已读' : '未读' }}
            </a-tag>
          </template>

          <!-- 时间列 -->
          <template v-else-if="column.key === 'createdAt'">
            {{ formatDate(record.createdAt) }}
          </template>

          <!-- 操作列 -->
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button
                v-if="!record.isRead"
                type="link"
                size="small"
                @click="handleMarkAsRead(record)"
              >
                标记已读
              </a-button>
              <a-button
                type="link"
                size="small"
                @click="handleView(record)"
              >
                查看
              </a-button>
              <a-button
                type="link"
                size="small"
                danger
                @click="handleDelete(record.id)"
              >
                删除
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import dayjs, { Dayjs } from 'dayjs'
import NotificationIcon from '@/components/NotificationIcon.vue'
import { notificationApi } from '@/api/modules/notification'
import type { Notification, NotificationType, NotificationQueryParams } from '@/types/notification'

/**
 * 组件状态
 */
const router = useRouter()
const notifications = ref<Notification[]>([])
const loading = ref(false)
const unreadCount = ref(0)
const selectedRowKeys = ref<number[]>([])
const dateRange = ref<[Dayjs, Dayjs] | null>(null)

// 查询参数
const queryParams = reactive<NotificationQueryParams>({
  page: 1,
  pageSize: 20,
  type: undefined,
  isRead: undefined,
  startDate: undefined,
  endDate: undefined
})

// 分页配置
const pagination = computed(() => ({
  current: queryParams.page,
  pageSize: queryParams.pageSize,
  total: total.value,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`
}))

const total = ref(0)

// 行选择配置
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: number[]) => {
    selectedRowKeys.value = keys
  }
}))

// 表格列配置
const columns = [
  {
    title: '标题',
    key: 'title',
    dataIndex: 'title',
    width: 300
  },
  {
    title: '内容',
    key: 'content',
    dataIndex: 'content',
    ellipsis: true
  },
  {
    title: '类型',
    key: 'type',
    dataIndex: 'type',
    width: 100
  },
  {
    title: '状态',
    key: 'isRead',
    dataIndex: 'isRead',
    width: 80
  },
  {
    title: '时间',
    key: 'createdAt',
    dataIndex: 'createdAt',
    width: 180
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    fixed: 'right'
  }
]

/**
 * 获取通知列表
 */
const fetchNotifications = async () => {
  loading.value = true
  try {
    const res = await notificationApi.getNotifications(queryParams)
    notifications.value = res.data.list
    total.value = res.data.total
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
    const res = await notificationApi.getUnreadCount()
    unreadCount.value = res.data.total
  } catch (error) {
    console.error('获取未读数量失败:', error)
  }
}

/**
 * 处理表格变化
 */
const handleTableChange = (pag: any) => {
  queryParams.page = pag.current
  queryParams.pageSize = pag.pageSize
  fetchNotifications()
}

/**
 * 处理日期变化
 */
const handleDateChange = (dates: [Dayjs, Dayjs] | null) => {
  if (dates) {
    queryParams.startDate = dates[0].format('YYYY-MM-DD')
    queryParams.endDate = dates[1].format('YYYY-MM-DD')
  } else {
    queryParams.startDate = undefined
    queryParams.endDate = undefined
  }
}

/**
 * 处理查询
 */
const handleSearch = () => {
  queryParams.page = 1
  fetchNotifications()
}

/**
 * 处理重置
 */
const handleReset = () => {
  queryParams.type = undefined
  queryParams.isRead = undefined
  queryParams.startDate = undefined
  queryParams.endDate = undefined
  dateRange.value = null
  queryParams.page = 1
  fetchNotifications()
}

/**
 * 标记为已读
 */
const handleMarkAsRead = async (notification: Notification) => {
  try {
    await notificationApi.markAsRead(notification.id)
    notification.isRead = true
    unreadCount.value = Math.max(0, unreadCount.value - 1)
    message.success('已标记为已读')
  } catch (error) {
    message.error('操作失败')
  }
}

/**
 * 标记所有为已读
 */
const handleMarkAllAsRead = async () => {
  try {
    await notificationApi.markAllAsRead()
    notifications.value.forEach(n => (n.isRead = true))
    unreadCount.value = 0
    message.success('已全部标记为已读')
  } catch (error) {
    message.error('操作失败')
  }
}

/**
 * 查看通知
 */
const handleView = (notification: Notification) => {
  if (!notification.isRead) {
    handleMarkAsRead(notification)
  }

  if (notification.link) {
    router.push(notification.link)
  } else if (notification.relatedId && notification.relatedType) {
    const routeMap: Record<string, string> = {
      TASK: `/tasks/${notification.relatedId}`,
      DOCUMENT: `/documents/${notification.relatedId}`,
      CHANGE: `/changes/${notification.relatedId}`,
      TEST: `/tests/${notification.relatedId}`
    }
    const route = routeMap[notification.relatedType]
    if (route) {
      router.push(route)
    }
  }
}

/**
 * 删除通知
 */
const handleDelete = async (id: number) => {
  try {
    await notificationApi.deleteNotification(id)
    const index = notifications.value.findIndex(n => n.id === id)
    if (index > -1) {
      const notification = notifications.value[index]
      if (!notification.isRead) {
        unreadCount.value = Math.max(0, unreadCount.value - 1)
      }
      notifications.value.splice(index, 1)
      total.value--
    }
    message.success('删除成功')
  } catch (error) {
    message.error('删除失败')
  }
}

/**
 * 批量删除
 */
const handleBatchDelete = async () => {
  try {
    await notificationApi.batchDeleteNotifications(selectedRowKeys.value)
    notifications.value = notifications.value.filter(
      n => !selectedRowKeys.value.includes(n.id)
    )
    total.value -= selectedRowKeys.value.length
    selectedRowKeys.value = []
    message.success('删除成功')
    fetchUnreadCount()
  } catch (error) {
    message.error('删除失败')
  }
}

/**
 * 清空已读
 */
const handleClearRead = async () => {
  try {
    await notificationApi.clearReadNotifications()
    notifications.value = notifications.value.filter(n => !n.isRead)
    total.value = notifications.value.length
    message.success('已清空已读通知')
  } catch (error) {
    message.error('操作失败')
  }
}

/**
 * 获取类型名称
 */
const getTypeName = (type: NotificationType): string => {
  const nameMap: Record<NotificationType, string> = {
    SYSTEM: '系统通知',
    TASK: '任务通知',
    DOCUMENT: '文档通知',
    CHANGE: '变更通知',
    TEST: '测试通知',
    COMMENT: '评论通知',
    MENTION: '@提及',
    APPROVAL: '审批通知'
  }
  return nameMap[type] || type
}

/**
 * 获取类型颜色
 */
const getTypeColor = (type: NotificationType): string => {
  const colorMap: Record<NotificationType, string> = {
    SYSTEM: 'blue',
    TASK: 'green',
    DOCUMENT: 'purple',
    CHANGE: 'orange',
    TEST: 'cyan',
    COMMENT: 'magenta',
    MENTION: 'gold',
    APPROVAL: 'geekblue'
  }
  return colorMap[type] || 'default'
}

/**
 * 格式化日期
 */
const formatDate = (date: string): string => {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

/**
 * 组件挂载
 */
onMounted(() => {
  fetchNotifications()
  fetchUnreadCount()
})
</script>

<style scoped lang="scss">
.notification-page {
  padding: 24px;

  .filter-bar {
    margin-bottom: 16px;
  }

  .title-cell {
    display: flex;
    align-items: center;
    gap: 8px;

    .unread-title {
      font-weight: 500;
    }
  }
}
</style>

