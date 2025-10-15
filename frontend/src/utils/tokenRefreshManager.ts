/**
 * Token刷新管理器
 * 解决并发请求时的竞态条件问题
 */

interface RefreshSubscriber {
  resolve: (token: string) => void
  reject: (error: Error) => void
}

export class TokenRefreshManager {
  private isRefreshing = false
  private refreshPromise: Promise<string> | null = null
  private subscribers: RefreshSubscriber[] = []
  private readonly timeout = 10000 // 10秒超时

  /**
   * 刷新Token
   */
  async refresh(refreshFn: () => Promise<{ token: string; refreshToken: string }>): Promise<string> {
    // 如果正在刷新，返回现有Promise
    if (this.refreshPromise) {
      return this.refreshPromise
    }

    this.isRefreshing = true
    
    this.refreshPromise = Promise.race([
      refreshFn(),
      new Promise<never>((_, reject) =>
        setTimeout(() => reject(new Error('Token refresh timeout')), this.timeout)
      )
    ])
      .then((response) => {
        const { token, refreshToken } = response
        
        // 更新本地存储
        localStorage.setItem('token', token)
        localStorage.setItem('refreshToken', refreshToken)
        
        // 通知所有等待的订阅者
        this.notifySubscribers(token)
        
        return token
      })
      .catch((error) => {
        // 通知所有订阅者失败
        this.notifyError(error)
        throw error
      })
      .finally(() => {
        // 清理状态
        this.isRefreshing = false
        this.refreshPromise = null
        this.subscribers = []
      })

    return this.refreshPromise
  }

  /**
   * 订阅Token刷新
   */
  subscribe(): Promise<string> {
    return new Promise((resolve, reject) => {
      this.subscribers.push({ resolve, reject })
    })
  }

  /**
   * 通知所有订阅者Token已刷新
   */
  private notifySubscribers(token: string): void {
    this.subscribers.forEach((subscriber) => subscriber.resolve(token))
  }

  /**
   * 通知所有订阅者刷新失败
   */
  private notifyError(error: Error): void {
    this.subscribers.forEach((subscriber) => subscriber.reject(error))
  }

  /**
   * 检查是否正在刷新
   */
  isRefreshingToken(): boolean {
    return this.isRefreshing
  }
}

// 导出单例
export const tokenRefreshManager = new TokenRefreshManager()
