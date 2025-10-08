<template>
  <a-card title="项目统计" class="project-charts-card">
    <a-spin :spinning="loading">
      <!-- 统计概览 -->
      <a-row :gutter="16" class="project-charts-card__overview">
        <a-col :xs="12" :sm="6">
          <a-statistic
            title="总任务数"
            :value="statistics?.totalTasks || 0"
            :value-style="{ color: '#1890ff' }"
          >
            <template #prefix>
              <CheckCircleOutlined />
            </template>
          </a-statistic>
        </a-col>
        <a-col :xs="12" :sm="6">
          <a-statistic
            title="已完成"
            :value="statistics?.completedTasks || 0"
            :value-style="{ color: '#52c41a' }"
          >
            <template #prefix>
              <CheckOutlined />
            </template>
          </a-statistic>
        </a-col>
        <a-col :xs="12" :sm="6">
          <a-statistic
            title="进行中"
            :value="statistics?.inProgressTasks || 0"
            :value-style="{ color: '#faad14' }"
          >
            <template #prefix>
              <SyncOutlined :spin="true" />
            </template>
          </a-statistic>
        </a-col>
        <a-col :xs="12" :sm="6">
          <a-statistic
            title="完成率"
            :value="completionRate"
            suffix="%"
            :value-style="{ color: completionRate >= 70 ? '#52c41a' : '#faad14' }"
          >
            <template #prefix>
              <RiseOutlined />
            </template>
          </a-statistic>
        </a-col>
      </a-row>

      <a-divider />

      <!-- 图表展示 -->
      <a-row :gutter="16">
        <!-- 任务进度图 -->
        <a-col :xs="24" :lg="12">
          <TaskProgressChart
            v-if="statistics"
            :total-tasks="statistics.totalTasks"
            :completed-tasks="statistics.completedTasks"
            :in-progress-tasks="statistics.inProgressTasks"
          />
        </a-col>

        <!-- 成员贡献图 -->
        <a-col :xs="24" :lg="12">
          <MemberContributionChart
            v-if="memberContributions.length > 0"
            :data="memberContributions"
          />
          <a-empty
            v-else
            description="暂无成员贡献数据"
            :image="Empty.PRESENTED_IMAGE_SIMPLE"
          />
        </a-col>
      </a-row>

      <!-- 其他统计信息 -->
      <a-divider />
      <a-row :gutter="16">
        <a-col :xs="12" :sm="8">
          <a-statistic
            title="文档数"
            :value="statistics?.totalDocuments || 0"
          >
            <template #prefix>
              <FileTextOutlined />
            </template>
          </a-statistic>
        </a-col>
        <a-col :xs="12" :sm="8">
          <a-statistic
            title="成员数"
            :value="statistics?.totalMembers || 0"
          >
            <template #prefix>
              <TeamOutlined />
            </template>
          </a-statistic>
        </a-col>
        <a-col :xs="24" :sm="8">
          <a-statistic
            title="平均任务完成率"
            :value="avgTaskCompletionRate"
            suffix="%"
          >
            <template #prefix>
              <BarChartOutlined />
            </template>
          </a-statistic>
        </a-col>
      </a-row>
    </a-spin>
  </a-card>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Empty } from 'ant-design-vue'
import {
  CheckCircleOutlined,
  CheckOutlined,
  SyncOutlined,
  RiseOutlined,
  FileTextOutlined,
  TeamOutlined,
  BarChartOutlined
} from '@ant-design/icons-vue'
import type { ProjectStatistics } from '@/types/project'
import { getProjectStatistics } from '@/api/modules/project'
import TaskProgressChart from '@/components/charts/TaskProgressChart.vue'
import MemberContributionChart from '@/components/charts/MemberContributionChart.vue'

/**
 * 组件属性
 */
interface Props {
  projectId: number
}

const props = defineProps<Props>()

// 响应式数据
const loading = ref(false)
const statistics = ref<ProjectStatistics | null>(null)

/**
 * 成员贡献数据(模拟数据,实际应从API获取)
 */
const memberContributions = ref([
  { name: '张三', taskCount: 15, completedCount: 12 },
  { name: '李四', taskCount: 12, completedCount: 10 },
  { name: '王五', taskCount: 10, completedCount: 8 },
  { name: '赵六', taskCount: 8, completedCount: 7 },
  { name: '钱七', taskCount: 6, completedCount: 5 }
])

/**
 * 计算完成率
 */
const completionRate = computed(() => {
  if (!statistics.value || statistics.value.totalTasks === 0) {
    return 0
  }
  return Math.round((statistics.value.completedTasks / statistics.value.totalTasks) * 100)
})

/**
 * 计算平均任务完成率
 */
const avgTaskCompletionRate = computed(() => {
  if (memberContributions.value.length === 0) {
    return 0
  }
  const totalRate = memberContributions.value.reduce((sum, member) => {
    const rate = member.taskCount > 0 ? (member.completedCount / member.taskCount) * 100 : 0
    return sum + rate
  }, 0)
  return Math.round(totalRate / memberContributions.value.length)
})

/**
 * 加载统计数据
 */
const loadStatistics = async () => {
  try {
    loading.value = true
    statistics.value = await getProjectStatistics(props.projectId)
  } catch (error) {
    console.error('Load statistics failed:', error)
  } finally {
    loading.value = false
  }
}

// 生命周期
onMounted(() => {
  loadStatistics()
})

// 暴露方法供父组件调用
defineExpose({
  loadStatistics
})
</script>

<style lang="scss" scoped>
.project-charts-card {
  margin-bottom: 24px;

  &__overview {
    margin-bottom: 16px;
  }

  :deep(.ant-statistic-title) {
    font-size: 14px;
    color: #8c8c8c;
  }

  :deep(.ant-statistic-content) {
    font-size: 24px;
    font-weight: 600;
  }
}
</style>

