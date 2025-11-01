package com.promanage.api.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.promanage.infrastructure.security.JwtTokenProvider;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;

import lombok.RequiredArgsConstructor;

/**
 * WebSocket认证服务
 *
 * <p>负责WebSocket连接中的JWT Token解析和认证
 *
 * @author ProManage Team
 * @since 2025-11-01
 */
@RequiredArgsConstructor
@Component
public class WebSocketAuthService {

  private static final Logger log = LoggerFactory.getLogger(WebSocketAuthService.class);

  private final JwtTokenProvider jwtTokenProvider;

  /**
   * 从Token解析用户ID
   *
   * <p>解析JWT Token并提取用户ID，处理各种可能的异常情况
   *
   * @param token JWT Token
   * @return 用户ID，如果Token无效或解析失败则返回null
   */
  public Long parseUserIdFromToken(String token) {
    if (token == null || token.trim().isEmpty()) {
      log.warn("Token为空");
      return null;
    }

    try {
      return jwtTokenProvider.getUserIdFromToken(token);
    } catch (ExpiredJwtException e) {
      log.warn("Token已过期: {}", e.getMessage());
      return null;
    } catch (MalformedJwtException e) {
      log.warn("Token格式错误: {}", e.getMessage());
      return null;
    } catch (SignatureException e) {
      log.warn("Token签名无效: {}", e.getMessage());
      return null;
    } catch (IllegalArgumentException e) {
      log.warn("Token参数错误: {}", e.getMessage());
      return null;
    } catch (RuntimeException e) {
      log.error("Token解析失败: {}", e.getMessage(), e);
      return null;
    }
  }

  /**
   * 验证Token是否有效
   *
   * @param token JWT Token
   * @return true如果Token有效，false否则
   */
  public boolean isTokenValid(String token) {
    if (token == null || token.trim().isEmpty()) {
      return false;
    }

    try {
      return jwtTokenProvider.validateToken(token);
    } catch (ExpiredJwtException e) {
      log.debug("Token已过期: {}", e.getMessage());
      return false;
    } catch (MalformedJwtException e) {
      log.debug("Token格式错误: {}", e.getMessage());
      return false;
    } catch (SignatureException e) {
      log.debug("Token签名无效: {}", e.getMessage());
      return false;
    } catch (IllegalArgumentException e) {
      log.debug("Token参数错误: {}", e.getMessage());
      return false;
    } catch (RuntimeException e) {
      log.error("Token验证失败: {}", e.getMessage(), e);
      return false;
    }
  }
}


