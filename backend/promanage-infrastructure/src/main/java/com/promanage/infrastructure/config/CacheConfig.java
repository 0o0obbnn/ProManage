package com.promanage.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.cache.interceptor.SimpleCacheResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.NonNull;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Spring Cache Configuration
 * <p>
 * Configures Spring Cache abstraction with custom key generation and error handling.
 * Works with RedisCacheManager configured in RedisConfig.
 *
 * Temporarily disabled to resolve startup conflicts.
 * </p>
 *
 * @author ProManage Team
 * @since 2025-09-30
 */
@Slf4j
//@Configuration
//@EnableCaching
public class CacheConfig implements CachingConfigurer {

    /**
     * Custom cache key generator
     * <p>
     * Generates cache keys based on:
     * - Class name
     * - Method name
     * - Method parameters
     * </p>
     * <p>
     * Example key format: com.promanage.service.UserService.getUserById[123]
     * </p>
     *
     * @return KeyGenerator instance
     */
    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return new CustomKeyGenerator();
    }

    /**
     * Cache error handler
     * <p>
     * Handles cache errors gracefully without breaking application flow.
     * Logs errors but allows the application to continue without cache.
     * </p>
     *
     * @return CacheErrorHandler instance
     */
    @Bean
    @Override
    public CacheErrorHandler errorHandler() {
        return new CustomCacheErrorHandler();
    }

    /**
     * Cache resolver
     * <p>
     * Uses default SimpleCacheResolver which resolves cache by name.
     * </p>
     *
     * @return CacheResolver instance
     */
    @Bean
    public CacheResolver customCacheResolver(CacheManager cacheManager) {
        return new SimpleCacheResolver(cacheManager);
    }

    /**
     * Custom Key Generator Implementation
     * <p>
     * Generates cache keys in a consistent and readable format.
     * Handles different parameter types appropriately.
     * </p>
     */
    public static class CustomKeyGenerator implements KeyGenerator {

        private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CustomKeyGenerator.class);

        /**
         * Key prefix separator
         */
        private static final String KEY_SEPARATOR = ":";

        /**
         * Parameter separator
         */
        private static final String PARAM_SEPARATOR = "_";

        /**
         * Generate cache key
         *
         * @param target Target object (class instance)
         * @param method Method being called
         * @param params Method parameters
         * @return Generated cache key
         */
        @Override
        @NonNull
        public Object generate(@NonNull Object target, @NonNull Method method, Object... params) {
            StringBuilder keyBuilder = new StringBuilder();

            // Add class name (simple name for readability)
            keyBuilder.append(target.getClass().getSimpleName());
            keyBuilder.append(KEY_SEPARATOR);

            // Add method name
            keyBuilder.append(method.getName());

            // Add parameters if present
            if (params != null && params.length > 0) {
                keyBuilder.append(KEY_SEPARATOR);
                keyBuilder.append(generateParamsKey(params));
            }

            String key = keyBuilder.toString();
            log.debug("Generated cache key: {}", key);
            return key;
        }

        /**
         * Generate key from parameters
         *
         * @param params Method parameters
         * @return Parameters key string
         */
        private String generateParamsKey(Object[] params) {
            if (params == null || params.length == 0) {
                return "";
            }

            StringBuilder paramsBuilder = new StringBuilder();

            for (int i = 0; i < params.length; i++) {
                Object param = params[i];

                if (i > 0) {
                    paramsBuilder.append(PARAM_SEPARATOR);
                }

                if (param == null) {
                    paramsBuilder.append("null");
                } else if (param.getClass().isArray()) {
                    // Handle array parameters
                    paramsBuilder.append(Arrays.deepToString((Object[]) param));
                } else if (param instanceof Iterable) {
                    // Handle collection parameters
                    paramsBuilder.append(param.toString());
                } else {
                    // Handle simple parameters
                    paramsBuilder.append(param.toString());
                }
            }

            return paramsBuilder.toString();
        }
    }

    /**
     * Custom Cache Error Handler
     * <p>
     * Handles cache errors gracefully by logging the error and allowing
     * the application to continue without cache. This prevents cache
     * failures from breaking the application.
     * </p>
     */
    public static class CustomCacheErrorHandler extends SimpleCacheErrorHandler {

        private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CustomCacheErrorHandler.class);

        /**
         * Handle cache get errors
         *
         * @param exception Exception that occurred
         * @param cache Cache instance
         * @param key Cache key
         */
        @Override
        public void handleCacheGetError(@NonNull RuntimeException exception,
                                       @NonNull org.springframework.cache.Cache cache,
                                       @NonNull Object key) {
            log.error("Cache GET error - Cache: {}, Key: {}, Error: {}",
                    cache.getName(), key, exception.getMessage(), exception);
            // Don't throw exception - allow fallback to database
        }

        /**
         * Handle cache put errors
         *
         * @param exception Exception that occurred
         * @param cache Cache instance
         * @param key Cache key
         * @param value Cache value
         */
        @Override
        public void handleCachePutError(@NonNull RuntimeException exception,
                                       @NonNull org.springframework.cache.Cache cache,
                                       @NonNull Object key,
                                       Object value) {
            log.error("Cache PUT error - Cache: {}, Key: {}, Error: {}",
                    cache.getName(), key, exception.getMessage(), exception);
            // Don't throw exception - data is still saved to database
        }

        /**
         * Handle cache evict errors
         *
         * @param exception Exception that occurred
         * @param cache Cache instance
         * @param key Cache key
         */
        @Override
        public void handleCacheEvictError(@NonNull RuntimeException exception,
                                         @NonNull org.springframework.cache.Cache cache,
                                         @NonNull Object key) {
            log.error("Cache EVICT error - Cache: {}, Key: {}, Error: {}",
                    cache.getName(), key, exception.getMessage(), exception);
            // Don't throw exception - stale cache is acceptable
        }

        /**
         * Handle cache clear errors
         *
         * @param exception Exception that occurred
         * @param cache Cache instance
         */
        @Override
        public void handleCacheClearError(@NonNull RuntimeException exception,
                                         @NonNull org.springframework.cache.Cache cache) {
            log.error("Cache CLEAR error - Cache: {}, Error: {}",
                    cache.getName(), exception.getMessage(), exception);
            // Don't throw exception - manual cache cleanup can be done later
        }
    }

    /**
     * Cache Usage Examples:
     * <p>
     * <pre>
     * // Cache a method result
     * {@literal @}Cacheable(value = "users", key = "#userId")
     * public User getUserById(Long userId) {
     *     return userRepository.findById(userId);
     * }
     *
     * // Evict cache on update
     * {@literal @}CacheEvict(value = "users", key = "#user.id")
     * public void updateUser(User user) {
     *     userRepository.save(user);
     * }
     *
     * // Evict all entries in cache
     * {@literal @}CacheEvict(value = "users", allEntries = true)
     * public void clearUserCache() {
     *     // Cache will be cleared
     * }
     *
     * // Update cache with new value
     * {@literal @}CachePut(value = "users", key = "#user.id")
     * public User saveUser(User user) {
     *     return userRepository.save(user);
     * }
     *
     * // Conditional caching
     * {@literal @}Cacheable(value = "users", condition = "#userId > 0", unless = "#result == null")
     * public User getUserById(Long userId) {
     *     return userRepository.findById(userId);
     * }
     * </pre>
     * </p>
     */
}