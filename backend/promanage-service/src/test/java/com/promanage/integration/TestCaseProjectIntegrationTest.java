package com.promanage.integration;

import com.promanage.common.entity.User;
import com.promanage.service.entity.TestCase;
import com.promanage.service.service.ITestCaseService;
import com.promanage.service.service.IUserService;
import com.promanage.common.domain.PageResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
// 修复SpyBean弃用问题
// import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 测试用例与项目关联集成测试
 * <p>
 * 测试测试用例与项目、用户之间的关联关系
 * </p>
 *
 * @author ProManage Team
 * @date 2025-10-08
 */
@DisplayName("测试用例与项目关联集成测试")
class TestCaseProjectIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ITestCaseService testCaseService;

    @Autowired
    private IUserService userService;

    // passwordEncoder 已在 UserServiceImpl 中自动注入，此处不需要

    @Test
    @DisplayName("测试用例完整生命周期测试")
    void testTestCaseFullLifecycle() {
        // 1. 创建用户（创建者和执行者）
        User creator = new User();
        creator.setUsername("creator");
        creator.setPassword("password123");
        creator.setEmail("creator@example.com");
        creator.setRealName("创建者");

        User assignee = new User();
        assignee.setUsername("assignee");
        assignee.setPassword("password123");
        assignee.setEmail("assignee@example.com");
        assignee.setRealName("执行者");

        Long creatorId = userService.create(creator);
        Long assigneeId = userService.create(assignee);

        // 2. 创建测试用例
        TestCase testCase = new TestCase();
        testCase.setTitle("登录功能测试");
        testCase.setDescription("验证用户登录功能是否正常");
        testCase.setPreconditions("用户已注册");
        testCase.setSteps("1. 打开登录页面\n2. 输入用户名密码\n3. 点击登录按钮");
        testCase.setExpectedResult("登录成功，跳转到主页");
        testCase.setType("FUNCTIONAL");
        testCase.setPriority(1);
        testCase.setProjectId(1L);
        testCase.setAssigneeId(assigneeId);
        testCase.setCreatorId(creatorId);
        testCase.setStatus(0); // 草稿状态

        Long testCaseId = testCaseService.createTestCase(testCase);
        assertNotNull(testCaseId);

        // 3. 查询测试用例详情
        TestCase detailResponse = testCaseService.getTestCaseById(testCaseId);
        
        assertNotNull(detailResponse);
        assertEquals(testCase.getTitle(), detailResponse.getTitle());
        assertEquals(testCase.getProjectId(), detailResponse.getProjectId());
        assertEquals(testCase.getAssigneeId(), detailResponse.getAssigneeId());

        // 4. 查询项目的测试用例列表
        PageResult<TestCase> projectTestCases = testCaseService.listTestCases(1L, 1, 10, 
            null, null, null, null, null, null, null, null);
        
        assertNotNull(projectTestCases);
        assertFalse(projectTestCases.getList().isEmpty());
        assertTrue(projectTestCases.getList().stream().anyMatch(tc -> tc.getId().equals(testCaseId)));

        // 5. 执行测试用例
        testCaseService.executeTestCase(
            testCaseId, 
            "PASS", // 通过
            "登录成功，跳转到主页",
            null, // 没有失败原因
            5, // 实际执行时间5分钟
            "Windows 10, Chrome 90",
            "测试通过",
            new String[]{"https://example.com/screenshot.png"},
            creatorId
        );

        // 6. 验证执行结果
        TestCase updatedResponse = testCaseService.getTestCaseById(testCaseId);
        
        assertEquals("登录成功，跳转到主页", updatedResponse.getActualResult());
        assertNotNull(updatedResponse.getLastExecutedAt());

        // 7. 获取测试用例统计信息
        ITestCaseService.TestCaseStatistics statistics = testCaseService.getTestCaseStatistics(1L);
        assertNotNull(statistics);
        assertTrue(statistics.getTotalCount() > 0);
    }

    @Test
    @DisplayName("测试用例复制测试")
    void testTestCaseCopy() {
        // 1. 创建用户
        User creator = new User();
        creator.setUsername("creator2");
        creator.setPassword("password123");
        creator.setEmail("creator2@example.com");
        creator.setRealName("创建者2");

        Long creatorId = userService.create(creator);

        // 2. 创建原始测试用例
        TestCase originalTestCase = new TestCase();
        originalTestCase.setTitle("原始测试用例");
        originalTestCase.setDescription("原始描述");
        originalTestCase.setSteps("原始步骤");
        originalTestCase.setExpectedResult("原始预期结果");
        originalTestCase.setType("FUNCTIONAL");
        originalTestCase.setPriority(1);
        originalTestCase.setProjectId(1L);
        originalTestCase.setCreatorId(creatorId);
        originalTestCase.setStatus(0); // 草稿状态

        Long originalTestCaseId = testCaseService.createTestCase(originalTestCase);

        // 3. 复制测试用例
        String newTitle = "复制的测试用例";
        Long copiedTestCaseId = testCaseService.copyTestCase(originalTestCaseId, newTitle, creatorId);

        // 4. 验证复制的测试用例
        assertNotNull(copiedTestCaseId);
        assertNotEquals(originalTestCaseId, copiedTestCaseId);

        TestCase copiedTestCase = testCaseService.getTestCaseById(copiedTestCaseId);
        TestCase originalTestCaseRetrieved = testCaseService.getTestCaseById(originalTestCaseId);

        assertEquals(newTitle, copiedTestCase.getTitle());
        assertEquals(originalTestCase.getDescription(), copiedTestCase.getDescription());
        assertEquals(originalTestCase.getSteps(), copiedTestCase.getSteps());
        assertEquals(originalTestCase.getExpectedResult(), copiedTestCase.getExpectedResult());
        assertEquals(originalTestCase.getType(), copiedTestCase.getType());
        assertEquals(originalTestCase.getPriority(), copiedTestCase.getPriority());
        assertEquals(originalTestCase.getProjectId(), copiedTestCase.getProjectId());

        // 5. 验证原始测试用例未受影响
        assertEquals("原始测试用例", originalTestCaseRetrieved.getTitle());
    }

    @Test
    @DisplayName("用户测试用例关联测试")
    void testUserTestCaseAssociation() {
        // 1. 创建用户
        User user = new User();
        user.setUsername("tester");
        user.setPassword("password123");
        user.setEmail("tester@example.com");
        user.setRealName("测试员");

        Long userId = userService.create(user);

        // 2. 创建测试用例，由该用户创建和分配
        TestCase testCase = new TestCase();
        testCase.setTitle("我创建的测试用例");
        testCase.setDescription("创建的测试用例");
        testCase.setSteps("步骤");
        testCase.setExpectedResult("结果");
        testCase.setType("FUNCTIONAL");
        testCase.setPriority(1);
        testCase.setProjectId(1L);
        testCase.setAssigneeId(userId); // 分配给自己
        testCase.setCreatorId(userId); // 自己创建
        testCase.setStatus(0); // 草稿状态

        Long createdTestCaseId = testCaseService.createTestCase(testCase);

        // 3. 获取用户负责的测试用例列表
        PageResult<TestCase> assignedTestCases = testCaseService.listTestCasesByAssignee(userId, 1, 10, null);
        assertNotNull(assignedTestCases);
        assertFalse(assignedTestCases.getList().isEmpty());

        // 4. 获取用户创建的测试用例列表
        PageResult<TestCase> createdTestCases = testCaseService.listTestCasesByCreator(userId, 1, 10, null);
        assertNotNull(createdTestCases);
        assertFalse(createdTestCases.getList().isEmpty());

        // 5. 验证测试用例在两个列表中都存在
        List<TestCase> assignedList = assignedTestCases.getList();
        List<TestCase> createdList = createdTestCases.getList();
        
        assertTrue(assignedList.stream().anyMatch(tc -> tc.getId().equals(createdTestCaseId)));
        assertTrue(createdList.stream().anyMatch(tc -> tc.getId().equals(createdTestCaseId)));
    }
}
