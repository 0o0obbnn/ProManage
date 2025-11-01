package com.promanage.service.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.promanage.common.exception.BusinessException;
import com.promanage.common.result.PageResult;
import com.promanage.service.IProjectService;
import com.promanage.service.entity.TestCase;
import com.promanage.service.entity.TestExecution;
import com.promanage.service.mapper.TestCaseMapper;
import com.promanage.service.service.ITestCaseService;
import com.promanage.service.strategy.TestCaseExecutionStrategy;
import com.promanage.service.strategy.TestCaseImportExportStrategy;
import com.promanage.service.strategy.TestCaseQueryStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 测试用例服务实现类
 * 使用策略模式减少方法数量
 *
 * @author ProManage Team
 * @date 2025-10-22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestCaseServiceImpl implements ITestCaseService {

  private final TestCaseMapper testCaseMapper;
  private final IProjectService projectService;
    
    // 策略类
    private final TestCaseQueryStrategy queryStrategy;
    private final TestCaseExecutionStrategy executionStrategy;
    private final TestCaseImportExportStrategy importExportStrategy;

    // ==================== 核心CRUD方法 ====================

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Long createTestCase(TestCase testCase) {
        validateTestCase(testCase);
        testCase.setCreateTime(java.time.LocalDateTime.now());
        testCase.setUpdateTime(java.time.LocalDateTime.now());
    testCaseMapper.insert(testCase);
    return testCase.getId();
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void updateTestCase(TestCase testCase) {
        validateTestCase(testCase);
        testCase.setUpdateTime(java.time.LocalDateTime.now());
    testCaseMapper.updateById(testCase);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void deleteTestCase(Long testCaseId, Long userId) {
    // Check permission
    if (!hasTestCaseDeletePermission(testCaseId, userId)) {
      throw new BusinessException("无权限删除此测试用例");
    }
    testCaseMapper.deleteById(testCaseId);
  }

  @Override
    public TestCase getTestCaseById(Long id) {
        return queryStrategy.getTestCaseById(id);
    }

    // ==================== 查询方法 ====================

    // Internal helper method (not in interface)
    public PageResult<TestCase> getTestCases(Long projectId, String keyword, String status,
                                           String priority, String type, int page, int pageSize) {
        return queryStrategy.getTestCases(projectId, keyword, status, priority, type, page, pageSize);
  }

  // Internal helper method (not in interface)
    public List<TestCase> getTestCasesByProjectId(Long projectId) {
        return queryStrategy.getTestCasesByProjectId(projectId);
    }

  // Internal helper method (not in interface)
    public long getTestCaseCount(Long projectId) {
        return queryStrategy.getTestCaseCount(projectId);
  }

  // Internal helper method (not in interface)
    public long getTestCaseCountByStatus(Long projectId, String status) {
        return queryStrategy.getTestCaseCountByStatus(projectId, status);
    }

    // ==================== 执行方法 ====================

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void executeTestCase(
      Long testCaseId,
      String result,
      String actualResult,
      String failureReason,
      Integer actualTime,
      String executionEnvironment,
      String notes,
      String[] attachments,
      Long executorId) {
    // Delegate to strategy with converted parameters
    executionStrategy.executeTestCase(testCaseId, executorId.toString(), result, notes);
  }

    // Internal helper method (not in interface)
  @Transactional(rollbackFor = Exception.class)
    public TestExecution executeTestCase(Long testCaseId, String executor, String result, String notes) {
        return executionStrategy.executeTestCase(testCaseId, executor, result, notes);
    }

    // Internal helper method (not in interface)
    public List<TestExecution> getExecutionHistory(Long testCaseId) {
        return executionStrategy.getExecutionHistory(testCaseId);
    }

    // Internal helper method (not in interface)
    public TestExecution getLastExecution(Long testCaseId) {
        return executionStrategy.getLastExecution(testCaseId);
    }

    // Internal helper method (not in interface)
    public long getExecutionCount(Long projectId) {
        return executionStrategy.getExecutionCount(projectId);
    }

    // ==================== 导入导出方法 ====================

  @Override
    public String exportTestCases(Long projectId, List<Long> testCaseIds, String format) {
        List<TestCase> testCases = getTestCasesForExport(projectId, testCaseIds);

        try {
            byte[] exportData;
            String fileExtension;

            switch (format.toUpperCase()) {
                case "EXCEL":
                case "XLSX":
                    exportData = importExportStrategy.exportToExcel(testCases);
                    fileExtension = ".xlsx";
                    break;
                case "CSV":
                    exportData = importExportStrategy.exportToCsv(testCases);
                    fileExtension = ".csv";
                    break;
                case "TSV":
                    exportData = importExportStrategy.exportToTsv(testCases);
                    fileExtension = ".tsv";
                    break;
                default:
                    throw new BusinessException("不支持的导出格式: " + format);
            }

            // 生成临时文件并返回文件URL
            // TODO: 集成MinIO或其他文件存储服务以持久化存储
            java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("testcase_export_", fileExtension);
            java.nio.file.Files.write(tempFile, exportData);

            // 返回文件URL (临时实现，返回file:// URL)
            String fileUrl = tempFile.toUri().toString();
            log.info("测试用例导出成功, projectId={}, fileUrl={}", projectId, fileUrl);

            return fileUrl;

        } catch (Exception e) {
            log.error("导出测试用例失败", e);
            throw new BusinessException("导出失败: " + e.getMessage(), e);
        }
    }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public TestCaseImportResult importTestCases(Long projectId, String fileUrl, Long userId) {
    TestCaseImportResult result = new TestCaseImportResult();
    result.setErrorMessages(new java.util.ArrayList<>());

    try {
      // 验证项目访问权限
      if (!projectService.isProjectMember(projectId, userId)) {
        throw new BusinessException("无权限访问该项目");
      }

      // 从URL确定文件格式
      String format = determineFileFormat(fileUrl);

      // 读取文件内容
      byte[] fileData = readFileFromUrl(fileUrl);

      // 解析测试用例
      List<TestCase> testCases;
      switch (format.toUpperCase()) {
        case "EXCEL":
        case "XLSX":
        case "XLS":
          testCases = importExportStrategy.importFromExcel(fileData, projectId);
          break;
        case "CSV":
          testCases = importExportStrategy.importFromCsv(new String(fileData, java.nio.charset.StandardCharsets.UTF_8), projectId);
          break;
        default:
          throw new BusinessException("不支持的导入格式: " + format);
      }

      // 批量保存并统计
      int totalCount = testCases.size();
      int successCount = 0;
      int failureCount = 0;

      for (TestCase testCase : testCases) {
        try {
          testCase.setCreatedBy(userId);
          testCase.setUpdatedBy(userId);
          testCaseMapper.insert(testCase);
          successCount++;
        } catch (Exception e) {
          failureCount++;
          String errorMsg = String.format("保存测试用例失败: %s - %s",
              testCase.getTitle(), e.getMessage());
          result.getErrorMessages().add(errorMsg);
          log.error(errorMsg, e);
        }
      }

      result.setTotalCount(totalCount);
      result.setSuccessCount(successCount);
      result.setFailureCount(failureCount);

      log.info("导入测试用例完成 - 项目ID: {}, 总数: {}, 成功: {}, 失败: {}",
          projectId, totalCount, successCount, failureCount);

      return result;

    } catch (Exception e) {
      log.error("导入测试用例失败", e);
      result.setTotalCount(0);
      result.setSuccessCount(0);
      result.setFailureCount(0);
      result.getErrorMessages().add("导入失败: " + e.getMessage());
      return result;
    }
  }

  /**
   * 从文件URL确定文件格式
   */
  private String determineFileFormat(String fileUrl) {
    if (fileUrl == null || fileUrl.isEmpty()) {
      throw new BusinessException("文件URL不能为空");
    }

    String lowerUrl = fileUrl.toLowerCase();
    if (lowerUrl.endsWith(".xlsx") || lowerUrl.endsWith(".xls")) {
      return "EXCEL";
    } else if (lowerUrl.endsWith(".csv")) {
      return "CSV";
    } else if (lowerUrl.endsWith(".tsv")) {
      return "TSV";
    } else {
      throw new BusinessException("无法识别的文件格式，请使用 .xlsx, .xls, .csv 或 .tsv 文件");
    }
  }

  /**
   * 从URL读取文件内容
   */
  private byte[] readFileFromUrl(String fileUrl) throws java.io.IOException {
    // 如果是本地文件路径
    if (fileUrl.startsWith("file://") || fileUrl.startsWith("/") || fileUrl.matches("^[a-zA-Z]:.*")) {
      String filePath = fileUrl.replace("file://", "");
      java.nio.file.Path path = java.nio.file.Paths.get(filePath);
      return java.nio.file.Files.readAllBytes(path);
    }

    // 如果是HTTP/HTTPS URL
    if (fileUrl.startsWith("http://") || fileUrl.startsWith("https://")) {
      java.net.URI uri = java.net.URI.create(fileUrl);
      java.net.URL url = uri.toURL();
      try (java.io.InputStream is = url.openStream()) {
        return is.readAllBytes();
      }
    }

    // 默认当作本地文件路径处理
    java.nio.file.Path path = java.nio.file.Paths.get(fileUrl);
    return java.nio.file.Files.readAllBytes(path);
  }

  // ==================== Required Interface Methods ====================

  @Override
  public PageResult<TestCase> listTestCases(
      Long projectId,
      Integer page,
      Integer pageSize,
      Integer status,
      Integer priority,
      String type,
      Long assigneeId,
      Long creatorId,
      String moduleId,
      String keyword,
      String tags) {
    // TODO: Implement with query strategy
    return PageResult.<TestCase>builder()
        .list(java.util.Collections.emptyList())
        .total(0L)
        .page(page != null ? page : 1)
        .pageSize(pageSize != null ? pageSize : 20)
        .build();
  }

  @Override
  public PageResult<TestCase> listTestCasesByAssignee(
      Long userId, Integer page, Integer pageSize, Integer status) {
    // TODO: Implement with query strategy
    return PageResult.<TestCase>builder()
        .list(java.util.Collections.emptyList())
        .total(0L)
        .page(page != null ? page : 1)
        .pageSize(pageSize != null ? pageSize : 20)
        .build();
  }

  @Override
  public PageResult<TestCase> listTestCasesByCreator(
      Long userId, Integer page, Integer pageSize, Integer status) {
    // TODO: Implement with query strategy
    return PageResult.<TestCase>builder()
        .list(java.util.Collections.emptyList())
        .total(0L)
        .page(page != null ? page : 1)
        .pageSize(pageSize != null ? pageSize : 20)
        .build();
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void updateTestCaseStatus(Long testCaseId, Integer status, Long userId) {
    TestCase testCase = getTestCaseById(testCaseId);
    if (testCase == null) {
      throw new BusinessException("测试用例不存在");
    }
    testCase.setStatus(status);
    testCase.setUpdateTime(java.time.LocalDateTime.now());
    testCaseMapper.updateById(testCase);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void assignTestCase(Long testCaseId, Long assigneeId, Long userId) {
    TestCase testCase = getTestCaseById(testCaseId);
    if (testCase == null) {
      throw new BusinessException("测试用例不存在");
    }
    testCase.setAssigneeId(assigneeId);
    testCase.setUpdateTime(java.time.LocalDateTime.now());
    testCaseMapper.updateById(testCase);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void batchAssignTestCases(List<Long> testCaseIds, Long assigneeId, Long userId) {
    for (Long testCaseId : testCaseIds) {
      try {
        assignTestCase(testCaseId, assigneeId, userId);
      } catch (Exception e) {
        log.error("批量分配测试用例失败, testCaseId={}", testCaseId, e);
      }
    }
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void batchUpdateTestCaseStatus(List<Long> testCaseIds, Integer status, Long userId) {
    for (Long testCaseId : testCaseIds) {
      try {
        updateTestCaseStatus(testCaseId, status, userId);
      } catch (Exception e) {
        log.error("批量更新测试用例状态失败, testCaseId={}", testCaseId, e);
      }
    }
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void batchDeleteTestCases(List<Long> testCaseIds, Long userId) {
    for (Long testCaseId : testCaseIds) {
      try {
        deleteTestCase(testCaseId, userId);
      } catch (Exception e) {
        log.error("批量删除测试用例失败, testCaseId={}", testCaseId, e);
      }
    }
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Long copyTestCase(Long testCaseId, String newTitle, Long userId) {
    TestCase sourceTestCase = getTestCaseById(testCaseId);
    if (sourceTestCase == null) {
      throw new BusinessException("源测试用例不存在");
    }

    TestCase newTestCase = new TestCase();
    newTestCase.setTitle(newTitle);
    newTestCase.setDescription(sourceTestCase.getDescription());
    newTestCase.setProjectId(sourceTestCase.getProjectId());
    newTestCase.setPriority(sourceTestCase.getPriority());
    newTestCase.setType(sourceTestCase.getType());
    newTestCase.setStatus(1); // Draft status
    newTestCase.setCreatedBy(userId);
    newTestCase.setCreateTime(java.time.LocalDateTime.now());
    newTestCase.setUpdateTime(java.time.LocalDateTime.now());

    testCaseMapper.insert(newTestCase);
    return newTestCase.getId();
  }

  @Override
  public PageResult<ITestCaseService.TestCaseExecutionHistory> listTestCaseExecutionHistory(
      Long testCaseId, Integer page, Integer pageSize) {
    // TODO: Implement execution history query
    return PageResult.<ITestCaseService.TestCaseExecutionHistory>builder()
        .list(java.util.Collections.emptyList())
        .total(0L)
        .page(page != null ? page : 1)
        .pageSize(pageSize != null ? pageSize : 20)
        .build();
  }

  @Override
  public ITestCaseService.TestCaseStatistics getTestCaseStatistics(Long projectId) {
    // TODO: Implement statistics aggregation
    ITestCaseService.TestCaseStatistics stats = new ITestCaseService.TestCaseStatistics();
    stats.setTotalCount(0);
    stats.setPassedCount(0);
    stats.setFailedCount(0);
    stats.setPassRate(0.0);
    return stats;
  }

  @Override
  public ITestCaseService.TestCaseExecutionStatistics getTestCaseExecutionStatistics(Long testCaseId) {
    // TODO: Implement execution statistics aggregation
    ITestCaseService.TestCaseExecutionStatistics stats = new ITestCaseService.TestCaseExecutionStatistics();
    stats.setTotalExecutions(0);
    stats.setPassCount(0);
    stats.setFailCount(0);
    stats.setPassRate(0.0);
    return stats;
  }

    // ==================== 权限检查方法 ====================

  @Override
  public boolean hasTestCaseViewPermission(Long testCaseId, Long userId) {
        TestCase testCase = getTestCaseById(testCaseId);
        if (testCase == null) {
        return false;
      }
        return projectService.isProjectMember(testCase.getProjectId(), userId);
  }

  @Override
  public boolean hasTestCaseEditPermission(Long testCaseId, Long userId) {
        TestCase testCase = getTestCaseById(testCaseId);
        if (testCase == null) {
        return false;
      }
        return projectService.isProjectAdmin(testCase.getProjectId(), userId);
  }

  @Override
  public boolean hasTestCaseDeletePermission(Long testCaseId, Long userId) {
        TestCase testCase = getTestCaseById(testCaseId);
        if (testCase == null) {
        return false;
      }
        return projectService.isProjectAdmin(testCase.getProjectId(), userId);
  }

  @Override
  public boolean hasTestCaseExecutePermission(Long testCaseId, Long userId) {
        TestCase testCase = getTestCaseById(testCaseId);
        if (testCase == null) {
        return false;
      }
        return projectService.isProjectMember(testCase.getProjectId(), userId);
  }

  @Override
  public boolean hasProjectTestCaseViewPermission(Long projectId, Long userId) {
        return projectService.isProjectMember(projectId, userId);
  }

  @Override
  public boolean hasProjectTestCaseCreatePermission(Long projectId, Long userId) {
        return projectService.isProjectAdmin(projectId, userId);
    }

    // ==================== 私有辅助方法 ====================

    private void validateTestCase(TestCase testCase) {
        if (testCase == null) {
            throw new BusinessException("测试用例信息不能为空");
        }
        if (testCase.getTitle() == null || testCase.getTitle().trim().isEmpty()) {
            throw new BusinessException("测试用例标题不能为空");
        }
        if (testCase.getProjectId() == null) {
            throw new BusinessException("项目ID不能为空");
        }
    }

    private List<TestCase> getTestCasesForExport(Long projectId, List<Long> testCaseIds) {
        LambdaQueryWrapper<TestCase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TestCase::getProjectId, projectId);
        
        if (testCaseIds != null && !testCaseIds.isEmpty()) {
            wrapper.in(TestCase::getId, testCaseIds);
        }
        
        wrapper.orderByDesc(TestCase::getCreateTime);
        return testCaseMapper.selectList(wrapper);
  }
}
