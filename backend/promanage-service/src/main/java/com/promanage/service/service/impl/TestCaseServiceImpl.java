package com.promanage.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.promanage.common.domain.PageResult;
import com.promanage.common.exception.BusinessException;
import com.promanage.service.entity.TestCase;
import com.promanage.service.entity.TestExecution;
import com.promanage.service.mapper.TestCaseMapper;
import com.promanage.service.mapper.TestExecutionMapper;
import com.promanage.service.service.IProjectService;
import com.promanage.service.service.ITestCaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * 测试用例服务实现类
 *
 * @author ProManage Team
 * @date 2025-10-09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestCaseServiceImpl implements ITestCaseService {

    private final TestCaseMapper testCaseMapper;
    private final TestExecutionMapper testExecutionMapper;
    private final IProjectService projectService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTestCase(TestCase testCase) {
        log.info("Creating test case: {}", testCase.getTitle());
        testCase.setCreateTime(LocalDateTime.now());
        testCase.setUpdateTime(LocalDateTime.now());
        testCaseMapper.insert(testCase);
        return testCase.getId();
    }

    @Override
    public TestCase getTestCaseById(Long testCaseId) {
        log.info("Getting test case by id: {}", testCaseId);
        TestCase testCase = testCaseMapper.selectById(testCaseId);
        if (testCase == null) {
            throw new BusinessException("测试用例不存在");
        }
        return testCase;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTestCase(TestCase testCase) {
        log.info("Updating test case: {}", testCase.getId());
        testCase.setUpdateTime(LocalDateTime.now());
        testCaseMapper.updateById(testCase);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTestCase(Long testCaseId, Long userId) {
        log.info("Deleting test case: {} by user: {}", testCaseId, userId);
        TestCase testCase = new TestCase();
        testCase.setId(testCaseId);
        testCase.setDeleted(true);
        testCase.setUpdateTime(LocalDateTime.now());
        testCaseMapper.updateById(testCase);
    }

    @Override
    public PageResult<TestCase> listTestCases(Long projectId, Integer page, Integer pageSize,
                                              Integer status, Integer priority, String type,
                                              Long assigneeId, Long creatorId, String module,
                                              String keyword, String tags) {
        log.info("Listing test cases for project: {}", projectId);

        Page<TestCase> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<TestCase> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(TestCase::getProjectId, projectId)
               .eq(TestCase::getDeleted, false);

        if (status != null) {
            wrapper.eq(TestCase::getStatus, status);
        }
        if (priority != null) {
            wrapper.eq(TestCase::getPriority, priority);
        }
        if (type != null && !type.isEmpty()) {
            wrapper.eq(TestCase::getType, type);
        }
        if (assigneeId != null) {
            wrapper.eq(TestCase::getAssigneeId, assigneeId);
        }
        if (creatorId != null) {
            wrapper.eq(TestCase::getCreatorId, creatorId);
        }
        if (module != null && !module.isEmpty()) {
            wrapper.eq(TestCase::getModule, module);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(TestCase::getTitle, keyword)
                            .or()
                            .like(TestCase::getDescription, keyword));
        }
        if (tags != null && !tags.isEmpty()) {
            wrapper.like(TestCase::getTags, tags);
        }

        wrapper.orderByDesc(TestCase::getCreateTime);

        IPage<TestCase> result = testCaseMapper.selectPage(pageParam, wrapper);

        return PageResult.<TestCase>builder()
                .list(result.getRecords())
                .total(result.getTotal())
                .page(page)
                .pageSize(pageSize)
                .build();
    }

    @Override
    public PageResult<TestCase> listTestCasesByAssignee(Long userId, Integer page, Integer pageSize, Integer status) {
        log.info("Listing test cases for assignee: {}", userId);

        Page<TestCase> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<TestCase> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(TestCase::getAssigneeId, userId)
               .eq(TestCase::getDeleted, false);

        if (status != null) {
            wrapper.eq(TestCase::getStatus, status);
        }

        wrapper.orderByDesc(TestCase::getUpdateTime);

        IPage<TestCase> result = testCaseMapper.selectPage(pageParam, wrapper);

        return PageResult.<TestCase>builder()
                .list(result.getRecords())
                .total(result.getTotal())
                .page(page)
                .pageSize(pageSize)
                .build();
    }

    @Override
    public PageResult<TestCase> listTestCasesByCreator(Long userId, Integer page, Integer pageSize, Integer status) {
        log.info("Listing test cases created by user: {}", userId);

        Page<TestCase> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<TestCase> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(TestCase::getCreatorId, userId)
               .eq(TestCase::getDeleted, false);

        if (status != null) {
            wrapper.eq(TestCase::getStatus, status);
        }

        wrapper.orderByDesc(TestCase::getCreateTime);

        IPage<TestCase> result = testCaseMapper.selectPage(pageParam, wrapper);

        return PageResult.<TestCase>builder()
                .list(result.getRecords())
                .total(result.getTotal())
                .page(page)
                .pageSize(pageSize)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeTestCase(Long testCaseId, String result, String actualResult, String failureReason,
                                Integer actualTime, String executionEnvironment, String notes,
                                String[] attachments, Long executorId) {
        log.info("Executing test case: {} by user: {}", testCaseId, executorId);

        // Validate test case exists
        getTestCaseById(testCaseId);

        // Map result string to integer
        Integer resultCode = mapResultStringToCode(result);

        // Create test execution record
        TestExecution execution = new TestExecution();
        execution.setTestCaseId(testCaseId);
        execution.setExecutorId(executorId);
        execution.setResult(resultCode);
        execution.setActualResult(actualResult);
        execution.setFailureReason(failureReason);
        execution.setExecutionTime(actualTime);
        execution.setExecutionEnvironment(executionEnvironment);
        execution.setNotes(notes);
        execution.setCreateTime(LocalDateTime.now());
        execution.setDeleted(false);

        // Convert attachments array to JSON string
        if (attachments != null && attachments.length > 0) {
            try {
                execution.setAttachments(objectMapper.writeValueAsString(attachments));
            } catch (Exception e) {
                log.error("Failed to serialize attachments", e);
            }
        }

        testExecutionMapper.insert(execution);

        // Update test case with execution results
        TestCase updateCase = new TestCase();
        updateCase.setId(testCaseId);
        updateCase.setActualResult(actualResult);
        updateCase.setActualTime(actualTime);
        updateCase.setLastExecutedAt(LocalDateTime.now());
        updateCase.setLastExecutedById(executorId);
        updateCase.setUpdateTime(LocalDateTime.now());

        // Update test case status based on result
        switch (resultCode) {
            case 0: // PASS
                updateCase.setStatus(3);
                break;
            case 1: // FAIL
                updateCase.setStatus(4);
                updateCase.setFailureReason(failureReason);
                break;
            case 2: // BLOCK
                updateCase.setStatus(5);
                break;
            case 3: // SKIP
                updateCase.setStatus(6);
                break;
        }

        testCaseMapper.updateById(updateCase);

        log.info("Test case {} executed successfully with result: {}", testCaseId, result);
    }

    /**
     * Map result string to result code
     * PASS -> 0, FAIL -> 1, BLOCK -> 2, SKIP -> 3
     */
    private Integer mapResultStringToCode(String result) {
        if (result == null) {
            throw new BusinessException("执行结果不能为空");
        }

        switch (result.toUpperCase()) {
            case "PASS":
                return 0;
            case "FAIL":
                return 1;
            case "BLOCK":
                return 2;
            case "SKIP":
                return 3;
            default:
                throw new BusinessException("无效的执行结果: " + result);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTestCaseStatus(Long testCaseId, Integer status, Long userId) {
        log.info("Updating test case status: {} to: {} by user: {}", testCaseId, status, userId);

        TestCase testCase = new TestCase();
        testCase.setId(testCaseId);
        testCase.setStatus(status);
        testCase.setUpdateTime(LocalDateTime.now());
        testCaseMapper.updateById(testCase);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignTestCase(Long testCaseId, Long assigneeId, Long userId) {
        log.info("Assigning test case: {} to user: {} by: {}", testCaseId, assigneeId, userId);

        TestCase testCase = new TestCase();
        testCase.setId(testCaseId);
        testCase.setAssigneeId(assigneeId);
        testCase.setUpdateTime(LocalDateTime.now());
        testCaseMapper.updateById(testCase);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAssignTestCases(List<Long> testCaseIds, Long assigneeId, Long userId) {
        log.info("Batch assigning {} test cases to user: {}", testCaseIds.size(), assigneeId);

        for (Long testCaseId : testCaseIds) {
            assignTestCase(testCaseId, assigneeId, userId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateTestCaseStatus(List<Long> testCaseIds, Integer status, Long userId) {
        log.info("Batch updating {} test cases status to: {}", testCaseIds.size(), status);

        for (Long testCaseId : testCaseIds) {
            updateTestCaseStatus(testCaseId, status, userId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteTestCases(List<Long> testCaseIds, Long userId) {
        log.info("Batch deleting {} test cases", testCaseIds.size());

        for (Long testCaseId : testCaseIds) {
            deleteTestCase(testCaseId, userId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long copyTestCase(Long testCaseId, String newTitle, Long userId) {
        log.info("Copying test case: {} with new title: {}", testCaseId, newTitle);

        TestCase original = getTestCaseById(testCaseId);

        TestCase copy = new TestCase();
        copy.setProjectId(original.getProjectId());
        copy.setTitle(newTitle);
        copy.setDescription(original.getDescription());
        copy.setPreconditions(original.getPreconditions());
        copy.setSteps(original.getSteps());
        copy.setExpectedResult(original.getExpectedResult());
        copy.setType(original.getType());
        copy.setPriority(original.getPriority());
        copy.setModule(original.getModule());
        copy.setTags(original.getTags());
        copy.setStatus(0); // Draft status
        copy.setCreatorId(userId);
        copy.setCreateTime(LocalDateTime.now());
        copy.setUpdateTime(LocalDateTime.now());
        copy.setDeleted(false);

        testCaseMapper.insert(copy);
        return copy.getId();
    }

    @Override
    public PageResult<TestCaseExecutionHistory> listTestCaseExecutionHistory(Long testCaseId, Integer page, Integer pageSize) {
        log.info("Listing execution history for test case: {}", testCaseId);

        Page<TestExecution> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<TestExecution> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TestExecution::getTestCaseId, testCaseId)
               .eq(TestExecution::getDeleted, false)
               .orderByDesc(TestExecution::getCreateTime);

        IPage<TestExecution> result = testExecutionMapper.selectPage(pageParam, wrapper);

        // Convert TestExecution entities to TestCaseExecutionHistory
        List<TestCaseExecutionHistory> historyList = new ArrayList<>();
        for (TestExecution execution : result.getRecords()) {
            TestCaseExecutionHistory history = new TestCaseExecutionHistory();
            history.setId(execution.getId());
            history.setTestCaseId(execution.getTestCaseId());
            history.setResult(mapResultCodeToString(execution.getResult()));
            history.setActualResult(execution.getActualResult());
            history.setFailureReason(execution.getFailureReason());
            history.setActualTime(execution.getExecutionTime());
            history.setExecutionEnvironment(execution.getExecutionEnvironment());
            history.setNotes(execution.getNotes());
            history.setExecutorId(execution.getExecutorId());
            history.setExecutedAt(execution.getCreateTime());
            history.setAttachments(execution.getAttachments());
            historyList.add(history);
        }

        return PageResult.<TestCaseExecutionHistory>builder()
                .list(historyList)
                .total(result.getTotal())
                .page(page)
                .pageSize(pageSize)
                .build();
    }

    /**
     * Map result code to result string
     * 0 -> PASS, 1 -> FAIL, 2 -> BLOCK, 3 -> SKIP
     */
    private String mapResultCodeToString(Integer code) {
        switch (code) {
            case 0: return "PASS";
            case 1: return "FAIL";
            case 2: return "BLOCK";
            case 3: return "SKIP";
            default: return "UNKNOWN";
        }
    }

    @Override
    public TestCaseStatistics getTestCaseStatistics(Long projectId) {
        log.info("Getting test case statistics for project: {}", projectId);

        Map<String, Object> statsMap = testExecutionMapper.getProjectStatistics(projectId);

        TestCaseStatistics stats = new TestCaseStatistics();
        stats.setTotalCount(getIntValue(statsMap, "totalcount"));
        stats.setDraftCount(getIntValue(statsMap, "draftcount"));
        stats.setPendingCount(getIntValue(statsMap, "pendingcount"));
        stats.setInProgressCount(getIntValue(statsMap, "inprogresscount"));
        stats.setPassedCount(getIntValue(statsMap, "passedcount"));
        stats.setFailedCount(getIntValue(statsMap, "failedcount"));
        stats.setBlockedCount(getIntValue(statsMap, "blockedcount"));
        stats.setSkippedCount(getIntValue(statsMap, "skippedcount"));
        stats.setTotalExecutions(getIntValue(statsMap, "totalexecutions"));

        // Calculate pass rate
        int passed = stats.getPassedCount();
        int total = stats.getTotalCount();
        if (total > 0) {
            stats.setPassRate((double) passed / total * 100);
        } else {
            stats.setPassRate(0.0);
        }

        // Get last execution time
        if (statsMap.get("lastexecutiontime") != null) {
            stats.setLastExecutionTime((LocalDateTime) statsMap.get("lastexecutiontime"));
        }

        return stats;
    }

    @Override
    public TestCaseExecutionStatistics getTestCaseExecutionStatistics(Long testCaseId) {
        log.info("Getting execution statistics for test case: {}", testCaseId);

        Map<String, Object> statsMap = testExecutionMapper.getExecutionStatistics(testCaseId);

        TestCaseExecutionStatistics stats = new TestCaseExecutionStatistics();
        stats.setTotalExecutions(getIntValue(statsMap, "total"));
        stats.setPassCount(getIntValue(statsMap, "pass"));
        stats.setFailCount(getIntValue(statsMap, "fail"));
        stats.setBlockCount(getIntValue(statsMap, "block"));
        stats.setSkipCount(getIntValue(statsMap, "skip"));

        // Calculate pass rate
        int total = stats.getTotalExecutions();
        int passed = stats.getPassCount();
        if (total > 0) {
            stats.setPassRate((double) passed / total * 100);
        } else {
            stats.setPassRate(0.0);
        }

        // Get average execution time
        Object avgTime = statsMap.get("avgtime");
        if (avgTime != null) {
            stats.setAverageExecutionTime(((Number) avgTime).doubleValue());
        } else {
            stats.setAverageExecutionTime(0.0);
        }

        // Get last execution info
        LambdaQueryWrapper<TestExecution> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TestExecution::getTestCaseId, testCaseId)
               .eq(TestExecution::getDeleted, false)
               .orderByDesc(TestExecution::getCreateTime)
               .last("LIMIT 1");

        TestExecution lastExecution = testExecutionMapper.selectOne(wrapper);
        if (lastExecution != null) {
            stats.setLastExecutionTime(lastExecution.getCreateTime());
            stats.setLastExecutionResult(mapResultCodeToString(lastExecution.getResult()));
        }

        return stats;
    }

    /**
     * Helper method to safely extract integer value from map
     */
    private Integer getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return 0;
    }

    @Override
    public boolean hasTestCaseViewPermission(Long testCaseId, Long userId) {
        try {
            TestCase testCase = testCaseMapper.selectById(testCaseId);
            if (testCase == null || testCase.getDeleted()) {
                return false;
            }
            // Check if user is project member
            return projectService.isMember(testCase.getProjectId(), userId);
        } catch (Exception e) {
            log.error("Error checking test case view permission", e);
            return false;
        }
    }

    @Override
    public boolean hasTestCaseEditPermission(Long testCaseId, Long userId) {
        try {
            TestCase testCase = testCaseMapper.selectById(testCaseId);
            if (testCase == null || testCase.getDeleted()) {
                return false;
            }

            // Creator can always edit
            if (testCase.getCreatorId().equals(userId)) {
                return true;
            }

            // Assignee can edit
            if (testCase.getAssigneeId() != null && testCase.getAssigneeId().equals(userId)) {
                return true;
            }

            // Project member can edit
            return projectService.isMember(testCase.getProjectId(), userId);
        } catch (Exception e) {
            log.error("Error checking test case edit permission", e);
            return false;
        }
    }

    @Override
    public boolean hasTestCaseDeletePermission(Long testCaseId, Long userId) {
        try {
            TestCase testCase = testCaseMapper.selectById(testCaseId);
            if (testCase == null || testCase.getDeleted()) {
                return false;
            }

            // Only creator can delete (simplified permission check)
            return testCase.getCreatorId().equals(userId);
        } catch (Exception e) {
            log.error("Error checking test case delete permission", e);
            return false;
        }
    }

    @Override
    public boolean hasTestCaseExecutePermission(Long testCaseId, Long userId) {
        try {
            TestCase testCase = testCaseMapper.selectById(testCaseId);
            if (testCase == null || testCase.getDeleted()) {
                return false;
            }

            // Assignee can execute
            if (testCase.getAssigneeId() != null && testCase.getAssigneeId().equals(userId)) {
                return true;
            }

            // Any project member with TESTER role can execute
            return projectService.isMember(testCase.getProjectId(), userId);
        } catch (Exception e) {
            log.error("Error checking test case execute permission", e);
            return false;
        }
    }

    @Override
    public boolean hasProjectTestCaseViewPermission(Long projectId, Long userId) {
        try {
            return projectService.isMember(projectId, userId);
        } catch (Exception e) {
            log.error("Error checking project test case view permission", e);
            return false;
        }
    }

    @Override
    public boolean hasProjectTestCaseCreatePermission(Long projectId, Long userId) {
        try {
            // Any project member can create test cases
            return projectService.isMember(projectId, userId);
        } catch (Exception e) {
            log.error("Error checking project test case create permission", e);
            return false;
        }
    }

    @Override
    public String exportTestCases(Long projectId, List<Long> testCaseIds, String format) {
        log.info("Exporting test cases for project: {} in format: {}", projectId, format);

        // Get test cases to export
        List<TestCase> testCases = new ArrayList<>();
        if (testCaseIds != null && !testCaseIds.isEmpty()) {
            // Export specific test cases
            for (Long testCaseId : testCaseIds) {
                TestCase testCase = testCaseMapper.selectById(testCaseId);
                if (testCase != null && !testCase.getDeleted() && testCase.getProjectId().equals(projectId)) {
                    testCases.add(testCase);
                }
            }
        } else {
            // Export all test cases in project
            LambdaQueryWrapper<TestCase> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TestCase::getProjectId, projectId)
                   .eq(TestCase::getDeleted, false)
                   .orderByDesc(TestCase::getCreateTime);
            testCases = testCaseMapper.selectList(wrapper);
        }

        if (testCases.isEmpty()) {
            throw new BusinessException("没有找到需要导出的测试用例");
        }

        // Generate export file based on format
        String exportUrl;
        try {
            switch (format.toUpperCase()) {
                case "EXCEL":
                    exportUrl = exportToExcel(testCases);
                    break;
                case "CSV":
                    exportUrl = exportToCsv(testCases);
                    break;
                case "PDF":
                    exportUrl = exportToPdf(testCases);
                    break;
                default:
                    throw new BusinessException("不支持的导出格式: " + format);
            }
        } catch (Exception e) {
            log.error("Error exporting test cases", e);
            throw new BusinessException("导出测试用例失败: " + e.getMessage());
        }

        log.info("Test cases exported successfully: {}", exportUrl);
        return exportUrl;
    }

    /**
     * Export test cases to Excel-compatible format without external deps.
     * Generates a tab-separated .xls file that Excel can open.
     */
    private String exportToExcel(List<TestCase> testCases) {
        try {
            StringBuilder tsv = new StringBuilder();
            // Header
            tsv.append("ID\t标题\t描述\t类型\t状态\t优先级\t模块\t创建人ID\t指派人ID\t创建时间\n");
            // Rows
            for (TestCase testCase : testCases) {
                tsv.append(testCase.getId()).append('\t')
                   .append(safeTab(testCase.getTitle())).append('\t')
                   .append(safeTab(testCase.getDescription())).append('\t')
                   .append(nullToEmpty(testCase.getType())).append('\t')
                   .append(nullToEmpty(testCase.getStatus())).append('\t')
                   .append(nullToEmpty(testCase.getPriority())).append('\t')
                   .append(safeTab(testCase.getModule())).append('\t')
                   .append(nullToEmpty(testCase.getCreatorId())).append('\t')
                   .append(testCase.getAssigneeId() != null ? testCase.getAssigneeId() : "").append('\t')
                   .append(nullToEmpty(testCase.getCreateTime())).append('\n');
            }

            String fileName = "test_cases_" + System.currentTimeMillis() + ".xls";
            Path exportDir = Paths.get("exports");
            if (!Files.exists(exportDir)) {
                Files.createDirectories(exportDir);
            }
            Path filePath = exportDir.resolve(fileName);
            Files.write(filePath, tsv.toString().getBytes(StandardCharsets.UTF_8));

            String fileUrl = "/exports/" + fileName;
            log.info("Excel export completed: {} (path: {})", fileUrl, filePath.toAbsolutePath());
            return fileUrl;
        } catch (Exception e) {
            log.error("Error exporting to Excel (TSV)", e);
            throw new BusinessException("Excel导出失败: " + e.getMessage());
        }
    }

    /**
     * Export test cases to CSV format
     */
    private String exportToCsv(List<TestCase> testCases) {
        try {
            StringBuilder csv = new StringBuilder();

            // CSV Header
            csv.append("ID,标题,描述,类型,状态,优先级,模块,创建人ID,指派人ID,创建时间\n");

            // CSV Data
            for (TestCase testCase : testCases) {
                csv.append(testCase.getId()).append(",")
                   .append(escapeCsv(testCase.getTitle())).append(",")
                   .append(escapeCsv(testCase.getDescription())).append(",")
                   .append(testCase.getType()).append(",")
                   .append(testCase.getStatus()).append(",")
                   .append(testCase.getPriority()).append(",")
                   .append(escapeCsv(testCase.getModule())).append(",")
                   .append(testCase.getCreatorId()).append(",")
                   .append(testCase.getAssigneeId() != null ? testCase.getAssigneeId() : "").append(",")
                   .append(testCase.getCreateTime()).append("\n");
            }

            // Persist to local filesystem under exports/
            String fileName = "test_cases_" + System.currentTimeMillis() + ".csv";
            Path exportDir = Paths.get("exports");
            if (!Files.exists(exportDir)) {
                Files.createDirectories(exportDir);
            }
            Path filePath = exportDir.resolve(fileName);
            Files.write(filePath, csv.toString().getBytes(StandardCharsets.UTF_8));

            String fileUrl = "/exports/" + fileName;
            log.info("CSV export completed: {} (path: {})", fileUrl, filePath.toAbsolutePath());
            return fileUrl;
        } catch (Exception e) {
            log.error("Error exporting to CSV", e);
            throw new BusinessException("CSV导出失败: " + e.getMessage());
        }
    }

    /**
     * Export test cases to PDF format (not enabled without external deps).
     */
    private String exportToPdf(List<TestCase> testCases) {
        throw new BusinessException("当前环境未启用PDF导出，请使用CSV/Excel导出");
    }

    /**
     * Escape CSV special characters
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        // Escape quotes and add quotes if contains comma or newline
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\"")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    @Override
    public TestCaseImportResult importTestCases(Long projectId, String fileUrl, Long userId) {
        log.info("Importing test cases for project: {} from file: {}", projectId, fileUrl);

        TestCaseImportResult result = new TestCaseImportResult();
        result.setTotalCount(0);
        result.setSuccessCount(0);
        result.setFailureCount(0);
        result.setErrorMessages(new ArrayList<>());

        try {
            if (fileUrl == null || fileUrl.isBlank()) {
                throw new BusinessException("文件地址不能为空");
            }
            Path path;
            if (fileUrl.startsWith("/exports/")) {
                path = Paths.get("exports", fileUrl.substring("/exports/".length()));
            } else {
                path = Paths.get(fileUrl);
            }
            if (!Files.exists(path)) {
                throw new BusinessException("文件不存在: " + path.toAbsolutePath());
            }

            String lower = path.getFileName().toString().toLowerCase();
            if (lower.endsWith(".csv") || lower.endsWith(".txt")) {
                // CSV import
                List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
                if (lines.size() > 10001) {
                    throw new BusinessException("单次导入最大支持10000行（含表头）");
                }
                result.setTotalCount(lines.size() > 0 ? lines.size() - 1 : 0);
                boolean headerSkipped = false;
                for (String line : lines) {
                    if (!headerSkipped) { headerSkipped = true; continue; }
                    if (line.isBlank()) continue;
                    String[] cols = parseCsvLine(line);
                    importRow(projectId, userId, result, cols);
                }
            } else if (lower.endsWith(".xls")) {
                // TSV-like .xls (we output as TSV earlier); handle as tab-separated text
                List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
                if (lines.size() > 10001) {
                    throw new BusinessException("单次导入最大支持10000行（含表头）");
                }
                result.setTotalCount(lines.size() > 0 ? lines.size() - 1 : 0);
                boolean headerSkipped = false;
                for (String line : lines) {
                    if (!headerSkipped) { headerSkipped = true; continue; }
                    if (line.isBlank()) continue;
                    String[] cols = line.split("\t", -1);
                    // align columns to CSV layout
                    importRow(projectId, userId, result, cols);
                }
            } else if (lower.endsWith(".xlsx")) {
                try (Workbook workbook = new XSSFWorkbook(Files.newInputStream(path))) {
                    Sheet sheet = workbook.getSheetAt(0);
                    int rowCount = sheet.getPhysicalNumberOfRows();
                    result.setTotalCount(Math.max(0, rowCount - 1));

                    boolean headerSkipped = false;
                    for (Row row : sheet) {
                        if (!headerSkipped) { headerSkipped = true; continue; }
                        List<String> colsList = new ArrayList<>();
                        int last = Math.max(10, row.getLastCellNum());
                        for (int i = 0; i < last; i++) {
                            colsList.add(getCellString(row.getCell(i)));
                        }
                        String[] cols = colsList.toArray(new String[0]);
                        importRow(projectId, userId, result, cols);
                    }
                }
            } else {
                throw new BusinessException("不支持的文件格式: " + lower);
            }
        } catch (Exception e) {
            log.error("Error importing test cases", e);
            result.getErrorMessages().add("导入失败: " + e.getMessage());
        }

        return result;
    }

    private void importRow(Long projectId, Long userId, TestCaseImportResult result, String[] cols) {
        try {
            TestCase tc = new TestCase();
            tc.setProjectId(projectId);
            tc.setTitle(getCsv(cols, 1));
            tc.setDescription(getCsv(cols, 2));
            tc.setType(getCsv(cols, 3));
            tc.setStatus(parseIntSafe(getCsv(cols, 4)));
            tc.setPriority(parseIntSafe(getCsv(cols, 5)));
            tc.setModule(getCsv(cols, 6));
            tc.setCreatorId(userId);
            tc.setAssigneeId(parseLongSafe(getCsv(cols, 8)));
            tc.setCreateTime(LocalDateTime.now());
            tc.setUpdateTime(LocalDateTime.now());
            testCaseMapper.insert(tc);
            result.setSuccessCount(result.getSuccessCount() + 1);
        } catch (Exception rowErr) {
            result.setFailureCount(result.getFailureCount() + 1);
            result.getErrorMessages().add("行导入失败: " + rowErr.getMessage());
        }
    }

    private String getCellString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    java.util.Date d = cell.getDateCellValue();
                    yield new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(d);
                } else {
                    yield String.valueOf((long) cell.getNumericCellValue());
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    private String safeTab(String s) {
        if (s == null) return "";
        return s.replace('\t', ' ').replace('\n', ' ').replace('\r', ' ');
    }

    private String nullToEmpty(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    private String[] parseCsvLine(String line) {
        // simple CSV split handling quoted values
        List<String> parts = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuote = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuote = !inQuote;
            } else if (c == ',' && !inQuote) {
                parts.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        parts.add(cur.toString());
        // unescape quotes
        for (int i = 0; i < parts.size(); i++) {
            String p = parts.get(i);
            if (p.startsWith("\"") && p.endsWith("\"")) {
                p = p.substring(1, p.length() - 1).replace("\"\"", "\"");
            }
            parts.set(i, p);
        }
        return parts.toArray(new String[0]);
    }

    private String getCsv(String[] cols, int idx) {
        return idx >= 0 && idx < cols.length ? cols[idx] : "";
    }

    private Integer parseIntSafe(String s) {
        try { return s == null || s.isBlank() ? null : Integer.parseInt(s.trim()); } catch (Exception e) { return null; }
    }

    private Long parseLongSafe(String s) {
        try { return s == null || s.isBlank() ? null : Long.parseLong(s.trim()); } catch (Exception e) { return null; }
    }
}
