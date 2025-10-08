/**
 * 用户状态管理
 * 已修复: 无限权限请求循环问题
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo } from '@/types/global'
import * as authApi from '@/api/modules/auth'
import { getToken, setToken, getRefreshToken, setRefreshToken, clearAuth } from '@/utils/auth'

export const useUserStore = defineStore('user', () => {
  // State
  const userInfo = ref<UserInfo | null>(null)
  const token = ref<string>('')
  const refreshToken = ref<string>('')
  const userPermissions = ref<string[]>([]) // 新增：存储用户权限列表

  // Getters
  const isLoggedIn = computed(() => !!token.value)
  const userName = computed(() => userInfo.value?.realName || userInfo.value?.username || '')
  const userRole = computed(() => userInfo.value?.roles?.[0]?.roleCode || '')
  const userRoles = computed(() => userInfo.value?.roles || [])
  const permissions = computed(() => {
    // 优先使用从后端获取的真实权限列表
    if (userPermissions.value.length > 0) {
      return userPermissions.value
    }
    
    // 如果还没有从后端获取权限，则使用基于角色的推断（向后兼容）
    if (!userInfo.value?.roles || userInfo.value.roles.length === 0) {
      return []
    }
    
    // 临时解决方案：根据角色代码推断权限
    const rolePermissions: string[] = []
    userInfo.value.roles.forEach(role => {
      // 超级管理员拥有所有权限
      if (role.roleCode === 'ROLE_SUPER_ADMIN') {
        // 添加所有文档相关权限
        rolePermissions.push(
          'document:view', 'document:list', 'document:create', 
          'document:update', 'document:delete', 'document:publish',
          'document:archive', 'document:view_versions', 'document:create_version'
        )
        // 添加其他常用权限
        rolePermissions.push(
          'project:view', 'project:list', 'project:create',
          'project:update', 'project:delete', 'project:archive',
          'user:view', 'user:list', 'user:create',
          'user:update', 'user:delete', 'profile:view',
          'profile:update', 'profile:change_password', 'profile:upload_avatar',
          'role:view', 'role:list', 'role:create',
          'role:update', 'role:delete', 'role:assign_permission',
          'permission:view', 'permission:list', 'permission:create',
          'permission:update', 'permission:delete',
          'system:view_logs', 'system:monitor', 'system:clear_cache',
          'system:config', 'user:reset_password', 'user:assign_role',
          'project:add_member', 'project:remove_member', 'project:view_members'
        )
      }
      // 项目经理权限
      else if (role.roleCode === 'ROLE_PROJECT_MANAGER') {
        rolePermissions.push(
          'document:view', 'document:list', 'document:create', 
          'document:update', 'document:delete', 'document:publish',
          'document:archive', 'document:view_versions', 'document:create_version',
          'project:view', 'project:list', 'project:create',
          'project:update', 'project:archive',
          'project:add_member', 'project:remove_member', 'project:view_members',
          'profile:view', 'profile:update', 'profile:change_password',
          'profile:upload_avatar'
        )
      }
      // 开发人员权限
      else if (role.roleCode === 'ROLE_DEVELOPER') {
        rolePermissions.push(
          'document:view', 'document:list', 'document:create', 
          'document:update', 'document:view_versions', 'document:create_version',
          'project:view', 'project:list', 'project:view_members',
          'profile:view', 'profile:update', 'profile:change_password',
          'profile:upload_avatar'
        )
      }
      // 其他角色可以在这里添加
    })
    
    return rolePermissions
  })

  /**
   * 检查是否有指定权限
   */
  const hasPermission = (permission: string): boolean => {
    // 超级管理员拥有所有权限
    if (userInfo.value?.roles?.some(role => role.roleCode === 'ROLE_SUPER_ADMIN')) {
      return true
    }
    
    return permissions.value.includes(permission)
  }

  /**
   * 用户登录
   */
  const login = async (username: string, password: string, rememberMe: boolean = false) => {
    try {
      const response = await authApi.login({
        username,
        password,
        rememberMe
      })

      token.value = response.token
      refreshToken.value = response.refreshToken
      userInfo.value = response.userInfo

      // 先持久化 token，确保后续API调用可以使用
      setToken(response.token)
      setRefreshToken(response.refreshToken)
      localStorage.setItem('userInfo', JSON.stringify(response.userInfo))

      // 登录成功后获取用户权限（此时token已保存，请求拦截器可以获取）
      await fetchUserPermissions()

      return response
    } catch (error) {
      console.error('Login failed:', error)
      throw error
    }
  }

  /**
   * 用户登出
   */
  const logout = async () => {
    try {
      await authApi.logout()
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      // 清除状态
      token.value = ''
      refreshToken.value = ''
      userInfo.value = null
      userPermissions.value = []

      // 使用auth工具清除本地存储
      clearAuth()
    }
  }

  /**
   * 刷新用户信息
   */
  const fetchUserInfo = async () => {
    try {
      const user = await authApi.getCurrentUser()
      userInfo.value = user
      localStorage.setItem('userInfo', JSON.stringify(user))
      
      // 获取用户权限
      await fetchUserPermissions()
      
      return user
    } catch (error) {
      console.error('Fetch user info failed:', error)
      throw error
    }
  }

  /**
   * 获取用户权限列表
   */
  const fetchUserPermissions = async () => {
    try {
      const permissions = await authApi.getUserPermissions()
      userPermissions.value = permissions
      return permissions
    } catch (error) {
      console.error('Fetch user permissions failed:', error)
      // 如果获取权限失败，userPermissions保持为空，将使用基于角色的推断
      throw error
    }
  }

  /**
   * 从本地存储恢复状态
   */
  const restoreFromLocalStorage = () => {
    const storedToken = getToken()
    const storedRefreshToken = getRefreshToken()
    const storedUserInfo = localStorage.getItem('userInfo')

    if (storedToken) {
      token.value = storedToken
    }
    if (storedRefreshToken) {
      refreshToken.value = storedRefreshToken
    }
    if (storedUserInfo) {
      try {
        userInfo.value = JSON.parse(storedUserInfo)
        // 注意: 不在恢复状态时获取权限,避免无限循环
        // 权限应该在登录成功后获取,或由路由守卫在需要时主动获取
      } catch (error) {
        console.error('Parse user info failed:', error)
      }
    }
  }

  return {
    // State
    userInfo,
    token,
    refreshToken,
    userPermissions,
    // Getters
    isLoggedIn,
    userName,
    userRole,
    userRoles,
    permissions,
    // Actions
    hasPermission,
    login,
    logout,
    fetchUserInfo,
    fetchUserPermissions,
    restoreFromLocalStorage
  }
})