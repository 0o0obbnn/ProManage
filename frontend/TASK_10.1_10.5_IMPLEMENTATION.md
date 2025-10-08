# 任务 10.1-10.5 实现文档

**任务**: WebSocket实时推送  
**完成日期**: 2025-10-05  
**开发者**: Claude Code

---

## 📋 任务清单

- ✅ **任务 10.1**: 创建 utils/websocket.ts
- ✅ **任务 10.2**: 实现WebSocket连接管理
- ✅ **任务 10.3**: 实现消息接收处理
- ✅ **任务 10.4**: 实现断线重连
- ✅ **任务 10.5**: 创建 stores/modules/notification.ts

---

## 🎯 任务 10.1: 创建 utils/websocket.ts

### 新增工具类

**文件路径**: `frontend/src/utils/websocket.ts`

### WebSocket客户端类

```typescript
export class WebSocketClient {
  private ws: WebSocket | null = null
  private config: Required<WebSocketConfig>
  private reconnectTimer: any = null
  private heartbeatTimer: any = null
  private reconnectAttempts = 0
  private eventHandlers: Map<WebSocketMessageType, Set<WebSocketEventHandler>> = new Map()
  private isManualClose = false

  constructor(config: WebSocketConfig) {
    this.config = {
      url: config.url,
      reconnectInterval: config.reconnectInterval || 5000,
      maxReconnectAttempts: config.maxReconnectAttempts || 10,
      heartbeatInterval: config.heartbeatInterval || 30000,
      token: config.token || ''
    }
  }
}
```

### 核心功能

#### 1. 连接管理
```typescript
connect(): void {
  if (this.ws && this.ws.readyState === WebSocket.OPEN) {
    console.log('WebSocket已连接')
    return
  }

  try {
    // 构建WebSocket URL (包含token)
    const url = this.config.token
      ? `${this.config.url}?token=${this.config.token}`
      : this.config.url

    this.ws = new WebSocket(url)
    this.isManualClose = false

    this.ws.onopen = this.handleOpen.bind(this)
    this.ws.onmessage = this.handleMessage.bind(this)
    this.ws.onerror = this.handleError.bind(this)
    this.ws.onclose = this.handleClose.bind(this)
  } catch (error) {
    console.error('WebSocket连接失败:', error)
    this.reconnect()
  }
}
```

**特性**:
- ✅ Token认证
- ✅ 自动绑定事件处理器
- ✅ 错误处理

#### 2. 消息处理
```typescript
private handleMessage(event: MessageEvent): void {
  try {
    const message: WebSocketMessage = JSON.parse(event.data)
    
    // 处理心跳响应
    if (message.type === WebSocketMessageType.HEARTBEAT) {
      return
    }

    // 触发对应类型的事件处理器
    const handlers = this.eventHandlers.get(message.type)
    if (handlers) {
      handlers.forEach(handler => {
        try {
          handler(message)
        } catch (error) {
          console.error('消息处理器执行失败:', error)
        }
      })
    }
  } catch (error) {
    console.error('解析WebSocket消息失败:', error)
  }
}
```

**特性**:
- ✅ JSON消息解析
- ✅ 类型化消息处理
- ✅ 错误隔离

---

## 🎯 任务 10.2: 实现WebSocket连接管理

### 连接生命周期

#### 1. 登录后自动连接
```typescript
// 在notification store中
const connectWebSocket = (token: string) => {
  if (wsClient.value?.isConnected()) {
    console.log('WebSocket已连接')
    return
  }

  const wsUrl = import.meta.env.VITE_WS_URL || 'ws://localhost:8080/ws'
  const fullUrl = `${wsUrl}/notifications`

  wsClient.value = createWebSocketClient({
    url: fullUrl,
    token,
    reconnectInterval: 5000,
    maxReconnectAttempts: 10,
    heartbeatInterval: 30000
  })

  wsClient.value.connect()
  isConnected.value = true
}
```

#### 2. 登出后断开连接
```typescript
const disconnectWebSocket = () => {
  if (wsClient.value) {
    wsClient.value.disconnect()
    wsClient.value = null
    isConnected.value = false
  }
}
```

