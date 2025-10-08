import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { message } from 'ant-design-vue'
import Login from './Login.vue'
import { useUserStore } from '@/stores/modules/user'
import { nextTick } from 'vue'

// Mock vue-router
const mockPush = vi.fn()
const mockRoute = {
  query: {}
}

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush
  }),
  useRoute: () => mockRoute,
  RouterLink: {
    name: 'RouterLink',
    props: ['to'],
    template: '<a><slot /></a>'
  }
}))

// Mock ant-design-vue message
vi.mock('ant-design-vue', async () => {
  const actual = await vi.importActual('ant-design-vue')
  return {
    ...actual,
    message: {
      success: vi.fn(),
      error: vi.fn()
    }
  }
})

describe('Login Component', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('should render login form correctly', () => {
    const wrapper = mount(Login, {
      global: {
        stubs: {
          'router-link': {
            template: '<a><slot /></a>'
          },
          'user-outlined': true,
          'lock-outlined': true
        }
      }
    })

    // Check if essential elements exist
    expect(wrapper.find('h1').text()).toBe('ProManage')
    expect(wrapper.find('p').text()).toBe('智能项目管理系统')
    expect(wrapper.find('input[type="text"]').exists()).toBe(true)
    expect(wrapper.find('input[type="password"]').exists()).toBe(true)
    expect(wrapper.find('button[type="submit"]').exists()).toBe(true)
  })

  it('should validate username field', async () => {
    const wrapper = mount(Login, {
      global: {
        stubs: {
          'router-link': {
            template: '<a><slot /></a>'
          },
          'user-outlined': true,
          'lock-outlined': true
        }
      }
    })

    // Find the form and trigger submit with empty fields
    const form = wrapper.find('form')
    await form.trigger('submit')
    await flushPromises()

    // Check for validation messages
    const formItemElements = wrapper.findAll('.ant-form-item-explain')
    expect(formItemElements.length).toBeGreaterThan(0)
  })

  it('should validate password field', async () => {
    const wrapper = mount(Login, {
      global: {
        stubs: {
          'router-link': {
            template: '<a><slot /></a>'
          },
          'user-outlined': true,
          'lock-outlined': true
        }
      }
    })

    // Fill username but leave password empty
    const usernameInput = wrapper.find('input[type="text"]')
    await usernameInput.setValue('testuser')

    // Trigger form submit
    const form = wrapper.find('form')
    await form.trigger('submit')
    await flushPromises()

    // Check for password validation message
    const formItemElements = wrapper.findAll('.ant-form-item-explain')
    expect(formItemElements.some(el => el.text().includes('请输入密码'))).toBe(true)
  })

  it('should call login with correct credentials', async () => {
    const userStore = useUserStore()
    userStore.login = vi.fn().mockResolvedValue(true)

    const wrapper = mount(Login, {
      global: {
        stubs: {
          'router-link': {
            template: '<a><slot /></a>'
          },
          'user-outlined': true,
          'lock-outlined': true
        }
      }
    })

    // Fill in form fields
    const usernameInput = wrapper.find('input[type="text"]')
    const passwordInput = wrapper.find('input[type="password"]')
    
    await usernameInput.setValue('testuser')
    await passwordInput.setValue('password123')

    // Submit form
    const form = wrapper.find('form')
    await form.trigger('submit')
    await flushPromises()

    // Verify login was called with correct parameters
    expect(userStore.login).toHaveBeenCalledWith('testuser', 'password123', false)
    expect(message.success).toHaveBeenCalledWith('登录成功')
    expect(mockPush).toHaveBeenCalledWith('/dashboard')
  })

  it('should remember user when checkbox is checked', async () => {
    const userStore = useUserStore()
    userStore.login = vi.fn().mockResolvedValue(true)

    const wrapper = mount(Login, {
      global: {
        stubs: {
          'router-link': {
            template: '<a><slot /></a>'
          },
          'user-outlined': true,
          'lock-outlined': true
        }
      }
    })

    // Fill in form fields and check remember me
    const usernameInput = wrapper.find('input[type="text"]')
    const passwordInput = wrapper.find('input[type="password"]')
    const rememberCheckbox = wrapper.find('.ant-checkbox-input')
    
    await usernameInput.setValue('testuser')
    await passwordInput.setValue('password123')
    await rememberCheckbox.setChecked(true)

    // Submit form
    const form = wrapper.find('form')
    await form.trigger('submit')
    await flushPromises()

    // Verify login was called with remember=true
    expect(userStore.login).toHaveBeenCalledWith('testuser', 'password123', true)
  })

  it('should handle login failure gracefully', async () => {
    const userStore = useUserStore()
    const errorMessage = '用户名或密码错误'
    userStore.login = vi.fn().mockRejectedValue(new Error(errorMessage))

    const wrapper = mount(Login, {
      global: {
        stubs: {
          'router-link': {
            template: '<a><slot /></a>'
          },
          'user-outlined': true,
          'lock-outlined': true
        }
      }
    })

    // Fill in form fields
    const usernameInput = wrapper.find('input[type="text"]')
    const passwordInput = wrapper.find('input[type="password"]')
    
    await usernameInput.setValue('wronguser')
    await passwordInput.setValue('wrongpassword')

    // Submit form
    const form = wrapper.find('form')
    await form.trigger('submit')
    await flushPromises()

    // Verify error message was shown
    expect(message.error).toHaveBeenCalledWith(errorMessage)
    expect(mockPush).not.toHaveBeenCalled()
  })

  it('should redirect to specified page after login', async () => {
    // Set up route with redirect query parameter
    mockRoute.query = { redirect: '/projects' }

    const userStore = useUserStore()
    userStore.login = vi.fn().mockResolvedValue(true)

    const wrapper = mount(Login, {
      global: {
        stubs: {
          'router-link': {
            template: '<a><slot /></a>'
          },
          'user-outlined': true,
          'lock-outlined': true
        }
      }
    })

    // Fill in form and submit
    const usernameInput = wrapper.find('input[type="text"]')
    const passwordInput = wrapper.find('input[type="password"]')
    
    await usernameInput.setValue('testuser')
    await passwordInput.setValue('password123')

    const form = wrapper.find('form')
    await form.trigger('submit')
    await flushPromises()

    // Should redirect to /projects instead of default /dashboard
    expect(mockPush).toHaveBeenCalledWith('/projects')
  })

  it('should show loading state during login', async () => {
    const userStore = useUserStore()
    let resolveLogin: any
    userStore.login = vi.fn().mockImplementation(() => 
      new Promise((resolve) => {
        resolveLogin = resolve
      })
    )

    const wrapper = mount(Login, {
      global: {
        stubs: {
          'router-link': {
            template: '<a><slot /></a>'
          },
          'user-outlined': true,
          'lock-outlined': true
        }
      }
    })

    // Fill in form fields
    const usernameInput = wrapper.find('input[type="text"]')
    const passwordInput = wrapper.find('input[type="password"]')
    
    await usernameInput.setValue('testuser')
    await passwordInput.setValue('password123')

    // Submit form
    const form = wrapper.find('form')
    await form.trigger('submit')
    await nextTick()

    // Check loading state
    const submitButton = wrapper.find('button[type="submit"]')
    expect(submitButton.classes()).toContain('ant-btn-loading')

    // Resolve login and check loading state is removed
    resolveLogin(true)
    await flushPromises()
    
    expect(submitButton.classes()).not.toContain('ant-btn-loading')
  })
})
