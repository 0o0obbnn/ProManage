<template>
  <a-card title="最近活动" :loading="loading">
    <template #extra>
      <a-button type="link" size="small" @click="handleRefresh">
        <template #icon><ReloadOutlined /></template>
        刷新
      </a-button>
    </template>

    <a-empty v-if="activities.length === 0" description="暂无活动记录" />

    <a-timeline v-else>
      <a-timeline-item
        v-for="activity in activities"
        :key="activity.id"
        :color="getActivityColor(activity.type)"
      >
        <template #dot>
          <component :is="getActivityIcon(activity.type)" />
        </template>

        <div class="activity-content">
          <div class="activity-header">
            <a-avatar
              v-if="activity.user.avatar"
              :src="activity.user.avatar"
              :size="24"
            />
            <a-avatar v-else :size="24">
              {{ activity.user.name.charAt(0) }}
            </a-avatar>
            <strong class="user-name">{{ activity.user.name }}</strong>
            <span class="action">{{ activity.action }}</span>
            <a class="target" @click="goToDetail(activity)">{{ activity.target }}</a>
          </div>
          <div class="activity-time">{{ formatTime(activity.createdAt) }}</div>
        </div>
      </a-timeline-item>
    </a-timeline>

    <div v-if="hasMore" class="load-more">
      <a-button type="link" @click="handleLoadMore">
        加载更多
      </a-button>
    </div>
  </a-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  ReloadOutlined,
  CheckCircleOutlined,
  FileTextOutlined,
  ExclamationCircleOutlined,
  MessageOutlined,
  ProjectOutlined
} from '@ant-design/icons-vue'
import type { Activity } from '@/api/modules/dashboard'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

/**
 * Props
 */
interface Props {
  activities: Activity[]
  loading?: boolean
  hasMore?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  hasMore: false
})

/**
 * Emits
 */
const emit = defineEmits<{
  refresh: []
  loadMore: []
}>()

/**
 * 组件状态
 */
const router = useRouter()

/**
 * 获取活动图标
 */
const getActivityIcon = (type: string) => {
  const iconMap: Record<string, any> = {
    task: CheckCircleOutlined,
    document: FileTextOutlined,
    change: ExclamationCircleOutlined,
    comment: MessageOutlined,
    project: ProjectOutlined
  }
  return iconMap[type] || CheckCircleOutlined
}

/**
 * 获取活动颜色
 */
const getActivityColor = (type: string) => {
  const colorMap: Record<string, string> = {
    task: '#52c41a',
    document: '#722ed1',
    change: '#fa8c16',
    comment: '#1890ff',
    project: '#13c2c2'
  }
  return colorMap[type] || '#1890ff'
}

/**
 * 格式化时间
 */
const formatTime = (time: string): string => {
  return dayjs(time).fromNow()
}

/**
 * 跳转到详情
 */
const goToDetail = (activity: Activity) => {
  const routeMap: Record<string, string> = {
    task: `/tasks/${activity.targetId}`,
    document: `/documents/${activity.targetId}`,
    change: `/changes/${activity.targetId}`,
    project: `/projects/${activity.targetId}`
  }

  const route = routeMap[activity.type]
  if (route) {
    router.push(route)
  }
}

/**
 * 刷新
 */
const handleRefresh = () => {
  emit('refresh')
}

/**
 * 加载更多
 */
const handleLoadMore = () => {
  emit('loadMore')
}
</script>

<style scoped lang="scss">
.activity-content {
  .activity-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 4px;

    .user-name {
      color: rgba(0, 0, 0, 0.85);
    }

    .action {
      color: rgba(0, 0, 0, 0.65);
    }

    .target {
      color: #1890ff;
      cursor: pointer;

      &:hover {
        text-decoration: underline;
      }
    }
  }

  .activity-time {
    font-size: 12px;
    color: rgba(0, 0, 0, 0.45);
    margin-left: 32px;
  }
}

.load-more {
  text-align: center;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

:deep(.ant-timeline-item-content) {
  margin-left: 24px;
}
</style>

