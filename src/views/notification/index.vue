<template>
  <div class="notification-page">
    <PageHeader title="通知中心">
      <template #extra>
        <a-space>
          <a-button @click="handleMarkAllRead">
            <CheckOutlined />
            全部已读
          </a-button>
          <a-button @click="handleOpenSettings">
            <SettingOutlined />
            通知设置
          </a-button>
        </a-space>
      </template>
    </PageHeader>

    <!-- 统计卡片 -->
    <a-row :gutter="16" class="stats-row">
      <a-col :xs="24" :sm="8">
        <a-card>
          <a-statistic
            title="未读通知"
            :value="stats?.unreadCount || 0"
            :value-style="{ color: '#1890ff' }"
          >
            <template #prefix>
              <BellOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="8">
        <a-card>
          <a-statistic
            title="今日通知"
            :value="stats?.todayCount || 0"
            :value-style="{ color: '#52c41a' }"
          >
            <template #prefix>
              <ClockCircleOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="8">
        <a-card>
          <a-statistic
            title="待处理任务"
            :value="stats?.actionRequiredCount || 0"
            :value-style="{ color: '#fa8c16' }"
          >
            <template #prefix>
              <ExclamationCircleOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <!-- 主内容区 -->
    <a-card class="notification-card">
      <!-- 筛选工具栏 -->
      <div class="filter-toolbar">
        <a-space wrap>
          <a-radio-group v-model:value="readFilter" button-style="solid" @change="handleFilterChange">
            <a-radio-button value="all">全部</a-radio-button>
            <a-radio-button value="unread">未读</a-radio-button>
            <a-radio-button value="read">已读</a-radio-button>
          </a-radio-group>

          <a-select
            v-model:value="typeFilter"
            placeholder="通知类型"
            style="width: 150px"
            allowClear
            @change="handleFilterChange"
          >
            <a-select-option v-for="(text, type) in notificationTypeTexts" :key="type" :value="type">
              {{ text }}
            </a-select-option>
          </a-select>

          <a-select
            v-model:value="projectFilter"
            placeholder="项目筛选"
            style="width: 150px"
            allowClear
            @change="handleFilterChange"
          >
            <a-select-option value="project1">项目A</a-select-option>
            <a-select-option value="project2">项目B</a-select-option>
          </a-select>

          <a-range-picker
            v-model:value="dateRange"
            :placeholder="['开始日期', '结束日期']"
            @change="handleFilterChange"
          />

          <a-input-search
            v-model:value="keyword"
            placeholder="搜索通知内容"
            style="width: 200px"
            @search="handleFilterChange"
          />
        </a-space>
      </div>

      <!-- Tabs -->
      <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
        <a-tab-pane key="all" tab="全部通知" />
        <a-tab-pane key="actionRequired">
          <template #tab>
            待处理
            <a-badge
              v-if="stats?.actionRequiredCount"
              :count="stats.actionRequiredCount"
              :number-style="{ backgroundColor: '#fa8c16', marginLeft: '4px' }"
            />
          </template>
        </a-tab-pane>
        <a-tab-pane key="mentioned">
          <template #tab>
            @我的
            <a-badge
              v-if="mentionedCount > 0"
              :count="mentionedCount"
              :number-style="{ backgroundColor: '#722ed1', marginLeft: '4px' }"
            />
          </template>
        </a-tab-pane>
        <a-tab-pane key="system" tab="系统通知" />
      </a-tabs>

      <!-- 通知列表 -->
      <a-spin :spinning="loading">
        <div class="notification-list">
          <template v-if="notifications.length > 0">
            <div
              v-for="notification in notifications"
              :key="notification.id"
              :class="['notification-item', { unread: !notification.read }]"
            >
              <div class="notification-checkbox">
                <a-checkbox
                  :checked="selectedIds.includes(notification.id)"
                  @change="handleSelectChange(notification.id, $event)"
                />
              </div>

              <div class="notification-icon">
                <component :is="getNotificationIcon(notification.type)" />
              </div>

              <div class="notification-content" @click="handleNotificationClick(notification)">
                <div class="notification-header">
                  <span class="notification-title">{{ notification.title }}</span>
                  <a-space>
                    <a-tag v-if="notification.priority" :color="getPriorityColor(notification.priority)">
                      {{ getPriorityText(notification.priority) }}
                    </a-tag>
                    <a-tag :color="getTypeColor(notification.type)">
                      {{ getTypeText(notification.type) }}
                    </a-tag>
                  </a-space>
                </div>

                <div class="notification-desc">{{ notification.content }}</div>

                <div class="notification-meta">
                  <a-space>
                    <span v-if="notification.from" class="meta-item">
                      <UserOutlined />
                      {{ notification.from.name }}
                    </span>
                    <span class="meta-item">
                      <ClockCircleOutlined />
                      {{ formatTime(notification.createdAt) }}
                    </span>
                    <span v-if="notification.projectName" class="meta-item project-tag">
                      <FolderOutlined />
                      {{ notification.projectName }}
                    </span>
                  </a-space>
                </div>
              </div>

              <div class="notification-actions">
                <a-space>
                  <a-tooltip title="标记已读">
                    <a-button
                      v-if="!notification.read"
                      type="text"
                      size="small"
                      @click.stop="handleMarkRead(notification.id)"
                    >
                      <CheckOutlined />
                    </a-button>
                  </a-tooltip>
                  <a-tooltip title="删除">
                    <a-button
                      type="text"
                      danger
                      size="small"
                      @click.stop="handleDelete(notification.id)"
                    >
                      <DeleteOutlined />
                    </a-button>
                  </a-tooltip>
                </a-space>
              </div>
            </div>
          </template>

          <a-empty
            v-else
            :image="Empty.PRESENTED_IMAGE_SIMPLE"
            description="暂无通知"
            style="padding: 60px 0"
          />
        </div>

        <!-- 批量操作 -->
        <div v-if="selectedIds.length > 0" class="batch-actions">
          <a-space>
            <span>已选择 {{ selectedIds.length }} 项</span>
            <a-button size="small" @click="handleBatchMarkRead">批量已读</a-button>
            <a-button size="small" danger @click="handleBatchDelete">批量删除</a-button>
            <a-button size="small" @click="handleClearSelection">取消选择</a-button>
          </a-space>
        </div>

        <!-- 分页 -->
        <div v-if="total > 0" class="pagination">
          <a-pagination
            v-model:current="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            :show-size-changer="true"
            :show-total="(total) => `共 ${total} 条`"
            @change="handlePageChange"
          />
        </div>
      </a-spin>
    </a-card>

    <!-- 通知详情弹窗 -->
    <NotificationDetailModal
      ref="detailModalRef"
      @updated="handleNotificationUpdated"
    />

    <!-- 通知设置抽屉 -->
    <NotificationSettingsDrawer
      ref="settingsDrawerRef"
      @saved="handleSettingsSaved"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { Empty, message, Modal } from 'ant-design-vue';
