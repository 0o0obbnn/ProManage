package com.promanage.api.controller;

import com.promanage.api.dto.request.LoginRequest;
import com.promanage.api.dto.request.RegisterRequest;
import com.promanage.api.dto.response.LoginResponse;
import com.promanage.common.domain.Result;
import com.promanage.service.entity.User;
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
 * AuthController 集成测试
 * <p>
 * 测试认证相关API接口
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-02
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security for testing
@DisplayName("AuthController 集成测试")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IAuthService authService;

    @MockBean
    private IUserService userService;

    private User testUser;
    private LoginResponse loginResponse;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        loginResponse = LoginResponse.builder()
                .token("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
                .refreshToken("refresh_token_here")
                .tokenType("Bearer")
                .expiresIn(86400L)
                .build();
    }

    @Test
    @DisplayName("should login successfully with valid credentials")
    void shouldLogin_whenValidCredentials() throws Exception {
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
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("should return validation error when username is blank")
    void shouldReturnValidationError_whenUsernameBlank() throws Exception {
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
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("should return validation error when password is too short")
    void shouldReturnValidationError_whenPasswordTooShort() throws Exception {
        // given
        String requestBody = """
                {
                    "username": "testuser",
                    "password": "123"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("should register successfully with valid data")
    void shouldRegister_whenValidData() throws Exception {
        // given
        when(userService.create(any(User.class))).thenReturn(1L);

        String requestBody = """
                {
                    "username": "newuser",
                    "password": "NewUser@123",
                    "confirmPassword": "NewUser@123",
                    "email": "newuser@example.com",
                    "phone": "13900139000",
                    "realName": "New User"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("注册成功"));
    }

    @Test
    @DisplayName("should return validation error when email format is invalid")
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
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("should return validation error when username contains special characters")
    void shouldReturnValidationError_whenUsernameHasSpecialChars() throws Exception {
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
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("should return validation error when phone format is invalid")
    void shouldReturnValidationError_whenInvalidPhoneFormat() throws Exception {
        // given
        String requestBody = """
                {
                    "username": "newuser",
                    "password": "NewUser@123",
                    "confirmPassword": "NewUser@123",
                    "email": "user@example.com",
                    "phone": "12345678901"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }
}
