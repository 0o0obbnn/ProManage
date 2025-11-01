package com.promanage.service.constant;

/**
 * 项目相关常量类
 * 
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-22
 */
public final class ProjectConstants {

    private ProjectConstants() {
        // 工具类，禁止实例化
    }

    // 错误消息常量
    public static final String ERROR_PROJECT_ID_NULL = "项目ID不能为空";
    public static final String ERROR_PROJECT_NOT_FOUND = "项目不存在";
    public static final String ERROR_PROJECT_NAME_EMPTY = "项目名称不能为空";
    public static final String ERROR_PROJECT_DESCRIPTION_EMPTY = "项目描述不能为空";
    public static final String ERROR_PROJECT_NO_PERMISSION = "您无权操作此项目";
    public static final String ERROR_PROJECT_ALREADY_EXISTS = "项目已存在";
    public static final String ERROR_PROJECT_MEMBER_NOT_FOUND = "项目成员不存在";
    public static final String ERROR_PROJECT_MEMBER_ALREADY_EXISTS = "项目成员已存在";

    // 项目状态常量
    public static final String STATUS_PLANNING = "PLANNING";
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_SUSPENDED = "SUSPENDED";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    // 项目类型常量
    public static final String TYPE_WEB_APPLICATION = "WEB_APPLICATION";
    public static final String TYPE_MOBILE_APP = "MOBILE_APP";
    public static final String TYPE_DESKTOP_APP = "DESKTOP_APP";
    public static final String TYPE_API_SERVICE = "API_SERVICE";
    public static final String TYPE_LIBRARY = "LIBRARY";
    public static final String TYPE_FRAMEWORK = "FRAMEWORK";

    // 项目优先级常量
    public static final String PRIORITY_LOW = "LOW";
    public static final String PRIORITY_MEDIUM = "MEDIUM";
    public static final String PRIORITY_HIGH = "HIGH";
    public static final String PRIORITY_URGENT = "URGENT";

    // 项目角色常量
    public static final String ROLE_PROJECT_MANAGER = "PROJECT_MANAGER";
    public static final String ROLE_TECHNICAL_LEAD = "TECHNICAL_LEAD";
    public static final String ROLE_DEVELOPER = "DEVELOPER";
    public static final String ROLE_TESTER = "TESTER";
    public static final String ROLE_DESIGNER = "DESIGNER";
    public static final String ROLE_ANALYST = "ANALYST";
    public static final String ROLE_OBSERVER = "OBSERVER";

    // 权限相关常量
    public static final String PERMISSION_PROJECT_VIEW = "project:view";
    public static final String PERMISSION_PROJECT_CREATE = "project:create";
    public static final String PERMISSION_PROJECT_UPDATE = "project:update";
    public static final String PERMISSION_PROJECT_DELETE = "project:delete";
    public static final String PERMISSION_PROJECT_MANAGE = "project:manage";

    // 缓存键常量
    public static final String CACHE_PROJECT_DETAIL = "project:detail:";
    public static final String CACHE_PROJECT_LIST = "project:list:";
    public static final String CACHE_PROJECT_MEMBERS = "project:members:";
    public static final String CACHE_PROJECT_STATISTICS = "project:statistics:";

    // 搜索相关常量
    public static final String SEARCH_KEYWORD = "keyword";
    public static final String SEARCH_NAME = "name";
    public static final String SEARCH_DESCRIPTION = "description";
    public static final String SEARCH_STATUS = "status";
    public static final String SEARCH_TYPE = "type";
    public static final String SEARCH_OWNER = "owner";

    // 分页相关常量
    public static final String PAGE_SIZE = "pageSize";
    public static final String PAGE_NUMBER = "page";
    public static final String SORT_FIELD = "sortField";
    public static final String SORT_ORDER = "sortOrder";

    // 项目设置常量
    public static final String SETTING_NOTIFICATION_EMAIL = "notification_email";
    public static final String SETTING_NOTIFICATION_SMS = "notification_sms";
    public static final String SETTING_NOTIFICATION_PUSH = "notification_push";
    public static final String SETTING_AUTO_SAVE = "auto_save";
    public static final String SETTING_VERSION_CONTROL = "version_control";

    // 项目模板常量
    public static final String TEMPLATE_AGILE = "AGILE";
    public static final String TEMPLATE_WATERFALL = "WATERFALL";
    public static final String TEMPLATE_SCRUM = "SCRUM";
    public static final String TEMPLATE_KANBAN = "KANBAN";
    public static final String TEMPLATE_CUSTOM = "CUSTOM";

    // 项目指标常量
    public static final String METRIC_PROGRESS = "progress";
    public static final String METRIC_QUALITY = "quality";
    public static final String METRIC_VELOCITY = "velocity";
    public static final String METRIC_BURNDOWN = "burndown";
    public static final String METRIC_BURNUP = "burnup";

    // 项目里程碑常量
    public static final String MILESTONE_PLANNING = "PLANNING";
    public static final String MILESTONE_DESIGN = "DESIGN";
    public static final String MILESTONE_DEVELOPMENT = "DEVELOPMENT";
    public static final String MILESTONE_TESTING = "TESTING";
    public static final String MILESTONE_DEPLOYMENT = "DEPLOYMENT";
    public static final String MILESTONE_MAINTENANCE = "MAINTENANCE";
}
