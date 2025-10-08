import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import ProjectTaskList from '../ProjectTaskList.vue'
import { useTaskStore } from '@/stores/modules/task'

// Mock Ant Design Vue components
vi.mock('ant-design-vue', () => ({
  message: {
    success: vi.fn(),
    error: vi.fn(),
    info: vi.fn()
  },
  Modal: {
    confirm: vi.fn()
  }
}))

describe('ProjectTaskList', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('should render correctly', () => {
    const wrapper = mount(ProjectTaskList, {
      props: {
        projectId: 1
      },
      global: {
        stubs: {
          'a-button': true,
          'a-radio-group': true,
          'a-radio-button': true,
          'a-input-search': true,
          'a-select': true,
          'a-select-option': true,
          'a-table': true,
          'GanttView': true,
          'TaskDetailDrawer': true,
          'TaskFormModal': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
  })

  it('should have correct view modes', () => {
    const wrapper = mount(ProjectTaskList, {
      props: {
        projectId: 1
      },
      global: {
        stubs: {
          'a-button': true,
          'a-radio-group': true,
          'a-radio-button': true,
          'a-input-search': true,
          'a-select': true,
          'a-select-option': true,
          'a-table': true,
          'GanttView': true,
          'TaskDetailDrawer': true,
          'TaskFormModal': true
        }
      }
    })

    const vm = wrapper.vm as any
    expect(['list', 'gantt']).toContain(vm.currentViewMode)
  })

  it('should emit createTask event when create button is clicked', async () => {
    const wrapper = mount(ProjectTaskList, {
      props: {
        projectId: 1
      },
      global: {
        stubs: {
          'a-button': true,
          'a-radio-group': true,
          'a-radio-button': true,
          'a-input-search': true,
          'a-select': true,
          'a-select-option': true,
          'a-table': true,
          'GanttView': true,
          'TaskDetailDrawer': true,
          'TaskFormModal': true
        }
      }
    })

    const vm = wrapper.vm as any
    await vm.handleCreateTask()

    expect(wrapper.emitted('createTask')).toBeTruthy()
  })

  it('should filter tasks by search keyword', async () => {
    const wrapper = mount(ProjectTaskList, {
      props: {
        projectId: 1
      },
      global: {
        stubs: {
          'a-button': true,
          'a-radio-group': true,
          'a-radio-button': true,
          'a-input-search': true,
          'a-select': true,
          'a-select-option': true,
          'a-table': true,
          'GanttView': true,
          'TaskDetailDrawer': true,
          'TaskFormModal': true
        }
      }
    })

    const vm = wrapper.vm as any
    
    // Set mock tasks
    vm.tasks = [
      { id: 1, title: 'Task 1', status: 'TODO', priority: 'high' },
      { id: 2, title: 'Task 2', status: 'IN_PROGRESS', priority: 'medium' },
      { id: 3, title: 'Another Task', status: 'DONE', priority: 'low' }
    ]

    // Test search
    vm.searchKeyword = 'Task 1'
    await wrapper.vm.$nextTick()

    expect(vm.filteredTasks.length).toBe(1)
    expect(vm.filteredTasks[0].title).toBe('Task 1')
  })

  it('should filter tasks by status', async () => {
    const wrapper = mount(ProjectTaskList, {
      props: {
        projectId: 1
      },
      global: {
        stubs: {
          'a-button': true,
          'a-radio-group': true,
          'a-radio-button': true,
          'a-input-search': true,
          'a-select': true,
          'a-select-option': true,
          'a-table': true,
          'GanttView': true,
          'TaskDetailDrawer': true,
          'TaskFormModal': true
        }
      }
    })

    const vm = wrapper.vm as any
    
    // Set mock tasks
    vm.tasks = [
      { id: 1, title: 'Task 1', status: 'TODO', priority: 'high' },
      { id: 2, title: 'Task 2', status: 'IN_PROGRESS', priority: 'medium' },
      { id: 3, title: 'Task 3', status: 'TODO', priority: 'low' }
    ]

    // Test status filter
    vm.filterStatus = 'TODO'
    await wrapper.vm.$nextTick()

    expect(vm.filteredTasks.length).toBe(2)
    expect(vm.filteredTasks.every((t: any) => t.status === 'TODO')).toBe(true)
  })

  it('should filter tasks by priority', async () => {
    const wrapper = mount(ProjectTaskList, {
      props: {
        projectId: 1
      },
      global: {
        stubs: {
          'a-button': true,
          'a-radio-group': true,
          'a-radio-button': true,
          'a-input-search': true,
          'a-select': true,
          'a-select-option': true,
          'a-table': true,
          'GanttView': true,
          'TaskDetailDrawer': true,
          'TaskFormModal': true
        }
      }
    })

    const vm = wrapper.vm as any
    
    // Set mock tasks
    vm.tasks = [
      { id: 1, title: 'Task 1', status: 'TODO', priority: 'high' },
      { id: 2, title: 'Task 2', status: 'IN_PROGRESS', priority: 'medium' },
      { id: 3, title: 'Task 3', status: 'TODO', priority: 'high' }
    ]

    // Test priority filter
    vm.filterPriority = 'high'
    await wrapper.vm.$nextTick()

    expect(vm.filteredTasks.length).toBe(2)
    expect(vm.filteredTasks.every((t: any) => t.priority === 'high')).toBe(true)
  })
})

