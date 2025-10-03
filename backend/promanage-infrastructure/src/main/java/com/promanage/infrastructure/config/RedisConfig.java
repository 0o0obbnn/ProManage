package com.promanage.infrastructure.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis Configuration
 * <p>
 * Configures Redis connection, serialization, and cache manager.
 * </p>
 *
 * @author ProManage Team
 * @since 2025-10-02
 */
@Slf4j
@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * Configure RedisTemplate with Jackson2 JSON serialization
     *
     * @param connectionFactory Redis connection factory
     * @return RedisTemplate instance
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        log.info("Initializing RedisTemplate with JSON serialization");

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Create ObjectMapper with JavaTimeModule
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL
        );

        // Use GenericJackson2JsonRedisSerializer with custom ObjectMapper
        GenericJackson2JsonRedisSerializer jackson2JsonRedisSerializer =
            new GenericJackson2JsonRedisSerializer(objectMapper);

        // String serializer for keys
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // Set key serializer
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);

        // Set value serializer
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        template.afterPropertiesSet();

        log.info("RedisTemplate initialized successfully");
        return template;
    }

    /**
     * Configure CacheManager with default cache configuration
     *
     * @param connectionFactory Redis connection factory
     * @return CacheManager instance
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        log.info("Initializing RedisCacheManager");

        // Create ObjectMapper with JavaTimeModule
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL
        );

        // Create GenericJackson2JsonRedisSerializer with custom ObjectMapper
        GenericJackson2JsonRedisSerializer jackson2JsonRedisSerializer =
            new GenericJackson2JsonRedisSerializer(objectMapper);

        // Default cache configuration
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1)) // Default 1 hour TTL
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer)
            )
            .disableCachingNullValues();

        RedisCacheManager cacheManager = RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .transactionAware()
            .build();

        log.info("RedisCacheManager initialized successfully");
        return cacheManager;
    }
}
