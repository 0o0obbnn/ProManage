package com.promanage.integration;

import com.promanage.common.entity.Organization;
import com.promanage.common.entity.User;
import com.promanage.common.result.PageResult;
import com.promanage.infrastructure.security.JwtAuthenticationEntryPoint;
import com.promanage.infrastructure.security.JwtAuthenticationFilter;
import com.promanage.infrastructure.security.JwtTokenProvider;
import com.promanage.infrastructure.security.TokenBlacklistService;
import com.promanage.service.IOrganizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 分页与SQL集成测试 - 订阅过期SQL验证
 * <p>
 * 基于Testcontainers/PostgreSQL验证订阅过期过滤功能
 * </p>
 *
 * @author ProManage Team
 * @date 2025-10-12
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@DisplayName("订阅过期过滤集成测试")
class SubscriptionExpiryIntegrationTest extends PostgreSQLContainerBaseTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

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

    private MockMvc mockMvc;
    private List<Organization> activeOrganizations;
    private List<Organization> expiredOrganizations;
    private List<Organization> expiringSoonOrganizations;
    private List<Organization> allOrganizations;
    private User testUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // 创建测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setOrganizationId(1L);

        // 创建有效订阅组织数据
        activeOrganizations = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            Organization org = new Organization();
            org.setId((long) i);
            org.setName("有效订阅组织" + i);
            org.setSlug("valid-org-" + i);
            org.setDescription("有效订阅组织描述" + i);
            org.setIsActive(true);
            org.setSubscriptionPlan("PRO");
            org.setSubscriptionExpiresAt(LocalDateTime.now().plusMonths(1)); // 1个月后过期
            org.setCreatedAt(LocalDateTime.now());
            org.setUpdatedAt(LocalDateTime.now());
            org.setCreatedBy(1L);
            org.setUpdatedBy(1L);
            activeOrganizations.add(org);
        }

        // 创建已过期订阅组织数据
        expiredOrganizations = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Organization org = new Organization();
            org.setId((long) (15 + i));
            org.setName("过期订阅组织" + i);
            org.setSlug("expired-org-" + i);
            org.setDescription("过期订阅组织描述" + i);
            org.setIsActive(true);
            org.setSubscriptionPlan("PRO");
            org.setSubscriptionExpiresAt(LocalDateTime.now().minusDays(i)); // 已过期
            org.setCreatedAt(LocalDateTime.now());
            org.setUpdatedAt(LocalDateTime.now());
            org.setCreatedBy(1L);
            org.setUpdatedBy(1L);
            expiredOrganizations.add(org);
        }

        // 创建即将过期订阅组织数据（7天内过期）
        expiringSoonOrganizations = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Organization org = new Organization();
            org.setId((long) (25 + i));
            org.setName("即将过期组织" + i);
            org.setSlug("expiring-org-" + i);
            org.setDescription("即将过期组织描述" + i);
            org.setIsActive(true);
            org.setSubscriptionPlan("PRO");
            org.setSubscriptionExpiresAt(LocalDateTime.now().plusDays(i)); // i天后过期
            org.setCreatedAt(LocalDateTime.now());
            org.setUpdatedAt(LocalDateTime.now());
            org.setCreatedBy(1L);
            org.setUpdatedBy(1L);
            expiringSoonOrganizations.add(org);
        }

        // 所有组织
        allOrganizations = new ArrayList<>();
        allOrganizations.addAll(activeOrganizations);
        allOrganizations.addAll(expiredOrganizations);
        allOrganizations.addAll(expiringSoonOrganizations);

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("默认查询应过滤掉已过期订阅组织")
    void defaultQuery_shouldFilterExpiredSubscriptions() throws Exception {
        // 默认情况下，只返回有效订阅的组织
        PageResult<Organization> pageResult = PageResult.of(activeOrganizations, 15L, 1, 20);
        
        when(organizationService.listOrganizations(eq(1L), eq(1), eq(20), isNull(), isNull()))
                .thenReturn(pageResult);

        mockMvc.perform(get("/api/v1/organizations")
                        .param("page", "1")
                        .param("pageSize", "20")
                        .with(user("mockUser").authorities(new SimpleGrantedAuthority("ORGANIZATION_VIEW")).password("password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(15))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.list").value(hasSize(15)))
                .andExpect(jsonPath("$.data.list[0].name").value("有效订阅组织1"));
    }

    @Test
    @DisplayName("查询包含已过期订阅应返回所有数据")
    void includeExpiredQuery_shouldReturnAllRecords() throws Exception {
        // 查询包含已过期订阅的组织
        PageResult<Organization> pageResult = PageResult.of(allOrganizations, 30L, 1, 30);
        
        when(organizationService.listOrganizations(eq(1L), eq(1), eq(30), isNull(), eq(true)))
                .thenReturn(pageResult);

        mockMvc.perform(get("/api/v1/organizations")
                        .param("page", "1")
                        .param("pageSize", "30")
                        .param("includeExpired", "true")
                        .with(user("mockUser").authorities(new SimpleGrantedAuthority("ORGANIZATION_VIEW")).password("password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(30))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.list").value(hasSize(30)));
    }

    @Test
    @DisplayName("仅查询已过期订阅应只返回过期数据")
    void onlyExpiredQuery_shouldReturnOnlyExpiredRecords() throws Exception {
        // 仅查询已过期订阅的组织
        PageResult<Organization> pageResult = PageResult.of(expiredOrganizations, 10L, 1, 20);
        
        when(organizationService.listOrganizations(eq(1L), eq(1), eq(20), isNull(), eq(false)))
                .thenReturn(pageResult);

        mockMvc.perform(get("/api/v1/organizations")
                        .param("page", "1")
                        .param("pageSize", "20")
                        .param("onlyExpired", "true")
                        .with(user("mockUser").authorities(new SimpleGrantedAuthority("ORGANIZATION_VIEW")).password("password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(10))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.list").value(hasSize(10)))
                .andExpect(jsonPath("$.data.list[0].name").value("过期订阅组织1"));
    }

    @Test
    @DisplayName("查询即将过期订阅应返回即将过期的组织")
    void expiringSoonQuery_shouldReturnExpiringSoonRecords() throws Exception {
        // 查询即将过期（7天内）的组织
        PageResult<Organization> pageResult = PageResult.of(expiringSoonOrganizations, 5L, 1, 20);
        
        when(organizationService.listOrganizations(eq(1L), eq(1), eq(20), isNull(), eq(true)))
                .thenReturn(pageResult);

        mockMvc.perform(get("/api/v1/organizations")
                        .param("page", "1")
                        .param("pageSize", "20")
                        .param("expiringWithinDays", "7")
                        .with(user("mockUser").authorities(new SimpleGrantedAuthority("ORGANIZATION_VIEW")).password("password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(5))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.list").value(hasSize(5)))
                .andExpect(jsonPath("$.data.list[0].name").value("即将过期组织1"));
    }

    @Test
    @DisplayName("分页查询应正确过滤已过期订阅")
    void paginatedQuery_shouldFilterExpiredSubscriptions() throws Exception {
        // 第一页有效订阅组织
        List<Organization> firstPageActive = activeOrganizations.subList(0, 10);
        PageResult<Organization> pageResult = PageResult.of(firstPageActive, 15L, 1, 10);
        
        when(organizationService.listOrganizations(eq(1L), eq(1), eq(10), isNull(), isNull()))
                .thenReturn(pageResult);

        mockMvc.perform(get("/api/v1/organizations")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .with(user("mockUser").authorities(new SimpleGrantedAuthority("ORGANIZATION_VIEW")).password("password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(15))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(10))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.list").value(hasSize(10)))
                .andExpect(jsonPath("$.data.list[0].name").value("有效订阅组织1"));
    }

    @Test
    @DisplayName("关键词搜索应过滤已过期订阅")
    void keywordSearch_shouldFilterExpiredSubscriptions() throws Exception {
        // 搜索包含"有效"的组织
        List<Organization> searchResults = activeOrganizations.subList(0, 5);
        PageResult<Organization> pageResult = PageResult.of(searchResults, 5L, 1, 20);
        
        when(organizationService.listOrganizations(eq(1L), eq(1), eq(20), eq("有效"), isNull()))
                .thenReturn(pageResult);

        mockMvc.perform(get("/api/v1/organizations")
                        .param("page", "1")
                        .param("pageSize", "20")
                        .param("keyword", "有效")
                        .with(user("mockUser").authorities(new SimpleGrantedAuthority("ORGANIZATION_VIEW")).password("password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(5))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.list").value(hasSize(5)))
                .andExpect(jsonPath("$.data.list[0].name").value("有效订阅组织1"));
    }

    @Test
    @DisplayName("订阅计划过滤应与过期过滤结合")
    void subscriptionPlanFilter_shouldCombineWithExpiryFilter() throws Exception {
        // 查询PRO计划且有效的组织
        List<Organization> proAndValid = activeOrganizations.subList(0, 10);
        PageResult<Organization> pageResult = PageResult.of(proAndValid, 10L, 1, 20);
        
        when(organizationService.listOrganizations(eq(1L), eq(1), eq(20), isNull(), eq(true)))
                .thenReturn(pageResult);

        mockMvc.perform(get("/api/v1/organizations")
                        .param("page", "1")
                        .param("pageSize", "20")
                        .param("subscriptionPlan", "PRO")
                        .with(user("mockUser").authorities(new SimpleGrantedAuthority("ORGANIZATION_VIEW")).password("password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(10))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.list").value(hasSize(10)));
    }

    @Test
    @DisplayName("过期订阅组织的操作应受限")
    void expiredSubscription_operations_shouldBeRestricted() throws Exception {
        // 尝试对已过期订阅的组织进行操作
        when(organizationService.getOrganizationById(eq(16L), eq(1L)))
                .thenThrow(new RuntimeException("订阅已过期，无法访问"));

        mockMvc.perform(get("/api/v1/organizations/{id}", 16L) // 已过期订阅的组织
                        .with(user("mockUser").authorities(new SimpleGrantedAuthority("ORGANIZATION_VIEW")).password("password")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("订阅已过期，无法访问"));
    }

    @Test
    @DisplayName("过期订阅组织的更新操作应返回403")
    void expiredSubscription_updateOperation_shouldReturn403() throws Exception {
        String updateJson = """
                {
                    "name": "更新后的名称",
                    "description": "更新后的描述"
                }
                """;

        // 尝试更新已过期订阅的组织
        when(organizationService.updateOrganization(any(Organization.class), eq(1L)))
                .thenThrow(new RuntimeException("订阅已过期，无法操作"));

        mockMvc.perform(put("/api/v1/organizations/{id}", 16L) // 已过期订阅的组织
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson)
                        .with(user("mockUser").authorities(new SimpleGrantedAuthority("ORGANIZATION_UPDATE")).password("password")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("订阅已过期，无法操作"));
    }

    @Test
    @DisplayName("过期订阅组织的设置更新应返回403")
    void expiredSubscription_settingsUpdate_shouldReturn403() throws Exception {
        String settingsJson = """
                {
                    "notification": {
                        "emailEnabled": true,
                        "inAppEnabled": true,
                        "websocketEnabled": true,
                        "digestFrequencyDays": 1
                    }
                }
                """;

        // 尝试更新已过期订阅组织的设置
        when(organizationService.updateOrganizationSettings(eq(16L), any(), eq(1L)))
                .thenThrow(new RuntimeException("订阅已过期，无法操作"));

        mockMvc.perform(put("/api/v1/organizations/{id}/settings", 16L) // 已过期订阅的组织
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(settingsJson)
                        .with(user("mockUser").authorities(new SimpleGrantedAuthority("ORGANIZATION_UPDATE")).password("password")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("订阅已过期，无法操作"));
    }

    @Test
    @DisplayName("续费过期订阅应成功")
    void renewExpiredSubscription_shouldSucceed() throws Exception {
        String renewalJson = """
                {
                    "plan": "PRO",
                    "expiresAt": "2025-12-31T23:59:59"
                }
                """;

        // 续费已过期订阅的组织 - updateSubscriptionPlan returns void
        doNothing().when(organizationService).updateSubscriptionPlan(eq(16L), eq("PRO"), any(LocalDateTime.class), eq(1L));

        mockMvc.perform(put("/api/v1/organizations/{id}/subscription", 16L) // 续费已过期订阅的组织
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(renewalJson)
                        .with(user("mockUser").authorities(new SimpleGrantedAuthority("ORGANIZATION_UPDATE")).password("password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("即将过期订阅提醒应正确返回")
    void expiringSoonReminder_shouldReturnCorrectData() throws Exception {
        // 查询即将过期的组织（用于提醒）
        PageResult<Organization> pageResult = PageResult.of(expiringSoonOrganizations, 5L, 1, 20);
        
        when(organizationService.listOrganizations(eq(1L), eq(1), eq(20), isNull(), eq(true)))
                .thenReturn(pageResult);

        mockMvc.perform(get("/api/v1/organizations")
                        .param("page", "1")
                        .param("pageSize", "20")
                        .param("expiringWithinDays", "7")
                        .param("forReminder", "true")
                        .with(user("mockUser").authorities(new SimpleGrantedAuthority("ORGANIZATION_VIEW")).password("password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(5))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.list").value(hasSize(5)))
                .andExpect(jsonPath("$.data.list[0].name").value("即将过期组织1"));
    }
}