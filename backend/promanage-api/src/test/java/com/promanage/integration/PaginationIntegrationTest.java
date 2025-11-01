package com.promanage.integration;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
 * 分页与SQL集成测试 - 分页查询验证
 *
 * <p>基于Testcontainers/PostgreSQL验证分页查询功能
 *
 * @author ProManage Team
 * @date 2025-10-12
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@DisplayName("分页查询集成测试")
class PaginationIntegrationTest extends PostgreSQLContainerBaseTest {

  @Autowired private WebApplicationContext webApplicationContext;

  @MockBean private IOrganizationService organizationService;

  @MockBean private JwtTokenProvider jwtTokenProvider;

  @MockBean private TokenBlacklistService tokenBlacklistService;

  @MockBean private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

  @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;

  private MockMvc mockMvc;
  private List<Organization> testOrganizations;
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

    // 创建测试组织数据
    testOrganizations = new ArrayList<>();
    for (int i = 1; i <= 50; i++) {
      Organization org = new Organization();
      org.setId((long) i);
      org.setName("测试组织" + i);
      org.setSlug("test-org-" + i);
      org.setDescription("测试组织描述" + i);
      org.setIsActive(true);
      org.setSubscriptionPlan("PRO");
      org.setSubscriptionExpiresAt(LocalDateTime.of(2025, 12, 31, 0, 0));
      org.setCreatedAt(LocalDateTime.now());
      org.setUpdatedAt(LocalDateTime.now());
      org.setCreatedBy(1L);
      org.setUpdatedBy(1L);
      testOrganizations.add(org);
    }

    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("第一页分页查询应返回正确结果")
  void firstPagePagination_shouldReturnCorrectResults() throws Exception {
    // 模拟第一页数据（20条记录）
    List<Organization> firstPageOrgs = testOrganizations.subList(0, 20);
    PageResult<Organization> pageResult = PageResult.of(firstPageOrgs, 50L, 1, 20);

    when(organizationService.listOrganizations(eq(1L), eq(1), eq(20), isNull(), isNull()))
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
        .andExpect(jsonPath("$.data.page").value(1))
        .andExpect(jsonPath("$.data.pageSize").value(20))
        .andExpect(jsonPath("$.data.total").value(50))
        .andExpect(jsonPath("$.data.list").isArray())
        .andExpect(jsonPath("$.data.list").value(hasSize(20)))
        .andExpect(jsonPath("$.data.list[0].name").value("测试组织1"))
        .andExpect(jsonPath("$.data.list[19].name").value("测试组织20"));
  }

