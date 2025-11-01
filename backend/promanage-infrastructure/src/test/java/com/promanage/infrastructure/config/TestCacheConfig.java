package com.promanage.infrastructure.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import lombok.extern.slf4j.Slf4j;

/**
 * 测试环境缓存配置
 * 
 * <p>为测试环境提供简化的缓存配置，使用内存缓存替代Redis
 *
 * @author ProManage Team
 * @date 2025-01-06
 */
@Slf4j
@TestConfiguration
@EnableCaching
public class TestCacheConfig {

    /**
     * 测试环境缓存管理器
     * 
     * <p>使用ConcurrentMapCacheManager提供内存缓存，便于测试验证
     */
    @Bean
    @Primary
    public CacheManager testCacheManager() {
        log.info("初始化测试环境缓存管理器");
        
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        
        // 预创建测试需要的缓存
        cacheManager.setCacheNames(java.util.Arrays.asList("userPermissions", "documents", "roles", "permissions"));
        
        return cacheManager;
    }
}
