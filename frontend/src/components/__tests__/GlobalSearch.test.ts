import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import GlobalSearch from '../GlobalSearch.vue'
import { SearchType } from '@/api/modules/search'

// Mock router
const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush
  })
}))

// Mock search API
vi.mock('@/api/modules/search', () => ({
  SearchType: {
    ALL: 'all',
    PROJECT: 'project',
    DOCUMENT: 'document',
    TASK: 'task',
    CHANGE: 'change'
  },
  searchApi: {
    search: vi.fn(() => Promise.resolve({
      data: {
        total: 10,
        list: [
          {
            id: 1,
            type: 'project',
            title: 'Test Project',
            description: 'Test Description',
            url: '/projects/1',
            createdAt: '2024-01-01T00:00:00Z'
          }
        ],
        page: 1,
        pageSize: 20
      }
    })),
    getSearchSuggestions: vi.fn(() => Promise.resolve({
      data: [
        {
          id: 1,
          type: 'project',
          title: 'Test Project',
          url: '/projects/1'
        }
      ]
    })),
    searchProjects: vi.fn(() => Promise.resolve({ data: { total: 0, list: [], page: 1, pageSize: 20 } })),
    searchDocuments: vi.fn(() => Promise.resolve({ data: { total: 0, list: [], page: 1, pageSize: 20 } })),
    searchTasks: vi.fn(() => Promise.resolve({ data: { total: 0, list: [], page: 1, pageSize: 20 } })),
    searchChanges: vi.fn(() => Promise.resolve({ data: { total: 0, list: [], page: 1, pageSize: 20 } }))
  }
}))

// Mock dayjs
vi.mock('dayjs', () => {
  const dayjs = (date?: any) => ({
    fromNow: () => '1小时前',
    format: (fmt: string) => '2024-01-01'
  })
  dayjs.extend = vi.fn()
  dayjs.locale = vi.fn()
  return { default: dayjs }
})

vi.mock('dayjs/plugin/relativeTime', () => ({ default: {} }))
vi.mock('dayjs/locale/zh-cn', () => ({ default: {} }))

