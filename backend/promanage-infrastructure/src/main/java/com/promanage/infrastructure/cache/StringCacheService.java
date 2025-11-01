package com.promanage.infrastructure.cache;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 字符串缓存服务接口
 *
 * @author ProManage Team
 * @date 2025-10-22
 */
public interface StringCacheService {

    /**
     * 设置字符串值
     */
    void set(String key, Object value);

    /**
     * 设置字符串值并指定过期时间
     */
    void set(String key, Object value, Duration timeout);

    /**
     * 设置字符串值并指定过期时间
     */
    void set(String key, Object value, long timeout, TimeUnit timeUnit);

    /**
     * 仅当键不存在时设置值
     */
    Boolean setIfAbsent(String key, Object value);

    /**
     * 仅当键不存在时设置值并指定过期时间
     */
    Boolean setIfAbsent(String key, Object value, Duration timeout);

    /**
     * 获取字符串值
     */
    Object get(String key);

    /**
     * 获取字符串值并转换为指定类型
     */
    <T> T get(String key, Class<T> clazz);

    /**
     * 获取并删除（原子操作）
     */
    Object getAndDelete(String key);

    /**
     * 递增
     */
    Long increment(String key);

    /**
     * 递增指定值
     */
    Long increment(String key, long delta);

    /**
     * 递减
     */
    Long decrement(String key);

    /**
     * 递减指定值
     */
    Long decrement(String key, long delta);
}
