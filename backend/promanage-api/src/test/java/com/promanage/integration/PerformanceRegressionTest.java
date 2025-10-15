package com.promanage.integration;

import com.promanage.common.entity.Organization;
import com.promanage.common.entity.User;
import com.promanage.common.result.PageResult;
import com.promanage.dto.OrganizationMemberDTO;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 性能回归抽样测试 - 组织列表/成员分页端点负载基准
 * <p>
 * 针对组织列表/成员分页端点做基础负载基准，确认新查询性能无明显退化
 * </p>
 *
 * @author ProManage Team
 * @date 2025-10-12
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@DisplayName("性能回归抽样测试")
class PerformanceRegressionTest extends PostgreSQLContainerBaseTest {

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
    private List<Organization> largeOrganizationDataset;
    private List<OrganizationMemberDTO> largeMemberDataset;
    private User testUser;
    private ExecutorService executorService;

    // 性能基准阈值（毫秒）
    private static final long ORGANIZATION_LIST_RESPONSE_TIME_THRESHOLD = 500L;
    private static final long MEMBER_LIST_RESPONSE_TIME_THRESHOLD = 300L;
    private static final int CONCURRENT_REQUESTS = 10;
    private static final int PERFORMANCE_TEST_ITERATIONS = 5;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        executorService = Executors.newFixedThreadPool(CONCURRENT_REQUESTS);
        
        // 创建测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setOrganizationId(1L);

        // 创建大量组织数据集（模拟生产环境数据量）
        largeOrganizationDataset = new ArrayList<>();
        for (int i = 1; i <= 1000; i++) {
            Organization org = new Organization();
            org.setId((long) i);
            org.setName("性能测试组织" + i);
            org.setSlug("perf-test-org-" + i);
            org.setDescription("性能测试组织描述" + i);
            org.setIsActive(i % 10 != 0); // 90%活跃
            org.setSubscriptionPlan(i % 3 == 0 ? "PRO" : "FREE");
            org.setSubscriptionExpiresAt(LocalDateTime.now().plusMonths(i % 12 + 1));
            org.setCreatedAt(LocalDateTime.now().minusDays(i));
            org.setUpdatedAt(LocalDateTime.now().minusHours(i % 24));
            org.setCreatedBy(1L);
            org.setUpdatedBy(1L);
            largeOrganizationDataset.add(org);
        }

