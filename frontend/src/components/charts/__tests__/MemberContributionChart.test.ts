import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import MemberContributionChart from '../MemberContributionChart.vue'
import * as echarts from 'echarts'

// Mock echarts
vi.mock('echarts', () => ({
  init: vi.fn(() => ({
    setOption: vi.fn(),
    resize: vi.fn(),
    dispose: vi.fn()
  }))
}))

describe('MemberContributionChart', () => {
  const mockData = [
    { name: '张三', taskCount: 15, completedCount: 12 },
    { name: '李四', taskCount: 12, completedCount: 10 },
    { name: '王五', taskCount: 10, completedCount: 8 }
  ]

  beforeEach(() => {
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('should render chart container', () => {
    const wrapper = mount(MemberContributionChart, {
      props: {
        data: mockData
      }
    })

    expect(wrapper.find('.member-contribution-chart').exists()).toBe(true)
  })

  it('should initialize echarts on mount', () => {
    mount(MemberContributionChart, {
      props: {
        data: mockData
      }
    })

    expect(echarts.init).toHaveBeenCalled()
  })

  it('should handle empty data', () => {
    const wrapper = mount(MemberContributionChart, {
      props: {
        data: []
      }
    })

    expect(wrapper.find('.member-contribution-chart').exists()).toBe(true)
  })

  it('should update chart when data changes', async () => {
    const wrapper = mount(MemberContributionChart, {
      props: {
        data: mockData
      }
    })

    const vm = wrapper.vm as any
    const setOptionSpy = vi.spyOn(vm.chartInstance, 'setOption')

    await wrapper.setProps({
      data: [
        { name: '赵六', taskCount: 8, completedCount: 7 }
      ]
    })

    expect(setOptionSpy).toHaveBeenCalled()
  })

  it('should dispose chart on unmount', () => {
    const wrapper = mount(MemberContributionChart, {
      props: {
        data: mockData
      }
    })

    const vm = wrapper.vm as any
    const disposeSpy = vi.spyOn(vm.chartInstance, 'dispose')

    wrapper.unmount()

    expect(disposeSpy).toHaveBeenCalled()
  })
})

