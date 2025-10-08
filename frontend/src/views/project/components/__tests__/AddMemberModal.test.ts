import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import AddMemberModal from '../AddMemberModal.vue'

// Mock API
vi.mock('@/api/modules/project', () => ({
  addProjectMember: vi.fn(() => Promise.resolve())
}))

// Mock ant-design-vue
vi.mock('ant-design-vue', async () => {
  const actual = await vi.importActual('ant-design-vue')
  return {
    ...actual,
    message: {
      success: vi.fn(),
      error: vi.fn(),
      info: vi.fn()
    }
  }
})

describe('AddMemberModal', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should render modal correctly', () => {
    const wrapper = mount(AddMemberModal, {
      props: {
        projectId: 1,
        open: true
      },
      global: {
        stubs: {
          'a-modal': true,
          'a-form': true,
          'a-form-item': true,
          'a-select': true,
          'a-select-option': true,
          'a-avatar': true,
          'a-tag': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
  })

  it('should emit update:open when modal visibility changes', async () => {
    const wrapper = mount(AddMemberModal, {
      props: {
        projectId: 1,
        open: false
      },
      global: {
        stubs: {
          'a-modal': true,
          'a-form': true,
          'a-form-item': true,
          'a-select': true,
          'a-select-option': true,
          'a-avatar': true,
          'a-tag': true
        }
      }
    })

    const vm = wrapper.vm as any
    vm.visible = true
    await wrapper.vm.$nextTick()

    expect(wrapper.emitted('update:open')).toBeTruthy()
    expect(wrapper.emitted('update:open')?.[0]).toEqual([true])
  })

  it('should filter existing members from available users', async () => {
    const wrapper = mount(AddMemberModal, {
      props: {
        projectId: 1,
        open: true,
        existingMemberIds: [1, 2]
      },
      global: {
        stubs: {
          'a-modal': true,
          'a-form': true,
          'a-form-item': true,
          'a-select': true,
          'a-select-option': true,
          'a-avatar': true,
          'a-tag': true
        }
      }
    })

    const vm = wrapper.vm as any
    await vm.loadAvailableUsers()

    // 应该过滤掉ID为1和2的用户
    expect(vm.availableUsers.every((u: any) => ![1, 2].includes(u.id))).toBe(true)
  })

  it('should call addProjectMember API on submit', async () => {
    const { addProjectMember } = await import('@/api/modules/project')
    
    const wrapper = mount(AddMemberModal, {
      props: {
        projectId: 1,
        open: true
      },
      global: {
        stubs: {
          'a-modal': true,
          'a-form': {
            template: '<div><slot /></div>'
          },
          'a-form-item': true,
          'a-select': true,
          'a-select-option': true,
          'a-avatar': true,
          'a-tag': true
        }
      }
    })

    const vm = wrapper.vm as any
    vm.formData.userId = 3
    vm.formData.roleId = 2

    // Mock form validation
    vm.formRef = {
      validate: vi.fn(() => Promise.resolve())
    }

    await vm.handleOk()

    expect(addProjectMember).toHaveBeenCalledWith(1, 3, 2)
  })

  it('should emit success event after successful submission', async () => {
    const wrapper = mount(AddMemberModal, {
      props: {
        projectId: 1,
        open: true
      },
      global: {
        stubs: {
          'a-modal': true,
          'a-form': true,
          'a-form-item': true,
          'a-select': true,
          'a-select-option': true,
          'a-avatar': true,
          'a-tag': true
        }
      }
    })

    const vm = wrapper.vm as any
    vm.formData.userId = 3
    vm.formData.roleId = 2

    // Mock form validation
    vm.formRef = {
      validate: vi.fn(() => Promise.resolve())
    }

    await vm.handleOk()

    expect(wrapper.emitted('success')).toBeTruthy()
  })

  it('should reset form when modal is closed', async () => {
    const wrapper = mount(AddMemberModal, {
      props: {
        projectId: 1,
        open: true
      },
      global: {
        stubs: {
          'a-modal': true,
          'a-form': true,
          'a-form-item': true,
          'a-select': true,
          'a-select-option': true,
          'a-avatar': true,
          'a-tag': true
        }
      }
    })

    const vm = wrapper.vm as any
    vm.formData.userId = 3
    vm.formData.roleId = 3

    vm.formRef = {
      resetFields: vi.fn()
    }

    vm.visible = false
    await wrapper.vm.$nextTick()

    expect(vm.formRef.resetFields).toHaveBeenCalled()
    expect(vm.formData.userId).toBeUndefined()
    expect(vm.formData.roleId).toBe(2) // 默认值
  })
})

