/**
 * 全局类型定义
 */

/**
 * 用户角色枚举
 */
export enum UserRole {
  SUPER_ADMIN = 'SUPER_ADMIN',
  PROJECT_MANAGER = 'PROJECT_MANAGER',
  DEVELOPER = 'DEVELOPER',
  TESTER = 'TESTER',
  UI_DESIGNER = 'UI_DESIGNER',
  OPERATIONS = 'OPERATIONS',
  THIRD_PARTY = 'THIRD_PARTY'
}

/**
 * 用户角色信息
 */
export interface RoleInfo {
  id: number
  roleName: string
  roleCode: string
  description?: string
  sort?: number
  status?: number
  createTime?: string
  updateTime?: string
}

/**
 * 用户信息接口
 */
export interface UserInfo {
  id: number
  username: string
  email: string
  phone?: string
  realName?: string
  avatar?: string
  status: number
  bio?: string
  department?: string
  position?: string
  lastLoginTime?: string
  lastLoginIp?: string
  createTime: string
  updateTime: string
  roles: RoleInfo[]
}

/**
 * 登录请求参数
 */
export interface LoginRequest {
  username: string // 用户名
  password: string
  rememberMe?: boolean // 记住我
}

/**
 * 登录响应数据
 */
export interface LoginResponse {
  token: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  userInfo: UserInfo
}

/**
 * API 响应包装器
 */
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

/**
 * 分页参数
 */
export interface PaginationParams {
  page: number
  pageSize: number
  sort?: string
  order?: 'asc' | 'desc'
}

/**
 * 分页响应数据
 */
export interface PageResult<T = any> {
  list: T[]
  total: number
  page: number
  pageSize: number
  totalPages: number
}

/**
 * 项目状态
 */
export enum ProjectStatus {
  ACTIVE = 'ACTIVE',
  COMPLETED = 'COMPLETED',
  ARCHIVED = 'ARCHIVED'
}

/**
 * 项目信息
 */
export interface ProjectInfo {
  id: number
  name: string
  description: string
  status: ProjectStatus
  ownerId: number
  ownerName: string
  memberCount: number
  createdAt: string
  updatedAt: string
}

/**
 * 菜单项配置
 */
export interface MenuItem {
  key: string
  label: string
  icon?: string
  path?: string
  children?: MenuItem[]
  badge?: number
  permissions?: string[]
}