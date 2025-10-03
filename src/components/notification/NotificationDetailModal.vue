<template>
  <a-modal
    v-model:open="visible"
    title="通知详情"
    width="600px"
    :footer="null"
    @cancel="handleClose"
  >
    <a-spin :spinning="loading">
      <div v-if="notification" class="notification-detail">
        <div class="notification-header">
          <div class="notification-type-badge" :style="{ background: getTypeColor(notification.type) }">
            <component :is="getNotificationIcon(notification.type)" />
            <span>{{ getTypeText(notification.type) }}</span>
          </div>
          <a-tag v-if="notification.priority" :color="getPriorityColor(notification.priority)">
            {{ getPriorityText(notification.priority) }}
          </a-tag>
        </div>

        <h3 class="notification-title">{{ notification.title }}</h3>

        <div class="notification-content">
          {{ notification.content }}
        </div>

        <a-divider />

        <div class="notification-meta">
          <div v-if="notification.from" class="meta-item">
            <span class="meta-label">发送人:</span>
            <a-space>
              <a-avatar v-if="notification.from.avatar" :src="notification.from.avatar" :size="24" />
              <a-avatar v-else :size="24">
                {{ notification.from.name.charAt(0) }}
              </a-avatar>
              <span>{{ notification.from.name }}</span>
            </a-space>
          </div>

          <div class="meta-item">
            <span class="meta-label">时间:</span>
            <span>{{ formatDateTime(notification.createdAt) }}</span>
          </div>

          <div v-if="notification.projectName" class="meta-item">
            <span class="meta-label">项目:</span>
            <a-tag>{{ notification.projectName }}</a-tag>
          </div>

          <div v-if="notification.relatedType" class="meta-item">
            <span class="meta-label">相关内容:</span>
            <a-button type="link" size="small" @click="handleViewRelated">
              查看{{ getRelatedTypeText(notification.relatedType) }}
              <RightOutlined />
            </a-button>
          </div>
        </div>

        <div class="notification-actions">
          <a-space>
            <a-button
              v-if="!notification.read"
              type="primary"
              @click="handleMarkRead"
            >
              标记已读
            </a-button>
            <a-button
              v-else
              @click="handleMarkUnread"
            >
              标记未读
            </a-button>
            <a-button danger @click="handleDelete">
              删除
            </a-button>
            <a-button v-if="notification.relatedType" @click="handleViewRelated">
              前往查看
            </a-button>
          </a-space>
        </div>
      </div>
    </a-spin>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import { RightOutlined } from '@ant-design/icons-vue';
import { useNotificationStore } from '@/stores/modules/notification';
import {
  notificationTypeIcons,
  notificationTypeColors,
  notificationTypeTexts
} from '@/utils/notification';
import type { Notification, NotificationType, NotificationRelatedType } from '@/types/notification';
import {
  UserAddOutlined,
  MessageOutlined,
  FileTextOutlined,
  EyeOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  SwapOutlined,
  ArrowUpOutlined,
  BellOutlined
} from '@ant-design/icons-vue';

interface Props {
  notificationId?: string;
}

const props = defineProps<Props>();

const emit = defineEmits<{
  close: [];
  updated: [];
}>();

const router = useRouter();
const notificationStore = useNotificationStore();

const visible = ref(false);
const loading = ref(false);
const notification = ref<Notification | null>(null);

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

const getRelatedTypeText = (type: NotificationRelatedType) => {
  const texts: Record<string, string> = {
    task: '任务',
    document: '文档',
    change: '变更',
    test: '测试',
    comment: '评论',
    project: '项目'
  };
  return texts[type] || '内容';
};

const formatDateTime = (dateString: string) => {
  const date = new Date(dateString);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

const loadNotification = async (id: string) => {
  loading.value = true;
  try {
    notification.value = await notificationStore.fetchNotificationDetail(id);
  } catch (error) {
    message.error('加载通知详情失败');
    handleClose();
  } finally {
    loading.value = false;
  }
};

const handleMarkRead = async () => {
  if (!notification.value) return;

  try {
    await notificationStore.markAsRead(notification.value.id);
    notification.value.read = true;
    message.success('已标记为已读');
    emit('updated');
  } catch (error) {
    message.error('操作失败');
  }
};

const handleMarkUnread = async () => {
  if (!notification.value) return;

  try {
    // 注意：这里需要API支持标记为未读，如果没有可以移除此功能
    notification.value.read = false;
    message.success('已标记为未读');
    emit('updated');
  } catch (error) {
    message.error('操作失败');
  }
};

const handleDelete = async () => {
  if (!notification.value) return;

  try {
    await notificationStore.deleteNotification(notification.value.id);
    message.success('已删除');
    emit('updated');
    handleClose();
  } catch (error) {
    message.error('删除失败');
  }
};

const handleViewRelated = () => {
  if (!notification.value?.relatedType || !notification.value?.relatedId) {
    return;
  }

  const routeMap: Record<string, string> = {
    task: '/tasks',
    document: '/documents',
    change: '/changes',
    test: '/tests'
  };

  const basePath = routeMap[notification.value.relatedType];
  if (basePath) {
    router.push(`${basePath}/${notification.value.relatedId}`);
    handleClose();
  }
};

const handleClose = () => {
  visible.value = false;
  emit('close');
};

const open = (id: string) => {
  visible.value = true;
  loadNotification(id);
};

watch(
  () => props.notificationId,
  (id) => {
    if (id) {
      open(id);
    }
  }
);

defineExpose({
  open
});
</script>

<style scoped lang="scss">
.notification-detail {
  padding: 8px 0;
}

.notification-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.notification-type-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  border-radius: var(--border-radius-base);
  color: #fff;
  font-size: var(--font-size-sm);
  font-weight: 500;
}

.notification-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0 0 16px 0;
  line-height: 1.5;
}

.notification-content {
  font-size: var(--font-size-base);
  color: var(--color-text-secondary);
  line-height: 1.8;
  white-space: pre-wrap;
  word-wrap: break-word;
  padding: 16px;
  background: var(--color-fill-quaternary);
  border-radius: var(--border-radius-base);
  margin-bottom: 16px;
}

.notification-meta {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;

  .meta-label {
    font-weight: 500;
    color: var(--color-text-tertiary);
    min-width: 70px;
  }
}

.notification-actions {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid var(--color-border-secondary);
  display: flex;
  justify-content: flex-end;
}
</style>
