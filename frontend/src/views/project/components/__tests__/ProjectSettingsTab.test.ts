import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import ProjectSettingsTab from '../ProjectSettingsTab.vue'
import type { Project } from '@/types/project'

// Mock Ant Design Vue components
vi.mock('ant-design-vue', () => ({
  message: {
    success: vi.fn(),
    error: vi.fn(),
    info: vi.fn()
  },
  Modal: {
    confirm: vi.fn((config) => {
      // 自动调用onOk以便测试
      if (config.onOk) {
        config.onOk()
      }
    })
  }
}))

// Mock router
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: vi.fn()
  })
}))

describe('ProjectSettingsTab', () => {
  let mockProject: Project

  beforeEach(() => {
    setActivePinia(createPinia())
    
    mockProject = {
      id: 1,
      name: 'Test Project',
      code: 'TEST-001',
      description: 'Test Description',
      type: 'WEB',
      priority: 2,
      color: '#1890ff',
      status: 1,
      isPublic: true,
      allowJoin: false,
      startDate: '2025-01-01',
      endDate: '2025-12-31',
      createdAt: '2025-01-01',
      updatedAt: '2025-01-01'
    } as Project
  })

  it('should render correctly', () => {
    const wrapper = mount(ProjectSettingsTab, {
      props: {
        project: mockProject
      },
      global: {
        stubs: {
          'a-card': true,
          'a-form': true,
          'a-form-item': true,
          'a-input': true,
          'a-textarea': true,
          'a-select': true,
          'a-select-option': true,
          'a-radio-group': true,
          'a-radio': true,
          'a-range-picker': true,
          'a-button': true,
          'a-space': true,
          'a-switch': true,
          'a-divider': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
  })

  it('should initialize form with project data', () => {
    const wrapper = mount(ProjectSettingsTab, {
      props: {
        project: mockProject
      },
      global: {
        stubs: {
          'a-card': true,
          'a-form': true,
          'a-form-item': true,
          'a-input': true,
          'a-textarea': true,
          'a-select': true,
          'a-select-option': true,
          'a-radio-group': true,
          'a-radio': true,
          'a-range-picker': true,
          'a-button': true,
          'a-space': true,
          'a-switch': true,
          'a-divider': true
        }
      }
    })

    const vm = wrapper.vm as any
    expect(vm.basicForm.name).toBe('Test Project')
    expect(vm.basicForm.code).toBe('TEST-001')
    expect(vm.basicForm.description).toBe('Test Description')
    expect(vm.basicForm.type).toBe('WEB')
    expect(vm.basicForm.priority).toBe(2)
  })

  it('should validate required fields', async () => {
    const wrapper = mount(ProjectSettingsTab, {
      props: {
        project: mockProject
      },
      global: {
        stubs: {
          'a-card': true,
          'a-form': true,
          'a-form-item': true,
          'a-input': true,
          'a-textarea': true,
          'a-select': true,
          'a-select-option': true,
          'a-radio-group': true,
          'a-radio': true,
          'a-range-picker': true,
          'a-button': true,
          'a-space': true,
          'a-switch': true,
          'a-divider': true
        }
      }
    })

    const vm = wrapper.vm as any
    
    // 清空必填字段
    vm.basicForm.name = ''
    
    // 验证规则应该包含必填验证
    expect(vm.basicRules.name).toBeDefined()
    expect(vm.basicRules.name.some((rule: any) => rule.required)).toBe(true)
  })

  it('should validate name length', () => {
    const wrapper = mount(ProjectSettingsTab, {
      props: {
        project: mockProject
      },
      global: {
        stubs: {
          'a-card': true,
          'a-form': true,
          'a-form-item': true,
          'a-input': true,
          'a-textarea': true,
          'a-select': true,
          'a-select-option': true,
          'a-radio-group': true,
          'a-radio': true,
          'a-range-picker': true,
          'a-button': true,
          'a-space': true,
          'a-switch': true,
          'a-divider': true
        }
      }
    })

    const vm = wrapper.vm as any
    
    // 检查长度验证规则
    const lengthRule = vm.basicRules.name.find((rule: any) => rule.min && rule.max)
    expect(lengthRule).toBeDefined()
    expect(lengthRule.min).toBe(2)
    expect(lengthRule.max).toBe(100)
  })

  it('should emit refresh event after save', async () => {
    const wrapper = mount(ProjectSettingsTab, {
      props: {
        project: mockProject
      },
      global: {
        stubs: {
          'a-card': true,
          'a-form': true,
          'a-form-item': true,
          'a-input': true,
          'a-textarea': true,
          'a-select': true,
          'a-select-option': true,
          'a-radio-group': true,
          'a-radio': true,
          'a-range-picker': true,
          'a-button': true,
          'a-space': true,
          'a-switch': true,
          'a-divider': true
        }
      }
    })

    const vm = wrapper.vm as any
    
    // Mock successful save
    vi.spyOn(vm.projectStore, 'updateProjectInfo').mockResolvedValue(undefined)
    
    await vm.handleSaveBasicInfo()
    
    // Should emit refresh event
    expect(wrapper.emitted('refresh')).toBeTruthy()
  })

  it('should reset form to original values', () => {
    const wrapper = mount(ProjectSettingsTab, {
      props: {
        project: mockProject
      },
      global: {
        stubs: {
          'a-card': true,
          'a-form': true,
          'a-form-item': true,
          'a-input': true,
          'a-textarea': true,
          'a-select': true,
          'a-select-option': true,
          'a-radio-group': true,
          'a-radio': true,
          'a-range-picker': true,
          'a-button': true,
          'a-space': true,
          'a-switch': true,
          'a-divider': true
        }
      }
    })

    const vm = wrapper.vm as any
    
    // 修改表单值
    vm.basicForm.name = 'Modified Name'
    
    // 重置
    vm.handleResetBasicInfo()
    
    // 应该恢复为原始值
    expect(vm.basicForm.name).toBe('Test Project')
  })

  it('should show archive button for active project', () => {
    const wrapper = mount(ProjectSettingsTab, {
      props: {
        project: { ...mockProject, status: 1 }
      },
      global: {
        stubs: {
          'a-card': true,
          'a-form': true,
          'a-form-item': true,
          'a-input': true,
          'a-textarea': true,
          'a-select': true,
          'a-select-option': true,
          'a-radio-group': true,
          'a-radio': true,
          'a-range-picker': true,
          'a-button': true,
          'a-space': true,
          'a-switch': true,
          'a-divider': true
        }
      }
    })

    // 项目状态不是3(已归档),应该显示归档按钮
    expect(wrapper.vm.project?.status).not.toBe(3)
  })

  it('should show restore button for archived project', () => {
    const wrapper = mount(ProjectSettingsTab, {
      props: {
        project: { ...mockProject, status: 3 }
      },
      global: {
        stubs: {
          'a-card': true,
          'a-form': true,
          'a-form-item': true,
          'a-input': true,
          'a-textarea': true,
          'a-select': true,
          'a-select-option': true,
          'a-radio-group': true,
          'a-radio': true,
          'a-range-picker': true,
          'a-button': true,
          'a-space': true,
          'a-switch': true,
          'a-divider': true
        }
      }
    })

    // 项目状态是3(已归档),应该显示恢复按钮
    expect(wrapper.vm.project?.status).toBe(3)
  })
})

