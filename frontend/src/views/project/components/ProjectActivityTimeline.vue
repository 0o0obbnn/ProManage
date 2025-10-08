<template>
  <a-card class="project-activity-timeline" title="最近活动">
    <!-- 加载状态 -->
    <div v-if="loading" class="project-activity-timeline__loading">
      <a-spin />
    </div>

    <!-- 活动时间线 -->
    <a-timeline v-else-if="activities.length > 0" mode="left">
      <a-timeline-item
        v-for="activity in activities"
        :key="activity.id"
        :color="getActivityColor(activity.type)"
      >
        <template #dot>
          <component :is="getActivityIcon(activity.type)" />
        </template>

        <div class="project-activity-timeline__item">
          <div class="project-activity-timeline__item-header">
            <a-space>
              <a-avatar :size="24">
                {{ activity.userName?.charAt(0) || 'U' }}
              </a-avatar>
              <span class="project-activity-timeline__item-user">
                {{ activity.userName }}
              </span>
            </a-space>
            <span class="project-activity-timeline__item-time">
              {{ formatTime(activity.createdAt) }}
            </span>
          </div>

          <div class="project-activity-timeline__item-content">
            <span class="project-activity-timeline__item-action">
              {{ getActivityText(activity.type) }}
            </span>
            <span class="project-activity-timeline__item-description">
              {{ activity.description }}
            </span>
          </div>
        </div>
      </a-timeline-item>
    </a-timeline>

    <!-- 空状态 -->
    <a-empty
      v-else
      description="暂无活动记录"
      :image="Empty.PRESENTED_IMAGE_SIMPLE"
    />

    <!-- 查看更多 -->
    <div v-if="activities.length > 0" class="project-activity-timeline__footer">
      <a-button type="link" size="small">
        查看全部活动
        <RightOutlined />
      </a-button>
    </div>
  </a-card>
</template>

<script setup lang="ts">
import { Empty } from 'ant-design-vue'
import {
  FileAddOutlined,
  EditOutlined,
  DeleteOutlined,
  UserAddOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  RightOutlined
} from '@ant-design/icons-vue'
import type { ProjectActivity } from '@/types/project'
import { formatDistanceToNow } from 'date-fns'
import { zhCN } from 'date-fns/locale'

/**
 * 组件属性
 */
interface Props {
  activities: ProjectActivity[]
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  loading: false
})

/**
 * 获取活动图标
 */
const getActivityIcon = (type: string) => {
  const iconMap: Record<string, any> = {
    create: FileAddOutlined,
    update: EditOutlined,
    delete: DeleteOutlined,
    member_add: UserAddOutlined,
    task_complete: CheckCircleOutlined,
    default: ClockCircleOutlined
  }
  return iconMap[type] || iconMap.default
}

/**
 * 获取活动颜色
 */
const getActivityColor = (type: string): string => {
  const colorMap: Record<string, string> = {
    create: 'green',
    update: 'blue',
    delete: 'red',
    member_add: 'purple',
    task_complete: 'green',
    default: 'gray'
  }
  return colorMap[type] || colorMap.default
}

/**
 * 获取活动文本
 */
const getActivityText = (type: string): string => {
  const textMap: Record<string, string> = {
    create: '创建了',
    update: '更新了',
    delete: '删除了',
    member_add: '添加了成员',
    member_remove: '移除了成员',
    task_create: '创建了任务',
    task_update: '更新了任务',
    task_complete: '完成了任务',
    document_create: '创建了文档',
    document_update: '更新了文档',
    comment_add: '添加了评论',
    default: '执行了操作'
  }
  return textMap[type] || textMap.default
}

/**
 * 格式化时间
 */
const formatTime = (date: string): string => {
  try {
    return formatDistanceToNow(new Date(date), {
      addSuffix: true,
      locale: zhCN
    })
  } catch {
    return date
  }
}
</script>

<style lang="scss" scoped>
.project-activity-timeline {
  margin-bottom: 24px;

  &__loading {
    display: flex;
    justify-content: center;
    padding: 40px 0;
  }

  &__item {
    &-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;
    }

    &-user {
      font-weight: 500;
      color: #262626;
    }

    &-time {
      font-size: 12px;
      color: #8c8c8c;
    }

    &-content {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    &-action {
      font-size: 14px;
      color: #595959;
    }

    &-description {
      font-size: 13px;
      color: #8c8c8c;
    }
  }

  &__footer {
    text-align: center;
    margin-top: 16px;
    padding-top: 16px;
    border-top: 1px solid #f0f0f0;
  }
}

:deep(.ant-timeline-item-content) {
  margin-left: 24px;
}
</style>

