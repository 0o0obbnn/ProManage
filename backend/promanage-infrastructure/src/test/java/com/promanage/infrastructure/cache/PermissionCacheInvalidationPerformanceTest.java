package com.promanage.infrastructure.cache;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import com.promanage.domain.event.RolePermissionChangedEvent;
import com.promanage.domain.event.UserRoleAssignedEvent;
import com.promanage.domain.event.UserRoleRemovedEvent;
import com.promanage.domain.mapper.UserRoleMapper;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;

/**
 * 权限缓存失效监听器性能测试
 * 
 * <p>测试缓存失效监听器在高并发场景下的性能表现
 *
 * @author ProManage Team
 * @date 2025-01-06
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("权限缓存失效性能测试")
@Disabled
class PermissionCacheInvalidationPerformanceTest {

    @Mock
    private CacheManager cacheManager;

    @Mock
    private UserRoleMapper userRoleMapper;

    @Mock
    private Cache userPermissionsCache;

    private PermissionCacheInvalidationListener listener;
    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        listener = new PermissionCacheInvalidationListener(cacheManager, userRoleMapper);
        executorService = Executors.newFixedThreadPool(20);
        
        // 默认缓存配置
        when(cacheManager.getCache("userPermissions")).thenReturn(userPermissionsCache);
    }

    @AfterEach
    void tearDown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    @Test
    @DisplayName("角色权限变更 - 大量用户缓存失效性能测试")
    void testRolePermissionChanged_LargeUserSetPerformance() {
        // Given
        Long roleId = 1L;
        int userCount = 1000;
        List<Long> userIds = generateUserIds(userCount);
        
        RolePermissionChangedEvent event = new RolePermissionChangedEvent(roleId);
        when(userRoleMapper.findUserIdsByRoleId(roleId)).thenReturn(userIds);

        // When
        long startTime = System.currentTimeMillis();
        listener.handleRolePermissionChanged(event);
        long endTime = System.currentTimeMillis();

        // Then
        long executionTime = endTime - startTime;
        log.info("处理{}个用户缓存失效耗时: {}ms", userCount, executionTime);
        
        // 性能断言：1000个用户缓存失效应在5秒内完成
        assertTrue(executionTime < 5000, 
            String.format("缓存失效耗时过长: %dms，期望 < 5000ms", executionTime));
        
        verify(userRoleMapper).findUserIdsByRoleId(roleId);
        verify(userPermissionsCache, times(userCount)).evict(any(Long.class));
    }

    @Test
    @DisplayName("并发角色权限变更 - 多线程性能测试")
    void testConcurrentRolePermissionChanged_Performance() throws InterruptedException {
        // Given
        int threadCount = 10;
        int eventsPerThread = 50;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // 准备测试数据
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            executorService.submit(() -> {
                try {
                    startLatch.await(); // 等待所有线程同时开始
                    
                    for (int j = 0; j < eventsPerThread; j++) {
                        Long roleId = (long) (threadIndex * eventsPerThread + j);
                        List<Long> userIds = generateUserIds(100); // 每个角色100个用户
                        
                        RolePermissionChangedEvent event = new RolePermissionChangedEvent(roleId);
                        when(userRoleMapper.findUserIdsByRoleId(roleId)).thenReturn(userIds);
                        
                        try {
                            listener.handleRolePermissionChanged(event);
                            successCount.incrementAndGet();
                        } catch (Exception e) {
                            failCount.incrementAndGet();
                            log.error("线程{}处理事件失败", threadIndex, e);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    failCount.incrementAndGet();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        // When
        long startTime = System.currentTimeMillis();
        startLatch.countDown(); // 开始执行
        boolean finished = finishLatch.await(30, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();

        // Then
        assertTrue(finished, "并发测试未在30秒内完成");
        
        long totalTime = endTime - startTime;
        int totalEvents = threadCount * eventsPerThread;
        double eventsPerSecond = (double) totalEvents / (totalTime / 1000.0);
        
        log.info("并发测试结果: 总事件数={}, 成功={}, 失败={}, 总耗时={}ms, 吞吐量={:.2f}事件/秒", 
            totalEvents, successCount.get(), failCount.get(), totalTime, eventsPerSecond);
        
        // 性能断言：吞吐量应大于100事件/秒
        assertTrue(eventsPerSecond > 100, 
            String.format("吞吐量过低: %.2f事件/秒，期望 > 100事件/秒", eventsPerSecond));
        
        // 成功率应大于95%
        double successRate = (double) successCount.get() / totalEvents;
        assertTrue(successRate > 0.95, 
            String.format("成功率过低: %.2f%%，期望 > 95%%", successRate * 100));
    }

    @Test
    @DisplayName("用户角色分配 - 高频事件处理性能测试")
    void testUserRoleAssigned_HighFrequencyPerformance() throws InterruptedException {
        // Given
        int eventCount = 500;
        CountDownLatch latch = new CountDownLatch(eventCount);
        AtomicInteger successCount = new AtomicInteger(0);

        // When
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < eventCount; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    Long userId = (long) index;
                    Long roleId = (long) (index % 10); // 10个角色循环使用
                    
                    UserRoleAssignedEvent event = new UserRoleAssignedEvent(userId, roleId);
                    listener.handleUserRoleAssigned(event);
                    
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("处理用户角色分配事件失败: {}", index, e);
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean finished = latch.await(10, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();

        // Then
        assertTrue(finished, "高频事件测试未在10秒内完成");
        
        long totalTime = endTime - startTime;
        double eventsPerSecond = (double) eventCount / (totalTime / 1000.0);
        
        log.info("高频事件测试结果: 事件数={}, 成功={}, 耗时={}ms, 吞吐量={:.2f}事件/秒", 
            eventCount, successCount.get(), totalTime, eventsPerSecond);
        
        // 性能断言：吞吐量应大于50事件/秒
        assertTrue(eventsPerSecond > 50, 
            String.format("高频事件吞吐量过低: %.2f事件/秒，期望 > 50事件/秒", eventsPerSecond));
    }

    @Test
    @DisplayName("缓存失效 - 异常情况下的性能表现")
    void testCacheEviction_ExceptionHandlingPerformance() {
        // Given
        Long roleId = 1L;
        int userCount = 100;
        List<Long> userIds = generateUserIds(userCount);
        
        RolePermissionChangedEvent event = new RolePermissionChangedEvent(roleId);
        when(userRoleMapper.findUserIdsByRoleId(roleId)).thenReturn(userIds);
        
        // 模拟部分缓存失效失败
        doAnswer(invocation -> {
            Long userId = invocation.getArgument(0);
            if (userId % 10 == 0) { // 每10个用户失败一次
                throw new RuntimeException("Cache eviction failed for user: " + userId);
            }
            return null;
        }).when(userPermissionsCache).evict(any(Long.class));

        // When
        long startTime = System.currentTimeMillis();
        listener.handleRolePermissionChanged(event);
        long endTime = System.currentTimeMillis();

        // Then
        long executionTime = endTime - startTime;
        log.info("异常情况下处理{}个用户缓存失效耗时: {}ms", userCount, executionTime);
        
        // 即使有异常，性能也不应该显著下降
        assertTrue(executionTime < 2000, 
            String.format("异常情况下缓存失效耗时过长: %dms，期望 < 2000ms", executionTime));
        
        verify(userPermissionsCache, times(userCount)).evict(any(Long.class));
    }

    @Test
    @DisplayName("内存使用 - 大量事件处理内存稳定性测试")
    void testMemoryUsage_LargeEventProcessing() {
        // Given
        int eventCount = 1000;
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // When
        long startTime = System.currentTimeMillis();
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        for (int i = 0; i < eventCount; i++) {
            final int index = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                Long userId = (long) index;
                Long roleId = (long) (index % 5);
                
                UserRoleAssignedEvent event = new UserRoleAssignedEvent(userId, roleId);
                listener.handleUserRoleAssigned(event);
            }, executorService);
            
            futures.add(future);
        }

        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        long endTime = System.currentTimeMillis();
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = finalMemory - initialMemory;

        // Then
        long totalTime = endTime - startTime;
        log.info("内存测试结果: 事件数={}, 耗时={}ms, 内存使用={}MB", 
            eventCount, totalTime, memoryUsed / 1024 / 1024);
        
        // 内存使用不应超过100MB
        assertTrue(memoryUsed < 100 * 1024 * 1024, 
            String.format("内存使用过多: %dMB，期望 < 100MB", memoryUsed / 1024 / 1024));
    }

    @Test
    @DisplayName("响应时间分布 - 不同用户数量下的性能表现")
    void testResponseTimeDistribution_DifferentUserCounts() {
        // Given
        int[] userCounts = {10, 50, 100, 500, 1000};
        long[] responseTimes = new long[userCounts.length];

        // When
        for (int i = 0; i < userCounts.length; i++) {
            int userCount = userCounts[i];
            Long roleId = (long) i;
            List<Long> userIds = generateUserIds(userCount);
            
            RolePermissionChangedEvent event = new RolePermissionChangedEvent(roleId);
            when(userRoleMapper.findUserIdsByRoleId(roleId)).thenReturn(userIds);

            long startTime = System.nanoTime();
            listener.handleRolePermissionChanged(event);
            long endTime = System.nanoTime();
            
            responseTimes[i] = (endTime - startTime) / 1_000_000; // 转换为毫秒
        }

        // Then
        log.info("响应时间分布测试结果:");
        for (int i = 0; i < userCounts.length; i++) {
            log.info("用户数: {}, 响应时间: {}ms", userCounts[i], responseTimes[i]);
            
            // 响应时间应随用户数量线性增长，但不应超过合理范围
            long expectedMaxTime = userCounts[i] * 2; // 每个用户最多2ms
            assertTrue(responseTimes[i] < expectedMaxTime, 
                String.format("用户数%d的响应时间%dms超过预期%dms", 
                    userCounts[i], responseTimes[i], expectedMaxTime));
        }
    }

    /**
     * 生成指定数量的用户ID列表
     */
    private List<Long> generateUserIds(int count) {
        List<Long> userIds = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            userIds.add((long) (i + 1));
        }
        return userIds;
    }
}
