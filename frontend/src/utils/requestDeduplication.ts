/**
 * 请求去重工具
 * 防止相同请求重复发送
 */

import type { AxiosRequestConfig } from 'axios'

interface PendingRequest {
  controller: AbortController
  timestamp: number
}

export class RequestDeduplication {
  private pendingRequests = new Map<string, PendingRequest>()
  private readonly timeout = 30000 // 30秒超时

  /**
   * 生成请求唯一标识
   */
  generateRequestKey(config: AxiosRequestConfig): string {
    const { method, url, params, data } = config
    return `${method}:${url}:${JSON.stringify(params)}:${JSON.stringify(data)}`
  }

  /**
   * 添加请求到待处理队列
   */
  addPendingRequest(config: AxiosRequestConfig): AbortController {
    const requestKey = this.generateRequestKey(config)
    
    // 如果存在相同请求，取消旧请求
    if (this.pendingRequests.has(requestKey)) {
      const pending = this.pendingRequests.get(requestKey)!
      pending.controller.abort('Duplicate request cancelled')
    }

    // 创建新的AbortController
    const controller = new AbortController()
    this.pendingRequests.set(requestKey, {
      controller,
      timestamp: Date.now()
    })

    return controller
  }

  /**
   * 从待处理队列移除请求
   */
  removePendingRequest(config: AxiosRequestConfig): void {
    const requestKey = this.generateRequestKey(config)
    this.pendingRequests.delete(requestKey)
  }

  /**
   * 清理超时的请求
   */
  cleanupTimeoutRequests(): void {
    const now = Date.now()
    for (const [key, pending] of this.pendingRequests.entries()) {
      if (now - pending.timestamp > this.timeout) {
        pending.controller.abort('Request timeout')
        this.pendingRequests.delete(key)
      }
    }
  }

  /**
   * 清空所有待处理请求
   */
  clearAll(): void {
    this.pendingRequests.forEach((pending) => {
      pending.controller.abort('Clear all requests')
    })
    this.pendingRequests.clear()
  }
}

// 导出单例
export const requestDeduplication = new RequestDeduplication()

// 定期清理超时请求
setInterval(() => {
  requestDeduplication.cleanupTimeoutRequests()
}, 60000) // 每分钟清理一次
