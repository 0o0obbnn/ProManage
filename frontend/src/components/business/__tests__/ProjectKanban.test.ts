import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import ProjectKanban from '../ProjectKanban.vue'
import type { Task } from '@/types/task'

// Mock API
vi.mock('@/api/modules/task', () => ({
  getProjectTasks: vi.fn(() => Promise.resolve({
    list: [],
    total: 0,
    page: 1,
    pageSize: 1000
  })),
  updateTaskStatus: vi.fn(() => Promise.resolve())
}))

// Mock @vueuse/integrations/useSortable
vi.mock('@vueuse/integrations/useSortable', () => ({
  useSortable: vi.fn()
}))

// Mock ant-design-vue
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

describe('ProjectKanban', () => {
  const mockTasks: Task[] = [
    {
      id: 1,
      title: '测试任务1',
      description: '这是一个测试任务',
      status: 0, // TODO
      priority: 1,
      projectId: 1,
      progressPercentage: 0,
      commentCount: 0,
      attachmentCount: 0,
      subtaskCount: 0,
      createTime: '2024-01-01T00:00:00Z',
      updateTime: '2024-01-01T00:00:00Z'
    },
    {
      id: 2,
      title: '测试任务2',
      description: '这是另一个测试任务',
      status: 1, // IN_PROGRESS
      priority: 2,
      projectId: 1,
      progressPercentage: 50,
      commentCount: 2,
      attachmentCount: 1,
      subtaskCount: 3,
      createTime: '2024-01-02T00:00:00Z',
      updateTime: '2024-01-02T00:00:00Z'
    }
  ]

  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should render kanban board correctly', () => {
    const wrapper = mount(ProjectKanban, {
      props: {
        projectId: 1
      },
      global: {
        stubs: {
          'a-space': true,
          'a-button': true,
          'a-input-search': true,
          'a-select': true,
          'a-select-option': true,
          'a-badge': true,
          'a-spin': true,
          'a-tabs': true,
          'a-tab-pane': true,
          'TransitionGroup': true,
          TaskCard: true
        }
      }
    })

    expect(wrapper.find('.project-kanban').exists()).toBe(true)
  })

  it('should have 4 kanban columns', () => {
    const wrapper = mount(ProjectKanban, {
      props: {
        projectId: 1
      },
      global: {
        stubs: {
          'a-space': true,
          'a-button': true,
          'a-input-search': true,
          'a-select': true,
          'a-select-option': true,
          'a-badge': true,
          'a-spin': true,
          'TransitionGroup': true,
          TaskCard: true
        }
      }
    })

    const vm = wrapper.vm as any
    expect(vm.columns.length).toBe(4)
    expect(vm.columns[0].title).toBe('待办')
    expect(vm.columns[1].title).toBe('进行中')
    expect(vm.columns[2].title).toBe('测试中')
    expect(vm.columns[3].title).toBe('已完成')
  })

  it('should emit create-task event when create button is clicked', async () => {
    const wrapper = mount(ProjectKanban, {
      props: {
        projectId: 1
      },
      global: {
        stubs: {
          'a-space': {
            template: '<div><slot /></div>'
          },
          'a-button': {
            template: '<button @click="$attrs.onClick"><slot /></button>'
          },
          'a-input-search': true,
          'a-select': true,
          'a-select-option': true,
          'a-badge': true,
          'a-spin': true,
          'TransitionGroup': true,
          TaskCard: true
        }
      }
    })

    const vm = wrapper.vm as any
    await vm.handleCreateTask()
    expect(wrapper.emitted('create-task')).toBeTruthy()
  })

  it('should load tasks on mount', async () => {
    const { getProjectTasks } = await import('@/api/modules/task')
    
    mount(ProjectKanban, {
      props: {
        projectId: 1
      },
      global: {
        stubs: {
          'a-space': true,
          'a-button': true,
          'a-input-search': true,
          'a-select': true,
          'a-select-option': true,
          'a-badge': true,
          'a-spin': true,
          'TransitionGroup': true,
          TaskCard: true
        }
      }
    })

    await vi.waitFor(() => {
      expect(getProjectTasks).toHaveBeenCalledWith(1, expect.any(Object))
    })
  })

  it('should filter tasks by priority', async () => {
    const wrapper = mount(ProjectKanban, {
      props: {
        projectId: 1
      },
      global: {
        stubs: {
          'a-space': true,
          'a-button': true,
          'a-input-search': true,
          'a-select': true,
          'a-select-option': true,
          'a-badge': true,
          'a-spin': true,
          'TransitionGroup': true,
          TaskCard: true
        }
      }
    })

    const vm = wrapper.vm as any
    vm.filterPriority = 2
    await vm.handleFilterChange()

    const { getProjectTasks } = await import('@/api/modules/task')
    expect(getProjectTasks).toHaveBeenCalledWith(1, expect.objectContaining({
      priority: 2
    }))
  })

  it('should search tasks by keyword', async () => {
    const wrapper = mount(ProjectKanban, {
      props: {
        projectId: 1
      },
      global: {
        stubs: {
          'a-space': true,
          'a-button': true,
          'a-input-search': true,
          'a-select': true,
          'a-select-option': true,
          'a-badge': true,
          'a-spin': true,
          'TransitionGroup': true,
          TaskCard: true
        }
      }
    })

    const vm = wrapper.vm as any
    vm.searchKeyword = '测试'
    await vm.handleSearch()

    const { getProjectTasks } = await import('@/api/modules/task')
    expect(getProjectTasks).toHaveBeenCalledWith(1, expect.objectContaining({
      keyword: '测试'
    }))
  })
})

