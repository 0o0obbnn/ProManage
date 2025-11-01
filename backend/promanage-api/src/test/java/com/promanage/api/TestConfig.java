package com.promanage.api;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/** 测试配置类 用于隔离测试环境，避免加载不必要的组件 */
@TestConfiguration
@EnableAutoConfiguration
@ComponentScan(
    basePackages = "com.promanage",
    excludeFilters = {
      @ComponentScan.Filter(
          type = FilterType.REGEX,
          pattern = "com\\.promanage\\.api\\.controller\\..*")
    })
public class TestConfig {}
