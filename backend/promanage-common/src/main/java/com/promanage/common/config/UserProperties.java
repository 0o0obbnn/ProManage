package com.promanage.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 用户相关配置属性
 * <p>
 * 从application.yml中读取用户相关的配置参数
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-01
 */
@Data
@Component
@ConfigurationProperties(prefix = "user")
public class UserProperties {

    /**
     * 默认角色ID
     * 新用户注册时自动分配的角色ID
     */
    private Long defaultRoleId = 2L;

    /**
     * 密码最小长度
     */
    private Integer passwordMinLength = 8;

    /**
     * 密码最大长度
     */
    private Integer passwordMaxLength = 32;

    /**
     * 用户名最小长度
     */
    private Integer usernameMinLength = 4;

    /**
     * 用户名最大长度
     */
    private Integer usernameMaxLength = 20;
}
