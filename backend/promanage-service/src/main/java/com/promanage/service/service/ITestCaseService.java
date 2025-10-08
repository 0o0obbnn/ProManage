package com.promanage.service.service;

import com.promanage.common.domain.PageResult;
import com.promanage.service.entity.TestCase;

import java.util.List;

/**
 * 测试用例服务接口
 * <p>
 * 提供测试用例管理的业务逻辑，包括测试用例的CRUD操作、执行管理和统计分析
 * </p>
 *
 * @author ProManage Team
 * @date 2025-10-08
 */
public interface ITestCaseService {

    /**
     * 创建测试用例
     *
     * @param testCase 测试用例实体
     * @return 测试用例ID
     */
    Long createTestCase(TestCase testCase);

    /**
     * 根据ID获取测试用例
     *
     * @param testCaseId 测试用例ID
     * @return 测试用例实体
     */
    TestCase getTestCaseById(Long testCaseId);

    /**
     * 更新测试用例
     *
     * @param testCase 测试用例实体
     */
    void updateTestCase(TestCase testCase);

    /**
     * 删除测试用例（软删除）
     *
     * @param testCaseId 测试用例ID
     * @param userId 操作人ID
     */
    void deleteTestCase(Long testCaseId, Long userId);

    /**
     * 获取项目测试用例列表
     *
     * @param projectId 项目ID
     * @param page 页码
     * @param pageSize 每页大小
     * @param status 测试用例状态（可选）
     * @param priority 优先级（可选）
     * @param type 测试用例类型（可选）
     * @param assigneeId 指派人ID（可选）
     * @param creatorId 创建人ID（可选）
     * @param moduleId 模块名称（可选）
     * @param keyword 关键词搜索（可选，搜索标题和描述）
     * @param tags 标签（可选）
     * @return 分页结果
     */
    PageResult<TestCase> listTestCases(Long projectId, Integer page, Integer pageSize,
                                      Integer status, Integer priority, String type,
                                      Long assigneeId, Long creatorId, String moduleId,
                                      String keyword, String tags);

    /**
     * 获取用户负责的测试用例列表
     *
     * @param userId 用户ID
     * @param page 页码
     * @param pageSize 每页大小
     * @param status 测试用例状态（可选）
     * @return 分页结果
     */
    PageResult<TestCase> listTestCasesByAssignee(Long userId, Integer page, Integer pageSize, Integer status);

    /**
     * 获取用户创建的测试用例列表
     *
     * @param userId 用户ID
     * @param page 页码
     * @param pageSize 每页大小
     * @param status 测试用例状态（可选）
     * @return 分页结果
     */
    PageResult<TestCase> listTestCasesByCreator(Long userId, Integer page, Integer pageSize, Integer status);

    /**
     * 执行测试用例
     *
     * @param testCaseId 测试用例ID
     * @param result 执行结果（PASS/FAIL/BLOCK/SKIP）
     * @param actualResult 实际结果
     * @param failureReason 失败原因（当结果为FAIL时）
     * @param actualTime 实际执行时间（分钟）
     * @param executionEnvironment 执行环境
     * @param notes 执行备注
     * @param attachments 附件URL列表
     * @param executorId 执行人ID
     */
    void executeTestCase(Long testCaseId, String result, String actualResult, String failureReason,
                        Integer actualTime, String executionEnvironment, String notes,
                        String[] attachments, Long executorId);

    /**
     * 更新测试用例状态
     *
     * @param testCaseId 测试用例ID
     * @param status 新状态
     * @param userId 操作人ID
     */
    void updateTestCaseStatus(Long testCaseId, Integer status, Long userId);

    /**
     * 分配测试用例
     *
     * @param testCaseId 测试用例ID
     * @param assigneeId 指派人ID
     * @param userId 操作人ID
     */
    void assignTestCase(Long testCaseId, Long assigneeId, Long userId);

    /**
     * 批量分配测试用例
     *
     * @param testCaseIds 测试用例ID列表
     * @param assigneeId 指派人ID
     * @param userId 操作人ID
     */
    void batchAssignTestCases(List<Long> testCaseIds, Long assigneeId, Long userId);

    /**
     * 批量更新测试用例状态
     *
     * @param testCaseIds 测试用例ID列表
     * @param status 新状态
     * @param userId 操作人ID
     */
    void batchUpdateTestCaseStatus(List<Long> testCaseIds, Integer status, Long userId);

    /**
     * 批量删除测试用例
     *
     * @param testCaseIds 测试用例ID列表
     * @param userId 操作人ID
     */
    void batchDeleteTestCases(List<Long> testCaseIds, Long userId);

