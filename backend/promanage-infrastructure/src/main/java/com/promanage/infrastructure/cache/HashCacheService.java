package com.promanage.infrastructure.cache;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 哈希缓存服务接口
 *
 * @author ProManage Team
 * @date 2025-10-22
 */
public interface HashCacheService {

    /**
     * 设置哈希字段
     */
    void hSet(String key, String field, Object value);

    /**
     * 设置哈希字段（仅当字段不存在时）
     */
    Boolean hSetIfAbsent(String key, String field, Object value);

    /**
     * 获取哈希字段值
     */
    Object hGet(String key, String field);

    /**
     * 获取哈希字段值并转换为指定类型
     */
    <T> T hGet(String key, String field, Class<T> clazz);

    /**
     * 获取多个哈希字段值
     */
    List<Object> hMultiGet(String key, Collection<String> fields);

    /**
     * 设置多个哈希字段
     */
    void hSetAll(String key, Map<String, Object> hash);

    /**
     * 获取所有哈希字段和值
     */
    Map<String, Object> hGetAll(String key);

    /**
     * 删除哈希字段
     */
    Long hDelete(String key, String... fields);

    /**
     * 检查哈希字段是否存在
     */
    Boolean hExists(String key, String field);

    /**
     * 获取哈希字段数量
     */
    Long hSize(String key);

    /**
     * 获取所有哈希字段名
     */
    Set<String> hKeys(String key);

    /**
     * 获取所有哈希字段值
     */
    List<Object> hValues(String key);

    /**
     * 哈希字段递增
     */
    Long hIncrement(String key, String field, long delta);

    /**
     * 哈希字段递增
     */
    Double hIncrement(String key, String field, double delta);
}
