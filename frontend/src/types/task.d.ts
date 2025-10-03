/**
 * 任务管理类型定义
 */

/**
 * 任务状态枚举
 */
export enum TaskStatus {
  TODO = 'todo',
  IN_PROGRESS = 'in_progress',
  TESTING = 'testing',
  DONE = 'done'
}

/**
 * 任务优先级枚举
 */
export enum TaskPriority {
  URGENT = 'urgent',
  HIGH = 'high',
  MEDIUM = 'medium',
  LOW = 'low'
}

/**
 * 用户信息（简化版）
 */
export interface TaskUser {
  id: number
  name: string
  avatar?: string
  email?: string
}

/**
 * 子任务接口
 */
export interface SubTask {
  id: number
  taskId: number
  title: string
  completed: boolean
  assignee?: TaskUser
  createdAt: string
  updatedAt: string
}

/**
 * 任务评论接口
 */
export interface TaskComment {
  id: number
  taskId: number
  content: string
  user: TaskUser
  parentId?: number
  replies?: TaskComment[]
  createdAt: string
  updatedAt: string
}

/**
 * 任务附件接口
 */
export interface TaskAttachment {
  id: number
  taskId: number
  name: string
  url: string
  size: number
  type: string
  uploadedBy: TaskUser
  createdAt: string
}

/**
 * 任务活动记录
 */
export interface TaskActivity {
  id: number
  taskId: number
  action: string
  description: string
  user: TaskUser
  metadata?: Record<string, any>
  createdAt: string
}

/**
 * 任务接口
 */
export interface Task {
  id: number
  title: string
  description?: string
  status: TaskStatus
  priority: TaskPriority
  assignee?: TaskUser
  assigneeId?: number
  reporter?: TaskUser
  reporterId: number
  participants?: TaskUser[]
  projectId: number
  projectName?: string
  sprintId?: number
  sprintName?: string
  parentId?: number
  tags?: string[]
  dueDate?: string
  startDate?: string
  estimatedHours?: number
  actualHours?: number
  progress: number
  completed: boolean
  subtasks?: SubTask[]
  subtaskCount: number
  completedSubtasks: number
  attachments?: TaskAttachment[]
  attachmentCount: number
  comments?: TaskComment[]
  commentCount: number
  activities?: TaskActivity[]
  createdAt: string
  updatedAt: string
}

/**
 * 迭代接口
 */
export interface Sprint {
  id: number
  name: string
  description?: string
  projectId: number
  status: 'planning' | 'active' | 'completed'
  startDate: string
  endDate: string
  taskCount: number
  completedTaskCount: number
  progress: number
  createdAt: string
  updatedAt: string
}

/**
 * 看板列接口
 */
export interface TaskColumn {
  id: string
  title: string
  status: TaskStatus
  color: string
  tasks: Task[]
  order: number
}

/**
 * 任务查询参数
 */
export interface TaskQueryParams {
  page?: number
  pageSize?: number
  keyword?: string
  projectId?: number
  sprintId?: number
  status?: TaskStatus
  priority?: TaskPriority
  assigneeId?: number
  reporterId?: number
  tags?: string[]
  dueDate?: string
  startDate?: string
  endDate?: string
  sort?: string
  order?: 'asc' | 'desc'
}

/**
 * 任务统计信息
 */
export interface TaskStats {
  total: number
  completed: number
  inProgress: number
  testing: number
  todo: number
  completionRate: number
  overdue: number
  todayDue: number
}

/**
 * 视图模式
 */
export type TaskViewMode = 'kanban' | 'list' | 'gantt'

/**
 * 分组方式
 */
export type TaskGroupBy = 'status' | 'assignee' | 'priority' | 'label'

/**
 * 任务创建/更新参数
 */
export interface TaskFormData {
  title: string
  description?: string
  status: TaskStatus
  priority: TaskPriority
  assigneeId?: number
  projectId: number
  sprintId?: number
  parentId?: number
  tags?: string[]
  dueDate?: string
  startDate?: string
  estimatedHours?: number
}

/**
 * 甘特图任务数据
 */
export interface GanttTask {
  id: number
  title: string
  start: Date
  end: Date
  progress: number
  status: TaskStatus
  priority: TaskPriority
  assignee?: TaskUser
  dependencies?: number[]
  color?: string
}