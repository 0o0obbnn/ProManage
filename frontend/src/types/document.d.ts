/**
 * 文档管理类型定义
 */

/**
 * 文件类型枚举
 */
export enum FileType {
  PDF = 'PDF',
  WORD = 'WORD',
  EXCEL = 'EXCEL',
  PPT = 'PPT',
  IMAGE = 'IMAGE',
  VIDEO = 'VIDEO',
  AUDIO = 'AUDIO',
  ZIP = 'ZIP',
  OTHER = 'OTHER'
}

/**
 * 文档状态枚举
 */
export enum DocumentStatus {
  DRAFT = 'DRAFT',
  UNDER_REVIEW = 'UNDER_REVIEW',
  APPROVED = 'APPROVED',
  ARCHIVED = 'ARCHIVED',
  DEPRECATED = 'DEPRECATED'
}

/**
 * 文档接口
 */
export interface Document {
  id: number
  name: string
  projectId: number
  projectName?: string
  fileType: FileType
  size: number // 文件大小（字节）
  version: string // 版本号
  tags: string[]
  author: {
    id: number
    name: string
    avatar?: string
  }
  status: DocumentStatus
  url: string // 文件URL
  previewUrl?: string // 预览URL
  description?: string
  folderId?: number // 所属文件夹
  folderPath?: string // 文件夹路径
  downloadCount: number
  viewCount: number
  priority?: number // 优先级 1-低，2-中，3-高
  categoryId?: number // 分类ID
  contentType?: string // 内容类型 markdown, html, rich_text
  reviewer?: {
    id: number
    name: string
    avatar?: string
  }
  publishedAt?: string // 发布时间
  archivedAt?: string // 归档时间
  isTemplate?: boolean // 是否为模板
  createdAt: string
  updatedAt: string
}

/**
 * 文档查询参数
 */
export interface DocumentQueryParams {
  page: number
  pageSize: number
  keyword?: string // 搜索关键词
  projectId?: number
  folderId?: number
  fileType?: FileType
  tags?: string[]
  authorId?: number
  status?: DocumentStatus
  categoryId?: number
  priority?: number
  isTemplate?: boolean
  startDate?: string // 创建时间范围
  endDate?: string
  sort?: 'name' | 'size' | 'createdAt' | 'updatedAt' | 'downloadCount' | 'viewCount' | 'priority'
  order?: 'asc' | 'desc'
}

/**
 * 文档版本接口
 */
export interface DocumentVersion {
  id: number
  documentId: number
  version: string
  title: string
  size: number
  url: string
  changes: string // 变更说明
  contentType: string // 内容类型
  isCurrent: boolean // 是否为当前版本
  contentHash?: string // 内容哈希值
  author: {
    id: number
    name: string
    avatar?: string
  }
  createdAt: string
}

/**
 * 文档上传参数
 */
export interface DocumentUploadParams {
  projectId: number
  folderId?: number
  categoryId?: number
  tags?: string[]
  description?: string
  status?: DocumentStatus
  priority?: number
  isTemplate?: boolean
  contentType?: string
}

/**
 * 文件夹接口
 */
export interface DocumentFolder {
  id: number
  name: string
  parentId?: number
  projectId: number
  path: string // 完整路径
  level: number // 层级
  documentCount: number
  children?: DocumentFolder[]
  createdAt: string
  updatedAt: string
}

/**
 * 文档评论接口
 */
export interface DocumentComment {
  id: number
  documentId: number
  content: string
  author: {
    id: number
    name: string
    avatar?: string
  }
  parentId?: number // 父评论ID（用于回复）
  replies?: DocumentComment[]
  createdAt: string
  updatedAt: string
}

/**
 * 文档统计信息
 */
export interface DocumentStats {
  totalCount: number
  totalSize: number
  fileTypeDistribution: {
    type: FileType
    count: number
    size: number
  }[]
  recentUploads: Document[]
  popularDocuments: Document[]
}

/**
 * 视图模式
 */
export type ViewMode = 'list' | 'grid' | 'tree'

/**
 * 创建文档请求参数
 */
export interface CreateDocumentRequest {
  title: string
  content: string
  contentType?: string
  summary?: string
  type: string
  categoryId?: number
  tags?: string[]
  isTemplate?: boolean
  projectId: number
  folderId?: number
  priority?: number
}

/**
 * 更新文档请求参数
 */
export interface UpdateDocumentRequest {
  title?: string
  content?: string
  contentType?: string
  summary?: string
  type?: string
  categoryId?: number
  tags?: string[]
  status?: DocumentStatus
  reviewerId?: number
  changelog?: string
  folderId?: number
  priority?: number
}