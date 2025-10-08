/**
 * WebSocket工具类测试
 */

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { WebSocketManager, initWebSocket, getWebSocketManager, closeWebSocket } from './websocket';

// Mock WebSocket
class MockWebSocket {
  static CONNECTING = 0;
  static OPEN = 1;
  static CLOSING = 2;
  static CLOSED = 3;

  readyState = MockWebSocket.CONNECTING;
  url: string;
  onopen: ((event: Event) => void) | null = null;
  onmessage: ((event: MessageEvent) => void) | null = null;
  onclose: ((event: CloseEvent) => void) | null = null;
  onerror: ((event: Event) => void) | null = null;

  constructor(url: string) {
    this.url = url;
    
    // 模拟连接成功
    setTimeout(() => {
      this.readyState = MockWebSocket.OPEN;
      if (this.onopen) {
        this.onopen(new Event('open'));
      }
    }, 100);
  }

  send(data: string): void {
    // 模拟收到响应
    if (this.onmessage) {
      try {
        const message = JSON.parse(data);
        if (message.type === 'heartbeat') {
          this.onmessage(new MessageEvent('message', {
            data: JSON.stringify({
              type: 'heartbeat',
              content: 'pong',
              timestamp: new Date().toISOString()
            })
          }));
        }
      } catch (e) {
        // 忽略解析错误
      }
    }
  }

  close(): void {
    this.readyState = MockWebSocket.CLOSED;
    if (this.onclose) {
      this.onclose(new CloseEvent('close'));
    }
  }

  // 模拟服务器发送消息
  simulateServerMessage(message: any): void {
    if (this.onmessage && this.readyState === MockWebSocket.OPEN) {
      this.onmessage(new MessageEvent('message', {
        data: JSON.stringify(message)
      }));
    }
  }
}

// 替换全局WebSocket
(global as any).WebSocket = MockWebSocket;

// 创建mock函数来跟踪WebSocket实例
const mockWebSocketTracker = {
  instances: [] as MockWebSocket[],
  clearInstances: () => {
    mockWebSocketTracker.instances = [];
  }
};

// 保存原始构造函数
const originalWebSocket = MockWebSocket;

// 重写构造函数以跟踪实例
const MockWebSocketWithTracker = function(url: string) {
  const instance = new originalWebSocket(url);
  mockWebSocketTracker.instances.push(instance);
  return instance;
} as any;

// 复制静态属性
Object.setPrototypeOf(MockWebSocketWithTracker, originalWebSocket);
Object.getOwnPropertyNames(originalWebSocket).forEach(name => {
  if (name !== 'prototype' && name !== 'name' && name !== 'length') {
    (MockWebSocketWithTracker as any)[name] = (originalWebSocket as any)[name];
  }
});

// 替换全局WebSocket
(global as any).WebSocket = MockWebSocketWithTracker;

