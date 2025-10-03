package com.promanage.infrastructure.cache;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Cache Service Interface
 * <p>
 * Provides a unified interface for cache operations with Redis.
 * Abstracts Redis operations to make cache management easier and more consistent.
 * </p>
 *
 * @author ProManage Team
 * @since 2025-09-30
 */
public interface CacheService {

    // ==================== String Operations ====================

    /**
     * Set string value
     *
     * @param key Cache key
     * @param value Value to cache
     */
    void set(String key, Object value);

    /**
     * Set string value with expiration
     *
     * @param key Cache key
     * @param value Value to cache
     * @param timeout Timeout duration
     */
    void set(String key, Object value, Duration timeout);

    /**
     * Set string value with expiration
     *
     * @param key Cache key
     * @param value Value to cache
     * @param timeout Timeout value
     * @param timeUnit Time unit
     */
    void set(String key, Object value, long timeout, TimeUnit timeUnit);

    /**
     * Set value if key does not exist
     *
     * @param key Cache key
     * @param value Value to cache
     * @return true if key was set, false if key already exists
     */
    Boolean setIfAbsent(String key, Object value);

    /**
     * Set value if key does not exist with expiration
     *
     * @param key Cache key
     * @param value Value to cache
     * @param timeout Timeout duration
     * @return true if key was set, false if key already exists
     */
    Boolean setIfAbsent(String key, Object value, Duration timeout);

    /**
     * Get string value
     *
     * @param key Cache key
     * @return Cached value or null
     */
    Object get(String key);

    /**
     * Get string value with type casting
     *
     * @param key Cache key
     * @param clazz Value class type
     * @param <T> Type parameter
     * @return Cached value or null
     */
    <T> T get(String key, Class<T> clazz);

    /**
     * Get and delete (atomic operation)
     *
     * @param key Cache key
     * @return Cached value before deletion
     */
    Object getAndDelete(String key);

    // ==================== Key Operations ====================

    /**
     * Delete key
     *
     * @param key Cache key
     * @return true if key was deleted, false otherwise
     */
    Boolean delete(String key);

    /**
     * Delete multiple keys
     *
     * @param keys Cache keys
     * @return Number of keys deleted
     */
    Long delete(Collection<String> keys);

    /**
     * Check if key exists
     *
     * @param key Cache key
     * @return true if key exists, false otherwise
     */
    Boolean exists(String key);

    /**
     * Set key expiration
     *
     * @param key Cache key
     * @param timeout Timeout duration
     * @return true if expiration was set, false otherwise
     */
    Boolean expire(String key, Duration timeout);

    /**
     * Set key expiration
     *
     * @param key Cache key
     * @param timeout Timeout value
     * @param timeUnit Time unit
     * @return true if expiration was set, false otherwise
     */
    Boolean expire(String key, long timeout, TimeUnit timeUnit);

    /**
     * Get key expiration time
     *
     * @param key Cache key
     * @return Expiration time in seconds, -1 if no expiration, -2 if key does not exist
     */
    Long getExpire(String key);

    /**
     * Remove key expiration (make it persistent)
     *
     * @param key Cache key
     * @return true if expiration was removed, false otherwise
     */
    Boolean persist(String key);

    // ==================== Hash Operations ====================

    /**
     * Set hash field value
     *
     * @param key Hash key
     * @param field Hash field
     * @param value Field value
     */
    void hSet(String key, String field, Object value);

    /**
     * Get hash field value
     *
     * @param key Hash key
     * @param field Hash field
     * @return Field value or null
     */
    Object hGet(String key, String field);

    /**
     * Get hash field value with type casting
     *
     * @param key Hash key
     * @param field Hash field
     * @param clazz Value class type
     * @param <T> Type parameter
     * @return Field value or null
     */
    <T> T hGet(String key, String field, Class<T> clazz);

    /**
     * Get all hash fields and values
     *
     * @param key Hash key
     * @return Map of field-value pairs
     */
    Map<Object, Object> hGetAll(String key);

    /**
     * Set multiple hash fields
     *
     * @param key Hash key
     * @param map Map of field-value pairs
     */
    void hSetAll(String key, Map<String, Object> map);

    /**
     * Delete hash field
     *
     * @param key Hash key
     * @param fields Hash fields to delete
     * @return Number of fields deleted
     */
    Long hDelete(String key, Object... fields);

    /**
     * Check if hash field exists
     *
     * @param key Hash key
     * @param field Hash field
     * @return true if field exists, false otherwise
     */
    Boolean hExists(String key, String field);

    // ==================== List Operations ====================

    /**
     * Push value to list (left push)
     *
     * @param key List key
     * @param value Value to push
     * @return List size after push
     */
    Long lPush(String key, Object value);

    /**
     * Push multiple values to list (left push)
     *
     * @param key List key
     * @param values Values to push
     * @return List size after push
     */
    Long lPushAll(String key, Object... values);

    /**
     * Pop value from list (left pop)
     *
     * @param key List key
     * @return Popped value or null
     */
    Object lPop(String key);

    /**
     * Get list range
     *
     * @param key List key
     * @param start Start index
     * @param end End index
     * @return List of values
     */
    List<Object> lRange(String key, long start, long end);

    /**
     * Get list size
     *
     * @param key List key
     * @return List size
     */
    Long lSize(String key);

    // ==================== Set Operations ====================

    /**
     * Add member to set
     *
     * @param key Set key
     * @param values Members to add
     * @return Number of members added
     */
    Long sAdd(String key, Object... values);

    /**
     * Get all set members
     *
     * @param key Set key
     * @return Set of members
     */
    Set<Object> sMembers(String key);

    /**
     * Check if member exists in set
     *
     * @param key Set key
     * @param value Member to check
     * @return true if member exists, false otherwise
     */
    Boolean sIsMember(String key, Object value);

    /**
     * Remove member from set
     *
     * @param key Set key
     * @param values Members to remove
     * @return Number of members removed
     */
    Long sRemove(String key, Object... values);

    /**
     * Get set size
     *
     * @param key Set key
     * @return Set size
     */
    Long sSize(String key);

    // ==================== Sorted Set Operations ====================

    /**
     * Add member to sorted set with score
     *
     * @param key Sorted set key
     * @param value Member value
     * @param score Member score
     * @return true if member was added, false if updated
     */
    Boolean zAdd(String key, Object value, double score);

    /**
     * Get sorted set members by score range
     *
     * @param key Sorted set key
     * @param min Minimum score
     * @param max Maximum score
     * @return Set of members
     */
    Set<Object> zRangeByScore(String key, double min, double max);

    /**
     * Remove member from sorted set
     *
     * @param key Sorted set key
     * @param values Members to remove
     * @return Number of members removed
     */
    Long zRemove(String key, Object... values);

    /**
     * Get sorted set size
     *
     * @param key Sorted set key
     * @return Set size
     */
    Long zSize(String key);

    // ==================== Pattern Operations ====================

    /**
     * Get keys matching pattern
     *
     * @param pattern Key pattern (supports * and ?)
     * @return Set of matching keys
     */
    Set<String> keys(String pattern);

    /**
     * Delete keys matching pattern
     *
     * @param pattern Key pattern
     * @return Number of keys deleted
     */
    Long deleteByPattern(String pattern);

    /**
     * Increment numeric value atomically
     *
     * @param key Cache key
     * @param delta Increment value
     * @return New value after increment
     */
    Long increment(String key, long delta);
}