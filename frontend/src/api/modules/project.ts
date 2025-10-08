/**
 * 项目管理 API
 */
import { get, post, put, del } from '../request'
import type {
  Project,
  ProjectDetail,
  ProjectQueryParams,
  CreateProjectRequest,
  UpdateProjectRequest,
  ProjectMember,
  AddProjectMemberRequest,
  UpdateProjectMemberRequest,
  ProjectStatistics
} from '@/types/project'
import type { PageResult } from '@/types/global'

/**
 * 获取项目列表
 */
export function getProjectList(params?: ProjectQueryParams) {
  return get<PageResult<Project>>('/api/v1/projects', { params })
}

/**
 * 获取项目详情
 */
export function getProjectDetail(id: number) {
  return get<ProjectDetail>(`/api/v1/projects/${id}`)
}

/**
 * 创建项目
 */
export function createProject(data: CreateProjectRequest) {
  return post<Project>('/api/v1/projects', data)
}

/**
 * 更新项目
 */
export function updateProject(id: number, data: UpdateProjectRequest) {
  return put<Project>(`/api/v1/projects/${id}`, data)
}

/**
 * 删除项目
 */
export function deleteProject(id: number) {
  return del(`/api/v1/projects/${id}`)
}

/**
 * 归档项目
 */
export function archiveProject(id: number) {
  return post(`/api/v1/projects/${id}/archive`)
}

/**
 * 恢复项目
 */
export function restoreProject(id: number) {
  return post(`/api/v1/projects/${id}/restore`)
}

/**
 * 获取项目成员列表
 */
export function getProjectMembers(projectId: number, params?: { page?: number; pageSize?: number; roleId?: number }) {
  return get<PageResult<ProjectMember>>(`/api/v1/projects/${projectId}/members`, { params })
}

/**
 * 添加项目成员
 */
export function addProjectMember(projectId: number, userId: number, roleId: number) {
  return post<ProjectMember>(`/api/v1/projects/${projectId}/members`, null, {
    params: { userId, roleId }
  })
}

/**
 * 更新项目成员角色
 */
export function updateProjectMemberRole(projectId: number, userId: number, roleId: number) {
  return put<ProjectMember>(`/api/v1/projects/${projectId}/members/${userId}`, null, {
    params: { roleId }
  })
}

/**
 * 移除项目成员
 */
export function removeProjectMember(projectId: number, userId: number) {
  return del(`/api/v1/projects/${projectId}/members/${userId}`)
}

/**
 * 获取项目统计信息
 */
export function getProjectStatistics(projectId: number) {
  return get<ProjectStatistics>(`/api/v1/projects/${projectId}/statistics`)
}

/**
 * 检查项目编码是否存在
 */
export function checkProjectCode(code: string) {
  return get<boolean>('/api/v1/projects/check-code', { params: { code } })
}

