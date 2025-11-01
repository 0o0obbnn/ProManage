package com.promanage.infrastructure.cache;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 多级缓存管理器
 *
 * <p>实现两级缓存：一级本地缓存（Caffeine），二级分布式缓存（Redis） 先从本地缓存获取，如果未命中则从Redis获取并写入本地缓存
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-16
 */
@Slf4j
@Component
public class MultiLevelCacheManager implements CacheManager {

  private final CacheManager localCacheManager;
  private final CacheManager redisCacheManager;

  public MultiLevelCacheManager(
      CacheManager localCacheManager,
      @Qualifier("redisCacheManager") CacheManager redisCacheManager) {
    this.localCacheManager = localCacheManager;
    this.redisCacheManager = redisCacheManager;
  }

  @Override
  public Cache getCache(String name) {
    // 创建多级缓存实例
    Cache localCache = localCacheManager.getCache(name);
    Cache redisCache = redisCacheManager.getCache(name);

    if (localCache != null && redisCache != null) {
      return new MultiLevelCache(name, localCache, redisCache);
    }

    // 如果任一缓存不可用，回退到Redis缓存
    return redisCache;
  }

  @Override
  public Collection<String> getCacheNames() {
    return redisCacheManager.getCacheNames();
  }
}
