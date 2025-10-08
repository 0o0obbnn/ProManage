/**
 * 项目管理相关类型定义
 */

/**
 * 项目状态枚举
 */
export enum ProjectStatus {
  /** 规划中 */
  PLANNING = 0,
  /** 进行中 */
  IN_PROGRESS = 1,
  /** 已完成 */
  COMPLETED = 2,
  /** 已归档 */
  ARCHIVED = 3,
  /** 已暂停 */
  PAUSED = 4
}

/**
 * 项目优先级枚举
 */
export enum ProjectPriority {
  /** 低 */
  LOW = 0,
  /** 中 */
  MEDIUM = 1,
  /** 高 */
  HIGH = 2,
  /** 紧急 */
  URGENT = 3
}

/**
 * 项目类型枚举
 */
export enum ProjectType {
  /** Web项目 */
  WEB = 'WEB',
  /** 移动应用 */
  APP = 'APP',
  /** 系统软件 */
  SYSTEM = 'SYSTEM',
  /** 其他 */
  OTHER = 'OTHER'
}

/**
 * 项目成员角色枚举
 */
export enum ProjectRole {
  /** 项目所有者 */
  OWNER = 'OWNER',
  /** 项目管理员 */
  ADMIN = 'ADMIN',
  /** 项目成员 */
  MEMBER = 'MEMBER',
  /** 访客 */
  GUEST = 'GUEST'
}

/**
 * 项目基本信息
 */
export interface Project {
  /** 项目ID */
  id: number
  /** 项目名称 */
  name: string
  /** 项目编码 */
  code: string
  /** 项目描述 */
  description?: string
  /** 项目状态 */
  status: ProjectStatus
  /** 项目优先级 */
  priority?: ProjectPriority
  /** 项目类型 */
  type?: ProjectType
  /** 项目进度(0-100) */
  progress: number
  /** 项目所有者ID */
  ownerId: number
  /** 项目所有者名称 */
  ownerName?: string
  /** 项目图标 */
  icon?: string
  /** 项目颜色 */
  color?: string
  /** 计划开始日期 */
  startDate?: string
  /** 计划结束日期 */
  endDate?: string
  /** 实际开始日期 */
  actualStartDate?: string
  /** 实际结束日期 */
  actualEndDate?: string
  /** 成员数量 */
  memberCount?: number
  /** 任务数量 */
  taskCount?: number
  /** 文档数量 */
  documentCount?: number
  /** 创建时间 */
  createdAt: string
  /** 更新时间 */
  updatedAt: string
  /** 创建人ID */
  createdBy?: number
  /** 更新人ID */
  updatedBy?: number
}

/**
 * 项目详情
 */
export interface ProjectDetail extends Project {
  /** 项目成员列表 */
  members?: ProjectMember[]
  /** 最近活动 */
  recentActivities?: ProjectActivity[]
  /** 项目统计 */
  statistics?: ProjectStatistics
}

/**
 * 项目成员
 */
export interface ProjectMember {
  /** 成员ID */
  id: number
  /** 项目ID */
  projectId: number
  /** 用户ID */
  userId: number
  /** 用户名 */
  username: string
  /** 用户昵称 */
  nickname?: string
  /** 用户头像 */
  avatar?: string
  /** 用户邮箱 */
  email?: string
  /** 角色ID */
  roleId: number
  /** 角色名称 */
  roleName: string
  /** 项目角色 */
  projectRole: ProjectRole
  /** 加入时间 */
  joinedAt: string
}

/**
 * 项目活动
 */
export interface ProjectActivity {
  /** 活动ID */
  id: number
  /** 项目ID */
  projectId: number
  /** 用户ID */
  userId: number
  /** 用户名 */
  username: string
  /** 活动类型 */
  type: string
  /** 活动内容 */
  content: string
  /** 活动时间 */
  createdAt: string
}

/**
 * 项目统计信息
 */
export interface ProjectStatistics {
  /** 总任务数 */
  totalTasks: number
  /** 已完成任务数 */
  completedTasks: number
  /** 进行中任务数 */
  inProgressTasks: number
  /** 总文档数 */
  totalDocuments: number
  /** 总成员数 */
  totalMembers: number
  /** 完成率 */
  completionRate: number
}

/**
 * 项目查询参数
 */
export interface ProjectQueryParams {
  /** 页码 */
  page?: number
  /** 每页数量 */
  pageSize?: number
  /** 关键词搜索 */
  keyword?: string
  /** 项目状态 */
  status?: ProjectStatus
  /** 项目优先级 */
  priority?: ProjectPriority
  /** 项目类型 */
  type?: ProjectType
  /** 项目所有者ID */
  ownerId?: number
  /** 排序字段 */
  sort?: 'createdAt' | 'updatedAt' | 'name' | 'progress'
  /** 排序方式 */
  order?: 'asc' | 'desc'
}

/**
 * 创建项目请求
 */
export interface CreateProjectRequest {
  /** 项目名称 */
  name: string
  /** 项目编码 */
  code: string
  /** 项目描述 */
  description?: string
  /** 项目优先级 */
  priority?: ProjectPriority
  /** 项目类型 */
  type?: ProjectType
  /** 项目图标 */
  icon?: string
  /** 项目颜色 */
  color?: string
  /** 计划开始日期 */
  startDate?: string
  /** 计划结束日期 */
  endDate?: string
}

/**
 * 更新项目请求
 */
export interface UpdateProjectRequest {
  /** 项目名称 */
  name?: string
  /** 项目描述 */
  description?: string
  /** 项目状态 */
  status?: ProjectStatus
  /** 项目优先级 */
  priority?: ProjectPriority
  /** 项目类型 */
  type?: ProjectType
  /** 项目进度 */
  progress?: number
  /** 项目图标 */
  icon?: string
  /** 项目颜色 */
  color?: string
  /** 计划开始日期 */
  startDate?: string
  /** 计划结束日期 */
  endDate?: string
  /** 实际开始日期 */
  actualStartDate?: string
  /** 实际结束日期 */
  actualEndDate?: string
}

/**
 * 添加项目成员请求
 */
export interface AddProjectMemberRequest {
  /** 用户ID */
  userId: number
  /** 角色ID */
  roleId: number
}

/**
 * 更新项目成员请求
 */
export interface UpdateProjectMemberRequest {
  /** 角色ID */
  roleId: number
}

