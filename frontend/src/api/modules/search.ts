/**
 * 搜索API
 */
import request from '../request'

/**
 * 搜索类型
 */
export enum SearchType {
  ALL = 'all',
  PROJECT = 'project',
  DOCUMENT = 'document',
  TASK = 'task',
  CHANGE = 'change'
}

/**
 * 搜索参数
 */
export interface SearchParams {
  keyword: string
  type?: SearchType
  scope?: string[]
  creatorId?: number
  startDate?: string
  endDate?: string
  page?: number
  pageSize?: number
}

/**
 * 搜索结果项
 */
export interface SearchResultItem {
  id: number
  type: SearchType
  title: string
  content?: string
  description?: string
  url: string
  createdAt: string
  creator?: {
    id: number
    name: string
    avatar?: string
  }
  highlight?: {
    title?: string
    content?: string
  }
}

/**
 * 搜索结果
 */
export interface SearchResult {
  total: number
  list: SearchResultItem[]
  page: number
  pageSize: number
}

/**
 * 搜索建议项
 */
export interface SearchSuggestion {
  id: number
  type: SearchType
  title: string
  url: string
}

/**
 * 全局搜索
 */
export const search = (params: SearchParams) => {
  return request<SearchResult>({
    url: '/api/v1/search',
    method: 'GET',
    params
  })
}

/**
 * 获取搜索建议
 */
export const getSearchSuggestions = (keyword: string) => {
  return request<SearchSuggestion[]>({
    url: '/api/v1/search/suggestions',
    method: 'GET',
    params: { keyword }
  })
}

/**
 * 搜索项目
 */
export const searchProjects = (params: SearchParams) => {
  return request<SearchResult>({
    url: '/api/v1/search/projects',
    method: 'GET',
    params
  })
}

/**
 * 搜索文档
 */
export const searchDocuments = (params: SearchParams) => {
  return request<SearchResult>({
    url: '/api/v1/search/documents',
    method: 'GET',
    params
  })
}

/**
 * 搜索任务
 */
export const searchTasks = (params: SearchParams) => {
  return request<SearchResult>({
    url: '/api/v1/search/tasks',
    method: 'GET',
    params
  })
}

/**
 * 搜索变更请求
 */
export const searchChanges = (params: SearchParams) => {
  return request<SearchResult>({
    url: '/api/v1/search/changes',
    method: 'GET',
    params
  })
}

/**
 * 导出搜索API
 */
export const searchApi = {
  search,
  getSearchSuggestions,
  searchProjects,
  searchDocuments,
  searchTasks,
  searchChanges
}

export default searchApi

