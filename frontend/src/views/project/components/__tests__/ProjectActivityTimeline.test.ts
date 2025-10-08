import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ProjectActivityTimeline from '../ProjectActivityTimeline.vue'
import type { ProjectActivity } from '@/types/project'

describe('ProjectActivityTimeline', () => {
  const mockActivities: ProjectActivity[] = [
    {
      id: 1,
      projectId: 1,
      userId: 1,
      userName: '张三',
      type: 'create',
      description: '创建了项目',
      createdAt: '2024-01-01T00:00:00Z'
    },
    {
      id: 2,
      projectId: 1,
      userId: 2,
      userName: '李四',
      type: 'update',
      description: '更新了项目信息',
      createdAt: '2024-01-02T00:00:00Z'
    }
  ]

  it('should render activity timeline correctly', () => {
    const wrapper = mount(ProjectActivityTimeline, {
      props: {
        activities: mockActivities,
        loading: false
      },
      global: {
        stubs: {
          'a-card': true,
          'a-timeline': true,
          'a-timeline-item': true,
          'a-space': true,
          'a-avatar': true,
          'a-button': true,
          'a-spin': true,
          'a-empty': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
  })

  it('should display loading state', () => {
    const wrapper = mount(ProjectActivityTimeline, {
      props: {
        activities: [],
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

  it('should display empty state when no activities', () => {
    const wrapper = mount(ProjectActivityTimeline, {
      props: {
        activities: [],
        loading: false
      },
      global: {
        stubs: {
          'a-card': true,
          'a-spin': true,
          'a-empty': {
            template: '<div class="empty-state">No activities</div>'
          }
        }
      }
    })

    expect(wrapper.find('.empty-state').exists()).toBe(true)
  })

  it('should get correct activity text', () => {
    const wrapper = mount(ProjectActivityTimeline, {
      props: {
        activities: mockActivities,
        loading: false
      }
    })

    const vm = wrapper.vm as any
    expect(vm.getActivityText('create')).toBe('创建了')
    expect(vm.getActivityText('update')).toBe('更新了')
    expect(vm.getActivityText('delete')).toBe('删除了')
    expect(vm.getActivityText('member_add')).toBe('添加了成员')
  })

  it('should get correct activity color', () => {
    const wrapper = mount(ProjectActivityTimeline, {
      props: {
        activities: mockActivities,
        loading: false
      }
    })

    const vm = wrapper.vm as any
    expect(vm.getActivityColor('create')).toBe('green')
    expect(vm.getActivityColor('update')).toBe('blue')
    expect(vm.getActivityColor('delete')).toBe('red')
    expect(vm.getActivityColor('member_add')).toBe('purple')
  })
})

