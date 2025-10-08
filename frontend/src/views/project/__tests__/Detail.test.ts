import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import Detail from '../Detail.vue'
import { useProjectStore } from '@/stores/modules/project'
import type { ProjectDetail } from '@/types/project'

// Mock vue-router
const mockPush = vi.fn()
const mockRoute = {
  params: { id: '1' }
}

vi.mock('vue-router', () => ({
  useRoute: () => mockRoute,
  useRouter: () => ({
    push: mockPush
  })
}))

// Mock ant-design-vue
vi.mock('ant-design-vue', async () => {
  const actual = await vi.importActual('ant-design-vue')
  return {
    ...actual,
    Modal: {
      confirm: vi.fn((config: any) => {
        if (config.onOk) {
          config.onOk()
        }
      })
    },
    message: {
      success: vi.fn(),
      error: vi.fn(),
      info: vi.fn()
    }
  }
})

// Mock 子组件
vi.mock('../components/ProjectInfoCard.vue', () => ({
  default: {
    name: 'ProjectInfoCard',
    template: '<div class="project-info-card">ProjectInfoCard</div>'
  }
}))

vi.mock('../components/ProjectStatisticsCard.vue', () => ({
  default: {
    name: 'ProjectStatisticsCard',
    template: '<div class="project-statistics-card">ProjectStatisticsCard</div>'
  }
}))

vi.mock('../components/ProjectMemberList.vue', () => ({
  default: {
    name: 'ProjectMemberList',
    template: '<div class="project-member-list">ProjectMemberList</div>'
  }
}))

vi.mock('../components/ProjectActivityTimeline.vue', () => ({
  default: {
    name: 'ProjectActivityTimeline',
    template: '<div class="project-activity-timeline">ProjectActivityTimeline</div>'
  }
}))

vi.mock('../components/ProjectFormModal.vue', () => ({
  default: {
    name: 'ProjectFormModal',
    template: '<div class="project-form-modal">ProjectFormModal</div>'
  }
}))

