package com.promanage.api;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 测试安全配置
 * 
 * <p>为测试环境提供简化的安全配置，避免复杂的JWT验证和外部依赖
 * 
 * @author ProManage Team
 * @since 2025-10-22
 */
@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {

    /**
     * 测试环境的安全过滤器链
     * 简化配置，允许大部分请求通过
     */
    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // 允许所有测试请求
                .requestMatchers("/api/test/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                // 管理员端点需要ADMIN角色
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // 其他请求需要认证
                .anyRequest().authenticated()
            )
            .httpBasic(httpBasic -> {}) // 启用HTTP Basic认证用于测试
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.disable()) // 允许H2控制台
            )
            .build();
    }

    /**
     * 测试用户详情服务
     * 提供内存中的测试用户
     */
    @Bean
    @Primary
    public UserDetailsService testUserDetailsService() {
        UserDetails user = User.builder()
            .username("testuser")
            .password(passwordEncoder().encode("testpass"))
            .roles("USER")
            .build();

        UserDetails admin = User.builder()
            .username("admin")
            .password(passwordEncoder().encode("adminpass"))
            .roles("ADMIN", "USER")
            .build();

        UserDetails manager = User.builder()
            .username("manager")
            .password(passwordEncoder().encode("managerpass"))
            .roles("MANAGER", "USER")
            .build();

        return new InMemoryUserDetailsManager(user, admin, manager);
    }

    /**
     * 密码编码器
     */
    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}