import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import TaskCard from '../TaskCard.vue'
import type { Task } from '@/types/task'

describe('TaskCard', () => {
  const mockTask: Task = {
    id: 1,
    title: '测试任务',
    description: '这是一个测试任务的详细描述',
    status: 1,
    priority: 2,
    projectId: 1,
    assigneeName: '张三',
    dueDate: '2024-12-31',
    progressPercentage: 65,
    commentCount: 3,
    attachmentCount: 2,
    subtaskCount: 5,
    createTime: '2024-01-01T00:00:00Z',
    updateTime: '2024-01-02T00:00:00Z'
  }

  it('should render task card correctly', () => {
    const wrapper = mount(TaskCard, {
      props: {
        task: mockTask
      },
      global: {
        stubs: {
          'a-tag': true,
          'a-avatar': true,
          'a-progress': true
        }
      }
    })

    expect(wrapper.find('.task-card').exists()).toBe(true)
    expect(wrapper.find('.task-card__title').text()).toBe('测试任务')
  })

  it('should display task description', () => {
    const wrapper = mount(TaskCard, {
      props: {
        task: mockTask
      },
      global: {
        stubs: {
          'a-tag': true,
          'a-avatar': true,
          'a-progress': true
        }
      }
    })

    expect(wrapper.find('.task-card__description').exists()).toBe(true)
  })

  it('should display assignee information', () => {
    const wrapper = mount(TaskCard, {
      props: {
        task: mockTask
      },
      global: {
        stubs: {
          'a-tag': true,
          'a-avatar': true,
          'a-progress': true
        }
      }
    })

    expect(wrapper.find('.task-card__assignee').exists()).toBe(true)
    expect(wrapper.find('.task-card__assignee-name').text()).toBe('张三')
  })

  it('should display due date', () => {
    const wrapper = mount(TaskCard, {
      props: {
        task: mockTask
      },
      global: {
        stubs: {
          'a-tag': true,
          'a-avatar': true,
          'a-progress': true
        }
      }
    })

    expect(wrapper.find('.task-card__due-date').exists()).toBe(true)
  })

  it('should display task statistics', () => {
    const wrapper = mount(TaskCard, {
      props: {
        task: mockTask
      },
      global: {
        stubs: {
          'a-tag': true,
          'a-avatar': true,
          'a-progress': true
        }
      }
    })

    expect(wrapper.find('.task-card__stats').exists()).toBe(true)
  })

  it('should emit click event when clicked', async () => {
    const wrapper = mount(TaskCard, {
      props: {
        task: mockTask
      },
      global: {
        stubs: {
          'a-tag': true,
          'a-avatar': true,
          'a-progress': true
        }
      }
    })

    await wrapper.find('.task-card').trigger('click')
    expect(wrapper.emitted('click')).toBeTruthy()
    expect(wrapper.emitted('click')?.[0]).toEqual([mockTask])
  })

  it('should get correct priority color', () => {
    const wrapper = mount(TaskCard, {
      props: {
        task: mockTask
      }
    })

    const vm = wrapper.vm as any
    expect(vm.getPriorityColor(0)).toBe('default')
    expect(vm.getPriorityColor(1)).toBe('blue')
    expect(vm.getPriorityColor(2)).toBe('orange')
    expect(vm.getPriorityColor(3)).toBe('red')
  })

  it('should get correct priority text', () => {
    const wrapper = mount(TaskCard, {
      props: {
        task: mockTask
      }
    })

    const vm = wrapper.vm as any
    expect(vm.getPriorityText(0)).toBe('低')
    expect(vm.getPriorityText(1)).toBe('中')
    expect(vm.getPriorityText(2)).toBe('高')
    expect(vm.getPriorityText(3)).toBe('紧急')
  })

  it('should truncate long description', () => {
    const longDescription = 'a'.repeat(150)
    const taskWithLongDesc: Task = {
      ...mockTask,
      description: longDescription
    }

    const wrapper = mount(TaskCard, {
      props: {
        task: taskWithLongDesc
      }
    })

    const vm = wrapper.vm as any
    const truncated = vm.truncateText(longDescription, 100)
    expect(truncated.length).toBeLessThanOrEqual(103) // 100 + '...'
  })

  it('should handle task without optional fields', () => {
    const minimalTask: Task = {
      id: 1,
      title: '最小任务',
      status: 0,
      priority: 0,
      projectId: 1,
      progressPercentage: 0,
      commentCount: 0,
      attachmentCount: 0,
      subtaskCount: 0,
      createTime: '2024-01-01T00:00:00Z',
      updateTime: '2024-01-01T00:00:00Z'
    }

    const wrapper = mount(TaskCard, {
      props: {
        task: minimalTask
      },
      global: {
        stubs: {
          'a-tag': true,
          'a-avatar': true,
          'a-progress': true
        }
      }
    })

    expect(wrapper.find('.task-card').exists()).toBe(true)
    expect(wrapper.find('.task-card__description').exists()).toBe(false)
    expect(wrapper.find('.task-card__assignee').exists()).toBe(false)
    expect(wrapper.find('.task-card__due-date').exists()).toBe(false)
  })
})

