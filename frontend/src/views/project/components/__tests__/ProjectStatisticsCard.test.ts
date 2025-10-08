import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ProjectStatisticsCard from '../ProjectStatisticsCard.vue'
import type { ProjectStatistics } from '@/types/project'

describe('ProjectStatisticsCard', () => {
  const mockStatistics: ProjectStatistics = {
    totalTasks: 20,
    completedTasks: 13,
    inProgressTasks: 5,
    todoTasks: 2,
    totalDocuments: 10,
    recentDocuments: 3,
    totalMembers: 5
  }

  it('should render statistics correctly', () => {
    const wrapper = mount(ProjectStatisticsCard, {
      props: {
        statistics: mockStatistics,
        loading: false
      },
      global: {
        stubs: {
          'a-card': true,
          'a-row': true,
          'a-col': true,
          'a-statistic': true,
          'a-progress': true,
          'a-divider': true,
          'a-spin': true,
          'a-empty': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
  })

  it('should display loading state', () => {
    const wrapper = mount(ProjectStatisticsCard, {
      props: {
        statistics: null,
        loading: true
      },
      global: {
        stubs: {
          'a-card': true,
          'a-spin': {
            template: '<div class="loading-spin">Loading...</div>'
          },
          'a-empty': true
        }
      }
    })

    expect(wrapper.find('.loading-spin').exists()).toBe(true)
  })

  it('should display empty state when no statistics', () => {
    const wrapper = mount(ProjectStatisticsCard, {
      props: {
        statistics: null,
        loading: false
      },
      global: {
        stubs: {
          'a-card': true,
          'a-spin': true,
          'a-empty': {
            template: '<div class="empty-state">No statistics</div>'
          }
        }
      }
    })

    expect(wrapper.find('.empty-state').exists()).toBe(true)
  })

  it('should calculate completion rate correctly', () => {
    const wrapper = mount(ProjectStatisticsCard, {
      props: {
        statistics: mockStatistics,
        loading: false
      }
    })

    const vm = wrapper.vm as any
    expect(vm.completionRate).toBe(65) // 13/20 * 100 = 65
  })

  it('should handle zero total tasks', () => {
    const emptyStatistics: ProjectStatistics = {
      totalTasks: 0,
      completedTasks: 0,
      inProgressTasks: 0,
      todoTasks: 0,
      totalDocuments: 0,
      recentDocuments: 0,
      totalMembers: 0
    }

    const wrapper = mount(ProjectStatisticsCard, {
      props: {
        statistics: emptyStatistics,
        loading: false
      }
    })

    const vm = wrapper.vm as any
    expect(vm.completionRate).toBe(0)
  })

  it('should get correct progress color', () => {
    const wrapper = mount(ProjectStatisticsCard, {
      props: {
        statistics: mockStatistics,
        loading: false
      }
    })

    const vm = wrapper.vm as any
    expect(vm.getProgressColor(20)).toBe('#ff4d4f') // < 30: red
    expect(vm.getProgressColor(50)).toBe('#faad14') // < 70: orange
    expect(vm.getProgressColor(80)).toBe('#52c41a') // >= 70: green
  })
})

