/**
 * Project Store 单元测试
 */
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useProjectStore } from '../project'
import type { Project } from '@/types/project'
import * as projectApi from '@/api/modules/project'

// Mock API
vi.mock('@/api/modules/project')
vi.mock('ant-design-vue', () => ({
  message: {
    success: vi.fn(),
    error: vi.fn()
  }
}))

describe('Project Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  const mockProject: Project = {
    id: 1,
    name: '测试项目',
    code: 'TEST-001',
    description: '测试项目描述',
    status: 1,
    priority: 2,
    progress: 50,
    ownerId: 1,
    ownerName: '张三',
    createdAt: '2025-01-01T00:00:00Z',
    updatedAt: '2025-01-01T00:00:00Z'
  }

  const mockProjectList = [mockProject]

  it('should initialize with default state', () => {
    const store = useProjectStore()

    expect(store.projectList).toEqual([])
    expect(store.currentProject).toBeNull()
    expect(store.loading).toBe(false)
    expect(store.pagination.page).toBe(1)
    expect(store.pagination.pageSize).toBe(20)
  })

  it('should fetch projects successfully', async () => {
    const store = useProjectStore()
    const mockResponse = {
      list: mockProjectList,
      total: 1,
      page: 1,
      pageSize: 20,
      totalPages: 1
    }

    vi.mocked(projectApi.getProjectList).mockResolvedValue(mockResponse)

    await store.fetchProjects()

    expect(store.projectList).toEqual(mockProjectList)
    expect(store.pagination.total).toBe(1)
    expect(store.loading).toBe(false)
  })

  it('should handle fetch projects error', async () => {
    const store = useProjectStore()
    const error = new Error('Network error')

    vi.mocked(projectApi.getProjectList).mockRejectedValue(error)

    await expect(store.fetchProjects()).rejects.toThrow('Network error')
    expect(store.loading).toBe(false)
  })

  it('should fetch project detail successfully', async () => {
    const store = useProjectStore()
    const mockDetail = { ...mockProject, members: [] }

    vi.mocked(projectApi.getProjectDetail).mockResolvedValue(mockDetail)

    await store.fetchProjectDetail(1)

    expect(store.currentProject).toEqual(mockDetail)
    expect(store.loading).toBe(false)
  })

  it('should create project successfully', async () => {
    const store = useProjectStore()
    const createData = {
      name: '新项目',
      code: 'NEW-001',
      description: '新项目描述'
    }

    vi.mocked(projectApi.checkProjectCode).mockResolvedValue(false)
    vi.mocked(projectApi.createProject).mockResolvedValue(mockProject)
    vi.mocked(projectApi.getProjectList).mockResolvedValue({
      list: [mockProject],
      total: 1,
      page: 1,
      pageSize: 20,
      totalPages: 1
    })

    const result = await store.createNewProject(createData)

    expect(result).toEqual(mockProject)
    expect(projectApi.createProject).toHaveBeenCalledWith(createData)
  })

  it('should not create project if code exists', async () => {
    const store = useProjectStore()
    const createData = {
      name: '新项目',
      code: 'EXISTING-001',
      description: '新项目描述'
    }

    vi.mocked(projectApi.checkProjectCode).mockResolvedValue(true)

    const result = await store.createNewProject(createData)

    expect(result).toBeNull()
    expect(projectApi.createProject).not.toHaveBeenCalled()
  })

  it('should update project successfully', async () => {
    const store = useProjectStore()
    const updateData = {
      name: '更新后的项目'
    }
    const updatedProject = { ...mockProject, ...updateData }

    vi.mocked(projectApi.updateProject).mockResolvedValue(updatedProject)

    await store.updateProjectInfo(1, updateData)

    expect(projectApi.updateProject).toHaveBeenCalledWith(1, updateData)
  })

  it('should delete project successfully', async () => {
    const store = useProjectStore()
    store.projectList = [mockProject]

    vi.mocked(projectApi.deleteProject).mockResolvedValue(undefined)

    await store.deleteProjectById(1)

    expect(store.projectList).toEqual([])
    expect(projectApi.deleteProject).toHaveBeenCalledWith(1)
  })

  it('should archive project successfully', async () => {
    const store = useProjectStore()

    vi.mocked(projectApi.archiveProject).mockResolvedValue(undefined)
    vi.mocked(projectApi.getProjectList).mockResolvedValue({
      list: [],
      total: 0,
      page: 1,
      pageSize: 20,
      totalPages: 0
    })

    await store.archiveProjectById(1)

    expect(projectApi.archiveProject).toHaveBeenCalledWith(1)
  })

  it('should restore project successfully', async () => {
    const store = useProjectStore()

    vi.mocked(projectApi.restoreProject).mockResolvedValue(undefined)
    vi.mocked(projectApi.getProjectList).mockResolvedValue({
      list: [mockProject],
      total: 1,
      page: 1,
      pageSize: 20,
      totalPages: 1
    })

    await store.restoreProjectById(1)

    expect(projectApi.restoreProject).toHaveBeenCalledWith(1)
  })

  it('should compute hasProjects correctly', () => {
    const store = useProjectStore()

    expect(store.hasProjects).toBe(false)

    store.projectList = [mockProject]
    expect(store.hasProjects).toBe(true)
  })

  it('should compute activeProjects correctly', () => {
    const store = useProjectStore()
    const activeProject = { ...mockProject, status: 1 }
    const completedProject = { ...mockProject, id: 2, status: 2 }

    store.projectList = [activeProject, completedProject]

    expect(store.activeProjects).toEqual([activeProject])
  })

  it('should compute completedProjects correctly', () => {
    const store = useProjectStore()
    const activeProject = { ...mockProject, status: 1 }
    const completedProject = { ...mockProject, id: 2, status: 2 }

    store.projectList = [activeProject, completedProject]

    expect(store.completedProjects).toEqual([completedProject])
  })

  it('should set query params correctly', () => {
    const store = useProjectStore()

    store.setQueryParams({ keyword: '测试', status: 1 })

    expect(store.queryParams.keyword).toBe('测试')
    expect(store.queryParams.status).toBe(1)
  })

  it('should reset query params correctly', () => {
    const store = useProjectStore()

    store.setQueryParams({ keyword: '测试', status: 1 })
    store.resetQueryParams()

    expect(store.queryParams.keyword).toBe('')
    expect(store.queryParams.status).toBeUndefined()
  })

  it('should set pagination correctly', () => {
    const store = useProjectStore()

    store.setPagination(2, 50)

    expect(store.pagination.page).toBe(2)
    expect(store.pagination.pageSize).toBe(50)
  })

  it('should set current project correctly', () => {
    const store = useProjectStore()
    const projectDetail = { ...mockProject, members: [] }

    store.setCurrentProject(projectDetail)

    expect(store.currentProject).toEqual(projectDetail)
  })
})

