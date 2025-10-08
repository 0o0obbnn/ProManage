import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import EditMemberRoleModal from '../EditMemberRoleModal.vue'
import type { ProjectMember } from '@/types/project'

// Mock API
vi.mock('@/api/modules/project', () => ({
  updateProjectMemberRole: vi.fn(() => Promise.resolve())
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

describe('EditMemberRoleModal', () => {
  const mockMember: ProjectMember = {
    id: 1,
    projectId: 1,
    userId: 2,
    username: 'testuser',
    realName: '测试用户',
    email: 'test@example.com',
    roleId: 2,
    roleName: '开发人员',
    roleCode: 'DEVELOPER',
    joinedAt: '2024-01-01T00:00:00Z',
    status: 1
  }

  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should render modal correctly', () => {
    const wrapper = mount(EditMemberRoleModal, {
      props: {
        projectId: 1,
        member: mockMember,
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

  it('should initialize form with member role', async () => {
    const wrapper = mount(EditMemberRoleModal, {
      props: {
        projectId: 1,
        member: mockMember,
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

    await wrapper.vm.$nextTick()
    const vm = wrapper.vm as any
    expect(vm.formData.roleId).toBe(mockMember.roleId)
  })

  it('should call updateProjectMemberRole API on submit', async () => {
    const { updateProjectMemberRole } = await import('@/api/modules/project')
    
    const wrapper = mount(EditMemberRoleModal, {
      props: {
        projectId: 1,
        member: mockMember,
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
    vm.formData.roleId = 3 // 改为测试人员

    // Mock form validation
    vm.formRef = {
      validate: vi.fn(() => Promise.resolve())
    }

    await vm.handleOk()

    expect(updateProjectMemberRole).toHaveBeenCalledWith(1, 2, 3)
  })

  it('should not call API if role unchanged', async () => {
    const { updateProjectMemberRole } = await import('@/api/modules/project')
    const { message } = await import('ant-design-vue')
    
    const wrapper = mount(EditMemberRoleModal, {
      props: {
        projectId: 1,
        member: mockMember,
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
    vm.formData.roleId = mockMember.roleId // 保持不变

    // Mock form validation
    vm.formRef = {
      validate: vi.fn(() => Promise.resolve())
    }

    await vm.handleOk()

    expect(updateProjectMemberRole).not.toHaveBeenCalled()
    expect(message.info).toHaveBeenCalledWith('角色未发生变化')
  })

  it('should emit success event after successful submission', async () => {
    const wrapper = mount(EditMemberRoleModal, {
      props: {
        projectId: 1,
        member: mockMember,
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
    vm.formData.roleId = 3

    // Mock form validation
    vm.formRef = {
      validate: vi.fn(() => Promise.resolve())
    }

    await vm.handleOk()

    expect(wrapper.emitted('success')).toBeTruthy()
  })

  it('should get correct role color', () => {
    const wrapper = mount(EditMemberRoleModal, {
      props: {
        projectId: 1,
        member: mockMember,
        open: true
      }
    })

    const vm = wrapper.vm as any
    expect(vm.getRoleColor(1)).toBe('red')
    expect(vm.getRoleColor(2)).toBe('blue')
    expect(vm.getRoleColor(3)).toBe('green')
    expect(vm.getRoleColor(4)).toBe('orange')
    expect(vm.getRoleColor(5)).toBe('purple')
    expect(vm.getRoleColor(6)).toBe('default')
  })
})

