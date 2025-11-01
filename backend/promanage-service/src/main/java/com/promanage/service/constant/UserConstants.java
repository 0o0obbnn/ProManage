package com.promanage.service.constant;

/**
 * 用户相关常量类
 * 
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-22
 */
public final class UserConstants {

    private UserConstants() {
        // 工具类，禁止实例化
    }

    // 错误消息常量
    public static final String ERROR_USER_ID_NULL = "用户ID不能为空";
    public static final String ERROR_USER_NOT_FOUND = "用户不存在";
    public static final String ERROR_USERNAME_EMPTY = "用户名不能为空";
    public static final String ERROR_EMAIL_EMPTY = "邮箱不能为空";
    public static final String ERROR_PASSWORD_EMPTY = "密码不能为空";
    public static final String ERROR_USER_ALREADY_EXISTS = "用户已存在";
    public static final String ERROR_USER_NO_PERMISSION = "您无权操作此用户";
    public static final String ERROR_USER_DISABLED = "用户已被禁用";
    public static final String ERROR_USER_LOCKED = "用户已被锁定";

    // 用户状态常量
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_LOCKED = "LOCKED";
    public static final String STATUS_DELETED = "DELETED";

    // 用户角色常量
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_MANAGER = "MANAGER";
    public static final String ROLE_DEVELOPER = "DEVELOPER";
    public static final String ROLE_TESTER = "TESTER";
    public static final String ROLE_DESIGNER = "DESIGNER";
    public static final String ROLE_ANALYST = "ANALYST";
    public static final String ROLE_OBSERVER = "OBSERVER";
    public static final String ROLE_GUEST = "GUEST";

    // 权限相关常量
    public static final String PERMISSION_USER_VIEW = "user:view";
    public static final String PERMISSION_USER_CREATE = "user:create";
    public static final String PERMISSION_USER_UPDATE = "user:update";
    public static final String PERMISSION_USER_DELETE = "user:delete";
    public static final String PERMISSION_USER_MANAGE = "user:manage";

    // 缓存键常量
    public static final String CACHE_USER_DETAIL = "user:detail:";
    public static final String CACHE_USER_LIST = "user:list:";
    public static final String CACHE_USER_ROLES = "user:roles:";
    public static final String CACHE_USER_PERMISSIONS = "user:permissions:";
    public static final String CACHE_USER_PROFILE = "user:profile:";

    // 搜索相关常量
    public static final String SEARCH_KEYWORD = "keyword";
    public static final String SEARCH_USERNAME = "username";
    public static final String SEARCH_EMAIL = "email";
    public static final String SEARCH_NAME = "name";
    public static final String SEARCH_ROLE = "role";
    public static final String SEARCH_STATUS = "status";

    // 分页相关常量
    public static final String PAGE_SIZE = "pageSize";
    public static final String PAGE_NUMBER = "page";
    public static final String SORT_FIELD = "sortField";
    public static final String SORT_ORDER = "sortOrder";

    // 用户设置常量
    public static final String SETTING_LANGUAGE = "language";
    public static final String SETTING_TIMEZONE = "timezone";
    public static final String SETTING_THEME = "theme";
    public static final String SETTING_NOTIFICATION = "notification";
    public static final String SETTING_EMAIL_NOTIFICATION = "email_notification";
    public static final String SETTING_SMS_NOTIFICATION = "sms_notification";
    public static final String SETTING_PUSH_NOTIFICATION = "push_notification";

    // 密码相关常量
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 128;
    public static final int PASSWORD_HISTORY_COUNT = 5;
    public static final int MAX_LOGIN_ATTEMPTS = 5;
    public static final int LOCKOUT_DURATION_MINUTES = 30;

    // 用户字段长度限制
    public static final int MAX_USERNAME_LENGTH = 50;
    public static final int MAX_EMAIL_LENGTH = 100;
    public static final int MAX_FIRST_NAME_LENGTH = 50;
    public static final int MAX_LAST_NAME_LENGTH = 50;
    public static final int MAX_PHONE_LENGTH = 20;
    public static final int MAX_ADDRESS_LENGTH = 200;

    // 用户头像相关常量
    public static final String AVATAR_DEFAULT = "default";
    public static final String AVATAR_CUSTOM = "custom";
    public static final String AVATAR_GRAVATAR = "gravatar";
    public static final long MAX_AVATAR_SIZE = 2 * 1024 * 1024; // 2MB
    public static final String[] ALLOWED_AVATAR_TYPES = {"jpg", "jpeg", "png", "gif"};

    // 用户活动类型常量
    public static final String ACTIVITY_LOGIN = "LOGIN";
    public static final String ACTIVITY_LOGOUT = "LOGOUT";
    public static final String ACTIVITY_PROFILE_UPDATE = "PROFILE_UPDATE";
    public static final String ACTIVITY_PASSWORD_CHANGE = "PASSWORD_CHANGE";
    public static final String ACTIVITY_EMAIL_CHANGE = "EMAIL_CHANGE";
    public static final String ACTIVITY_ROLE_CHANGE = "ROLE_CHANGE";

    // 用户统计常量
    public static final String STAT_TOTAL_USERS = "total_users";
    public static final String STAT_ACTIVE_USERS = "active_users";
    public static final String STAT_NEW_USERS = "new_users";
    public static final String STAT_ONLINE_USERS = "online_users";
    public static final String STAT_USER_GROWTH = "user_growth";
}
