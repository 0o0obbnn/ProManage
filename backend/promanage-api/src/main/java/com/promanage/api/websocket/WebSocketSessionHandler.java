package com.promanage.api.websocket;

import com.promanage.common.websocket.WebSocketMessage;

/**
 * WebSocket会话处理器接口
 */
public interface WebSocketSessionHandler {

    /**
     * 发送消息
     */
    boolean sendMessage(WebSocketMessage message);

    /**
     * 检查会话是否活跃
     */
    boolean isActive();

    /**
     * 关闭会话
     */
    void close();

    /**
     * 获取会话ID
     */
    String getSessionId();
}