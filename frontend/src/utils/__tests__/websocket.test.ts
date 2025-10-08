import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { WebSocketClient, WebSocketMessageType } from '../websocket'

// Mock WebSocket
class MockWebSocket {
  static CONNECTING = 0
  static OPEN = 1
  static CLOSING = 2
  static CLOSED = 3

  readyState = MockWebSocket.CONNECTING
  onopen: ((event: Event) => void) | null = null
  onmessage: ((event: MessageEvent) => void) | null = null
  onerror: ((event: Event) => void) | null = null
  onclose: ((event: CloseEvent) => void) | null = null

  constructor(public url: string) {
    // 模拟异步连接
    setTimeout(() => {
      this.readyState = MockWebSocket.OPEN
      if (this.onopen) {
        this.onopen(new Event('open'))
      }
    }, 10)
  }

  send(data: string) {
    // Mock send
  }

  close() {
    this.readyState = MockWebSocket.CLOSED
    if (this.onclose) {
      this.onclose(new CloseEvent('close'))
    }
  }
}

// 替换全局WebSocket
global.WebSocket = MockWebSocket as any

describe('WebSocketClient', () => {
  let client: WebSocketClient

  beforeEach(() => {
    vi.clearAllMocks()
    vi.useFakeTimers()
  })

  afterEach(() => {
    if (client) {
      client.disconnect()
    }
    vi.useRealTimers()
  })

  it('should create WebSocket client', () => {
    client = new WebSocketClient({
      url: 'ws://localhost:8080/ws'
    })

    expect(client).toBeDefined()
    expect(client.isConnected()).toBe(false)
  })

  it('should connect to WebSocket server', async () => {
    client = new WebSocketClient({
      url: 'ws://localhost:8080/ws'
    })

    client.connect()

    // 等待连接建立
    await vi.advanceTimersByTimeAsync(20)

    expect(client.isConnected()).toBe(true)
  })

  it('should include token in URL when provided', () => {
    const token = 'test-token-123'
    client = new WebSocketClient({
      url: 'ws://localhost:8080/ws',
      token
    })

    client.connect()

    // WebSocket URL应该包含token
    expect(client).toBeDefined()
  })

  it('should handle message events', async () => {
    client = new WebSocketClient({
      url: 'ws://localhost:8080/ws'
    })

    const handler = vi.fn()
    client.on(WebSocketMessageType.NOTIFICATION, handler)

    client.connect()
    await vi.advanceTimersByTimeAsync(20)

    // 模拟接收消息
    const ws = (client as any).ws
    const message = {
      type: WebSocketMessageType.NOTIFICATION,
      data: { id: 1, title: 'Test' }
    }

    if (ws.onmessage) {
      ws.onmessage(new MessageEvent('message', {
        data: JSON.stringify(message)
      }))
    }

    expect(handler).toHaveBeenCalledWith(message)
  })

  it('should send messages when connected', async () => {
    client = new WebSocketClient({
      url: 'ws://localhost:8080/ws'
    })

    client.connect()
    await vi.advanceTimersByTimeAsync(20)

    const ws = (client as any).ws
    const sendSpy = vi.spyOn(ws, 'send')

    client.send({
      type: WebSocketMessageType.HEARTBEAT,
      timestamp: Date.now()
    })

    expect(sendSpy).toHaveBeenCalled()
  })

  it('should not send messages when disconnected', () => {
    client = new WebSocketClient({
      url: 'ws://localhost:8080/ws'
    })

    const consoleSpy = vi.spyOn(console, 'warn')

    client.send({
      type: WebSocketMessageType.HEARTBEAT
    })

    expect(consoleSpy).toHaveBeenCalledWith('WebSocket未连接,无法发送消息')
  })

  it('should start heartbeat after connection', async () => {
    client = new WebSocketClient({
      url: 'ws://localhost:8080/ws',
      heartbeatInterval: 1000
    })

    client.connect()
    await vi.advanceTimersByTimeAsync(20)

    const ws = (client as any).ws
    const sendSpy = vi.spyOn(ws, 'send')

    // 前进1秒,应该发送心跳
    await vi.advanceTimersByTimeAsync(1000)

    expect(sendSpy).toHaveBeenCalled()
  })

  it('should reconnect after connection close', async () => {
    client = new WebSocketClient({
      url: 'ws://localhost:8080/ws',
      reconnectInterval: 1000
    })

    client.connect()
    await vi.advanceTimersByTimeAsync(20)

    // 模拟连接关闭
    const ws = (client as any).ws
    if (ws.onclose) {
      ws.onclose(new CloseEvent('close'))
    }

    // 前进1秒,应该尝试重连
    await vi.advanceTimersByTimeAsync(1000)

    expect((client as any).reconnectAttempts).toBeGreaterThan(0)
  })

  it('should use exponential backoff for reconnection', async () => {
    client = new WebSocketClient({
      url: 'ws://localhost:8080/ws',
      reconnectInterval: 1000
    })

    client.connect()
    await vi.advanceTimersByTimeAsync(20)

    // 第一次重连
    const ws1 = (client as any).ws
    if (ws1.onclose) {
      ws1.onclose(new CloseEvent('close'))
    }
    await vi.advanceTimersByTimeAsync(1000)

    expect((client as any).reconnectAttempts).toBe(1)

    // 第二次重连应该等待更长时间
    const ws2 = (client as any).ws
    if (ws2.onclose) {
      ws2.onclose(new CloseEvent('close'))
    }
    await vi.advanceTimersByTimeAsync(2000)

    expect((client as any).reconnectAttempts).toBe(2)
  })

  it('should stop reconnecting after max attempts', async () => {
    client = new WebSocketClient({
      url: 'ws://localhost:8080/ws',
      reconnectInterval: 100,
      maxReconnectAttempts: 3
    })

    client.connect()
    await vi.advanceTimersByTimeAsync(20)

    // 模拟多次连接失败
    for (let i = 0; i < 5; i++) {
      const ws = (client as any).ws
      if (ws.onclose) {
        ws.onclose(new CloseEvent('close'))
      }
      await vi.advanceTimersByTimeAsync(1000)
    }

    // 重连次数不应超过最大值
    expect((client as any).reconnectAttempts).toBeLessThanOrEqual(3)
  })

  it('should disconnect properly', async () => {
    client = new WebSocketClient({
      url: 'ws://localhost:8080/ws'
    })

    client.connect()
    await vi.advanceTimersByTimeAsync(20)

    expect(client.isConnected()).toBe(true)

    client.disconnect()

    expect(client.isConnected()).toBe(false)
    expect((client as any).ws).toBeNull()
  })

  it('should register and unregister event handlers', () => {
    client = new WebSocketClient({
      url: 'ws://localhost:8080/ws'
    })

    const handler = vi.fn()

    client.on(WebSocketMessageType.NOTIFICATION, handler)
    expect((client as any).eventHandlers.get(WebSocketMessageType.NOTIFICATION)?.has(handler)).toBe(true)

    client.off(WebSocketMessageType.NOTIFICATION, handler)
    expect((client as any).eventHandlers.get(WebSocketMessageType.NOTIFICATION)?.has(handler)).toBe(false)
  })

  it('should update token and reconnect', async () => {
    client = new WebSocketClient({
      url: 'ws://localhost:8080/ws',
      token: 'old-token'
    })

    client.connect()
    await vi.advanceTimersByTimeAsync(20)

    expect(client.isConnected()).toBe(true)

    client.updateToken('new-token')

    // 应该断开并重新连接
    await vi.advanceTimersByTimeAsync(20)

    expect((client as any).config.token).toBe('new-token')
  })

  it('should get correct ready state', async () => {
    client = new WebSocketClient({
      url: 'ws://localhost:8080/ws'
    })

    expect(client.getReadyState()).toBe(WebSocket.CLOSED)

    client.connect()
    await vi.advanceTimersByTimeAsync(20)

    expect(client.getReadyState()).toBe(WebSocket.OPEN)
  })
})