describe('Project Detail Page', () => {
  let pinia: ReturnType<typeof createPinia>
  let projectStore: ReturnType<typeof useProjectStore>

  const mockProject: ProjectDetail = {
    id: 1,
    name: '测试项目',
    code: 'TEST-001',
    description: '这是一个测试项目',
    status: 1,
    priority: 2,
    type: 'WEB',
    color: '#1890ff',
    progress: 65,
    ownerId: 1,
    ownerName: '张三',
    organizationId: 1,
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-02T00:00:00Z',
    members: [],
    recentActivities: [],
    statistics: {
      totalTasks: 20,
      completedTasks: 13,
      inProgressTasks: 5,
      todoTasks: 2,
      totalDocuments: 10,
      recentDocuments: 3,
      totalMembers: 5
    }
  }

  beforeEach(() => {
    pinia = createPinia()
    setActivePinia(pinia)
    projectStore = useProjectStore()

    // Mock store methods
    vi.spyOn(projectStore, 'fetchProjectDetail').mockResolvedValue(mockProject)
    vi.spyOn(projectStore, 'fetchProjectMembers').mockResolvedValue([])
    vi.spyOn(projectStore, 'fetchProjectStatistics').mockResolvedValue(mockProject.statistics!)
    vi.spyOn(projectStore, 'deleteProjectById').mockResolvedValue()
    vi.spyOn(projectStore, 'archiveProjectById').mockResolvedValue()
    vi.spyOn(projectStore, 'restoreProjectById').mockResolvedValue()

    // Set initial store state
    projectStore.currentProject = mockProject
  })

  it('should render project detail page correctly', async () => {
    const wrapper = mount(Detail, {
      global: {
        plugins: [pinia],
        stubs: {
          'a-breadcrumb': true,
          'a-breadcrumb-item': true,
          'a-spin': true,
          'a-result': true,
          'a-row': true,
          'a-col': true,
          'a-button': true,
          'a-dropdown': true,
          'a-menu': true,
          'a-menu-item': true,
          'a-menu-divider': true,
          'router-link': true
        }
      }
    })

    await wrapper.vm.$nextTick()

    expect(wrapper.find('.project-detail').exists()).toBe(true)
  })

  it('should load project detail on mount', async () => {
    mount(Detail, {
      global: {
        plugins: [pinia],
        stubs: {
          'a-breadcrumb': true,
          'a-breadcrumb-item': true,
          'a-spin': true,
          'a-result': true,
          'a-row': true,
          'a-col': true,
          'a-button': true,
          'a-dropdown': true,
          'a-menu': true,
          'a-menu-item': true,
          'a-menu-divider': true,
          'router-link': true
        }
      }
    })

    await vi.waitFor(() => {
      expect(projectStore.fetchProjectDetail).toHaveBeenCalledWith(1)
      expect(projectStore.fetchProjectMembers).toHaveBeenCalledWith(1)
      expect(projectStore.fetchProjectStatistics).toHaveBeenCalledWith(1)
    })
  })

  it('should display loading state', async () => {
    const wrapper = mount(Detail, {
      global: {
        plugins: [pinia],
        stubs: {
          'a-breadcrumb': true,
          'a-breadcrumb-item': true,
          'a-spin': true,
          'a-result': true,
          'a-row': true,
          'a-col': true,
          'a-button': true,
          'a-dropdown': true,
          'a-menu': true,
          'a-menu-item': true,
          'a-menu-divider': true,
          'router-link': true
        }
      }
    })

    // Initially should show loading
    expect(wrapper.find('.project-detail__loading').exists()).toBe(true)
  })

  it('should display error state when loading fails', async () => {
    vi.spyOn(projectStore, 'fetchProjectDetail').mockRejectedValue(new Error('Network error'))

    const wrapper = mount(Detail, {
      global: {
        plugins: [pinia],
        stubs: {
          'a-breadcrumb': true,
          'a-breadcrumb-item': true,
          'a-spin': true,
          'a-result': true,
          'a-row': true,
          'a-col': true,
          'a-button': true,
          'a-dropdown': true,
          'a-menu': true,
          'a-menu-item': true,
          'a-menu-divider': true,
          'router-link': true
        }
      }
    })

    await vi.waitFor(() => {
      expect(wrapper.vm.error).toBe(true)
    })
  })

  it('should handle delete project', async () => {
    const wrapper = mount(Detail, {
      global: {
        plugins: [pinia],
        stubs: {
          'a-breadcrumb': true,
          'a-breadcrumb-item': true,
          'a-spin': true,
          'a-result': true,
          'a-row': true,
          'a-col': true,
          'a-button': true,
          'a-dropdown': true,
          'a-menu': true,
          'a-menu-item': true,
          'a-menu-divider': true,
          'router-link': true
        }
      }
    })

    await wrapper.vm.$nextTick()

    // Call handleDelete method
    await wrapper.vm.handleDelete()

    await vi.waitFor(() => {
      expect(projectStore.deleteProjectById).toHaveBeenCalledWith(1)
      expect(mockPush).toHaveBeenCalledWith('/projects')
    })
  })

  it('should handle archive project', async () => {
    const wrapper = mount(Detail, {
      global: {
        plugins: [pinia],
        stubs: {
          'a-breadcrumb': true,
          'a-breadcrumb-item': true,
          'a-spin': true,
          'a-result': true,
          'a-row': true,
          'a-col': true,
          'a-button': true,
          'a-dropdown': true,
          'a-menu': true,
          'a-menu-item': true,
          'a-menu-divider': true,
          'router-link': true
        }
      }
    })

    await wrapper.vm.$nextTick()

    // Call handleArchive method
    await wrapper.vm.handleArchive()

    await vi.waitFor(() => {
      expect(projectStore.archiveProjectById).toHaveBeenCalledWith(1)
    })
  })

  it('should handle restore project', async () => {
    const wrapper = mount(Detail, {
      global: {
        plugins: [pinia],
        stubs: {
          'a-breadcrumb': true,
          'a-breadcrumb-item': true,
          'a-spin': true,
          'a-result': true,
          'a-row': true,
          'a-col': true,
          'a-button': true,
          'a-dropdown': true,
          'a-menu': true,
          'a-menu-item': true,
          'a-menu-divider': true,
          'router-link': true
        }
      }
    })

    await wrapper.vm.$nextTick()

    // Call handleRestore method
    await wrapper.vm.handleRestore()

    await vi.waitFor(() => {
      expect(projectStore.restoreProjectById).toHaveBeenCalledWith(1)
    })
  })
})

