import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import ProjectInfoCard from '../ProjectInfoCard.vue'
import type { ProjectDetail } from '@/types/project'

describe('ProjectInfoCard', () => {
  const mockProject: ProjectDetail = {
    id: 1,
    name: '测试项目',
    code: 'TEST-001',
    description: '这是一个测试项目的详细描述',
    status: 1,
    priority: 2,
    type: 'WEB',
    color: '#1890ff',
    progress: 65,
    ownerId: 1,
    ownerName: '张三',
    organizationId: 1,
    startDate: '2024-01-01',
    endDate: '2024-12-31',
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-02T00:00:00Z'
  }

  it('should render project information correctly', () => {
    const wrapper = mount(ProjectInfoCard, {
      props: {
        project: mockProject
      },
      global: {
        stubs: {
          'a-card': true,
          'a-descriptions': true,
          'a-descriptions-item': true,
          'a-tag': true,
          'a-space': true,
          'a-avatar': true,
          'a-progress': true,
          'a-button': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
  })

  it('should emit edit event when edit button is clicked', async () => {
    const wrapper = mount(ProjectInfoCard, {
      props: {
        project: mockProject
      },
      global: {
        stubs: {
          'a-card': true,
          'a-descriptions': true,
          'a-descriptions-item': true,
          'a-tag': true,
          'a-space': true,
          'a-avatar': true,
          'a-progress': true,
          'a-button': {
            template: '<button @click="$emit(\'click\')"><slot /></button>'
          }
        }
      }
    })

    await wrapper.find('button').trigger('click')
    expect(wrapper.emitted('edit')).toBeTruthy()
  })

  it('should display correct status tag', () => {
    const wrapper = mount(ProjectInfoCard, {
      props: {
        project: mockProject
      }
    })

    const vm = wrapper.vm as any
    expect(vm.getStatusText(1)).toBe('进行中')
    expect(vm.getStatusColor(1)).toBe('processing')
  })

  it('should display correct priority tag', () => {
    const wrapper = mount(ProjectInfoCard, {
      props: {
        project: mockProject
      }
    })

    const vm = wrapper.vm as any
    expect(vm.getPriorityText(2)).toBe('高优先级')
    expect(vm.getPriorityColor(2)).toBe('orange')
  })

  it('should display correct type text', () => {
    const wrapper = mount(ProjectInfoCard, {
      props: {
        project: mockProject
      }
    })

    const vm = wrapper.vm as any
    expect(vm.getTypeText('WEB')).toBe('Web项目')
  })

  it('should format dates correctly', () => {
    const wrapper = mount(ProjectInfoCard, {
      props: {
        project: mockProject
      }
    })

    const vm = wrapper.vm as any
    expect(vm.formatDate('2024-01-01')).toBe('2024-01-01')
  })

  it('should handle missing optional fields', () => {
    const minimalProject: ProjectDetail = {
      id: 1,
      name: '最小项目',
      code: 'MIN-001',
      status: 0,
      progress: 0,
      ownerId: 1,
      organizationId: 1,
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-01T00:00:00Z'
    }

    const wrapper = mount(ProjectInfoCard, {
      props: {
        project: minimalProject
      },
      global: {
        stubs: {
          'a-card': true,
          'a-descriptions': true,
          'a-descriptions-item': true,
          'a-tag': true,
          'a-space': true,
          'a-avatar': true,
          'a-progress': true,
          'a-button': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
  })
})

