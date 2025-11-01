package com.promanage.api.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.promanage.common.entity.Organization;
import com.promanage.common.entity.User;
import com.promanage.common.result.PageResult;
import com.promanage.dto.OrganizationMemberDTO;
import com.promanage.dto.OrganizationSettingsDTO;
import com.promanage.infrastructure.security.JwtAuthenticationEntryPoint;
import com.promanage.infrastructure.security.JwtAuthenticationFilter;
import com.promanage.infrastructure.security.JwtTokenProvider;
import com.promanage.infrastructure.security.TokenBlacklistService;
import com.promanage.service.IOrganizationService;

/**
 * 安全/租户回归测试套件 - 管理员/成员权限分支测试
 *
 * <p>测试场景：验证管理员和普通成员的权限差异，确保权限控制正确
 *
 * @author ProManage Team
 * @date 2025-10-12
 */
@WebMvcTest(OrganizationController.class)
@DisplayName("管理员/成员权限分支回归测试")
class RoleBasedPermissionTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private IOrganizationService organizationService;

  @MockBean private JwtTokenProvider jwtTokenProvider;

  @MockBean private TokenBlacklistService tokenBlacklistService;

  @MockBean private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

  @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;

  private User orgAdmin;
  private User orgMember;
  private Organization organization;

  @BeforeEach
  void setUp() {
    // 设置组织
    organization = new Organization();
    organization.setId(1L);
    organization.setName("测试组织");
    organization.setSlug("test-org");
    organization.setIsActive(true);
    organization.setSubscriptionPlan("PRO");
    organization.setSubscriptionExpiresAt(LocalDateTime.of(2025, 12, 31, 0, 0));

    // 设置组织管理员
    orgAdmin = new User();
    orgAdmin.setId(100L);
    orgAdmin.setUsername("admin");
    orgAdmin.setEmail("admin@test.com");
    orgAdmin.setRealName("管理员");
    orgAdmin.setOrganizationId(1L);

    // 设置组织普通成员
    orgMember = new User();
    orgMember.setId(200L);
    orgMember.setUsername("member");
    orgMember.setEmail("member@test.com");
    orgMember.setRealName("普通成员");
    orgMember.setOrganizationId(1L);
  }

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  // ==================== 管理员权限测试 ====================

  @Test
  @DisplayName("管理员可以更新组织设置")
  void admin_canUpdateOrganizationSettings() throws Exception {
    OrganizationSettingsDTO settings =
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
            eq(1L), any(OrganizationSettingsDTO.class), eq(100L)))
        .thenReturn(settings);

    mockMvc
        .perform(
            put("/api/v1/organizations/{id}/settings", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(settings))
                .with(
                    user("mockUser")
                        .authorities(new SimpleGrantedAuthority("ORGANIZATION_UPDATE"))
                        .password("password")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data.security.passwordMinLength").value(10));
  }

  @Test
  @DisplayName("管理员可以激活组织")
  void admin_canActivateOrganization() throws Exception {
    doNothing().when(organizationService).activateOrganization(eq(1L), eq(100L));

    mockMvc
        .perform(
            put("/api/v1/organizations/{id}/activate", 1L)
                .with(
                    user("mockUser")
                        .authorities(new SimpleGrantedAuthority("ORGANIZATION_UPDATE"))
                        .password("password")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200));
  }

  @Test
  @DisplayName("管理员可以停用组织")
  void admin_canDeactivateOrganization() throws Exception {
    doNothing().when(organizationService).deactivateOrganization(eq(1L), eq(100L));

    mockMvc
        .perform(
            put("/api/v1/organizations/{id}/deactivate", 1L)
                .with(
                    user("mockUser")
                        .authorities(new SimpleGrantedAuthority("ORGANIZATION_UPDATE"))
                        .password("password")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200));
  }

  @Test
  @DisplayName("管理员可以更新订阅计划")
  void admin_canUpdateSubscriptionPlan() throws Exception {
    String subscriptionJson =
        """
                {
                    "plan": "ENTERPRISE",
                    "expiresAt": "2025-12-31T23:59:59"
                }
                """;

    doNothing()
        .when(organizationService)
        .updateSubscriptionPlan(eq(1L), eq("ENTERPRISE"), any(LocalDateTime.class), eq(100L));

    mockMvc
        .perform(
            put("/api/v1/organizations/{id}/subscription", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(subscriptionJson)
                .with(
                    user("mockUser")
                        .authorities(new SimpleGrantedAuthority("ORGANIZATION_UPDATE"))
                        .password("password")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200));
  }

  // ==================== 成员权限测试 ====================

  @Test
  @DisplayName("成员不能更新组织设置应返回403")
  void member_cannotUpdateOrganizationSettings_shouldReturn403() throws Exception {
    OrganizationSettingsDTO settings =
        OrganizationSettingsDTO.builder()
            .notification(
                OrganizationSettingsDTO.NotificationSettings.builder()
                    .emailEnabled(true)
                    .inAppEnabled(true)
                    .websocketEnabled(true)
                    .digestFrequencyDays(1)
                    .build())
            .build();

    when(organizationService.updateOrganizationSettings(
            eq(1L), any(OrganizationSettingsDTO.class), eq(200L)))
        .thenThrow(new RuntimeException("无权操作"));

    mockMvc
        .perform(
            put("/api/v1/organizations/{id}/settings", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(settings))
                .with(
                    user("mockUser")
                        .authorities(new SimpleGrantedAuthority("ORGANIZATION_VIEW"))
                        .password("password")))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value(403));
  }

  @Test
  @DisplayName("成员不能激活组织应返回403")
  void member_cannotActivateOrganization_shouldReturn403() throws Exception {
    doThrow(new RuntimeException("无权操作"))
        .when(organizationService)
        .activateOrganization(eq(1L), eq(200L));

    mockMvc
        .perform(
            put("/api/v1/organizations/{id}/activate", 1L)
                .with(
                    user("mockUser")
                        .authorities(new SimpleGrantedAuthority("ORGANIZATION_VIEW"))
                        .password("password")))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value(403));
  }

  @Test
  @DisplayName("成员不能停用组织应返回403")
  void member_cannotDeactivateOrganization_shouldReturn403() throws Exception {
    doThrow(new RuntimeException("无权操作"))
        .when(organizationService)
        .deactivateOrganization(eq(1L), eq(200L));

    mockMvc
        .perform(
            put("/api/v1/organizations/{id}/deactivate", 1L)
                .with(
                    user("mockUser")
                        .authorities(new SimpleGrantedAuthority("ORGANIZATION_VIEW"))
                        .password("password")))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value(403));
  }

  @Test
  @DisplayName("成员不能更新订阅计划应返回403")
  void member_cannotUpdateSubscriptionPlan_shouldReturn403() throws Exception {
    String subscriptionJson =
        """
                {
                    "plan": "ENTERPRISE",
                    "expiresAt": "2025-12-31T23:59:59"
                }
                """;

    doThrow(new RuntimeException("无权操作"))
        .when(organizationService)
        .updateSubscriptionPlan(eq(1L), eq("ENTERPRISE"), any(LocalDateTime.class), eq(200L));

    mockMvc
        .perform(
            put("/api/v1/organizations/{id}/subscription", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(subscriptionJson)
                .with(
                    user("mockUser")
                        .authorities(new SimpleGrantedAuthority("ORGANIZATION_VIEW"))
                        .password("password")))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value(403));
  }

  // ==================== 共同权限测试 ====================

  @Test
  @DisplayName("管理员和成员都可以查看组织列表")
  void bothAdminAndMember_canListOrganizations() throws Exception {
    PageResult<Organization> pageResult = PageResult.of(Arrays.asList(organization), 1L, 1, 20);

    // 管理员查看
    when(organizationService.listOrganizations(
            eq(100L), eq(1), eq(20), nullable(String.class), nullable(Boolean.class)))
        .thenReturn(pageResult);

    mockMvc
        .perform(
            get("/api/v1/organizations")
                .with(
                    user("mockUser")
                        .authorities(new SimpleGrantedAuthority("ORGANIZATION_VIEW"))
                        .password("password")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200));

    // 成员查看
    when(organizationService.listOrganizations(
            eq(200L), eq(1), eq(20), nullable(String.class), nullable(Boolean.class)))
        .thenReturn(pageResult);

    mockMvc
        .perform(
            get("/api/v1/organizations")
                .with(
                    user("mockUser")
                        .authorities(new SimpleGrantedAuthority("ORGANIZATION_VIEW"))
                        .password("password")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200));
  }

  @Test
  @DisplayName("管理员和成员都可以查看组织详情")
  void bothAdminAndMember_canGetOrganizationDetails() throws Exception {
    // 管理员查看
    when(organizationService.getOrganizationById(eq(1L), eq(100L))).thenReturn(organization);

    mockMvc
        .perform(
            get("/api/v1/organizations/{id}", 1L)
                .with(
                    user("mockUser")
                        .authorities(new SimpleGrantedAuthority("ORGANIZATION_VIEW"))
                        .password("password")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200));

    // 成员查看
    when(organizationService.getOrganizationById(eq(1L), eq(200L))).thenReturn(organization);

    mockMvc
        .perform(
            get("/api/v1/organizations/{id}", 1L)
                .with(
                    user("mockUser")
                        .authorities(new SimpleGrantedAuthority("ORGANIZATION_VIEW"))
                        .password("password")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200));
  }

  @Test
  @DisplayName("管理员和成员都可以查看组织成员列表")
  void bothAdminAndMember_canListOrganizationMembers() throws Exception {
    OrganizationMemberDTO memberDTO =
        OrganizationMemberDTO.builder()
            .id(300L)
            .username("testmember")
            .email("testmember@test.com")
            .realName("测试成员")
            .build();

    PageResult<OrganizationMemberDTO> pageResult =
        PageResult.of(Arrays.asList(memberDTO), 1L, 1, 20);

    // 管理员查看
    when(organizationService.listOrganizationMembers(eq(1L), eq(100L), eq(1), eq(20)))
        .thenReturn(pageResult);

    mockMvc
        .perform(
            get("/api/v1/organizations/{id}/members", 1L)
                .param("page", "1")
                .param("pageSize", "20")
                .with(
                    user("mockUser")
                        .authorities(new SimpleGrantedAuthority("ORGANIZATION_VIEW"))
                        .password("password")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200));

    // 成员查看
    when(organizationService.listOrganizationMembers(eq(1L), eq(200L), eq(1), eq(20)))
        .thenReturn(pageResult);

    mockMvc
        .perform(
            get("/api/v1/organizations/{id}/members", 1L)
                .param("page", "1")
                .param("pageSize", "20")
                .with(
                    user("mockUser")
                        .authorities(new SimpleGrantedAuthority("ORGANIZATION_VIEW"))
                        .password("password")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200));
  }

  @Test
  @DisplayName("管理员和成员都可以查看组织设置")
  void bothAdminAndMember_canGetOrganizationSettings() throws Exception {
    OrganizationSettingsDTO settings =
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
                    .passwordMinLength(8)
                    .passwordRequireSpecialChar(true)
                    .sessionTimeoutMinutes(60)
                    .twoFactorAuthEnabled(false)
                    .build())
            .project(
                OrganizationSettingsDTO.ProjectSettings.builder()
                    .defaultVisibility("PRIVATE")
                    .allowPublicProjects(true)
                    .maxProjects(100)
                    .maxMembersPerProject(50)
                    .storageLimitMb(10240L)
                    .build())
            .build();

    // 管理员查看
    when(organizationService.getOrganizationSettings(eq(1L), eq(100L))).thenReturn(settings);

    mockMvc
        .perform(
            get("/api/v1/organizations/{id}/settings", 1L)
                .with(
                    user("mockUser")
                        .authorities(new SimpleGrantedAuthority("ORGANIZATION_VIEW"))
                        .password("password")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200));

    // 成员查看
    when(organizationService.getOrganizationSettings(eq(1L), eq(200L))).thenReturn(settings);

    mockMvc
        .perform(
            get("/api/v1/organizations/{id}/settings", 1L)
                .with(
                    user("mockUser")
                        .authorities(new SimpleGrantedAuthority("ORGANIZATION_VIEW"))
                        .password("password")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200));
  }

  // ==================== 权限边界测试 ====================

  @Test
  @DisplayName("管理员缺少ORGANIZATION_UPDATE权限不能执行管理操作")
  void admin_withoutUpdatePermission_cannotPerformAdminActions() throws Exception {
    // 即使是管理员，如果没有ORGANIZATION_UPDATE权限也不能执行管理操作
    when(organizationService.updateOrganizationSettings(
            eq(1L), any(OrganizationSettingsDTO.class), eq(100L)))
        .thenThrow(new RuntimeException("无权操作"));

    OrganizationSettingsDTO settings =
        OrganizationSettingsDTO.builder()
            .notification(
                OrganizationSettingsDTO.NotificationSettings.builder().emailEnabled(true).build())
            .build();

    mockMvc
        .perform(
            put("/api/v1/organizations/{id}/settings", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(settings))
                .with(
                    user("mockUser")
                        .authorities(new SimpleGrantedAuthority("ORGANIZATION_VIEW"))
                        .password("password"))) // 只有查看权限
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value(403));
  }

  @Test
  @DisplayName("成员缺少ORGANIZATION_VIEW权限不能查看组织信息")
  void member_withoutViewPermission_cannotViewOrganization() throws Exception {
    when(organizationService.getOrganizationById(eq(1L), eq(200L)))
        .thenThrow(new RuntimeException("无权访问"));

    mockMvc
        .perform(
            get("/api/v1/organizations/{id}", 1L)
                .with(user("mockUser").password("password"))) // 没有任何权限
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value(403));
  }
}
