package com.promanage.infrastructure.cache;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 集合缓存服务接口
 *
 * @author ProManage Team
 * @date 2025-10-22
 */
public interface SetCacheService {
    /**
     * 添加元素到集合
     */
    Long add(String key, Object... values);

    /**
     * 添加元素到集合
     */
    Long add(String key, Collection<Object> values);

    /**
     * 从集合中移除元素
     */
    Long remove(String key, Object... values);

    /**
     * 从集合中移除元素
     */
    Long remove(String key, Collection<Object> values);

    /**
     * 获取集合中的所有元素
     */
    Set<Object> members(String key);

    /**
     * 检查元素是否在集合中
     */
    Boolean isMember(String key, Object value);

    /**
     * 获取集合大小
     */
    Long size(String key);

    /**
     * 随机获取集合中的元素
     */
    Object randomMember(String key);

    /**
     * 随机获取集合中的多个元素
     */
    List<Object> randomMembers(String key, long count);

    /**
     * 随机移除并返回集合中的元素
     */
    Object randomPop(String key);

    /**
     * 移动元素到另一个集合
     */
    Boolean move(String sourceKey, String destinationKey, Object value);

    /**
     * 计算多个集合的并集
     */
    Set<Object> union(String... keys);

    /**
     * 计算多个集合的交集
     */
    Set<Object> intersect(String... keys);

    /**
     * 计算多个集合的差集
     */
    Set<Object> difference(String... keys);
}
