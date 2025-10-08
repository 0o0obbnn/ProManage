<template>
  <div class="dashboard-enhanced">
    <!-- 页面头部 -->
    <div class="dashboard-header">
      <h2>工作台</h2>
      <a-space>
        <a-button @click="handleRefresh" :loading="refreshing">
          <template #icon><ReloadOutlined /></template>
          刷新
        </a-button>
      </a-space>
    </div>

    <!-- 统计卡片 -->
    <StatsCards :stats="dashboardStats" :loading="statsLoading" style="margin-bottom: 16px" />

    <!-- 图表区域 -->
    <a-row :gutter="16" style="margin-bottom: 16px">
      <a-col :xs="24" :lg="12">
        <ProjectProgressChart
          :data="projectProgress"
          :loading="chartLoading"
          @view-all="handleViewProjects"
        />
      </a-col>

      <a-col :xs="24" :lg="12">
        <TeamActivityChart
          :data="teamActivity"
          :loading="chartLoading"
          @days-change="handleTeamActivityDaysChange"
        />
      </a-col>
    </a-row>

    <!-- 最近活动 -->
    <a-row :gutter="16">
      <a-col :xs="24">
        <RecentActivities
          :activities="recentActivities"
          :loading="activityLoading"
          :has-more="hasMoreActivities"
          @refresh="handleRefreshActivities"
          @load-more="handleLoadMoreActivities"
        />
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { ReloadOutlined } from '@ant-design/icons-vue'
import { dashboardApi } from '@/api/modules/dashboard'
import type { DashboardStats, ProjectProgress, TeamActivity, Activity } from '@/api/modules/dashboard'
import StatsCards from './components/StatsCards.vue'
import ProjectProgressChart from './components/ProjectProgressChart.vue'
import TeamActivityChart from './components/TeamActivityChart.vue'
import RecentActivities from './components/RecentActivities.vue'

/**
 * 组件状态
 */
const router = useRouter()
const refreshing = ref(false)
const statsLoading = ref(false)
const chartLoading = ref(false)
const activityLoading = ref(false)

// 数据
const dashboardStats = ref<DashboardStats>({
  taskStats: { total: 0, completed: 0, inProgress: 0, pending: 0 },
  projectStats: { total: 0, active: 0, completed: 0, archived: 0 },
  changeStats: { total: 0, pending: 0, approved: 0, rejected: 0 },
  testStats: { total: 0, pending: 0, passed: 0, failed: 0 }
})

const projectProgress = ref<ProjectProgress[]>([])
const teamActivity = ref<TeamActivity[]>([])
const recentActivities = ref<Activity[]>([])
const hasMoreActivities = ref(false)
const activityPage = ref(1)
const activityPageSize = ref(20)

// 自动刷新定时器
let autoRefreshTimer: any = null

/**
 * 获取Dashboard统计数据
 */
const fetchDashboardStats = async () => {
  statsLoading.value = true
  try {
    const res = await dashboardApi.getDashboardStats()
    dashboardStats.value = res.data
  } catch (error) {
    console.error('获取统计数据失败:', error)
    message.error('获取统计数据失败')
  } finally {
    statsLoading.value = false
  }
}

/**
 * 获取项目进度数据
 */
const fetchProjectProgress = async () => {
  chartLoading.value = true
  try {
    const res = await dashboardApi.getProjectProgress()
    projectProgress.value = res.data
  } catch (error) {
    console.error('获取项目进度失败:', error)
    message.error('获取项目进度失败')
  } finally {
    chartLoading.value = false
  }
}

/**
 * 获取团队活跃度数据
 */
const fetchTeamActivity = async (days: number = 30) => {
  chartLoading.value = true
  try {
    const res = await dashboardApi.getTeamActivity(days)
    teamActivity.value = res.data
  } catch (error) {
    console.error('获取团队活跃度失败:', error)
    message.error('获取团队活跃度失败')
  } finally {
    chartLoading.value = false
  }
}

/**
 * 获取最近活动
 */
const fetchRecentActivities = async (append = false) => {
  activityLoading.value = true
  try {
    const limit = activityPage.value * activityPageSize.value
    const res = await dashboardApi.getRecentActivities(limit)
    
    if (append) {
      recentActivities.value = [...recentActivities.value, ...res.data]
    } else {
      recentActivities.value = res.data
    }

    hasMoreActivities.value = res.data.length >= limit
  } catch (error) {
    console.error('获取最近活动失败:', error)
    message.error('获取最近活动失败')
  } finally {
    activityLoading.value = false
  }
}

/**
 * 刷新所有数据
 */
const handleRefresh = async () => {
  refreshing.value = true
  try {
    await Promise.all([
      fetchDashboardStats(),
      fetchProjectProgress(),
      fetchTeamActivity(),
      fetchRecentActivities()
    ])
    message.success('刷新成功')
  } catch (error) {
    message.error('刷新失败')
  } finally {
    refreshing.value = false
  }
}

/**
 * 查看项目列表
 */
const handleViewProjects = () => {
  router.push('/projects')
}

/**
 * 处理团队活跃度天数变化
 */
const handleTeamActivityDaysChange = (days: number) => {
  fetchTeamActivity(days)
}

/**
 * 刷新活动
 */
const handleRefreshActivities = () => {
  activityPage.value = 1
  fetchRecentActivities()
}

/**
 * 加载更多活动
 */
const handleLoadMoreActivities = () => {
  activityPage.value++
  fetchRecentActivities(true)
}

/**
 * 启动自动刷新
 */
const startAutoRefresh = () => {
  // 每5分钟自动刷新
  autoRefreshTimer = setInterval(() => {
    handleRefresh()
  }, 5 * 60 * 1000)
}

/**
 * 停止自动刷新
 */
const stopAutoRefresh = () => {
  if (autoRefreshTimer) {
    clearInterval(autoRefreshTimer)
    autoRefreshTimer = null
  }
}

/**
 * 组件挂载
 */
onMounted(() => {
  // 初始加载数据
  handleRefresh()
  
  // 启动自动刷新
  startAutoRefresh()
})

/**
 * 组件卸载
 */
onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<style scoped lang="scss">
.dashboard-enhanced {
  padding: 24px;

  .dashboard-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;

    h2 {
      margin: 0;
      font-size: 24px;
      font-weight: 600;
    }
  }
}
</style>

