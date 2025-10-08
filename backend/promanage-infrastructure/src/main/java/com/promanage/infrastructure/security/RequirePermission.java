package com.promanage.infrastructure.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限检查注解
 * <p>
 * 用于检查用户是否具有特定权限
 * </p>
 *
 * @author ProManage Team
 * @since 2025-10-06
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    
    /**
     * 权限编码
     *
     * @return 权限编码
     */
    String value();
    
    /**
     * 权限检查失败时的错误消息
     *
     * @return 错误消息
     */
    String message() default "权限不足";
}