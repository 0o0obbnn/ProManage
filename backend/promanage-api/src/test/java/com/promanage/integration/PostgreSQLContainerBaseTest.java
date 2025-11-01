package com.promanage.integration;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Testcontainers集成测试基类
 *
 * <p>提供基于Testcontainers的PostgreSQL数据库集成测试基础配置
 *
 * @author ProManage Team
 * @date 2025-10-12
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
public abstract class PostgreSQLContainerBaseTest {

  @SuppressWarnings("resource") // Container lifecycle managed by @Testcontainers annotation
  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
          .withDatabaseName("promanage_test")
          .withUsername("test")
          .withPassword("test")
          .withReuse(true);

  @DynamicPropertySource
  static void postgresProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);

    // Flyway配置
    registry.add("spring.flyway.url", postgres::getJdbcUrl);
    registry.add("spring.flyway.user", postgres::getUsername);
    registry.add("spring.flyway.password", postgres::getPassword);

    // Redis配置（使用嵌入式Redis进行测试）
    registry.add("spring.data.redis.host", () -> "localhost");
    registry.add("spring.data.redis.port", () -> "6370");
    registry.add("spring.data.redis.database", () -> "1");
  }

  @BeforeAll
  static void setup() {
    // 确保容器启动
    postgres.start();
  }
}