        // 创建大量成员数据集
        largeMemberDataset = new ArrayList<>();
        for (int i = 1; i <= 500; i++) {
            OrganizationMemberDTO member = OrganizationMemberDTO.builder()
                    .id((long) i)
                    .username("member" + i)
                    .email("member" + i + "@test.com")
                    .realName("测试成员" + i)
                    .position("开发工程师")
                    .status(1) // 1-启用
                    .lastLoginTime(LocalDateTime.now().minusHours(i % 72))
                    .build();
            largeMemberDataset.add(member);
        }

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("组织列表分页查询性能基准测试")
    void organizationListPagination_performanceBenchmark() throws Exception {
        // 分页查询1000条组织数据
        PageResult<Organization> pageResult = PageResult.of(
                largeOrganizationDataset.subList(0, 20), 
                1000L, 
                1, 
                20
        );
        
        when(organizationService.listOrganizations(eq(1L), eq(1), eq(20), isNull(), isNull()))
                .thenReturn(pageResult);

        // 执行性能测试
        long totalTime = 0;
        long maxTime = 0;
        long minTime = Long.MAX_VALUE;

        for (int i = 0; i < PERFORMANCE_TEST_ITERATIONS; i++) {
            long startTime = System.currentTimeMillis();
            
            mockMvc.perform(get("/api/v1/organizations")
                            .param("page", "1")
                            .param("pageSize", "20")
                            .with(user("mockUser").authorities(new SimpleGrantedAuthority("ORGANIZATION_VIEW")).password("password")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.total").value(1000))
                    .andExpect(jsonPath("$.data.list").isArray())
                    .andExpect(jsonPath("$.data.list").value(hasSize(20)));
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            totalTime += duration;
            maxTime = Math.max(maxTime, duration);
            minTime = Math.min(minTime, duration);
        }

        long avgTime = totalTime / PERFORMANCE_TEST_ITERATIONS;

        System.out.println("组织列表分页性能测试结果:");
        System.out.println("平均响应时间: " + avgTime + "ms");
        System.out.println("最大响应时间: " + maxTime + "ms");
        System.out.println("最小响应时间: " + minTime + "ms");

        // 验证性能符合基准要求
        if (avgTime > ORGANIZATION_LIST_RESPONSE_TIME_THRESHOLD) {
            throw new AssertionError("组织列表分页查询性能退化! 平均响应时间: " + avgTime + 
                    "ms, 阈值: " + ORGANIZATION_LIST_RESPONSE_TIME_THRESHOLD + "ms");
        }
    }

    @Test
    @DisplayName("组织成员列表分页查询性能基准测试")
    void organizationMemberListPagination_performanceBenchmark() throws Exception {
        // 分页查询500条成员数据
        PageResult<OrganizationMemberDTO> pageResult = PageResult.of(
                largeMemberDataset.subList(0, 20), 
                500L, 
                1, 
                20
        );
        
        when(organizationService.listOrganizationMembers(eq(1L), eq(1L), eq(1), eq(20)))
                .thenReturn(pageResult);

        // 执行性能测试
        long totalTime = 0;
        long maxTime = 0;
        long minTime = Long.MAX_VALUE;

        for (int i = 0; i < PERFORMANCE_TEST_ITERATIONS; i++) {
            long startTime = System.currentTimeMillis();
            
            mockMvc.perform(get("/api/v1/organizations/{id}/members", 1L)
                            .param("page", "1")
                            .param("pageSize", "20")
                            .with(user("mockUser").authorities(new SimpleGrantedAuthority("ORGANIZATION_VIEW")).password("password")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.total").value(500))
                    .andExpect(jsonPath("$.data.list").isArray())
                    .andExpect(jsonPath("$.data.list").value(hasSize(20)));
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            totalTime += duration;
            maxTime = Math.max(maxTime, duration);
            minTime = Math.min(minTime, duration);
        }

        long avgTime = totalTime / PERFORMANCE_TEST_ITERATIONS;

        System.out.println("组织成员列表分页性能测试结果:");
        System.out.println("平均响应时间: " + avgTime + "ms");
        System.out.println("最大响应时间: " + maxTime + "ms");
        System.out.println("最小响应时间: " + minTime + "ms");

        // 验证性能符合基准要求
        if (avgTime > MEMBER_LIST_RESPONSE_TIME_THRESHOLD) {
            throw new AssertionError("组织成员列表分页查询性能退化! 平均响应时间: " + avgTime + 
                    "ms, 阈值: " + MEMBER_LIST_RESPONSE_TIME_THRESHOLD + "ms");
        }
    }

    @Test
    @DisplayName("组织列表并发访问性能测试")
    void organizationListConcurrentAccess_performanceTest() throws Exception {
        // 分页查询组织数据
        PageResult<Organization> pageResult = PageResult.of(
                largeOrganizationDataset.subList(0, 20), 
                1000L, 
                1, 
                20
        );
        
        when(organizationService.listOrganizations(anyLong(), anyInt(), anyInt(), isNull(), isNull()))
                .thenReturn(pageResult);

        // 并发访问测试
        List<CompletableFuture<Long>> futures = new ArrayList<>();
        
        for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
            CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
                try {
                    long startTime = System.currentTimeMillis();
                    
                    mockMvc.perform(get("/api/v1/organizations")
                                    .param("page", "1")
                                    .param("pageSize", "20")
                                    .with(user("mockUser").authorities(new SimpleGrantedAuthority("ORGANIZATION_VIEW")).password("password")))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.code").value(200));
                    
                    return System.currentTimeMillis() - startTime;
                } catch (Exception e) {
                    throw new RuntimeException("并发请求失败", e);
                }
            }, executorService);
            
            futures.add(future);
        }

        // 等待所有请求完成并收集响应时间
        List<Long> responseTimes = new ArrayList<>();
        for (CompletableFuture<Long> future : futures) {
            responseTimes.add(future.get(10, TimeUnit.SECONDS));
        }

        // 计算统计数据
        long totalTime = responseTimes.stream().mapToLong(Long::longValue).sum();
        long avgTime = totalTime / CONCURRENT_REQUESTS;
        long maxTime = responseTimes.stream().mapToLong(Long::longValue).max().orElse(0);
        long minTime = responseTimes.stream().mapToLong(Long::longValue).min().orElse(0);

        System.out.println("组织列表并发访问性能测试结果:");
        System.out.println("并发请求数: " + CONCURRENT_REQUESTS);
        System.out.println("平均响应时间: " + avgTime + "ms");
        System.out.println("最大响应时间: " + maxTime + "ms");
        System.out.println("最小响应时间: " + minTime + "ms");

        // 验证并发性能符合基准要求（允许并发时响应时间稍长）
        long concurrentThreshold = ORGANIZATION_LIST_RESPONSE_TIME_THRESHOLD * 2;
        if (avgTime > concurrentThreshold) {
            throw new AssertionError("组织列表并发访问性能退化! 平均响应时间: " + avgTime + 
                    "ms, 阈值: " + concurrentThreshold + "ms");
        }
    }

    @Test
    @DisplayName("组织成员列表并发访问性能测试")
    void organizationMemberListConcurrentAccess_performanceTest() throws Exception {
        // 分页查询成员数据
        PageResult<OrganizationMemberDTO> pageResult = PageResult.of(
                largeMemberDataset.subList(0, 20), 
                500L, 
                1, 
                20
        );
        
        when(organizationService.listOrganizationMembers(anyLong(), anyLong(), anyInt(), anyInt()))
                .thenReturn(pageResult);

        // 并发访问测试
        List<CompletableFuture<Long>> futures = new ArrayList<>();
        
        for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
            CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
                try {
                    long startTime = System.currentTimeMillis();
                    
                    mockMvc.perform(get("/api/v1/organizations/{id}/members", 1L)
                                    .param("page", "1")
                                    .param("pageSize", "20")
                                    .with(user("mockUser").authorities(new SimpleGrantedAuthority("ORGANIZATION_VIEW")).password("password")))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.code").value(200));
                    
                    return System.currentTimeMillis() - startTime;
                } catch (Exception e) {
                    throw new RuntimeException("并发请求失败", e);
                }
            }, executorService);
            
