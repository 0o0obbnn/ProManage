/**
 * Dashboard API
 */
import request from '../request'

/**
 * 统计数据
 */
export interface DashboardStats {
  taskStats: {
    total: number
    completed: number
    inProgress: number
    pending: number
  }
  projectStats: {
    total: number
    active: number
    completed: number
    archived: number
  }
  changeStats: {
    total: number
    pending: number
    approved: number
    rejected: number
  }
  testStats: {
    total: number
    pending: number
    passed: number
    failed: number
  }
}

/**
 * 项目进度数据
 */
export interface ProjectProgress {
  projectId: number
  projectName: string
  progress: number
  totalTasks: number
  completedTasks: number
}

/**
 * 团队活跃度数据
 */
export interface TeamActivity {
  date: string
  count: number
}

/**
 * 活动记录
 */
export interface Activity {
  id: number
  type: 'task' | 'document' | 'change' | 'comment' | 'project'
  user: {
    id: number
    name: string
    avatar?: string
  }
  action: string
  target: string
  targetId: number
  createdAt: string
}

/**
 * 获取Dashboard统计数据
 */
export const getDashboardStats = () => {
  return request<DashboardStats>({
    url: '/api/v1/dashboard/stats',
    method: 'GET'
  })
}

/**
 * 获取项目进度数据
 */
export const getProjectProgress = () => {
  return request<ProjectProgress[]>({
    url: '/api/v1/dashboard/project-progress',
    method: 'GET'
  })
}

/**
 * 获取团队活跃度数据
 */
export const getTeamActivity = (days: number = 30) => {
  return request<TeamActivity[]>({
    url: '/api/v1/dashboard/team-activity',
    method: 'GET',
    params: { days }
  })
}

/**
 * 获取最近活动
 */
export const getRecentActivities = (limit: number = 20) => {
  return request<Activity[]>({
    url: '/api/v1/dashboard/activities',
    method: 'GET',
    params: { limit }
  })
}

/**
 * 导出Dashboard API
 */
export const dashboardApi = {
  getDashboardStats,
  getProjectProgress,
  getTeamActivity,
  getRecentActivities
}

export default dashboardApi

