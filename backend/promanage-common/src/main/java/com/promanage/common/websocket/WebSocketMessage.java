package com.promanage.common.websocket;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

/**
 * WebSocket消息类
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebSocketMessage {

    /**
     * 消息类型
     */
    private String type;

    /**
     * 消息内容
     */
    private Object content;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 相关数据ID
     */
    private Long relatedId;

    /**
     * 相关数据类型
     */
    private String relatedType;

    /**
     * 发送时间
     */
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * 创建通知消息
     */
    public static WebSocketMessage notification(String title, Object content, Long relatedId, String relatedType) {
        return WebSocketMessage.builder()
                .type("notification")
                .title(title)
                .content(content)
                .relatedId(relatedId)
                .relatedType(relatedType)
                .build();
    }

    /**
     * 创建系统消息
     */
    public static WebSocketMessage system(String content) {
        return WebSocketMessage.builder()
                .type("system")
                .content(content)
                .build();
    }

    /**
     * 创建带标题的系统消息
     */
    public static WebSocketMessage system(String title, String content) {
        return WebSocketMessage.builder()
                .type("system")
                .title(title)
                .content(content)
                .build();
    }

    /**
     * 创建心跳消息
     */
    public static WebSocketMessage heartbeat() {
        return WebSocketMessage.builder()
                .type("heartbeat")
                .content("pong")
                .build();
    }

    /**
     * 创建错误消息
     */
    public static WebSocketMessage error(String content) {
        return WebSocketMessage.builder()
                .type("error")
                .content(content)
                .build();
    }
    
    // Getter方法
    public String getType() { return type; }
    public Object getContent() { return content; }
    public String getTitle() { return title; }
    public Long getRelatedId() { return relatedId; }
    public String getRelatedType() { return relatedType; }
    public LocalDateTime getTimestamp() { return timestamp; }
    
    // Setter方法
    public void setType(String type) { this.type = type; }
    public void setContent(Object content) { this.content = content; }
    public void setTitle(String title) { this.title = title; }
    public void setRelatedId(Long relatedId) { this.relatedId = relatedId; }
    public void setRelatedType(String relatedType) { this.relatedType = relatedType; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    // Builder模式
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String type;
        private Object content;
        private String title;
        private Long relatedId;
        private String relatedType;
        private LocalDateTime timestamp;
        
        public Builder type(String type) { this.type = type; return this; }
        public Builder content(Object content) { this.content = content; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder relatedId(Long relatedId) { this.relatedId = relatedId; return this; }
        public Builder relatedType(String relatedType) { this.relatedType = relatedType; return this; }
        public Builder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }
        
        public WebSocketMessage build() {
            WebSocketMessage message = new WebSocketMessage();
            message.type = this.type;
            message.content = this.content;
            message.title = this.title;
            message.relatedId = this.relatedId;
            message.relatedType = this.relatedType;
            message.timestamp = this.timestamp != null ? this.timestamp : LocalDateTime.now();
            return message;
        }
    }
    
    // 无参构造器
    public WebSocketMessage() {}
    
    // 全参构造器
    public WebSocketMessage(String type, Object content, String title, Long relatedId, String relatedType, LocalDateTime timestamp) {
        this.type = type;
        this.content = content;
        this.title = title;
        this.relatedId = relatedId;
        this.relatedType = relatedType;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    }
}