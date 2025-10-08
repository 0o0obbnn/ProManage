package com.promanage.infrastructure.config;

import com.promanage.infrastructure.interceptor.ApiLoggingInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置
 * <p>
 * 配置拦截器、视图解析器等Web相关组件
 * </p>
 *
 * @author ProManage Team
 * @date 2025-10-08
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final ApiLoggingInterceptor apiLoggingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiLoggingInterceptor)
                .addPathPatterns("/api/**") // 拦截所有API请求
                .excludePathPatterns(
                        "/api/auth/login",     // 排除登录接口
                        "/api/auth/register",  // 排除注册接口
                        "/api/auth/forgot-password/send-code", // 排除忘记密码发送验证码
                        "/api/auth/forgot-password/reset",     // 排除密码重置
                        "/api/health",         // 排除健康检查
                        "/api/actuator/**"     // 排除监控端点
                );
    }
}