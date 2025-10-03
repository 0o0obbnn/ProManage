/**
 * 数据分析 API
 */
import { get, post } from '../request'
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
  ReportConfig,
  ExportReportParams,
  AnalyticsQueryParams,
  TimeRange
} from '@/types/analytics'
import type { PageResult } from '@/types/global'

/**
 * 获取仪表板统计
 */
export function getDashboardStats(projectId?: string) {
  return get<DashboardStats>('/analytics/dashboard/stats', {
    params: { projectId }
  })
}

/**
 * 获取项目进度
 */
export function getProjectProgress(projectId?: string) {
  return get<ProjectProgress[]>('/analytics/project/progress', {
    params: { projectId }
  })
}

/**
 * 获取任务趋势
 */
export function getTaskTrend(projectId?: string, period: TimeRange = TimeRange.MONTH) {
  return get<TaskTrendData>('/analytics/task/trend', {
    params: { projectId, period }
  })
}

/**
 * 获取团队工作量
 */
export function getTeamWorkload(projectId?: string) {
  return get<TeamWorkload>('/analytics/team/workload', {
    params: { projectId }
  })
}

/**
 * 获取缺陷分析
 */
export function getBugAnalysis(projectId?: string, type: 'severity' | 'module' | 'status' = 'severity') {
  return get<BugAnalysis>('/analytics/bug/analysis', {
    params: { projectId, type }
  })
}

/**
 * 获取文档活跃度
 */
export function getDocumentActivity(projectId?: string, days: number = 30) {
  return get<DocumentActivity>('/analytics/document/activity', {
    params: { projectId, days }
  })
}

/**
 * 获取变更影响
 */
export function getChangeImpact(projectId?: string, days: number = 30) {
  return get<ChangeImpactData>('/analytics/change/impact', {
    params: { projectId, days }
  })
}

/**
 * 获取任务明细
 */
export function getTaskDetails(params: AnalyticsQueryParams & { page?: number; pageSize?: number }) {
  return get<PageResult<TaskDetail>>('/analytics/task/details', {
    params
  })
}

/**
 * 获取成员贡献
 */
export function getMemberContribution(params: AnalyticsQueryParams) {
  return get<MemberContribution[]>('/analytics/member/contribution', {
    params
  })
}

/**
 * 获取质量指标
 */
export function getQualityMetrics(params: AnalyticsQueryParams) {
  return get<QualityMetrics[]>('/analytics/quality/metrics', {
    params
  })
}

/**
 * 获取时间线分析
 */
export function getTimelineAnalysis(params: AnalyticsQueryParams) {
  return get<TimelineAnalysis>('/analytics/timeline/analysis', {
    params
  })
}

/**
 * 获取自定义报告列表
 */
export function getCustomReports(projectId?: string) {
  return get<CustomReport[]>('/analytics/reports', {
    params: { projectId }
  })
}

/**
 * 创建自定义报告
 */
export function createCustomReport(data: ReportConfig) {
  return post<CustomReport>('/analytics/reports', data)
}

/**
 * 更新自定义报告
 */
export function updateCustomReport(id: string, data: Partial<ReportConfig>) {
  return post<CustomReport>(`/analytics/reports/${id}`, data)
}

/**
 * 删除自定义报告
 */
export function deleteCustomReport(id: string) {
  return post(`/analytics/reports/${id}/delete`)
}

/**
 * 生成报告
 */
export function generateReport(id: string) {
  return post<{ fileUrl: string }>(`/analytics/reports/${id}/generate`)
}

/**
 * 导出报告
 */
export function exportReport(params: ExportReportParams) {
  return post<{ fileUrl: string; fileName: string }>('/analytics/export', params, {
    responseType: 'blob'
  })
}

/**
 * 获取报告详情
 */
export function getReportDetail(id: string) {
  return get<CustomReport>(`/analytics/reports/${id}`)
}

/**
 * 配置定时报告
 */
export function configScheduleReport(data: {
  reportId: string
  schedule: ReportConfig['schedule']
}) {
  return post('/analytics/reports/schedule', data)
}

/**
 * 取消定时报告
 */
export function cancelScheduleReport(reportId: string) {
  return post(`/analytics/reports/${reportId}/schedule/cancel`)
}

/**
 * 获取项目概览
 */
export function getProjectOverview(projectId: string) {
  return get<{
    stats: DashboardStats
    progress: ProjectProgress
    recentActivities: any[]
    members: any[]
  }>(`/analytics/project/${projectId}/overview`)
}

/**
 * 获取团队效率分析
 */
export function getTeamEfficiency(params: AnalyticsQueryParams) {
  return get<{
    avgCompletionTime: number
    avgResponseTime: number
    collaborationIndex: number
    productivityTrend: Array<{ date: string; value: number }>
  }>('/analytics/team/efficiency', {
    params
  })
}

/**
 * 获取风险预警
 */
export function getRiskAlerts(projectId?: string) {
  return get<Array<{
    id: string
    type: 'delay' | 'quality' | 'resource' | 'dependency'
    severity: 'critical' | 'high' | 'medium' | 'low'
    title: string
    description: string
    affectedItems: string[]
    suggestedActions: string[]
  }>>('/analytics/risk/alerts', {
    params: { projectId }
  })
}
