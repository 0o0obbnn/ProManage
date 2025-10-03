/**
 * 文档状态管理
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type {
  Document,
  DocumentQueryParams,
  DocumentVersion,
  DocumentFolder,
  DocumentComment,
  DocumentStats,
  ViewMode,
  CreateDocumentRequest,
  UpdateDocumentRequest
} from '@/types/document'
import type { PageResult } from '@/types/global'
import * as documentApi from '@/api/modules/document'
import { message } from 'ant-design-vue'

export const useDocumentStore = defineStore('document', () => {
  // State
  const documentList = ref<Document[]>([])
  const currentDocument = ref<Document | null>(null)
  const versions = ref<DocumentVersion[]>([])
  const comments = ref<DocumentComment[]>([])
  const folders = ref<DocumentFolder[]>([])
  const tags = ref<string[]>([])
  const stats = ref<DocumentStats | null>(null)

  const loading = ref(false)
  const uploading = ref(false)
  const downloadingIds = ref<Set<number>>(new Set())

  const pagination = ref({
    page: 1,
    pageSize: 20,
    total: 0,
    totalPages: 0
  })

  const queryParams = ref<Partial<DocumentQueryParams>>({
    keyword: '',
    projectId: undefined,
    folderId: undefined,
    fileType: undefined,
    tags: [],
    status: undefined,
    sort: 'createdAt',
    order: 'desc'
  })

  const viewMode = ref<ViewMode>('list')
  const selectedDocumentIds = ref<number[]>([])

  // Getters
  const hasDocuments = computed(() => documentList.value.length > 0)
  const selectedDocuments = computed(() =>
    documentList.value.filter(doc => selectedDocumentIds.value.includes(doc.id))
  )
  const selectedCount = computed(() => selectedDocumentIds.value.length)

  /**
   * 获取文档列表
   */
  const fetchDocuments = async (params?: Partial<DocumentQueryParams>) => {
    try {
      loading.value = true

      const mergedParams: DocumentQueryParams = {
        page: pagination.value.page,
        pageSize: pagination.value.pageSize,
        ...queryParams.value,
        ...params
      }

      const result = await documentApi.getDocumentList(mergedParams)

      documentList.value = result.list
      pagination.value.total = result.total
      pagination.value.page = result.page
      pagination.value.pageSize = result.pageSize
      pagination.value.totalPages = result.totalPages

      return result
    } catch (error) {
      console.error('Fetch documents failed:', error)
      message.error('获取文档列表失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 创建文档
   */
  const createDocument = async (projectId: number, data: CreateDocumentRequest) => {
    try {
      const document = await documentApi.createDocument(projectId, data)
      message.success('文档创建成功')

      // 刷新列表
      await fetchDocuments()

      return document
    } catch (error) {
      console.error('Create document failed:', error)
      message.error('文档创建失败')
      throw error
    }
  }

  /**
   * 获取文档详情
   */
  const fetchDocumentDetail = async (id: number) => {
    try {
      loading.value = true
      const document = await documentApi.getDocumentDetail(id)
      currentDocument.value = document
      return document
    } catch (error) {
      console.error('Fetch document detail failed:', error)
      message.error('获取文档详情失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新文档
   */
  const updateDocument = async (id: number, data: UpdateDocumentRequest) => {
    try {
      const document = await documentApi.updateDocument(id, data)

      // 更新列表中的文档
      const index = documentList.value.findIndex(doc => doc.id === id)
      if (index !== -1) {
        documentList.value[index] = document
      }

      // 更新当前文档
      if (currentDocument.value?.id === id) {
        currentDocument.value = document
      }

      message.success('文档更新成功')
      return document
    } catch (error) {
      console.error('Update document failed:', error)
      message.error('文档更新失败')
      throw error
    }
  }

  /**
   * 上传文档
   */
  const uploadDocument = async (file: File, params: any) => {
    try {
      uploading.value = true

      const formData = new FormData()
      formData.append('file', file)

      Object.keys(params).forEach(key => {
        const value = params[key]
        if (value !== undefined && value !== null) {
          if (Array.isArray(value)) {
            formData.append(key, JSON.stringify(value))
          } else {
            formData.append(key, value)
          }
        }
      })

      const document = await documentApi.uploadDocument(formData)
      message.success('文档上传成功')

      // 刷新列表
      await fetchDocuments()

      return document
    } catch (error) {
      console.error('Upload document failed:', error)
      message.error('文档上传失败')
      throw error
    } finally {
      uploading.value = false
    }
  }

  /**
   * 下载文档
   */
  const downloadDocument = async (id: number, name: string) => {
    try {
      downloadingIds.value.add(id)

      const blob = await documentApi.downloadDocument(id)

      // 创建下载链接
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = name
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)

      message.success('文档下载成功')
    } catch (error) {
      console.error('Download document failed:', error)
      message.error('文档下载失败')
      throw error
    } finally {
      downloadingIds.value.delete(id)
    }
  }

  /**
   * 删除文档
   */
  const deleteDocument = async (id: number) => {
    try {
      await documentApi.deleteDocument(id)
      message.success('文档删除成功')

      // 从列表中移除
      documentList.value = documentList.value.filter(doc => doc.id !== id)

      // 更新分页总数
      pagination.value.total -= 1

      return true
    } catch (error) {
      console.error('Delete document failed:', error)
      message.error('文档删除失败')
      throw error
    }
  }

  /**
   * 批量删除文档
   */
  const batchDeleteDocuments = async (ids: number[]) => {
    try {
      await documentApi.batchDeleteDocuments(ids)
      message.success(`成功删除 ${ids.length} 个文档`)

      // 刷新列表
      await fetchDocuments()

      // 清空选择
      selectedDocumentIds.value = []

      return true
    } catch (error) {
      console.error('Batch delete documents failed:', error)
      message.error('批量删除失败')
      throw error
    }
  }

  /**
   * 更新文档标签
   */
  const updateDocumentTags = async (id: number, tags: string[]) => {
    try {
      const document = await documentApi.updateDocumentTags(id, tags)

      // 更新列表中的文档
      const index = documentList.value.findIndex(doc => doc.id === id)
      if (index !== -1) {
        documentList.value[index] = document
      }

      message.success('标签更新成功')
      return document
    } catch (error) {
      console.error('Update document tags failed:', error)
      message.error('标签更新失败')
      throw error
    }
  }

  /**
   * 获取版本历史
   */
  const fetchVersions = async (id: number) => {
    try {
      const result = await documentApi.getDocumentVersions(id)
      versions.value = result
      return result
    } catch (error) {
      console.error('Fetch versions failed:', error)
      message.error('获取版本历史失败')
      throw error
    }
  }

  /**
   * 获取评论
   */
  const fetchComments = async (id: number) => {
    try {
      const result = await documentApi.getDocumentComments(id)
      comments.value = result
      return result
    } catch (error) {
      console.error('Fetch comments failed:', error)
      message.error('获取评论失败')
      throw error
    }
  }

  /**
   * 添加评论
   */
  const addComment = async (id: number, content: string, parentId?: number) => {
    try {
      const comment = await documentApi.addDocumentComment(id, { content, parentId })
      comments.value.unshift(comment)
      message.success('评论添加成功')
      return comment
    } catch (error) {
      console.error('Add comment failed:', error)
      message.error('评论添加失败')
      throw error
    }
  }

  /**
   * 删除评论
   */
  const deleteComment = async (documentId: number, commentId: number) => {
    try {
      await documentApi.deleteDocumentComment(documentId, commentId)
      comments.value = comments.value.filter(c => c.id !== commentId)
      message.success('评论删除成功')
    } catch (error) {
      console.error('Delete comment failed:', error)
      message.error('评论删除失败')
      throw error
    }
  }

  /**
   * 获取文件夹树
   */
  const fetchFolders = async (projectId?: number) => {
    try {
      const result = await documentApi.getFolderTree(projectId)
      folders.value = result
      return result
    } catch (error) {
      console.error('Fetch folders failed:', error)
      message.error('获取文件夹失败')
      throw error
    }
  }

  /**
   * 创建文件夹
   */
  const createFolder = async (name: string, parentId?: number, projectId?: number) => {
    try {
      const folder = await documentApi.createFolder({
        name,
        parentId,
        projectId: projectId || 0
      })
      message.success('文件夹创建成功')

      // 刷新文件夹树
      await fetchFolders(projectId)

      return folder
    } catch (error) {
      console.error('Create folder failed:', error)
      message.error('文件夹创建失败')
      throw error
    }
  }

  /**
   * 获取标签列表
   */
  const fetchTags = async (projectId?: number) => {
    try {
      const result = await documentApi.getTagList(projectId)
      tags.value = result
      return result
    } catch (error) {
      console.error('Fetch tags failed:', error)
      throw error
    }
  }

  /**
   * 获取统计信息
   */
  const fetchStats = async (projectId?: number) => {
    try {
      const result = await documentApi.getDocumentStats(projectId)
      stats.value = result
      return result
    } catch (error) {
      console.error('Fetch stats failed:', error)
      throw error
    }
  }

  /**
   * 移动文档
   */
  const moveDocument = async (id: number, folderId?: number) => {
    try {
      await documentApi.moveDocument(id, folderId)
      message.success('文档移动成功')
      await fetchDocuments()
    } catch (error) {
      console.error('Move document failed:', error)
      message.error('文档移动失败')
      throw error
    }
  }

  /**
   * 分享文档
   */
  const shareDocument = async (id: number, expireAt?: string, password?: string) => {
    try {
      const result = await documentApi.shareDocument(id, { expireAt, password })
      message.success('分享链接生成成功')
      return result
    } catch (error) {
      console.error('Share document failed:', error)
      message.error('分享失败')
      throw error
    }
  }

  /**
   * 设置查询参数
   */
  const setQueryParams = (params: Partial<DocumentQueryParams>) => {
    queryParams.value = { ...queryParams.value, ...params }
  }

  /**
   * 重置查询参数
   */
  const resetQueryParams = () => {
    queryParams.value = {
      keyword: '',
      projectId: undefined,
      folderId: undefined,
      fileType: undefined,
      tags: [],
      status: undefined,
      sort: 'createdAt',
      order: 'desc'
    }
  }

  /**
   * 设置分页
   */
  const setPagination = (page: number, pageSize?: number) => {
    pagination.value.page = page
    if (pageSize) {
      pagination.value.pageSize = pageSize
    }
  }

  /**
   * 切换视图模式
   */
  const setViewMode = (mode: ViewMode) => {
    viewMode.value = mode
    localStorage.setItem('document_view_mode', mode)
  }

  /**
   * 选择文档
   */
  const selectDocument = (id: number) => {
    if (!selectedDocumentIds.value.includes(id)) {
      selectedDocumentIds.value.push(id)
    }
  }

  /**
   * 取消选择文档
   */
  const unselectDocument = (id: number) => {
    selectedDocumentIds.value = selectedDocumentIds.value.filter(docId => docId !== id)
  }

  /**
   * 切换文档选择
   */
  const toggleDocumentSelect = (id: number) => {
    if (selectedDocumentIds.value.includes(id)) {
      unselectDocument(id)
    } else {
      selectDocument(id)
    }
  }

  /**
   * 全选
   */
  const selectAll = () => {
    selectedDocumentIds.value = documentList.value.map(doc => doc.id)
  }

  /**
   * 清空选择
   */
  const clearSelection = () => {
    selectedDocumentIds.value = []
  }

  /**
   * 恢复视图模式
   */
  const restoreViewMode = () => {
    const savedMode = localStorage.getItem('document_view_mode') as ViewMode
    if (savedMode) {
      viewMode.value = savedMode
    }
  }

  return {
    // State
    documentList,
    currentDocument,
    versions,
    comments,
    folders,
    tags,
    stats,
    loading,
    uploading,
    downloadingIds,
    pagination,
    queryParams,
    viewMode,
    selectedDocumentIds,
    // Getters
    hasDocuments,
    selectedDocuments,
    selectedCount,
    // Actions
    fetchDocuments,
    createDocument,
    fetchDocumentDetail,
    updateDocument,
    uploadDocument,
    downloadDocument,
    deleteDocument,
    batchDeleteDocuments,
    updateDocumentTags,
    fetchVersions,
    fetchComments,
    addComment,
    deleteComment,
    fetchFolders,
    createFolder,
    fetchTags,
    fetchStats,
    moveDocument,
    shareDocument,
    setQueryParams,
    resetQueryParams,
    setPagination,
    setViewMode,
    selectDocument,
    unselectDocument,
    toggleDocumentSelect,
    selectAll,
    clearSelection,
    restoreViewMode
  }
})