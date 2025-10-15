import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import dayjs from 'dayjs'
import ProjectFormModal from './ProjectFormModal.vue'
import { projectApi } from '@/api/modules/project'
import { message } from 'ant-design-vue'

// Mock dependencies
vi.mock('@/api/modules/project')
vi.mock('ant-design-vue', () => ({
  message: {
    success: vi.fn(),
    error: vi.fn()
  }
}))

describe('ProjectFormModal', () => {
  let wrapper: any
  const mockUsers = [
    { id: 1, name: '张三' },
    { id: 2, name: '李四' },
    { id: 3, name: '王五' }
  ]

  beforeEach(() => {
    wrapper = mount(ProjectFormModal, {
      props: {
        visible: true,
        users: mockUsers
      },
      global: {
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
            props: ['value', 'placeholder', 'disabled']
          },
          'a-textarea': {
            template: '<textarea />',
            props: ['value', 'placeholder', 'rows']
          },
          'a-select': {
            template: '<div><slot /></div>',
            props: ['value', 'placeholder', 'filterOption']
          },
          'a-select-option': {
            template: '<option :value="value"><slot /></option>',
            props: ['value']
          },
          'a-range-picker': {
            template: '<input />',
            props: ['value']
          },
          'a-radio-group': {
            template: '<div><slot /></div>',
            props: ['value']
          },
          'a-radio': {
            template: '<input type="radio" />',
            props: ['value']
          }
        }
      }
    })
  })

  it('renders correctly for create mode', () => {
    expect(wrapper.find('div').exists()).toBe(true)
    // In create mode, title should be "创建项目"
    expect(wrapper.vm.isEditMode).toBe(false)
  })

  it('renders correctly for edit mode', async () => {
    const mockProject = {
      id: 1,
      name: '测试项目',
      key: 'TEST',
      description: '这是一个测试项目',
      leaderId: 1,
      startDate: '2023-01-01',
      endDate: '2023-12-31',
      status: 'ACTIVE',
      visibility: 'PUBLIC'
    }

    await wrapper.setProps({
      project: mockProject
    })

    expect(wrapper.vm.isEditMode).toBe(true)
  })

  it('validates project name', async () => {
    const form = wrapper.vm
    const nameInput = wrapper.find('input[placeholder*="项目名称"]')

    // Test empty name
    await form.formRef?.validateFields(['name']).catch(() => {})
    // Since we're stubbing the form, we need to test the validation rules directly
    const nameRule = form.rules.name[0]
    expect(nameRule.required).toBe(true)

    // Test minimum length
    const minLengthRule = form.rules.name[1]
    expect(minLengthRule.min).toBe(2)
    expect(minLengthRule.max).toBe(100)
  })

  it('validates project key', async () => {
    const form = wrapper.vm
    const keyRule = form.rules.key

    // Test required
    expect(keyRule[0].required).toBe(true)

    // Test pattern
    expect(keyRule[1].pattern).toBe(/^[A-Z0-9_-]{2,50}$/)
    expect(keyRule[1].message).toContain('需为2-50个大写字母、数字、下划线或连字符')
  })

  it('validates project leader', async () => {
    const form = wrapper.vm
    const leaderRule = form.rules.leaderId[0]
    
    expect(leaderRule.required).toBe(true)
    expect(leaderRule.message).toBe('请选择项目负责人')
  })

  it('validates description length', async () => {
    const form = wrapper.vm
    const descRule = form.rules.description[0]
    
    expect(descRule.max).toBe(500)
    expect(descRule.message).toContain('长度不超过 500 个字符')
  })

  it('disables project key in edit mode', async () => {
    const mockProject = {
      id: 1,
      name: '测试项目',
      key: 'TEST',
      description: '测试描述',
      leaderId: 1,
      status: 'ACTIVE',
      visibility: 'PUBLIC'
    }

    await wrapper.setProps({
      project: mockProject
    })

    const keyInput = wrapper.find('input[placeholder*="PROJ"]')
    expect(keyInput.attributes('disabled')).toBeDefined()
  })

  it('populates form with project data in edit mode', async () => {
    const mockProject = {
      id: 1,
      name: '测试项目',
      key: 'TEST',
      description: '测试描述',
      leaderId: 1,
      startDate: '2023-01-01',
      endDate: '2023-12-31',
      status: 'ACTIVE',
      visibility: 'PUBLIC'
    }

    await wrapper.setProps({
      project: mockProject
    })

    const formState = wrapper.vm.formState
    expect(formState.name).toBe('测试项目')
    expect(formState.key).toBe('TEST')
    expect(formState.description).toBe('测试描述')
    expect(formState.leaderId).toBe(1)
    expect(formState.status).toBe('ACTIVE')
    expect(formState.visibility).toBe('PUBLIC')
    expect(formState.dateRange).toEqual([
      dayjs('2023-01-01'),
      dayjs('2023-12-31')
    ])
  })

  it('resets form when switching from edit to create mode', async () => {
    // Start in edit mode
    const mockProject = {
      id: 1,
      name: '测试项目',
      key: 'TEST',
      description: '测试描述',
      leaderId: 1,
      status: 'ACTIVE',
      visibility: 'PUBLIC'
    }

    await wrapper.setProps({
      project: mockProject
    })

    // Switch to create mode
    await wrapper.setProps({
      project: null
    })

    const formState = wrapper.vm.formState
    expect(formState.name).toBe('')
    expect(formState.key).toBe('')
    expect(formState.description).toBe('')
    expect(formState.leaderId).toBe(null)
    expect(formState.status).toBe('PLANNING')
    expect(formState.visibility).toBe('PRIVATE')
    expect(formState.dateRange).toBe(null)
  })

  it('submits form to create project', async () => {
    const mockResponse = { id: 1, name: '新项目' }
    vi.mocked(projectApi.createProject).mockResolvedValue(mockResponse)

    // Fill form
    wrapper.vm.formState.name = '新项目'
    wrapper.vm.formState.key = 'NEW'
    wrapper.vm.formState.description = '新项目描述'
    wrapper.vm.formState.leaderId = 1
    wrapper.vm.formState.dateRange = [dayjs('2023-01-01'), dayjs('2023-12-31')]
    wrapper.vm.formState.status = 'ACTIVE'
    wrapper.vm.formState.visibility = 'PUBLIC'

    // Mock form validation to pass
    wrapper.vm.formRef = {
      validate: vi.fn().mockResolvedValue(true),
      resetFields: vi.fn()
    }

    // Submit form
    await wrapper.vm.handleSubmit()

    // Verify API was called with correct data
    expect(projectApi.createProject).toHaveBeenCalledWith({
      name: '新项目',
      key: 'NEW',
      description: '新项目描述',
      leaderId: 1,
      startDate: '2023-01-01',
      endDate: '2023-12-31',
      status: 'ACTIVE',
      visibility: 'PUBLIC'
    })

    // Verify success message
    expect(message.success).toHaveBeenCalledWith('项目创建成功')
  })

  it('submits form to update project', async () => {
    const mockProject = {
      id: 1,
      name: '测试项目',
      key: 'TEST',
      description: '测试描述',
      leaderId: 1,
      status: 'ACTIVE',
      visibility: 'PUBLIC'
    }

    const mockResponse = { id: 1, name: '更新项目' }
    vi.mocked(projectApi.updateProject).mockResolvedValue(mockResponse)

    await wrapper.setProps({
      project: mockProject
    })

    // Update form data
    wrapper.vm.formState.name = '更新项目'
    wrapper.vm.formState.description = '更新描述'

    // Mock form validation to pass
    wrapper.vm.formRef = {
      validate: vi.fn().mockResolvedValue(true),
      resetFields: vi.fn()
    }

    // Submit form
    await wrapper.vm.handleSubmit()

    // Verify API was called with correct data
    expect(projectApi.updateProject).toHaveBeenCalledWith(1, {
      name: '更新项目',
      key: 'TEST',
      description: '更新描述',
      leaderId: 1,
      startDate: undefined,
      endDate: undefined,
      status: 'ACTIVE',
      visibility: 'PUBLIC'
    })

    // Verify success message
    expect(message.success).toHaveBeenCalledWith('项目更新成功')
  })

  it('handles form submission errors', async () => {
    const error = new Error('创建失败')
    vi.mocked(projectApi.createProject).mockRejectedValue(error)

    // Fill form
    wrapper.vm.formState.name = '新项目'
    wrapper.vm.formState.key = 'NEW'
    wrapper.vm.formState.leaderId = 1

    // Mock form validation to pass
    wrapper.vm.formRef = {
      validate: vi.fn().mockResolvedValue(true),
      resetFields: vi.fn()
    }

    // Submit form
    await wrapper.vm.handleSubmit()

    // Verify error was logged (console.error should be called)
    expect(console.error).toHaveBeenCalledWith('表单提交失败:', error)
  })

  it('emits events correctly', async () => {
    // Test cancel event
    await wrapper.vm.handleCancel()
    expect(wrapper.emitted('update:visible')).toBeTruthy()
    expect(wrapper.emitted('update:visible')[0]).toEqual([false])

    // Test success event
    vi.mocked(projectApi.createProject).mockResolvedValue({ id: 1 })
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
    
    vi.mocked(projectApi.createProject).mockReturnValue(createPromise)

    // Mock form validation to pass
    wrapper.vm.formRef = {
      validate: vi.fn().mockResolvedValue(true),
      resetFields: vi.fn()
    }

    // Fill form
    wrapper.vm.formState.name = '新项目'
    wrapper.vm.formState.key = 'NEW'
    wrapper.vm.formState.leaderId = 1

    // Submit form
    wrapper.vm.handleSubmit()

    // Loading should be true
    expect(wrapper.vm.loading).toBe(true)

    // Resolve promise
    resolveCreate({ id: 1, name: '新项目' })

    // Loading should be false
    expect(wrapper.vm.loading).toBe(false)
  })
})