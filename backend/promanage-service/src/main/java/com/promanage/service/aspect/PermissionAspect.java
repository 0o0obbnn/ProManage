package com.promanage.service.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.promanage.common.domain.Result;
import com.promanage.common.domain.ResultCode;
import com.promanage.infrastructure.security.RequirePermission;
import com.promanage.service.service.IUserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 权限检查切面
 *
 * <p>处理@RequirePermission注解，检查用户是否具有特定权限
 *
 * @author ProManage Team
 * @since 2025-10-06
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {

  private final IUserService userService;

  /**
   * 环绕通知，处理权限检查
   *
   * @param joinPoint 连接点
   * @param requirePermission 权限注解
   * @return 方法执行结果
   * @throws Throwable 如果权限检查失败，抛出异常
   */
  @Around("@annotation(requirePermission)")
  public Object checkPermission(ProceedingJoinPoint joinPoint, RequirePermission requirePermission)
      throws Throwable {
    // 获取当前用户认证信息
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    // 如果没有认证信息，返回权限不足
    if (authentication == null || !authentication.isAuthenticated()) {
      log.warn("用户未认证，无法访问需要权限的资源: {}", requirePermission.value());
      return Result.error(ResultCode.UNAUTHORIZED.getCode(), "用户未认证");
    }

    // 获取用户名
    String username = authentication.getName();
    if (!StringUtils.hasText(username)) {
      log.warn("用户名为空，无法检查权限: {}", requirePermission.value());
      return Result.error(ResultCode.UNAUTHORIZED.getCode(), "用户未认证");
    }

    try {
      // 获取用户信息
      var user = userService.getByUsername(username);
      if (user == null) {
        log.warn("用户不存在: {}", username);
        return Result.error(ResultCode.USER_NOT_FOUND.getCode(), "用户不存在");
      }

      // 检查用户是否具有指定权限
      boolean hasPermission = checkUserPermission(user.getId(), requirePermission.value());

      if (!hasPermission) {
        log.warn("用户{}没有权限访问资源: {}", username, requirePermission.value());
        return Result.error(ResultCode.FORBIDDEN.getCode(), requirePermission.message());
      }

      // 执行原方法
      return joinPoint.proceed();
    } catch (IllegalArgumentException e) {
      log.error("权限检查过程中发生错误", e);
      return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "权限检查失败");
    }
  }

  /**
   * 检查用户是否具有指定权限
   *
   * @param userId 用户ID
   * @param permissionCode 权限编码
   * @return 是否有权限
   */
  private boolean checkUserPermission(Long userId, String permissionCode) {
    try {
      return userService.hasPermission(userId, permissionCode);
    } catch (IllegalArgumentException e) {
      log.error("检查用户权限时发生错误", e);
      return false;
    }
  }
}
