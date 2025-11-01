package com.promanage.service.constant;

/**
 * 通用常量类
 * 
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-22
 */
public final class CommonConstants {

    private CommonConstants() {
        // 工具类，禁止实例化
    }

    // 通用错误消息
    public static final String ERROR_USER_NOT_LOGIN = "用户未登录";
    public static final String ERROR_PARAM_NULL = "参数不能为空";
    public static final String ERROR_NOT_FOUND = "记录不存在";
    public static final String ERROR_NO_PERMISSION = "权限不足";
    public static final String ERROR_SYSTEM_ERROR = "系统错误";
    public static final String ERROR_INVALID_PARAMETER = "参数无效";
    public static final String ERROR_OPERATION_FAILED = "操作失败";

    // ID相关常量
    public static final String USER_ID = "userId";
    public static final String PROJECT_ID = "projectId";
    public static final String DOCUMENT_ID = "documentId";
    public static final String TASK_ID = "taskId";
    public static final String CHANGE_REQUEST_ID = "changeRequestId";
    public static final String ORGANIZATION_ID = "organizationId";
    public static final String ROLE_ID = "roleId";
    public static final String PERMISSION_ID = "permissionId";

    // 缓存键前缀
    public static final String CACHE_USER_ROLES = "userRoles";
    public static final String CACHE_USER_PERMISSIONS = "userPermissions";
    public static final String CACHE_DOCUMENT_VERSIONS = "documentVersions";
    public static final String CACHE_PROJECT_MEMBERS = "projectMembers";

    // 分页相关常量
    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;

    // 字符串长度限制
    public static final int MAX_TITLE_LENGTH = 200;
    public static final int MAX_DESCRIPTION_LENGTH = 2000;
    public static final int MAX_COMMENT_LENGTH = 1000;
    public static final int MAX_NAME_LENGTH = 100;

    // 文件相关常量
    public static final String EXPORTS_PATH = "/exports/";
    public static final String UPLOADS_PATH = "/uploads/";
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    // 日期格式常量
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    // 状态相关常量
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";
    public static final String STATUS_DELETED = "DELETED";
    public static final String STATUS_PENDING = "PENDING";

    // 权限相关常量
    public static final String PERMISSION_VIEW = "view";
    public static final String PERMISSION_CREATE = "create";
    public static final String PERMISSION_UPDATE = "update";
    public static final String PERMISSION_DELETE = "delete";

    // 数字常量
    public static final int ZERO = 0;
    public static final int ONE = 1;
    public static final int THOUSAND = 1_000;
    public static final int TEN_THOUSAND = 10_000;
    public static final int ONE_MILLION = 1_000_000;

    // 布尔值常量
    public static final String TRUE = "true";
    public static final String FALSE = "false";

    // 空字符串常量
    public static final String EMPTY_STRING = "";
    public static final String SPACE = " ";
    public static final String COMMA = ",";
    public static final String DOT = ".";
    public static final String COLON = ":";
    public static final String SEMICOLON = ";";
    public static final String UNDERSCORE = "_";
    public static final String HYPHEN = "-";
}
