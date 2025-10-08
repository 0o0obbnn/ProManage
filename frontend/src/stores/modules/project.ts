/**
 * 项目状态管理
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { message } from 'ant-design-vue'
import type {
  Project,
  ProjectDetail,
  ProjectQueryParams,
  CreateProjectRequest,
  UpdateProjectRequest,
  ProjectMember,
  AddProjectMemberRequest,
  UpdateProjectMemberRequest,
  ProjectStatistics
} from '@/types/project'
import type { PageResult } from '@/types/global'
import {
  getProjectList,
  getProjectDetail,
  createProject,
  updateProject,
  deleteProject,
  archiveProject,
  restoreProject,
  getProjectMembers,
  addProjectMember,
  updateProjectMemberRole,
  removeProjectMember,
  getProjectStatistics,
  checkProjectCode
} from '@/api/modules/project'

export const useProjectStore = defineStore('project', () => {
  // State
  const projectList = ref<Project[]>([])
  const currentProject = ref<ProjectDetail | null>(null)
  const projectMembers = ref<ProjectMember[]>([])
  const projectStatistics = ref<ProjectStatistics | null>(null)
  
  const loading = ref(false)
  const memberLoading = ref(false)
  
  const pagination = ref({
    page: 1,
    pageSize: 20,
    total: 0,
    totalPages: 0
  })
  
  const queryParams = ref<Partial<ProjectQueryParams>>({
    keyword: '',
    status: undefined,
    priority: undefined,
    type: undefined,
    ownerId: undefined,
    sort: 'updatedAt',
    order: 'desc'
  })

  // Getters
  const hasProjects = computed(() => projectList.value.length > 0)
  
  const activeProjects = computed(() => 
    projectList.value.filter(p => p.status === 1)
  )
  
  const completedProjects = computed(() => 
    projectList.value.filter(p => p.status === 2)
  )
  
  const archivedProjects = computed(() => 
    projectList.value.filter(p => p.status === 3)
  )

  // Actions
  
  /**
   * 获取项目列表
   */
  const fetchProjects = async (params?: Partial<ProjectQueryParams>) => {
    try {
      loading.value = true
      
      const mergedParams = {
        ...queryParams.value,
        ...params,
        page: params?.page || pagination.value.page,
        pageSize: params?.pageSize || pagination.value.pageSize
      }
      
      const response = await getProjectList(mergedParams)
      
      projectList.value = response.list || []
      pagination.value = {
        page: response.page || 1,
        pageSize: response.pageSize || 20,
        total: response.total || 0,
        totalPages: response.totalPages || 0
      }
      
      return response
    } catch (error) {
      console.error('Fetch projects failed:', error)
      message.error('获取项目列表失败')
      throw error
    } finally {
      loading.value = false
    }
  }
  
  /**
   * 获取项目详情
   */
  const fetchProjectDetail = async (id: number) => {
    try {
      loading.value = true
      const project = await getProjectDetail(id)
      currentProject.value = project
      return project
    } catch (error) {
      console.error('Fetch project detail failed:', error)
      message.error('获取项目详情失败')
      throw error
    } finally {
      loading.value = false
    }
  }
  
  /**
   * 创建项目
   */
  const createNewProject = async (data: CreateProjectRequest) => {
    try {
      loading.value = true
      
      // 检查项目编码是否存在
      const codeExists = await checkProjectCode(data.code)
      if (codeExists) {
        message.error('项目编码已存在')
        return null
      }
      
      const project = await createProject(data)
      message.success('项目创建成功')
      
      // 刷新项目列表
      await fetchProjects()
      
      return project
    } catch (error) {
      console.error('Create project failed:', error)
      message.error('创建项目失败')
      throw error
    } finally {
      loading.value = false
    }
  }
  
  /**
   * 更新项目
   */
  const updateProjectInfo = async (id: number, data: UpdateProjectRequest) => {
    try {
      loading.value = true
      const project = await updateProject(id, data)
      message.success('项目更新成功')
      
      // 更新当前项目
      if (currentProject.value && currentProject.value.id === id) {
        currentProject.value = { ...currentProject.value, ...project }
      }
      
      // 更新列表中的项目
      const index = projectList.value.findIndex(p => p.id === id)
      if (index !== -1) {
        projectList.value[index] = { ...projectList.value[index], ...project }
      }
      
      return project
    } catch (error) {
      console.error('Update project failed:', error)
      message.error('更新项目失败')
      throw error
    } finally {
      loading.value = false
    }
  }
  
  /**
   * 删除项目
   */
  const deleteProjectById = async (id: number) => {
    try {
      loading.value = true
      await deleteProject(id)
      message.success('项目删除成功')
      
      // 从列表中移除
      projectList.value = projectList.value.filter(p => p.id !== id)
      
      // 清空当前项目
      if (currentProject.value && currentProject.value.id === id) {
        currentProject.value = null
      }
    } catch (error) {
      console.error('Delete project failed:', error)
      message.error('删除项目失败')
      throw error
    } finally {
      loading.value = false
    }
  }
  
  /**
   * 归档项目
   */
  const archiveProjectById = async (id: number) => {
    try {
      loading.value = true
      await archiveProject(id)
      message.success('项目已归档')
      
      // 刷新项目列表
      await fetchProjects()
    } catch (error) {
      console.error('Archive project failed:', error)
      message.error('归档项目失败')
      throw error
    } finally {
      loading.value = false
    }
  }
  
  /**
   * 恢复项目
   */
  const restoreProjectById = async (id: number) => {
    try {
      loading.value = true
      await restoreProject(id)
      message.success('项目已恢复')
      
      // 刷新项目列表
      await fetchProjects()
    } catch (error) {
      console.error('Restore project failed:', error)
      message.error('恢复项目失败')
      throw error
    } finally {
      loading.value = false
    }
  }
  
  /**
   * 获取项目成员
   */
  const fetchProjectMembers = async (projectId: number) => {
    try {
      memberLoading.value = true
      const membersResult = await getProjectMembers(projectId)
      projectMembers.value = membersResult.list
      return membersResult.list
    } catch (error) {
      console.error('Fetch project members failed:', error)
      message.error('获取项目成员失败')
      throw error
    } finally {
      memberLoading.value = false
    }
  }
  
  /**
   * 添加项目成员
   */
  const addMember = async (projectId: number, data: AddProjectMemberRequest) => {
    try {
      memberLoading.value = true
      await addProjectMember(projectId, data.userId, data.roleId)
      message.success('成员添加成功')
      
      // 刷新成员列表
      await fetchProjectMembers(projectId)
    } catch (error) {
      console.error('Add project member failed:', error)
      message.error('添加成员失败')
      throw error
    } finally {
      memberLoading.value = false
    }
  }
  
  /**
   * 更新项目成员角色
   */
  const updateMemberRole = async (
    projectId: number,
    userId: number,
    data: UpdateProjectMemberRequest
  ) => {
    try {
      memberLoading.value = true
      await updateProjectMemberRole(projectId, userId, data.roleId)
      message.success('成员角色更新成功')
      
      // 刷新成员列表
      await fetchProjectMembers(projectId)
    } catch (error) {
      console.error('Update project member failed:', error)
      message.error('更新成员角色失败')
      throw error
    } finally {
      memberLoading.value = false
    }
  }
  
  /**
   * 移除项目成员
   */
  const removeMember = async (projectId: number, userId: number) => {
    try {
      memberLoading.value = true
      await removeProjectMember(projectId, userId)
      message.success('成员移除成功')
      
      // 刷新成员列表
      await fetchProjectMembers(projectId)
    } catch (error) {
      console.error('Remove project member failed:', error)
      message.error('移除成员失败')
      throw error
    } finally {
      memberLoading.value = false
    }
  }
  
  /**
   * 获取项目统计信息
   */
  const fetchProjectStatistics = async (projectId: number) => {
    try {
      const stats = await getProjectStatistics(projectId)
      projectStatistics.value = stats
      return stats
    } catch (error) {
      console.error('Fetch project statistics failed:', error)
      message.error('获取项目统计失败')
      throw error
    }
  }
  
  /**
   * 设置查询参数
   */
  const setQueryParams = (params: Partial<ProjectQueryParams>) => {
    queryParams.value = { ...queryParams.value, ...params }
  }
  
  /**
   * 重置查询参数
   */
  const resetQueryParams = () => {
    queryParams.value = {
      keyword: '',
      status: undefined,
      priority: undefined,
      type: undefined,
      ownerId: undefined,
      sort: 'updatedAt',
      order: 'desc'
    }
  }
  
  /**
   * 设置分页参数
   */
  const setPagination = (page: number, pageSize?: number) => {
    pagination.value.page = page
    if (pageSize) {
      pagination.value.pageSize = pageSize
    }
  }
  
  /**
   * 设置当前项目
   */
  const setCurrentProject = (project: ProjectDetail | null) => {
    currentProject.value = project
  }

  return {
    // State
    projectList,
    currentProject,
    projectMembers,
    projectStatistics,
    loading,
    memberLoading,
    pagination,
    queryParams,
    // Getters
    hasProjects,
    activeProjects,
    completedProjects,
    archivedProjects,
    // Actions
    fetchProjects,
    fetchProjectDetail,
    createNewProject,
    updateProjectInfo,
    deleteProjectById,
    archiveProjectById,
    restoreProjectById,
    fetchProjectMembers,
    addMember,
    updateMemberRole,
    removeMember,
    fetchProjectStatistics,
    setQueryParams,
    resetQueryParams,
    setPagination,
    setCurrentProject
  }
})