import { useNotificationStore } from '@/stores/modules/notification';
import PageHeader from '@/components/common/PageHeader.vue';
import NotificationDetailModal from '@/components/notification/NotificationDetailModal.vue';
import NotificationSettingsDrawer from '../components/NotificationSettingsDrawer.vue';
import {
  BellOutlined,
  ClockCircleOutlined,
  ExclamationCircleOutlined,
  CheckOutlined,
  SettingOutlined,
  DeleteOutlined,
  UserOutlined,
  FolderOutlined,
  UserAddOutlined,
  MessageOutlined,
  FileTextOutlined,
  EyeOutlined,
  CheckCircleOutlined,
  SwapOutlined,
  ArrowUpOutlined
} from '@ant-design/icons-vue';
import {
  notificationTypeTexts,
  notificationTypeColors,
  formatNotificationTime
} from '@/utils/notification';
import type { Notification, NotificationType } from '@/types/notification';
import type { Dayjs } from 'dayjs';

const router = useRouter();
const notificationStore = useNotificationStore();

const loading = ref(false);
const activeTab = ref('all');
const readFilter = ref<'all' | 'unread' | 'read'>('all');
const typeFilter = ref<string | undefined>(undefined);
const projectFilter = ref<string | undefined>(undefined);
const dateRange = ref<[Dayjs, Dayjs] | null>(null);
const keyword = ref('');
const currentPage = ref(1);
const pageSize = ref(20);
const selectedIds = ref<string[]>([]);

