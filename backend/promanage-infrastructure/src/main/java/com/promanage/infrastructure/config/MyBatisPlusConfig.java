package com.promanage.infrastructure.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * MyBatis Plus 配置类 (基本配置)
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-09-30
 */
@Slf4j
@Configuration
@EnableTransactionManagement
public class MyBatisPlusConfig {

    /**
     * MyBatis Plus 拦截器配置 (基本配置)
     *
     * @return MybatisPlusInterceptor 拦截器实例
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        log.info("Initializing MyBatis Plus interceptor (basic configuration)");
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        log.info("MyBatis Plus interceptor initialized successfully");
        return interceptor;
    }
}