/**
 * 项目模板类型定义
 */

/**
 * 项目模板
 */
export interface ProjectTemplate {
  /** 模板ID */
  id: number
  /** 模板名称 */
  name: string
  /** 模板描述 */
  description?: string
  /** 模板图标 */
  icon?: string
  /** 模板颜色 */
  color?: string
  /** 模板类型 */
  type: TemplateType
  /** 是否为系统模板 */
  isSystem: boolean
  /** 创建者ID */
  creatorId?: number
  /** 创建者名称 */
  creatorName?: string
  /** 使用次数 */
  usageCount: number
  /** 创建时间 */
  createTime: string
  /** 更新时间 */
  updateTime: string
}

/**
 * 模板类型
 */
export enum TemplateType {
  /** 敏捷开发 */
  AGILE = 'AGILE',
  /** 瀑布模型 */
  WATERFALL = 'WATERFALL',
  /** 看板 */
  KANBAN = 'KANBAN',
  /** Scrum */
  SCRUM = 'SCRUM',
  /** 自定义 */
  CUSTOM = 'CUSTOM'
}

/**
 * 从模板创建项目请求
 */
export interface CreateProjectFromTemplateRequest {
  /** 模板ID */
  templateId: number
  /** 项目名称 */
  name: string
  /** 项目编码 */
  code: string
  /** 项目描述 */
  description?: string
  /** 计划开始日期 */
  plannedStartDate?: string
  /** 计划结束日期 */
  plannedEndDate?: string
}

