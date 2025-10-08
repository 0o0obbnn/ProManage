package com.promanage.common.wrapper;

import com.promanage.common.domain.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * API响应包装器
 * <p>
 * 统一包装所有REST API的响应，确保返回格式一致
 * </p>
 *
 * @author ProManage Team
 * @date 2025-10-08
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.promanage.api.controller")
public class ApiResponseWrapper implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 如果返回类型已经是Result，则不需要再次包装
        return !returnType.getParameterType().equals(Result.class);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        // 如果返回值已经是Result类型，直接返回
        if (body instanceof Result) {
            return body;
        }

        // 如果返回值是null，返回成功结果
        if (body == null) {
            return Result.success();
        }

        // 包装返回值
        return Result.success(body);
    }
}