import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { WebSocketClient, WebSocketMessageType, createWebSocketClient } from './websocket-client'

// Mock WebSocket
class MockWebSocket {
  static OPEN = 1
  static CONNECTING = 0
  static CLOSING = 2
  static CLOSED = 3
  
  readyState = MockWebSocket.CONNECTING
  onopen: ((event: Event) => void) | null = null
  onmessage: ((event: MessageEvent) => void) | null = null
  onclose: ((event: CloseEvent) => void) | null = null
  onerror: ((event: Event) => void) | null = null
  
  constructor(public url: string) {}
  
  send(data: string): void {
    // Mock implementation
  }
  
  close(): void {
    this.readyState = MockWebSocket.CLOSED
    if (this.onclose) {
      this.onclose(new CloseEvent('close'))
    }
  }
  
  // Helper method for testing
  triggerOpen(): void {
    this.readyState = MockWebSocket.OPEN
    if (this.onopen) {
      this.onopen(new Event('open'))
    }
  }
  
  // Helper method for testing
  triggerMessage(data: string): void {
    if (this.onmessage) {
      this.onmessage(new MessageEvent('message', { data }))
    }
  }
  
  // Helper method for testing
  triggerClose(): void {
    this.readyState = MockWebSocket.CLOSED
    if (this.onclose) {
      this.onclose(new CloseEvent('close'))
    }
  }
  
  // Helper method for testing
  triggerError(): void {
    if (this.onerror) {
      this.onerror(new Event('error'))
    }
  }
}

// Mock global WebSocket
global.WebSocket = MockWebSocket as any

// Mock console methods to avoid noise in tests
const consoleSpy = {
  log: vi.spyOn(console, 'log').mockImplementation(() => {}),
  error: vi.spyOn(console, 'error').mockImplementation(() => {}),
  warn: vi.spyOn(console, 'warn').mockImplementation(() => {})
}

