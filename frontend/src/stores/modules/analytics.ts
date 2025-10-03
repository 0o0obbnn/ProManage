/**
 * 数据分析状态管理
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type {
  DashboardStats,
  ProjectProgress,
  TaskTrendData,
  TeamWorkload,
  BugAnalysis,
  DocumentActivity,
  ChangeImpactData,
  TaskDetail,
  MemberContribution,
  QualityMetrics,
  TimelineAnalysis,
  CustomReport,
  ReportConfig
} from '@/types/analytics'
import { TimeRange } from '@/types/analytics'
import type { PageResult } from '@/types/global'
import * as analyticsApi from '@/api/modules/analytics'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'

export const useAnalyticsStore = defineStore('analytics', () => {
  // State
  const dashboardStats = ref<DashboardStats | null>(null)
  const projectProgress = ref<ProjectProgress[]>([])
  const taskTrendData = ref<TaskTrendData | null>(null)
  const teamWorkload = ref<TeamWorkload | null>(null)
  const bugAnalysis = ref<BugAnalysis | null>(null)
  const documentActivity = ref<DocumentActivity | null>(null)
  const changeImpactData = ref<ChangeImpactData | null>(null)

  const taskDetails = ref<TaskDetail[]>([])
  const memberContributions = ref<MemberContribution[]>([])
  const qualityMetrics = ref<QualityMetrics[]>([])
  const timelineAnalysis = ref<TimelineAnalysis | null>(null)

  const customReports = ref<CustomReport[]>([])
  const currentReport = ref<CustomReport | null>(null)

  const selectedProjectId = ref<string>()
  const selectedTimeRange = ref<TimeRange>(TimeRange.MONTH)
  const dateRange = ref<[string, string]>([
    dayjs().subtract(30, 'day').format('YYYY-MM-DD'),
    dayjs().format('YYYY-MM-DD')
  ])

  const taskDetailPagination = ref({
    page: 1,
    pageSize: 20,
    total: 0,
    totalPages: 0
  })

  const loading = ref(false)
  const statsLoading = ref(false)
  const chartLoading = ref(false)
  const tableLoading = ref(false)
  const reportLoading = ref(false)

  // Getters
  const hasData = computed(() => dashboardStats.value !== null)

  const completionRateTrend = computed(() => {
    if (!dashboardStats.value) return 'stable'
    const delta = dashboardStats.value.completionRateDelta
    if (delta > 0) return 'up'
    if (delta < 0) return 'down'
    return 'stable'
  })

  const onTimeRateTrend = computed(() => {
    if (!dashboardStats.value) return 'stable'
    const delta = dashboardStats.value.onTimeRateDelta
    if (delta > 0) return 'up'
    if (delta < 0) return 'down'
    return 'stable'
  })

  const totalTasks = computed(() => {
    if (!taskTrendData.value) return 0
    const latest = taskTrendData.value.data[taskTrendData.value.data.length - 1]
    return latest?.total || 0
  })

  const averageWorkload = computed(() => {
    if (!teamWorkload.value) return 0
    return teamWorkload.value.avgWorkload
  })

  const topPerformers = computed(() => {
    return memberContributions.value
      .sort((a, b) => b.contributionScore - a.contributionScore)
      .slice(0, 5)
  })

  /**
   * 获取仪表板统计
   */
  const fetchDashboardStats = async (projectId?: string) => {
    try {
      statsLoading.value = true
      const stats = await analyticsApi.getDashboardStats(projectId)
      dashboardStats.value = stats
      return stats
    } catch (error) {
      console.error('Fetch dashboard stats failed:', error)
      message.error('获取仪表板统计失败')
      throw error
    } finally {
      statsLoading.value = false
    }
  }

  /**
   * 获取项目进度
   */
  const fetchProjectProgress = async (projectId?: string) => {
    try {
      chartLoading.value = true
      const progress = await analyticsApi.getProjectProgress(projectId)
      projectProgress.value = progress
      return progress
    } catch (error) {
      console.error('Fetch project progress failed:', error)
      message.error('获取项目进度失败')
      throw error
    } finally {
      chartLoading.value = false
    }
  }

  /**
   * 获取任务趋势
   */
  const fetchTaskTrend = async (projectId?: string, period: TimeRange = TimeRange.MONTH) => {
    try {
      chartLoading.value = true
      const trend = await analyticsApi.getTaskTrend(projectId, period)
      taskTrendData.value = trend
      return trend
    } catch (error) {
      console.error('Fetch task trend failed:', error)
      message.error('获取任务趋势失败')
      throw error
    } finally {
      chartLoading.value = false
    }
  }

  /**
   * 获取团队工作量
   */
  const fetchTeamWorkload = async (projectId?: string) => {
    try {
      chartLoading.value = true
      const workload = await analyticsApi.getTeamWorkload(projectId)
      teamWorkload.value = workload
      return workload
    } catch (error) {
      console.error('Fetch team workload failed:', error)
      message.error('获取团队工作量失败')
      throw error
    } finally {
      chartLoading.value = false
    }
  }

  /**
   * 获取缺陷分析
   */
  const fetchBugAnalysis = async (projectId?: string, type: 'severity' | 'module' | 'status' = 'severity') => {
    try {
      chartLoading.value = true
      const analysis = await analyticsApi.getBugAnalysis(projectId, type)
      bugAnalysis.value = analysis
      return analysis
    } catch (error) {
      console.error('Fetch bug analysis failed:', error)
      message.error('获取缺陷分析失败')
      throw error
    } finally {
      chartLoading.value = false
    }
  }

  /**
   * 获取文档活跃度
   */
  const fetchDocumentActivity = async (projectId?: string, days: number = 30) => {
    try {
      chartLoading.value = true
      const activity = await analyticsApi.getDocumentActivity(projectId, days)
      documentActivity.value = activity
      return activity
    } catch (error) {
      console.error('Fetch document activity failed:', error)
      message.error('获取文档活跃度失败')
      throw error
    } finally {
      chartLoading.value = false
    }
  }

  /**
   * 获取变更影响
   */
  const fetchChangeImpact = async (projectId?: string, days: number = 30) => {
    try {
      chartLoading.value = true
      const impact = await analyticsApi.getChangeImpact(projectId, days)
      changeImpactData.value = impact
      return impact
    } catch (error) {
      console.error('Fetch change impact failed:', error)
      message.error('获取变更影响失败')
      throw error
    } finally {
      chartLoading.value = false
    }
  }

  /**
   * 获取任务明细
   */
  const fetchTaskDetails = async (params?: any) => {
    try {
      tableLoading.value = true
      const result = await analyticsApi.getTaskDetails({
        projectId: selectedProjectId.value,
        dateRange: {
          start: dateRange.value[0],
          end: dateRange.value[1]
        },
        page: taskDetailPagination.value.page,
        pageSize: taskDetailPagination.value.pageSize,
        ...params
      })
      taskDetails.value = result.list
      taskDetailPagination.value.total = result.total
      taskDetailPagination.value.totalPages = result.totalPages
      return result
    } catch (error) {
      console.error('Fetch task details failed:', error)
      message.error('获取任务明细失败')
      throw error
    } finally {
      tableLoading.value = false
    }
  }

  /**
   * 获取成员贡献
   */
  const fetchMemberContribution = async () => {
    try {
      tableLoading.value = true
      const contributions = await analyticsApi.getMemberContribution({
        projectId: selectedProjectId.value,
        dateRange: {
          start: dateRange.value[0],
          end: dateRange.value[1]
        }
      })
      memberContributions.value = contributions
      return contributions
    } catch (error) {
      console.error('Fetch member contribution failed:', error)
      message.error('获取成员贡献失败')
      throw error
    } finally {
      tableLoading.value = false
    }
  }

  /**
   * 获取质量指标
   */
  const fetchQualityMetrics = async () => {
    try {
      tableLoading.value = true
      const metrics = await analyticsApi.getQualityMetrics({
        projectId: selectedProjectId.value,
        dateRange: {
          start: dateRange.value[0],
          end: dateRange.value[1]
        }
      })
      qualityMetrics.value = metrics
      return metrics
    } catch (error) {
      console.error('Fetch quality metrics failed:', error)
      message.error('获取质量指标失败')
      throw error
    } finally {
      tableLoading.value = false
    }
  }

  /**
   * 获取时间线分析
   */
  const fetchTimelineAnalysis = async () => {
    try {
      tableLoading.value = true
      const analysis = await analyticsApi.getTimelineAnalysis({
        projectId: selectedProjectId.value,
        dateRange: {
          start: dateRange.value[0],
          end: dateRange.value[1]
        }
      })
      timelineAnalysis.value = analysis
      return analysis
    } catch (error) {
      console.error('Fetch timeline analysis failed:', error)
      message.error('获取时间线分析失败')
      throw error
    } finally {
      tableLoading.value = false
    }
  }

  /**
   * 获取自定义报告列表
   */
  const fetchCustomReports = async (projectId?: string) => {
    try {
      reportLoading.value = true
      const reports = await analyticsApi.getCustomReports(projectId)
      customReports.value = reports
      return reports
    } catch (error) {
      console.error('Fetch custom reports failed:', error)
      message.error('获取自定义报告失败')
      throw error
    } finally {
      reportLoading.value = false
    }
  }

  /**
   * 创建自定义报告
   */
  const createCustomReport = async (config: ReportConfig) => {
    try {
      reportLoading.value = true
      const report = await analyticsApi.createCustomReport(config)
      customReports.value.unshift(report)
      message.success('报告创建成功')
      return report
    } catch (error) {
      console.error('Create custom report failed:', error)
      message.error('创建报告失败')
      throw error
    } finally {
      reportLoading.value = false
    }
  }

  /**
   * 更新自定义报告
   */
  const updateCustomReport = async (id: string, config: Partial<ReportConfig>) => {
    try {
      reportLoading.value = true
      const report = await analyticsApi.updateCustomReport(id, config)
      const index = customReports.value.findIndex(r => r.id === id)
      if (index !== -1) {
        customReports.value[index] = report
      }
      message.success('报告更新成功')
      return report
    } catch (error) {
      console.error('Update custom report failed:', error)
      message.error('更新报告失败')
      throw error
    } finally {
      reportLoading.value = false
    }
  }

  /**
   * 删除自定义报告
   */
  const deleteCustomReport = async (id: string) => {
    try {
      reportLoading.value = true
      await analyticsApi.deleteCustomReport(id)
      customReports.value = customReports.value.filter(r => r.id !== id)
      message.success('报告删除成功')
    } catch (error) {
      console.error('Delete custom report failed:', error)
      message.error('删除报告失败')
      throw error
    } finally {
      reportLoading.value = false
    }
  }

  /**
   * 生成报告
   */
  const generateReport = async (id: string) => {
    try {
      reportLoading.value = true
      const result = await analyticsApi.generateReport(id)
      message.success('报告生成成功')
      return result
    } catch (error) {
      console.error('Generate report failed:', error)
      message.error('生成报告失败')
      throw error
    } finally {
      reportLoading.value = false
    }
  }

  /**
   * 导出报告
   */
  const exportReport = async (params: any) => {
    try {
      reportLoading.value = true
      const result = await analyticsApi.exportReport(params)
      message.success('报告导出成功')
      return result
    } catch (error) {
      console.error('Export report failed:', error)
      message.error('导出报告失败')
      throw error
    } finally {
      reportLoading.value = false
    }
  }

  /**
   * 刷新所有数据
   */
  const refreshAllData = async () => {
    try {
      loading.value = true
      await Promise.all([
        fetchDashboardStats(selectedProjectId.value),
        fetchProjectProgress(selectedProjectId.value),
        fetchTaskTrend(selectedProjectId.value, selectedTimeRange.value),
        fetchTeamWorkload(selectedProjectId.value),
        fetchBugAnalysis(selectedProjectId.value),
        fetchDocumentActivity(selectedProjectId.value),
        fetchChangeImpact(selectedProjectId.value)
      ])
      message.success('数据刷新成功')
    } catch (error) {
      console.error('Refresh all data failed:', error)
      message.error('刷新数据失败')
    } finally {
      loading.value = false
    }
  }

  /**
   * 设置选中的项目
   */
  const setSelectedProject = (projectId?: string) => {
    selectedProjectId.value = projectId
  }

  /**
   * 设置时间范围
   */
  const setTimeRange = (range: TimeRange) => {
    selectedTimeRange.value = range
  }

  /**
   * 设置日期范围
   */
  const setDateRange = (range: [string, string]) => {
    dateRange.value = range
  }

  /**
   * 设置任务明细分页
   */
  const setTaskDetailPagination = (page: number, pageSize?: number) => {
    taskDetailPagination.value.page = page
    if (pageSize) {
      taskDetailPagination.value.pageSize = pageSize
    }
  }

  /**
   * 重置所有数据
   */
  const resetData = () => {
    dashboardStats.value = null
    projectProgress.value = []
    taskTrendData.value = null
    teamWorkload.value = null
    bugAnalysis.value = null
    documentActivity.value = null
    changeImpactData.value = null
    taskDetails.value = []
    memberContributions.value = []
    qualityMetrics.value = []
    timelineAnalysis.value = null
  }

  return {
    // State
    dashboardStats,
    projectProgress,
    taskTrendData,
    teamWorkload,
    bugAnalysis,
    documentActivity,
    changeImpactData,
    taskDetails,
    memberContributions,
    qualityMetrics,
    timelineAnalysis,
    customReports,
    currentReport,
    selectedProjectId,
    selectedTimeRange,
    dateRange,
    taskDetailPagination,
    loading,
    statsLoading,
    chartLoading,
    tableLoading,
    reportLoading,
    // Getters
    hasData,
    completionRateTrend,
    onTimeRateTrend,
    totalTasks,
    averageWorkload,
    topPerformers,
    // Actions
    fetchDashboardStats,
    fetchProjectProgress,
    fetchTaskTrend,
    fetchTeamWorkload,
    fetchBugAnalysis,
    fetchDocumentActivity,
    fetchChangeImpact,
    fetchTaskDetails,
    fetchMemberContribution,
    fetchQualityMetrics,
    fetchTimelineAnalysis,
    fetchCustomReports,
    createCustomReport,
    updateCustomReport,
    deleteCustomReport,
    generateReport,
    exportReport,
    refreshAllData,
    setSelectedProject,
    setTimeRange,
    setDateRange,
    setTaskDetailPagination,
    resetData
  }
})
