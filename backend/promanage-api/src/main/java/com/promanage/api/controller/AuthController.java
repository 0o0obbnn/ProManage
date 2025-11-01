package com.promanage.api.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import com.promanage.api.dto.request.ChangePasswordRequest;
import com.promanage.api.dto.request.CheckPasswordStrengthRequest;
import com.promanage.api.dto.request.LoginRequest;
import com.promanage.api.dto.request.RefreshTokenRequest;
import com.promanage.api.dto.request.RegisterRequest;
import com.promanage.api.dto.request.ResetPasswordRequest;
import com.promanage.api.dto.request.SendResetCodeRequest;
import com.promanage.api.dto.response.LoginResponse;
import com.promanage.api.dto.response.RoleResponse;
import com.promanage.api.dto.response.UserResponse;
import com.promanage.common.domain.Result;
import com.promanage.common.domain.ResultCode;
import com.promanage.common.entity.User;
import com.promanage.common.exception.BusinessException;
import com.promanage.common.util.IpUtils;
import com.promanage.infrastructure.security.JwtTokenProvider;
import com.promanage.infrastructure.security.SecurityUtils;
import com.promanage.infrastructure.security.TokenBlacklistService;
import com.promanage.service.dto.PasswordStrengthResponse;
import com.promanage.domain.entity.Role;
import com.promanage.service.service.IAuthService;
import com.promanage.service.service.IPasswordService;
import com.promanage.service.service.IUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 认证控制器
 *
 * <p>处理用户登录、登出、令牌刷新等认证相关请求
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-01
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "认证管理", description = "用户登录、登出、令牌管理相关接口")
@RequiredArgsConstructor
public class AuthController {

  private final IAuthService authService;
  private final IUserService userService;
  private final JwtTokenProvider jwtTokenProvider;
  private final IPasswordService passwordService;
  private final TokenBlacklistService tokenBlacklistService;

