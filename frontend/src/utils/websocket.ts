/**
 * WebSocket工具类
 * 处理WebSocket连接和心跳检测
 */

export interface WebSocketMessage {
  type: string;
  content?: any;
  title?: string;
  relatedId?: number;
  relatedType?: string;
  timestamp?: string;
}

export interface WebSocketOptions {
  url: string;
  token: string;
  heartbeatInterval?: number; // 心跳间隔，默认30秒
  reconnectInterval?: number; // 重连间隔，默认5秒
  maxReconnectAttempts?: number; // 最大重连次数，默认10次
  onMessage?: (message: WebSocketMessage) => void;
  onConnect?: () => void;
  onDisconnect?: () => void;
  onError?: (error: Event) => void;
}

export class WebSocketManager {
  private ws: WebSocket | null = null;
  private options: Required<WebSocketOptions>;
  private heartbeatTimer: number | null = null;
  private reconnectTimer: number | null = null;
  private reconnectAttempts = 0;
  private isManualClose = false;

  constructor(options: WebSocketOptions) {
    this.options = {
      heartbeatInterval: 30000, // 30秒
      reconnectInterval: 5000, // 5秒
      maxReconnectAttempts: 10,
      onMessage: () => {},
      onConnect: () => {},
      onDisconnect: () => {},
      onError: () => {},
      ...options,
    };
  }

  /**
   * 连接WebSocket
   */
  connect(): void {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      return;
    }

    const url = `${this.options.url}?token=${this.options.token}`;
    this.ws = new WebSocket(url);
    this.isManualClose = false;

    this.ws.onopen = () => {
      console.log('WebSocket连接成功');
      this.reconnectAttempts = 0;
      this.startHeartbeat();
      this.options.onConnect();
    };

    this.ws.onmessage = (event) => {
      try {
        const message: WebSocketMessage = JSON.parse(event.data);
        this.handleMessage(message);
        this.options.onMessage(message);
      } catch (error) {
        console.error('解析WebSocket消息失败:', error);
      }
    };

    this.ws.onclose = () => {
      console.log('WebSocket连接关闭');
      this.stopHeartbeat();
      this.options.onDisconnect();
      
      // 如果不是手动关闭，尝试重连
      if (!this.isManualClose && this.reconnectAttempts < this.options.maxReconnectAttempts) {
        this.scheduleReconnect();
      }
    };

    this.ws.onerror = (error) => {
      console.error('WebSocket错误:', error);
      this.options.onError(error);
    };
  }

  /**
   * 断开连接
   */
  disconnect(): void {
    this.isManualClose = true;
    this.stopHeartbeat();
    this.clearReconnectTimer();
    
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
  }

  /**
   * 发送消息
   */
  send(message: WebSocketMessage): void {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(message));
    } else {
      console.warn('WebSocket未连接，无法发送消息');
    }
  }

  /**
   * 处理收到的消息
   */
  private handleMessage(message: WebSocketMessage): void {
    switch (message.type) {
      case 'heartbeat':
        // 收到心跳响应，不需要特殊处理
        break;
      case 'notification':
        // 处理通知消息
        this.handleNotification(message);
        break;
      case 'system':
        // 处理系统消息
        this.handleSystemMessage(message);
        break;
      case 'error':
        // 处理错误消息
        console.error('WebSocket错误消息:', message.content);
        break;
      default:
        console.log('未知消息类型:', message.type);
    }
  }

  /**
   * 处理通知消息
   */
  private handleNotification(message: WebSocketMessage): void {
    // 这里可以集成通知系统，例如显示通知
    if ('Notification' in window && Notification.permission === 'granted') {
      new Notification(message.title || '新通知', {
        body: typeof message.content === 'string' ? message.content : JSON.stringify(message.content),
        icon: '/favicon.ico',
      });
    }
  }

  /**
   * 处理系统消息
   */
  private handleSystemMessage(message: WebSocketMessage): void {
    console.log('系统消息:', message.title, message.content);
  }

  /**
   * 开始心跳检测
   */
  private startHeartbeat(): void {
    this.stopHeartbeat();
    this.heartbeatTimer = window.setInterval(() => {
      if (this.ws && this.ws.readyState === WebSocket.OPEN) {
        this.send({ type: 'heartbeat', content: 'ping' });
      }
    }, this.options.heartbeatInterval);
  }

  /**
   * 停止心跳检测
   */
  private stopHeartbeat(): void {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer);
      this.heartbeatTimer = null;
    }
  }

  /**
   * 安排重连
   */
  private scheduleReconnect(): void {
    this.clearReconnectTimer();
    this.reconnectAttempts++;
    
    console.log(`尝试重连 (${this.reconnectAttempts}/${this.options.maxReconnectAttempts})...`);
    
    this.reconnectTimer = window.setTimeout(() => {
      this.connect();
    }, this.options.reconnectInterval);
  }

  /**
   * 清除重连定时器
   */
  private clearReconnectTimer(): void {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }
  }

  /**
   * 请求通知权限
   */
  static requestNotificationPermission(): Promise<NotificationPermission> {
    if ('Notification' in window) {
      return Notification.requestPermission();
    }
    return Promise.resolve('denied');
  }
}

// 创建全局WebSocket管理器实例
let wsManager: WebSocketManager | null = null;

/**
 * 初始化WebSocket连接
 */
export function initWebSocket(options: WebSocketOptions): WebSocketManager {
  if (wsManager) {
    wsManager.disconnect();
  }
  
  wsManager = new WebSocketManager(options);
  wsManager.connect();
  
  return wsManager;
}

/**
 * 获取全局WebSocket管理器实例
 */
export function getWebSocketManager(): WebSocketManager | null {
  return wsManager;
}

/**
 * 关闭全局WebSocket连接
 */
export function closeWebSocket(): void {
  if (wsManager) {
    wsManager.disconnect();
    wsManager = null;
  }
}