package com.promanage.api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.promanage.api.dto.request.LoginRequest;
import com.promanage.api.dto.request.CreatePermissionRequest;
import com.promanage.api.dto.request.CreateTestCaseRequest;
import com.promanage.api.dto.request.UserCreateRequest;
import com.promanage.common.domain.Result;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * API控制器集成测试
 * <p>
 * 测试各个控制器之间的交互和完整的API流程
 * </p>
 *
 * @author ProManage Team
 * @date 2025-10-08
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("API控制器集成测试")
class ControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String authToken;

    @Test
    @DisplayName("完整API流程测试 - 用户登录到权限管理")
    void testCompleteApiFlow() throws Exception {
        // 1. 用户注册
        UserCreateRequest registerRequest = new UserCreateRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("password123");
        registerRequest.setEmail("test@example.com");
        registerRequest.setRealName("测试用户");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("testuser"));

        // 2. 用户登录
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").exists())
                .andReturn();

        // 提取认证令牌
        String responseContent = loginResult.getResponse().getContentAsString();
        Result<?> loginResultData = objectMapper.readValue(responseContent, Result.class);
        Map<String, Object> dataMap = objectMapper.convertValue(loginResultData.getData(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        authToken = (String) dataMap.get("token");

        // 3. 创建权限
        CreatePermissionRequest permissionRequest = new CreatePermissionRequest();
        permissionRequest.setPermissionName("文档管理");
        permissionRequest.setPermissionCode("document:manage");
        permissionRequest.setType("api");
        permissionRequest.setUrl("/api/documents");
        permissionRequest.setMethod("POST");

        mockMvc.perform(post("/api/v1/permissions")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(permissionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 4. 获取权限列表
        mockMvc.perform(get("/api/v1/permissions")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());

        // 5. 获取当前用户信息
        mockMvc.perform(get("/api/v1/auth/me")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("testuser"));

        // 6. 用户登出
        mockMvc.perform(post("/api/v1/auth/logout")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试用例管理API流程测试")
    void testTestCaseManagementApiFlow() throws Exception {
        // 1. 先登录获取令牌
        authToken = loginAndGetToken("testcaseuser", "password123");

        // 2. 创建测试用例
        CreateTestCaseRequest createRequest = new CreateTestCaseRequest();
        createRequest.setTitle("登录功能测试");
        createRequest.setDescription("验证用户登录功能");
        createRequest.setPreconditions("用户已注册");
        createRequest.setSteps("1. 打开登录页面\n2. 输入用户名密码\n3. 点击登录按钮");
        createRequest.setExpectedResult("登录成功，跳转到主页");
        createRequest.setType("功能测试");
        createRequest.setPriority(1);
        // 注释掉setProjectId方法调用，因为CreateTestCaseRequest可能没有这个方法
        // createRequest.setProjectId(1L);

        MvcResult createResult = mockMvc.perform(post("/api/v1/test-cases")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        // 提取测试用例ID
        String createResponseContent = createResult.getResponse().getContentAsString();
        Result<?> createResultData = objectMapper.readValue(createResponseContent, Result.class);
        Map<String, Object> createDataMap = objectMapper.convertValue(createResultData.getData(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        Long testCaseId = ((Number) createDataMap.get("id")).longValue();

        // 3. 获取测试用例详情
        mockMvc.perform(get("/api/v1/test-cases/" + testCaseId)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("登录功能测试"));

        // 4. 获取项目测试用例列表
        mockMvc.perform(get("/api/v1/test-cases")
                .header("Authorization", "Bearer " + authToken)
                .param("projectId", "1")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());

        // 5. 执行测试用例
        mockMvc.perform(post("/api/v1/test-cases/" + testCaseId + "/execute")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"actualResult\":\"登录成功\",\"status\":1,\"executionNotes\":\"测试通过\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 6. 获取测试用例统计
        mockMvc.perform(get("/api/v1/test-cases/statistics")
                .header("Authorization", "Bearer " + authToken)
                .param("projectId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalCount").exists());
    }

    @Test
    @DisplayName("用户管理API流程测试")
    void testUserManagementApiFlow() throws Exception {
        // 1. 使用管理员账户登录
        authToken = loginAndGetToken("admin", "admin123");

        // 2. 创建新用户
        UserCreateRequest createUserRequest = new UserCreateRequest();
        createUserRequest.setUsername("newuser");
        createUserRequest.setPassword("password123");
        createUserRequest.setEmail("newuser@example.com");
        createUserRequest.setRealName("新用户");
        createUserRequest.setPosition("测试工程师");

        MvcResult createResult = mockMvc.perform(post("/api/v1/users")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        // 提取用户ID
        String createResponseContent = createResult.getResponse().getContentAsString();
        Result<?> createResultData = objectMapper.readValue(createResponseContent, Result.class);
        Map<String, Object> userDataMap = objectMapper.convertValue(createResultData.getData(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        Long userId = ((Number) userDataMap.get("id")).longValue();

        // 3. 获取用户详情
        mockMvc.perform(get("/api/v1/users/" + userId)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("newuser"));

        // 4. 获取用户列表
        mockMvc.perform(get("/api/v1/users")
                .header("Authorization", "Bearer " + authToken)
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());

        // 5. 更新用户状态
        mockMvc.perform(put("/api/v1/users/" + userId + "/status")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 6. 分配角色
        mockMvc.perform(post("/api/v1/users/" + userId + "/roles")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roleIds\":[1,2]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("权限验证测试")
    void testPermissionValidation() throws Exception {
        // 1. 使用普通用户登录
        authToken = loginAndGetToken("normaluser", "password123");

        // 2. 尝试访问管理员权限的API（应该失败）
        mockMvc.perform(post("/api/v1/users")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"test\",\"password\":\"test\"}"))
                .andExpect(status().isForbidden());

        // 3. 尝试访问无权限的API（应该失败）
        mockMvc.perform(delete("/api/v1/users/1")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isForbidden());

        // 4. 访问有权限的API（应该成功）
        mockMvc.perform(get("/api/v1/auth/me")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 登录并获取认证令牌
     */
    private String loginAndGetToken(String username, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").exists())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        Result<?> loginResult = objectMapper.readValue(responseContent, Result.class);
        Map<String, Object> tokenDataMap = objectMapper.convertValue(loginResult.getData(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        return (String) tokenDataMap.get("token");
    }
}
