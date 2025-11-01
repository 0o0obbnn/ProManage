package com.promanage.api;

/**
 * 测试常量类
 *
 * @author ProManage Team
 * @date 2025-10-22
 */
public final class TestConstants {

    // ==================== 测试数据常量 ====================
    
    /** 测试用户前缀 */
    public static final String TEST_USER_PREFIX = "testuser";
    
    /** 测试邮箱域名 */
    public static final String TEST_EMAIL_DOMAIN = "@example.com";
    
    /** 测试组织前缀 */
    public static final String TEST_ORG_PREFIX = "Test Organization";
    
    /** 测试项目前缀 */
    public static final String TEST_PROJECT_PREFIX = "Test Project";
    
    /** 测试代码前缀 */
    public static final String TEST_CODE_PREFIX = "TEST_";
    
    /** 测试描述 */
    public static final String TEST_DESCRIPTION = "Test data for unit testing";
    
    /** 默认状态 - 正常 */
    public static final Integer DEFAULT_STATUS_NORMAL = 1;
    
    /** 默认优先级 - 中等 */
    public static final Integer DEFAULT_PRIORITY_MEDIUM = 2;
    
    /** 默认进度 */
    public static final Integer DEFAULT_PROGRESS = 45;
    
    /** 软件类型 */
    public static final String TYPE_SOFTWARE = "SOFTWARE";
    
    /** 管理员角色 */
    public static final String ROLE_ADMIN = "ADMIN";
    
    /** 成员角色 */
    public static final String ROLE_MEMBER = "MEMBER";
    
    /** 默认密码哈希 */
    public static final String DEFAULT_PASSWORD_HASH = "$2a$10$encoded.password.hash";
    
    /** 默认用户ID */
    public static final Long DEFAULT_USER_ID = 1L;
    
    /** 默认组织ID */
    public static final Long DEFAULT_ORG_ID = 1L;
    
    /** 默认项目ID */
    public static final Long DEFAULT_PROJECT_ID = 1L;
    
    /** 默认天数偏移 */
    public static final int DEFAULT_DAYS_OFFSET = 30;
    
    /** 项目天数偏移 */
    public static final int PROJECT_DAYS_OFFSET = 60;
    
    /** 组织天数偏移 */
    public static final int ORG_DAYS_OFFSET = 60;
    
    private TestConstants() {
        // 工具类，禁止实例化
    }
}
