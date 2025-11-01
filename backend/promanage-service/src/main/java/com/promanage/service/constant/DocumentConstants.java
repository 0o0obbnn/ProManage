package com.promanage.service.constant;

/**
 * 文档相关常量类
 * 
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-22
 */
public final class DocumentConstants {

    private DocumentConstants() {
        // 工具类，禁止实例化
    }

    // 错误消息常量
    public static final String ERROR_DOCUMENT_ID_NULL = "文档ID不能为空";
    public static final String ERROR_DOCUMENT_NOT_FOUND = "文档不存在";
    public static final String ERROR_DOCUMENT_TITLE_EMPTY = "文档标题不能为空";
    public static final String ERROR_DOCUMENT_CONTENT_EMPTY = "文档内容不能为空";
    public static final String ERROR_DOCUMENT_PROJECT_ID_NULL = "项目ID不能为空";
    public static final String ERROR_DOCUMENT_NO_PERMISSION = "您无权操作此文档";
    public static final String ERROR_DOCUMENT_ALREADY_EXISTS = "文档已存在";
    public static final String ERROR_DOCUMENT_VERSION_NOT_FOUND = "文档版本不存在";

    // 文档状态常量
    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_PUBLISHED = "PUBLISHED";
    public static final String STATUS_ARCHIVED = "ARCHIVED";
    public static final String STATUS_DELETED = "DELETED";

    // 文档类型常量
    public static final String TYPE_REQUIREMENT = "REQUIREMENT";
    public static final String TYPE_DESIGN = "DESIGN";
    public static final String TYPE_TEST_CASE = "TEST_CASE";
    public static final String TYPE_USER_MANUAL = "USER_MANUAL";
    public static final String TYPE_API_DOC = "API_DOC";
    public static final String TYPE_MEETING_MINUTES = "MEETING_MINUTES";

    // 内容类型常量
    public static final String CONTENT_TYPE_MARKDOWN = "markdown";
    public static final String CONTENT_TYPE_HTML = "html";
    public static final String CONTENT_TYPE_TEXT = "text";
    public static final String CONTENT_TYPE_RICH_TEXT = "rich_text";

    // 文件扩展名常量
    public static final String EXTENSION_MD = ".md";
    public static final String EXTENSION_HTML = ".html";
    public static final String EXTENSION_TXT = ".txt";
    public static final String EXTENSION_PDF = ".pdf";
    public static final String EXTENSION_DOC = ".doc";
    public static final String EXTENSION_DOCX = ".docx";

    // 文档操作常量
    public static final String ACTION_CREATE = "CREATE";
    public static final String ACTION_UPDATE = "UPDATE";
    public static final String ACTION_DELETE = "DELETE";
    public static final String ACTION_PUBLISH = "PUBLISH";
    public static final String ACTION_ARCHIVE = "ARCHIVE";
    public static final String ACTION_RESTORE = "RESTORE";

    // 权限相关常量
    public static final String PERMISSION_DOCUMENT_VIEW = "document:view";
    public static final String PERMISSION_DOCUMENT_CREATE = "document:create";
    public static final String PERMISSION_DOCUMENT_UPDATE = "document:update";
    public static final String PERMISSION_DOCUMENT_DELETE = "document:delete";
    public static final String PERMISSION_DOCUMENT_PUBLISH = "document:publish";

    // 缓存键常量
    public static final String CACHE_DOCUMENT_DETAIL = "document:detail:";
    public static final String CACHE_DOCUMENT_LIST = "document:list:";
    public static final String CACHE_DOCUMENT_VERSIONS = "document:versions:";
    public static final String CACHE_DOCUMENT_FAVORITES = "document:favorites:";

    // 搜索相关常量
    public static final String SEARCH_KEYWORD = "keyword";
    public static final String SEARCH_TITLE = "title";
    public static final String SEARCH_CONTENT = "content";
    public static final String SEARCH_TAG = "tag";
    public static final String SEARCH_AUTHOR = "author";

    // 分页相关常量
    public static final String PAGE_SIZE = "pageSize";
    public static final String PAGE_NUMBER = "page";
    public static final String SORT_FIELD = "sortField";
    public static final String SORT_ORDER = "sortOrder";

    // 导出相关常量
    public static final String EXPORT_FORMAT_PDF = "pdf";
    public static final String EXPORT_FORMAT_WORD = "word";
    public static final String EXPORT_FORMAT_HTML = "html";
    public static final String EXPORT_FORMAT_MARKDOWN = "markdown";

    // 文件大小限制常量
    public static final long MAX_DOCUMENT_SIZE = 50 * 1024 * 1024; // 50MB
    public static final long MAX_ATTACHMENT_SIZE = 10 * 1024 * 1024; // 10MB
    public static final int MAX_ATTACHMENT_COUNT = 10;

    // 版本相关常量
    public static final String VERSION_SEPARATOR = ".";
    public static final int MAX_VERSION_DEPTH = 3;
    public static final String VERSION_MAJOR = "major";
    public static final String VERSION_MINOR = "minor";
    public static final String VERSION_PATCH = "patch";
}