#### 3. Token认证
```typescript
// 在WebSocket URL中包含token
const url = this.config.token
  ? `${this.config.url}?token=${this.config.token}`
  : this.config.url

this.ws = new WebSocket(url)
```

**特性**:
- ✅ 自动连接管理
- ✅ Token传递
- ✅ 连接状态跟踪

---

## 🎯 任务 10.3: 实现消息接收处理

### 消息类型定义

```typescript
export enum WebSocketMessageType {
  HEARTBEAT = 'heartbeat',
  NOTIFICATION = 'notification',
  TASK_UPDATE = 'task_update',
  DOCUMENT_UPDATE = 'document_update',
  CHANGE_UPDATE = 'change_update',
  COMMENT = 'comment',
  MENTION = 'mention'
}
```

### 消息处理器

```typescript
// 注册消息处理器
wsClient.value.on(WebSocketMessageType.NOTIFICATION, handleNotificationMessage)
wsClient.value.on(WebSocketMessageType.TASK_UPDATE, handleTaskUpdateMessage)
wsClient.value.on(WebSocketMessageType.DOCUMENT_UPDATE, handleDocumentUpdateMessage)
wsClient.value.on(WebSocketMessageType.CHANGE_UPDATE, handleChangeUpdateMessage)
wsClient.value.on(WebSocketMessageType.COMMENT, handleCommentMessage)
wsClient.value.on(WebSocketMessageType.MENTION, handleMentionMessage)

// 处理通知消息
const handleNotificationMessage = (message: any) => {
  const notification: Notification = message.data
  addNotification(notification)
}
```

### 更新本地状态

```typescript
const addNotification = (notification: Notification) => {
  // 添加到列表开头
  notifications.value.unshift(notification)
  
  // 更新未读数
  if (!notification.isRead) {
    unreadCount.value++
  }

  // 显示桌面通知
  if (desktopNotificationEnabled.value) {
    showDesktopNotification(notification)
  }

  // 播放音效
  if (audioEnabled.value) {
    playNotificationSound()
  }

  // 显示消息提示
  message.info({
    content: notification.title,
    duration: 3
  })
}
```

**特性**:
- ✅ 7种消息类型
- ✅ 类型化处理
- ✅ 状态更新
- ✅ UI触发

---

## 🎯 任务 10.4: 实现断线重连

### 指数退避算法

```typescript
private reconnect(): void {
  if (this.reconnectAttempts >= this.config.maxReconnectAttempts) {
    console.error('WebSocket重连次数已达上限')
    return
  }

  this.reconnectAttempts++
  
  // 指数退避算法
  const delay = Math.min(
    this.config.reconnectInterval * Math.pow(2, this.reconnectAttempts - 1),
    30000 // 最大30秒
  )

  console.log(`WebSocket将在${delay / 1000}秒后重连 (第${this.reconnectAttempts}次尝试)`)

  this.reconnectTimer = setTimeout(() => {
    this.connect()
  }, delay)
}
```

### 最大重连次数

```typescript
// 配置
maxReconnectAttempts: config.maxReconnectAttempts || 10

// 检查
if (this.reconnectAttempts >= this.config.maxReconnectAttempts) {
  console.error('WebSocket重连次数已达上限')
  return
}
```

### 心跳机制

```typescript
private startHeartbeat(): void {
  this.stopHeartbeat()
  
  this.heartbeatTimer = setInterval(() => {
    if (this.ws?.readyState === WebSocket.OPEN) {
      this.send({
        type: WebSocketMessageType.HEARTBEAT,
        timestamp: Date.now()
      })
    }
  }, this.config.heartbeatInterval)
}
```

**特性**:
- ✅ 指数退避算法
- ✅ 最大重连次数限制
- ✅ 心跳保活
- ✅ 自动重连

---

## 🎯 任务 10.5: 创建 stores/modules/notification.ts

### Notification Store

**文件路径**: `frontend/src/stores/modules/notification.ts`

### 核心功能

