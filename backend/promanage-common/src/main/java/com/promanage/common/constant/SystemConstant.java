package com.promanage.common.constant;

/**
 * 系统常量
 * <p>
 * 定义系统级别的常量
 * </p>
 *
 * @author ProManage Team
 * @date 2025-09-30
 */
public interface SystemConstant {

    /**
     * 系统编码
     */
    String CHARSET_UTF8 = "UTF-8";

    /**
     * 系统默认语言
     */
    String DEFAULT_LANGUAGE = "zh_CN";

    /**
     * 系统默认时区
     */
    String DEFAULT_TIMEZONE = "Asia/Shanghai";

    /**
     * 系统超级管理员ID
     */
    Long SUPER_ADMIN_ID = 1L;

    /**
     * 系统超级管理员用户名
     */
    String SUPER_ADMIN_USERNAME = "admin";

    /**
     * 系统名称
     */
    String SYSTEM_NAME = "ProManage";

    /**
     * 系统版本
     */
    String SYSTEM_VERSION = "1.0.0";

    /**
     * Token Header名称
     */
    String TOKEN_HEADER = "Authorization";

    /**
     * Token前缀
     */
    String TOKEN_PREFIX = "Bearer ";

    /**
     * Token有效期 (秒) - 7天
     */
    Long TOKEN_EXPIRATION = 7 * 24 * 60 * 60L;

    /**
     * Refresh Token有效期 (秒) - 30天
     */
    Long REFRESH_TOKEN_EXPIRATION = 30 * 24 * 60 * 60L;

    /**
     * 默认分页大小
     */
    Integer DEFAULT_PAGE_SIZE = 20;

    /**
     * 最大分页大小
     */
    Integer MAX_PAGE_SIZE = 100;

    /**
     * 文件上传最大大小 (字节) - 500MB
     */
    Long MAX_FILE_SIZE = 500 * 1024 * 1024L;

    /**
     * 图片上传最大大小 (字节) - 10MB
     */
    Long MAX_IMAGE_SIZE = 10 * 1024 * 1024L;

    /**
     * 允许的文档类型
     */
    String[] ALLOWED_DOCUMENT_TYPES = {
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "txt", "md", "html", "json", "xml"
    };

    /**
     * 允许的图片类型
     */
    String[] ALLOWED_IMAGE_TYPES = {
            "jpg", "jpeg", "png", "gif", "bmp", "svg", "webp"
    };

    /**
     * 缓存前缀
     */
    String CACHE_PREFIX = "promanage:";

    /**
     * 用户缓存前缀
     */
    String USER_CACHE_PREFIX = CACHE_PREFIX + "user:";

    /**
     * 文档缓存前缀
     */
    String DOCUMENT_CACHE_PREFIX = CACHE_PREFIX + "document:";

    /**
     * 项目缓存前缀
     */
    String PROJECT_CACHE_PREFIX = CACHE_PREFIX + "project:";

    /**
     * 验证码缓存前缀
     */
    String CAPTCHA_CACHE_PREFIX = CACHE_PREFIX + "captcha:";

    /**
     * 验证码有效期 (秒) - 5分钟
     */
    Long CAPTCHA_EXPIRATION = 5 * 60L;

    /**
     * 登录失败次数限制
     */
    Integer LOGIN_FAIL_LIMIT = 5;

    /**
     * 登录失败锁定时间 (秒) - 30分钟
     */
    Long LOGIN_LOCK_TIME = 30 * 60L;

    /**
     * 密码最小长度
     */
    Integer PASSWORD_MIN_LENGTH = 8;

    /**
     * 密码最大长度
     */
    Integer PASSWORD_MAX_LENGTH = 32;

    /**
     * 用户名最小长度
     */
    Integer USERNAME_MIN_LENGTH = 4;

    /**
     * 用户名最大长度
     */
    Integer USERNAME_MAX_LENGTH = 20;

    /**
     * 日期时间格式
     */
    String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * 日期格式
     */
    String DATE_PATTERN = "yyyy-MM-dd";

    /**
     * 时间格式
     */
    String TIME_PATTERN = "HH:mm:ss";

    /**
     * 成功标识
     */
    Integer SUCCESS = 1;

    /**
     * 失败标识
     */
    Integer FAIL = 0;

    /**
     * 是
     */
    Integer YES = 1;

    /**
     * 否
     */
    Integer NO = 0;

    /**
     * 启用
     */
    Integer ENABLED = 1;

    /**
     * 禁用
     */
    Integer DISABLED = 0;

    /**
     * 已删除
     */
    Integer DELETED = 1;

    /**
     * 未删除
     */
    Integer NOT_DELETED = 0;
}