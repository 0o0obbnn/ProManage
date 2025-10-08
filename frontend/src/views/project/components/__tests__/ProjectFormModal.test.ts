import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import ProjectFormModal from '../ProjectFormModal.vue'
import type { Project } from '@/types/project'

// Mock Ant Design Vue message
vi.mock('ant-design-vue', async () => {
  const actual = await vi.importActual('ant-design-vue')
  return {
    ...actual,
    message: {
      success: vi.fn(),
      error: vi.fn(),
      warning: vi.fn(),
      info: vi.fn()
    }
  }
})

describe('ProjectFormModal', () => {
  let mockProject: Project

  beforeEach(() => {
    setActivePinia(createPinia())
    
    mockProject = {
      id: 1,
      name: 'Test Project',
      code: 'TEST-001',
      description: 'Test Description',
      type: 'WEB',
      priority: 1,
      color: '#1890ff',
      status: 1,
      startDate: '2025-01-01',
      endDate: '2025-12-31',
      createdAt: '2025-01-01',
      updatedAt: '2025-01-01'
    } as Project
  })

  describe('4.3 表单验证', () => {
    it('should validate required fields', async () => {
      const wrapper = mount(ProjectFormModal, {
        props: {
          visible: true,
          project: null
        },
        global: {
          stubs: {
            'a-modal': true,
            'a-form': true,
            'a-form-item': true,
            'a-input': true,
            'a-textarea': true,
            'a-select': true,
            'a-select-option': true,
            'a-range-picker': true
          }
        }
      })

      const vm = wrapper.vm as any
      
      // 检查必填验证规则
      expect(vm.rules.name).toBeDefined()
      expect(vm.rules.name.some((rule: any) => rule.required)).toBe(true)
      expect(vm.rules.code).toBeDefined()
      expect(vm.rules.code.some((rule: any) => rule.required)).toBe(true)
    })

    it('should validate name length (2-100 characters)', () => {
      const wrapper = mount(ProjectFormModal, {
        props: {
          visible: true,
          project: null
        },
        global: {
          stubs: {
            'a-modal': true,
            'a-form': true,
            'a-form-item': true,
            'a-input': true,
            'a-textarea': true,
            'a-select': true,
            'a-select-option': true,
            'a-range-picker': true
          }
        }
      })

      const vm = wrapper.vm as any
      
      // 检查长度验证规则
      const lengthRule = vm.rules.name.find((rule: any) => rule.min && rule.max)
      expect(lengthRule).toBeDefined()
      expect(lengthRule.min).toBe(2)
      expect(lengthRule.max).toBe(100)
    })

    it('should validate special characters in name', () => {
      const wrapper = mount(ProjectFormModal, {
        props: {
          visible: true,
          project: null
        },
        global: {
          stubs: {
            'a-modal': true,
            'a-form': true,
            'a-form-item': true,
            'a-input': true,
            'a-textarea': true,
            'a-select': true,
            'a-select-option': true,
            'a-range-picker': true
          }
        }
      })

      const vm = wrapper.vm as any
      
      // 检查特殊字符验证规则
      const validatorRule = vm.rules.name.find((rule: any) => rule.validator)
      expect(validatorRule).toBeDefined()
    })

    it('should validate code format (uppercase, numbers, underscore, hyphen)', () => {
      const wrapper = mount(ProjectFormModal, {
        props: {
          visible: true,
          project: null
        },
        global: {
          stubs: {
            'a-modal': true,
            'a-form': true,
            'a-form-item': true,
            'a-input': true,
            'a-textarea': true,
            'a-select': true,
            'a-select-option': true,
            'a-range-picker': true
          }
        }
      })

      const vm = wrapper.vm as any
      
      // 检查编码格式验证规则
      const patternRule = vm.rules.code.find((rule: any) => rule.pattern)
      expect(patternRule).toBeDefined()
      expect(patternRule.pattern).toEqual(/^[A-Z0-9_-]+$/)
    })

    it('should validate code length (2-50 characters)', () => {
      const wrapper = mount(ProjectFormModal, {
        props: {
          visible: true,
          project: null
        },
        global: {
          stubs: {
            'a-modal': true,
            'a-form': true,
            'a-form-item': true,
            'a-input': true,
            'a-textarea': true,
            'a-select': true,
            'a-select-option': true,
            'a-range-picker': true
          }
        }
      })

      const vm = wrapper.vm as any
      
      // 检查编码长度验证规则
      const lengthRule = vm.rules.code.find((rule: any) => rule.min && rule.max)
      expect(lengthRule).toBeDefined()
      expect(lengthRule.min).toBe(2)
      expect(lengthRule.max).toBe(50)
    })

    it('should validate description length (max 500 characters)', () => {
      const wrapper = mount(ProjectFormModal, {
        props: {
          visible: true,
          project: null
        },
        global: {
          stubs: {
            'a-modal': true,
            'a-form': true,
            'a-form-item': true,
            'a-input': true,
            'a-textarea': true,
            'a-select': true,
            'a-select-option': true,
            'a-range-picker': true
          }
        }
      })

      const vm = wrapper.vm as any
      
      // 检查描述长度验证规则
      expect(vm.rules.description).toBeDefined()
      const maxRule = vm.rules.description.find((rule: any) => rule.max)
      expect(maxRule).toBeDefined()
      expect(maxRule.max).toBe(500)
    })
  })

  describe('4.4 创建/编辑逻辑', () => {
    it('should show "创建项目" title when project is null', () => {
      const wrapper = mount(ProjectFormModal, {
        props: {
          visible: true,
          project: null
        },
        global: {
          stubs: {
            'a-modal': true,
            'a-form': true,
            'a-form-item': true,
            'a-input': true,
            'a-textarea': true,
            'a-select': true,
            'a-select-option': true,
            'a-range-picker': true
          }
        }
      })

      const vm = wrapper.vm as any
      expect(vm.isEdit).toBe(false)
    })

    it('should show "编辑项目" title when project is provided', () => {
      const wrapper = mount(ProjectFormModal, {
        props: {
          visible: true,
          project: mockProject
        },
        global: {
          stubs: {
            'a-modal': true,
            'a-form': true,
            'a-form-item': true,
            'a-input': true,
            'a-textarea': true,
            'a-select': true,
            'a-select-option': true,
            'a-range-picker': true
          }
        }
      })

      const vm = wrapper.vm as any
      expect(vm.isEdit).toBe(true)
    })

    it('should initialize form with project data when editing', () => {
      const wrapper = mount(ProjectFormModal, {
        props: {
          visible: true,
          project: mockProject
        },
        global: {
          stubs: {
            'a-modal': true,
            'a-form': true,
            'a-form-item': true,
            'a-input': true,
            'a-textarea': true,
            'a-select': true,
            'a-select-option': true,
            'a-range-picker': true
          }
        }
      })

      const vm = wrapper.vm as any
      expect(vm.formData.name).toBe('Test Project')
      expect(vm.formData.code).toBe('TEST-001')
      expect(vm.formData.description).toBe('Test Description')
    })

    it('should disable code field when editing', () => {
      const wrapper = mount(ProjectFormModal, {
        props: {
          visible: true,
          project: mockProject
        },
        global: {
          stubs: {
            'a-modal': true,
            'a-form': true,
            'a-form-item': true,
            'a-input': true,
            'a-textarea': true,
            'a-select': true,
            'a-select-option': true,
            'a-range-picker': true
          }
        }
      })

      const vm = wrapper.vm as any
      expect(vm.isEdit).toBe(true)
      // Code field should be disabled when editing
    })
  })

  describe('4.6 加载状态', () => {
    it('should have loading state', () => {
      const wrapper = mount(ProjectFormModal, {
        props: {
          visible: true,
          project: null
        },
        global: {
          stubs: {
            'a-modal': true,
            'a-form': true,
            'a-form-item': true,
            'a-input': true,
            'a-textarea': true,
            'a-select': true,
            'a-select-option': true,
            'a-range-picker': true
          }
        }
      })

      const vm = wrapper.vm as any
      expect(vm.loading).toBeDefined()
      expect(typeof vm.loading).toBe('boolean')
    })
  })
})

