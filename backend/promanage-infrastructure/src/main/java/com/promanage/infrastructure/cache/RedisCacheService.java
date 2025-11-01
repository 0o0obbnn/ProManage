package com.promanage.infrastructure.cache;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis Cache Service Implementation
 *
 * <p>Provides Redis-based cache operations implementation.
 *
 * @author ProManage Team
 * @since 2025-10-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisCacheService implements CacheService {

  private final RedisTemplate<String, Object> redisTemplate;

  // ==================== String Operations ====================

  @Override
  public void set(String key, Object value) {
    redisTemplate.opsForValue().set(key, value);
  }

  @Override
  public void set(String key, Object value, Duration timeout) {
    redisTemplate.opsForValue().set(key, value, timeout);
  }

  @Override
  public void set(String key, Object value, long timeout, TimeUnit timeUnit) {
    redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
  }

  @Override
  public Boolean setIfAbsent(String key, Object value) {
    return redisTemplate.opsForValue().setIfAbsent(key, value);
  }

  @Override
  public Boolean setIfAbsent(String key, Object value, Duration timeout) {
    return redisTemplate.opsForValue().setIfAbsent(key, value, timeout);
  }

  @Override
  public Object get(String key) {
    return redisTemplate.opsForValue().get(key);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T get(String key, Class<T> clazz) {
    Object value = get(key);
    if (value == null) {
      return null;
    }
    return (T) value;
  }

  @Override
  public Object getAndDelete(String key) {
    return redisTemplate.opsForValue().getAndDelete(key);
  }

  // ==================== Key Operations ====================

  @Override
  public Boolean delete(String key) {
    return redisTemplate.delete(key);
  }

  @Override
  public Long delete(Collection<String> keys) {
    return redisTemplate.delete(keys);
  }

  @Override
  public Boolean exists(String key) {
    return redisTemplate.hasKey(key);
  }

  @Override
  public Boolean expire(String key, Duration timeout) {
    return redisTemplate.expire(key, timeout);
  }

  @Override
  public Boolean expire(String key, long timeout, TimeUnit timeUnit) {
    return redisTemplate.expire(key, timeout, timeUnit);
  }

  @Override
  public Long getExpire(String key) {
    return redisTemplate.getExpire(key);
  }

  @Override
  public Boolean persist(String key) {
    return redisTemplate.persist(key);
  }

  // ==================== Hash Operations ====================

  @Override
  public void hSet(String key, String field, Object value) {
    redisTemplate.opsForHash().put(key, field, value);
  }

  @Override
  public Object hGet(String key, String field) {
    return redisTemplate.opsForHash().get(key, field);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T hGet(String key, String field, Class<T> clazz) {
    Object value = hGet(key, field);
    if (value == null) {
      return null;
    }
    return (T) value;
  }

  @Override
  public Map<Object, Object> hGetAll(String key) {
    return redisTemplate.opsForHash().entries(key);
  }

  @Override
  public void hSetAll(String key, Map<String, Object> map) {
    redisTemplate.opsForHash().putAll(key, map);
  }

  @Override
  public Long hDelete(String key, Object... fields) {
    return redisTemplate.opsForHash().delete(key, fields);
  }

  @Override
  public Boolean hExists(String key, String field) {
    return redisTemplate.opsForHash().hasKey(key, field);
  }

  // ==================== List Operations ====================

  @Override
  public Long lPush(String key, Object value) {
    return redisTemplate.opsForList().leftPush(key, value);
  }

  @Override
  public Long lPushAll(String key, Object... values) {
    return redisTemplate.opsForList().leftPushAll(key, values);
  }

  @Override
  public Object lPop(String key) {
    return redisTemplate.opsForList().leftPop(key);
  }

  @Override
  public List<Object> lRange(String key, long start, long end) {
    return redisTemplate.opsForList().range(key, start, end);
  }

  @Override
  public Long lSize(String key) {
    return redisTemplate.opsForList().size(key);
  }

  // ==================== Set Operations ====================

  @Override
  public Long sAdd(String key, Object... values) {
    return redisTemplate.opsForSet().add(key, values);
  }

  @Override
  public Set<Object> sMembers(String key) {
    return redisTemplate.opsForSet().members(key);
  }

  @Override
  public Boolean sIsMember(String key, Object value) {
    return redisTemplate.opsForSet().isMember(key, value);
  }

  @Override
  public Long sRemove(String key, Object... values) {
    return redisTemplate.opsForSet().remove(key, values);
  }

  @Override
  public Long sSize(String key) {
    return redisTemplate.opsForSet().size(key);
  }

  // ==================== Sorted Set Operations ====================

  @Override
  public Boolean zAdd(String key, Object value, double score) {
    return redisTemplate.opsForZSet().add(key, value, score);
  }

  @Override
  public Set<Object> zRangeByScore(String key, double min, double max) {
    return redisTemplate.opsForZSet().rangeByScore(key, min, max);
  }

  @Override
  public Long zRemove(String key, Object... values) {
    return redisTemplate.opsForZSet().remove(key, values);
  }

  @Override
  public Long zSize(String key) {
    return redisTemplate.opsForZSet().size(key);
  }

  // ==================== Pattern Operations ====================

  @Override
  public Set<String> keys(String pattern) {
    return redisTemplate.keys(pattern);
  }

  @Override
  public Long deleteByPattern(String pattern) {
    Set<String> keys = keys(pattern);
    if (keys != null && !keys.isEmpty()) {
      return delete(keys);
    }
    return 0L;
  }

  @Override
  public Long increment(String key, long delta) {
    return redisTemplate.opsForValue().increment(key, delta);
  }
}
