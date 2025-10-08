/**
 * 任务管理 API
 */
import { get, post, put, del } from '../request'
import type {
  Task,
  TaskQueryParams,
  TaskFormData,
  Sprint,
  TaskComment,
  SubTask,
  TaskStats,
  TaskActivity,
  TaskAttachment
} from '@/types/task'
import type { PageResult } from '@/types/global'

/**
 * 获取项目任务列表
 */
export function getProjectTasks(projectId: number, params?: TaskQueryParams) {
  return get<PageResult<Task>>(`/api/v1/projects/${projectId}/tasks`, { params })
}

/**
 * 获取任务详情
 */
export function getTaskDetail(id: number) {
  return get<Task>(`/api/v1/tasks/${id}`)
}

/**
 * 创建任务
 */
export function createTask(projectId: number, data: TaskFormData) {
  return post<Task>(`/api/v1/projects/${projectId}/tasks`, data)
}

/**
 * 更新任务
 */
export function updateTask(id: number, data: Partial<TaskFormData>) {
  return put<Task>(`/api/v1/tasks/${id}`, data)
}

/**
 * 删除任务
 */
export function deleteTask(id: number) {
  return del(`/api/v1/tasks/${id}`)
}

/**
 * 更新任务状态
 */
export function updateTaskStatus(id: number, status: number) {
  return put(`/api/v1/tasks/${id}/status`, null, { params: { status } })
}

/**
 * 分配任务
 */
export function assignTask(id: number, assigneeId: number) {
  return put(`/api/v1/tasks/${id}/assign`, null, { params: { assigneeId } })
}

/**
 * 更新任务进度
 */
export function updateTaskProgress(id: number, progress: number) {
  return put(`/api/v1/tasks/${id}/progress`, null, { params: { progress } })
}

/**
 * 获取迭代列表
 */
export function getSprintList(projectId?: number) {
  return get<Sprint[]>('/sprints', { params: { projectId } })
}

/**
 * 创建迭代
 */
export function createSprint(data: {
  name: string
  description?: string
  projectId: number
  startDate: string
  endDate: string
}) {
  return post<Sprint>('/sprints', data)
}

/**
 * 更新迭代
 */
export function updateSprint(id: number, data: Partial<Sprint>) {
  return put<Sprint>(`/sprints/${id}`, data)
}

/**
 * 删除迭代
 */
export function deleteSprint(id: number) {
  return del(`/sprints/${id}`)
}

/**
 * 获取任务评论
 */
export function getTaskComments(id: number) {
  return get<TaskComment[]>(`/tasks/${id}/comments`)
}

/**
 * 添加任务评论
 */
export function addTaskComment(id: number, data: { content: string; parentId?: number }) {
  return post<TaskComment>(`/tasks/${id}/comments`, data)
}

/**
 * 删除任务评论
 */
export function deleteTaskComment(taskId: number, commentId: number) {
  return del(`/tasks/${taskId}/comments/${commentId}`)
}

/**
 * 获取子任务列表
 */
export function getSubTasks(taskId: number) {
  return get<SubTask[]>(`/tasks/${taskId}/subtasks`)
}

/**
 * 创建子任务
 */
export function createSubTask(taskId: number, data: { title: string; assigneeId?: number }) {
  return post<SubTask>(`/tasks/${taskId}/subtasks`, data)
}

/**
 * 更新子任务
 */
export function updateSubTask(id: number, data: Partial<SubTask>) {
  return put<SubTask>(`/subtasks/${id}`, data)
}

/**
 * 删除子任务
 */
export function deleteSubTask(id: number) {
  return del(`/subtasks/${id}`)
}

/**
 * 切换子任务完成状态
 */
export function toggleSubTaskComplete(id: number, completed: boolean) {
  return put<SubTask>(`/subtasks/${id}/toggle`, { completed })
}

/**
 * 获取任务附件
 */
export function getTaskAttachments(taskId: number) {
  return get<TaskAttachment[]>(`/tasks/${taskId}/attachments`)
}

/**
 * 上传任务附件
 */
export function uploadTaskAttachment(taskId: number, formData: FormData) {
  return post<TaskAttachment>(`/tasks/${taskId}/attachments`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 删除任务附件
 */
export function deleteTaskAttachment(taskId: number, attachmentId: number) {
  return del(`/tasks/${taskId}/attachments/${attachmentId}`)
}

/**
 * 获取任务活动记录
 */
export function getTaskActivities(taskId: number) {
  return get<TaskActivity[]>(`/tasks/${taskId}/activities`)
}

/**
 * 获取任务统计
 */
export function getTaskStats(projectId?: number, sprintId?: number) {
  return get<TaskStats>('/tasks/stats', { params: { projectId, sprintId } })
}

/**
 * 复制任务
 */
export function copyTask(id: number, data: { projectId?: number; sprintId?: number }) {
  return post<Task>(`/tasks/${id}/copy`, data)
}

/**
 * 移动任务到迭代
 */
export function moveTaskToSprint(id: number, sprintId?: number) {
  return put<Task>(`/tasks/${id}/move`, { sprintId })
}

/**
 * 获取标签列表
 */
export function getTaskTags(projectId?: number) {
  return get<string[]>('/tasks/tags', { params: { projectId } })
}