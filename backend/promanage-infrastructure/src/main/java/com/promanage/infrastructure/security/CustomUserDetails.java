package com.promanage.infrastructure.security;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.promanage.common.entity.User;

public class CustomUserDetails implements UserDetails {

  private final Long id;
  private final String username;
  @JsonIgnore private final String password;
  private final Long organizationId;
  private final boolean isActive;
  private final Collection<? extends GrantedAuthority> authorities;

  public CustomUserDetails(
      Long id,
      String username,
      String password,
      Long organizationId,
      boolean isActive,
      Collection<? extends GrantedAuthority> authorities) {
    this.id = id;
    this.username = username;
    this.password = password;
    this.organizationId = organizationId;
    this.isActive = isActive;
    this.authorities = authorities;
  }

  public static CustomUserDetails create(User user) {
    // In a real application, you would fetch user's roles/authorities from the database.
    // For now, we'll grant a simple 'ROLE_USER'.
    List<GrantedAuthority> authorities =
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

    return new CustomUserDetails(
        user.getId(),
        user.getUsername(),
        user.getPassword(),
        user.getOrganizationId(),
        user.getStatus() != null && user.getStatus() == 1, // status 1 = active
        authorities);
  }

  public Long getId() {
    return id;
  }

  public Long getOrganizationId() {
    return organizationId;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return isActive;
  }
}
