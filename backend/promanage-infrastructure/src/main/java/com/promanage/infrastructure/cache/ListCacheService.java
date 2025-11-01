package com.promanage.infrastructure.cache;

import java.util.Collection;
import java.util.List;

/**
 * 列表缓存服务接口
 *
 * @author ProManage Team
 * @date 2025-10-22
 */
public interface ListCacheService {

    /**
     * 从左侧推入元素
     */
    Long leftPush(String key, Object value);

    /**
     * 从左侧推入多个元素
     */
    Long leftPushAll(String key, Collection<Object> values);

    /**
     * 从右侧推入元素
     */
    Long rightPush(String key, Object value);

    /**
     * 从右侧推入多个元素
     */
    Long rightPushAll(String key, Collection<Object> values);

    /**
     * 从左侧弹出元素
     */
    Object leftPop(String key);

    /**
     * 从右侧弹出元素
     */
    Object rightPop(String key);

    /**
     * 获取列表长度
     */
    Long size(String key);

    /**
     * 获取列表指定范围的元素
     */
    List<Object> range(String key, long start, long end);

    /**
     * 获取列表指定索引的元素
     */
    Object index(String key, long index);

    /**
     * 设置列表指定索引的元素
     */
    void set(String key, long index, Object value);

    /**
     * 删除列表中的元素
     */
    Long remove(String key, long count, Object value);

    /**
     * 修剪列表
     */
    void trim(String key, long start, long end);
}
