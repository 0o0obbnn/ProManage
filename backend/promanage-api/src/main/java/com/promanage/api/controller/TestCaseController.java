package com.promanage.api.controller;

import com.promanage.api.dto.request.CreateTestCaseRequest;
import com.promanage.api.dto.request.UpdateTestCaseRequest;
import com.promanage.api.dto.request.ExecuteTestCaseRequest;
import com.promanage.api.dto.response.TestCaseResponse;
import com.promanage.api.dto.response.TestCaseDetailResponse;
import com.promanage.common.domain.PageResult;
import com.promanage.common.domain.Result;
import com.promanage.common.exception.BusinessException;
import com.promanage.infrastructure.utils.SecurityUtils;
import com.promanage.service.entity.TestCase;
import com.promanage.common.entity.User;
import com.promanage.service.service.ITestCaseService;
import com.promanage.service.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 测试用例管理控制器
 * <p>
 * 提供测试用例的创建、查询、更新、删除以及执行管理功能
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-08
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@Tag(name = "测试用例管理", description = "测试用例创建、查询、更新、删除以及执行管理接口")
@RequiredArgsConstructor
public class TestCaseController {

    private final ITestCaseService testCaseService;
    private final IUserService userService;

    /**
     * 获取项目测试用例列表
     *
     * @param projectId 项目ID
     * @param page 页码
     * @param size 每页大小
     * @param status 测试用例状态
     * @param priority 优先级
     * @param type 测试用例类型
     * @param assigneeId 指派人ID
     * @param creatorId 创建人ID
     * @param moduleId 模块名称
     * @param keyword 关键词搜索
     * @param tags 标签
     * @return 测试用例列表
     */
    @GetMapping("/projects/{projectId}/test-cases")
    @Operation(summary = "获取测试用例列表", description = "获取项目的测试用例列表")
    public Result<PageResult<TestCaseResponse>> getTestCases(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer priority,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) Long creatorId,
            @RequestParam(required = false) String moduleId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String tags) {

        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("获取测试用例列表请求, projectId={}, userId={}, page={}, size={}, status={}, priority={}, type={}, assigneeId={}, creatorId={}, moduleId={}, keyword={}, tags={}",
                projectId, userId, page, size, status, priority, type, assigneeId, creatorId, moduleId, keyword, tags);

        // 检查权限
        if (!testCaseService.hasProjectTestCaseViewPermission(projectId, userId)) {
            throw new BusinessException("没有权限查看此项目的测试用例");
        }

        PageResult<TestCase> testCasePage = testCaseService.listTestCases(
                projectId, page, size, status, priority, type, assigneeId, creatorId, moduleId, keyword, tags);

        List<TestCaseResponse> testCaseResponses = testCasePage.getList().stream()
                .map(this::convertToTestCaseResponse)
                .collect(Collectors.toList());

        PageResult<TestCaseResponse> response = PageResult.of(
                testCaseResponses,
                testCasePage.getTotal(),
                testCasePage.getPage(),
                testCasePage.getPageSize()
        );

        log.info("获取测试用例列表成功, projectId={}, total={}", projectId, response.getTotal());
        return Result.success(response);
    }

    /**
     * 创建测试用例
     *
     * @param projectId 项目ID
     * @param request 创建测试用例请求
     * @return 创建的测试用例信息
     */
    @PostMapping("/projects/{projectId}/test-cases")
    @Operation(summary = "创建测试用例", description = "在项目中创建新测试用例")
    public Result<TestCaseDetailResponse> createTestCase(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateTestCaseRequest request) {

        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("创建测试用例请求, projectId={}, userId={}, title={}", projectId, userId, request.getTitle());

        // 检查权限
        if (!testCaseService.hasProjectTestCaseCreatePermission(projectId, userId)) {
            throw new BusinessException("没有权限在此项目中创建测试用例");
        }

        TestCase testCase = new TestCase();
        testCase.setTitle(request.getTitle());
        testCase.setDescription(request.getDescription());
        testCase.setPreconditions(request.getPreconditions());
        testCase.setSteps(request.getSteps());
        testCase.setExpectedResult(request.getExpectedResult());
        testCase.setType(request.getType());
        testCase.setStatus(0); // 默认状态：草稿
        testCase.setPriority(request.getPriority());
        testCase.setProjectId(projectId);
        testCase.setRequirementId(request.getRequirementId());
        testCase.setTaskId(request.getTaskId());
        testCase.setModule(request.getModule());
        testCase.setTags(request.getTags());
        testCase.setCreatorId(userId);
        testCase.setAssigneeId(request.getAssigneeId());
        testCase.setReviewerId(request.getReviewerId());
        testCase.setEstimatedTime(request.getEstimatedTime());
        testCase.setTestData(request.getTestData());
        testCase.setSeverity(request.getSeverity());
        testCase.setVersionNumber("1.0"); // 默认版本

        Long testCaseId = testCaseService.createTestCase(testCase);

        TestCase createdTestCase = testCaseService.getTestCaseById(testCaseId);
        TestCaseDetailResponse response = convertToTestCaseDetailResponse(createdTestCase);

        log.info("测试用例创建成功, testCaseId={}, title={}", testCaseId, request.getTitle());
        return Result.success(response);
    }

    /**
     * 获取测试用例详情
     *
     * @param testCaseId 测试用例ID
     * @return 测试用例详情
     */
    @GetMapping("/test-cases/{testCaseId}")
    @Operation(summary = "获取测试用例详情", description = "获取测试用例的详细信息")
    public Result<TestCaseDetailResponse> getTestCase(@PathVariable Long testCaseId) {
        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("获取测试用例详情请求, testCaseId={}, userId={}", testCaseId, userId);

        // 检查权限
        if (!testCaseService.hasTestCaseViewPermission(testCaseId, userId)) {
            throw new BusinessException("没有权限查看此测试用例");
        }

        TestCase testCase = testCaseService.getTestCaseById(testCaseId);
        if (testCase == null) {
            throw new BusinessException("测试用例不存在");
        }

        TestCaseDetailResponse response = convertToTestCaseDetailResponse(testCase);

        log.info("获取测试用例详情成功, testCaseId={}", testCaseId);
        return Result.success(response);
    }

    /**
     * 更新测试用例
     *
     * @param testCaseId 测试用例ID
     * @param request 更新测试用例请求
     * @return 更新后的测试用例信息
     */
    @PutMapping("/test-cases/{testCaseId}")
    @Operation(summary = "更新测试用例", description = "更新测试用例信息")
    public Result<TestCaseDetailResponse> updateTestCase(
            @PathVariable Long testCaseId,
            @Valid @RequestBody UpdateTestCaseRequest request) {

        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("更新测试用例请求, testCaseId={}, userId={}", testCaseId, userId);

        // 检查权限
        if (!testCaseService.hasTestCaseEditPermission(testCaseId, userId)) {
            throw new BusinessException("没有权限编辑此测试用例");
        }

        TestCase testCase = testCaseService.getTestCaseById(testCaseId);
        if (testCase == null) {
            throw new BusinessException("测试用例不存在");
        }

        // 更新测试用例信息
        if (request.getTitle() != null) {
            testCase.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            testCase.setDescription(request.getDescription());
        }
        if (request.getPreconditions() != null) {
            testCase.setPreconditions(request.getPreconditions());
        }
        if (request.getSteps() != null) {
            testCase.setSteps(request.getSteps());
        }
        if (request.getExpectedResult() != null) {
            testCase.setExpectedResult(request.getExpectedResult());
        }
        if (request.getActualResult() != null) {
            testCase.setActualResult(request.getActualResult());
        }
        if (request.getType() != null) {
            testCase.setType(request.getType());
        }
        if (request.getStatus() != null) {
            testCase.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            testCase.setPriority(request.getPriority());
        }
        if (request.getRequirementId() != null) {
            testCase.setRequirementId(request.getRequirementId());
        }
        if (request.getTaskId() != null) {
            testCase.setTaskId(request.getTaskId());
        }
        if (request.getModule() != null) {
            testCase.setModule(request.getModule());
        }
        if (request.getTags() != null) {
            testCase.setTags(request.getTags());
        }
        if (request.getAssigneeId() != null) {
            testCase.setAssigneeId(request.getAssigneeId());
        }
        if (request.getReviewerId() != null) {
            testCase.setReviewerId(request.getReviewerId());
        }
        if (request.getEstimatedTime() != null) {
            testCase.setEstimatedTime(request.getEstimatedTime());
        }
        if (request.getActualTime() != null) {
            testCase.setActualTime(request.getActualTime());
        }
        if (request.getExecutionEnvironment() != null) {
            testCase.setExecutionEnvironment(request.getExecutionEnvironment());
        }
        if (request.getTestData() != null) {
            testCase.setTestData(request.getTestData());
        }
        if (request.getFailureReason() != null) {
            testCase.setFailureReason(request.getFailureReason());
        }
        if (request.getSeverity() != null) {
            testCase.setSeverity(request.getSeverity());
        }
        if (request.getVersion() != null) {
            testCase.setVersionNumber(request.getVersion());
        }

        testCaseService.updateTestCase(testCase);

        TestCase updatedTestCase = testCaseService.getTestCaseById(testCaseId);
        TestCaseDetailResponse response = convertToTestCaseDetailResponse(updatedTestCase);

        log.info("测试用例更新成功, testCaseId={}", testCaseId);
        return Result.success(response);
    }

    /**
     * 删除测试用例
     *
     * @param testCaseId 测试用例ID
     * @return 操作结果
     */
    @DeleteMapping("/test-cases/{testCaseId}")
    @Operation(summary = "删除测试用例", description = "删除测试用例")
    public Result<Void> deleteTestCase(@PathVariable Long testCaseId) {
        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("删除测试用例请求, testCaseId={}, userId={}", testCaseId, userId);

        // 检查权限
        if (!testCaseService.hasTestCaseDeletePermission(testCaseId, userId)) {
            throw new BusinessException("没有权限删除此测试用例");
        }

        testCaseService.deleteTestCase(testCaseId, userId);

        log.info("测试用例删除成功, testCaseId={}", testCaseId);
        return Result.success();
    }

    /**
     * 执行测试用例
     *
     * @param testCaseId 测试用例ID
     * @param request 执行测试用例请求
     * @return 执行结果
     */
    @PostMapping("/test-cases/{testCaseId}/execute")
    @Operation(summary = "执行测试用例", description = "执行测试用例并记录结果")
    public Result<TestCaseDetailResponse> executeTestCase(
            @PathVariable Long testCaseId,
            @Valid @RequestBody ExecuteTestCaseRequest request) {

        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("执行测试用例请求, testCaseId={}, userId={}, result={}", testCaseId, userId, request.getResult());

        // 检查权限
        if (!testCaseService.hasTestCaseExecutePermission(testCaseId, userId)) {
            throw new BusinessException("没有权限执行此测试用例");
        }

        testCaseService.executeTestCase(
                testCaseId,
                request.getResult(),
                request.getActualResult(),
                request.getFailureReason(),
                request.getActualTime(),
                request.getExecutionEnvironment(),
                request.getNotes(),
                request.getAttachments(),
                userId
        );

        TestCase updatedTestCase = testCaseService.getTestCaseById(testCaseId);
        TestCaseDetailResponse response = convertToTestCaseDetailResponse(updatedTestCase);

        log.info("测试用例执行成功, testCaseId={}, result={}", testCaseId, request.getResult());
        return Result.success(response);
    }

    /**
     * 分配测试用例
     *
     * @param testCaseId 测试用例ID
     * @param assigneeId 指派人ID
     * @return 操作结果
     */
    @PostMapping("/test-cases/{testCaseId}/assign")
    @Operation(summary = "分配测试用例", description = "将测试用例分配给指定人员")
    public Result<TestCaseResponse> assignTestCase(
            @PathVariable Long testCaseId,
            @RequestParam Long assigneeId) {

        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("分配测试用例请求, testCaseId={}, currentUserId={}, assigneeId={}", testCaseId, userId, assigneeId);

        // 检查权限
        if (!testCaseService.hasTestCaseEditPermission(testCaseId, userId)) {
            throw new BusinessException("没有权限分配此测试用例");
        }

        testCaseService.assignTestCase(testCaseId, assigneeId, userId);

        TestCase updatedTestCase = testCaseService.getTestCaseById(testCaseId);
        TestCaseResponse response = convertToTestCaseResponse(updatedTestCase);

        log.info("测试用例分配成功, testCaseId={}, assigneeId={}", testCaseId, assigneeId);
        return Result.success(response);
    }

    /**
     * 复制测试用例
     *
     * @param testCaseId 源测试用例ID
     * @param newTitle 新测试用例标题
     * @return 新测试用例信息
     */
    @PostMapping("/test-cases/{testCaseId}/copy")
    @Operation(summary = "复制测试用例", description = "复制现有测试用例创建新测试用例")
    public Result<TestCaseDetailResponse> copyTestCase(
            @PathVariable Long testCaseId,
            @RequestParam String newTitle) {

        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("复制测试用例请求, testCaseId={}, userId={}, newTitle={}", testCaseId, userId, newTitle);

        // 检查权限
        if (!testCaseService.hasTestCaseViewPermission(testCaseId, userId)) {
            throw new BusinessException("没有权限查看此测试用例");
        }

        Long newTestCaseId = testCaseService.copyTestCase(testCaseId, newTitle, userId);
        TestCase newTestCase = testCaseService.getTestCaseById(newTestCaseId);
        TestCaseDetailResponse response = convertToTestCaseDetailResponse(newTestCase);

        log.info("测试用例复制成功, originalTestCaseId={}, newTestCaseId={}", testCaseId, newTestCaseId);
        return Result.success(response);
    }

    /**
     * 获取测试用例执行历史
     *
     * @param testCaseId 测试用例ID
     * @param page 页码
     * @param size 每页大小
     * @return 执行历史列表
     */
    @GetMapping("/test-cases/{testCaseId}/executions")
    @Operation(summary = "获取测试用例执行历史", description = "获取测试用例的执行历史记录")
    public Result<PageResult<ITestCaseService.TestCaseExecutionHistory>> getTestCaseExecutions(
            @PathVariable Long testCaseId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("获取测试用例执行历史请求, testCaseId={}, userId={}, page={}, size={}", testCaseId, userId, page, size);

        // 检查权限
        if (!testCaseService.hasTestCaseViewPermission(testCaseId, userId)) {
            throw new BusinessException("没有权限查看此测试用例");
        }

        PageResult<ITestCaseService.TestCaseExecutionHistory> executionHistory = 
                testCaseService.listTestCaseExecutionHistory(testCaseId, page, size);

        log.info("获取测试用例执行历史成功, testCaseId={}, total={}", testCaseId, executionHistory.getTotal());
        return Result.success(executionHistory);
    }

    /**
     * 获取项目测试用例统计信息
     *
     * @param projectId 项目ID
     * @return 统计信息
     */
    @GetMapping("/projects/{projectId}/test-cases/statistics")
    @Operation(summary = "获取项目测试用例统计", description = "获取项目的测试用例统计信息")
    public Result<ITestCaseService.TestCaseStatistics> getProjectTestCaseStatistics(
            @PathVariable Long projectId) {

        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("获取项目测试用例统计请求, projectId={}, userId={}", projectId, userId);

        // 检查权限
        if (!testCaseService.hasProjectTestCaseViewPermission(projectId, userId)) {
            throw new BusinessException("没有权限查看此项目的测试用例");
        }

        ITestCaseService.TestCaseStatistics statistics = testCaseService.getTestCaseStatistics(projectId);

        log.info("获取项目测试用例统计成功, projectId={}", projectId);
        return Result.success(statistics);
    }

    // 辅助方法

    private TestCaseResponse convertToTestCaseResponse(TestCase testCase) {
        User creator = userService.getById(testCase.getCreatorId());
        User assignee = userService.getById(testCase.getAssigneeId());
        User reviewer = userService.getById(testCase.getReviewerId());
        User lastExecutedBy = userService.getById(testCase.getLastExecutedById());

        return TestCaseResponse.builder()
                .id(testCase.getId())
                .title(testCase.getTitle())
                .description(testCase.getDescription())
                .preconditions(testCase.getPreconditions())
                .steps(testCase.getSteps())
                .expectedResult(testCase.getExpectedResult())
                .actualResult(testCase.getActualResult())
                .type(testCase.getType())
                .status(testCase.getStatus())
                .priority(testCase.getPriority())
                .projectId(testCase.getProjectId())
                .requirementId(testCase.getRequirementId())
                .taskId(testCase.getTaskId())
                .module(testCase.getModule())
                .tags(testCase.getTags())
                .creatorId(testCase.getCreatorId())
                .creatorName(creator != null ? creator.getRealName() : "未知")
                .creatorAvatar(creator != null ? creator.getAvatar() : null)
                .assigneeId(testCase.getAssigneeId())
                .assigneeName(assignee != null ? assignee.getRealName() : null)
                .assigneeAvatar(assignee != null ? assignee.getAvatar() : null)
                .reviewerId(testCase.getReviewerId())
                .reviewerName(reviewer != null ? reviewer.getRealName() : null)
                .reviewerAvatar(reviewer != null ? reviewer.getAvatar() : null)
                .estimatedTime(testCase.getEstimatedTime())
                .actualTime(testCase.getActualTime())
                .executionEnvironment(testCase.getExecutionEnvironment())
                .testData(testCase.getTestData())
                .failureReason(testCase.getFailureReason())
                .severity(testCase.getSeverity())
                .lastExecutedAt(testCase.getLastExecutedAt())
                .lastExecutedById(testCase.getLastExecutedById())
                .lastExecutedByName(lastExecutedBy != null ? lastExecutedBy.getRealName() : null)
                .version(testCase.getVersionNumber())
                .createTime(testCase.getCreateTime())
                .updateTime(testCase.getUpdateTime())
                .build();
    }

    private TestCaseDetailResponse convertToTestCaseDetailResponse(TestCase testCase) {
        TestCaseResponse testCaseResponse = convertToTestCaseResponse(testCase);
        
        // 获取执行历史
        PageResult<ITestCaseService.TestCaseExecutionHistory> executionHistory = 
                testCaseService.listTestCaseExecutionHistory(testCase.getId(), 1, 10);
        
        // 获取执行统计
        ITestCaseService.TestCaseExecutionStatistics executionStatistics = 
                testCaseService.getTestCaseExecutionStatistics(testCase.getId());
        
        // 构建统计信息
        TestCaseDetailResponse.TestCaseStatistics statistics = TestCaseDetailResponse.TestCaseStatistics.builder()
                .totalExecutions(executionStatistics.getTotalExecutions())
                .passCount(executionStatistics.getPassCount())
                .failCount(executionStatistics.getFailCount())
                .blockCount(executionStatistics.getBlockCount())
                .skipCount(executionStatistics.getSkipCount())
                .passRate(executionStatistics.getPassRate())
                .averageExecutionTime(executionStatistics.getAverageExecutionTime())
                .lastExecutionTime(executionStatistics.getLastExecutionTime())
                .lastExecutionResult(executionStatistics.getLastExecutionResult())
                .build();
        
        // 转换执行历史
        List<TestCaseDetailResponse.TestCaseExecutionHistoryResponse> executionHistoryResponses = 
                executionHistory.getList().stream()
                        .map(this::convertToExecutionHistoryResponse)
                        .collect(Collectors.toList());
        
        return TestCaseDetailResponse.builder()
                .id(testCaseResponse.getId())
                .title(testCaseResponse.getTitle())
                .description(testCaseResponse.getDescription())
                .preconditions(testCaseResponse.getPreconditions())
                .steps(testCaseResponse.getSteps())
                .expectedResult(testCaseResponse.getExpectedResult())
                .actualResult(testCaseResponse.getActualResult())
                .type(testCaseResponse.getType())
                .status(testCaseResponse.getStatus())
                .priority(testCaseResponse.getPriority())
                .projectId(testCaseResponse.getProjectId())
                .requirementId(testCaseResponse.getRequirementId())
                .taskId(testCaseResponse.getTaskId())
                .module(testCaseResponse.getModule())
                .tags(testCaseResponse.getTags())
                .creatorId(testCaseResponse.getCreatorId())
                .creatorName(testCaseResponse.getCreatorName())
                .creatorAvatar(testCaseResponse.getCreatorAvatar())
                .assigneeId(testCaseResponse.getAssigneeId())
                .assigneeName(testCaseResponse.getAssigneeName())
                .assigneeAvatar(testCaseResponse.getAssigneeAvatar())
                .reviewerId(testCaseResponse.getReviewerId())
                .reviewerName(testCaseResponse.getReviewerName())
                .reviewerAvatar(testCaseResponse.getReviewerAvatar())
                .estimatedTime(testCaseResponse.getEstimatedTime())
                .actualTime(testCaseResponse.getActualTime())
                .executionEnvironment(testCaseResponse.getExecutionEnvironment())
                .testData(testCaseResponse.getTestData())
                .failureReason(testCaseResponse.getFailureReason())
                .severity(testCaseResponse.getSeverity())
                .lastExecutedAt(testCaseResponse.getLastExecutedAt())
                .lastExecutedById(testCaseResponse.getLastExecutedById())
                .lastExecutedByName(testCaseResponse.getLastExecutedByName())
                .version(testCaseResponse.getVersion())
                .createTime(testCaseResponse.getCreateTime())
                .updateTime(testCaseResponse.getUpdateTime())
                .executionHistory(executionHistoryResponses)
                .statistics(statistics)
                .build();
    }

    private TestCaseDetailResponse.TestCaseExecutionHistoryResponse convertToExecutionHistoryResponse(
            ITestCaseService.TestCaseExecutionHistory history) {
        User executor = userService.getById(history.getExecutorId());
        
        return TestCaseDetailResponse.TestCaseExecutionHistoryResponse.builder()
                .id(history.getId())
                .testCaseId(history.getTestCaseId())
                .result(history.getResult())
                .actualResult(history.getActualResult())
                .failureReason(history.getFailureReason())
                .actualTime(history.getActualTime())
                .executionEnvironment(history.getExecutionEnvironment())
                .notes(history.getNotes())
                .executorId(history.getExecutorId())
                .executorName(executor != null ? executor.getRealName() : "未知")
                .executorAvatar(executor != null ? executor.getAvatar() : null)
                .executedAt(history.getExecutedAt())
                .build();
    }
}
