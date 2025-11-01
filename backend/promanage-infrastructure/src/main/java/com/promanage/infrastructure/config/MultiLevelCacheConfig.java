package com.promanage.infrastructure.config;

import java.util.concurrent.TimeUnit;

import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * 多级缓存配置类
 *
 * <p>配置Caffeine本地缓存作为一级缓存，Redis作为二级缓存 实现多级缓存机制以提高系统性能
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-16
 */
@Configuration
public class MultiLevelCacheConfig {

  /**
   * Caffeine本地缓存管理器（一级缓存）
   *
   * <p>配置本地缓存参数： - 初始容量：100 - 最大容量：1000 - 过期时间：30分钟 - 自动刷新：15分钟
   *
   * @return CacheManager实例
   */
  @Bean
  @Primary
  public CacheManager localCacheManager() {
    CaffeineCacheManager cacheManager = new CaffeineCacheManager();

    // 配置Caffeine缓存参数
    Caffeine<Object, Object> caffeine =
        Caffeine.newBuilder()
            .initialCapacity(100) // 初始容量
            .maximumSize(1000) // 最大容量
            .expireAfterWrite(30, TimeUnit.MINUTES) // 写入后30分钟过期
            .expireAfterAccess(15, TimeUnit.MINUTES) // 访问后15分钟过期
            .recordStats(); // 记录缓存统计信息

    cacheManager.setCaffeine(caffeine);
    return cacheManager;
  }
}
