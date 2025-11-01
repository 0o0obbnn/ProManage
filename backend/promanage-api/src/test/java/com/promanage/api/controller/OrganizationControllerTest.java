package com.promanage.api.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.promanage.common.entity.Organization;
import com.promanage.common.result.PageResult;
import com.promanage.dto.OrganizationMemberDTO;
import com.promanage.dto.OrganizationSettingsDTO;
import com.promanage.dto.mapper.OrganizationMapperImpl;
import com.promanage.infrastructure.security.JwtAuthenticationEntryPoint;
import com.promanage.infrastructure.security.JwtAuthenticationFilter;
import com.promanage.infrastructure.security.JwtTokenProvider;
import com.promanage.infrastructure.security.TokenBlacklistService;
import com.promanage.service.IOrganizationService;

@WebMvcTest(OrganizationController.class)
@Import(OrganizationMapperImpl.class)
class OrganizationControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private IOrganizationService organizationService;

  @MockBean private JwtTokenProvider jwtTokenProvider;

  @MockBean private TokenBlacklistService tokenBlacklistService;

  @MockBean private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

  @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;

  @MockBean private UserDetailsService userDetailsService;

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("分页获取组织列表应返回DTO并包含分页元数据")
  void listOrganizations_shouldReturnDtoPage() throws Exception {
    Organization organization = new Organization();
    organization.setId(1L);
    organization.setName("Acme Corp");
    organization.setSlug("acme");
    organization.setDescription("Global company");
    organization.setContactEmail("admin@acme.com");
    organization.setIsActive(true);
    organization.setSubscriptionPlan("PRO");
    organization.setSubscriptionExpiresAt(LocalDateTime.of(2025, 12, 31, 0, 0));

    PageResult<Organization> pageResult = PageResult.of(List.of(organization), 1L, 1, 20);
    when(organizationService.listOrganizations(eq(42L), eq(1), eq(20), isNull(), isNull()))
        .thenReturn(pageResult);

    mockMvc
        .perform(get("/api/v1/organizations").with(authenticatedUser(42L, "ORGANIZATION_VIEW")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data.list[0].id").value(1))
        .andExpect(jsonPath("$.data.list[0].name").value("Acme Corp"))
        .andExpect(jsonPath("$.data.page").value(1))
        .andExpect(jsonPath("$.data.total").value(1));

    verify(organizationService).listOrganizations(42L, 1, 20, null, null);
  }

  @Test
  @DisplayName("未携带认证信息的请求返回401")
  void listOrganizations_withoutAuth_shouldReturnUnauthorized() throws Exception {
    mockMvc.perform(get("/api/v1/organizations")).andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("获取组织成员列表应返回脱敏成员DTO")
  void getOrganizationMembers_shouldReturnSanitizedMembers() throws Exception {
    OrganizationMemberDTO memberDTO =
        OrganizationMemberDTO.builder()
            .id(10L)
            .username("jane")
            .email("jane@example.com")
            .realName("Jane Doe")
            .build();

    PageResult<OrganizationMemberDTO> pageResult = PageResult.of(List.of(memberDTO), 1L, 1, 20);
    when(organizationService.listOrganizationMembers(eq(5L), eq(42L), eq(1), eq(20)))
        .thenReturn(pageResult);

    mockMvc
        .perform(
            get("/api/v1/organizations/{id}/members", 5L)
                .param("page", "1")
                .param("pageSize", "20")
                .with(authenticatedUser(42L, "ORGANIZATION_VIEW")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.list[0].id").value(10))
        .andExpect(jsonPath("$.data.list[0].username").value("jane"))
        .andExpect(jsonPath("$.data.list[0].realName").value("Jane Doe"));

    verify(organizationService).listOrganizationMembers(5L, 42L, 1, 20);
  }

  @Test
  @DisplayName("更新组织设置应调用服务层并返回新设置")
  void updateOrganizationSettings_shouldReturnUpdatedSettings() throws Exception {
    OrganizationSettingsDTO request =
        OrganizationSettingsDTO.builder()
            .notification(
                OrganizationSettingsDTO.NotificationSettings.builder()
                    .emailEnabled(true)
                    .inAppEnabled(true)
                    .websocketEnabled(true)
                    .digestFrequencyDays(1)
                    .build())
            .security(
                OrganizationSettingsDTO.SecuritySettings.builder()
                    .passwordMinLength(10)
                    .passwordRequireSpecialChar(true)
                    .sessionTimeoutMinutes(90)
                    .twoFactorAuthEnabled(true)
                    .build())
            .project(
                OrganizationSettingsDTO.ProjectSettings.builder()
                    .defaultVisibility("PRIVATE")
                    .allowPublicProjects(true)
                    .maxProjects(100)
                    .maxMembersPerProject(50)
                    .storageLimitMb(20480L)
                    .build())
            .build();

    when(organizationService.updateOrganizationSettings(
            eq(5L), any(OrganizationSettingsDTO.class), eq(42L)))
        .thenReturn(request);

    mockMvc
        .perform(
            put("/api/v1/organizations/{id}/settings", 5L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(authenticatedUser(42L, "ORGANIZATION_UPDATE")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.security.passwordMinLength").value(10))
        .andExpect(jsonPath("$.data.project.storageLimitMb").value(20480L));

    verify(organizationService)
        .updateOrganizationSettings(eq(5L), any(OrganizationSettingsDTO.class), eq(42L));
  }

  private RequestPostProcessor authenticatedUser(Long userId, String... authorities) {
    return request -> {
      List<GrantedAuthority> grantedAuthorities = new java.util.ArrayList<>();
      for (String authority : authorities) {
        grantedAuthorities.add(new SimpleGrantedAuthority(authority));
      }
      UsernamePasswordAuthenticationToken authenticationToken =
          new UsernamePasswordAuthenticationToken("mockUser", "password", grantedAuthorities);
      Map<String, Object> details = new HashMap<>();
      details.put("userId", userId);
      authenticationToken.setDetails(details);
      SecurityContextHolder.getContext().setAuthentication(authenticationToken);
      request.setUserPrincipal(authenticationToken);
      return request;
    };
  }
}