const detailModalRef = ref<InstanceType<typeof NotificationDetailModal>>();
const settingsDrawerRef = ref<InstanceType<typeof NotificationSettingsDrawer>>();

const notifications = computed(() => notificationStore.notifications);
const total = computed(() => notificationStore.total);
const stats = computed(() => notificationStore.stats);
const mentionedCount = computed(() => notificationStore.mentionedNotifications.length);

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

const getTypeColor = (type: NotificationType) => {
  return notificationTypeColors[type] || '#8c8c8c';
};

const getTypeText = (type: NotificationType) => {
  return notificationTypeTexts[type] || '通知';
};

const getPriorityColor = (priority: string) => {
  const colors: Record<string, string> = {
    urgent: 'red',
    high: 'orange',
    normal: 'blue',
    low: 'default'
  };
  return colors[priority] || 'default';
};

const getPriorityText = (priority: string) => {
  const texts: Record<string, string> = {
    urgent: '紧急',
    high: '高',
    normal: '中',
    low: '低'
  };
  return texts[priority] || priority;
};

const formatTime = (dateString: string) => {
  return formatNotificationTime(dateString);
};

const loadNotifications = async () => {
  loading.value = true;
  try {
    const params: any = {
      page: currentPage.value,
      pageSize: pageSize.value
    };

    // 读取状态筛选
    if (readFilter.value !== 'all') {
      params.read = readFilter.value === 'read';
    }

    // 通知类型筛选
    if (typeFilter.value) {
      params.type = [typeFilter.value];
    }

    // Tab筛选
    if (activeTab.value === 'actionRequired') {
      params.actionRequired = true;
    } else if (activeTab.value === 'mentioned') {
      params.type = ['task_mentioned'];
    } else if (activeTab.value === 'system') {
      params.type = ['system'];
    }

    // 项目筛选
    if (projectFilter.value) {
      params.projectId = projectFilter.value;
    }

    // 日期范围筛选
    if (dateRange.value) {
      params.startDate = dateRange.value[0].format('YYYY-MM-DD');
      params.endDate = dateRange.value[1].format('YYYY-MM-DD');
    }

    // 关键词搜索
    if (keyword.value) {
      params.keyword = keyword.value;
    }

    await notificationStore.fetchNotifications(params);
  } catch (error) {
    message.error('加载通知列表失败');
  } finally {
    loading.value = false;
  }
};

const loadStats = async () => {
  try {
    await notificationStore.fetchStats();
  } catch (error) {
    console.error('Failed to load stats:', error);
  }
};

const handleTabChange = () => {
  currentPage.value = 1;
  selectedIds.value = [];
  loadNotifications();
};

const handleFilterChange = () => {
  currentPage.value = 1;
  selectedIds.value = [];
  loadNotifications();
};

const handlePageChange = (page: number, size: number) => {
  currentPage.value = page;
  pageSize.value = size;
  loadNotifications();
};

const handleSelectChange = (id: string, e: any) => {
  if (e.target.checked) {
    selectedIds.value.push(id);
  } else {
    const index = selectedIds.value.indexOf(id);
    if (index > -1) {
      selectedIds.value.splice(index, 1);
    }
  }
};

