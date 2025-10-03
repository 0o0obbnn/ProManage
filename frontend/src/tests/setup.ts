/**
 * Vitest测试环境配置
 */
import { config } from '@vue/test-utils'
import { vi } from 'vitest'

// 模拟Ant Design Vue组件
const mockAntComponents = {
  'a-button': { template: '<button><slot /></button>' },
  'a-input': { template: '<input />' },
  'a-form': { template: '<form><slot /></form>' },
  'a-form-item': { template: '<div><slot /></div>' },
  'a-select': { template: '<select><slot /></select>' },
  'a-select-option': { template: '<option><slot /></option>' },
  'a-textarea': { template: '<textarea />' },
  'a-checkbox-group': { template: '<div><slot /></div>' },
  'a-checkbox': { template: '<input type="checkbox" />' },
  'a-switch': { template: '<input type="checkbox" />' },
  'a-range-picker': { template: '<input type="date" />' },
  'a-modal': { template: '<div><slot /></div>' },
  'a-table': { template: '<table><slot /></table>' },
  'a-progress': { template: '<div>Progress</div>' },
  'a-tag': { template: '<span><slot /></span>' },
  'a-avatar': { template: '<div><slot /></div>' },
  'a-spin': { template: '<div><slot /></div>' },
  'a-empty': { template: '<div>Empty</div>' },
  'a-timeline': { template: '<div><slot /></div>' },
  'a-timeline-item': { template: '<div><slot /></div>' },
  'a-badge': { template: '<span><slot /></span>' },
  'a-space': { template: '<div><slot /></div>' },
  'a-collapse': { template: '<div><slot /></div>' },
  'a-collapse-panel': { template: '<div><slot /></div>' },
  'a-result': { template: '<div><slot /></div>' },
  'a-alert': { template: '<div><slot /></div>' },
  'a-divider': { template: '<hr />' }
}

// 注册全局组件
Object.entries(mockAntComponents).forEach(([name, component]) => {
  config.global.components[name] = component
})

// 模拟Vue Router
const mockRouter = {
  push: vi.fn(),
  replace: vi.fn(),
  go: vi.fn(),
  back: vi.fn(),
  forward: vi.fn(),
  currentRoute: {
    value: {
      path: '/',
      name: 'home',
      params: {},
      query: {},
      meta: {}
    }
  }
}

vi.mock('vue-router', () => ({
  useRouter: () => mockRouter,
  useRoute: () => mockRouter.currentRoute
}))

// 模拟Pinia stores
vi.mock('@/stores/modules/analytics', () => ({
  useAnalyticsStore: () => ({
    loading: false,
    tableLoading: false,
    reportLoading: false,
    dashboardStats: null,
    memberContributions: [],
    qualityMetrics: [],
    timelineAnalysis: null,
    createCustomReport: vi.fn(),
    fetchMemberContribution: vi.fn(),
    fetchQualityMetrics: vi.fn(),
    fetchTimelineAnalysis: vi.fn(),
    setSelectedProject: vi.fn(),
    setDateRange: vi.fn()
  })
}))

vi.mock('@/stores/modules/user', () => ({
  useUserStore: () => ({
    user: null,
    isLoggedIn: false,
    hasPermission: vi.fn(() => true),
    login: vi.fn(),
    logout: vi.fn()
  })
}))

// 模拟Ant Design Vue的message
vi.mock('ant-design-vue', () => ({
  message: {
    success: vi.fn(),
    error: vi.fn(),
    warning: vi.fn(),
    info: vi.fn()
  }
}))

// 模拟dayjs
vi.mock('dayjs', () => {
  const mockDayjs = (date?: any) => ({
    format: (format: string) => '2024-01-01',
    subtract: (amount: number, unit: string) => mockDayjs(),
    add: (amount: number, unit: string) => mockDayjs(),
    startOf: (unit: string) => mockDayjs(),
    endOf: (unit: string) => mockDayjs(),
    isBefore: (date: any) => false,
    isAfter: (date: any) => false,
    valueOf: () => new Date().getTime()
  })
  return {
    default: mockDayjs
  }
})

// 模拟lodash-es
vi.mock('lodash-es', () => ({
  debounce: (fn: Function, delay: number) => {
    let timeoutId: NodeJS.Timeout
    return (...args: any[]) => {
      clearTimeout(timeoutId)
      timeoutId = setTimeout(() => fn.apply(null, args), delay)
    }
  }
}))

// 模拟ECharts
vi.mock('echarts', () => ({
  init: vi.fn(() => ({
    setOption: vi.fn(),
    resize: vi.fn(),
    dispose: vi.fn()
  })),
  dispose: vi.fn()
}))

// 模拟DOMPurify
vi.mock('dompurify', () => ({
  default: {
    sanitize: (html: string) => html
  }
}))

// 全局测试工具
global.ResizeObserver = vi.fn().mockImplementation(() => ({
  observe: vi.fn(),
  unobserve: vi.fn(),
  disconnect: vi.fn()
}))

// 模拟window.matchMedia
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: vi.fn().mockImplementation(query => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: vi.fn(),
    removeListener: vi.fn(),
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    dispatchEvent: vi.fn()
  }))
})

// 模拟IntersectionObserver
global.IntersectionObserver = vi.fn().mockImplementation(() => ({
  observe: vi.fn(),
  unobserve: vi.fn(),
  disconnect: vi.fn()
}))

// 清理所有模拟
afterEach(() => {
  vi.clearAllMocks()
})
