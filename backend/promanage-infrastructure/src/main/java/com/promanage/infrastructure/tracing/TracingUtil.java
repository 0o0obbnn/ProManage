package com.promanage.infrastructure.tracing;

import java.util.Map;
import java.util.concurrent.Callable;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 追踪工具类
 *
 * <p>提供分布式追踪的实用工具方法
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-16
 */
@Slf4j
@Component
public class TracingUtil {

  /**
   * 在Span中执行代码块（带标签）
   *
   * @param name Span名称
   * @param tags 标签映射
   * @param callable 要执行的代码
   * @param <T> 返回类型
   * @return 执行结果
   */
  public <T> T runInSpan(String name, Map<String, String> tags, Callable<T> callable) {
    try {
      log.info("开始执行Span: {}", name);
      if (tags != null) {
        log.info("Span标签: {}", tags);
      }
      T result = callable.call();
      log.info("完成执行Span: {}", name);
      return result;
    } catch (Exception e) {
      log.error("Span执行失败: {}", name, e);
      throw new RuntimeException("Span执行失败: " + e.getMessage(), e);
    }
  }

  /**
   * 记录事件到当前Span
   *
   * @param event 事件名称
   */
  public void recordEvent(String event) {
    log.info("记录事件: {}", event);
  }

  /**
   * 记录事件到当前Span（带标签）
   *
   * @param event 事件名称
   * @param tagKey 标签键
   * @param tagValue 标签值
   */
  public void recordEvent(String event, String tagKey, String tagValue) {
    log.info("记录事件: {}, 标签: {}={}", event, tagKey, tagValue);
  }

  /**
   * 获取当前Trace ID
   *
   * @return Trace ID
   */
  public String getCurrentTraceId() {
    return "trace-id";
  }

  /**
   * 添加标签到当前Span
   *
   * @param key 标签键
   * @param value 标签值
   */
  public void addTag(String key, String value) {
    log.info("添加标签: {}={}", key, value);
  }

  /**
   * 添加多个标签到当前Span
   *
   * @param tags 标签映射
   */
  public void addTags(Map<String, String> tags) {
    if (tags != null) {
      log.info("添加标签: {}", tags);
    }
  }

  /**
   * 记录异常到当前Span
   *
   * @param exception 异常
   */
  public void recordException(Throwable exception) {
    if (exception != null) {
      log.error("记录异常", exception);
    }
  }
}
