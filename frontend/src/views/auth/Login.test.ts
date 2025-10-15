import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import { createPinia, setActivePinia } from 'pinia'
import Login from './Login.vue'
import { useUserStore } from '@/stores/modules/user'
import type { UserInfo } from '@/types/global'
import * as authApi from '@/api/modules/auth'
import { message } from 'ant-design-vue'

// Mock dependencies
vi.mock('@/api/modules/auth')
vi.mock('ant-design-vue', () => ({
  message: {
    success: vi.fn(),
    error: vi.fn()
  }
}))

// Mock router
const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: { template: '<div>Home</div>' } },
    { path: '/dashboard', component: { template: '<div>Dashboard</div>' } }
  ]
})

describe('LoginForm', () => {
  let wrapper: any
  let mockUserStore: any

  beforeEach(() => {
    // Create a fresh Pinia instance for each test
    const pinia = createPinia()
    setActivePinia(pinia)

    // Mock user store
    mockUserStore = {
      login: vi.fn(),
      userInfo: null,
      token: ''
    }

    // Mount component with mocked dependencies
    wrapper = mount(Login, {
      global: {
        plugins: [router, pinia],
        mocks: {
          $router: router,
          $route: { query: {} }
        },
        stubs: {
          'router-link': true
        }
      }
    })
  })

  it('renders correctly', () => {
    expect(wrapper.find('.login-container').exists()).toBe(true)
    expect(wrapper.find('.login-box').exists()).toBe(true)
    expect(wrapper.find('.login-header').exists()).toBe(true)
    expect(wrapper.find('input[placeholder="用户名"]').exists()).toBe(true)
    expect(wrapper.find('input[placeholder="密码"]').exists()).toBe(true)
    expect(wrapper.find('button[type="submit"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('ProManage')
    expect(wrapper.text()).toContain('智能项目管理系统')
  })

  it('validates username field', async () => {
    const usernameInput = wrapper.find('input[placeholder="用户名"]')
    const form = wrapper.findComponent({ name: 'AForm' })

    // Test empty username
    await form.trigger('submit')
    expect(wrapper.text()).toContain('请输入用户名')

    // Test minimum length
    await usernameInput.setValue('ab')
    await form.trigger('submit')
    expect(wrapper.text()).toContain('长度至少3个字符')

    // Test valid username
    await usernameInput.setValue('admin')
    await form.trigger('submit')
    expect(wrapper.text()).not.toContain('长度至少3个字符')
  })

  it('validates password field', async () => {
    const passwordInput = wrapper.find('input[type="password"]')
    const form = wrapper.findComponent({ name: 'AForm' })

    // Test empty password
    await form.trigger('submit')
    expect(wrapper.text()).toContain('请输入密码')

    // Test minimum length
    await passwordInput.setValue('12345')
    await form.trigger('submit')
    expect(wrapper.text()).toContain('密码长度至少6个字符')

    // Test valid password
    await passwordInput.setValue('password123')
    await form.trigger('submit')
    expect(wrapper.text()).not.toContain('密码长度至少6个字符')
  })

  it('submits form with valid credentials', async () => {
    const userStore = useUserStore()
    const mockResponse = {
      token: 'mock-token',
      refreshToken: 'mock-refresh-token',
      userInfo: {
        id: 1,
        username: 'admin',
        realName: '管理员',
        roles: [{ roleCode: 'ROLE_ADMIN' }]
      } as UserInfo
    }

    vi.mocked(userStore.login).mockResolvedValue(mockResponse)
    vi.mocked(authApi.getUserPermissions).mockResolvedValue(['document:view', 'project:view'])

    // Fill in form with valid credentials
    await wrapper.find('input[placeholder="用户名"]').setValue('admin')
    await wrapper.find('input[type="password"]').setValue('password123')

    // Submit form
    await wrapper.find('form').trigger('submit')

    // Wait for async operations
    await wrapper.vm.$nextTick()

    // Verify login was called with correct credentials
    expect(userStore.login).toHaveBeenCalledWith('admin', 'password123', false)
    expect(message.success).toHaveBeenCalledWith('登录成功')
  })

  it('handles login failure', async () => {
    const userStore = useUserStore()
    const errorMessage = '用户名或密码错误'
    
    vi.mocked(userStore.login).mockRejectedValue(new Error(errorMessage))

    // Fill in form with invalid credentials
    await wrapper.find('input[placeholder="用户名"]').setValue('invalid')
    await wrapper.find('input[type="password"]').setValue('invalid')

    // Submit form
    await wrapper.find('form').trigger('submit')

    // Wait for async operations
    await wrapper.vm.$nextTick()

    // Verify error message is shown
    expect(message.error).toHaveBeenCalledWith(errorMessage)
  })

  it('shows loading state during login', async () => {
    const userStore = useUserStore()
    let resolveLogin: (value: any) => void
    
    // Create a promise that we can resolve later
    const loginPromise = new Promise(resolve => {
      resolveLogin = resolve
    })
    
    vi.mocked(userStore.login).mockReturnValue(loginPromise)

    // Fill in form
    await wrapper.find('input[placeholder="用户名"]').setValue('admin')
    await wrapper.find('input[type="password"]').setValue('password123')

    // Submit form
    const submitButton = wrapper.find('button[type="submit"]')
    await wrapper.find('form').trigger('submit')

    // Button should be in loading state
    expect(submitButton.attributes('loading')).toBeDefined()

    // Resolve login promise
    resolveLogin({
      token: 'mock-token',
      refreshToken: 'mock-refresh-token',
      userInfo: { id: 1, username: 'admin' }
    })

    // Wait for async operations
    await wrapper.vm.$nextTick()

    // Button should no longer be in loading state
    expect(submitButton.attributes('loading')).toBeUndefined()
  })

  it('remembers login state when checkbox is checked', async () => {
    const userStore = useUserStore()
    const mockResponse = {
      token: 'mock-token',
      refreshToken: 'mock-refresh-token',
      userInfo: { id: 1, username: 'admin' }
    }

    vi.mocked(userStore.login).mockResolvedValue(mockResponse)
    vi.mocked(authApi.getUserPermissions).mockResolvedValue(['document:view'])

    // Check remember me checkbox
    await wrapper.find('input[type="checkbox"]').setValue(true)

    // Fill in form
    await wrapper.find('input[placeholder="用户名"]').setValue('admin')
    await wrapper.find('input[type="password"]').setValue('password123')

    // Submit form
    await wrapper.find('form').trigger('submit')

    // Wait for async operations
    await wrapper.vm.$nextTick()

    // Verify login was called with rememberMe = true
    expect(userStore.login).toHaveBeenCalledWith('admin', 'password123', true)
  })

  it('redirects to specified route after login', async () => {
    const userStore = useUserStore()
    const mockResponse = {
      token: 'mock-token',
      refreshToken: 'mock-refresh-token',
      userInfo: { id: 1, username: 'admin' }
    }

    vi.mocked(userStore.login).mockResolvedValue(mockResponse)
    vi.mocked(authApi.getUserPermissions).mockResolvedValue(['document:view'])

    // Set up route with redirect query
    await wrapper.setData({
      $route: { query: { redirect: '/projects' } }
    })

    // Fill in form
    await wrapper.find('input[placeholder="用户名"]').setValue('admin')
    await wrapper.find('input[type="password"]').setValue('password123')

    // Submit form
    await wrapper.find('form').trigger('submit')

    // Wait for async operations
    await wrapper.vm.$nextTick()

    // Verify router push was called with redirect path
    expect(router.currentRoute.value.path).toBe('/projects')
  })

  it('redirects to dashboard by default when no redirect specified', async () => {
    const userStore = useUserStore()
    const mockResponse = {
      token: 'mock-token',
      refreshToken: 'mock-refresh-token',
      userInfo: { id: 1, username: 'admin' }
    }

    vi.mocked(userStore.login).mockResolvedValue(mockResponse)
    vi.mocked(authApi.getUserPermissions).mockResolvedValue(['document:view'])

    // Fill in form
    await wrapper.find('input[placeholder="用户名"]').setValue('admin')
    await wrapper.find('input[type="password"]').setValue('password123')

    // Submit form
    await wrapper.find('form').trigger('submit')

    // Wait for async operations
    await wrapper.vm.$nextTick()

    // Verify router push was called with default path
    expect(router.currentRoute.value.path).toBe('/dashboard')
  })
})