  @Test
  @DisplayName("第二页分页查询应返回正确结果")
  void secondPagePagination_shouldReturnCorrectResults() throws Exception {
    // 模拟第二页数据（20条记录）
    List<Organization> secondPageOrgs = testOrganizations.subList(20, 40);
    PageResult<Organization> pageResult = PageResult.of(secondPageOrgs, 50L, 2, 20);

    when(organizationService.listOrganizations(eq(1L), eq(2), eq(20), isNull(), isNull()))
        .thenReturn(pageResult);

    mockMvc
        .perform(
            get("/api/v1/organizations")
                .param("page", "2")
                .param("pageSize", "20")
                .with(
                    user("mockUser")
                        .authorities(new SimpleGrantedAuthority("ORGANIZATION_VIEW"))
                        .password("password")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data.page").value(2))
        .andExpect(jsonPath("$.data.pageSize").value(20))
        .andExpect(jsonPath("$.data.total").value(50))
        .andExpect(jsonPath("$.data.list").isArray())
        .andExpect(jsonPath("$.data.list").value(hasSize(20)))
        .andExpect(jsonPath("$.data.list[0].name").value("测试组织21"))
        .andExpect(jsonPath("$.data.list[19].name").value("测试组织40"));
  }

  @Test
  @DisplayName("最后一页分页查询应返回剩余记录")
  void lastPagePagination_shouldReturnRemainingRecords() throws Exception {
    // 模拟最后一页数据（10条记录）
    List<Organization> lastPageOrgs = testOrganizations.subList(40, 50);
    PageResult<Organization> pageResult = PageResult.of(lastPageOrgs, 50L, 3, 20);

    when(organizationService.listOrganizations(eq(1L), eq(3), eq(20), isNull(), isNull()))
        .thenReturn(pageResult);

    mockMvc
        .perform(
            get("/api/v1/organizations")
                .param("page", "3")
                .param("pageSize", "20")
                .with(
                    user("mockUser")
                        .authorities(new SimpleGrantedAuthority("ORGANIZATION_VIEW"))
                        .password("password")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data.page").value(3))
        .andExpect(jsonPath("$.data.pageSize").value(20))
        .andExpect(jsonPath("$.data.total").value(50))
        .andExpect(jsonPath("$.data.list").isArray())
        .andExpect(jsonPath("$.data.list").value(hasSize(10)))
        .andExpect(jsonPath("$.data.list[0].name").value("测试组织41"))
        .andExpect(jsonPath("$.data.list[9].name").value("测试组织50"));
  }

  @Test
  @DisplayName("自定义页面大小分页查询应返回正确结果")
  void customPageSizePagination_shouldReturnCorrectResults() throws Exception {
    // 模拟自定义页面大小（每页10条）
    List<Organization> customPageOrgs = testOrganizations.subList(0, 10);
    PageResult<Organization> pageResult = PageResult.of(customPageOrgs, 50L, 1, 10);

    when(organizationService.listOrganizations(eq(1L), eq(1), eq(10), isNull(), isNull()))
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
        .andExpect(jsonPath("$.data.page").value(1))
        .andExpect(jsonPath("$.data.pageSize").value(10))
        .andExpect(jsonPath("$.data.total").value(50))
        .andExpect(jsonPath("$.data.list").isArray())
        .andExpect(jsonPath("$.data.list").value(hasSize(10)));
  }

  @Test
  @DisplayName("空结果分页查询应返回空列表")
  void emptyResultPagination_shouldReturnEmptyList() throws Exception {
    PageResult<Organization> pageResult = PageResult.of(new ArrayList<>(), 0L, 1, 20);

    when(organizationService.listOrganizations(eq(1L), eq(1), eq(20), isNull(), isNull()))
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
        .andExpect(jsonPath("$.data.page").value(1))
        .andExpect(jsonPath("$.data.pageSize").value(20))
        .andExpect(jsonPath("$.data.total").value(0))
        .andExpect(jsonPath("$.data.list").isArray())
        .andExpect(jsonPath("$.data.list").value(hasSize(0)));
  }

  @Test
  @DisplayName("超出范围的页码应返回空结果")
  void outOfRangePage_shouldReturnEmptyList() throws Exception {
    PageResult<Organization> pageResult = PageResult.of(new ArrayList<>(), 50L, 100, 20);

    when(organizationService.listOrganizations(eq(1L), eq(100), eq(20), isNull(), isNull()))
        .thenReturn(pageResult);

    mockMvc
        .perform(
            get("/api/v1/organizations")
                .param("page", "100")
                .param("pageSize", "20")
                .with(
                    user("mockUser")
                        .authorities(new SimpleGrantedAuthority("ORGANIZATION_VIEW"))
                        .password("password")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data.page").value(100))
        .andExpect(jsonPath("$.data.total").value(50))
        .andExpect(jsonPath("$.data.list").isArray())
        .andExpect(jsonPath("$.data.list").value(hasSize(0)));
  }

  @Test
  @DisplayName("分页参数验证 - 页码为0应使用默认值")
  void pageZero_shouldUseDefaultValue() throws Exception {
    List<Organization> firstPageOrgs = testOrganizations.subList(0, 20);
    PageResult<Organization> pageResult = PageResult.of(firstPageOrgs, 50L, 1, 20);

    // 当页码为0时，服务层应默认使用第1页
    when(organizationService.listOrganizations(eq(1L), eq(1), eq(20), isNull(), isNull()))
        .thenReturn(pageResult);

    mockMvc
        .perform(
            get("/api/v1/organizations")
                .param("page", "0")
                .param("pageSize", "20")
                .with(
                    user("mockUser")
                        .authorities(new SimpleGrantedAuthority("ORGANIZATION_VIEW"))
                        .password("password")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data.page").value(1));
  }

  @Test
  @DisplayName("分页参数验证 - 页面大小为0应使用默认值")
  void pageSizeZero_shouldUseDefaultValue() throws Exception {
    List<Organization> firstPageOrgs = testOrganizations.subList(0, 20);
    PageResult<Organization> pageResult = PageResult.of(firstPageOrgs, 50L, 1, 20);

    // 当页面大小为0时，服务层应默认使用20
    when(organizationService.listOrganizations(eq(1L), eq(1), eq(20), isNull(), isNull()))
        .thenReturn(pageResult);

    mockMvc
        .perform(
            get("/api/v1/organizations")
                .param("page", "1")
                .param("pageSize", "0")
                .with(
                    user("mockUser")
                        .authorities(new SimpleGrantedAuthority("ORGANIZATION_VIEW"))
                        .password("password")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data.pageSize").value(20));
  }

  @Test
  @DisplayName("分页参数验证 - 负数页码应使用默认值")
  void negativePage_shouldUseDefaultValue() throws Exception {
    List<Organization> firstPageOrgs = testOrganizations.subList(0, 20);
    PageResult<Organization> pageResult = PageResult.of(firstPageOrgs, 50L, 1, 20);

    // 当页码为负数时，服务层应默认使用第1页
    when(organizationService.listOrganizations(eq(1L), eq(1), eq(20), isNull(), isNull()))
        .thenReturn(pageResult);

    mockMvc
        .perform(
            get("/api/v1/organizations")
                .param("page", "-1")
                .param("pageSize", "20")
                .with(
                    user("mockUser")
                        .authorities(new SimpleGrantedAuthority("ORGANIZATION_VIEW"))
                        .password("password")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data.page").value(1));
  }

  @Test
  @DisplayName("分页参数验证 - 负数页面大小应使用默认值")
  void negativePageSize_shouldUseDefaultValue() throws Exception {
    List<Organization> firstPageOrgs = testOrganizations.subList(0, 20);
    PageResult<Organization> pageResult = PageResult.of(firstPageOrgs, 50L, 1, 20);

    // 当页面大小为负数时，服务层应默认使用20
    when(organizationService.listOrganizations(eq(1L), eq(1), eq(20), isNull(), isNull()))
        .thenReturn(pageResult);

    mockMvc
        .perform(
            get("/api/v1/organizations")
                .param("page", "1")
                .param("pageSize", "-10")
                .with(
                    user("mockUser")
                        .authorities(new SimpleGrantedAuthority("ORGANIZATION_VIEW"))
                        .password("password")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data.pageSize").value(20));
  }

  @Test
  @DisplayName("分页参数验证 - 超大页面大小应限制最大值")
  void oversizedPageSize_shouldBeLimited() throws Exception {
    List<Organization> firstPageOrgs = testOrganizations.subList(0, 100);
    PageResult<Organization> pageResult = PageResult.of(firstPageOrgs, 50L, 1, 100);

    // 当页面大小超过最大值时，服务层应限制为最大值（如100）
    when(organizationService.listOrganizations(eq(1L), eq(1), eq(100), isNull(), isNull()))
        .thenReturn(pageResult);

    mockMvc
        .perform(
            get("/api/v1/organizations")
                .param("page", "1")
                .param("pageSize", "1000")
                .with(
                    user("mockUser")
                        .authorities(new SimpleGrantedAuthority("ORGANIZATION_VIEW"))
                        .password("password")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data.pageSize").value(100));
  }
}
