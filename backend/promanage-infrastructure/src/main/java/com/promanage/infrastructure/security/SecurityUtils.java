package com.promanage.infrastructure.security;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.extern.slf4j.Slf4j;

/**
 * Security Utility Class
 *
 * <p>Provides helper methods to access security context information such as current user, username,
 * roles, and authentication status. This class is thread-safe and uses Spring Security's
 * SecurityContextHolder.
 *
 * @author ProManage Team
 * @since 2025-09-30
 */
@Slf4j
public final class SecurityUtils {

  /** Role prefix used by Spring Security */
  private static final String ROLE_PREFIX = "ROLE_";

  /** Private constructor to prevent instantiation */
  private SecurityUtils() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  /**
   * Get current Authentication object from SecurityContext
   *
   * @return Optional containing Authentication if present, empty otherwise
   */
  public static Optional<Authentication> getAuthentication() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.isAuthenticated()) {
      return Optional.of(authentication);
    }
    return Optional.empty();
  }

  /**
   * Get current authenticated user's UserDetails
   *
   * @return Optional containing UserDetails if present, empty otherwise
   */
  public static Optional<UserDetails> getCurrentUser() {
    return getAuthentication()
        .map(Authentication::getPrincipal)
        .filter(principal -> principal instanceof UserDetails)
        .map(principal -> (UserDetails) principal);
  }

  /**
   * Get current authenticated user's username
   *
   * @return Optional containing username if present, empty otherwise
   */
  public static Optional<String> getCurrentUsername() {
    return getAuthentication().map(Authentication::getName);
  }

  /**
   * Get current authenticated user's username or return default value
   *
   * @param defaultValue Default value to return if username is not available
   * @return Username or default value
   */
  public static String getCurrentUsername(String defaultValue) {
    return getCurrentUsername().orElse(defaultValue);
  }

  /**
   * Get current authenticated user's username or throw exception
   *
   * @return Username
   * @throws IllegalStateException if no authenticated user is found
   */
  public static String getCurrentUsernameOrThrow() {
    return getCurrentUsername()
        .orElseThrow(() -> new IllegalStateException("No authenticated user found"));
  }

  /**
   * Get current authenticated user's authorities (roles and permissions)
   *
   * @return Set of authority strings
   */
  public static Set<String> getCurrentUserAuthorities() {
    return getAuthentication().map(Authentication::getAuthorities).stream()
        .flatMap(Collection::stream)
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toSet());
  }

  /**
   * Get current authenticated user's roles (authorities with ROLE_ prefix removed)
   *
   * @return Set of role strings without ROLE_ prefix
   */
  public static Set<String> getCurrentUserRoles() {
    return getCurrentUserAuthorities().stream()
        .filter(authority -> authority.startsWith(ROLE_PREFIX))
        .map(authority -> authority.substring(ROLE_PREFIX.length()))
        .collect(Collectors.toSet());
  }

  /**
   * Check if current user is authenticated
   *
   * @return true if user is authenticated, false otherwise
   */
  public static boolean isAuthenticated() {
    return getAuthentication().map(Authentication::isAuthenticated).orElse(false);
  }

  /**
   * Check if current user is anonymous
   *
   * @return true if user is anonymous, false otherwise
   */
  public static boolean isAnonymous() {
    return !isAuthenticated();
  }

  /**
   * Check if current user has a specific authority
   *
   * @param authority Authority to check
   * @return true if user has the authority, false otherwise
   */
  public static boolean hasAuthority(String authority) {
    if (authority == null) {
      return false;
    }
    return getCurrentUserAuthorities().contains(authority);
  }

  /**
   * Check if current user has any of the specified authorities
   *
   * @param authorities Authorities to check
   * @return true if user has any of the authorities, false otherwise
   */
  public static boolean hasAnyAuthority(String... authorities) {
    if (authorities == null || authorities.length == 0) {
      return false;
    }
    Set<String> userAuthorities = getCurrentUserAuthorities();
    for (String authority : authorities) {
      if (userAuthorities.contains(authority)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Check if current user has all of the specified authorities
   *
   * @param authorities Authorities to check
   * @return true if user has all authorities, false otherwise
   */
  public static boolean hasAllAuthorities(String... authorities) {
    if (authorities == null || authorities.length == 0) {
      return true;
    }
    Set<String> userAuthorities = getCurrentUserAuthorities();
    for (String authority : authorities) {
      if (!userAuthorities.contains(authority)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Check if current user has a specific role
   *
   * @param role Role to check (without ROLE_ prefix)
   * @return true if user has the role, false otherwise
   */
  public static boolean hasRole(String role) {
    if (role == null) {
      return false;
    }
    String authority = role.startsWith(ROLE_PREFIX) ? role : ROLE_PREFIX + role;
    return hasAuthority(authority);
  }

  /**
   * Check if current user has any of the specified roles
   *
   * @param roles Roles to check (without ROLE_ prefix)
   * @return true if user has any of the roles, false otherwise
   */
  public static boolean hasAnyRole(String... roles) {
    if (roles == null || roles.length == 0) {
      return false;
    }
    for (String role : roles) {
      if (hasRole(role)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Check if current user has all of the specified roles
   *
   * @param roles Roles to check (without ROLE_ prefix)
   * @return true if user has all roles, false otherwise
   */
  public static boolean hasAllRoles(String... roles) {
    if (roles == null || roles.length == 0) {
      return true;
    }
    for (String role : roles) {
      if (!hasRole(role)) {
        return false;
      }
    }
    return true;
  }

  /** Clear current security context Useful for logout or testing scenarios */
  public static void clearContext() {
    SecurityContextHolder.clearContext();
    log.debug("Security context cleared");
  }

  /**
   * Get principal object from authentication
   *
   * @return Optional containing principal object
   */
  public static Optional<Object> getPrincipal() {
    return getAuthentication().map(Authentication::getPrincipal);
  }

  /**
   * Get credentials from authentication
   *
   * @return Optional containing credentials
   */
  public static Optional<Object> getCredentials() {
    return getAuthentication().map(Authentication::getCredentials);
  }

  /**
   * Check if current user is a system administrator Assumes admin role is named "ADMIN"
   *
   * @return true if user is admin, false otherwise
   */
  public static boolean isAdmin() {
    return hasRole("ADMIN");
  }

  /**
   * Check if current user is a regular user Assumes user role is named "USER"
   *
   * @return true if user has USER role, false otherwise
   */
  public static boolean isUser() {
    return hasRole("USER");
  }

  /**
   * Get authentication details
   *
   * @return Optional containing authentication details
   */
  public static Optional<Object> getAuthenticationDetails() {
    return getAuthentication().map(Authentication::getDetails);
  }

  /**
   * Get current authenticated user's ID from authentication details
   *
   * <p>This method expects the user ID to be stored in the JWT token claims under the key "userId".
   * The JWT token should be generated with this claim when the user authenticates.
   *
   * @return Optional containing user ID if present, empty otherwise
   */
  public static Optional<Long> getCurrentUserId() {
    return getAuthenticationDetails()
        .filter(details -> details instanceof java.util.Map)
        .flatMap(
            details -> {
              @SuppressWarnings("unchecked")
              java.util.Map<String, Object> detailsMap = (java.util.Map<String, Object>) details;
              Object userId = detailsMap.get("userId");
              if (userId instanceof Number) {
                return Optional.of(((Number) userId).longValue());
              } else if (userId instanceof String) {
                try {
                  return Optional.of(Long.parseLong((String) userId));
                } catch (NumberFormatException e) {
                  log.error("Failed to parse userId from string: {}", userId, e);
                  return Optional.empty();
                }
              }
              return Optional.empty();
            })
        .or(
            () -> {
              // Fallback: try to get from principal if it's a custom UserDetails implementation
              return getCurrentUser()
                  .filter(userDetails -> userDetails instanceof UserDetailsWithId)
                  .map(userDetails -> ((UserDetailsWithId) userDetails).getUserId());
            });
  }

  /**
   * Get current authenticated user's ID or throw exception
   *
   * @return User ID
   * @throws IllegalStateException if no authenticated user is found or user ID is not available
   */
  public static Long getCurrentUserIdOrThrow() {
    return getCurrentUserId()
        .orElseThrow(() -> new IllegalStateException("User ID not available in security context"));
  }

  /**
   * Interface for UserDetails implementations that provide user ID Implement this interface in your
   * custom UserDetails if you want to store user ID in the principal instead of JWT claims
   */
  public interface UserDetailsWithId {
    /**
     * Get the user ID
     *
     * @return User ID
     */
    Long getUserId();
  }
}
