/**
 * 文档管理 API
 */
import { get, post, put, del } from '../request'
import type {
  Document,
  DocumentQueryParams,
  DocumentVersion,
  DocumentFolder,
  DocumentComment,
  DocumentStats,
  DocumentUploadParams,
  CreateDocumentRequest,
  UpdateDocumentRequest
} from '@/types/document'
import type { PageResult } from '@/types/global'

/**
 * 获取文档列表
 */
export function getDocumentList(params: DocumentQueryParams) {
  return get<PageResult<Document>>('/documents', { params })
}

/**
 * 创建文档
 */
export function createDocument(projectId: number, data: CreateDocumentRequest) {
  return post<Document>(`/projects/${projectId}/documents`, data)
}

/**
 * 获取文档详情
 */
export function getDocumentDetail(id: number) {
  return get<Document>(`/documents/${id}`)
}

/**
 * 更新文档
 */
export function updateDocument(id: number, data: UpdateDocumentRequest) {
  return put<Document>(`/documents/${id}`, data)
}

/**
 * 删除文档
 */
export function deleteDocument(id: number) {
  return del(`/documents/${id}`)
}

/**
 * 上传文档
 */
export function uploadDocument(formData: FormData) {
  return post<Document>('/documents/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    timeout: 300000 // 5分钟超时
  })
}

/**
 * 下载文档
 */
export function downloadDocument(id: number) {
  return get<Blob>(`/documents/${id}/download`, {
    responseType: 'blob'
  })
}

/**
 * 批量删除文档
 */
export function batchDeleteDocuments(ids: number[]) {
  return post('/documents/batch-delete', { ids })
}

/**
 * 更新文档信息
 */
export function updateDocumentInfo(id: number, data: Partial<Document>) {
  return put<Document>(`/documents/${id}`, data)
}

/**
 * 更新文档标签
 */
export function updateDocumentTags(id: number, tags: string[]) {
  return put<Document>(`/documents/${id}/tags`, { tags })
}

/**
 * 获取文档版本历史
 */
export function getDocumentVersions(id: number) {
  return get<DocumentVersion[]>(`/documents/${id}/versions`)
}

/**
 * 上传文档新版本
 */
export function uploadDocumentVersion(id: number, formData: FormData) {
  return post<DocumentVersion>(`/documents/${id}/versions`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    timeout: 300000
  })
}

/**
 * 回滚到指定版本
 */
export function rollbackToVersion(documentId: number, versionId: number) {
  return post<Document>(`/documents/${documentId}/versions/${versionId}/rollback`)
}

/**
 * 获取文档评论
 */
export function getDocumentComments(id: number) {
  return get<DocumentComment[]>(`/documents/${id}/comments`)
}

/**
 * 添加文档评论
 */
export function addDocumentComment(id: number, data: { content: string; parentId?: number }) {
  return post<DocumentComment>(`/documents/${id}/comments`, data)
}

/**
 * 删除文档评论
 */
export function deleteDocumentComment(documentId: number, commentId: number) {
  return del(`/documents/${documentId}/comments/${commentId}`)
}

/**
 * 获取文件夹树
 */
export function getFolderTree(projectId?: number) {
  return get<DocumentFolder[]>('/documents/folders', { params: { projectId } })
}

/**
 * 创建文件夹
 */
export function createFolder(data: { name: string; parentId?: number; projectId: number }) {
  return post<DocumentFolder>('/documents/folders', data)
}

/**
 * 更新文件夹
 */
export function updateFolder(id: number, data: { name: string }) {
  return put<DocumentFolder>(`/documents/folders/${id}`, data)
}

/**
 * 删除文件夹
 */
export function deleteFolder(id: number) {
  return del(`/documents/folders/${id}`)
}

/**
 * 移动文档到文件夹
 */
export function moveDocument(id: number, folderId?: number) {
  return put(`/documents/${id}/move`, { folderId })
}

/**
 * 获取文档统计信息
 */
export function getDocumentStats(projectId?: number) {
  return get<DocumentStats>('/documents/stats', { params: { projectId } })
}

/**
 * 获取标签列表
 */
export function getTagList(projectId?: number) {
  return get<string[]>('/documents/tags', { params: { projectId } })
}

/**
 * 分享文档（生成分享链接）
 */
export function shareDocument(id: number, data: { expireAt?: string; password?: string }) {
  return post<{ shareUrl: string; shareCode: string }>(`/documents/${id}/share`, data)
}

/**
 * 复制文档
 */
export function copyDocument(id: number, data: { projectId?: number; folderId?: number }) {
  return post<Document>(`/documents/${id}/copy`, data)
}