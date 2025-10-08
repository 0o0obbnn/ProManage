package com.promanage.infrastructure.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * JWT Token Provider
 * <p>
 * Handles JWT token generation, validation, and extraction of claims.
 * Uses JJWT library with HS512 signature algorithm for secure token management.
 * </p>
 *
 * @author ProManage Team
 * @since 2025-09-30
 */
@Slf4j
@Component
public class JwtTokenProvider {

    /**
     * JWT secret key (minimum 512 bits for HS512)
     * Should be configured in application.yml and kept secure
     * NO DEFAULT VALUE - must be configured in production
     */
    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * JWT token expiration time in milliseconds
     * Default: 24 hours (86400000 ms)
     */
    @Value("${jwt.expiration:86400000}")
    private long jwtExpirationMs;

    /**
     * JWT refresh token expiration time in milliseconds
     * Default: 7 days (604800000 ms)
     */
    @Value("${jwt.refresh-expiration:604800000}")
    private long jwtRefreshExpirationMs;

    /**
     * JWT token issuer
     */
    @Value("${jwt.issuer:ProManage}")
    private String jwtIssuer;

    /**
     * Claims key for user roles
     */
    private static final String CLAIM_KEY_AUTHORITIES = "authorities";

    /**
     * Claims key for user ID
     */
    private static final String CLAIM_KEY_USER_ID = "userId";

    /**
     * Minimum required secret length (in characters)
     */
    private static final int MIN_SECRET_LENGTH = 64;

    /**
     * Validate JWT configuration on application startup
     * This ensures that weak or default secrets are detected early
     */
    @PostConstruct
    public void validateConfiguration() {
        log.info("Validating JWT configuration...");

        // Check if secret is provided
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            throw new IllegalStateException(
                "JWT secret must be configured in application.yml. " +
                "Set jwt.secret property with a strong random secret (at least 64 characters)."
            );
        }

        // Check secret length
        if (jwtSecret.length() < MIN_SECRET_LENGTH) {
            throw new IllegalStateException(
                String.format(
                    "JWT secret is too short (%d characters). Minimum required: %d characters. " +
                    "Please use a strong random secret.",
                    jwtSecret.length(), MIN_SECRET_LENGTH
                )
            );
        }

        // Check for weak/common secrets using enhanced validation
        if (!JwtSecretGenerator.isStrongSecret(jwtSecret)) {
            double entropy = JwtSecretGenerator.calculateEntropy(jwtSecret);
            log.error("⚠️  SECURITY CRITICAL: JWT secret failed strength validation!");
            log.error("⚠️  Secret entropy: {} bits (minimum required: 256 bits)", String.format("%.2f", entropy));
            log.error("⚠️  Secret length: {} characters (minimum required: 64 characters)", jwtSecret.length());
            log.error("⚠️  SECURITY RISK: Weak JWT secrets can be cracked, leading to complete authentication bypass!");
            log.error("⚠️  Generate a strong secret immediately using one of these methods:");
            log.error("⚠️    1. Run: java -cp your-app.jar com.promanage.infrastructure.security.JwtSecretGenerator");
            log.error("⚠️    2. Use: openssl rand -base64 64");
            log.error("⚠️    3. Use: pwgen -s 64 1");
            log.error("⚠️  Then configure: export JWT_SECRET=\"your-generated-secret\"");
            throw new IllegalStateException(
                "JWT secret failed security validation. Please generate a strong random secret with at least 256 bits of entropy."
            );
        }

        log.info("✅ JWT secret passed security validation");
        log.info("✅ Secret length: {} characters", jwtSecret.length());
        log.info("✅ Secret entropy: {} bits", String.format("%.2f", JwtSecretGenerator.calculateEntropy(jwtSecret)));

        // Check expiration times
        if (jwtExpirationMs <= 0) {
            throw new IllegalStateException("JWT expiration time must be positive");
        }

        if (jwtRefreshExpirationMs <= jwtExpirationMs) {
            log.warn("⚠️  Refresh token expiration ({} ms) should be longer than access token expiration ({} ms)",
                jwtRefreshExpirationMs, jwtExpirationMs);
        }

