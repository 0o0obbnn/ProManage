package com.promanage.api.websocket;

/** WebSocket会话信息 */
public class WebSocketSessionInfo {
  private Long userId;
  private String sessionId;
  private WebSocketSessionHandler handler;
  private long connectTime;
  private long lastHeartbeat;

  // Getter方法
  public Long getUserId() {
    return userId;
  }

  public String getSessionId() {
    return sessionId;
  }

  public WebSocketSessionHandler getHandler() {
    return handler;
  }

  public long getConnectTime() {
    return connectTime;
  }

  public long getLastHeartbeat() {
    return lastHeartbeat;
  }

  // Setter方法
  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  public void setHandler(WebSocketSessionHandler handler) {
    this.handler = handler;
  }

  public void setConnectTime(long connectTime) {
    this.connectTime = connectTime;
  }

  public void setLastHeartbeat(long lastHeartbeat) {
    this.lastHeartbeat = lastHeartbeat;
  }

  // Builder模式
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Long userId;
    private String sessionId;
    private WebSocketSessionHandler handler;
    private long connectTime;
    private long lastHeartbeat;

    public Builder userId(Long userId) {
      this.userId = userId;
      return this;
    }

    public Builder sessionId(String sessionId) {
      this.sessionId = sessionId;
      return this;
    }

    public Builder handler(WebSocketSessionHandler handler) {
      this.handler = handler;
      return this;
    }

    public Builder connectTime(long connectTime) {
      this.connectTime = connectTime;
      return this;
    }

    public Builder lastHeartbeat(long lastHeartbeat) {
      this.lastHeartbeat = lastHeartbeat;
      return this;
    }

    public WebSocketSessionInfo build() {
      WebSocketSessionInfo info = new WebSocketSessionInfo();
      info.userId = this.userId;
      info.sessionId = this.sessionId;
      info.handler = this.handler;
      info.connectTime = this.connectTime;
      info.lastHeartbeat = this.lastHeartbeat;
      return info;
    }
  }

  // 无参构造器
  public WebSocketSessionInfo() {}

  // 全参构造器
  public WebSocketSessionInfo(
      Long userId,
      String sessionId,
      WebSocketSessionHandler handler,
      long connectTime,
      long lastHeartbeat) {
    this.userId = userId;
    this.sessionId = sessionId;
    this.handler = handler;
    this.connectTime = connectTime;
    this.lastHeartbeat = lastHeartbeat;
  }
}
