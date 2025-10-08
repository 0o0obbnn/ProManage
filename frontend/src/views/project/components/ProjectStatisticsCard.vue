<template>
  <a-card class="project-statistics-card" title="项目统计">
    <!-- 加载状态 -->
    <div v-if="loading" class="project-statistics-card__loading">
      <a-spin />
    </div>

    <!-- 统计数据 -->
    <div v-else-if="statistics" class="project-statistics-card__content">
      <!-- 任务统计 -->
      <div class="project-statistics-card__section">
        <h4 class="project-statistics-card__section-title">
          <CheckCircleOutlined />
          任务统计
        </h4>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-statistic
              title="总任务数"
              :value="statistics.totalTasks"
              :value-style="{ color: '#1890ff' }"
            >
              <template #prefix>
                <FileTextOutlined />
              </template>
            </a-statistic>
          </a-col>
          <a-col :span="12">
            <a-statistic
              title="已完成"
              :value="statistics.completedTasks"
              :value-style="{ color: '#52c41a' }"
            >
              <template #prefix>
                <CheckOutlined />
              </template>
            </a-statistic>
          </a-col>
        </a-row>
        <a-row :gutter="16" style="margin-top: 16px">
          <a-col :span="12">
            <a-statistic
              title="进行中"
              :value="statistics.inProgressTasks"
              :value-style="{ color: '#faad14' }"
            >
              <template #prefix>
                <SyncOutlined :spin="true" />
              </template>
            </a-statistic>
          </a-col>
          <a-col :span="12">
            <a-statistic
              title="待办"
              :value="statistics.todoTasks"
              :value-style="{ color: '#8c8c8c' }"
            >
              <template #prefix>
                <ClockCircleOutlined />
              </template>
            </a-statistic>
          </a-col>
        </a-row>
        <div class="project-statistics-card__progress">
          <div class="project-statistics-card__progress-label">
            <span>完成率</span>
            <span class="project-statistics-card__progress-value">
              {{ completionRate }}%
            </span>
          </div>
          <a-progress
            :percent="completionRate"
            :stroke-color="getProgressColor(completionRate)"
            :show-info="false"
          />
        </div>
      </div>

      <a-divider />

      <!-- 文档统计 -->
      <div class="project-statistics-card__section">
        <h4 class="project-statistics-card__section-title">
          <FolderOutlined />
          文档统计
        </h4>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-statistic
              title="总文档数"
              :value="statistics.totalDocuments"
              :value-style="{ color: '#1890ff' }"
            >
              <template #prefix>
                <FileOutlined />
              </template>
            </a-statistic>
          </a-col>
          <a-col :span="12">
            <a-statistic
              title="最近更新"
              :value="statistics.recentDocuments"
              :value-style="{ color: '#52c41a' }"
            >
              <template #prefix>
                <EditOutlined />
              </template>
            </a-statistic>
          </a-col>
        </a-row>
      </div>

      <a-divider />

      <!-- 成员统计 -->
      <div class="project-statistics-card__section">
        <h4 class="project-statistics-card__section-title">
          <TeamOutlined />
          成员统计
        </h4>
        <a-statistic
          title="团队成员"
          :value="statistics.totalMembers"
          :value-style="{ color: '#1890ff' }"
        >
          <template #prefix>
            <UserOutlined />
          </template>
        </a-statistic>
      </div>
    </div>

    <!-- 空状态 -->
    <a-empty
      v-else
      description="暂无统计数据"
      :image="Empty.PRESENTED_IMAGE_SIMPLE"
    />
  </a-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Empty } from 'ant-design-vue'
import {
  CheckCircleOutlined,
  FileTextOutlined,
  CheckOutlined,
  SyncOutlined,
  ClockCircleOutlined,
  FolderOutlined,
  FileOutlined,
  EditOutlined,
  TeamOutlined,
  UserOutlined
} from '@ant-design/icons-vue'
import type { ProjectStatistics } from '@/types/project'

/**
 * 组件属性
 */
interface Props {
  statistics?: ProjectStatistics | null
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  statistics: null,
  loading: false
})

/**
 * 计算完成率
 */
const completionRate = computed(() => {
  if (!props.statistics || props.statistics.totalTasks === 0) {
    return 0
  }
  return Math.round((props.statistics.completedTasks / props.statistics.totalTasks) * 100)
})

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
.project-statistics-card {
  margin-bottom: 24px;

  &__loading {
    display: flex;
    justify-content: center;
    padding: 40px 0;
  }

  &__content {
    padding: 8px 0;
  }

  &__section {
    &-title {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 16px;
      font-size: 14px;
      font-weight: 600;
      color: #262626;

      .anticon {
        color: #1890ff;
      }
    }
  }

  &__progress {
    margin-top: 16px;

    &-label {
      display: flex;
      justify-content: space-between;
      margin-bottom: 8px;
      font-size: 14px;
      color: #595959;
    }

    &-value {
      font-weight: 600;
      color: #262626;
    }
  }
}
</style>

