package com.promanage.infrastructure.cache;

import java.util.concurrent.Callable;

import org.springframework.cache.Cache;

import lombok.extern.slf4j.Slf4j;

/**
 * 多级缓存实现
 *
 * <p>实现两级缓存：一级本地缓存（Caffeine），二级分布式缓存（Redis） 先从本地缓存获取，如果未命中则从Redis获取并写入本地缓存
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-16
 */
@Slf4j
public class MultiLevelCache implements Cache {

  private final String name;
  private final Cache localCache;
  private final Cache redisCache;

  public MultiLevelCache(String name, Cache localCache, Cache redisCache) {
    this.name = name;
    this.localCache = localCache;
    this.redisCache = redisCache;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Object getNativeCache() {
    return this;
  }

  @Override
  public ValueWrapper get(Object key) {
    log.debug("MultiLevelCache get, cacheName={}, key={}", name, key);

    // 1. 先从本地缓存获取
    ValueWrapper localValue = localCache.get(key);
    if (localValue != null) {
      log.debug("MultiLevelCache hit local cache, cacheName={}, key={}", name, key);
      return localValue;
    }

    // 2. 本地缓存未命中，从Redis获取
    ValueWrapper redisValue = redisCache.get(key);
    if (redisValue != null) {
      log.debug("MultiLevelCache hit redis cache, cacheName={}, key={}", name, key);
      // 3. 将Redis中的值放入本地缓存
      localCache.put(key, redisValue.get());
      return redisValue;
    }

    log.debug("MultiLevelCache miss, cacheName={}, key={}", name, key);
    return null;
  }

  @Override
  public <T> T get(Object key, Class<T> type) {
    log.debug("MultiLevelCache get with type, cacheName={}, key={}, type={}", name, key, type);

    // 1. 先从本地缓存获取
    T localValue = localCache.get(key, type);
    if (localValue != null) {
      log.debug("MultiLevelCache hit local cache with type, cacheName={}, key={}", name, key);
      return localValue;
    }

    // 2. 本地缓存未命中，从Redis获取
    T redisValue = redisCache.get(key, type);
    if (redisValue != null) {
      log.debug("MultiLevelCache hit redis cache with type, cacheName={}, key={}", name, key);
      // 3. 将Redis中的值放入本地缓存
      localCache.put(key, redisValue);
      return redisValue;
    }

    log.debug("MultiLevelCache miss with type, cacheName={}, key={}", name, key);
    return null;
  }

  @Override
  public <T> T get(Object key, Callable<T> valueLoader) {
    log.debug("MultiLevelCache get with valueLoader, cacheName={}, key={}", name, key);

    // 1. 先从本地缓存获取
    T localValue = localCache.get(key, valueLoader);
    if (localValue != null) {
      log.debug(
          "MultiLevelCache hit local cache with valueLoader, cacheName={}, key={}", name, key);
      return localValue;
    }

    // 2. 本地缓存未命中，从Redis获取
    T redisValue = redisCache.get(key, valueLoader);
    if (redisValue != null) {
      log.debug(
          "MultiLevelCache hit redis cache with valueLoader, cacheName={}, key={}", name, key);
      // 3. 将Redis中的值放入本地缓存
      localCache.put(key, redisValue);
      return redisValue;
    }

    log.debug("MultiLevelCache miss with valueLoader, cacheName={}, key={}", name, key);
    return null;
  }

  @Override
  public void put(Object key, Object value) {
    log.debug("MultiLevelCache put, cacheName={}, key={}", name, key);

    // 同时写入本地缓存和Redis
    localCache.put(key, value);
    redisCache.put(key, value);
  }

  @Override
  public void evict(Object key) {
    log.debug("MultiLevelCache evict, cacheName={}, key={}", name, key);

    // 同时从本地缓存和Redis中删除
    localCache.evict(key);
    redisCache.evict(key);
  }

  @Override
  public void clear() {
    log.debug("MultiLevelCache clear, cacheName={}", name);

    // 同时清空本地缓存和Redis
    localCache.clear();
    redisCache.clear();
  }

  @Override
  public ValueWrapper putIfAbsent(Object key, Object value) {
    log.debug("MultiLevelCache putIfAbsent, cacheName={}, key={}", name, key);

    // 先尝试在本地缓存中放入
    ValueWrapper localResult = localCache.putIfAbsent(key, value);
    if (localResult == null) {
      // 本地缓存中没有该键，同时在Redis中放入
      redisCache.putIfAbsent(key, value);
    }
    return localResult;
  }
}
