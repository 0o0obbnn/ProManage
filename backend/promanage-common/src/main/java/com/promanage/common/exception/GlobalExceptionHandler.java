package com.promanage.common.exception;

import com.promanage.common.domain.Result;
import com.promanage.common.domain.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * <p>
 * 统一处理系统中的所有异常,返回标准的错误响应
 * </p>
 *
 * @author ProManage Team
 * @date 2025-09-30
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

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
        
        log.error("Exception Details: ", e);
        log.error("Request Info - URL: {}, Method: {}, IP: {}, UserAgent: {}", 
                 requestUrl, method, clientIp, userAgent);
    }

    /**
     * 处理业务异常
     *
     * @param e 业务异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        logExceptionDetails(e, request);
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常 - @Valid
     *
     * @param e 参数校验异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        log.warn("参数校验失败: {}", message);
        logExceptionDetails(e, request);
        return Result.error(ResultCode.VALIDATION_ERROR.getCode(), message);
    }

    /**
     * 处理参数绑定异常
     *
     * @param e 参数绑定异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException e, HttpServletRequest request) {
        String message = e.getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        log.warn("参数绑定失败: {}", message);
        logExceptionDetails(e, request);
        return Result.error(ResultCode.VALIDATION_ERROR.getCode(), message);
    }

    /**
     * 处理参数校验异常 - @Validated
     *
     * @param e 参数校验异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        String message = e.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));

        log.warn("参数校验失败: {}", message);
        logExceptionDetails(e, request);
        return Result.error(ResultCode.VALIDATION_ERROR.getCode(), message);
    }

    /**
     * 处理参数类型不匹配异常
     *
     * @param e 参数类型不匹配异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        String message = String.format("参数类型错误: %s 应为 %s 类型",
                e.getName(),
                e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "未知");

        log.warn("参数类型错误: {}", message);
        logExceptionDetails(e, request);
        return Result.error(ResultCode.BAD_REQUEST.getCode(), message);
    }

    /**
     * 处理非法参数异常
     *
     * @param e 非法参数异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        log.warn("非法参数: {}", e.getMessage());
        logExceptionDetails(e, request);
        return Result.error(ResultCode.BAD_REQUEST.getCode(), e.getMessage());
    }

    /**
     * 处理系统异常
     *
     * @param e 系统异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常: ", e);
        logExceptionDetails(e, request);
        return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
    }
}