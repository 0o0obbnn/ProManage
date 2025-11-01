package com.promanage.infrastructure.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * Elasticsearch Configuration
 *
 * <p>Only enabled when spring.elasticsearch.enabled=true
 *
 * @author ProManage Team
 * @version 1.1
 * @since 2025-10-30
 */
@Configuration
@ConditionalOnProperty(
    name = "spring.elasticsearch.enabled",
    havingValue = "true",
    matchIfMissing = false)
// TODO: [AUDIT-C-002] Move search repositories to the infrastructure layer and update this path
@EnableElasticsearchRepositories(basePackages = "com.promanage.service.repository.search")
public class ElasticsearchConfig extends ElasticsearchConfiguration {

  @Value("${spring.elasticsearch.uris:localhost:9200}")
  private String elasticsearchUris;

  @Value("${spring.elasticsearch.username:}")
  private String username;

  @Value("${spring.elasticsearch.password:}")
  private String password;

  @Override
  public ClientConfiguration clientConfiguration() {
    var builder =
        ClientConfiguration.builder()
            .connectedTo(elasticsearchUris)
            .withConnectTimeout(Duration.ofSeconds(10)) // Increased connect timeout
            .withSocketTimeout(Duration.ofSeconds(30)); // Increased socket timeout

    // --- Production Security Configuration ---
    // Uncomment and configure the following for production environments
    /*
    if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
        builder.withBasicAuth(username, password);
    }
    */

    // --- SSL/TLS Configuration ---
    // builder.usingSsl();

    return builder.build();
  }
}
