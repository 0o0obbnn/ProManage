/**
 * ErrorBoundary组件测试
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import ErrorBoundary from '@/components/common/ErrorBoundary.vue'

// 创建一个会抛出错误的组件用于测试
const ErrorComponent = {
  template: '<div>{{ errorMethod() }}</div>',
  methods: {
    errorMethod() {
      throw new Error('Test error')
    }
  }
}

describe('ErrorBoundary', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('应该正常渲染子组件', () => {
    const wrapper = mount(ErrorBoundary, {
      slots: {
        default: '<div>正常内容</div>'
      }
    })

    expect(wrapper.text()).toContain('正常内容')
    expect(wrapper.find('.error-boundary').exists()).toBe(false)
  })

  it('应该捕获子组件错误并显示错误界面', async () => {
    // 模拟console.error避免测试输出错误信息
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

    const wrapper = mount(ErrorBoundary, {
      slots: {
        default: ErrorComponent
      }
    })

    // 等待错误被捕获
    await wrapper.vm.$nextTick()

    expect(wrapper.find('.error-boundary').exists()).toBe(true)
    expect(wrapper.text()).toContain('页面出现错误')
    expect(wrapper.text()).toContain('Test error')

    consoleSpy.mockRestore()
  })

  it('应该显示重试、报告问题、返回首页按钮', async () => {
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

    const wrapper = mount(ErrorBoundary, {
      slots: {
        default: ErrorComponent
      }
    })

    await wrapper.vm.$nextTick()

    expect(wrapper.find('button').exists()).toBe(true)
    expect(wrapper.text()).toContain('重试')
    expect(wrapper.text()).toContain('报告问题')
    expect(wrapper.text()).toContain('返回首页')

    consoleSpy.mockRestore()
  })

  it('应该调用onError回调', async () => {
    const onErrorSpy = vi.fn()
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

    const wrapper = mount(ErrorBoundary, {
      props: {
        onError: onErrorSpy
      },
      slots: {
        default: ErrorComponent
      }
    })

    await wrapper.vm.$nextTick()

    expect(onErrorSpy).toHaveBeenCalledWith(
      expect.any(Error),
      expect.objectContaining({
        instance: expect.any(Object),
        info: expect.any(String)
      })
    )

    consoleSpy.mockRestore()
  })

  it('应该处理重试功能', async () => {
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
    const reloadSpy = vi.spyOn(window.location, 'reload').mockImplementation(() => {})

    const wrapper = mount(ErrorBoundary, {
      slots: {
        default: ErrorComponent
      }
    })

    await wrapper.vm.$nextTick()

    // 点击重试按钮
    const retryButton = wrapper.find('button')
    await retryButton.trigger('click')

    expect(reloadSpy).toHaveBeenCalled()

    consoleSpy.mockRestore()
    reloadSpy.mockRestore()
  })

  it('应该处理报告问题功能', async () => {
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
    const consoleLogSpy = vi.spyOn(console, 'log').mockImplementation(() => {})

    const wrapper = mount(ErrorBoundary, {
      slots: {
        default: ErrorComponent
      }
    })

    await wrapper.vm.$nextTick()

    // 点击报告问题按钮
    const reportButton = wrapper.findAll('button')[1]
    await reportButton.trigger('click')

    expect(consoleLogSpy).toHaveBeenCalledWith(
      'Error report:',
      expect.objectContaining({
        message: 'Test error',
        stack: expect.any(String),
        url: expect.any(String),
        userAgent: expect.any(String),
        timestamp: expect.any(String)
      })
    )

    consoleSpy.mockRestore()
    consoleLogSpy.mockRestore()
  })

  it('应该处理返回首页功能', async () => {
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

    const wrapper = mount(ErrorBoundary, {
      slots: {
        default: ErrorComponent
      }
    })

    await wrapper.vm.$nextTick()

    // 点击返回首页按钮
    const homeButton = wrapper.findAll('button')[2]
    await homeButton.trigger('click')

    // 验证router.push被调用
    expect(wrapper.vm.$router.push).toHaveBeenCalledWith('/')

    consoleSpy.mockRestore()
  })

  it('在开发环境下应该显示错误详情', async () => {
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

    // 模拟开发环境
    vi.stubGlobal('import', {
      meta: { env: { DEV: true } }
    })

    const wrapper = mount(ErrorBoundary, {
      slots: {
        default: ErrorComponent
      }
    })

    await wrapper.vm.$nextTick()

    expect(wrapper.find('.error-details').exists()).toBe(true)
    expect(wrapper.text()).toContain('错误详情')

    consoleSpy.mockRestore()
    vi.unstubAllGlobals()
  })
})
