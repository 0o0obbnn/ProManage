/**
 * ProjectCard 组件单元测试
 */
import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import ProjectCard from '../ProjectCard.vue'
import type { Project } from '@/types/project'

// Mock Ant Design Vue 组件
vi.mock('ant-design-vue', () => ({
  Card: { name: 'ACard', template: '<div><slot /></div>' },
  Tag: { name: 'ATag', template: '<span><slot /></span>' },
  Progress: { name: 'AProgress', template: '<div />' },
  Avatar: { name: 'AAvatar', template: '<div><slot /></div>' },
  Button: { name: 'AButton', template: '<button><slot /></button>' },
  Dropdown: { name: 'ADropdown', template: '<div><slot /></div>' },
  Menu: { name: 'AMenu', template: '<div><slot /></div>' },
  MenuItem: { name: 'AMenuItem', template: '<div><slot /></div>' },
  MenuDivider: { name: 'AMenuDivider', template: '<div />' },
  Tooltip: { name: 'ATooltip', template: '<div><slot /></div>' }
}))

// Mock date-fns
vi.mock('date-fns', () => ({
  formatDistanceToNow: () => '2天前'
}))

vi.mock('date-fns/locale', () => ({
  zhCN: {}
}))

describe('ProjectCard', () => {
  const mockProject: Project = {
    id: 1,
    name: '测试项目',
    code: 'TEST-001',
    description: '这是一个测试项目',
    status: 1,
    priority: 2,
    type: 'WEB',
    progress: 65,
    ownerId: 1,
    ownerName: '张三',
    memberCount: 5,
    taskCount: 20,
    documentCount: 10,
    color: '#1890ff',
    createdAt: '2025-01-01T00:00:00Z',
    updatedAt: '2025-01-03T00:00:00Z'
  }

  it('should render project information correctly', () => {
    const wrapper = mount(ProjectCard, {
      props: {
        project: mockProject
      },
      global: {
        stubs: {
          ACard: true,
          ATag: true,
          AProgress: true,
          AAvatar: true,
          AButton: true,
          ADropdown: true,
          AMenu: true,
          AMenuItem: true,
          AMenuDivider: true,
          ATooltip: true
        }
      }
    })

    expect(wrapper.text()).toContain('测试项目')
    expect(wrapper.text()).toContain('TEST-001')
    expect(wrapper.text()).toContain('这是一个测试项目')
  })

  it('should emit click event when card is clicked', async () => {
    const wrapper = mount(ProjectCard, {
      props: {
        project: mockProject
      },
      global: {
        stubs: {
          ACard: true,
          ATag: true,
          AProgress: true,
          AAvatar: true,
          AButton: true,
          ADropdown: true,
          AMenu: true,
          AMenuItem: true,
          AMenuDivider: true,
          ATooltip: true
        }
      }
    })

    await wrapper.find('.project-card').trigger('click')
    expect(wrapper.emitted('click')).toBeTruthy()
    expect(wrapper.emitted('click')?.[0]).toEqual([mockProject])
  })

  it('should display correct status tag', () => {
    const wrapper = mount(ProjectCard, {
      props: {
        project: mockProject
      },
      global: {
        stubs: {
          ACard: true,
          ATag: true,
          AProgress: true,
          AAvatar: true,
          AButton: true,
          ADropdown: true,
          AMenu: true,
          AMenuItem: true,
          AMenuDivider: true,
          ATooltip: true
        }
      }
    })

    expect(wrapper.text()).toContain('进行中')
  })

  it('should display correct priority tag', () => {
    const wrapper = mount(ProjectCard, {
      props: {
        project: mockProject
      },
      global: {
        stubs: {
          ACard: true,
          ATag: true,
          AProgress: true,
          AAvatar: true,
          AButton: true,
          ADropdown: true,
          AMenu: true,
          AMenuItem: true,
          AMenuDivider: true,
          ATooltip: true
        }
      }
    })

    expect(wrapper.text()).toContain('高优先级')
  })

  it('should display project statistics', () => {
    const wrapper = mount(ProjectCard, {
      props: {
        project: mockProject
      },
      global: {
        stubs: {
          ACard: true,
          ATag: true,
          AProgress: true,
          AAvatar: true,
          AButton: true,
          ADropdown: true,
          AMenu: true,
          AMenuItem: true,
          AMenuDivider: true,
          ATooltip: true
        }
      }
    })

    expect(wrapper.text()).toContain('5 成员')
    expect(wrapper.text()).toContain('20 任务')
    expect(wrapper.text()).toContain('10 文档')
  })

  it('should display progress correctly', () => {
    const wrapper = mount(ProjectCard, {
      props: {
        project: mockProject
      },
      global: {
        stubs: {
          ACard: true,
          ATag: true,
          AProgress: true,
          AAvatar: true,
          AButton: true,
          ADropdown: true,
          AMenu: true,
          AMenuItem: true,
          AMenuDivider: true,
          ATooltip: true
        }
      }
    })

    expect(wrapper.text()).toContain('65%')
  })

  it('should handle missing optional fields', () => {
    const minimalProject: Project = {
      id: 2,
      name: '最小项目',
      code: 'MIN-001',
      status: 0,
      progress: 0,
      ownerId: 1,
      createdAt: '2025-01-01T00:00:00Z',
      updatedAt: '2025-01-01T00:00:00Z'
    }

    const wrapper = mount(ProjectCard, {
      props: {
        project: minimalProject
      },
      global: {
        stubs: {
          ACard: true,
          ATag: true,
          AProgress: true,
          AAvatar: true,
          AButton: true,
          ADropdown: true,
          AMenu: true,
          AMenuItem: true,
          AMenuDivider: true,
          ATooltip: true
        }
      }
    })

    expect(wrapper.text()).toContain('最小项目')
    expect(wrapper.text()).toContain('暂无描述')
    expect(wrapper.text()).toContain('0 成员')
  })

  it('should emit edit event when edit menu item is clicked', async () => {
    const wrapper = mount(ProjectCard, {
      props: {
        project: mockProject
      },
      global: {
        stubs: {
          ACard: true,
          ATag: true,
          AProgress: true,
          AAvatar: true,
          AButton: true,
          ADropdown: true,
          AMenu: true,
          AMenuItem: true,
          AMenuDivider: true,
          ATooltip: true
        }
      }
    })

    // 模拟菜单点击
    await wrapper.vm.handleMenuClick({ key: 'edit' })
    expect(wrapper.emitted('edit')).toBeTruthy()
    expect(wrapper.emitted('edit')?.[0]).toEqual([mockProject])
  })

  it('should emit delete event when delete menu item is clicked', async () => {
    const wrapper = mount(ProjectCard, {
      props: {
        project: mockProject
      },
      global: {
        stubs: {
          ACard: true,
          ATag: true,
          AProgress: true,
          AAvatar: true,
          AButton: true,
          ADropdown: true,
          AMenu: true,
          AMenuItem: true,
          AMenuDivider: true,
          ATooltip: true
        }
      }
    })

    await wrapper.vm.handleMenuClick({ key: 'delete' })
    expect(wrapper.emitted('delete')).toBeTruthy()
    expect(wrapper.emitted('delete')?.[0]).toEqual([mockProject])
  })

  it('should emit archive event when archive menu item is clicked', async () => {
    const wrapper = mount(ProjectCard, {
      props: {
        project: mockProject
      },
      global: {
        stubs: {
          ACard: true,
          ATag: true,
          AProgress: true,
          AAvatar: true,
          AButton: true,
          ADropdown: true,
          AMenu: true,
          AMenuItem: true,
          AMenuDivider: true,
          ATooltip: true
        }
      }
    })

    await wrapper.vm.handleMenuClick({ key: 'archive' })
    expect(wrapper.emitted('archive')).toBeTruthy()
    expect(wrapper.emitted('archive')?.[0]).toEqual([mockProject])
  })
})

