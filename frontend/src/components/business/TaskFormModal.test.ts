import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import dayjs from 'dayjs'
import TaskFormModal from './TaskFormModal.vue'
import { taskApi } from '@/api/modules/task'
import { message } from 'ant-design-vue'
import { useUserStore } from '@/stores/modules/user'

// Mock dependencies
vi.mock('@/api/modules/task')
vi.mock('ant-design-vue', () => ({
  message: {
    success: vi.fn(),
    error: vi.fn()
  }
}))

describe('TaskFormModal', () => {
  let wrapper: any
  const mockUsers = [
    { id: 1, name: '张三' },
    { id: 2, name: '李四' },
    { id: 3, name: '王五' }
  ]

  beforeEach(() => {
    // Create a fresh Pinia instance for each test
    const pinia = createPinia()
    setActivePinia(pinia)

    // Mock user store
    const userStore = useUserStore()
    userStore.userInfo = { id: 1, username: 'testuser' }

    wrapper = mount(TaskFormModal, {
      props: {
        visible: true,
        projectId: 1,
        users: mockUsers
      },
      global: {
        plugins: [pinia],
        stubs: {
          'a-modal': {
            template: '<div><slot /></div>',
            props: ['open', 'title', 'confirmLoading']
          },
          'a-form': {
            template: '<div><slot /></div>',
            props: ['model', 'rules', 'layout']
          },
          'a-form-item': {
            template: '<div><slot /></div>',
            props: ['label', 'name']
          },
          'a-input': {
            template: '<input />',
            props: ['value', 'placeholder']
          },
          'a-textarea': {
            template: '<textarea />',
            props: ['value', 'placeholder', 'rows']
          },
          'a-select': {
            template: '<div><slot /></div>',
            props: ['value', 'placeholder', 'filterOption', 'mode', 'allowClear']
          },
          'a-select-option': {
            template: '<option :value="value"><slot /></option>',
            props: ['value']
          },
          'a-date-picker': {
            template: '<input />',
            props: ['value']
          },
          'a-input-number': {
            template: '<input type="number" />',
            props: ['value', 'min']
          },
          'a-row': {
            template: '<div><slot /></div>',
            props: ['gutter']
          },
          'a-col': {
            template: '<div><slot /></div>',
            props: ['span']
          }
        }
      }
    })
  })

  it('renders correctly for create mode', () => {
    expect(wrapper.find('div').exists()).toBe(true)
    // In create mode, title should be "创建任务"
    expect(wrapper.vm.isEditMode).toBe(false)
  })

  it('renders correctly for edit mode', async () => {
    const mockTask = {
      id: 1,
      title: '测试任务',
      description: '这是一个测试任务',
      assigneeId: 1,
      reporterId: 2,
      status: 'IN_PROGRESS',
      priority: 'HIGH',
      dueDate: '2023-12-31',
      estimatedHours: 8,
      labels: ['bug', 'urgent']
    }

    await wrapper.setProps({
      task: mockTask
    })

    expect(wrapper.vm.isEditMode).toBe(true)
  })

  it('initializes form with default values for create mode', () => {
    const formState = wrapper.vm.formState
    expect(formState.title).toBe('')
    expect(formState.description).toBe('')
    expect(formState.assigneeId).toBe(null)
    expect(formState.reporterId).toBe(1) // Current user
    expect(formState.status).toBe('TODO')
    expect(formState.priority).toBe('MEDIUM')
    expect(formState.dueDate).toBe(null)
    expect(formState.estimatedHours).toBe(null)
    expect(formState.labels).toEqual([])
  })

  it('populates form with task data in edit mode', async () => {
    const mockTask = {
      id: 1,
      title: '测试任务',
      description: '这是一个测试任务',
      assigneeId: 1,
      reporterId: 2,
      status: 'IN_PROGRESS',
      priority: 'HIGH',
      dueDate: '2023-12-31',
      estimatedHours: 8,
      labels: ['bug', 'urgent']
    }

    await wrapper.setProps({
      task: mockTask
    })

    const formState = wrapper.vm.formState
    expect(formState.title).toBe('测试任务')
    expect(formState.description).toBe('这是一个测试任务')
    expect(formState.assigneeId).toBe(1)
    expect(formState.reporterId).toBe(2)
    expect(formState.status).toBe('IN_PROGRESS')
    expect(formState.priority).toBe('HIGH')
    expect(formState.dueDate).toEqual(dayjs('2023-12-31'))
    expect(formState.estimatedHours).toBe(8)
    expect(formState.labels).toEqual(['bug', 'urgent'])
  })

  it('resets form when switching from edit to create mode', async () => {
    // Start in edit mode
    const mockTask = {
      id: 1,
      title: '测试任务',
      description: '这是一个测试任务',
      assigneeId: 1,
      reporterId: 2,
      status: 'IN_PROGRESS',
      priority: 'HIGH',
      dueDate: '2023-12-31',
      estimatedHours: 8,
      labels: ['bug', 'urgent']
    }

    await wrapper.setProps({
      task: mockTask
    })

    // Switch to create mode
    await wrapper.setProps({
      task: null
    })

    const formState = wrapper.vm.formState
    expect(formState.title).toBe('')
    expect(formState.description).toBe('')
    expect(formState.assigneeId).toBe(null)
    expect(formState.reporterId).toBe(1) // Current user
    expect(formState.status).toBe('TODO')
    expect(formState.priority).toBe('MEDIUM')
    expect(formState.dueDate).toBe(null)
    expect(formState.estimatedHours).toBe(null)
    expect(formState.labels).toEqual([])
  })

  it('validates task title', async () => {
    const form = wrapper.vm
    const titleRule = form.rules.title

    // Test required
    expect(titleRule[0].required).toBe(true)

    // Test max length
    expect(titleRule[1].max).toBe(200)
    expect(titleRule[1].message).toContain('标题长度不超过 200 个字符')
  })

  it('validates reporter field', async () => {
    const form = wrapper.vm
    const reporterRule = form.rules.reporterId[0]
    
    expect(reporterRule.required).toBe(true)
    expect(reporterRule.message).toBe('请选择报告人')
  })

  it('submits form to create task', async () => {
    const mockResponse = { id: 1, title: '新任务' }
    vi.mocked(taskApi.createTask).mockResolvedValue(mockResponse)

    // Fill form
    wrapper.vm.formState.title = '新任务'
    wrapper.vm.formState.description = '新任务描述'
    wrapper.vm.formState.assigneeId = 1
    wrapper.vm.formState.reporterId = 2
    wrapper.vm.formState.status = 'IN_PROGRESS'
    wrapper.vm.formState.priority = 'HIGH'
    wrapper.vm.formState.dueDate = dayjs('2023-12-31')
    wrapper.vm.formState.estimatedHours = 8
    wrapper.vm.formState.labels = ['bug', 'urgent']

    // Mock form validation to pass
    wrapper.vm.formRef = {
      validate: vi.fn().mockResolvedValue(true),
      resetFields: vi.fn()
    }

    // Submit form
    await wrapper.vm.handleSubmit()

    // Verify API was called with correct data
    expect(taskApi.createTask).toHaveBeenCalledWith({
      title: '新任务',
      description: '新任务描述',
      assigneeId: 1,
      reporterId: 2,
      status: 'IN_PROGRESS',
      priority: 'HIGH',
      dueDate: '2023-12-31',
      estimatedHours: 8,
      labels: ['bug', 'urgent'],
      projectId: 1
    })

    // Verify success message
    expect(message.success).toHaveBeenCalledWith('任务创建成功')
  })

  it('submits form to update task', async () => {
    const mockTask = {
      id: 1,
      title: '测试任务',
      description: '测试描述',
      assigneeId: 1,
      reporterId: 2,
      status: 'IN_PROGRESS',
      priority: 'HIGH',
      dueDate: '2023-12-31',
      estimatedHours: 8,
      labels: ['bug', 'urgent']
    }

    const mockResponse = { id: 1, title: '更新任务' }
    vi.mocked(taskApi.updateTask).mockResolvedValue(mockResponse)

    await wrapper.setProps({
      task: mockTask
    })

    // Update form data
    wrapper.vm.formState.title = '更新任务'
    wrapper.vm.formState.description = '更新描述'

    // Mock form validation to pass
    wrapper.vm.formRef = {
      validate: vi.fn().mockResolvedValue(true),
      resetFields: vi.fn()
    }

    // Submit form
    await wrapper.vm.handleSubmit()

    // Verify API was called with correct data
    expect(taskApi.updateTask).toHaveBeenCalledWith(1, {
      title: '更新任务',
      description: '更新描述',
      assigneeId: 1,
      reporterId: 2,
      status: 'IN_PROGRESS',
      priority: 'HIGH',
      dueDate: '2023-12-31',
      estimatedHours: 8,
      labels: ['bug', 'urgent'],
      projectId: 1
    })

    // Verify success message
    expect(message.success).toHaveBeenCalledWith('任务更新成功')
  })

  it('handles form submission errors', async () => {
    const error = new Error('创建失败')
    vi.mocked(taskApi.createTask).mockRejectedValue(error)

    // Fill form
    wrapper.vm.formState.title = '新任务'
    wrapper.vm.formState.assigneeId = 1
    wrapper.vm.formState.reporterId = 2

    // Mock form validation to pass
    wrapper.vm.formRef = {
      validate: vi.fn().mockResolvedValue(true),
      resetFields: vi.fn()
    }

    // Submit form
    await wrapper.vm.handleSubmit()

    // Verify error was logged (console.error should be called)
    expect(console.error).toHaveBeenCalledWith('任务表单提交失败:', error)
  })

  it('emits events correctly', async () => {
    // Test cancel event
    await wrapper.vm.handleCancel()
    expect(wrapper.emitted('update:visible')).toBeTruthy()
    expect(wrapper.emitted('update:visible')[0]).toEqual([false])

    // Test success event
    vi.mocked(taskApi.createTask).mockResolvedValue({ id: 1 })
    wrapper.vm.formRef = {
      validate: vi.fn().mockResolvedValue(true),
      resetFields: vi.fn()
    }

    await wrapper.vm.handleSubmit()

    expect(wrapper.emitted('success')).toBeTruthy()
    expect(wrapper.emitted('update:visible')[1]).toEqual([false])
  })

  it('filters user options correctly', () => {
    // Test filter function
    const option = {
      children: [
        {
          children: '张三'
        }
      ]
    }

    // Should match case-insensitive
    expect(wrapper.vm.filterUserOption('zhang', option)).toBe(true)
    expect(wrapper.vm.filterUserOption('ZHANG', option)).toBe(true)
    expect(wrapper.vm.filterUserOption('三', option)).toBe(true)
    
    // Should not match
    expect(wrapper.vm.filterUserOption('李', option)).toBe(false)
  })

  it('shows loading state during submission', async () => {
    let resolveCreate: (value: any) => void
    
    // Create a promise that we can resolve later
    const createPromise = new Promise(resolve => {
      resolveCreate = resolve
    })
    
    vi.mocked(taskApi.createTask).mockReturnValue(createPromise)

    // Mock form validation to pass
    wrapper.vm.formRef = {
      validate: vi.fn().mockResolvedValue(true),
      resetFields: vi.fn()
    }

    // Fill form
    wrapper.vm.formState.title = '新任务'
    wrapper.vm.formState.assigneeId = 1
    wrapper.vm.formState.reporterId = 2

    // Submit form
    wrapper.vm.handleSubmit()

    // Loading should be true
    expect(wrapper.vm.loading).toBe(true)

    // Resolve promise
    resolveCreate({ id: 1, title: '新任务' })

    // Loading should be false
    expect(wrapper.vm.loading).toBe(false)
  })

  it('handles null due date correctly', async () => {
    // Fill form with null due date
    wrapper.vm.formState.title = '新任务'
    wrapper.vm.formState.assigneeId = 1
    wrapper.vm.formState.reporterId = 2
    wrapper.vm.formState.dueDate = null

    // Mock form validation to pass
    wrapper.vm.formRef = {
      validate: vi.fn().mockResolvedValue(true),
      resetFields: vi.fn()
    }

    vi.mocked(taskApi.createTask).mockResolvedValue({ id: 1 })

    // Submit form
    await wrapper.vm.handleSubmit()

    // Verify API was called with undefined dueDate
    expect(taskApi.createTask).toHaveBeenCalledWith(
      expect.objectContaining({
        dueDate: undefined
      })
    )
  })

  it('handles empty labels correctly', async () => {
    // Fill form with empty labels
    wrapper.vm.formState.title = '新任务'
    wrapper.vm.formState.assigneeId = 1
    wrapper.vm.formState.reporterId = 2
    wrapper.vm.formState.labels = []

    // Mock form validation to pass
    wrapper.vm.formRef = {
      validate: vi.fn().mockResolvedValue(true),
      resetFields: vi.fn()
    }

    vi.mocked(taskApi.createTask).mockResolvedValue({ id: 1 })

    // Submit form
    await wrapper.vm.handleSubmit()

    // Verify API was called with empty labels array
    expect(taskApi.createTask).toHaveBeenCalledWith(
      expect.objectContaining({
        labels: []
      })
    )
  })
})