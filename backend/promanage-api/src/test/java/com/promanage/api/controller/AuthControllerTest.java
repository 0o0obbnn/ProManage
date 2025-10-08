package com.promanage.api.controller;

import com.promanage.api.dto.response.LoginResponse;
import com.promanage.common.entity.User;
import com.promanage.service.service.IAuthService;
import com.promanage.service.service.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController 集成测试 - 重新设计版本
 * <p>
 * 基于 ProManage API 规范全面测试认证相关API接口
 * </p>
 *
 * @author ProManage Team
 * @version 2.0
 * @since 2025-10-07
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // 禁用安全过滤器进行测试
@DisplayName("AuthController 集成测试 - 重新设计版本")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IAuthService authService;

    @MockBean
    private IUserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        // 初始化测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRealName("测试用户");
        testUser.setPhone("13800138000");
        testUser.setStatus(0); // ACTIVE
    }

    // ==================== 登录功能测试 ====================

    @Test
    @DisplayName("用户登录 - 成功")
    void shouldLoginSuccessfully_whenValidCredentials() throws Exception {
        // given
        when(authService.authenticate(anyString(), anyString())).thenReturn(testUser);

        String requestBody = """
                {
                    "username": "testuser",
                    "password": "Test@123456"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.expiresIn").exists())
                .andExpect(jsonPath("$.data.userInfo.id").value(1))
                .andExpect(jsonPath("$.data.userInfo.username").value("testuser"))
                .andExpect(jsonPath("$.data.userInfo.email").value("test@example.com"));
    }

    @Test
    @DisplayName("用户登录 - 用户名不存在")
    void shouldReturnUnauthorized_whenUsernameNotFound() throws Exception {
        // given
        when(authService.authenticate(anyString(), anyString())).thenReturn(null);

        String requestBody = """
                {
                    "username": "nonexistent",
                    "password": "Test@123456"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    }

    @Test
    @DisplayName("用户登录 - 密码错误")
    void shouldReturnUnauthorized_whenInvalidPassword() throws Exception {
        // given
        when(authService.authenticate(anyString(), anyString())).thenReturn(null);

        String requestBody = """
                {
                    "username": "testuser",
                    "password": "WrongPassword123"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    }

    @Test
    @DisplayName("用户登录 - 请求参数验证失败（用户名为空）")
    void shouldReturnValidationError_whenUsernameIsBlank() throws Exception {
        // given
        String requestBody = """
                {
                    "username": "",
                    "password": "Test@123456"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.validation_errors.username").exists());
    }

    @Test
    @DisplayName("用户登录 - 请求参数验证失败（密码为空）")
    void shouldReturnValidationError_whenPasswordIsBlank() throws Exception {
        // given
        String requestBody = """
                {
                    "username": "testuser",
                    "password": ""
                }
                """;

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.validation_errors.password").exists());
    }

    // ==================== 注册功能测试 ====================

    @Test
    @DisplayName("用户注册 - 成功")
    void shouldRegisterSuccessfully_whenValidData() throws Exception {
        // given
        when(userService.create(any(User.class))).thenReturn(1L);

        String requestBody = """
                {
                    "username": "newuser",
                    "password": "NewUser@123",
                    "confirmPassword": "NewUser@123",
                    "email": "newuser@example.com",
                    "phone": "13900139000",
                    "realName": "新用户"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("注册成功"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("newuser"))
                .andExpect(jsonPath("$.data.email").value("newuser@example.com"));
    }

    @Test
    @DisplayName("用户注册 - 用户名已存在")
    void shouldReturnError_whenUsernameAlreadyExists() throws Exception {
        // given
        when(userService.create(any(User.class))).thenThrow(new RuntimeException("用户名已存在"));

        String requestBody = """
                {
                    "username": "existinguser",
                    "password": "NewUser@123",
                    "confirmPassword": "NewUser@123",
                    "email": "newuser@example.com",
                    "phone": "13900139000"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("用户名已存在"));
    }

    @Test
    @DisplayName("用户注册 - 邮箱格式无效")
    void shouldReturnValidationError_whenInvalidEmailFormat() throws Exception {
        // given
        String requestBody = """
                {
                    "username": "newuser",
                    "password": "NewUser@123",
                    "confirmPassword": "NewUser@123",
                    "email": "invalid-email",
                    "phone": "13900139000"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.validation_errors.email").exists());
    }

    @Test
    @DisplayName("用户注册 - 密码和确认密码不匹配")
    void shouldReturnValidationError_whenPasswordMismatch() throws Exception {
        // given
        String requestBody = """
                {
                    "username": "newuser",
                    "password": "NewUser@123",
                    "confirmPassword": "DifferentPassword123",
                    "email": "newuser@example.com",
                    "phone": "13900139000"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("两次输入的密码不一致"));
    }

    @Test
    @DisplayName("用户注册 - 密码强度不足")
    void shouldReturnValidationError_whenWeakPassword() throws Exception {
        // given
        String requestBody = """
                {
                    "username": "newuser",
                    "password": "123",
                    "confirmPassword": "123",
                    "email": "newuser@example.com",
                    "phone": "13900139000"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.validation_errors.password").exists());
    }

    @Test
    @DisplayName("用户注册 - 手机号格式无效")
    void shouldReturnValidationError_whenInvalidPhoneFormat() throws Exception {
        // given
        String requestBody = """
                {
                    "username": "newuser",
                    "password": "NewUser@123",
                    "confirmPassword": "NewUser@123",
                    "email": "newuser@example.com",
                    "phone": "12345678901"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.validation_errors.phone").exists());
    }

    @Test
    @DisplayName("用户注册 - 用户名包含特殊字符")
    void shouldReturnValidationError_whenUsernameContainsSpecialChars() throws Exception {
        // given
        String requestBody = """
                {
                    "username": "user@name",
                    "password": "NewUser@123",
                    "confirmPassword": "NewUser@123",
                    "email": "user@example.com"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.validation_errors.username").exists());
    }

    // ==================== 密码重置功能测试 ====================

    @Test
    @DisplayName("发送密码重置验证码 - 成功")
    void shouldSendResetCodeSuccessfully() throws Exception {
        // given
        String requestBody = """
                {
                    "email": "user@example.com"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/v1/auth/forgot-password/send-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("验证码已发送到您的邮箱"));
    }

    @Test
    @DisplayName("发送密码重置验证码 - 邮箱不存在")
    void shouldReturnError_whenEmailNotFoundForResetCode() throws Exception {
        // given
        String requestBody = """
                {
                    "email": "nonexistent@example.com"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/v1/auth/forgot-password/send-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("用户不存在"));
    }

    @Test
    @DisplayName("重置密码 - 成功")
    void shouldResetPasswordSuccessfully() throws Exception {
        // given
        String requestBody = """
                {
                    "email": "user@example.com",
                    "verificationCode": "123456",
                    "newPassword": "NewPassword@123",
                    "confirmPassword": "NewPassword@123"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/v1/auth/forgot-password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("密码重置成功"));
    }

    @Test
    @DisplayName("重置密码 - 验证码错误")
    void shouldReturnError_whenInvalidVerificationCode() throws Exception {
        // given
        String requestBody = """
                {
                    "email": "user@example.com",
                    "verificationCode": "000000",
                    "newPassword": "NewPassword@123",
                    "confirmPassword": "NewPassword@123"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/v1/auth/forgot-password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("验证码错误或已过期"));
    }

    // ==================== 修改密码功能测试 ====================

    @Test
    @DisplayName("修改密码 - 成功")
    void shouldChangePasswordSuccessfully() throws Exception {
        // given
        String requestBody = """
                {
                    "oldPassword": "OldPassword@123",
                    "newPassword": "NewPassword@123",
                    "confirmPassword": "NewPassword@123"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("密码修改成功"));
    }

    @Test
    @DisplayName("修改密码 - 旧密码错误")
    void shouldReturnError_whenOldPasswordIncorrect() throws Exception {
        // given
        String requestBody = """
                {
                    "oldPassword": "WrongOldPassword",
                    "newPassword": "NewPassword@123",
                    "confirmPassword": "NewPassword@123"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("旧密码错误"));
    }

    // ==================== 密码强度检查功能测试 ====================

    @Test
    @DisplayName("检查密码强度 - 强密码")
    void shouldReturnStrongPasswordLevel() throws Exception {
        // given
        String requestBody = """
                {
                    "password": "VeryStrong@Password123!"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/v1/auth/check-password-strength")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.level").value("VERY_STRONG"))
                .andExpect(jsonPath("$.data.score").value(90))
                .andExpect(jsonPath("$.data.suggestions").isArray());
    }

    @Test
    @DisplayName("检查密码强度 - 弱密码")
    void shouldReturnWeakPasswordLevel() throws Exception {
        // given
        String requestBody = """
                {
                    "password": "123"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/v1/auth/check-password-strength")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.level").value("WEAK"))
                .andExpect(jsonPath("$.data.score").value(20))
                .andExpect(jsonPath("$.data.suggestions").isArray());
    }

    // ==================== 刷新令牌功能测试 ====================

    @Test
    @DisplayName("刷新访问令牌 - 成功")
    void shouldRefreshTokenSuccessfully() throws Exception {
        // given
        String requestBody = """
                {
                    "refresh_token": "valid_refresh_token_here"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").exists())
                .andExpect(jsonPath("$.expires_in").exists());
    }

    // ==================== 登出功能测试 ====================

    @Test
    @DisplayName("用户登出 - 成功")
    void shouldLogoutSuccessfully() throws Exception {
        // when & then
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully logged out"));
    }

    // ==================== 获取当前用户信息测试 ====================

    @Test
    @DisplayName("获取当前用户信息 - 成功")
    void shouldGetCurrentUserSuccessfully() throws Exception {
        // when & then
        mockMvc.perform(post("/api/v1/auth/me"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.username").exists())
                .andExpect(jsonPath("$.data.email").exists());
    }
}
