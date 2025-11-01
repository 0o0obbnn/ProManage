package com.promanage.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.promanage.infrastructure.security.JwtAuthenticationEntryPoint;
import com.promanage.infrastructure.security.JwtAuthenticationFilter;
import com.promanage.infrastructure.security.JwtTokenProvider;
import com.promanage.infrastructure.security.TokenBlacklistService;
import com.promanage.service.IOrganizationService;

/**
 * 安全/租户回归测试套件 - 未登录401测试
 *
 * <p>测试场景：未携带认证信息的请求访问受保护资源，应返回401 Unauthorized
 *
 * @author ProManage Team
 * @date 2025-10-12
 */
@WebMvcTest(OrganizationController.class)
@DisplayName("未登录401回归测试")
class UnauthorizedAccess401Test {

  @Autowired private MockMvc mockMvc;

  @MockBean private IOrganizationService organizationService;

  @MockBean private JwtTokenProvider jwtTokenProvider;

  @MockBean private TokenBlacklistService tokenBlacklistService;

  @MockBean private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

  @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;

  @BeforeEach
  void setUp() {
    // 确保安全上下文为空
    SecurityContextHolder.clearContext();
  }

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("未登录访问组织列表应返回401")
  void listOrganizations_unauthorized_shouldReturn401() throws Exception {
    mockMvc
        .perform(get("/api/v1/organizations").param("page", "1").param("pageSize", "20"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value(401))
        .andExpect(jsonPath("$.message").exists());
  }

  @Test
  @DisplayName("未登录获取组织详情应返回401")
  void getOrganization_unauthorized_shouldReturn401() throws Exception {
    mockMvc
        .perform(get("/api/v1/organizations/{id}", 1L))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value(401));
  }

  @Test
  @DisplayName("未登录获取组织成员列表应返回401")
  void getOrganizationMembers_unauthorized_shouldReturn401() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/organizations/{id}/members", 1L)
                .param("page", "1")
                .param("pageSize", "20"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value(401));
  }

  @Test
  @DisplayName("未登录创建组织应返回401")
  void createOrganization_unauthorized_shouldReturn401() throws Exception {
    String orgJson =
        """
                {
                    "name": "新组织",
                    "slug": "new-org",
                    "description": "新组织描述"
                }
                """;

    mockMvc
        .perform(
            post("/api/v1/organizations").contentType(MediaType.APPLICATION_JSON).content(orgJson))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value(401));
  }

  @Test
  @DisplayName("未登录更新组织应返回401")
  void updateOrganization_unauthorized_shouldReturn401() throws Exception {
    String orgJson =
        """
                {
                    "name": "更新后的组织",
                    "description": "更新后的描述"
                }
                """;

    mockMvc
        .perform(
            put("/api/v1/organizations/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(orgJson))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value(401));
  }

  @Test
  @DisplayName("未登录删除组织应返回401")
  void deleteOrganization_unauthorized_shouldReturn401() throws Exception {
    mockMvc
        .perform(delete("/api/v1/organizations/{id}", 1L))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value(401));
  }

  @Test
  @DisplayName("未登录获取组织设置应返回401")
  void getOrganizationSettings_unauthorized_shouldReturn401() throws Exception {
    mockMvc
        .perform(get("/api/v1/organizations/{id}/settings", 1L))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value(401));
  }

  @Test
  @DisplayName("未登录更新组织设置应返回401")
  void updateOrganizationSettings_unauthorized_shouldReturn401() throws Exception {
    String settingsJson =
        """
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

    mockMvc
        .perform(
            put("/api/v1/organizations/{id}/settings", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(settingsJson))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value(401));
  }

  @Test
  @DisplayName("未登录激活组织应返回401")
  void activateOrganization_unauthorized_shouldReturn401() throws Exception {
    mockMvc
        .perform(put("/api/v1/organizations/{id}/activate", 1L))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value(401));
  }

  @Test
  @DisplayName("未登录停用组织应返回401")
  void deactivateOrganization_unauthorized_shouldReturn401() throws Exception {
    mockMvc
        .perform(put("/api/v1/organizations/{id}/deactivate", 1L))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value(401));
  }

  @Test
  @DisplayName("未登录更新订阅计划应返回401")
  void updateSubscriptionPlan_unauthorized_shouldReturn401() throws Exception {
    String subscriptionJson =
        """
                {
                    "plan": "PRO",
                    "expiresAt": "2025-12-31T23:59:59"
                }
                """;

    mockMvc
        .perform(
            put("/api/v1/organizations/{id}/subscription", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(subscriptionJson))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value(401));
  }

  @Test
  @DisplayName("无效Token访问应返回401")
  void invalidToken_shouldReturn401() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/organizations")
                .header("Authorization", "Bearer invalid-token")
                .param("page", "1")
                .param("pageSize", "20"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value(401));
  }

  @Test
  @DisplayName("过期Token访问应返回401")
  void expiredToken_shouldReturn401() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/organizations")
                .header("Authorization", "Bearer expired-token")
                .param("page", "1")
                .param("pageSize", "20"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value(401));
  }

  @Test
  @DisplayName("黑名单Token访问应返回401")
  void blacklistedToken_shouldReturn401() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/organizations")
                .header("Authorization", "Bearer blacklisted-token")
                .param("page", "1")
                .param("pageSize", "20"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value(401));
  }

  @Test
  @DisplayName("无Authorization头访问应返回401")
  void noAuthorizationHeader_shouldReturn401() throws Exception {
    mockMvc
        .perform(get("/api/v1/organizations").param("page", "1").param("pageSize", "20"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value(401));
  }

  @Test
  @DisplayName("格式错误的Authorization头应返回401")
  void malformedAuthorizationHeader_shouldReturn401() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/organizations")
                .header("Authorization", "InvalidFormat token")
                .param("page", "1")
                .param("pageSize", "20"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value(401));
  }

  @Test
  @DisplayName("空Token访问应返回401")
  void emptyToken_shouldReturn401() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/organizations")
                .header("Authorization", "Bearer ")
                .param("page", "1")
                .param("pageSize", "20"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value(401));
  }
}
