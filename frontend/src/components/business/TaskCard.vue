<template>
  <div class="task-card" @click="emit('click', task)">
    <!-- 任务标题 -->
    <div class="task-card__header">
      <h4 class="task-card__title">{{ task.title }}</h4>
      <a-tag :color="getPriorityColor(task.priority)" size="small">
        {{ getPriorityText(task.priority) }}
      </a-tag>
    </div>

    <!-- 任务描述 -->
    <p v-if="task.description" class="task-card__description">
      {{ truncateText(task.description, 100) }}
    </p>

    <!-- 任务元信息 -->
    <div class="task-card__meta">
      <!-- 指派人 -->
      <div v-if="task.assigneeName" class="task-card__assignee">
        <a-avatar :size="24">
          {{ task.assigneeName.charAt(0) }}
        </a-avatar>
        <span class="task-card__assignee-name">{{ task.assigneeName }}</span>
      </div>

      <!-- 截止日期 -->
      <div v-if="task.dueDate" class="task-card__due-date" :class="getDueDateClass(task.dueDate)">
        <ClockCircleOutlined />
        <span>{{ formatDate(task.dueDate) }}</span>
      </div>
    </div>

    <!-- 任务统计 -->
    <div class="task-card__stats">
      <!-- 子任务 -->
      <div v-if="task.subtaskCount > 0" class="task-card__stat">
        <CheckSquareOutlined />
        <span>{{ task.subtaskCount }}</span>
      </div>

      <!-- 评论 -->
      <div v-if="task.commentCount > 0" class="task-card__stat">
        <CommentOutlined />
        <span>{{ task.commentCount }}</span>
      </div>

      <!-- 附件 -->
      <div v-if="task.attachmentCount > 0" class="task-card__stat">
        <PaperClipOutlined />
        <span>{{ task.attachmentCount }}</span>
      </div>

      <!-- 进度 -->
      <div v-if="task.progressPercentage > 0" class="task-card__progress">
        <a-progress
          :percent="task.progressPercentage"
          :show-info="false"
          :stroke-width="4"
          :stroke-color="getProgressColor(task.progressPercentage)"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  ClockCircleOutlined,
  CheckSquareOutlined,
  CommentOutlined,
  PaperClipOutlined
} from '@ant-design/icons-vue'
import type { Task, TaskPriority } from '@/types/task'
import { format, isPast, isToday } from 'date-fns'

/**
 * 组件属性
 */
interface Props {
  task: Task
}

const props = defineProps<Props>()

/**
 * 组件事件
 */
interface Emits {
  (e: 'click', task: Task): void
}

const emit = defineEmits<Emits>()

/**
 * 获取优先级颜色
 */
const getPriorityColor = (priority: TaskPriority): string => {
  const colorMap: Record<TaskPriority, string> = {
    0: 'default', // 低
    1: 'blue', // 中
    2: 'orange', // 高
    3: 'red' // 紧急
  }
  return colorMap[priority] || 'default'
}

/**
 * 获取优先级文本
 */
const getPriorityText = (priority: TaskPriority): string => {
  const textMap: Record<TaskPriority, string> = {
    0: '低',
    1: '中',
    2: '高',
    3: '紧急'
  }
  return textMap[priority] || '未知'
}

/**
 * 截断文本
 */
const truncateText = (text: string, maxLength: number): string => {
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}

/**
 * 格式化日期
 */
const formatDate = (date: string): string => {
  try {
    return format(new Date(date), 'MM-dd')
  } catch {
    return date
  }
}

/**
 * 获取截止日期样式类
 */
const getDueDateClass = (dueDate: string): string => {
  try {
    const date = new Date(dueDate)
    if (isPast(date) && !isToday(date)) {
      return 'task-card__due-date--overdue'
    }
    if (isToday(date)) {
      return 'task-card__due-date--today'
    }
    return ''
  } catch {
    return ''
  }
}

/**
 * 获取进度条颜色
 */
const getProgressColor = (progress: number): string => {
  if (progress < 30) return '#ff4d4f'
  if (progress < 70) return '#faad14'
  return '#52c41a'
}
</script>

<style lang="scss" scoped>
.task-card {
  padding: 12px;
  background: white;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    border-color: #1890ff;
  }

  &__header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 8px;
    gap: 8px;
  }

  &__title {
    flex: 1;
    margin: 0;
    font-size: 14px;
    font-weight: 500;
    color: #262626;
    line-height: 1.5;
    word-break: break-word;
  }

  &__description {
    margin: 0 0 12px;
    font-size: 12px;
    color: #8c8c8c;
    line-height: 1.5;
    word-break: break-word;
  }

  &__meta {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 8px;
  }

  &__assignee {
    display: flex;
    align-items: center;
    gap: 6px;

    &-name {
      font-size: 12px;
      color: #595959;
    }
  }

  &__due-date {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    color: #595959;

    &--overdue {
      color: #ff4d4f;
    }

    &--today {
      color: #faad14;
    }
  }

  &__stats {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  &__stat {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    color: #8c8c8c;
  }

  &__progress {
    flex: 1;
    min-width: 60px;
  }
}
</style>

