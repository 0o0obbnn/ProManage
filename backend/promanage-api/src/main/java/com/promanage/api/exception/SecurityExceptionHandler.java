package com.promanage.api.exception;

import com.promanage.common.domain.Result;
import com.promanage.common.domain.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 安全异常处理器
 * <p>
 * 处理认证和权限相关的异常
 * </p>
 *
 * @author ProManage Team
 * @date 2025-10-03
 */
@Slf4j
@RestControllerAdvice
public class SecurityExceptionHandler {

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多级代理的情况，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 记录异常详细信息
     */
    private void logExceptionDetails(Exception e, HttpServletRequest request) {
        String clientIp = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String requestUrl = request.getRequestURL().toString();
        String method = request.getMethod();
        
        log.error("Security Exception Details: ", e);
        log.error("Request Info - URL: {}, Method: {}, IP: {}, UserAgent: {}", 
                 requestUrl, method, clientIp, userAgent);
    }

    /**
     * 处理认证异常
     *
     * @param e 认证异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
        log.warn("认证失败: {}", e.getMessage());
        logExceptionDetails(e, request);
        return Result.error(ResultCode.UNAUTHORIZED.getCode(), "认证失败: " + e.getMessage());
    }

    /**
     * 处理权限不足异常
     *
     * @param e 权限不足异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        log.warn("权限不足: {}", e.getMessage());
        logExceptionDetails(e, request);
        return Result.error(ResultCode.FORBIDDEN.getCode(), "权限不足: " + e.getMessage());
    }
}