  /**
   * 用户登录
   *
   * <p>验证用户名和密码，成功后返回JWT令牌
   *
   * @param loginRequest 登录请求
   * @param request HTTP请求
   * @return 登录响应（包含JWT令牌和用户信息）
   */
  @PostMapping("/login")
  @Operation(summary = "用户登录", description = "通过用户名和密码进行登录认证")
  public Result<LoginResponse> login(
      @Valid @RequestBody LoginRequest loginRequest,
      HttpServletRequest request,
      HttpServletResponse responseWrapper) {
    // 设置禁止缓存的HTTP头，避免304状态码问题
    responseWrapper.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    responseWrapper.setHeader("Pragma", "no-cache");
    responseWrapper.setHeader("Expires", "0");

    log.info("用户登录请求, username={}", loginRequest.getUsername());

    // 认证用户
    User user = authService.authenticate(loginRequest.getUsername(), loginRequest.getPassword());

    // 获取用户角色
    List<Role> roles = userService.getUserRoles(user.getId());

    // 构建权限列表（添加ROLE_前缀）
    List<GrantedAuthority> authorities =
        roles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleCode()))
            .collect(Collectors.toList());

    String authoritiesStr =
        authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(","));

    // 生成JWT令牌
    String token = jwtTokenProvider.generateToken(user.getUsername(), user.getId(), authoritiesStr);

    // 生成刷新令牌（如果勾选了"记住我"）
    String refreshToken = null;
    if (loginRequest.getRememberMe() != null && loginRequest.getRememberMe()) {
      refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());
    }

    // 更新最后登录信息
    String ipAddress = IpUtils.getClientIpAddress(request);
    authService.updateLastLogin(user.getId(), ipAddress);

    // 转换角色信息
    List<RoleResponse> roleResponses =
        roles.stream().map(this::convertToRoleResponse).collect(Collectors.toList());

    // 构建用户响应
    UserResponse userResponse =
        UserResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .phone(user.getPhone())
            .realName(user.getRealName())
            .avatar(user.getAvatar())
            .status(user.getStatus())
            .position(user.getPosition())
            .lastLoginTime(user.getLastLoginTime())
            .lastLoginIp(user.getLastLoginIp())
            .createTime(user.getCreateTime())
            .updateTime(user.getUpdateTime())
            .roles(roleResponses)
            .build();

    // 构建登录响应
    LoginResponse response =
        LoginResponse.builder()
            .token(token)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(jwtTokenProvider.getJwtExpirationMs() / 1000) // 转换为秒
            .userInfo(userResponse)
            .build();

    log.info("用户登录成功, username={}, userId={}", user.getUsername(), user.getId());
    return Result.success(response);
  }

  /**
   * 用户注册
   *
   * <p>创建新用户账号，自动分配默认角色
   *
   * @param registerRequest 注册请求
   * @return 注册成功的用户信息
   */
  @PostMapping("/register")
  @Operation(summary = "用户注册", description = "创建新用户账号")
  public Result<UserResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
    log.info(
        "用户注册请求, username={}, email={}", registerRequest.getUsername(), registerRequest.getEmail());

    // 调用注册服务（带密码确认校验）
    User user =
        authService.register(
            registerRequest.getUsername(),
            registerRequest.getPassword(),
            registerRequest.getConfirmPassword(),
            registerRequest.getEmail(),
            registerRequest.getPhone(),
            registerRequest.getRealName());

    // 获取用户角色
    List<Role> roles = userService.getUserRoles(user.getId());
    List<RoleResponse> roleResponses =
        roles.stream().map(this::convertToRoleResponse).collect(Collectors.toList());

    // 构建用户响应
    UserResponse userResponse =
        UserResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .phone(user.getPhone())
            .realName(user.getRealName())
            .avatar(user.getAvatar())
            .status(user.getStatus())
            .position(user.getPosition())
            .createTime(user.getCreateTime())
            .updateTime(user.getUpdateTime())
            .roles(roleResponses)
            .build();

    log.info("用户注册成功, username={}, userId={}", user.getUsername(), user.getId());
    return Result.success(userResponse);
  }

  /**
   * 用户登出
   *
   * <p>登出当前用户，将JWT令牌加入黑名单
   *
   * @param request HTTP请求（用于获取Authorization头中的token）
   * @return 操作结果
   */
  @PostMapping("/logout")
  @Operation(summary = "用户登出", description = "登出当前用户并将JWT令牌加入黑名单")
  public Result<Void> logout(HttpServletRequest request) {
    log.info("用户登出请求");

    // 从请求中提取JWT令牌

    String token = extractTokenFromRequest(request);

    if (token != null) {
      // 将令牌加入黑名单

      tokenBlacklistService.blacklistToken(token);

      log.info("JWT令牌已成功加入黑名单");
    } else {
      log.warn("登出请求中未找到有效的JWT令牌");
    }

    return Result.success("登出成功");
  }

  /**
   * 刷新令牌
   *
   * <p>使用刷新令牌获取新的访问令牌，实现刷新令牌轮换机制
   *
   * @param request 包含刷新令牌的请求体
   * @return 新的JWT令牌
   */
  @PostMapping("/refresh")
  @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌")
  public Result<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
    log.info("刷新令牌请求");

    String refreshToken = request.getRefreshToken();

    // 检查刷新令牌是否在黑名单中
    if (tokenBlacklistService.isBlacklisted(refreshToken)) {
      log.warn("尝试使用已加入黑名单的刷新令牌");
      return Result.error(401, "刷新令牌无效");
    }

    // 验证刷新令牌
    if (!jwtTokenProvider.validateToken(refreshToken)) {
      log.warn("刷新令牌无效");
      return Result.error(401, "刷新令牌无效");
    }

    // 从刷新令牌中提取用户名
    String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
    if (username == null) {
      log.warn("无法从刷新令牌中提取用户名");
      return Result.error(401, "刷新令牌无效");
    }

    // 获取用户信息
    User user = userService.getByUsername(username);
    if (user == null || user.getDeleted()) {
      log.warn("用户不存在, username={}", username);
      return Result.error(401, "用户不存在");
    }

    // 获取用户角色
    List<Role> roles = userService.getUserRoles(user.getId());

    // 构建权限列表
    String authoritiesStr =
        roles.stream().map(role -> "ROLE_" + role.getRoleCode()).collect(Collectors.joining(","));

    // 生成新的访问令牌
    String newToken =
        jwtTokenProvider.generateToken(user.getUsername(), user.getId(), authoritiesStr);

    // 生成新的刷新令牌
    String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());

    // 立即将旧的刷新令牌加入黑名单（实现刷新令牌轮换机制）
    tokenBlacklistService.blacklistToken(refreshToken);

    // 构建响应
    LoginResponse response =
        LoginResponse.builder()
            .token(newToken)
            .refreshToken(newRefreshToken)
            .tokenType("Bearer")
            .expiresIn(jwtTokenProvider.getJwtExpirationMs() / 1000)
            .build();

    log.info("令牌刷新成功, username={}", username);
    return Result.success(response);
  }

  /**
   * 获取当前用户信息
   *
   * <p>根据JWT令牌获取当前登录用户的信息
   *
   * @param authentication Spring Security认证对象
   * @param response HttpServletResponse to set cache control headers
   * @return 用户信息
   */
  @GetMapping("/me")
  @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
  public Result<UserResponse> getCurrentUser(
      Authentication authentication, HttpServletResponse responseWrapper) {
    // 设置禁止缓存的HTTP头，避免304状态码问题
    responseWrapper.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    responseWrapper.setHeader("Pragma", "no-cache");
    responseWrapper.setHeader("Expires", "0");
    log.info("获取当前用户信息");

    if (authentication == null || !authentication.isAuthenticated()) {
      log.warn("用户未认证");
      return Result.error(401, "用户未认证");
    }

    String username = authentication.getName();
    User user = userService.getByUsername(username);

    if (user == null || user.getDeleted()) {
      log.warn("用户不存在, username={}", username);
      return Result.error(404, "用户不存在");
    }

    // 获取用户角色
    List<Role> roles = userService.getUserRoles(user.getId());
    List<RoleResponse> roleResponses =
        roles.stream().map(this::convertToRoleResponse).collect(Collectors.toList());

    // 构建用户响应
    UserResponse userResponse =
        UserResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .phone(user.getPhone())
            .realName(user.getRealName())
            .avatar(user.getAvatar())
            .status(user.getStatus())
            .position(user.getPosition())
            .lastLoginTime(user.getLastLoginTime())
            .lastLoginIp(user.getLastLoginIp())
            .createTime(user.getCreateTime())
            .updateTime(user.getUpdateTime())
            .roles(roleResponses)
            .build();

    log.info("获取当前用户信息成功, username={}", username);
    return Result.success(userResponse);
  }

  /**
   * 从HTTP请求中提取JWT令牌
   *
   * @param request HTTP请求
   * @return JWT令牌或null
   */
  private String extractTokenFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }

  /**
   * 转换Role实体为RoleResponse DTO
   *
   * @param role 角色实体
   * @return 角色响应DTO
   */
  private RoleResponse convertToRoleResponse(Role role) {
    return RoleResponse.builder()
        .id(role.getId())
        .roleName(role.getRoleName())
        .roleCode(role.getRoleCode())
        .description(role.getDescription())
        .sort(role.getSort())
        .status(role.getStatus())
        .createTime(role.getCreateTime())
        .updateTime(role.getUpdateTime())
        .build();
  }

  /**
   * 发送密码重置验证码
   *
   * <p>向用户邮箱发送6位数字验证码，用于重置密码
   *
   * @param request 发送验证码请求
   * @return 操作结果
   */
  @PostMapping("/forgot-password/send-code")
  @Operation(summary = "发送密码重置验证码", description = "向用户邮箱发送验证码")
  public Result<Void> sendResetCode(@Valid @RequestBody SendResetCodeRequest request) {
    log.info("发送密码重置验证码请求, email={}", request.getEmail());

    authService.sendPasswordResetCode(request.getEmail());

    log.info("验证码发送成功, email={}", request.getEmail());
    return Result.success("验证码已发送到您的邮箱");
  }

  /**
   * 重置密码
   *
   * <p>通过邮箱验证码重置用户密码
   *
   * @param request 重置密码请求
   * @return 操作结果
   */
  @PostMapping("/forgot-password/reset")
  @Operation(summary = "重置密码", description = "通过验证码重置密码")
  public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    log.info("重置密码请求, email={}", request.getEmail());

    // 重置密码（带密码确认校验）
    authService.resetPassword(
        request.getEmail(),
        request.getVerificationCode(),
        request.getNewPassword(),
        request.getConfirmPassword());

    log.info("密码重置成功, email={}", request.getEmail());
    return Result.success("密码重置成功");
  }

  /**
   * 修改密码
   *
   * <p>已登录用户修改自己的密码，需要验证旧密码
   *
   * @param request 修改密码请求
   * @return 操作结果
   */
  @PostMapping("/change-password")
  @Operation(summary = "修改密码", description = "已登录用户修改自己的密码")
  public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
    // 获取当前登录用户ID
    Long userId =
        SecurityUtils.getCurrentUserId()
            .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "请先登录"));

    log.info("修改密码请求, userId={}", userId);

    // 修改密码（服务层会验证两次密码是否一致）
    userService.updatePassword(
        userId, request.getOldPassword(), request.getNewPassword(), request.getConfirmPassword());

    log.info("密码修改成功, userId={}", userId);
    return Result.success("密码修改成功");
  }

  /**
   * 检查密码强度
   *
   * <p>实时检查密码强度，返回强度等级、百分比和改进建议
   *
   * @param request 密码强度检查请求
   * @return 密码强度信息
   */
  @PostMapping("/check-password-strength")
  @Operation(summary = "检查密码强度", description = "实时检查密码强度并给出改进建议")
  public Result<PasswordStrengthResponse> checkPasswordStrength(
      @Valid @RequestBody CheckPasswordStrengthRequest request) {
    log.debug("检查密码强度请求");

    PasswordStrengthResponse response =
        passwordService.checkPasswordStrength(request.getPassword());

    log.debug("密码强度检查完成, level={}", response.getLevel());
    return Result.success(response);
  }

  /**
   * 获取当前用户权限列表
   *
   * <p>获取当前登录用户的所有权限列表
   *
   * @param authentication Spring Security认证对象
   * @param response HttpServletResponse to set cache control headers
   * @return 用户权限列表
   */
  @GetMapping("/permissions")
  @Operation(summary = "获取当前用户权限列表", description = "获取当前登录用户的所有权限列表")
  public Result<List<String>> getUserPermissions(
      Authentication authentication, HttpServletResponse responseWrapper) {
    // 设置禁止缓存的HTTP头，避免304状态码问题
    responseWrapper.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    responseWrapper.setHeader("Pragma", "no-cache");
    responseWrapper.setHeader("Expires", "0");
    log.info("获取当前用户权限列表");

    if (authentication == null || !authentication.isAuthenticated()) {
      log.warn("用户未认证");
      return Result.error(401, "用户未认证");
    }

    String username = authentication.getName();
    User user = userService.getByUsername(username);

    if (user == null || user.getDeleted()) {
      log.warn("用户不存在, username={}", username);
      return Result.error(404, "用户不存在");
    }

    // 获取用户权限
    List<com.promanage.domain.entity.Permission> permissions =
        userService.getUserPermissions(user.getId());
    List<String> permissionCodes =
        permissions.stream()
            .map(com.promanage.domain.entity.Permission::getPermissionCode)
            .collect(Collectors.toList());

    log.info("获取当前用户权限列表成功, username={}, permissionCount={}", username, permissionCodes.size());
    return Result.success(permissionCodes);
  }
}