describe('WebSocket工具类测试', () => {
  let wsManager: WebSocketManager | null = null;

  beforeEach(() => {
    // 清理之前的连接
    closeWebSocket();
    mockWebSocketTracker.clearInstances();
    vi.useFakeTimers();
  });

  afterEach(() => {
    closeWebSocket();
    vi.useRealTimers();
  });

  it('应该能够创建WebSocketManager实例', () => {
    wsManager = new WebSocketManager({
      url: 'ws://localhost:8080/ws/notifications',
      token: 'userId:123'
    });

    expect(wsManager).toBeInstanceOf(WebSocketManager);
  });

  it('应该能够初始化WebSocket连接', (done) => {
    wsManager = initWebSocket({
      url: 'ws://localhost:8080/ws/notifications',
      token: 'userId:123',
      onConnect: () => {
        console.log('连接成功');
      }
    });

    expect(wsManager).toBeInstanceOf(WebSocketManager);
    expect(getWebSocketManager()).toBe(wsManager);
  });

  it('应该能够发送消息', () => {
    return new Promise<void>((resolve) => {
      wsManager = initWebSocket({
        url: 'ws://localhost:8080/ws/notifications',
        token: 'userId:123',
        onConnect: () => {
          // 发送心跳消息
          wsManager!.send({ type: 'heartbeat', content: 'ping' });
        },
        onMessage: (message) => {
          if (message.type === 'heartbeat') {
            expect(message.content).toBe('pong');
            resolve();
          }
        }
      });

      // 快进时间，触发连接成功
      vi.advanceTimersByTime(100);
      
      // 快进时间，触发消息响应
      vi.advanceTimersByTime(100);
    });
  });

  it('应该能够处理通知消息', () => {
    return new Promise<void>((resolve) => {
      wsManager = initWebSocket({
        url: 'ws://localhost:8080/ws/notifications',
        token: 'userId:123',
        onMessage: (message) => {
          if (message.type === 'notification') {
            expect(message.title).toBe('测试通知');
            expect(message.content).toBe('测试内容');
            resolve();
          }
        }
      });

      // 快进时间，触发连接成功
      vi.advanceTimersByTime(100);
      
      // 模拟服务器发送通知消息
      const mockWs = mockWebSocketTracker.instances[0];
      mockWs.simulateServerMessage({
        type: 'notification',
        title: '测试通知',
        content: '测试内容',
        timestamp: new Date().toISOString()
      });
    });
  });

  it('应该能够处理系统消息', () => {
    return new Promise<void>((resolve) => {
      wsManager = initWebSocket({
        url: 'ws://localhost:8080/ws/notifications',
        token: 'userId:123',
        onMessage: (message) => {
          if (message.type === 'system') {
            expect(message.title).toBe('系统消息');
            expect(message.content).toBe('系统内容');
            resolve();
          }
        }
      });

      // 快进时间，触发连接成功
      vi.advanceTimersByTime(100);
      
      // 模拟服务器发送系统消息
      const mockWs = mockWebSocketTracker.instances[0];
      mockWs.simulateServerMessage({
        type: 'system',
        title: '系统消息',
        content: '系统内容',
        timestamp: new Date().toISOString()
      });
    });
  });

  it('应该能够处理错误消息', () => {
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation();
    
    return new Promise<void>((resolve) => {
      wsManager = initWebSocket({
        url: 'ws://localhost:8080/ws/notifications',
        token: 'userId:123',
        onMessage: (message) => {
          if (message.type === 'error') {
            expect(message.content).toBe('错误信息');
            consoleSpy.mockRestore();
            resolve();
          }
        }
      });

      // 快进时间，触发连接成功
      vi.advanceTimersByTime(100);
      
      // 模拟服务器发送错误消息
      const mockWs = mockWebSocketTracker.instances[0];
      mockWs.simulateServerMessage({
        type: 'error',
        content: '错误信息',
        timestamp: new Date().toISOString()
      });
    });
  });

  it('应该能够断开连接', () => {
    wsManager = initWebSocket({
      url: 'ws://localhost:8080/ws/notifications',
      token: 'userId:123'
    });

    // 断开连接
    closeWebSocket();
    expect(getWebSocketManager()).toBeNull();
  });

  it('应该能够启动心跳检测', () => {
    const sendSpy = vi.fn();
    
    // Mock WebSocket的send方法
    const originalSend = MockWebSocket.prototype.send;
    MockWebSocket.prototype.send = function(data: string) {
      sendSpy(data);
      return originalSend.call(this, data);
    };

    wsManager = initWebSocket({
      url: 'ws://localhost:8080/ws/notifications',
      token: 'userId:123',
      heartbeatInterval: 1000 // 1秒心跳间隔
    });

    // 快进时间，触发连接成功
    vi.advanceTimersByTime(100);
    
    // 快进时间，触发心跳
    vi.advanceTimersByTime(1000);
    
    // 验证心跳消息已发送
    expect(sendSpy).toHaveBeenCalledWith(JSON.stringify({
      type: 'heartbeat',
      content: 'ping'
    }));

    // 恢复原方法
    MockWebSocket.prototype.send = originalSend;
  });

  it('应该能够处理连接错误', () => {
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation();
    
    return new Promise<void>((resolve) => {
      wsManager = initWebSocket({
        url: 'ws://localhost:8080/ws/notifications',
        token: 'userId:123',
        onError: (error) => {
          consoleSpy.mockRestore();
          resolve();
        }
      });

      // 快进时间，触发连接成功
      vi.advanceTimersByTime(100);
      
      // 模拟连接错误
      const mockWs = mockWebSocketTracker.instances[0];
      if (mockWs.onerror) {
        mockWs.onerror(new Event('error'));
      }
    });
  });

  it('应该能够处理连接关闭', () => {
    return new Promise((resolve) => {
      wsManager = initWebSocket({
        url: 'ws://localhost:8080/ws/notifications',
        token: 'userId:123',
        onDisconnect: () => {
          resolve(null);
        }
      });

      // 快进时间，触发连接成功
      vi.advanceTimersByTime(100);
      
      // 模拟连接关闭
    const mockWs = mockWebSocketTracker.instances[0];
      mockWs.close();
    });
  });

  describe('自动重连机制', () => {
    it('当连接意外断开时应该尝试重连', () => {
      const onConnectCallback = vi.fn();
      wsManager = initWebSocket({
        url: 'ws://localhost:8080/ws/notifications',
        token: 'userId:123',
        reconnectInterval: 1000, // 1秒后重连
        onConnect: onConnectCallback,
      });

      // 1. 初始连接
      vi.advanceTimersByTime(100);
      expect(onConnectCallback).toHaveBeenCalledTimes(1);
      const firstInstance = mockWebSocketTracker.instances[0];

      // 2. 模拟意外断开
      firstInstance.close();

      // 3. 快进时间以触发重连
      vi.advanceTimersByTime(1000);
      vi.advanceTimersByTime(100); // 等待重连成功

      // 4. 验证是否创建了新的WebSocket实例并再次调用onConnect
      expect(mockWebSocketTracker.instances.length).toBe(2);
      expect(onConnectCallback).toHaveBeenCalledTimes(2);
    });

    it('在达到最大重连次数后应该停止重连', () => {
        const maxAttempts = 3;
        wsManager = initWebSocket({
            url: 'ws://localhost:8080/ws/notifications',
            token: 'userId:123',
            reconnectInterval: 1000,
            maxReconnectAttempts: maxAttempts,
        });

        // 初始连接
        vi.advanceTimersByTime(100);
        expect(mockWebSocketTracker.instances.length).toBe(1);

        // 模拟连续断开
        for (let i = 0; i < maxAttempts; i++) {
            mockWebSocketTracker.instances[i].close();
            vi.advanceTimersByTime(1000); // 触发重连
            vi.advanceTimersByTime(100); // 等待连接
        }

        // 验证重连了 maxAttempts 次
        expect(mockWebSocketTracker.instances.length).toBe(maxAttempts + 1);

        // 再次模拟断开
        mockWebSocketTracker.instances[maxAttempts].close();
        vi.advanceTimersByTime(1000); // 快进时间

        // 验证没有再次重连
        expect(mockWebSocketTracker.instances.length).toBe(maxAttempts + 1);
    });

    it('手动断开连接时不应该重连', () => {
        wsManager = initWebSocket({
            url: 'ws://localhost:8080/ws/notifications',
            token: 'userId:123',
            reconnectInterval: 1000,
        });

        // 初始连接
        vi.advanceTimersByTime(100);
        expect(mockWebSocketTracker.instances.length).toBe(1);

        // 手动断开
        wsManager.disconnect();

        // 快进时间
        vi.advanceTimersByTime(2000);

        // 验证没有创建新的连接
        expect(mockWebSocketTracker.instances.length).toBe(1);
    });
  });
});