describe('GlobalSearch', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
  })

  it('should render trigger button', () => {
    const wrapper = mount(GlobalSearch, {
      global: {
        stubs: {
          'a-modal': true,
          'a-input-search': true,
          'a-tag': {
            template: '<span><slot /></span>'
          },
          'a-button': true,
          'a-collapse': true,
          'a-collapse-panel': true,
          'a-form': true,
          'a-form-item': true,
          'a-checkbox-group': true,
          'a-checkbox': true,
          'a-range-picker': true,
          'a-select': true,
          'a-select-option': true,
          'a-tabs': true,
          'a-tab-pane': true,
          SearchResultList: true
        }
      }
    })

    expect(wrapper.find('.global-search-trigger').exists()).toBe(true)
    expect(wrapper.text()).toContain('搜索')
  })

  it('should open modal when trigger is clicked', async () => {
    const wrapper = mount(GlobalSearch, {
      global: {
        stubs: {
          'a-modal': {
            template: '<div v-if="open"><slot /></div>',
            props: ['open']
          },
          'a-input-search': true,
          'a-tag': true,
          'a-button': true,
          'a-collapse': true,
          'a-collapse-panel': true,
          'a-form': true,
          'a-form-item': true,
          'a-checkbox-group': true,
          'a-checkbox': true,
          'a-range-picker': true,
          'a-select': true,
          'a-select-option': true,
          'a-tabs': true,
          'a-tab-pane': true,
          SearchResultList: true
        }
      }
    })

    await wrapper.find('.global-search-trigger').trigger('click')
    expect(wrapper.vm.showModal).toBe(true)
  })

  it('should load search history from localStorage', () => {
    const history = ['test1', 'test2', 'test3']
    localStorage.setItem('searchHistory', JSON.stringify(history))

    const wrapper = mount(GlobalSearch, {
      global: {
        stubs: {
          'a-modal': true,
          'a-input-search': true,
          'a-tag': true,
          'a-button': true,
          'a-collapse': true,
          'a-collapse-panel': true,
          'a-form': true,
          'a-form-item': true,
          'a-checkbox-group': true,
          'a-checkbox': true,
          'a-range-picker': true,
          'a-select': true,
          'a-select-option': true,
          'a-tabs': true,
          'a-tab-pane': true,
          SearchResultList: true
        }
      }
    })

    expect(wrapper.vm.searchHistory).toEqual(history)
  })

  it('should add keyword to history when searching', async () => {
    const wrapper = mount(GlobalSearch, {
      global: {
        stubs: {
          'a-modal': true,
          'a-input-search': true,
          'a-tag': true,
          'a-button': true,
          'a-collapse': true,
          'a-collapse-panel': true,
          'a-form': true,
          'a-form-item': true,
          'a-checkbox-group': true,
          'a-checkbox': true,
          'a-range-picker': true,
          'a-select': true,
          'a-select-option': true,
          'a-tabs': true,
          'a-tab-pane': true,
          SearchResultList: true
        }
      }
    })

    wrapper.vm.keyword = 'test keyword'
    await wrapper.vm.handleSearch()

    expect(wrapper.vm.searchHistory).toContain('test keyword')
    expect(localStorage.getItem('searchHistory')).toContain('test keyword')
  })

  it('should clear search history', async () => {
    localStorage.setItem('searchHistory', JSON.stringify(['test1', 'test2']))

    const wrapper = mount(GlobalSearch, {
      global: {
        stubs: {
          'a-modal': true,
          'a-input-search': true,
          'a-tag': true,
          'a-button': true,
          'a-collapse': true,
          'a-collapse-panel': true,
          'a-form': true,
          'a-form-item': true,
          'a-checkbox-group': true,
          'a-checkbox': true,
          'a-range-picker': true,
          'a-select': true,
          'a-select-option': true,
          'a-tabs': true,
          'a-tab-pane': true,
          SearchResultList: true
        }
      }
    })

    await wrapper.vm.clearHistory()

    expect(wrapper.vm.searchHistory).toEqual([])
    expect(localStorage.getItem('searchHistory')).toBeNull()
  })

  it('should get type icon correctly', () => {
    const wrapper = mount(GlobalSearch, {
      global: {
        stubs: {
          'a-modal': true,
          'a-input-search': true,
          'a-tag': true,
          'a-button': true,
          'a-collapse': true,
          'a-collapse-panel': true,
          'a-form': true,
          'a-form-item': true,
          'a-checkbox-group': true,
          'a-checkbox': true,
          'a-range-picker': true,
          'a-select': true,
          'a-select-option': true,
          'a-tabs': true,
          'a-tab-pane': true,
          SearchResultList: true
        }
      }
    })

    expect(wrapper.vm.getTypeIcon(SearchType.PROJECT)).toBeDefined()
    expect(wrapper.vm.getTypeIcon(SearchType.DOCUMENT)).toBeDefined()
    expect(wrapper.vm.getTypeIcon(SearchType.TASK)).toBeDefined()
    expect(wrapper.vm.getTypeIcon(SearchType.CHANGE)).toBeDefined()
  })

  it('should get type label correctly', () => {
    const wrapper = mount(GlobalSearch, {
      global: {
        stubs: {
          'a-modal': true,
          'a-input-search': true,
          'a-tag': true,
          'a-button': true,
          'a-collapse': true,
          'a-collapse-panel': true,
          'a-form': true,
          'a-form-item': true,
          'a-checkbox-group': true,
          'a-checkbox': true,
          'a-range-picker': true,
          'a-select': true,
          'a-select-option': true,
          'a-tabs': true,
          'a-tab-pane': true,
          SearchResultList: true
        }
      }
    })

    expect(wrapper.vm.getTypeLabel(SearchType.PROJECT)).toBe('项目')
    expect(wrapper.vm.getTypeLabel(SearchType.DOCUMENT)).toBe('文档')
    expect(wrapper.vm.getTypeLabel(SearchType.TASK)).toBe('任务')
    expect(wrapper.vm.getTypeLabel(SearchType.CHANGE)).toBe('变更')
  })
})

