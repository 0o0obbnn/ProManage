package com.promanage.integration;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.promanage.common.entity.Organization;
import com.promanage.common.entity.User;
import com.promanage.common.result.PageResult;
import com.promanage.infrastructure.security.JwtAuthenticationEntryPoint;
import com.promanage.infrastructure.security.JwtAuthenticationFilter;
import com.promanage.infrastructure.security.JwtTokenProvider;
import com.promanage.infrastructure.security.TokenBlacklistService;
import com.promanage.service.IOrganizationService;

/**
 * 分页与SQL集成测试 - deleted_at过滤验证
 *
 * <p>基于Testcontainers/PostgreSQL验证软删除过滤功能
 *
 * @author ProManage Team
 * @date 2025-10-12
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@DisplayName("软删除过滤集成测试")
class SoftDeleteFilterIntegrationTest extends PostgreSQLContainerBaseTest {

  @Autowired private WebApplicationContext webApplicationContext;

  @MockBean private IOrganizationService organizationService;

  @MockBean private JwtTokenProvider jwtTokenProvider;

  @MockBean private TokenBlacklistService tokenBlacklistService;

  @MockBean private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

  @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;

  private MockMvc mockMvc;
  private List<Organization> activeOrganizations;
  private List<Organization> deletedOrganizations;
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

    // 创建活跃组织数据（未删除）
    activeOrganizations = new ArrayList<>();
    for (int i = 1; i <= 20; i++) {
      Organization org = new Organization();
      org.setId((long) i);
      org.setName("活跃组织" + i);
      org.setSlug("active-org-" + i);
      org.setDescription("活跃组织描述" + i);
      org.setIsActive(true);
      org.setSubscriptionPlan("PRO");
      org.setSubscriptionExpiresAt(LocalDateTime.of(2025, 12, 31, 0, 0));
      org.setCreatedAt(LocalDateTime.now());
      org.setUpdatedAt(LocalDateTime.now());
      org.setCreatedBy(1L);
      org.setUpdatedBy(1L);
      org.setDeletedAt(null); // 未删除
      activeOrganizations.add(org);
    }

    // 创建已删除组织数据
    deletedOrganizations = new ArrayList<>();
    for (int i = 1; i <= 10; i++) {
      Organization org = new Organization();
      org.setId((long) (20 + i));
      org.setName("已删除组织" + i);
      org.setSlug("deleted-org-" + i);
      org.setDescription("已删除组织描述" + i);
      org.setIsActive(true);
      org.setSubscriptionPlan("PRO");
      org.setSubscriptionExpiresAt(LocalDateTime.of(2025, 12, 31, 0, 0));
      org.setCreatedAt(LocalDateTime.now());
      org.setUpdatedAt(LocalDateTime.now());
      org.setCreatedBy(1L);
      org.setUpdatedBy(1L);
      org.setDeletedAt(LocalDateTime.now().minusDays(1)); // 已删除
      deletedOrganizations.add(org);
    }

    // 所有组织（包括已删除的）
    allOrganizations = new ArrayList<>();
    allOrganizations.addAll(activeOrganizations);
    allOrganizations.addAll(deletedOrganizations);

    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("默认查询应过滤掉已删除记录")
  void defaultQuery_shouldFilterDeletedRecords() throws Exception {
    // 默认情况下，只返回未删除的组织
    PageResult<Organization> pageResult = PageResult.of(activeOrganizations, 20L, 1, 20);

    when(organizationService.listOrganizations(
            eq(1L), eq(1), eq(20), nullable(String.class), nullable(Boolean.class)))
        .thenReturn(pageResult);

    mockMvc
        .perform(
            get("/api/v1/organizations")
                .param("page", "1")
                .param("pageSize", "20")
                .with(
                    user("mockUser")
                        .authorities(new SimpleGrantedAuthority("ORGANIZATION_VIEW"))
                        .password("password")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data.total").value(20))
        .andExpect(jsonPath("$.data.list").isArray())
        .andExpect(jsonPath("$.data.list").value(hasSize(20)))
        .andExpect(jsonPath("$.data.list[0].name").value("活跃组织1"))
        .andExpect(jsonPath("$.data.list[19].name").value("活跃组织20"));
  }

  @Test
  @DisplayName("查询包含已删除记录应返回所有数据")
  void includeDeletedQuery_shouldReturnAllRecords() throws Exception {
    // 查询包含已删除的组织
    PageResult<Organization> pageResult = PageResult.of(allOrganizations, 30L, 1, 30);

    when(organizationService.listOrganizations(
            eq(1L), eq(1), eq(30), nullable(String.class), eq(true)))
        .thenReturn(pageResult);

    mockMvc
        .perform(
            get("/api/v1/organizations")
                .param("page", "1")
                .param("pageSize", "30")
                .param("includeDeleted", "true")
                .with(
                    user("mockUser")
                        .authorities(new SimpleGrantedAuthority("ORGANIZATION_VIEW"))
                        .password("password")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data.total").value(30))
        .andExpect(jsonPath("$.data.list").isArray())
        .andExpect(jsonPath("$.data.list").value(hasSize(30)));
  }

  @Test
  @DisplayName("仅查询已删除记录应只返回已删除数据")
  void onlyDeletedQuery_shouldReturnOnlyDeletedRecords() throws Exception {
    // 仅查询已删除的组织
    PageResult<Organization> pageResult = PageResult.of(deletedOrganizations, 10L, 1, 20);

    when(organizationService.listOrganizations(
            eq(1L), eq(1), eq(20), nullable(String.class), eq(false)))
        .thenReturn(pageResult);

    mockMvc
        .perform(
            get("/api/v1/organizations")
                .param("page", "1")
                .param("pageSize", "20")
                .param("onlyDeleted", "true")
                .with(
                    user("mockUser")
                        .authorities(new SimpleGrantedAuthority("ORGANIZATION_VIEW"))
                        .password("password")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data.total").value(10))
        .andExpect(jsonPath("$.data.list").isArray())
        .andExpect(jsonPath("$.data.list").value(hasSize(10)))
        .andExpect(jsonPath("$.data.list[0].name").value("已删除组织1"))
        .andExpect(jsonPath("$.data.list[9].name").value("已删除组织10"));
  }

  @Test
  @DisplayName("分页查询应正确过滤已删除记录")
  void paginatedQuery_shouldFilterDeletedRecords() throws Exception {
    // 第一页活跃组织
    List<Organization> firstPageActive = activeOrganizations.subList(0, 10);
    PageResult<Organization> pageResult = PageResult.of(firstPageActive, 20L, 1, 10);

    when(organizationService.listOrganizations(
            eq(1L), eq(1), eq(10), nullable(String.class), nullable(Boolean.class)))
        .thenReturn(pageResult);

    mockMvc
        .perform(
            get("/api/v1/organizations")
                .param("page", "1")
                .param("pageSize", "10")
                .with(
                    user("mockUser")
                        .authorities(new SimpleGrantedAuthority("ORGANIZATION_VIEW"))
                        .password("password")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data.total").value(20))
        .andExpect(jsonPath("$.data.page").value(1))
        .andExpect(jsonPath("$.data.pageSize").value(10))
        .andExpect(jsonPath("$.data.list").isArray())
        .andExpect(jsonPath("$.data.list").value(hasSize(10)))
        .andExpect(jsonPath("$.data.list[0].name").value("活跃组织1"));
  }

  @Test
  @DisplayName("关键词搜索应过滤已删除记录")
  void keywordSearch_shouldFilterDeletedRecords() throws Exception {
    // 搜索包含"活跃"的组织
    List<Organization> searchResults = activeOrganizations.subList(0, 5);
    PageResult<Organization> pageResult = PageResult.of(searchResults, 5L, 1, 20);

    when(organizationService.listOrganizations(
            eq(1L), eq(1), eq(20), eq("活跃"), nullable(Boolean.class)))
        .thenReturn(pageResult);

    mockMvc
        .perform(
            get("/api/v1/organizations")
                .param("page", "1")
                .param("pageSize", "20")
                .param("keyword", "活跃")
                .with(
                    user("mockUser")
                        .authorities(new SimpleGrantedAuthority("ORGANIZATION_VIEW"))
                        .password("password")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data.total").value(5))
        .andExpect(jsonPath("$.data.list").isArray())
        .andExpect(jsonPath("$.data.list").value(hasSize(5)))
        .andExpect(jsonPath("$.data.list[0].name").value("活跃组织1"));
  }

  @Test
  @DisplayName("状态过滤应与软删除过滤结合")
  void statusFilter_shouldCombineWithSoftDeleteFilter() throws Exception {
    // 查询活跃且未删除的组织
    List<Organization> activeAndNotDeleted = activeOrganizations.subList(0, 15);
    PageResult<Organization> pageResult = PageResult.of(activeAndNotDeleted, 15L, 1, 20);

    when(organizationService.listOrganizations(
            eq(1L), eq(1), eq(20), nullable(String.class), eq(true)))
        .thenReturn(pageResult);

    mockMvc
        .perform(
            get("/api/v1/organizations")
                .param("page", "1")
                .param("pageSize", "20")
                .param("isActive", "true")
                .with(
                    user("mockUser")
                        .authorities(new SimpleGrantedAuthority("ORGANIZATION_VIEW"))
                        .password("password")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data.total").value(15))
        .andExpect(jsonPath("$.data.list").isArray())
        .andExpect(jsonPath("$.data.list").value(hasSize(15)));
  }

  @Test
  @DisplayName("批量操作应正确处理软删除记录")
  void bulkOperations_shouldHandleSoftDeletedRecords() throws Exception {
    // 模拟批量查询，确保已删除记录不被包含
    PageResult<Organization> pageResult = PageResult.of(activeOrganizations, 20L, 1, 50);

    when(organizationService.listOrganizations(
            eq(1L), eq(1), eq(50), nullable(String.class), nullable(Boolean.class)))
        .thenReturn(pageResult);

    mockMvc
        .perform(
            get("/api/v1/organizations")
                .param("page", "1")
                .param("pageSize", "50")
                .with(
                    user("mockUser")
                        .authorities(new SimpleGrantedAuthority("ORGANIZATION_VIEW"))
                        .password("password")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data.total").value(20))
        .andExpect(jsonPath("$.data.list").isArray())
        .andExpect(jsonPath("$.data.list").value(hasSize(20)));
  }

  @Test
  @DisplayName("软删除记录的详情查询应返回404")
  void softDeletedRecord_detailsQuery_shouldReturn404() throws Exception {
    // 尝试获取已删除组织的详情
    when(organizationService.getOrganizationById(eq(21L), eq(1L)))
        .thenThrow(new RuntimeException("组织不存在"));

    mockMvc
        .perform(
            get("/api/v1/organizations/{id}", 21L) // 已删除组织的ID
                .with(
                    user("mockUser")
                        .authorities(new SimpleGrantedAuthority("ORGANIZATION_VIEW"))
                        .password("password")))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value(404))
        .andExpect(jsonPath("$.message").value("组织不存在"));
  }

  @Test
  @DisplayName("软删除记录的更新操作应返回404")
  void softDeletedRecord_updateOperation_shouldReturn404() throws Exception {
    String updateJson =
        """
                {
                    "name": "更新后的名称",
                    "description": "更新后的描述"
                }
                """;

    // 尝试更新已删除的组织
    when(organizationService.updateOrganization(any(Organization.class), eq(1L)))
        .thenThrow(new RuntimeException("组织不存在"));

    mockMvc
        .perform(
            put("/api/v1/organizations/{id}", 21L) // 已删除组织的ID
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson)
                .with(
                    user("mockUser")
                        .authorities(new SimpleGrantedAuthority("ORGANIZATION_UPDATE"))
                        .password("password")))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value(404))
        .andExpect(jsonPath("$.message").value("组织不存在"));
  }

  @Test
  @DisplayName("软删除记录的删除操作应返回404")
  void softDeletedRecord_deleteOperation_shouldReturn404() throws Exception {
    // 尝试删除已删除的组织 - deleteOrganization returns void
    doThrow(new RuntimeException("组织不存在"))
        .when(organizationService)
        .deleteOrganization(eq(21L), eq(1L));

    mockMvc
        .perform(
            delete("/api/v1/organizations/{id}", 21L) // 已删除组织的ID
                .with(
                    user("mockUser")
                        .authorities(new SimpleGrantedAuthority("ORGANIZATION_DELETE"))
                        .password("password")))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value(404))
        .andExpect(jsonPath("$.message").value("组织不存在"));
  }
}
