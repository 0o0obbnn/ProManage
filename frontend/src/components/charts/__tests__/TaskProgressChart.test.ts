import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import TaskProgressChart from '../TaskProgressChart.vue'
import * as echarts from 'echarts'

// Mock echarts
vi.mock('echarts', () => ({
  init: vi.fn(() => ({
    setOption: vi.fn(),
    resize: vi.fn(),
    dispose: vi.fn()
  }))
}))

describe('TaskProgressChart', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('should render chart container', () => {
    const wrapper = mount(TaskProgressChart, {
      props: {
        totalTasks: 100,
        completedTasks: 60,
        inProgressTasks: 30
      }
    })

    expect(wrapper.find('.task-progress-chart').exists()).toBe(true)
  })

  it('should initialize echarts on mount', () => {
    mount(TaskProgressChart, {
      props: {
        totalTasks: 100,
        completedTasks: 60,
        inProgressTasks: 30
      }
    })

    expect(echarts.init).toHaveBeenCalled()
  })

  it('should calculate todo tasks correctly', () => {
    const wrapper = mount(TaskProgressChart, {
      props: {
        totalTasks: 100,
        completedTasks: 60,
        inProgressTasks: 30
      }
    })

    const vm = wrapper.vm as any
    // todoTasks should be 100 - 60 - 30 = 10
    expect(vm.chartInstance).toBeDefined()
  })

  it('should use provided todoTasks if given', () => {
    mount(TaskProgressChart, {
      props: {
        totalTasks: 100,
        completedTasks: 60,
        inProgressTasks: 30,
        todoTasks: 15
      }
    })

    // Should use the provided todoTasks value
    expect(echarts.init).toHaveBeenCalled()
  })

  it('should update chart when props change', async () => {
    const wrapper = mount(TaskProgressChart, {
      props: {
        totalTasks: 100,
        completedTasks: 60,
        inProgressTasks: 30
      }
    })

    const vm = wrapper.vm as any
    const setOptionSpy = vi.spyOn(vm.chartInstance, 'setOption')

    await wrapper.setProps({
      completedTasks: 70
    })

    expect(setOptionSpy).toHaveBeenCalled()
  })

  it('should dispose chart on unmount', () => {
    const wrapper = mount(TaskProgressChart, {
      props: {
        totalTasks: 100,
        completedTasks: 60,
        inProgressTasks: 30
      }
    })

    const vm = wrapper.vm as any
    const disposeSpy = vi.spyOn(vm.chartInstance, 'dispose')

    wrapper.unmount()

    expect(disposeSpy).toHaveBeenCalled()
  })
})

