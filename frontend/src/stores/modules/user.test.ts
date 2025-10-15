import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useUserStore } from './user'
import * as authApi from '@/api/modules/auth'
import * as authUtils from '@/utils/auth'

// Mock auth API
vi.mock('@/api/modules/auth', () => ({
  default: {
    login: vi.fn(),
    logout: vi.fn(),
    getCurrentUser: vi.fn(),
    getUserPermissions: vi.fn()
  },
  login: vi.fn(),
  logout: vi.fn(),
  getCurrentUser: vi.fn(),
  getUserPermissions: vi.fn()
}))

// Mock auth utils
vi.mock('@/utils/auth', () => ({
  getToken: vi.fn(() => 'mock-token'),
  setToken: vi.fn(),
  getRefreshToken: vi.fn(() => 'mock-refresh-token'),
  setRefreshToken: vi.fn(),
  clearAuth: vi.fn()
}))

// Mock localStorage
const localStorageMock = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn()
}
Object.defineProperty(window, 'localStorage', {
  value: localStorageMock
})

describe('UserStore', () => {
  let userStore: any

  beforeEach(() => {
    // Create a fresh pinia instance
    setActivePinia(createPinia())
    userStore = useUserStore()
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.resetAllMocks()
  })

  describe('Initial State', () => {
    it('has correct initial state', () => {
      expect(userStore.userInfo).toBeNull()
      expect(userStore.token).toBe('')
      expect(userStore.refreshToken).toBe('')
      expect(userStore.userPermissions).toEqual([])
    })
  })

  describe('Getters', () => {
    it('computes isLoggedIn correctly', () => {
      expect(userStore.isLoggedIn).toBe(false)
      
      userStore.token = 'some-token'
      expect(userStore.isLoggedIn).toBe(true)
    })

    it('computes userName correctly', () => {
      expect(userStore.userName).toBe('')
      
      userStore.userInfo = {
        realName: 'John Doe',
        username: 'johndoe'
      }
      expect(userStore.userName).toBe('John Doe')
      
      userStore.userInfo = {
        username: 'johndoe'
      }
      expect(userStore.userName).toBe('johndoe')
    })

    it('computes userRole correctly', () => {
      expect(userStore.userRole).toBe('')
      
      userStore.userInfo = {
        roles: [{ roleCode: 'ROLE_ADMIN' }]
      }
      expect(userStore.userRole).toBe('ROLE_ADMIN')
    })

    it('computes userRoles correctly', () => {
      expect(userStore.userRoles).toEqual([])
      
      userStore.userInfo = {
        roles: [
          { roleCode: 'ROLE_ADMIN' },
          { roleCode: 'ROLE_USER' }
        ]
      }
      expect(userStore.userRoles).toHaveLength(2)
    })

    it('computes permissions from userPermissions when available', () => {
      userStore.userPermissions = ['user:view', 'user:create']
      expect(userStore.permissions).toEqual(['user:view', 'user:create'])
    })

    it('computes permissions from roles when userPermissions is empty', () => {
      userStore.userInfo = {
        roles: [{ roleCode: 'ROLE_SUPER_ADMIN' }]
      }
      expect(userStore.permissions).toContain('document:view')
      expect(userStore.permissions).toContain('project:view')
      expect(userStore.permissions).toContain('user:create')
    })

    it('computes permissions for project manager role', () => {
      userStore.userInfo = {
        roles: [{ roleCode: 'ROLE_PROJECT_MANAGER' }]
      }
      expect(userStore.permissions).toContain('document:view')
      expect(userStore.permissions).toContain('project:create')
      expect(userStore.permissions).not.toContain('user:delete')
    })

    it('computes permissions for developer role', () => {
      userStore.userInfo = {
        roles: [{ roleCode: 'ROLE_DEVELOPER' }]
      }
      expect(userStore.permissions).toContain('document:view')
      expect(userStore.permissions).not.toContain('project:delete')
    })
  })

  describe('Actions', () => {
    describe('login', () => {
      it('logs in successfully', async () => {
        const mockResponse = {
          token: 'new-token',
          refreshToken: 'new-refresh-token',
          userInfo: {
            id: 1,
            username: 'testuser',
            realName: 'Test User',
            roles: [{ roleCode: 'ROLE_USER' }]
          }
        }
        
        vi.mocked(authApi.login).mockResolvedValue(mockResponse)
        vi.mocked(authApi.getUserPermissions).mockResolvedValue(['user:view'])
        
        const result = await userStore.login('testuser', 'password', false)
        
        expect(authApi.login).toHaveBeenCalledWith({
          username: 'testuser',
          password: 'password',
          rememberMe: false
        })
        
        expect(userStore.token).toBe('new-token')
        expect(userStore.refreshToken).toBe('new-refresh-token')
        expect(userStore.userInfo).toEqual(mockResponse.userInfo)
        expect(authUtils.setToken).toHaveBeenCalledWith('new-token')
        expect(authUtils.setRefreshToken).toHaveBeenCalledWith('new-refresh-token')
        expect(localStorage.setItem).toHaveBeenCalledWith('userInfo', JSON.stringify(mockResponse.userInfo))
        expect(authApi.getUserPermissions).toHaveBeenCalled()
        expect(userStore.userPermissions).toEqual(['user:view'])
        expect(result).toEqual(mockResponse)
      })

      it('handles login error', async () => {
        const error = new Error('Login failed')
        vi.mocked(authApi.login).mockRejectedValue(error)
        
        await expect(userStore.login('testuser', 'wrongpassword')).rejects.toThrow('Login failed')
        
        expect(userStore.token).toBe('')
        expect(userStore.userInfo).toBeNull()
      })
    })

    describe('logout', () => {
      it('logs out successfully', async () => {
        userStore.token = 'some-token'
        userStore.refreshToken = 'some-refresh-token'
        userStore.userInfo = { id: 1, username: 'test' }
        userStore.userPermissions = ['user:view']
        
        vi.mocked(authApi.logout).mockResolvedValue(undefined)
        
        await userStore.logout()
        
        expect(authApi.logout).toHaveBeenCalled()
        expect(userStore.token).toBe('')
        expect(userStore.refreshToken).toBe('')
        expect(userStore.userInfo).toBeNull()
        expect(userStore.userPermissions).toEqual([])
        expect(authUtils.clearAuth).toHaveBeenCalled()
      })

      it('handles logout error', async () => {
        const error = new Error('Logout failed')
        vi.mocked(authApi.logout).mockRejectedValue(error)
        
        userStore.token = 'some-token'
        
        await userStore.logout()
        
        // Should still clear state even if API call fails
        expect(userStore.token).toBe('')
        expect(authUtils.clearAuth).toHaveBeenCalled()
      })
    })

    describe('fetchUserInfo', () => {
      it('fetches user info successfully', async () => {
        const mockUser = {
          id: 1,
          username: 'testuser',
          realName: 'Test User',
          roles: [{ roleCode: 'ROLE_USER' }]
        }
        
        vi.mocked(authApi.getCurrentUser).mockResolvedValue(mockUser)
        vi.mocked(authApi.getUserPermissions).mockResolvedValue(['user:view'])
        
        const result = await userStore.fetchUserInfo()
        
        expect(authApi.getCurrentUser).toHaveBeenCalled()
        expect(userStore.userInfo).toEqual(mockUser)
        expect(localStorage.setItem).toHaveBeenCalledWith('userInfo', JSON.stringify(mockUser))
        expect(authApi.getUserPermissions).toHaveBeenCalled()
        expect(userStore.userPermissions).toEqual(['user:view'])
        expect(result).toEqual(mockUser)
      })

      it('handles fetch user info error', async () => {
        const error = new Error('Failed to fetch user info')
        vi.mocked(authApi.getCurrentUser).mockRejectedValue(error)
        
        await expect(userStore.fetchUserInfo()).rejects.toThrow('Failed to fetch user info')
      })
    })

    describe('fetchUserPermissions', () => {
      it('fetches permissions successfully', async () => {
        const mockPermissions = ['user:view', 'user:create', 'project:view']
        vi.mocked(authApi.getUserPermissions).mockResolvedValue(mockPermissions)
        
        const result = await userStore.fetchUserPermissions()
        
        expect(authApi.getUserPermissions).toHaveBeenCalled()
        expect(userStore.userPermissions).toEqual(mockPermissions)
        expect(result).toEqual(mockPermissions)
      })

      it('handles fetch permissions error', async () => {
        const error = new Error('Failed to fetch permissions')
        vi.mocked(authApi.getUserPermissions).mockRejectedValue(error)
        
        await expect(userStore.fetchUserPermissions()).rejects.toThrow('Failed to fetch permissions')
      })
    })

    describe('restoreFromLocalStorage', () => {
      it('restores state from localStorage', () => {
        vi.mocked(authUtils.getToken).mockReturnValue('stored-token')
        vi.mocked(authUtils.getRefreshToken).mockReturnValue('stored-refresh-token')
        localStorageMock.getItem.mockReturnValue(JSON.stringify({
          id: 1,
          username: 'testuser',
          roles: [{ roleCode: 'ROLE_USER' }]
        }))
        
        userStore.restoreFromLocalStorage()
        
        expect(userStore.token).toBe('stored-token')
        expect(userStore.refreshToken).toBe('stored-refresh-token')
        expect(userStore.userInfo).toEqual({
          id: 1,
          username: 'testuser',
          roles: [{ roleCode: 'ROLE_USER' }]
        })
      })

      it('handles invalid user info in localStorage', () => {
        vi.mocked(authUtils.getToken).mockReturnValue('stored-token')
        localStorageMock.getItem.mockReturnValue('invalid json')
        
        // Should not throw error
        expect(() => userStore.restoreFromLocalStorage()).not.toThrow()
        
        expect(userStore.token).toBe('stored-token')
        expect(userStore.userInfo).toBeNull()
      })

      it('does not fetch permissions on restore', () => {
        vi.mocked(authUtils.getToken).mockReturnValue('stored-token')
        localStorageMock.getItem.mockReturnValue(JSON.stringify({
          id: 1,
          username: 'testuser',
          roles: [{ roleCode: 'ROLE_USER' }]
        }))
        
        userStore.restoreFromLocalStorage()
        
        // Should not call getUserPermissions
        expect(authApi.getUserPermissions).not.toHaveBeenCalled()
      })
    })
  })

  describe('Methods', () => {
    describe('hasPermission', () => {
      it('returns true for super admin', () => {
        userStore.userInfo = {
          roles: [{ roleCode: 'ROLE_SUPER_ADMIN' }]
        }
        
        expect(userStore.hasPermission('any:permission')).toBe(true)
        expect(userStore.hasPermission('non:existent:permission')).toBe(true)
      })

      it('returns true for existing permission', () => {
        userStore.userPermissions = ['user:view', 'user:create']
        
        expect(userStore.hasPermission('user:view')).toBe(true)
        expect(userStore.hasPermission('user:create')).toBe(true)
      })

      it('returns false for non-existent permission', () => {
        userStore.userPermissions = ['user:view']
        
        expect(userStore.hasPermission('user:delete')).toBe(false)
      })

      it('returns false when no permissions', () => {
        expect(userStore.hasPermission('any:permission')).toBe(false)
      })
    })
  })
})