package com.promanage.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Web MVC 配置类
 * <p>
 * 配置Spring MVC的相关设置，包括：
 * - 静态资源处理
 * - CORS跨域配置
 * - 消息转换器配置
 * - 日期时间格式化
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-09-30
 */
@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 配置静态资源处理
     * <p>
     * 配置Swagger UI等静态资源的访问路径
     * </p>
     *
     * @param registry ResourceHandlerRegistry 资源处理器注册表
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("Configuring static resource handlers");
        
        // Swagger UI 静态资源
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/");
        
        // Swagger API 文档
        registry.addResourceHandler("/v3/api-docs/**")
                .addResourceLocations("classpath:/META-INF/resources/");
        
        // Druid 监控页面静态资源
        registry.addResourceHandler("/druid/**")
                .addResourceLocations("classpath:/META-INF/resources/");
        
        log.info("Static resource handlers configured successfully");
    }

    /**
     * 配置CORS跨域
     * <p>
     * 配置跨域资源共享策略
     * </p>
     *
     * @param registry CorsRegistry CORS注册表
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        log.info("Configuring CORS mappings");
        
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
        
        log.info("CORS mappings configured successfully");
    }

    /**
     * 配置消息转换器
     * <p>
     * 配置JSON消息转换器，处理日期时间格式化
     * </p>
     *
     * @param converters 消息转换器列表
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("Configuring message converters");
        
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        
        // 配置日期时间格式化
        converter.getObjectMapper().setDateFormat(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        
        converters.add(converter);
        
        log.info("Message converters configured successfully");
    }
}