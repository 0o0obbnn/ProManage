package com.promanage.service.enums;

/**
 * 通知类型枚举
 */
public enum NotificationType {
    
    /**
     * 项目相关
     */
    PROJECT_CREATED("PROJECT_CREATED", "项目创建"),
    PROJECT_UPDATED("PROJECT_UPDATED", "项目更新"),
    PROJECT_MEMBER_ADDED("PROJECT_MEMBER_ADDED", "项目成员添加"),
    PROJECT_MEMBER_REMOVED("PROJECT_MEMBER_REMOVED", "项目成员移除"),
    
    /**
     * 任务相关
     */
    TASK_CREATED("TASK_CREATED", "任务创建"),
    TASK_UPDATED("TASK_UPDATED", "任务更新"),
    TASK_ASSIGNED("TASK_ASSIGNED", "任务分配"),
    TASK_COMPLETED("TASK_COMPLETED", "任务完成"),
    TASK_OVERDUE("TASK_OVERDUE", "任务逾期"),
    
    /**
     * 文档相关
     */
    DOCUMENT_CREATED("DOCUMENT_CREATED", "文档创建"),
    DOCUMENT_UPDATED("DOCUMENT_UPDATED", "文档更新"),
    DOCUMENT_SHARED("DOCUMENT_SHARED", "文档共享"),
    
    /**
     * 变更请求相关
     */
    CHANGE_REQUEST_CREATED("CHANGE_REQUEST_CREATED", "变更请求创建"),
    CHANGE_REQUEST_APPROVED("CHANGE_REQUEST_APPROVED", "变更请求批准"),
    CHANGE_REQUEST_REJECTED("CHANGE_REQUEST_REJECTED", "变更请求拒绝"),
    
    /**
     * 系统相关
     */
    SYSTEM_ANNOUNCEMENT("SYSTEM_ANNOUNCEMENT", "系统公告"),
    SYSTEM_MAINTENANCE("SYSTEM_MAINTENANCE", "系统维护"),
    
    /**
     * 评论相关
     */
    COMMENT_ADDED("COMMENT_ADDED", "评论添加"),
    COMMENT_REPLY("COMMENT_REPLY", "评论回复");
    
    private final String code;
    private final String description;
    
    NotificationType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static NotificationType fromCode(String code) {
        for (NotificationType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown notification type code: " + code);
    }
}