            futures.add(future);
        }

        // 等待所有请求完成并收集响应时间
        List<Long> responseTimes = new ArrayList<>();
        for (CompletableFuture<Long> future : futures) {
            responseTimes.add(future.get(10, TimeUnit.SECONDS));
        }

        // 计算统计数据
        long totalTime = responseTimes.stream().mapToLong(Long::longValue).sum();
        long avgTime = totalTime / CONCURRENT_REQUESTS;
        long maxTime = responseTimes.stream().mapToLong(Long::longValue).max().orElse(0);
        long minTime = responseTimes.stream().mapToLong(Long::longValue).min().orElse(0);

        System.out.println("组织成员列表并发访问性能测试结果:");
        System.out.println("并发请求数: " + CONCURRENT_REQUESTS);
        System.out.println("平均响应时间: " + avgTime + "ms");
        System.out.println("最大响应时间: " + maxTime + "ms");
        System.out.println("最小响应时间: " + minTime + "ms");

        // 验证并发性能符合基准要求
        long concurrentThreshold = MEMBER_LIST_RESPONSE_TIME_THRESHOLD * 2;
        if (avgTime > concurrentThreshold) {
            throw new AssertionError("组织成员列表并发访问性能退化! 平均响应时间: " + avgTime + 
                    "ms, 阈值: " + concurrentThreshold + "ms");
        }
    }

    @Test
    @DisplayName("大数据量分页性能测试")
    void largeDatasetPagination_performanceTest() throws Exception {
        // 测试不同页面大小的性能
        int[] pageSizes = {10, 20, 50, 100};
        
        for (int pageSize : pageSizes) {
            PageResult<Organization> pageResult = PageResult.of(
                    largeOrganizationDataset.subList(0, pageSize), 
                    1000L, 
                    1, 
                    pageSize
            );
            
            when(organizationService.listOrganizations(eq(1L), eq(1), eq(pageSize), isNull(), isNull()))
                    .thenReturn(pageResult);

            long startTime = System.currentTimeMillis();
            
            mockMvc.perform(get("/api/v1/organizations")
                            .param("page", "1")
                            .param("pageSize", String.valueOf(pageSize))
                            .with(user("mockUser").authorities(new SimpleGrantedAuthority("ORGANIZATION_VIEW")).password("password")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.pageSize").value(pageSize));
            
            long duration = System.currentTimeMillis() - startTime;
            
            System.out.println("页面大小 " + pageSize + " 的响应时间: " + duration + "ms");
            
            // 验证大数据量分页性能不会过度退化
            long threshold = ORGANIZATION_LIST_RESPONSE_TIME_THRESHOLD + (pageSize - 20) * 5;
            if (duration > threshold) {
                throw new AssertionError("页面大小 " + pageSize + " 的分页性能退化! 响应时间: " + 
                        duration + "ms, 阈值: " + threshold + "ms");
            }
        }
    }

    @Test
    @DisplayName("复杂查询条件性能测试")
    void complexQuery_performanceTest() throws Exception {
        // 测试复杂查询条件的性能
        PageResult<Organization> pageResult = PageResult.of(
                largeOrganizationDataset.subList(0, 20), 
                1000L, 
                1, 
                20
        );
        
        when(organizationService.listOrganizations(eq(1L), eq(1), eq(20), eq("测试"), isNull()))
                .thenReturn(pageResult);

        long startTime = System.currentTimeMillis();
        
        mockMvc.perform(get("/api/v1/organizations")
                        .param("page", "1")
                        .param("pageSize", "20")
                        .param("keyword", "测试")
                        .param("isActive", "true")
                        .param("subscriptionPlan", "PRO")
                        .with(user("mockUser").authorities(new SimpleGrantedAuthority("ORGANIZATION_VIEW")).password("password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        
        long duration = System.currentTimeMillis() - startTime;
        
        System.out.println("复杂查询条件响应时间: " + duration + "ms");
        
        // 验证复杂查询性能符合基准要求（允许稍长的响应时间）
        long complexQueryThreshold = ORGANIZATION_LIST_RESPONSE_TIME_THRESHOLD * 2;
        if (duration > complexQueryThreshold) {
            throw new AssertionError("复杂查询性能退化! 响应时间: " + duration + 
                    "ms, 阈值: " + complexQueryThreshold + "ms");
        }
    }
}