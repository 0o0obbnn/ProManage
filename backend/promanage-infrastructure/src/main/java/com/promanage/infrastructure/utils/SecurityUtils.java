package com.promanage.infrastructure.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Security工具类
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-02
 */
public class SecurityUtils {

    private SecurityUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID的Optional包装
     */
    public static Optional<Long> getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        // 从authentication的details或principal中获取用户ID
        // 这里需要根据实际的JWT token结构来调整
        try {
            // 假设用户名格式为 "userId:username" 或者从自定义的principal中获取
            Object principal = authentication.getPrincipal();
            if (principal instanceof String) {
                return Optional.empty(); // Anonymous user
            }

            // 如果使用了自定义的UserDetails，可以直接从中获取
            // 这里提供一个简单的实现
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 获取当前登录用户名
     *
     * @return 用户名的Optional包装
     */
    public static Optional<String> getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        return Optional.ofNullable(authentication.getName());
    }
}