        log.info("✅ JWT configuration validated successfully");
        log.info("   - Secret length: {} characters", jwtSecret.length());
        log.info("   - Access token expiration: {} ms ({} hours)",
            jwtExpirationMs, jwtExpirationMs / 3600000);
        log.info("   - Refresh token expiration: {} ms ({} days)",
            jwtRefreshExpirationMs, jwtRefreshExpirationMs / 86400000);
        log.info("   - Issuer: {}", jwtIssuer);
    }

    /**
     * Generate JWT token from Authentication object
     *
     * @param authentication Spring Security Authentication object
     * @return JWT token string
     */
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return generateToken(userDetails.getUsername(), authentication);
    }

    /**
     * Generate JWT token with username and authentication details
     *
     * @param username Username
     * @param authentication Authentication object containing authorities
     * @return JWT token string
     */
    public String generateToken(String username, Authentication authentication) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        // Extract authorities (roles/permissions)
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .subject(username)
                .claim(CLAIM_KEY_AUTHORITIES, authorities)
                .issuer(jwtIssuer)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), Jwts.SIG.HS512)
                .compact();
    }

    /**
     * Generate JWT token with username and user ID
     *
     * @param username Username
     * @param userId User ID
     * @param authorities User authorities/roles
     * @return JWT token string
     */
    public String generateToken(String username, Long userId, String authorities) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(username)
                .claim(CLAIM_KEY_USER_ID, userId)
                .claim(CLAIM_KEY_AUTHORITIES, authorities)
                .issuer(jwtIssuer)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), Jwts.SIG.HS512)
                .compact();
    }

    /**
     * Generate refresh token
     *
     * @param username Username
     * @return Refresh token string
     */
    public String generateRefreshToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtRefreshExpirationMs);

        return Jwts.builder()
                .subject(username)
                .issuer(jwtIssuer)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), Jwts.SIG.HS512)
                .compact();
    }

    /**
     * Extract username from JWT token
     *
     * @param token JWT token
     * @return Username
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getSubject();
        } catch (Exception e) {
            log.error("Failed to extract username from token", e);
            return null;
        }
    }

    /**
     * Extract user ID from JWT token
     *
     * @param token JWT token
     * @return User ID
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Object userId = claims.get(CLAIM_KEY_USER_ID);
            if (userId instanceof Number) {
                return ((Number) userId).longValue();
            }
            return null;
        } catch (Exception e) {
            log.error("Failed to extract user ID from token", e);
            return null;
        }
    }

    /**
     * Extract authorities from JWT token
     *
     * @param token JWT token
     * @return Comma-separated authorities string
     */
    public String getAuthoritiesFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.get(CLAIM_KEY_AUTHORITIES, String.class);
        } catch (Exception e) {
            log.error("Failed to extract authorities from token", e);
            return null;
        }
    }

    /**
     * Extract expiration date from JWT token
     *
     * @param token JWT token
     * @return Expiration date
     */
    public Date getExpirationDateFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getExpiration();
        } catch (Exception e) {
            log.error("Failed to extract expiration date from token", e);
            return null;
        }
    }

    /**
     * Get all claims from JWT token
     *
     * @param token JWT token
     * @return Claims object
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Validate JWT token
     *
     * @param token JWT token
     * @return true if token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        } catch (Exception e) {
            log.error("JWT token validation failed: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Check if token is expired
     *
     * @param token JWT token
     * @return true if token is expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration != null && expiration.before(new Date());
        } catch (Exception e) {
            log.error("Failed to check token expiration", e);
            return true;
        }
    }

    /**
     * Get signing key from secret
     *
     * @return SecretKey for JWT signing
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Get token expiration time in milliseconds
     *
     * @return Token expiration time
     */
    public long getJwtExpirationMs() {
        return jwtExpirationMs;
    }

    /**
     * Get refresh token expiration time in milliseconds
     *
     * @return Refresh token expiration time
     */
    public long getJwtRefreshExpirationMs() {
        return jwtRefreshExpirationMs;
    }
}