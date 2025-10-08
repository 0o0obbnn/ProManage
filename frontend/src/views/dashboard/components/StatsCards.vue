<template>
  <a-row :gutter="16">
    <a-col :xs="24" :sm="12" :md="6">
      <a-card :loading="loading" hoverable>
        <a-statistic
          title="我的任务"
          :value="stats.taskStats.total"
          :value-style="{ color: '#3f8600' }"
        >
          <template #prefix>
            <CheckSquareOutlined />
          </template>
        </a-statistic>
        <div class="stat-detail">
          <span class="detail-item">
            <span class="label">进行中:</span>
            <span class="value">{{ stats.taskStats.inProgress }}</span>
          </span>
          <span class="detail-item">
            <span class="label">已完成:</span>
            <span class="value">{{ stats.taskStats.completed }}</span>
          </span>
        </div>
      </a-card>
    </a-col>

    <a-col :xs="24" :sm="12" :md="6">
      <a-card :loading="loading" hoverable>
        <a-statistic
          title="进行中项目"
          :value="stats.projectStats.active"
          :value-style="{ color: '#1890ff' }"
        >
          <template #prefix>
            <ProjectOutlined />
          </template>
        </a-statistic>
        <div class="stat-detail">
          <span class="detail-item">
            <span class="label">总项目:</span>
            <span class="value">{{ stats.projectStats.total }}</span>
          </span>
          <span class="detail-item">
            <span class="label">已完成:</span>
            <span class="value">{{ stats.projectStats.completed }}</span>
          </span>
        </div>
      </a-card>
    </a-col>

    <a-col :xs="24" :sm="12" :md="6">
      <a-card :loading="loading" hoverable>
        <a-statistic
          title="待审批变更"
          :value="stats.changeStats.pending"
          :value-style="{ color: '#fa8c16' }"
        >
          <template #prefix>
            <SwapOutlined />
          </template>
        </a-statistic>
        <div class="stat-detail">
          <span class="detail-item">
            <span class="label">已批准:</span>
            <span class="value">{{ stats.changeStats.approved }}</span>
          </span>
          <span class="detail-item">
            <span class="label">已拒绝:</span>
            <span class="value">{{ stats.changeStats.rejected }}</span>
          </span>
        </div>
      </a-card>
    </a-col>

    <a-col :xs="24" :sm="12" :md="6">
      <a-card :loading="loading" hoverable>
        <a-statistic
          title="待测试用例"
          :value="stats.testStats.pending"
          :value-style="{ color: '#722ed1' }"
        >
          <template #prefix>
            <ExperimentOutlined />
          </template>
        </a-statistic>
        <div class="stat-detail">
          <span class="detail-item">
            <span class="label">已通过:</span>
            <span class="value">{{ stats.testStats.passed }}</span>
          </span>
          <span class="detail-item">
            <span class="label">失败:</span>
            <span class="value">{{ stats.testStats.failed }}</span>
          </span>
        </div>
      </a-card>
    </a-col>
  </a-row>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  CheckSquareOutlined,
  ProjectOutlined,
  SwapOutlined,
  ExperimentOutlined
} from '@ant-design/icons-vue'
import type { DashboardStats } from '@/api/modules/dashboard'

/**
 * Props
 */
interface Props {
  stats: DashboardStats
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  loading: false
})
</script>

<style scoped lang="scss">
.stat-detail {
  display: flex;
  justify-content: space-between;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;

  .detail-item {
    display: flex;
    flex-direction: column;
    font-size: 12px;

    .label {
      color: rgba(0, 0, 0, 0.45);
      margin-bottom: 4px;
    }

    .value {
      color: rgba(0, 0, 0, 0.85);
      font-weight: 500;
    }
  }
}

:deep(.ant-card) {
  transition: all 0.3s;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }
}
</style>