    /**
     * 复制测试用例
     *
     * @param testCaseId 源测试用例ID
     * @param newTitle 新测试用例标题
     * @param userId 操作人ID
     * @return 新测试用例ID
     */
    Long copyTestCase(Long testCaseId, String newTitle, Long userId);

    /**
     * 获取测试用例执行历史
     *
     * @param testCaseId 测试用例ID
     * @param page 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResult<TestCaseExecutionHistory> listTestCaseExecutionHistory(Long testCaseId, Integer page, Integer pageSize);

    /**
     * 获取测试用例统计信息
     *
     * @param projectId 项目ID
     * @return 测试用例统计信息
     */
    TestCaseStatistics getTestCaseStatistics(Long projectId);

    /**
     * 获取测试用例执行统计
     *
     * @param testCaseId 测试用例ID
     * @return 测试用例执行统计
     */
    TestCaseExecutionStatistics getTestCaseExecutionStatistics(Long testCaseId);

    /**
     * 检查用户是否有测试用例查看权限
     *
     * @param testCaseId 测试用例ID
     * @param userId 用户ID
     * @return true表示有权限
     */
    boolean hasTestCaseViewPermission(Long testCaseId, Long userId);

    /**
     * 检查用户是否有测试用例编辑权限
     *
     * @param testCaseId 测试用例ID
     * @param userId 用户ID
     * @return true表示有权限
     */
    boolean hasTestCaseEditPermission(Long testCaseId, Long userId);

    /**
     * 检查用户是否有测试用例删除权限
     *
     * @param testCaseId 测试用例ID
     * @param userId 用户ID
     * @return true表示有权限
     */
    boolean hasTestCaseDeletePermission(Long testCaseId, Long userId);

    /**
     * 检查用户是否有测试用例执行权限
     *
     * @param testCaseId 测试用例ID
     * @param userId 用户ID
     * @return true表示有权限
     */
    boolean hasTestCaseExecutePermission(Long testCaseId, Long userId);

    /**
     * 检查用户是否有项目测试用例查看权限
     *
     * @param projectId 项目ID
     * @param userId 用户ID
     * @return true表示有权限
     */
    boolean hasProjectTestCaseViewPermission(Long projectId, Long userId);

    /**
     * 检查用户是否有项目测试用例创建权限
     *
     * @param projectId 项目ID
     * @param userId 用户ID
     * @return true表示有权限
     */
    boolean hasProjectTestCaseCreatePermission(Long projectId, Long userId);

    /**
     * 导出测试用例
     *
     * @param projectId 项目ID
     * @param testCaseIds 测试用例ID列表（可选，为空则导出所有）
     * @param format 导出格式（EXCEL/CSV/PDF）
     * @return 导出文件的URL
     */
    String exportTestCases(Long projectId, List<Long> testCaseIds, String format);

    /**
     * 导入测试用例
     *
     * @param projectId 项目ID
     * @param fileUrl 导入文件URL
     * @param userId 操作人ID
     * @return 导入结果统计
     */
    TestCaseImportResult importTestCases(Long projectId, String fileUrl, Long userId);

    /**
     * 测试用例执行历史实体类（内部类）
     */
    class TestCaseExecutionHistory {
        private Long id;
        private Long testCaseId;
        private String result;
        private String actualResult;
        private String failureReason;
        private Integer actualTime;
        private String executionEnvironment;
        private String notes;
        private Long executorId;
        private java.time.LocalDateTime executedAt;
        private String attachments;

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getTestCaseId() { return testCaseId; }
        public void setTestCaseId(Long testCaseId) { this.testCaseId = testCaseId; }
        public String getResult() { return result; }
        public void setResult(String result) { this.result = result; }
        public String getActualResult() { return actualResult; }
        public void setActualResult(String actualResult) { this.actualResult = actualResult; }
        public String getFailureReason() { return failureReason; }
        public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
        public Integer getActualTime() { return actualTime; }
        public void setActualTime(Integer actualTime) { this.actualTime = actualTime; }
        public String getExecutionEnvironment() { return executionEnvironment; }
        public void setExecutionEnvironment(String executionEnvironment) { this.executionEnvironment = executionEnvironment; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
        public Long getExecutorId() { return executorId; }
        public void setExecutorId(Long executorId) { this.executorId = executorId; }
        public java.time.LocalDateTime getExecutedAt() { return executedAt; }
        public void setExecutedAt(java.time.LocalDateTime executedAt) { this.executedAt = executedAt; }
        public String getAttachments() { return attachments; }
        public void setAttachments(String attachments) { this.attachments = attachments; }
    }

