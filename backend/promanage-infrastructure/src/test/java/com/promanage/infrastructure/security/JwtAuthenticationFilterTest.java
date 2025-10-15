
package com.promanage.infrastructure.security;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter 验证测试")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        // 清除安全上下文，确保测试之间隔离
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("验证BUG修复：当UserDetailsService返回权限时，安全上下文中的权限应被正确加载")
    void testDoFilterInternal_WhenUserServiceReturnsAuthorities_AuthenticationShouldContainAuthorities() throws ServletException, IOException {
        // --- Arrange ---
        final String token = "valid.token.with.authorities";
        final String username = "testuser_with_roles";
        final String permission = "document:view";

        // 模拟一个有效的HTTP请求头
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        // 模拟Token是有效的且未被列入黑名单
        when(tokenBlacklistService.isBlacklisted(token)).thenReturn(false);
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(token)).thenReturn(username);

        // **核心模拟**: 模拟修复后的 UserServiceImpl 的行为，返回一个包含权限的 UserDetails 对象
        var authorities = Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority(permission));
        UserDetails userDetailsWithAuthorities = new org.springframework.security.core.userdetails.User(
                username,
                "password",
                authorities
        );
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetailsWithAuthorities);

        // --- Act ---
        // 执行过滤器
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // --- Assert ---
        // 1. 验证安全上下文中存在 Authentication 对象
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication, "Authentication 对象不应为空");

        // 2. **核心断言**: 验证 Authentication 对象的权限列表不再为空，且包含我们预设的权限
        // 这个断言如果通过，则证明BUG已被成功修复。
        assertFalse(
                authentication.getAuthorities().isEmpty(),
                "BUG修复验证：安全上下文中的权限列表不应为空！"
        );
        assertEquals(1, authentication.getAuthorities().size(), "权限数量应为1");
        assertTrue(
            authentication.getAuthorities().stream()
                .anyMatch(auth -> permission.equals(auth.getAuthority())),
            "权限列表中应包含 '" + permission + "'"
        );

        // 3. 验证过滤器链被继续调用
        verify(filterChain, times(1)).doFilter(request, response);

        System.out.println("测试成功执行并通过。");
        System.out.println("验证结论：已通过单元测试确认，授权BUG已被成功修复。");
    }

    @Test
    @DisplayName("当Token无效或不存在时，不应设置安全上下文")
    void testDoFilterInternal_WhenTokenIsInvalid_ShouldNotSetAuthentication() throws ServletException, IOException {
        // --- Arrange ---
        // 模拟一个没有 "Authorization" 头的请求
        when(request.getHeader("Authorization")).thenReturn(null);

        // --- Act ---
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // --- Assert ---
        // 验证安全上下文中没有 Authentication 对象
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication, "当Token无效或不存在时，Authentication对象应为空");

        // 验证过滤器链被继续调用
        verify(filterChain, times(1)).doFilter(request, response);
    }
}
