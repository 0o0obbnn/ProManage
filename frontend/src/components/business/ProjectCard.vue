<template>
  <a-card
    :class="['project-card', { 'project-card--hover': hoverable }]"
    :hoverable="hoverable"
    @click="handleClick"
  >
    <!-- 项目头部 -->
    <template #title>
      <div class="project-card__header">
        <div class="project-card__icon" :style="{ backgroundColor: project.color || '#1890ff' }">
          <component :is="iconComponent" v-if="iconComponent" />
          <FolderOutlined v-else />
        </div>
        <div class="project-card__title">
          <a-tooltip :title="project.name">
            <h3 class="project-card__name">{{ project.name }}</h3>
          </a-tooltip>
          <span class="project-card__code">{{ project.code }}</span>
        </div>
      </div>
    </template>

    <!-- 项目操作 -->
    <template #extra>
      <a-dropdown :trigger="['click']" @click.stop>
        <a-button type="text" size="small">
          <MoreOutlined />
        </a-button>
        <template #overlay>
          <a-menu @click="handleMenuClick">
            <a-menu-item key="edit">
              <EditOutlined />
              编辑
            </a-menu-item>
            <a-menu-item key="archive" v-if="project.status !== 3">
              <InboxOutlined />
              归档
            </a-menu-item>
            <a-menu-item key="restore" v-if="project.status === 3">
              <RollbackOutlined />
              恢复
            </a-menu-item>
            <a-menu-divider />
            <a-menu-item key="delete" danger>
              <DeleteOutlined />
              删除
            </a-menu-item>
          </a-menu>
        </template>
      </a-dropdown>
    </template>

    <!-- 项目内容 -->
    <div class="project-card__content">
      <!-- 项目描述 -->
      <p class="project-card__description">
        {{ project.description || '暂无描述' }}
      </p>

      <!-- 项目状态和优先级 -->
      <div class="project-card__tags">
        <a-tag :color="getStatusColor(project.status)">
          {{ getStatusText(project.status) }}
        </a-tag>
        <a-tag v-if="project.priority !== undefined" :color="getPriorityColor(project.priority)">
          {{ getPriorityText(project.priority) }}
        </a-tag>
        <a-tag v-if="project.type">
          {{ project.type }}
        </a-tag>
      </div>

      <!-- 项目进度 -->
      <div class="project-card__progress">
        <div class="project-card__progress-label">
          <span>进度</span>
          <span class="project-card__progress-value">{{ project.progress }}%</span>
        </div>
        <a-progress
          :percent="project.progress"
          :stroke-color="getProgressColor(project.progress)"
          :show-info="false"
          size="small"
        />
      </div>

      <!-- 项目统计 -->
      <div class="project-card__stats">
        <div class="project-card__stat">
          <TeamOutlined />
          <span>{{ project.memberCount || 0 }} 成员</span>
        </div>
        <div class="project-card__stat">
          <CheckCircleOutlined />
          <span>{{ project.taskCount || 0 }} 任务</span>
        </div>
        <div class="project-card__stat">
          <FileTextOutlined />
          <span>{{ project.documentCount || 0 }} 文档</span>
        </div>
      </div>

      <!-- 项目时间 -->
      <div class="project-card__footer">
        <div class="project-card__owner">
          <a-avatar :size="24" :src="ownerAvatar">
            {{ project.ownerName?.charAt(0) || 'U' }}
          </a-avatar>
          <span>{{ project.ownerName || '未知' }}</span>
        </div>
        <div class="project-card__time">
          <ClockCircleOutlined />
          <span>{{ formatDate(project.updatedAt) }}</span>
        </div>
      </div>
    </div>
  </a-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  FolderOutlined,
  MoreOutlined,
  EditOutlined,
  DeleteOutlined,
  InboxOutlined,
  RollbackOutlined,
  TeamOutlined,
  CheckCircleOutlined,
  FileTextOutlined,
  ClockCircleOutlined
} from '@ant-design/icons-vue'
import type { Project, ProjectStatus, ProjectPriority } from '@/types/project'
import { formatDistanceToNow } from 'date-fns'
import { zhCN } from 'date-fns/locale'

/**
 * 组件属性
 */
interface Props {
  /** 项目数据 */
  project: Project
  /** 是否可悬停 */
  hoverable?: boolean
  /** 所有者头像 */
  ownerAvatar?: string
}

const props = withDefaults(defineProps<Props>(), {
  hoverable: true,
  ownerAvatar: ''
})

/**
 * 组件事件
 */
interface Emits {
  (e: 'click', project: Project): void
  (e: 'edit', project: Project): void
  (e: 'delete', project: Project): void
  (e: 'archive', project: Project): void
  (e: 'restore', project: Project): void
}

const emit = defineEmits<Emits>()

/**
 * 图标组件
 */
const iconComponent = computed(() => {
  // 可以根据项目类型返回不同的图标
  return null
})

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
const formatDate = (date: string): string => {
  try {
    return formatDistanceToNow(new Date(date), {
      addSuffix: true,
      locale: zhCN
    })
  } catch {
    return date
  }
}

/**
 * 处理卡片点击
 */
const handleClick = () => {
  emit('click', props.project)
}

/**
 * 处理菜单点击
 */
const handleMenuClick = ({ key }: { key: string }) => {
  switch (key) {
    case 'edit':
      emit('edit', props.project)
      break
    case 'delete':
      emit('delete', props.project)
      break
    case 'archive':
      emit('archive', props.project)
      break
    case 'restore':
      emit('restore', props.project)
      break
  }
}
</script>

<style lang="scss" scoped>
.project-card {
  height: 100%;
  transition: all 0.3s ease;

  &--hover:hover {
    transform: translateY(-4px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }

  &__header {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  &__icon {
    width: 40px;
    height: 40px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 20px;
    flex-shrink: 0;
  }

  &__title {
    flex: 1;
    min-width: 0;
  }

  &__name {
    margin: 0;
    font-size: 16px;
    font-weight: 600;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__code {
    font-size: 12px;
    color: #8c8c8c;
  }

  &__content {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  &__description {
    margin: 0;
    color: #595959;
    font-size: 14px;
    line-height: 1.6;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    min-height: 44px;
  }

  &__tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }

  &__progress {
    &-label {
      display: flex;
      justify-content: space-between;
      margin-bottom: 8px;
      font-size: 14px;
    }

    &-value {
      font-weight: 600;
      color: #1890ff;
    }
  }

  &__stats {
    display: flex;
    gap: 16px;
    padding: 12px 0;
    border-top: 1px solid #f0f0f0;
    border-bottom: 1px solid #f0f0f0;
  }

  &__stat {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 14px;
    color: #595959;

    .anticon {
      color: #8c8c8c;
    }
  }

  &__footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  &__owner {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    color: #595959;
  }

  &__time {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 12px;
    color: #8c8c8c;
  }
}
</style>