    /**
     * 测试用例统计信息实体类（内部类）
     */
    class TestCaseStatistics {
        private Integer totalCount;
        private Integer draftCount;
        private Integer pendingCount;
        private Integer inProgressCount;
        private Integer passedCount;
        private Integer failedCount;
        private Integer blockedCount;
        private Integer skippedCount;
        private Double passRate;
        private Integer totalExecutions;
        private java.time.LocalDateTime lastExecutionTime;

        // Getters and Setters
        public Integer getTotalCount() { return totalCount; }
        public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }
        public Integer getDraftCount() { return draftCount; }
        public void setDraftCount(Integer draftCount) { this.draftCount = draftCount; }
        public Integer getPendingCount() { return pendingCount; }
        public void setPendingCount(Integer pendingCount) { this.pendingCount = pendingCount; }
        public Integer getInProgressCount() { return inProgressCount; }
        public void setInProgressCount(Integer inProgressCount) { this.inProgressCount = inProgressCount; }
        public Integer getPassedCount() { return passedCount; }
        public void setPassedCount(Integer passedCount) { this.passedCount = passedCount; }
        public Integer getFailedCount() { return failedCount; }
        public void setFailedCount(Integer failedCount) { this.failedCount = failedCount; }
        public Integer getBlockedCount() { return blockedCount; }
        public void setBlockedCount(Integer blockedCount) { this.blockedCount = blockedCount; }
        public Integer getSkippedCount() { return skippedCount; }
        public void setSkippedCount(Integer skippedCount) { this.skippedCount = skippedCount; }
        public Double getPassRate() { return passRate; }
        public void setPassRate(Double passRate) { this.passRate = passRate; }
        public Integer getTotalExecutions() { return totalExecutions; }
        public void setTotalExecutions(Integer totalExecutions) { this.totalExecutions = totalExecutions; }
        public java.time.LocalDateTime getLastExecutionTime() { return lastExecutionTime; }
        public void setLastExecutionTime(java.time.LocalDateTime lastExecutionTime) { this.lastExecutionTime = lastExecutionTime; }
    }

    /**
     * 测试用例执行统计实体类（内部类）
     */
    class TestCaseExecutionStatistics {
        private Integer totalExecutions;
        private Integer passCount;
        private Integer failCount;
        private Integer blockCount;
        private Integer skipCount;
        private Double passRate;
        private Double averageExecutionTime;
        private java.time.LocalDateTime lastExecutionTime;
        private String lastExecutionResult;

        // Getters and Setters
        public Integer getTotalExecutions() { return totalExecutions; }
        public void setTotalExecutions(Integer totalExecutions) { this.totalExecutions = totalExecutions; }
        public Integer getPassCount() { return passCount; }
        public void setPassCount(Integer passCount) { this.passCount = passCount; }
        public Integer getFailCount() { return failCount; }
        public void setFailCount(Integer failCount) { this.failCount = failCount; }
        public Integer getBlockCount() { return blockCount; }
        public void setBlockCount(Integer blockCount) { this.blockCount = blockCount; }
        public Integer getSkipCount() { return skipCount; }
        public void setSkipCount(Integer skipCount) { this.skipCount = skipCount; }
        public Double getPassRate() { return passRate; }
        public void setPassRate(Double passRate) { this.passRate = passRate; }
        public Double getAverageExecutionTime() { return averageExecutionTime; }
        public void setAverageExecutionTime(Double averageExecutionTime) { this.averageExecutionTime = averageExecutionTime; }
        public java.time.LocalDateTime getLastExecutionTime() { return lastExecutionTime; }
        public void setLastExecutionTime(java.time.LocalDateTime lastExecutionTime) { this.lastExecutionTime = lastExecutionTime; }
        public String getLastExecutionResult() { return lastExecutionResult; }
        public void setLastExecutionResult(String lastExecutionResult) { this.lastExecutionResult = lastExecutionResult; }
    }

    /**
     * 测试用例导入结果实体类（内部类）
     */
    class TestCaseImportResult {
        private Integer totalCount;
        private Integer successCount;
        private Integer failureCount;
        private List<String> errorMessages;

        // Getters and Setters
        public Integer getTotalCount() { return totalCount; }
        public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }
        public Integer getSuccessCount() { return successCount; }
        public void setSuccessCount(Integer successCount) { this.successCount = successCount; }
        public Integer getFailureCount() { return failureCount; }
        public void setFailureCount(Integer failureCount) { this.failureCount = failureCount; }
        public List<String> getErrorMessages() { return errorMessages; }
        public void setErrorMessages(List<String> errorMessages) { this.errorMessages = errorMessages; }
    }
}