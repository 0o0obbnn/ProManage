package com.promanage.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Async Task Executor Configuration
 * <p>
 * Configures thread pool for asynchronous task execution.
 * Enables @Async annotation for background task processing.
 * </p>
 * <p>
 * Thread pool sizing guidelines:
 * - Core pool size: Number of threads always alive
 * - Max pool size: Maximum number of threads
 * - Queue capacity: Size of task queue before creating new threads
 * - Keep alive: Time for idle threads to stay alive
 * </p>
 *
 * @author ProManage Team
 * @since 2025-09-30
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    /**
     * Core pool size - threads always alive
     * Set based on CPU cores (typically 2 * cores for I/O bound tasks)
     */
    private static final int CORE_POOL_SIZE = 10;

    /**
     * Maximum pool size - maximum number of threads
     * Should be higher than core pool size to handle load spikes
     */
    private static final int MAX_POOL_SIZE = 50;

    /**
     * Queue capacity - size of task queue
     * Tasks exceeding this will trigger new thread creation (up to max pool size)
     */
    private static final int QUEUE_CAPACITY = 200;

    /**
     * Keep alive time in seconds
     * Idle threads exceeding core pool size will be terminated after this time
     */
    private static final int KEEP_ALIVE_SECONDS = 60;

    /**
     * Thread name prefix for easy identification in logs
     */
    private static final String THREAD_NAME_PREFIX = "ProManage-Async-";

    /**
     * Configure async task executor
     * <p>
     * Creates a thread pool with the following characteristics:
     * - Core threads: Always alive for immediate task execution
     * - Max threads: Created on demand when queue is full
     * - Queue: Holds tasks when all core threads are busy
     * - Rejection policy: Caller runs (fallback to synchronous execution)
     * </p>
     *
     * @return Configured ThreadPoolTaskExecutor
     */
    @Bean(name = "asyncTaskExecutor")
    @Override
    public Executor getAsyncExecutor() {
        log.info("Configuring async task executor");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Core thread pool configuration
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setKeepAliveSeconds(KEEP_ALIVE_SECONDS);

        // Thread naming for identification
        executor.setThreadNamePrefix(THREAD_NAME_PREFIX);

        // Allow core threads to timeout (helps with resource management)
        executor.setAllowCoreThreadTimeOut(true);

        // Wait for tasks to complete on shutdown (graceful shutdown)
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // Await termination timeout (max time to wait for tasks to complete)
        executor.setAwaitTerminationSeconds(60);

        // Rejection policy - CallerRunsPolicy
        // When pool is full, the task is executed by the calling thread
        // This provides graceful degradation instead of throwing exception
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // Initialize the executor
        executor.initialize();

        log.info("Async task executor configured - Core: {}, Max: {}, Queue: {}",
                CORE_POOL_SIZE, MAX_POOL_SIZE, QUEUE_CAPACITY);

        return executor;
    }

    /**
     * Configure exception handler for uncaught async exceptions
     * <p>
     * Handles exceptions that occur in @Async methods without return value.
     * Logs the exception details for debugging and monitoring.
     * </p>
     *
     * @return AsyncUncaughtExceptionHandler
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new CustomAsyncExceptionHandler();
    }

    /**
     * Custom exception handler for async tasks
     * <p>
     * Logs exception details including:
     * - Method name where exception occurred
     * - Exception type and message
     * - Stack trace for debugging
     * </p>
     */
    public static class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

        private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CustomAsyncExceptionHandler.class);

        @Override
        public void handleUncaughtException(Throwable throwable, Method method, Object... params) {
            log.error("Async task execution failed - Method: {}, Exception: {}",
                    method.getName(),
                    throwable.getMessage(),
                    throwable);

            // Log method parameters for debugging (be careful with sensitive data)
            if (params != null && params.length > 0) {
                log.debug("Method parameters: {}", (Object) params);
            }

            // 异步任务异常监控和告警
            handleAsyncTaskException(throwable, method, params);
        }

        /**
         * 处理异步任务异常
         * <p>
         * 包括监控指标收集、告警通知等功能
         * </p>
         *
         * @param throwable 异常信息
         * @param method 方法信息
         * @param params 方法参数
         */
        private void handleAsyncTaskException(Throwable throwable, Method method, Object... params) {
            try {
                // 1. 记录异常指标
                recordAsyncExceptionMetrics(throwable, method);
                
                // 2. 检查是否需要发送告警
                if (shouldTriggerAlert(throwable, method)) {
                    sendAsyncTaskAlert(throwable, method, params);
                }
                
                // 3. 记录到监控系统
                logToMonitoringSystem(throwable, method, params);
                
            } catch (Exception e) {
                // 避免异常处理本身抛出异常
                log.error("处理异步任务异常时发生错误", e);
            }
        }

        /**
         * 记录异步异常指标
         */
        private void recordAsyncExceptionMetrics(Throwable throwable, Method method) {
            // 这里可以集成Micrometer或其他监控系统
            log.warn("异步任务异常指标记录: method={}, exception={}", 
                method.getName(), throwable.getClass().getSimpleName());
        }

        /**
         * 判断是否需要触发告警
         */
        private boolean shouldTriggerAlert(Throwable throwable, Method method) {
            // 根据异常类型和方法重要性判断是否需要告警
            return throwable instanceof RuntimeException || 
                   throwable instanceof Error ||
                   method.getName().contains("Critical") ||
                   method.getName().contains("Important");
        }

        /**
         * 发送异步任务告警
         */
        private void sendAsyncTaskAlert(Throwable throwable, Method method, Object... params) {
            // 这里可以集成邮件、短信、钉钉等告警渠道
            log.error("异步任务告警: method={}, exception={}, message={}", 
                method.getName(), throwable.getClass().getSimpleName(), throwable.getMessage());
        }

        /**
         * 记录到监控系统
         */
        private void logToMonitoringSystem(Throwable throwable, Method method, Object... params) {
            // 这里可以集成ELK、Prometheus等监控系统
            log.debug("异步任务异常已记录到监控系统: method={}", method.getName());
        }
    }

    /**
     * Create additional executor for specific use cases
     * <p>
     * Example: Email sending executor with different configuration
     * </p>
     *
     * @return Executor for email tasks
     */
    @Bean(name = "emailTaskExecutor")
    public Executor emailTaskExecutor() {
        log.info("Configuring email task executor");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Smaller pool for email tasks (I/O bound, not CPU intensive)
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(50);
        executor.setKeepAliveSeconds(60);

        executor.setThreadNamePrefix("ProManage-Email-");
        executor.setAllowCoreThreadTimeOut(true);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();

        log.info("Email task executor configured");
        return executor;
    }

    /**
     * Create executor for file processing tasks
     * <p>
     * Higher capacity for file upload/download operations
     * </p>
     *
     * @return Executor for file tasks
     */
    @Bean(name = "fileTaskExecutor")
    public Executor fileTaskExecutor() {
        log.info("Configuring file task executor");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Medium-sized pool for file operations
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(15);
        executor.setQueueCapacity(100);
        executor.setKeepAliveSeconds(120);

        executor.setThreadNamePrefix("ProManage-File-");
        executor.setAllowCoreThreadTimeOut(true);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();

        log.info("File task executor configured");
        return executor;
    }

    /**
     * Usage Examples:
     * <p>
     * <pre>
     * // Using default async executor
     * {@literal @}Async
     * public void processDataAsync() {
     *     // This method runs asynchronously
     * }
     *
     * // Using specific executor
     * {@literal @}Async("emailTaskExecutor")
     * public void sendEmailAsync(String to, String subject, String body) {
     *     // Send email asynchronously
     * }
     *
     * // Async method with return value (Future)
     * {@literal @}Async
     * public CompletableFuture<String> processWithResult() {
     *     // Process data
     *     return CompletableFuture.completedFuture("Result");
     * }
     *
     * // Async method with exception handling
     * {@literal @}Async
     * public void riskyOperation() {
     *     try {
     *         // Risky operation
     *     } catch (Exception e) {
     *         // Handle exception
     *         throw new AsyncException("Operation failed", e);
     *     }
     * }
     * </pre>
     * </p>
     */
}