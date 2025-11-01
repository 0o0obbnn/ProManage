package com.promanage.service.util;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import lombok.extern.slf4j.Slf4j;

/**
 * 测试缓存管理器
 * 
 * <p>提供测试环境中的缓存管理功能，支持缓存操作统计和验证
 *
 * @author ProManage Team
 * @date 2025-01-06
 */
@Slf4j
public class TestCacheManager implements CacheManager {

    private final Map<String, TestCache> caches = new ConcurrentHashMap<>();
    private final AtomicLong operationCount = new AtomicLong(0);
    private final AtomicInteger evictionCount = new AtomicInteger(0);

    @Override
    public Cache getCache(String name) {
        return caches.computeIfAbsent(name, TestCache::new);
    }

    @Override
    public Collection<String> getCacheNames() {
        return caches.keySet();
    }

    /**
     * 获取缓存操作统计信息
     */
    public CacheStats getCacheStats() {
        return CacheStats.builder()
            .totalOperations(operationCount.get())
            .totalEvictions(evictionCount.get())
            .cacheCount(caches.size())
            .build();
    }

    /**
     * 获取指定缓存的统计信息
     */
    public CacheStats getCacheStats(String cacheName) {
        TestCache cache = caches.get(cacheName);
        if (cache == null) {
            return CacheStats.builder().build();
        }
        return cache.getStats();
    }

    /**
     * 清空所有缓存
     */
    public void clearAllCaches() {
        caches.values().forEach(Cache::clear);
        operationCount.set(0);
        evictionCount.set(0);
    }

    /**
     * 清空指定缓存
     */
    public void clearCache(String cacheName) {
        TestCache cache = caches.get(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }

    /**
     * 获取缓存中的条目数量
     */
    public int getCacheSize(String cacheName) {
        TestCache cache = caches.get(cacheName);
        return cache != null ? cache.size() : 0;
    }

    /**
     * 检查缓存中是否存在指定键
     */
    public boolean containsKey(String cacheName, Object key) {
        TestCache cache = caches.get(cacheName);
        return cache != null && cache.containsKey(key);
    }

    /**
     * 测试缓存实现
     */
    private class TestCache implements Cache {
        private final String name;
        private final Map<Object, Object> store = new ConcurrentHashMap<>();
        private final AtomicLong getCount = new AtomicLong(0);
        private final AtomicLong putCount = new AtomicLong(0);
        private final AtomicLong evictCount = new AtomicLong(0);
        private final AtomicLong clearCount = new AtomicLong(0);

        public TestCache(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Object getNativeCache() {
            return store;
        }

        @Override
        public ValueWrapper get(Object key) {
            operationCount.incrementAndGet();
            getCount.incrementAndGet();
            log.debug("缓存获取: {} -> {}", name, key);
            return () -> store.get(key);
        }

        @Override
        public <T> T get(Object key, Class<T> type) {
            operationCount.incrementAndGet();
            getCount.incrementAndGet();
            log.debug("缓存获取: {} -> {} (类型: {})", name, key, type.getSimpleName());
            Object value = store.get(key);
            if (value != null && type.isInstance(value)) {
                return type.cast(value);
            }
            return null;
        }

        @Override
        public <T> T get(Object key, java.util.concurrent.Callable<T> valueLoader) {
            operationCount.incrementAndGet();
            getCount.incrementAndGet();
            log.debug("缓存获取: {} -> {} (带加载器)", name, key);
            
            Object value = store.get(key);
            if (value != null) {
                @SuppressWarnings("unchecked")
                T castValue = (T) value;
                return castValue;
            }
            
            try {
                T loadedValue = valueLoader.call();
                store.put(key, loadedValue);
                putCount.incrementAndGet();
                return loadedValue;
            } catch (Exception e) {
                throw new RuntimeException("缓存加载失败", e);
            }
        }

        @Override
        public void put(Object key, Object value) {
            operationCount.incrementAndGet();
            putCount.incrementAndGet();
            log.debug("缓存存储: {} -> {} = {}", name, key, value);
            store.put(key, value);
        }

        @Override
        public void evict(Object key) {
            operationCount.incrementAndGet();
            evictCount.incrementAndGet();
            evictionCount.incrementAndGet();
            log.debug("缓存失效: {} -> {}", name, key);
            store.remove(key);
        }

        @Override
        public void clear() {
            operationCount.incrementAndGet();
            clearCount.incrementAndGet();
            log.debug("缓存清空: {}", name);
            store.clear();
        }

        public int size() {
            return store.size();
        }

        public boolean containsKey(Object key) {
            return store.containsKey(key);
        }

        public CacheStats getStats() {
            return CacheStats.builder()
                .totalOperations(getCount.get() + putCount.get() + evictCount.get() + clearCount.get())
                .totalEvictions(evictCount.get())
                .cacheCount(1)
                .build();
        }
    }

    /**
     * 缓存统计信息
     */
    public static class CacheStats {
        private long totalOperations;
        private long totalEvictions;
        private int cacheCount;

        public static CacheStatsBuilder builder() {
            return new CacheStatsBuilder();
        }

        public static class CacheStatsBuilder {
            private CacheStats stats = new CacheStats();

            public CacheStatsBuilder totalOperations(long totalOperations) {
                stats.totalOperations = totalOperations;
                return this;
            }

            public CacheStatsBuilder totalEvictions(long totalEvictions) {
                stats.totalEvictions = totalEvictions;
                return this;
            }

            public CacheStatsBuilder cacheCount(int cacheCount) {
                stats.cacheCount = cacheCount;
                return this;
            }

            public CacheStats build() {
                return stats;
            }
        }

        // Getters
        public long getTotalOperations() { return totalOperations; }
        public long getTotalEvictions() { return totalEvictions; }
        public int getCacheCount() { return cacheCount; }

        @Override
        public String toString() {
            return String.format("CacheStats{operations=%d, evictions=%d, caches=%d}",
                totalOperations, totalEvictions, cacheCount);
        }
    }
}
