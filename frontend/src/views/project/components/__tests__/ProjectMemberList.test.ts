import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ProjectMemberList from '../ProjectMemberList.vue'
import type { ProjectMember } from '@/types/project'

describe('ProjectMemberList', () => {
  const mockMembers: ProjectMember[] = [
    {
      id: 1,
      projectId: 1,
      userId: 1,
      userName: '张三',
      roleId: 1,
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-01T00:00:00Z'
    },
    {
      id: 2,
      projectId: 1,
      userId: 2,
      userName: '李四',
      roleId: 2,
      createdAt: '2024-01-02T00:00:00Z',
      updatedAt: '2024-01-02T00:00:00Z'
    }
  ]

  it('should render member list correctly', () => {
    const wrapper = mount(ProjectMemberList, {
      props: {
        members: mockMembers,
        loading: false
      },
      global: {
        stubs: {
          'a-card': true,
          'a-list': true,
          'a-list-item': true,
          'a-list-item-meta': true,
          'a-avatar': true,
          'a-tag': true,
          'a-button': true,
          'a-dropdown': true,
          'a-menu': true,
          'a-menu-item': true,
          'a-menu-divider': true,
          'a-spin': true,
          'a-empty': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
  })

  it('should display loading state', () => {
    const wrapper = mount(ProjectMemberList, {
      props: {
        members: [],
        loading: true
      },
      global: {
        stubs: {
          'a-card': true,
          'a-list': true,
          'a-spin': {
            template: '<div class="loading-spin">Loading...</div>'
          },
          'a-empty': true,
          'a-button': true
        }
      }
    })

    expect(wrapper.find('.loading-spin').exists()).toBe(true)
  })

  it('should display empty state when no members', () => {
    const wrapper = mount(ProjectMemberList, {
      props: {
        members: [],
        loading: false
      },
      global: {
        stubs: {
          'a-card': true,
          'a-list': true,
          'a-spin': true,
          'a-empty': {
            template: '<div class="empty-state">No members</div>'
          },
          'a-button': true
        }
      }
    })

    expect(wrapper.find('.empty-state').exists()).toBe(true)
  })

  it('should emit add event when add button is clicked', async () => {
    const wrapper = mount(ProjectMemberList, {
      props: {
        members: mockMembers,
        loading: false
      },
      global: {
        stubs: {
          'a-card': {
            template: '<div><slot name="extra" /></div>'
          },
          'a-list': true,
          'a-button': {
            template: '<button @click="$emit(\'click\')"><slot /></button>'
          }
        }
      }
    })

    await wrapper.find('button').trigger('click')
    expect(wrapper.emitted('add')).toBeTruthy()
  })

  it('should get correct role text', () => {
    const wrapper = mount(ProjectMemberList, {
      props: {
        members: mockMembers,
        loading: false
      }
    })

    const vm = wrapper.vm as any
    expect(vm.getRoleText(1)).toBe('所有者')
    expect(vm.getRoleText(2)).toBe('管理员')
    expect(vm.getRoleText(3)).toBe('成员')
    expect(vm.getRoleText(4)).toBe('访客')
  })

  it('should get correct role color', () => {
    const wrapper = mount(ProjectMemberList, {
      props: {
        members: mockMembers,
        loading: false
      }
    })

    const vm = wrapper.vm as any
    expect(vm.getRoleColor(1)).toBe('red')
    expect(vm.getRoleColor(2)).toBe('orange')
    expect(vm.getRoleColor(3)).toBe('blue')
    expect(vm.getRoleColor(4)).toBe('default')
  })
})

