<template>
  <div class="analytics-dashboard">
    <!-- 仪表板头部 -->
    <div class="dashboard-header">
      <h2>数据分析</h2>
      <a-space>
        <a-select
          v-model:value="selectedProjectId"
          style="width: 200px"
          placeholder="选择项目"
          allow-clear
          @change="handleProjectChange"
        >
          <a-select-option value="">全部项目</a-select-option>
          <a-select-option
            v-for="project in mockProjects"
            :key="project.id"
            :value="project.id"
          >
            {{ project.name }}
          </a-select-option>
        </a-select>

        <a-range-picker
          v-model:value="dateRange"
          :presets="datePresets"
          @change="handleDateRangeChange"
        />

        <a-button @click="handleExportReport">
          <template #icon><ExportOutlined /></template>
          导出报告
        </a-button>

        <a-button @click="handleRefreshData" :loading="loading">
          <template #icon><ReloadOutlined /></template>
          刷新
        </a-button>
      </a-space>
    </div>

    <!-- 关键指标卡片 -->
    <a-row :gutter="16" style="margin-bottom: 16px">
      <a-col :xs="24" :sm="12" :md="6">
        <a-card :loading="statsLoading">
          <a-statistic
            title="项目完成率"
            :value="dashboardStats?.completionRate || 0"
            suffix="%"
            :value-style="{ color: '#52c41a' }"
          >
            <template #prefix>
              <RiseOutlined />
            </template>
          </a-statistic>
          <div class="stat-footer">
            <span
              class="trend-value"
              :class="getTrendClass(dashboardStats?.completionRateDelta || 0)"
            >
              {{ getTrendText(dashboardStats?.completionRateDelta || 0) }}
            </span>
            <span class="stat-hint">较上周期</span>
          </div>
        </a-card>
      </a-col>

      <a-col :xs="24" :sm="12" :md="6">
        <a-card :loading="statsLoading">
          <a-statistic
            title="任务按时完成率"
            :value="dashboardStats?.onTimeRate || 0"
            suffix="%"
          >
            <template #prefix>
              <ClockCircleOutlined />
            </template>
          </a-statistic>
          <div class="stat-footer">
            <span
              class="trend-value"
              :class="getTrendClass(dashboardStats?.onTimeRateDelta || 0)"
            >
              {{ getTrendText(dashboardStats?.onTimeRateDelta || 0) }}
            </span>
            <span class="stat-hint">较上周期</span>
          </div>
        </a-card>
      </a-col>

      <a-col :xs="24" :sm="12" :md="6">
        <a-card :loading="statsLoading">
          <a-statistic
            title="团队协作效率"
            :value="dashboardStats?.collaborationScore || 0"
            :value-style="{ color: '#1890ff' }"
          >
            <template #prefix>
              <TeamOutlined />
            </template>
          </a-statistic>
          <div class="stat-footer">
            <span class="stat-hint">目标: 提升50%</span>
          </div>
        </a-card>
      </a-col>

      <a-col :xs="24" :sm="12" :md="6">
        <a-card :loading="statsLoading">
          <a-statistic
            title="测试用例复用率"
            :value="dashboardStats?.testReuseRate || 0"
            suffix="%"
            :value-style="
              (dashboardStats?.testReuseRate || 0) >= 70
                ? { color: '#52c41a' }
                : { color: '#faad14' }
            "
          >
            <template #prefix>
              <ExperimentOutlined />
            </template>
          </a-statistic>
          <div class="stat-footer">
            <span class="stat-hint">目标: 70%+</span>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 图表区域 - 第一行 -->
    <a-row :gutter="16" style="margin-bottom: 16px">
      <a-col :xs="24" :lg="12">
        <a-card title="任务完成趋势" :bordered="false" :loading="chartLoading">
          <template #extra>
            <a-radio-group
              v-model:value="taskTrendPeriod"
              button-style="solid"
              size="small"
              @change="handleTaskTrendPeriodChange"
            >
              <a-radio-button value="week">周</a-radio-button>
              <a-radio-button value="month">月</a-radio-button>
              <a-radio-button value="quarter">季度</a-radio-button>
            </a-radio-group>
          </template>

          <TaskTrendChart
            :data="taskTrendData"
            :period="taskTrendPeriod"
            :height="300"
          />
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="12">
        <a-card title="项目进度概览" :bordered="false" :loading="chartLoading">
          <template #extra>
            <a-button type="link" size="small" @click="handleViewProjectDetails">
              查看详情 →
            </a-button>
          </template>

          <a-list :data-source="projectProgress" size="small">
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta>
                  <template #avatar>
                    <a-avatar
                      :style="{ backgroundColor: item.color }"
                      shape="square"
                    >
                      {{ item.name.charAt(0) }}
                    </a-avatar>
                  </template>
                  <template #title>
                    {{ item.name }}
                  </template>
                  <template #description>
                    <a-progress
                      :percent="item.progress"
                      :status="getProgressStatus(item)"
                      size="small"
                    />
                  </template>
                </a-list-item-meta>
              </a-list-item>
            </template>
          </a-list>
        </a-card>
      </a-col>
    </a-row>

    <!-- 图表区域 - 第二行 -->
    <a-row :gutter="16" style="margin-bottom: 16px">
      <a-col :xs="24" :lg="12">
        <a-card title="团队工作量分布" :bordered="false" :loading="chartLoading">
          <TeamWorkloadChart
            :data="teamWorkload"
            :height="300"
            @member-click="handleMemberClick"
          />
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="12">
        <a-card title="缺陷统计分析" :bordered="false" :loading="chartLoading">
          <template #extra>
            <a-select
              v-model:value="bugChartType"
              size="small"
              style="width: 120px"
              @change="handleBugChartTypeChange"
            >
              <a-select-option value="severity">按严重程度</a-select-option>
              <a-select-option value="module">按模块</a-select-option>
              <a-select-option value="status">按状态</a-select-option>
            </a-select>
          </template>

          <BugAnalysisChart
            :data="bugAnalysis"
            :type="bugChartType"
            :height="300"
          />
        </a-card>
      </a-col>
    </a-row>

    <!-- 图表区域 - 第三行 -->
    <a-row :gutter="16" style="margin-bottom: 16px">
      <a-col :xs="24" :lg="12">
        <a-card title="文档活跃度" :bordered="false" :loading="chartLoading">
          <DocumentActivityChart :data="documentActivity" :height="300" />

          <a-divider />

          <a-row :gutter="16">
            <a-col :span="8">
              <a-statistic
                title="总文档数"
                :value="documentActivity?.stats.total || 0"
              >
                <template #prefix>
                  <FileTextOutlined />
                </template>
              </a-statistic>
            </a-col>
            <a-col :span="8">
              <a-statistic
                title="今日更新"
                :value="documentActivity?.stats.todayUpdated || 0"
              >
                <template #prefix>
                  <EditOutlined />
                </template>
              </a-statistic>
            </a-col>
            <a-col :span="8">
              <a-statistic
                title="平均查看次数"
                :value="documentActivity?.stats.avgViews || 0"
              >
                <template #prefix>
                  <EyeOutlined />
                </template>
              </a-statistic>
            </a-col>
          </a-row>
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="12">
        <a-card title="变更影响分析" :bordered="false" :loading="chartLoading">
          <ChangeImpactChart :data="changeImpactData" :height="300" />

          <a-divider />

          <a-descriptions :column="2" size="small">
            <a-descriptions-item label="本周变更">
              {{ changeImpactData?.stats.weeklyChanges || 0 }}
            </a-descriptions-item>
            <a-descriptions-item label="自动通知率">
              {{ changeImpactData?.stats.autoNotifyRate || 0 }}%
            </a-descriptions-item>
            <a-descriptions-item label="平均响应时间">
              {{ changeImpactData?.stats.avgResponseTime || 0 }}h
            </a-descriptions-item>
            <a-descriptions-item label="重大变更">
              {{ changeImpactData?.stats.criticalChanges || 0 }}
            </a-descriptions-item>
          </a-descriptions>
        </a-card>
      </a-col>
    </a-row>

    <!-- 详细数据表格 -->
    <a-card title="详细数据" :bordered="false" style="margin-bottom: 16px">
      <a-tabs v-model:activeKey="detailTab">
        <a-tab-pane key="tasks" tab="任务明细">
          <TaskDetailTable
            :project-id="selectedProjectId"
            :date-range="getDateRangeStrings()"
          />
        </a-tab-pane>

        <a-tab-pane key="members" tab="成员贡献">
          <MemberContributionTable
            :project-id="selectedProjectId"
            :date-range="getDateRangeStrings()"
          />
        </a-tab-pane>

        <a-tab-pane key="quality" tab="质量指标">
          <QualityMetricsTable
            :project-id="selectedProjectId"
            :date-range="getDateRangeStrings()"
          />
        </a-tab-pane>

        <a-tab-pane key="timeline" tab="时间线分析">
          <TimelineAnalysisChart
            :project-id="selectedProjectId"
            :date-range="getDateRangeStrings()"
          />
        </a-tab-pane>
      </a-tabs>
    </a-card>

    <!-- 自定义报告 -->
    <a-card title="自定义报告" :bordered="false">
      <a-space style="margin-bottom: 16px">
        <a-button type="primary" @click="showCreateReportModal">
          <template #icon><PlusOutlined /></template>
          创建报告
        </a-button>
        <a-button @click="showScheduleReportDrawer">
          <template #icon><ClockCircleOutlined /></template>
          定时报告
        </a-button>
      </a-space>

      <a-list
        :data-source="customReports"
        :grid="{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 4 }"
        :loading="reportLoading"
      >
        <template #renderItem="{ item }">
          <a-list-item>
            <a-card hoverable size="small">
              <a-card-meta
                :title="item.name"
                :description="item.description"
              >
                <template #avatar>
                  <FileTextOutlined style="font-size: 24px; color: #1890ff" />
                </template>
              </a-card-meta>

              <template #actions>
                <a @click="handleViewReport(item)">
                  <EyeOutlined /> 查看
                </a>
                <a @click="handleEditReport(item)">
                  <EditOutlined /> 编辑
                </a>
                <a @click="handleExportSingleReport(item)">
                  <ExportOutlined /> 导出
                </a>
              </template>
            </a-card>
          </a-list-item>
        </template>
      </a-list>
    </a-card>

    <!-- 创建报告弹窗 -->
    <CreateReportModal
      v-model:open="createReportModalVisible"
      :projects="mockProjects"
      @success="handleReportCreated"
    />

    <!-- 定时报告配置抽屉 -->
    <ReportConfigDrawer
      v-model:open="scheduleReportDrawerVisible"
      :report-id="currentReportId"
      :users="mockUsers"
      @success="handleScheduleConfigured"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAnalyticsStore } from '@/stores/modules/analytics'