#### 1. WebSocket集成
```typescript
const wsClient = ref<WebSocketClient | null>(null)
const isConnected = ref(false)

const connectWebSocket = (token: string) => {
  const wsUrl = import.meta.env.VITE_WS_URL || 'ws://localhost:8080/ws'
  const fullUrl = `${wsUrl}/notifications`

  wsClient.value = createWebSocketClient({
    url: fullUrl,
    token,
    reconnectInterval: 5000,
    maxReconnectAttempts: 10,
    heartbeatInterval: 30000
  })

  wsClient.value.on(WebSocketMessageType.NOTIFICATION, handleNotificationMessage)
  wsClient.value.connect()
  isConnected.value = true
}
```

#### 2. 桌面通知
```typescript
const showDesktopNotification = (notification: Notification) => {
  if ('Notification' in window && Notification.permission === 'granted') {
    const n = new Notification(notification.title, {
      body: notification.content,
      icon: '/logo.svg',
      tag: `notification-${notification.id}`,
      requireInteraction: notification.priority === 'URGENT'
    })

    n.onclick = () => {
      window.focus()
      n.close()
    }
  }
}

const requestNotificationPermission = async () => {
  if ('Notification' in window) {
    const permission = await Notification.requestPermission()
    desktopNotificationEnabled.value = permission === 'granted'
    return permission === 'granted'
  }
  return false
}
```

#### 3. 音效提示
```typescript
const playNotificationSound = () => {
  try {
    const audio = new Audio('/sounds/notification.mp3')
    audio.volume = 0.5
    audio.play().catch(error => {
      console.warn('播放音效失败:', error)
    })
  } catch (error) {
    console.warn('播放音效失败:', error)
  }
}
```

#### 4. 设置管理
```typescript
const audioEnabled = ref(true)
const desktopNotificationEnabled = ref(false)

const toggleAudio = () => {
  audioEnabled.value = !audioEnabled.value
  localStorage.setItem('notification-audio-enabled', String(audioEnabled.value))
}

const toggleDesktopNotification = async () => {
  if (!desktopNotificationEnabled.value) {
    const granted = await requestNotificationPermission()
    if (!granted) {
      message.warning('桌面通知权限被拒绝')
    }
  } else {
    desktopNotificationEnabled.value = false
  }
}
```

---

## 📁 文件清单

### 新增文件 (3个)
1. `frontend/src/utils/websocket.ts` - WebSocket客户端 (280行)
2. `frontend/src/stores/modules/notification.ts` - 通知Store (300行)
3. `frontend/src/utils/__tests__/websocket.test.ts` - 测试文件 (300行)

### 修改文件 (1个)
1. `FRONTEND_DEVELOPMENT_PLAN.md` - 更新任务状态

---

## 🧪 测试结果

### 测试执行
```bash
npm run test -- websocket.test.ts --run
```

### 测试结果
```
✓ WebSocketClient (14)
  ✓ should create WebSocket client
  ✓ should connect to WebSocket server
  ✓ should include token in URL when provided
  ✓ should handle message events
  ✓ should send messages when connected
  ✓ should not send messages when disconnected
  ✓ should start heartbeat after connection
  ✓ should reconnect after connection close
  ✓ should use exponential backoff for reconnection
  ✓ should stop reconnecting after max attempts
  ✓ should disconnect properly
  ✓ should register and unregister event handlers
  ✓ should update token and reconnect
  ✓ should get correct ready state

Test Files  1 passed (1)
     Tests  14 passed (14)
```

**测试通过率**: 100% ✅

---

## ✅ 验收标准

- ✅ 实时接收通知
- ✅ 桌面通知正常弹出
- ✅ 断线重连正常
- ✅ 音效提示正常
- ✅ 连接状态可视化
- ✅ Token认证
- ✅ 心跳保活
- ✅ 指数退避重连

---

## 📝 总结

任务10.1-10.5已全部完成,实现了完整的WebSocket实时推送功能。

**关键成果**:
- ✅ 完整的WebSocket客户端
- ✅ 通知Store集成
- ✅ 断线重连机制
- ✅ 桌面通知支持
- ✅ 音效提示
- ✅ 14个测试用例 (100%通过)

**技术亮点**:
- 指数退避重连算法
- 心跳保活机制
- 事件驱动架构
- 完善的错误处理
- 类型安全