const handleClearSelection = () => {
  selectedIds.value = [];
};

const handleNotificationClick = (notification: Notification) => {
  detailModalRef.value?.open(notification.id);
};

const handleMarkRead = async (id: string) => {
  try {
    await notificationStore.markAsRead(id);
    message.success('已标记为已读');
    loadStats();
  } catch (error) {
    message.error('操作失败');
  }
};

const handleDelete = (id: string) => {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除这条通知吗？',
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        await notificationStore.deleteNotification(id);
        message.success('删除成功');
        loadStats();
        if (notifications.value.length === 0 && currentPage.value > 1) {
          currentPage.value--;
        }
        loadNotifications();
      } catch (error) {
        message.error('删除失败');
      }
    }
  });
};

const handleMarkAllRead = async () => {
  try {
    loading.value = true;
    await notificationStore.markAllAsRead();
    message.success('已全部标记为已读');
    loadStats();
    loadNotifications();
  } catch (error) {
    message.error('操作失败');
  } finally {
    loading.value = false;
  }
};

const handleBatchMarkRead = async () => {
  try {
    loading.value = true;
    for (const id of selectedIds.value) {
      await notificationStore.markAsRead(id);
    }
    message.success('批量标记成功');
    selectedIds.value = [];
    loadStats();
    loadNotifications();
  } catch (error) {
    message.error('操作失败');
  } finally {
    loading.value = false;
  }
};

const handleBatchDelete = () => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除选中的 ${selectedIds.value.length} 条通知吗？`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        loading.value = true;
        await notificationStore.batchDelete(selectedIds.value);
        message.success('批量删除成功');
        selectedIds.value = [];
        loadStats();
        loadNotifications();
      } catch (error) {
        message.error('删除失败');
      } finally {
        loading.value = false;
      }
    }
  });
};

const handleOpenSettings = () => {
  settingsDrawerRef.value?.open();
};

const handleNotificationUpdated = () => {
  loadStats();
  loadNotifications();
};

const handleSettingsSaved = () => {
  message.success('通知设置已保存');
};

onMounted(() => {
  loadNotifications();
  loadStats();
});

onUnmounted(() => {
  // 清理
});
</script>

<style scoped lang="scss">
.notification-page {
  padding: 24px;
}

.stats-row {
  margin-bottom: 24px;
}

.notification-card {
  .filter-toolbar {
    margin-bottom: 24px;
  }
}

.notification-list {
  min-height: 400px;
}

.notification-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  border-bottom: 1px solid var(--color-border-secondary);
  transition: background-color 0.2s;

  &:hover {
    background-color: var(--color-fill-quaternary);
  }

  &.unread {
    background-color: var(--color-fill-quaternary);

    &:hover {
      background-color: var(--color-fill-tertiary);
    }
  }

  &:last-child {
    border-bottom: none;
  }
}

.notification-checkbox {
  display: flex;
  align-items: center;
  padding-top: 4px;
}

.notification-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-primary-bg);
  color: var(--color-primary);
  border-radius: var(--border-radius-base);
  flex-shrink: 0;
  font-size: 18px;
}

.notification-content {
  flex: 1;
  min-width: 0;
  cursor: pointer;
}

.notification-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.notification-title {
  font-size: var(--font-size-base);
  font-weight: 600;
  color: var(--color-text-primary);
}

.notification-desc {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  line-height: 1.6;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.notification-meta {
  font-size: var(--font-size-sm);
  color: var(--color-text-tertiary);

  .meta-item {
    display: inline-flex;
    align-items: center;
    gap: 4px;
  }

  .project-tag {
    padding: 2px 8px;
    background: var(--color-fill-secondary);
    border-radius: var(--border-radius-sm);
  }
}

.notification-actions {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.batch-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  background: var(--color-fill-quaternary);
  border-radius: var(--border-radius-base);
  margin-top: 16px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
}
</style>
