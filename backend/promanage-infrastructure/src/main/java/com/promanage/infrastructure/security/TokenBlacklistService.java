package com.promanage.infrastructure.security;

import java.util.concurrent.TimeUnit;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Token Blacklist Service
 *
 * <p>Manages JWT token blacklist for secure logout functionality. Stores blacklisted tokens in
 * Redis with automatic expiration.
 *
 * @author ProManage Team
 * @since 2025-10-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

  private final RedisTemplate<String, String> redisTemplate;
  private final JwtTokenProvider jwtTokenProvider;

  private static final String BLACKLIST_PREFIX = "blacklist:token:";
  private static final String BLACKLIST_SET_PREFIX = "blacklist:user:";

  /**
   * Blacklist a JWT token
   *
   * @param token JWT token to blacklist
   */
  public void blacklistToken(String token) {
    if (token == null || token.isBlank()) {
      log.warn("Attempt to blacklist empty token");
      return;
    }

    try {
      // Extract token ID (jti claim) or use token hash if jti not available
      String tokenId = extractTokenId(token);
      String username = jwtTokenProvider.getUsernameFromToken(token);
      Long userId = jwtTokenProvider.getUserIdFromToken(token);

      // Calculate remaining time until token expiration
      long expirationTime = jwtTokenProvider.getExpirationDateFromToken(token).getTime();
      long currentTime = System.currentTimeMillis();
      long ttl = Math.max(expirationTime - currentTime, 0);

      if (ttl > 0) {
        // Store in blacklist with TTL matching token expiration
        String blacklistKey = BLACKLIST_PREFIX + tokenId;
        redisTemplate.opsForValue().set(blacklistKey, "1", ttl, TimeUnit.MILLISECONDS);

        // Also track by user for audit purposes
        String userBlacklistKey = BLACKLIST_SET_PREFIX + userId;
        redisTemplate.opsForSet().add(userBlacklistKey, tokenId);
        redisTemplate.expire(userBlacklistKey, ttl, TimeUnit.MILLISECONDS);

        log.info(
            "Token blacklisted successfully - user: {}, tokenId: {}, ttl: {}ms",
            username,
            tokenId,
            ttl);
      } else {
        log.warn("Attempt to blacklist already expired token - user: {}", username);
      }

    } catch (Exception e) {
      log.error("Failed to blacklist token", e);
      throw new RuntimeException("Token blacklisting failed", e);
    }
  }

  /**
   * Check if a token is blacklisted
   *
   * @param token JWT token to check
   * @return true if token is blacklisted
   */
  public boolean isBlacklisted(String token) {
    if (token == null || token.isBlank()) {
      return false;
    }

    try {
      String tokenId = extractTokenId(token);
      String blacklistKey = BLACKLIST_PREFIX + tokenId;

      Boolean isBlacklisted = redisTemplate.hasKey(blacklistKey);
      boolean result = Boolean.TRUE.equals(isBlacklisted);

      if (result) {
        log.debug("Token is blacklisted: {}", tokenId);
      }

      return result;

    } catch (Exception e) {
      log.error("Error checking token blacklist status", e);
      throw new RuntimeException("Failed to check token blacklist status", e);
    }
  }

  /**
   * Get all blacklisted tokens for a specific user
   *
   * @param userId User ID
   * @return Number of blacklisted tokens for the user
   */
  public long getUserBlacklistCount(Long userId) {
    if (userId == null) {
      return 0;
    }

    try {
      String userBlacklistKey = BLACKLIST_SET_PREFIX + userId;
      Long count = redisTemplate.opsForSet().size(userBlacklistKey);
      return count != null ? count : 0;

    } catch (Exception e) {
      log.error("Error getting user blacklist count for user: {}", userId, e);
      throw new RuntimeException("Failed to get user blacklist count", e);
    }
  }

  /**
   * Clear all blacklisted tokens for a specific user
   *
   * @param userId User ID
   */
  public void clearUserBlacklist(Long userId) {
    if (userId == null) {
      return;
    }

    try {
      String userBlacklistKey = BLACKLIST_SET_PREFIX + userId;
      var tokenIds = redisTemplate.opsForSet().members(userBlacklistKey);

      if (tokenIds != null) {
        // Remove individual token blacklist entries
        for (String tokenId : tokenIds) {
          String blacklistKey = BLACKLIST_PREFIX + tokenId;
          redisTemplate.delete(blacklistKey);
        }
      }

      // Remove user blacklist set
      redisTemplate.delete(userBlacklistKey);
      log.info("Cleared blacklist for user: {}", userId);

    } catch (Exception e) {
      log.error("Error clearing user blacklist for user: {}", userId, e);
      throw new RuntimeException("Failed to clear user blacklist", e);
    }
  }

  /**
   * Extract token identifier using token hash
   *
   * <p>Since we don't have a jti (JWT ID) claim, we use the SHA-256 hash of the token as a unique
   * identifier for blacklisting.
   *
   * @param token JWT token
   * @return Token identifier (SHA-256 hash)
   */
  private String extractTokenId(String token) {
    return generateTokenHash(token);
  }

  /**
   * Generate a hash of the token for identification
   *
   * @param token JWT token
   * @return Token hash
   */
  private String generateTokenHash(String token) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
      StringBuilder hexString = new StringBuilder();
      for (byte b : hash) {
        hexString.append(String.format("%02x", b));
      }
      return hexString.toString();
    } catch (NoSuchAlgorithmException e) {
      log.error("SHA-256 algorithm not found", e);
      throw new IllegalStateException("SHA-256 algorithm not found", e);
    }
  }

  /** Clean up expired blacklist entries (can be scheduled) */
  public void cleanupExpiredTokens() {
    log.debug("Starting token blacklist cleanup");

    // Redis will handle TTL expiration automatically
    // This method can be used for additional cleanup if needed

    // For now, just log statistics
    try {
      var keys = redisTemplate.keys(BLACKLIST_PREFIX + "*");
      if (keys != null) {
        log.info("Token blacklist cleanup completed. Active blacklisted tokens: {}", keys.size());
      }
    } catch (Exception e) {
      log.error("Error during token blacklist cleanup", e);
      throw new RuntimeException("Failed to cleanup expired tokens", e);
    }
  }

  /**
   * Get blacklist statistics
   *
   * @return Blacklist statistics
   */
  public BlacklistStatistics getStatistics() {
    try {
      var tokenKeys = redisTemplate.keys(BLACKLIST_PREFIX + "*");
      var userKeys = redisTemplate.keys(BLACKLIST_SET_PREFIX + "*");

      long totalBlacklisted = tokenKeys != null ? tokenKeys.size() : 0;
      long totalUsers = userKeys != null ? userKeys.size() : 0;

      return new BlacklistStatistics(totalBlacklisted, totalUsers);

    } catch (Exception e) {
      log.error("Error getting blacklist statistics", e);
      throw new RuntimeException("Failed to get blacklist statistics", e);
    }
  }

  /** Blacklist statistics */
  public static class BlacklistStatistics {
    private final long totalBlacklistedTokens;
    private final long totalUsersWithBlacklistedTokens;

    public BlacklistStatistics(long totalBlacklistedTokens, long totalUsersWithBlacklistedTokens) {
      this.totalBlacklistedTokens = totalBlacklistedTokens;
      this.totalUsersWithBlacklistedTokens = totalUsersWithBlacklistedTokens;
    }

    public long getTotalBlacklistedTokens() {
      return totalBlacklistedTokens;
    }

    public long getTotalUsersWithBlacklistedTokens() {
      return totalUsersWithBlacklistedTokens;
    }
  }
}
