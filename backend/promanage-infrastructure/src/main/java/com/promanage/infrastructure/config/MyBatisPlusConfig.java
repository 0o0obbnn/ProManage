package com.promanage.infrastructure.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * MyBatis Plus Configuration
 *
 * @author ProManage Team
 * @version 1.1
 * @since 2025-10-30
 */
@Slf4j
@Configuration
@EnableTransactionManagement
public class MyBatisPlusConfig {

  /**
   * Configures the MyBatis Plus interceptor, including the pagination plugin.
   *
   * @return MybatisPlusInterceptor instance
   */
  @Bean
  public MybatisPlusInterceptor mybatisPlusInterceptor() {
    log.info("Initializing MyBatis Plus interceptor");
    MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

    // Add the pagination interceptor
    interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
    log.info("PaginationInnerInterceptor added to MyBatis Plus");

    log.info("MyBatis Plus interceptor initialized successfully");
    return interceptor;
  }
}
