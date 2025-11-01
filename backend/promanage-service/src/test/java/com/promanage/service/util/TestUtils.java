package com.promanage.service.util;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.springframework.context.ApplicationEventPublisher;

import lombok.extern.slf4j.Slf4j;

/**
 * 测试工具类
 * 
 * <p>提供测试中常用的工具方法
 *
 * @author ProManage Team
 * @date 2025-01-06
 */
@Slf4j
public class TestUtils {

    /**
     * 使用反射设置字段值
     */
    public static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = findField(target.getClass(), fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            log.error("设置字段失败: {}.{}", target.getClass().getSimpleName(), fieldName, e);
            throw new RuntimeException("设置字段失败", e);
        }
    }

    /**
     * 使用反射获取字段值
     */
    @SuppressWarnings("unchecked")
    public static <T> T getField(Object target, String fieldName, Class<T> fieldType) {
        try {
            Field field = findField(target.getClass(), fieldName);
            field.setAccessible(true);
            return (T) field.get(target);
        } catch (Exception e) {
            log.error("获取字段失败: {}.{}", target.getClass().getSimpleName(), fieldName, e);
            throw new RuntimeException("获取字段失败", e);
        }
    }

    /**
     * 查找字段（包括父类）
     */
    private static Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        Class<?> currentClass = clazz;
        while (currentClass != null) {
            try {
                return currentClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                currentClass = currentClass.getSuperclass();
            }
        }
        throw new NoSuchFieldException("字段不存在: " + fieldName);
    }

    /**
     * 等待指定时间
     */
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("等待被中断", e);
        }
    }

    /**
     * 等待条件满足
     */
    public static boolean waitForCondition(java.util.function.BooleanSupplier condition, 
                                         long timeout, TimeUnit unit) {
        long startTime = System.currentTimeMillis();
        long timeoutMs = unit.toMillis(timeout);
        
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            if (condition.getAsBoolean()) {
                return true;
            }
            sleep(10); // 短暂等待
        }
        return false;
    }

    /**
     * 等待CountDownLatch
     */
    public static boolean waitForLatch(CountDownLatch latch, long timeout, TimeUnit unit) {
        try {
            return latch.await(timeout, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 创建测试事件发布器
     */
    public static TestEventPublisher createTestEventPublisher() {
        return new TestEventPublisher();
    }

    /**
     * 创建测试缓存管理器
     */
    public static TestCacheManager createTestCacheManager() {
        return new TestCacheManager();
    }

    /**
     * 创建测试数据构建器
     */
    public static TestDataBuilder createTestDataBuilder() {
        return new TestDataBuilder();
    }

    /**
     * 验证对象不为null
     */
    public static void assertNotNull(Object object, String message) {
        if (object == null) {
            throw new AssertionError(message);
        }
    }

    /**
     * 验证条件为true
     */
    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    /**
     * 验证条件为false
     */
    public static void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError(message);
        }
    }

    /**
     * 验证两个对象相等
     */
    public static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) {
            return;
        }
        if (expected == null || !expected.equals(actual)) {
            throw new AssertionError(String.format("%s: expected=%s, actual=%s", 
                message, expected, actual));
        }
    }

    /**
     * 验证两个对象不相等
     */
    public static void assertNotEquals(Object unexpected, Object actual, String message) {
        if (unexpected == null && actual == null) {
            throw new AssertionError(String.format("%s: unexpected and actual are both null", message));
        }
        if (unexpected != null && unexpected.equals(actual)) {
            throw new AssertionError(String.format("%s: unexpected and actual are equal: %s", 
                message, actual));
        }
    }

    /**
     * 验证异常被抛出
     */
    public static void assertThrows(Class<? extends Throwable> expectedType, 
                                  java.util.function.Supplier<?> supplier) {
        try {
            supplier.get();
            throw new AssertionError("期望抛出异常: " + expectedType.getSimpleName());
        } catch (Throwable actual) {
            if (!expectedType.isInstance(actual)) {
                throw new AssertionError("期望异常类型: " + expectedType.getSimpleName() + 
                    ", 实际异常类型: " + actual.getClass().getSimpleName());
            }
        }
    }

    /**
     * 验证异常不被抛出
     */
    public static void assertDoesNotThrow(java.util.function.Supplier<?> supplier) {
        try {
            supplier.get();
        } catch (Throwable e) {
            throw new AssertionError("不期望抛出异常，但抛出了: " + e.getClass().getSimpleName(), e);
        }
    }

    /**
     * 创建性能测试计时器
     */
    public static PerformanceTimer startTimer() {
        return new PerformanceTimer();
    }

    /**
     * 性能测试计时器
     */
    public static class PerformanceTimer {
        private final long startTime;

        public PerformanceTimer() {
            this.startTime = System.nanoTime();
        }

        public long getElapsedNanos() {
            return System.nanoTime() - startTime;
        }

        public long getElapsedMillis() {
            return getElapsedNanos() / 1_000_000;
        }

        public double getElapsedSeconds() {
            return getElapsedNanos() / 1_000_000_000.0;
        }

        public void logElapsed(String operation) {
            log.info("{} 耗时: {}ms", operation, getElapsedMillis());
        }
    }

    /**
     * 创建重试机制
     */
    public static <T> T retry(java.util.function.Supplier<T> supplier, 
                             int maxAttempts, long delayMs) {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return supplier.get();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    log.warn("第{}次尝试失败，{}ms后重试: {}", attempt, delayMs, e.getMessage());
                    sleep(delayMs);
                }
            }
        }
        
        throw new RuntimeException("重试" + maxAttempts + "次后仍然失败", lastException);
    }

    /**
     * 创建重试机制（带条件）
     */
    public static <T> T retryUntil(java.util.function.Supplier<T> supplier,
                                  java.util.function.Predicate<T> condition,
                                  int maxAttempts, long delayMs) {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                T result = supplier.get();
                if (condition.test(result)) {
                    return result;
                }
                log.warn("第{}次尝试结果不满足条件: {}", attempt, result);
            } catch (Exception e) {
                lastException = e;
                log.warn("第{}次尝试失败: {}", attempt, e.getMessage());
            }
            
            if (attempt < maxAttempts) {
                sleep(delayMs);
            }
        }
        
        throw new RuntimeException("重试" + maxAttempts + "次后仍然失败", lastException);
    }
}
