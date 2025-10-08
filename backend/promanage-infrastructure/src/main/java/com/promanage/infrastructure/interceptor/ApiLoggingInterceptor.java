package com.promanage.infrastructure.interceptor;

import com.promanage.common.util.IpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * API请求日志拦截器
 * <p>
 * 记录所有API请求的详细信息，包括请求参数、响应时间等
 * </p>
 *
 * @author ProManage Team
 * @date 2025-10-08
 */
@Slf4j
@Component
public class ApiLoggingInterceptor implements HandlerInterceptor {

    private static final String START_TIME_ATTRIBUTE = "startTime";
    private static final String REQUEST_ID_ATTRIBUTE = "requestId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 记录请求开始时间
        long startTime = System.currentTimeMillis();
        request.setAttribute(START_TIME_ATTRIBUTE, startTime);

        // 生成请求ID
        String requestId = generateRequestId();
        request.setAttribute(REQUEST_ID_ATTRIBUTE, requestId);

        // 记录请求信息
        String clientIp = IpUtils.getClientIpAddress(request);
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();

        log.info("API请求开始 - RequestId: {}, IP: {}, Method: {}, URI: {}, Params: {}",
                requestId, clientIp, method, uri, queryString);

        // 记录请求头（可选，避免记录敏感信息）
        if (log.isDebugEnabled()) {
            Map<String, String> headers = getRequestHeaders(request);
            log.debug("请求头 - RequestId: {}, Headers: {}", requestId, headers);
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 可以在这里处理响应前的逻辑
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 计算请求处理时间
        Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
        String requestId = (String) request.getAttribute(REQUEST_ID_ATTRIBUTE);

        if (startTime != null) {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            String method = request.getMethod();
            String uri = request.getRequestURI();
            int status = response.getStatus();

            // 记录请求完成信息
            if (ex != null) {
                log.error("API请求异常 - RequestId: {}, Method: {}, URI: {}, Status: {}, Duration: {}ms, Error: {}",
                        requestId, method, uri, status, duration, ex.getMessage(), ex);
            } else {
                log.info("API请求完成 - RequestId: {}, Method: {}, URI: {}, Status: {}, Duration: {}ms",
                        requestId, method, uri, status, duration);
            }
        }
    }

    /**
     * 生成请求ID
     */
    private String generateRequestId() {
        return String.format("%d-%d", System.currentTimeMillis(), (int)(Math.random() * 10000));
    }

    /**
     * 获取请求头信息
     */
    private Map<String, String> getRequestHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
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

    /**
     * 判断是否为敏感请求头
     */
    private boolean isSensitiveHeader(String headerName) {
        String lowerName = headerName.toLowerCase();
        return lowerName.contains("authorization") ||
               lowerName.contains("token") ||
               lowerName.contains("password") ||
               lowerName.contains("secret") ||
               lowerName.contains("key");
    }
}