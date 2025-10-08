<template>
  <a-card class="project-info-card" title="项目信息">
    <template #extra>
      <a-button type="link" size="small" @click="emit('edit')">
        <EditOutlined />
        编辑
      </a-button>
    </template>

    <a-descriptions :column="{ xs: 1, sm: 2, md: 2, lg: 2 }" bordered>
      <!-- 项目名称 -->
      <a-descriptions-item label="项目名称">
        {{ project.name }}
      </a-descriptions-item>

      <!-- 项目编码 -->
      <a-descriptions-item label="项目编码">
        <a-tag color="blue">{{ project.code }}</a-tag>
      </a-descriptions-item>

      <!-- 项目状态 -->
      <a-descriptions-item label="项目状态">
        <a-tag :color="getStatusColor(project.status)">
          {{ getStatusText(project.status) }}
        </a-tag>
      </a-descriptions-item>

      <!-- 项目优先级 -->
      <a-descriptions-item label="优先级">
        <a-tag v-if="project.priority !== undefined" :color="getPriorityColor(project.priority)">
          {{ getPriorityText(project.priority) }}
        </a-tag>
        <span v-else>-</span>
      </a-descriptions-item>

      <!-- 项目类型 -->
      <a-descriptions-item label="项目类型">
        <a-tag v-if="project.type">{{ getTypeText(project.type) }}</a-tag>
        <span v-else>-</span>
      </a-descriptions-item>

      <!-- 项目所有者 -->
      <a-descriptions-item label="项目所有者">
        <a-space>
          <a-avatar :size="24">
            {{ project.ownerName?.charAt(0) || 'U' }}
          </a-avatar>
          <span>{{ project.ownerName || '未知' }}</span>
        </a-space>
      </a-descriptions-item>

      <!-- 项目进度 -->
      <a-descriptions-item label="项目进度" :span="2">
        <div class="project-info-card__progress">
          <a-progress
            :percent="project.progress"
            :stroke-color="getProgressColor(project.progress)"
          />
        </div>
      </a-descriptions-item>

      <!-- 计划开始日期 -->
      <a-descriptions-item label="计划开始">
        {{ formatDate(project.startDate) || '-' }}
      </a-descriptions-item>

      <!-- 计划结束日期 -->
      <a-descriptions-item label="计划结束">
        {{ formatDate(project.endDate) || '-' }}
      </a-descriptions-item>

      <!-- 实际开始日期 -->
      <a-descriptions-item label="实际开始">
        {{ formatDate(project.actualStartDate) || '-' }}
      </a-descriptions-item>

      <!-- 实际结束日期 -->
      <a-descriptions-item label="实际结束">
        {{ formatDate(project.actualEndDate) || '-' }}
      </a-descriptions-item>

      <!-- 创建时间 -->
      <a-descriptions-item label="创建时间">
        {{ formatDateTime(project.createdAt) }}
      </a-descriptions-item>

      <!-- 更新时间 -->
      <a-descriptions-item label="更新时间">
        {{ formatDateTime(project.updatedAt) }}
      </a-descriptions-item>

      <!-- 项目描述 -->
      <a-descriptions-item label="项目描述" :span="2">
        <div class="project-info-card__description">
          {{ project.description || '暂无描述' }}
        </div>
      </a-descriptions-item>
    </a-descriptions>
  </a-card>
</template>

<script setup lang="ts">
import { EditOutlined } from '@ant-design/icons-vue'
import type { ProjectDetail, ProjectStatus, ProjectPriority, ProjectType } from '@/types/project'
import { format } from 'date-fns'

/**
 * 组件属性
 */
interface Props {
  project: ProjectDetail
}

const props = defineProps<Props>()

/**
 * 组件事件
 */
interface Emits {
  (e: 'edit'): void
}

const emit = defineEmits<Emits>()

/**
 * 获取状态颜色
 */
const getStatusColor = (status: ProjectStatus): string => {
  const colorMap: Record<ProjectStatus, string> = {
    0: 'default', // 规划中
    1: 'processing', // 进行中
    2: 'success', // 已完成
    3: 'warning', // 已归档
    4: 'error' // 已暂停
  }
  return colorMap[status] || 'default'
}

/**
 * 获取状态文本
 */
const getStatusText = (status: ProjectStatus): string => {
  const textMap: Record<ProjectStatus, string> = {
    0: '规划中',
    1: '进行中',
    2: '已完成',
    3: '已归档',
    4: '已暂停'
  }
  return textMap[status] || '未知'
}

/**
 * 获取优先级颜色
 */
const getPriorityColor = (priority: ProjectPriority): string => {
  const colorMap: Record<ProjectPriority, string> = {
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
const getPriorityText = (priority: ProjectPriority): string => {
  const textMap: Record<ProjectPriority, string> = {
    0: '低优先级',
    1: '中优先级',
    2: '高优先级',
    3: '紧急'
  }
  return textMap[priority] || '未知'
}

/**
 * 获取类型文本
 */
const getTypeText = (type: ProjectType): string => {
  const textMap: Record<ProjectType, string> = {
    WEB: 'Web项目',
    APP: '移动应用',
    SYSTEM: '系统软件',
    OTHER: '其他'
  }
  return textMap[type] || type
}

/**
 * 获取进度条颜色
 */
const getProgressColor = (progress: number): string => {
  if (progress < 30) return '#ff4d4f'
  if (progress < 70) return '#faad14'
  return '#52c41a'
}

/**
 * 格式化日期
 */
const formatDate = (date?: string): string => {
  if (!date) return ''
  try {
    return format(new Date(date), 'yyyy-MM-dd')
  } catch {
    return date
  }
}

/**
 * 格式化日期时间
 */
const formatDateTime = (date: string): string => {
  try {
    return format(new Date(date), 'yyyy-MM-dd HH:mm:ss')
  } catch {
    return date
  }
}
</script>

<style lang="scss" scoped>
.project-info-card {
  margin-bottom: 24px;

  &__progress {
    width: 100%;
  }

  &__description {
    line-height: 1.6;
    white-space: pre-wrap;
    word-break: break-word;
  }
}
</style>