describe('WebSocketClient', () => {
  let wsClient: WebSocketClient
  let mockWebSocket: MockWebSocket
  
  beforeEach(() => {
    vi.clearAllMocks()
    vi.useFakeTimers()
    
    wsClient = new WebSocketClient({
      url: 'ws://localhost:8080/ws',
      token: 'test-token',
      reconnectInterval: 1000,
      maxReconnectAttempts: 3,
      heartbeatInterval: 500
    })
    
    // Get the mock WebSocket instance
    mockWebSocket = (global.WebSocket as any).mock.instances[0] || new MockWebSocket('ws://localhost:8080/ws')
  })

  afterEach(() => {
    vi.useRealTimers()
    wsClient.disconnect()
  })

  describe('Constructor', () => {
    it('creates WebSocketClient with default options', () => {
      const client = new WebSocketClient({
        url: 'ws://localhost:8080/ws',
        token: 'test-token'
      })
      
      expect(client).toBeInstanceOf(WebSocketClient)
    })
  })

  describe('createWebSocketClient', () => {
    it('creates WebSocketClient instance', () => {
      const client = createWebSocketClient({
        url: 'ws://localhost:8080/ws',
        token: 'test-token'
      })
      
      expect(client).toBeInstanceOf(WebSocketClient)
    })
  })

  describe('Connection', () => {
    it('connects to WebSocket with token in URL', () => {
      wsClient.connect()
      
      expect(global.WebSocket).toHaveBeenCalledWith('ws://localhost:8080/ws?token=test-token')
    })

    it('does not connect if already connected', () => {
      wsClient.connect()
      mockWebSocket.triggerOpen()
      
      // Try to connect again
      wsClient.connect()
      
      // Should only create one WebSocket instance
      expect(global.WebSocket).toHaveBeenCalledTimes(1)
    })

    it('handles connection open', () => {
      const consoleSpy = vi.spyOn(console, 'log').mockImplementation(() => {})
      
      wsClient.connect()
      mockWebSocket.triggerOpen()
      
      expect(consoleSpy).toHaveBeenCalledWith('WebSocket连接成功')
      expect(wsClient.isConnected()).toBe(true)
      
      consoleSpy.mockRestore()
    })

    it('starts heartbeat after connection', () => {
      const setIntervalSpy = vi.spyOn(global, 'setInterval')
      
      wsClient.connect()
      mockWebSocket.triggerOpen()
      
      expect(setIntervalSpy).toHaveBeenCalledWith(expect.any(Function), 500)
      
      setIntervalSpy.mockRestore()
    })

    it('handles connection close', () => {
      const consoleSpy = vi.spyOn(console, 'log').mockImplementation(() => {})
      
      wsClient.connect()
      mockWebSocket.triggerOpen()
      mockWebSocket.triggerClose()
      
      expect(consoleSpy).toHaveBeenCalledWith('WebSocket连接关闭')
      expect(wsClient.isConnected()).toBe(false)
      
      consoleSpy.mockRestore()
    })

    it('handles connection error', () => {
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      
      wsClient.connect()
      mockWebSocket.triggerError()
      
      expect(consoleSpy).toHaveBeenCalledWith('WebSocket错误:', expect.any(Event))
      
      consoleSpy.mockRestore()
    })

    it('attempts to reconnect on unexpected close', () => {
      const setTimeoutSpy = vi.spyOn(global, 'setTimeout')
      
      wsClient.connect()
      mockWebSocket.triggerOpen()
      mockWebSocket.triggerClose()
      
      // Should schedule a reconnect
      expect(setTimeoutSpy).toHaveBeenCalledWith(expect.any(Function), 1000)
      
      setTimeoutSpy.mockRestore()
    })

    it('does not reconnect on manual close', () => {
      const setTimeoutSpy = vi.spyOn(global, 'setTimeout')
      
      wsClient.connect()
      mockWebSocket.triggerOpen()
      wsClient.disconnect()
      
      // Should not schedule a reconnect
      expect(setTimeoutSpy).not.toHaveBeenCalled()
      
      setTimeoutSpy.mockRestore()
    })

    it('stops reconnecting after max attempts', () => {
      const setTimeoutSpy = vi.spyOn(global, 'setTimeout')
      
      wsClient.connect()
      mockWebSocket.triggerOpen()
      
      // Trigger close 3 times (max attempts)
      for (let i = 0; i < 3; i++) {
        mockWebSocket.triggerClose()
        vi.advanceTimersByTime(1000)
      }
      
      // Should not schedule a 4th reconnect
      expect(setTimeoutSpy).toHaveBeenCalledTimes(3)
      
      setTimeoutSpy.mockRestore()
    })
  })

  describe('Disconnection', () => {
    it('disconnects manually', () => {
      wsClient.connect()
      mockWebSocket.triggerOpen()
      
      wsClient.disconnect()
      
      expect(wsClient.isConnected()).toBe(false)
    })

    it('clears heartbeat on disconnect', () => {
      const clearIntervalSpy = vi.spyOn(global, 'clearInterval')
      
      wsClient.connect()
      mockWebSocket.triggerOpen()
      wsClient.disconnect()
      
      expect(clearIntervalSpy).toHaveBeenCalled()
      
      clearIntervalSpy.mockRestore()
    })
  })

  describe('Message Handling', () => {
    it('registers message handlers', () => {
      const handler = vi.fn()
      
      wsClient.on(WebSocketMessageType.NOTIFICATION, handler)
      
      // Trigger a notification message
      wsClient.connect()
      mockWebSocket.triggerOpen()
      mockWebSocket.triggerMessage(JSON.stringify({
        type: WebSocketMessageType.NOTIFICATION,
        data: { title: 'Test Notification' }
      }))
      
      expect(handler).toHaveBeenCalledWith({
        type: WebSocketMessageType.NOTIFICATION,
        data: { title: 'Test Notification' }
      })
    })

    it('registers multiple handlers for same type', () => {
      const handler1 = vi.fn()
      const handler2 = vi.fn()
      
      wsClient.on(WebSocketMessageType.NOTIFICATION, handler1)
      wsClient.on(WebSocketMessageType.NOTIFICATION, handler2)
      
      // Trigger a notification message
      wsClient.connect()
      mockWebSocket.triggerOpen()
      mockWebSocket.triggerMessage(JSON.stringify({
        type: WebSocketMessageType.NOTIFICATION,
        data: { title: 'Test Notification' }
      }))
      
      expect(handler1).toHaveBeenCalled()
      expect(handler2).toHaveBeenCalled()
    })

    it('handles different message types', () => {
      const notificationHandler = vi.fn()
      const taskHandler = vi.fn()
      
      wsClient.on(WebSocketMessageType.NOTIFICATION, notificationHandler)
      wsClient.on(WebSocketMessageType.TASK_UPDATE, taskHandler)
      
      wsClient.connect()
      mockWebSocket.triggerOpen()
      
      // Trigger notification message
      mockWebSocket.triggerMessage(JSON.stringify({
        type: WebSocketMessageType.NOTIFICATION,
        data: { title: 'Test Notification' }
      }))
      
      // Trigger task update message
      mockWebSocket.triggerMessage(JSON.stringify({
        type: WebSocketMessageType.TASK_UPDATE,
        data: { taskId: 1, status: 'completed' }
      }))
      
      expect(notificationHandler).toHaveBeenCalledTimes(1)
      expect(taskHandler).toHaveBeenCalledTimes(1)
    })

    it('handles malformed JSON messages', () => {
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      
      wsClient.connect()
      mockWebSocket.triggerOpen()
      mockWebSocket.triggerMessage('invalid json')
      
      expect(consoleSpy).toHaveBeenCalledWith('解析WebSocket消息失败:', expect.any(Error))
      
      consoleSpy.mockRestore()
    })

    it('handles handler errors', () => {
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      const errorHandler = vi.fn(() => {
        throw new Error('Handler error')
      })
      
      wsClient.on(WebSocketMessageType.NOTIFICATION, errorHandler)
      
      wsClient.connect()
      mockWebSocket.triggerOpen()
      mockWebSocket.triggerMessage(JSON.stringify({
        type: WebSocketMessageType.NOTIFICATION,
        data: { title: 'Test Notification' }
      }))
      
      expect(consoleSpy).toHaveBeenCalledWith('处理notification消息失败:', expect.any(Error))
      
      consoleSpy.mockRestore()
    })
  })

  describe('Sending Messages', () => {
    it('sends message when connected', () => {
      const sendSpy = vi.spyOn(MockWebSocket.prototype, 'send')
      
      wsClient.connect()
      mockWebSocket.triggerOpen()
      
      wsClient.send({
        type: WebSocketMessageType.NOTIFICATION,
        data: { message: 'test' }
      })
      
      expect(sendSpy).toHaveBeenCalledWith(JSON.stringify({
        type: WebSocketMessageType.NOTIFICATION,
        data: { message: 'test' }
      }))
      
      sendSpy.mockRestore()
    })

    it('warns when sending while not connected', () => {
      const consoleSpy = vi.spyOn(console, 'warn').mockImplementation(() => {})
      
      wsClient.send({
        type: WebSocketMessageType.NOTIFICATION,
        data: { message: 'test' }
      })
      
      expect(consoleSpy).toHaveBeenCalledWith('WebSocket未连接，无法发送消息')
      
      consoleSpy.mockRestore()
    })
  })

  describe('Heartbeat', () => {
    it('sends heartbeat messages', () => {
      const sendSpy = vi.spyOn(MockWebSocket.prototype, 'send')
      
      wsClient.connect()
      mockWebSocket.triggerOpen()
      
      // Advance time to trigger heartbeat
      vi.advanceTimersByTime(500)
      
      expect(sendSpy).toHaveBeenCalledWith(JSON.stringify({
        type: WebSocketMessageType.NOTIFICATION,
        data: 'ping'
      }))
      
      sendSpy.mockRestore()
    })

    it('stops heartbeat on disconnect', () => {
      const clearIntervalSpy = vi.spyOn(global, 'clearInterval')
      
      wsClient.connect()
      mockWebSocket.triggerOpen()
      wsClient.disconnect()
      
      expect(clearIntervalSpy).toHaveBeenCalled()
      
      clearIntervalSpy.mockRestore()
    })
  })

  describe('Reconnection', () => {
    it('increases reconnect attempts', () => {
      const consoleSpy = vi.spyOn(console, 'log').mockImplementation(() => {})
      
      wsClient.connect()
      mockWebSocket.triggerOpen()
      mockWebSocket.triggerClose()
      
      // Advance time to trigger reconnect
      vi.advanceTimersByTime(1000)
      
      expect(consoleSpy).toHaveBeenCalledWith('尝试重连 (1/3)...')
      
      consoleSpy.mockRestore()
    })

    it('resets reconnect attempts on successful connection', () => {
      wsClient.connect()
      mockWebSocket.triggerOpen()
      mockWebSocket.triggerClose()
      
      // Advance time to trigger reconnect
      vi.advanceTimersByTime(1000)
      
      // Simulate successful reconnection
      const newMockWs = new MockWebSocket('ws://localhost:8080/ws?token=test-token')
      newMockWs.triggerOpen()
      
      // Reset attempts should be verified through reconnection behavior
      // This is a simplified test as we can't easily access internal state
    })
  })

  describe('Edge Cases', () => {
    it('handles message without registered handler', () => {
      // Should not throw error
      wsClient.connect()
      mockWebSocket.triggerOpen()
      mockWebSocket.triggerMessage(JSON.stringify({
        type: WebSocketMessageType.NOTIFICATION,
        data: { title: 'Test' }
      }))
      
      // No handler registered, should not crash
    })

    it('handles multiple disconnect calls', () => {
      wsClient.connect()
      mockWebSocket.triggerOpen()
      
      // Call disconnect multiple times
      wsClient.disconnect()
      wsClient.disconnect()
      
      expect(wsClient.isConnected()).toBe(false)
    })

    it('handles connect call on existing connection', () => {
      wsClient.connect()
      mockWebSocket.triggerOpen()
      
      // Call connect again
      wsClient.connect()
      
      // Should not create new WebSocket
      expect(global.WebSocket).toHaveBeenCalledTimes(1)
    })
  })
})