import { TimeRange } from '@/types/analytics'
import type { ProjectProgress, CustomReport } from '@/types/analytics'
import {
  RiseOutlined,
  ClockCircleOutlined,
  TeamOutlined,
  ExperimentOutlined,
  ExportOutlined,
  ReloadOutlined,
  PlusOutlined,
  FileTextOutlined,
  EditOutlined,
  EyeOutlined
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import dayjs, { type Dayjs } from 'dayjs'

import TaskTrendChart from '@/components/charts/TaskTrendChart.vue'
import TeamWorkloadChart from '@/components/charts/TeamWorkloadChart.vue'
import BugAnalysisChart from '@/components/charts/BugAnalysisChart.vue'
import DocumentActivityChart from '@/components/charts/DocumentActivityChart.vue'
import ChangeImpactChart from '@/components/charts/ChangeImpactChart.vue'
import TaskDetailTable from '@/components/analytics/TaskDetailTable.vue'
import MemberContributionTable from '@/components/analytics/MemberContributionTable.vue'
import QualityMetricsTable from '@/components/analytics/QualityMetricsTable.vue'
import TimelineAnalysisChart from '@/components/analytics/TimelineAnalysisChart.vue'
import CreateReportModal from '@/components/analytics/CreateReportModal.vue'
import ReportConfigDrawer from '@/components/analytics/ReportConfigDrawer.vue'

const router = useRouter()
const analyticsStore = useAnalyticsStore()

// State
const selectedProjectId = ref<string>()
const dateRange = ref<[Dayjs, Dayjs]>([
  dayjs().subtract(30, 'day'),
  dayjs()
])
const taskTrendPeriod = ref<TimeRange>(TimeRange.MONTH)
const bugChartType = ref<'severity' | 'module' | 'status'>('severity')
const detailTab = ref('tasks')

const createReportModalVisible = ref(false)
const scheduleReportDrawerVisible = ref(false)
const currentReportId = ref<string>()

// Mock data
const mockProjects = ref([
  { id: '1', name: '项目A' },
  { id: '2', name: '项目B' },
  { id: '3', name: '项目C' }
])

const mockUsers = ref([
  { id: '1', name: '张三', email: 'zhangsan@example.com' },
  { id: '2', name: '李四', email: 'lisi@example.com' },
  { id: '3', name: '王五', email: 'wangwu@example.com' }
])

// Computed
const loading = computed(() => analyticsStore.loading)
const statsLoading = computed(() => analyticsStore.statsLoading)
const chartLoading = computed(() => analyticsStore.chartLoading)
const reportLoading = computed(() => analyticsStore.reportLoading)

const dashboardStats = computed(() => analyticsStore.dashboardStats)
const projectProgress = computed(() => analyticsStore.projectProgress)
const taskTrendData = computed(() => analyticsStore.taskTrendData)
const teamWorkload = computed(() => analyticsStore.teamWorkload)
const bugAnalysis = computed(() => analyticsStore.bugAnalysis)
const documentActivity = computed(() => analyticsStore.documentActivity)
const changeImpactData = computed(() => analyticsStore.changeImpactData)
const customReports = computed(() => analyticsStore.customReports)

const datePresets = [
  { label: '最近7天', value: [dayjs().subtract(7, 'day'), dayjs()] as [Dayjs, Dayjs] },
  { label: '最近30天', value: [dayjs().subtract(30, 'day'), dayjs()] as [Dayjs, Dayjs] },
  { label: '最近90天', value: [dayjs().subtract(90, 'day'), dayjs()] as [Dayjs, Dayjs] },
  { label: '本月', value: [dayjs().startOf('month'), dayjs()] as [Dayjs, Dayjs] },
  { label: '本季度', value: [dayjs().startOf('quarter'), dayjs()] as [Dayjs, Dayjs] }
]

// Methods
const handleProjectChange = () => {
  analyticsStore.setSelectedProject(selectedProjectId.value)
  loadAllData()
}

const handleDateRangeChange = () => {
  if (dateRange.value) {
    analyticsStore.setDateRange([
      dateRange.value[0].format('YYYY-MM-DD'),
      dateRange.value[1].format('YYYY-MM-DD')
    ])
    loadAllData()
  }
}

const handleTaskTrendPeriodChange = () => {
  analyticsStore.fetchTaskTrend(selectedProjectId.value, taskTrendPeriod.value)
}

const handleBugChartTypeChange = () => {
  analyticsStore.fetchBugAnalysis(selectedProjectId.value, bugChartType.value)
}

const handleRefreshData = async () => {
  await analyticsStore.refreshAllData()
}

const handleExportReport = () => {
  message.info('导出报告功能开发中')
}

const handleViewProjectDetails = () => {
  router.push('/projects')
}

const handleMemberClick = (userId: string) => {
  console.log('Member clicked:', userId)
  message.info(`查看成员 ${userId} 的详细信息`)
}

const getProgressStatus = (project: ProjectProgress) => {
  if (project.status === 'on_track') return undefined
  if (project.status === 'at_risk') return 'active'
  return 'exception'
}

const getTrendClass = (delta: number) => {
  if (delta > 0) return 'trend-up'
  if (delta < 0) return 'trend-down'
  return 'trend-stable'
}

const getTrendText = (delta: number) => {
  if (delta > 0) return `↑ ${delta.toFixed(1)}%`
  if (delta < 0) return `↓ ${Math.abs(delta).toFixed(1)}%`
  return '→ 0%'
}

const getDateRangeStrings = (): [string, string] => {
  return [
    dateRange.value[0].format('YYYY-MM-DD'),
    dateRange.value[1].format('YYYY-MM-DD')
  ]
}

const showCreateReportModal = () => {
  createReportModalVisible.value = true
}

const showScheduleReportDrawer = () => {
  scheduleReportDrawerVisible.value = true
}

const handleReportCreated = () => {
  analyticsStore.fetchCustomReports(selectedProjectId.value)
}

const handleScheduleConfigured = () => {
  message.success('定时报告配置成功')
}

const handleViewReport = (report: CustomReport) => {
  message.info(`查看报告: ${report.name}`)
}

const handleEditReport = (report: CustomReport) => {
  message.info(`编辑报告: ${report.name}`)
}

const handleExportSingleReport = (report: CustomReport) => {
  message.info(`导出报告: ${report.name}`)
}

const loadAllData = async () => {
  await Promise.all([
    analyticsStore.fetchDashboardStats(selectedProjectId.value),
    analyticsStore.fetchProjectProgress(selectedProjectId.value),
    analyticsStore.fetchTaskTrend(selectedProjectId.value, taskTrendPeriod.value),
    analyticsStore.fetchTeamWorkload(selectedProjectId.value),
    analyticsStore.fetchBugAnalysis(selectedProjectId.value, bugChartType.value),
    analyticsStore.fetchDocumentActivity(selectedProjectId.value),
    analyticsStore.fetchChangeImpact(selectedProjectId.value),
    analyticsStore.fetchCustomReports(selectedProjectId.value)
  ])
}

onMounted(() => {
  loadAllData()
})
</script>

<style scoped lang="scss">
.analytics-dashboard {
  padding: 24px;
  background: #f0f2f5;
  min-height: calc(100vh - 64px);

  .dashboard-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 24px;
    background: #ffffff;
    border-radius: 4px;
    margin-bottom: 16px;

    h2 {
      margin: 0;
      font-size: 20px;
      font-weight: 600;
      color: #262626;
    }
  }

  .stat-footer {
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px solid #f0f0f0;
    display: flex;
    justify-content: space-between;
    align-items: center;

    .stat-hint {
      color: #8c8c8c;
      font-size: 12px;
    }

    .trend-value {
      font-size: 14px;
      font-weight: 500;

      &.trend-up {
        color: #52c41a;
      }

      &.trend-down {
        color: #f5222d;
      }

      &.trend-stable {
        color: #8c8c8c;
      }
    }
  }
}
</style>
