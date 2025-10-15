package com.promanage.api.controller;

import com.promanage.common.result.PageResult;
import com.promanage.common.entity.Organization;
import com.promanage.common.entity.User;
import com.promanage.dto.OrganizationMemberDTO;
import com.promanage.infrastructure.security.JwtAuthenticationEntryPoint;
import com.promanage.infrastructure.security.JwtAuthenticationFilter;
import com.promanage.infrastructure.security.JwtTokenProvider;
import com.promanage.infrastructure.security.TokenBlacklistService;
import com.promanage.service.IOrganizationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 安全/租户回归测试套件 - 跨租户访问403测试
 * <p>
 * 测试场景：用户尝试访问不属于其组织的资源，应返回403 Forbidden
 * </p>
 *
 * @author ProManage Team
 * @date 2025-10-12
 */
@WebMvcTest(OrganizationController.class)
@DisplayName("跨租户访问403回归测试")
class CrossTenantAccess403Test {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IOrganizationService organizationService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    @MockitoBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private User userFromOrgA;
    private User userFromOrgB;
    private Organization orgA;
    private Organization orgB;

    @BeforeEach
    void setUp() {
        // 设置组织A的用户
        userFromOrgA = new User();
        userFromOrgA.setId(100L);
        userFromOrgA.setUsername("userA");
        userFromOrgA.setEmail("userA@orgA.com");
        userFromOrgA.setOrganizationId(1L); // 属于组织A

        // 设置组织B的用户
        userFromOrgB = new User();
        userFromOrgB.setId(200L);
        userFromOrgB.setUsername("userB");
        userFromOrgB.setEmail("userB@orgB.com");
        userFromOrgB.setOrganizationId(2L); // 属于组织B

        // 设置组织A
        orgA = new Organization();
        orgA.setId(1L);
        orgA.setName("组织A");
        orgA.setSlug("org-a");
        orgA.setIsActive(true);
        orgA.setSubscriptionPlan("PRO");
        orgA.setSubscriptionExpiresAt(LocalDateTime.of(2025, 12, 31, 0, 0));

        // 设置组织B
        orgB = new Organization();
        orgB.setId(2L);
        orgB.setName("组织B");
        orgB.setSlug("org-b");
        orgB.setIsActive(true);
        orgB.setSubscriptionPlan("PRO");
        orgB.setSubscriptionExpiresAt(LocalDateTime.of(2025, 12, 31, 0, 0));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("用户访问不属于其组织的组织详情应返回403")
    void getOrganization_crossTenant_shouldReturn403() throws Exception {
        // 模拟用户尝试访问组织B的详情，但用户属于组织A
        when(organizationService.getOrganizationById(eq(2L), eq(100L)))
                .thenThrow(new RuntimeException("无权访问"));

        mockMvc.perform(get("/api/v1/organizations/{id}", 2L) // 访问组织B
                        .with(user("mockUser").authorities(new SimpleGrantedAuthority("ORGANIZATION_VIEW"))
                                .password("password"))) // 用户属于组织A
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("用户尝试获取其他组织的成员列表应返回403")
    void getOrganizationMembers_crossTenant_shouldReturn403() throws Exception {
        // 模拟用户尝试获取组织B的成员列表，但用户属于组织A
        when(organizationService.listOrganizationMembers(eq(2L), eq(100L), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("无权访问"));

        mockMvc.perform(get("/api/v1/organizations/{id}/members", 2L) // 获取组织B的成员
                        .param("page", "1")
                        .param("pageSize", "20")
                        .with(user("mockUser").authorities(new SimpleGrantedAuthority("ORGANIZATION_VIEW"))
                                .password("password"))) // 用户属于组织A
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("用户尝试更新其他组织的设置应返回403")
    void updateOrganizationSettings_crossTenant_shouldReturn403() throws Exception {
        String settingsJson = """
                {
                    "notification": {
                        "emailEnabled": true,
                        "inAppEnabled": true,
                        "websocketEnabled": true,
                        "digestFrequencyDays": 1
                    },
                    "security": {
                        "passwordMinLength": 8,
                        "passwordRequireSpecialChar": true,
                        "sessionTimeoutMinutes": 60,
                        "twoFactorAuthEnabled": false
                    },
                    "project": {
                        "defaultVisibility": "PRIVATE",
                        "allowPublicProjects": true,
                        "maxProjects": 100,
                        "maxMembersPerProject": 50,
                        "storageLimitMb": 10240
                    }
                }
                """;

        // 模拟用户尝试更新组织B的设置，但用户属于组织A
        when(organizationService.updateOrganizationSettings(eq(2L), any(), eq(100L)))
                .thenThrow(new RuntimeException("无权操作"));

        mockMvc.perform(put("/api/v1/organizations/{id}/settings", 2L) // 更新组织B设置
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(settingsJson)
                        .with(user("mockUser").authorities(new SimpleGrantedAuthority("ORGANIZATION_UPDATE"))
                                .password("password"))) // 用户属于组织A
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("用户尝试停用其他组织应返回403")
    void deactivateOrganization_crossTenant_shouldReturn403() throws Exception {
        // 模拟用户尝试停用组织B，但用户属于组织A
        doThrow(new RuntimeException("无权操作"))
                .when(organizationService).deactivateOrganization(eq(2L), eq(100L));

        mockMvc.perform(put("/api/v1/organizations/{id}/deactivate", 2L) // 停用组织B
                        .with(user("mockUser").authorities(new SimpleGrantedAuthority("ORGANIZATION_UPDATE"))
                                .password("password"))) // 用户属于组织A
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("用户尝试激活其他组织应返回403")
    void activateOrganization_crossTenant_shouldReturn403() throws Exception {
        // 模拟用户尝试激活组织B，但用户属于组织A
        doThrow(new RuntimeException("无权操作"))
                .when(organizationService).activateOrganization(eq(2L), eq(100L));

        mockMvc.perform(put("/api/v1/organizations/{id}/activate", 2L) // 激活组织B
                        .with(user("mockUser").authorities(new SimpleGrantedAuthority("ORGANIZATION_UPDATE"))
                                .password("password"))) // 用户属于组织A
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("用户尝试更新其他组织的订阅计划应返回403")
    void updateSubscriptionPlan_crossTenant_shouldReturn403() throws Exception {
        String subscriptionJson = """
                {
                    "plan": "ENTERPRISE",
                    "expiresAt": "2025-12-31T23:59:59"
                }
                """;

        // 模拟用户尝试更新组织B的订阅计划，但用户属于组织A
        doThrow(new RuntimeException("无权操作"))
                .when(organizationService).updateSubscriptionPlan(eq(2L), anyString(), any(), eq(100L));

        mockMvc.perform(put("/api/v1/organizations/{id}/subscription", 2L) // 更新组织B订阅
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(subscriptionJson)
                        .with(user("mockUser").authorities(new SimpleGrantedAuthority("ORGANIZATION_UPDATE"))
                                .password("password"))) // 用户属于组织A
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("用户尝试删除其他组织应返回403")
    void deleteOrganization_crossTenant_shouldReturn403() throws Exception {
        // 模拟用户尝试删除组织B，但用户属于组织A
        doThrow(new RuntimeException("无权操作"))
                .when(organizationService).deleteOrganization(eq(2L), eq(100L));

        mockMvc.perform(put("/api/v1/organizations/{id}/delete", 2L) // 删除组织B
                        .with(user("mockUser").authorities(new SimpleGrantedAuthority("ORGANIZATION_DELETE"))
                                .password("password"))) // 用户属于组织A
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("服务层正确拒绝跨租户访问")
    void serviceLayer_crossTenant_shouldThrowException() throws Exception {
        // 验证服务层正确拒绝跨租户访问
        // 用户属于组织A，尝试访问组织B的成员列表
        when(organizationService.listOrganizationMembers(eq(2L), eq(100L), eq(1), eq(20)))
                .thenThrow(new RuntimeException("无权访问"));

        mockMvc.perform(get("/api/v1/organizations/{id}/members", 2L)
                        .param("page", "1")
                        .param("pageSize", "20")
                        .with(user("mockUser").authorities(new SimpleGrantedAuthority("ORGANIZATION_VIEW"))
                                .password("password")))
                .andExpect(status().isForbidden());
    }
}