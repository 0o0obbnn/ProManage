package com.promanage.service.impl;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.promanage.common.domain.ResultCode;
import com.promanage.common.entity.User;
import com.promanage.common.exception.BusinessException;
import com.promanage.service.service.IAuthService;
import com.promanage.service.service.IEmailService;
import com.promanage.service.service.IUserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 认证服务实现类
 *
 * <p>实现用户登录认证、密码验证等功能
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

  private final IUserService userService;
  private final PasswordEncoder passwordEncoder;
  private final StringRedisTemplate redisTemplate;
  private final IEmailService emailService;

  private static final String RESET_CODE_PREFIX = "password_reset:";
  private static final String REGISTER_LOCK_PREFIX = "register_lock:";
  private static final int CODE_EXPIRATION_MINUTES = 5;
  private static final int REGISTER_LOCK_TIMEOUT_SECONDS = 10;
  private static final SecureRandom RANDOM = new SecureRandom();

  @Override
  public User authenticate(String username, String password) {
    log.info("用户认证, username={}", username);

    // 查询用户
    User user = userService.getByUsername(username);
    if (user == null || user.getDeleted()) {
      log.warn("用户不存在, username={}", username);
      throw new BusinessException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
    }

    // 验证密码
    String dbPassword = user.getPassword();
    log.info(
        "DEBUG - username={}, dbPasswordIsNull={}, dbPasswordLength={}",
        username,
        dbPassword == null,
        dbPassword != null ? dbPassword.length() : 0);
    if (dbPassword != null && dbPassword.length() > 0) {
      log.info(
          "DEBUG - dbPasswordPrefix={}",
          dbPassword.substring(0, Math.min(20, dbPassword.length())));
    }
    if (!passwordEncoder.matches(password, user.getPassword())) {
      log.warn("密码错误, username={}", username);
      throw new BusinessException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
    }

    // 检查用户状态
    if (user.getStatus() == null || user.getStatus() != 0) {
      log.warn("用户状态异常, username={}, status={}", username, user.getStatus());
      String message = "账户已被禁用";
      if (user.getStatus() != null && user.getStatus() == 2) {
        message = "账户已被锁定";
      }
      throw new BusinessException(ResultCode.FORBIDDEN, message);
    }

    log.info("用户认证成功, username={}, userId={}", username, user.getId());
    return user;
  }

  @Override
  public void updateLastLogin(Long userId, String ipAddress) {
    log.info("更新最后登录信息, userId={}, ipAddress={}", userId, ipAddress);
    userService.updateLastLogin(userId, ipAddress);
  }

  /**
   * 用户注册
   *
   * <p>使用Redis分布式锁防止并发注册导致的重复用户创建
   *
   * @param username 用户名
   * @param password 密码
   * @param email 邮箱
   * @param phone 电话
   * @param realName 真实姓名
   * @return 注册成功的用户
   */
  @Override
  public User register(
      String username, String password, String email, String phone, String realName) {
    log.info("用户注册, username={}, email={}", username, email);

    // 使用Redis分布式锁防止并发注册
    String usernameLockKey = REGISTER_LOCK_PREFIX + "username:" + username;
    String emailLockKey = REGISTER_LOCK_PREFIX + "email:" + email;

    // 尝试获取用户名锁
    Boolean usernameLockAcquired =
        redisTemplate
            .opsForValue()
            .setIfAbsent(usernameLockKey, "1", REGISTER_LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS);

    if (Boolean.FALSE.equals(usernameLockAcquired)) {
      log.warn("用户名注册冲突，其他请求正在注册相同用户名, username={}", username);
      throw new BusinessException(ResultCode.BAD_REQUEST, "系统繁忙，请稍后重试");
    }

    try {
      // 尝试获取邮箱锁
      Boolean emailLockAcquired =
          redisTemplate
              .opsForValue()
              .setIfAbsent(emailLockKey, "1", REGISTER_LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS);

      if (Boolean.FALSE.equals(emailLockAcquired)) {
        log.warn("邮箱注册冲突，其他请求正在注册相同邮箱, email={}", email);
        throw new BusinessException(ResultCode.BAD_REQUEST, "系统繁忙，请稍后重试");
      }

      try {
        // 双重检查：验证用户名唯一性
        if (userService.existsByUsername(username)) {
          log.warn("用户名已存在, username={}", username);
          throw new BusinessException(ResultCode.BAD_REQUEST, "用户名已存在");
        }

        // 双重检查：验证邮箱唯一性
        if (userService.existsByEmail(email)) {
          log.warn("邮箱已被注册, email={}", email);
          throw new BusinessException(ResultCode.BAD_REQUEST, "邮箱已被注册");
        }

        // 创建用户实体
        User user = new User();
        user.setUsername(username);
        user.setPassword(password); // 密码会在 register 方法中自动加密
        user.setEmail(email);
        user.setPhone(phone);
        user.setRealName(realName);

        // 调用UserService创建用户
        Long userId = userService.create(user);
        if (userId == null || userId <= 0) {
          log.error("用户创建失败, username={}", username);
          throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "用户创建失败");
        }

        // 重新查询用户信息返回
        User createdUser = userService.getById(userId);
        if (createdUser == null) {
          log.error("用户创建成功但查询失败, userId={}", userId);
          throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "用户创建异常");
        }

        log.info("用户注册成功, username={}, userId={}", username, userId);
        return createdUser;

      } finally {
        // 释放邮箱锁
        redisTemplate.delete(emailLockKey);
      }

    } finally {
      // 释放用户名锁
      redisTemplate.delete(usernameLockKey);
    }
  }

  @Override
  public User register(
      String username,
      String password,
      String confirmPassword,
      String email,
      String phone,
      String realName) {
    // 验证两次密码是否一致
    if (!password.equals(confirmPassword)) {
      log.warn("两次密码不一致, username={}", username);
      throw new BusinessException(ResultCode.BAD_REQUEST, "两次密码不一致");
    }

    // 调用原有的注册方法
    return register(username, password, email, phone, realName);
  }

  @Override
  public void sendPasswordResetCode(String email) {
    log.info("发送密码重置验证码, email={}", email);

    // 验证邮箱是否存在
    User user = userService.getByEmail(email);
    if (user == null || user.getDeleted()) {
      log.warn("邮箱未注册, email={}", email);
      throw new BusinessException(ResultCode.NOT_FOUND, "该邮箱未注册");
    }

    // 生成6位数字验证码
    String code = String.format("%06d", RANDOM.nextInt(1000000));

    // 存入Redis，有效期5分钟
    String key = RESET_CODE_PREFIX + email;
    redisTemplate.opsForValue().set(key, code, CODE_EXPIRATION_MINUTES, TimeUnit.MINUTES);

    // 发送邮件
    try {
      emailService.sendPasswordResetCode(email, code);
      log.info("密码重置验证码已发送, email={}", email);
    } catch (MailException e) {
      log.error("发送验证码邮件失败, email={}", email, e);
      // 即使邮件发送失败，也不删除验证码，以便开发环境测试
      // 在生产环境中可以考虑抛出异常
      throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "邮件发送失败，请稍后重试");
    }
  }

  @Override
  public boolean verifyResetCode(String email, String code) {
    log.info("验证密码重置验证码, email={}", email);

    String key = RESET_CODE_PREFIX + email;
    String storedCode = redisTemplate.opsForValue().get(key);

    if (storedCode == null) {
      log.warn("验证码不存在或已过期, email={}", email);
      return false;
    }

    boolean valid = storedCode.equals(code);
    log.info("验证码验证结果, email={}, valid={}", email, valid);
    return valid;
  }

  @Override
  public void resetPassword(String email, String code, String newPassword) {
    log.info("重置密码, email={}", email);

    // 验证验证码
    if (!verifyResetCode(email, code)) {
      log.warn("验证码无效, email={}", email);
      throw new BusinessException(ResultCode.BAD_REQUEST, "验证码无效或已过期");
    }

    // 获取用户
    User user = userService.getByEmail(email);
    if (user == null || user.getDeleted()) {
      log.warn("用户不存在, email={}", email);
      throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
    }

    // 重置密码 (使用resetPassword方法，不需要验证旧密码)
    userService.resetPassword(user.getId(), newPassword);

    // 删除验证码
    String key = RESET_CODE_PREFIX + email;
    redisTemplate.delete(key);

    log.info("密码重置成功, email={}, userId={}", email, user.getId());
  }

  @Override
  public void resetPassword(String email, String code, String newPassword, String confirmPassword) {
    // 验证两次密码是否一致
    if (!newPassword.equals(confirmPassword)) {
      log.warn("两次密码不一致, email={}", email);
      throw new BusinessException(ResultCode.BAD_REQUEST, "两次密码不一致");
    }

    // 调用原有的重置密码方法
    resetPassword(email, code, newPassword);
  }
}