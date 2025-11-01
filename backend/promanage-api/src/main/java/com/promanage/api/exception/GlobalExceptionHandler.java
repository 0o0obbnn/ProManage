package com.promanage.api.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.promanage.common.domain.Result;
import com.promanage.common.domain.ResultCode;
import com.promanage.common.exception.BusinessException;
import com.promanage.common.util.IpUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

/**
 * 全局异常处理器
 *
 * <p>统一处理应用中的各种异常，返回标准化的错误响应
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  private void logExceptionDetails(Exception e, HttpServletRequest request, boolean isClientError) {
    String clientIp = request != null ? IpUtils.getClientIpAddress(request) : "unknown";
    String userAgent = request != null ? request.getHeader("User-Agent") : "unknown";
    String requestUrl = request != null ? request.getRequestURL().toString() : "unknown";
    String method = request != null ? request.getMethod() : "unknown";

    // 根据异常类型确定日志级别
    if (isClientError) {
      log.warn(
          "Client Error - URL: {}, Method: {}, IP: {}, UserAgent: {}, Exception: {}",
          requestUrl,
          method,
          clientIp,
          userAgent,
          e.getMessage());
    } else {
      log.error(
          "Server Error - URL: {}, Method: {}, IP: {}, UserAgent: {}, Exception: ",
          requestUrl,
          method,
          clientIp,
          userAgent,
          e);
    }
  }

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<Result<Void>> handleBusinessException(
      BusinessException e, HttpServletRequest request) {
    boolean isClientError = e.getCode() >= 400 && e.getCode() < 500;
    logExceptionDetails(e, request, isClientError);

    HttpStatus status = HttpStatus.resolve(e.getCode());
    if (status == null
        || status.is1xxInformational()
        || status.is2xxSuccessful()
        || status.is3xxRedirection()) {
      status = HttpStatus.BAD_REQUEST;
    }

    Result<Void> body = Result.error(e.getCode(), e.getMessage());
    return ResponseEntity.status(status).body(body);
  }

  /**
   * 统一处理参数验证异常
   *
   * <p>处理MethodArgumentNotValidException和BindException，避免代码重复
   */
  @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Result<Map<String, String>> handleValidationException(
      Exception e, HttpServletRequest request) {
    logExceptionDetails(e, request, true);

    Map<String, String> errors = new HashMap<>();

    if (e instanceof MethodArgumentNotValidException methodArgumentNotValidException) {
      methodArgumentNotValidException
          .getBindingResult()
          .getAllErrors()
          .forEach(
              error -> {
                String fieldName =
                    error instanceof FieldError fieldError
                        ? fieldError.getField()
                        : error.getObjectName();
                String errorMessage = error.getDefaultMessage();
                errors.put(fieldName, errorMessage);
              });
    } else if (e instanceof BindException bindException) {
      bindException
          .getBindingResult()
          .getAllErrors()
          .forEach(
              error -> {
                String fieldName =
                    error instanceof FieldError fieldError
                        ? fieldError.getField()
                        : error.getObjectName();
                String errorMessage = error.getDefaultMessage();
                errors.put(fieldName, errorMessage);
              });
    }

    Result<Map<String, String>> result = Result.error(ResultCode.PARAM_ERROR.getCode(), "参数验证失败");
    result.setData(errors);
    return result;
  }

  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Result<Map<String, String>> handleConstraintViolationException(
      ConstraintViolationException e, HttpServletRequest request) {
    logExceptionDetails(e, request, true);

    Map<String, String> errors = new HashMap<>();
    Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
    for (ConstraintViolation<?> violation : violations) {
      String fieldName = violation.getPropertyPath().toString();
      String errorMessage = violation.getMessage();
      errors.put(fieldName, errorMessage);
    }

    Result<Map<String, String>> result = Result.error(ResultCode.PARAM_ERROR.getCode(), "参数约束验证失败");
    result.setData(errors);
    return result;
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Result<Void> handleMethodArgumentTypeMismatchException(
      MethodArgumentTypeMismatchException e, HttpServletRequest request) {
    String message =
        String.format(
            "参数类型错误: %s 应为 %s 类型",
            e.getName(), e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "未知");
    logExceptionDetails(e, request, true);
    return Result.error(ResultCode.BAD_REQUEST.getCode(), message);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Result<Void> handleIllegalArgumentException(
      IllegalArgumentException e, HttpServletRequest request) {
    logExceptionDetails(e, request, true);
    return Result.error(ResultCode.BAD_REQUEST.getCode(), e.getMessage());
  }

  @ExceptionHandler(AccessDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public Result<Void> handleAccessDeniedException(
      AccessDeniedException e, HttpServletRequest request) {
    logExceptionDetails(e, request, true);
    return Result.error(ResultCode.FORBIDDEN);
  }

  @ExceptionHandler(BadCredentialsException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public Result<Void> handleBadCredentialsException(
      BadCredentialsException e, HttpServletRequest request) {
    logExceptionDetails(e, request, true);
    return Result.error(ResultCode.UNAUTHORIZED);
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public Result<Void> handleException(Exception e, HttpServletRequest request) {
    logExceptionDetails(e, request, false);
    return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
  }
}
