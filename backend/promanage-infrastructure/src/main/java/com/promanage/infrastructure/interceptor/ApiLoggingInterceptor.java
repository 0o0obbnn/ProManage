package com.promanage.infrastructure.interceptor;

import java.util.Enumeration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.promanage.common.util.IpUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * API请求日志拦截器
 *
 * <p>记录所有API请求的详细信息，包括请求参数、响应时间等 支持 Correlation ID 跨服务追踪
 *
 * @author ProManage Team
 * @date 2025-10-08
 */
@Slf4j
@Component
public class ApiLoggingInterceptor implements HandlerInterceptor {

  private static final String START_TIME_ATTRIBUTE = "startTime";
  private static final String REQUEST_ID_ATTRIBUTE = "requestId";

  // ✅ P2-004: MDC key for correlation ID (enables distributed tracing)
  private static final String CORRELATION_ID = "correlationId";
  private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    // 记录请求开始时间
    long startTime = System.currentTimeMillis();
    request.setAttribute(START_TIME_ATTRIBUTE, startTime);

    // ✅ P2-004: 生成或获取 Correlation ID (支持分布式追踪)
    String correlationId = getOrCreateCorrelationId(request);
    request.setAttribute(REQUEST_ID_ATTRIBUTE, correlationId);

    // ✅ P2-004: 将 Correlation ID 放入 MDC，所有后续日志自动包含此ID
    MDC.put(CORRELATION_ID, correlationId);

    // ✅ P2-004: 将 Correlation ID 添加到响应头，便于客户端追踪
    response.setHeader(CORRELATION_ID_HEADER, correlationId);

    // 记录请求信息
    String clientIp = IpUtils.getClientIpAddress(request);
    String method = request.getMethod();
    String uri = request.getRequestURI();
    String queryString = request.getQueryString();

    log.info(
        "API请求开始 - IP: {}, Method: {}, URI: {}, Params: {}", clientIp, method, uri, queryString);

    // 记录请求头（可选，避免记录敏感信息）
    if (log.isDebugEnabled()) {
      Map<String, String> headers = getRequestHeaders(request);
      log.debug("请求头 - Headers: {}", headers);
    }

    return true;
  }

  @Override
  public void postHandle(
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler,
      ModelAndView modelAndView)
      throws Exception {
    // 可以在这里处理响应前的逻辑
  }

  @Override
  public void afterCompletion(
      HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
      throws Exception {
    try {
      // 计算请求处理时间
      Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);

      if (startTime != null) {
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        String method = request.getMethod();
        String uri = request.getRequestURI();
        int status = response.getStatus();

        // 记录请求完成信息
        if (ex != null) {
          log.error(
              "API请求异常 - Method: {}, URI: {}, Status: {}, Duration: {}ms, Error: {}",
              method,
              uri,
              status,
              duration,
              ex.getMessage(),
              ex);
        } else {
          log.info(
              "API请求完成 - Method: {}, URI: {}, Status: {}, Duration: {}ms",
              method,
              uri,
              status,
              duration);
        }
      }
    } finally {
      // ✅ P2-004: 清理 MDC，防止内存泄漏（异步环境下尤其重要）
      MDC.remove(CORRELATION_ID);
    }
  }

  /**
   * ✅ P2-004: 获取或创建 Correlation ID
   *
   * <p>优先从请求头获取（支持跨服务传递），否则生成新的UUID 这样可以追踪跨多个微服务的完整请求链路
   *
   * @param request HTTP请求
   * @return Correlation ID
   */
  private String getOrCreateCorrelationId(HttpServletRequest request) {
    // 1. 尝试从请求头获取已有的 Correlation ID (微服务间传递)
    String correlationId = request.getHeader(CORRELATION_ID_HEADER);

    // 2. 如果没有，生成新的 UUID 格式的 Correlation ID
    if (correlationId == null || correlationId.isBlank()) {
      correlationId = UUID.randomUUID().toString().replace("-", "");
      log.debug("生成新 Correlation ID: {}", correlationId);
    } else {
      log.debug("使用传入的 Correlation ID: {}", correlationId);
    }

    return correlationId;
  }

  /** 获取请求头信息 */
  private Map<String, String> getRequestHeaders(HttpServletRequest request) {
    Map<String, String> headers = new ConcurrentHashMap<>();
    Enumeration<String> headerNames = request.getHeaderNames();

    while (headerNames.hasMoreElements()) {
      String headerName = headerNames.nextElement();
      // 过滤敏感信息
      if (!isSensitiveHeader(headerName)) {
        headers.put(headerName, request.getHeader(headerName));
      }
    }

    return headers;
  }

  /** 判断是否为敏感请求头 */
  private boolean isSensitiveHeader(String headerName) {
    String lowerName = headerName.toLowerCase();
    return lowerName.contains("authorization")
        || lowerName.contains("token")
        || lowerName.contains("password")
        || lowerName.contains("secret")
        || lowerName.contains("key");
  }
}
