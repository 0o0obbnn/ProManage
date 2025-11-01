package com.promanage.service.constant;

/**
 * 变更请求相关常量
 * 
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-22
 */
public final class ChangeRequestConstants {

    private ChangeRequestConstants() {
        // 工具类，禁止实例化
    }

    // 错误消息常量
    public static final String ERROR_CHANGE_REQUEST_NULL = "变更请求信息不能为空";
    public static final String ERROR_TITLE_EMPTY = "变更请求标题不能为空";
    public static final String ERROR_PROJECT_ID_NULL = "项目ID不能为空";
    public static final String ERROR_CHANGE_REQUEST_ID_NULL = "变更请求ID不能为空";
    public static final String ERROR_CHANGE_REQUEST_NOT_FOUND = "变更请求不存在";
    public static final String ERROR_NOT_PROJECT_MEMBER = "您不是该项目成员，无权操作变更请求";
    public static final String ERROR_NO_APPROVAL_PERMISSION = "您无权审批此变更请求";
    public static final String ERROR_INVALID_STATUS = "变更请求状态不允许此操作";
    public static final String ERROR_USER_NOT_LOGIN = "用户未登录";
    public static final String ERROR_INVALID_PARAMETER = "参数无效";

    // 状态相关常量
    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_PENDING_APPROVAL = "PENDING_APPROVAL";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    // 优先级相关常量
    public static final String PRIORITY_LOW = "LOW";
    public static final String PRIORITY_MEDIUM = "MEDIUM";
    public static final String PRIORITY_HIGH = "HIGH";
    public static final String PRIORITY_URGENT = "URGENT";

    // 审批决策常量
    public static final String DECISION_APPROVED = "APPROVED";
    public static final String DECISION_REJECTED = "REJECTED";

    // 分页默认值
    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;

    // 字符串长度限制
    public static final int MAX_TITLE_LENGTH = 200;
    public static final int MAX_DESCRIPTION_LENGTH = 2000;
    public static final int MAX_COMMENT_LENGTH = 1000;

    // 业务规则常量
    public static final int MAX_APPROVAL_LEVELS = 3;
    public static final int MAX_IMPACT_ANALYSIS_DEPTH = 5;
}
