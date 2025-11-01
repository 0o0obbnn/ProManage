package com.promanage.service.config;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;

/**
 * 测试配置类
 * 
 * @author ProManage Team
 * @date 2025-10-22
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = {
    "com.promanage.service",
    "com.promanage.common",
    "com.promanage.infrastructure",
    "com.promanage.domain"
})
public class TestConfiguration {
    
    // UserMapper现在由domain模块提供，不需要在这里定义
    
    /**
     * 为测试提供ObjectMapper Bean
     */
    @Bean
    @Primary
    public com.fasterxml.jackson.databind.ObjectMapper objectMapper() {
        return new com.fasterxml.jackson.databind.ObjectMapper();
    }
}