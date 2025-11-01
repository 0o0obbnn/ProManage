package com.promanage.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.promanage.infrastructure.tracing.TracingWebInterceptor;

import lombok.RequiredArgsConstructor;

/**
 * Web配置类
 *
 * <p>配置Web拦截器，包括分布式追踪拦截器
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-16
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

  private final TracingWebInterceptor tracingWebInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    // 注册分布式追踪拦截器，拦截所有请求
    registry
        .addInterceptor(tracingWebInterceptor)
        .addPathPatterns("/**")
        .excludePathPatterns(
            "/actuator/**", // 健康检查端点
            "/error", // 错误页面
            "/favicon.ico", // 网站图标
            "/swagger-ui/**", // Swagger UI
            "/v3/api-docs/**", // OpenAPI文档
            "/webjars/**" // WebJars资源
            );
  }
}
