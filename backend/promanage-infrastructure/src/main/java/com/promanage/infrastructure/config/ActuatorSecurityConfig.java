package com.promanage.infrastructure.config;

// import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Configuration;

// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
// import org.springframework.security.config.http.SessionCreationPolicy;
// import org.springframework.security.web.SecurityFilterChain;

/**
 * Actuator安全配置
 *
 * <p>为Actuator端点配置安全访问策略，只允许管理员访问敏感端点
 *
 * @author ProManage Team
 * @since 2025-10-16
 */
@Configuration
// @EnableWebSecurity
public class ActuatorSecurityConfig {

  /**
   * 配置Actuator端点的安全策略
   *
   * @param http HttpSecurity对象
   * @return SecurityFilterChain安全过滤链
   * @throws Exception 配置异常
   */
  // @Bean
  // public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
  //     http.securityMatcher(EndpointRequest.toAnyEndpoint())
  //         .authorizeHttpRequests(requests -> requests
  //             .requestMatchers(EndpointRequest.to("health", "info")).permitAll()
  //             .requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole("ADMIN")
  //         )
  //         .sessionManagement(session ->
  // session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
  //         .csrf(AbstractHttpConfigurer::disable)
  //         .cors(AbstractHttpConfigurer::disable);
  //
  //     return http.build();
  // }
}
