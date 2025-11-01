package com.promanage.infrastructure.utils;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.promanage.infrastructure.security.CustomUserDetails;

/**
 * Security工具类
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-02
 */
public final class SecurityUtils {

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

    Object principal = authentication.getPrincipal();
    if (principal instanceof CustomUserDetails) {
      return Optional.of(((CustomUserDetails) principal).getId());
    }

    // Fallback for other principal types if any, though unlikely with our setup
    return Optional.empty();
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
