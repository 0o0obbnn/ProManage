package com.promanage.infrastructure.config;

// import io.micrometer.tracing.Tracer;
// import io.micrometer.tracing.exporter.SpanExporter;
// import io.micrometer.tracing.reporter.wavefront.WavefrontSpanHandler;
// import io.micrometer.tracing.zipkin.ZipkinSpanExporter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

/**
 * 分布式追踪配置类
 *
 * <p>配置Micrometer Tracing与Zipkin集成，实现分布式追踪功能 使用Spring Boot自动配置，只需添加相应的依赖即可
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-16
 */
@Slf4j
@Configuration
// @ConditionalOnProperty(name = "promanage.tracing.enabled", havingValue = "true", matchIfMissing =
// true)
public class TracingConfig {

  @Value("${promanage.tracing.zipkin.endpoint:http://localhost:9411/api/v2/spans}")
  private String zipkinEndpoint;

  /**
   * 配置Zipkin Span导出器
   *
   * @return Zipkin Span导出器
   */
  // @Bean
  // public SpanExporter zipkinSpanExporter() {
  //     return ZipkinSpanExporter.builder()
  //             .uri(zipkinEndpoint)
  //             .build();
  // }
}
