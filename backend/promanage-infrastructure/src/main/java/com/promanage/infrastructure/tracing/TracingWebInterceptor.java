package com.promanage.infrastructure.tracing;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Web请求追踪拦截器
 *
 * <p>自动追踪所有HTTP请求，创建Span并记录请求信息
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-16
 */
@Slf4j
@Component
public class TracingWebInterceptor implements HandlerInterceptor {

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    // 生成请求ID
    String requestId = UUID.randomUUID().toString();
    request.setAttribute("requestId", requestId);

    // 获取客户端IP地址
    String clientIp = getClientIp(request);
    request.setAttribute("clientIp", clientIp);

    log.debug("开始追踪HTTP请求, requestId: {}, clientIp: {}", requestId, clientIp);
    return true;
  }

  @Override
  public void postHandle(
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler,
      ModelAndView modelAndView)
      throws Exception {
    // 记录处理完成事件
  }

  @Override
  public void afterCompletion(
      HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
      throws Exception {
    String requestId = (String) request.getAttribute("requestId");
    String clientIp = (String) request.getAttribute("clientIp");
    log.debug(
        "完成追踪HTTP请求, requestId: {}, clientIp: {}, status: {}",
        requestId,
        clientIp,
        response.getStatus());
  }

  /**
   * 获取客户端IP地址
   *
   * @param request HTTP请求
   * @return 客户端IP地址
   */
  private String getClientIp(HttpServletRequest request) {
    String xfHeader = request.getHeader("X-Forwarded-For");
    if (xfHeader == null) {
      xfHeader = request.getHeader("X-Real-IP");
    }
    if (xfHeader == null) {
      return request.getRemoteAddr();
    }
    return xfHeader.split(",")[0].trim();
  }
}
