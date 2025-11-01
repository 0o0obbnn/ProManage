package com.promanage.infrastructure.cache;

import java.time.Duration;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 键操作缓存服务接口
 *
 * @author ProManage Team
 * @date 2025-10-22
 */
public interface KeyCacheService {

    /**
     * 删除键
     */
    Boolean delete(String key);

    /**
     * 删除多个键
     */
    Long delete(Collection<String> keys);

    /**
     * 检查键是否存在
     */
    Boolean exists(String key);

    /**
     * 设置键的过期时间
     */
    Boolean expire(String key, Duration timeout);

    /**
     * 设置键的过期时间
     */
    Boolean expire(String key, long timeout, TimeUnit timeUnit);

    /**
     * 获取键的剩余过期时间
     */
    Long getExpire(String key);

    /**
     * 获取键的剩余过期时间
     */
    Long getExpire(String key, TimeUnit timeUnit);

    /**
     * 移除键的过期时间
     */
    Boolean persist(String key);

    /**
     * 获取匹配模式的所有键
     */
    Set<String> keys(String pattern);

    /**
     * 获取随机键
     */
    String randomKey();

    /**
     * 重命名键
     */
    void rename(String oldKey, String newKey);

    /**
     * 重命名键（仅当新键不存在时）
     */
    Boolean renameIfAbsent(String oldKey, String newKey);

    /**
     * 获取键的类型
     */
    String type(String key);
}
