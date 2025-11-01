package com.promanage.service.impl;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.promanage.common.domain.ResultCode;
import com.promanage.common.exception.BusinessException;
import com.promanage.infrastructure.tracing.Traceable;
import com.promanage.infrastructure.tracing.TracingUtil;
import com.promanage.service.IProjectActivityService;
import com.promanage.service.entity.Project;
import com.promanage.service.mapper.ProjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 项目追踪服务示例
 *
 * <p>展示如何在服务中使用分布式追踪功能
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectTracingService {

  private final TracingUtil tracingUtil;
  private final ProjectMapper projectMapper;
  private final IProjectActivityService projectActivityService;

  /** 示例：使用@Traceable注解的基本方法 */
  @Traceable(
      value = "processProjectData",
      tags = {"projectId", "operation"})
  public void processProjectData(Long projectId, String operation) {
    log.info("处理项目数据, projectId: {}, operation: {}", projectId, operation);

    // 模拟业务逻辑
    try {
      Thread.sleep(100); // 模拟处理时间

      // 记录业务事件
      tracingUtil.recordEvent("Data processing started", "projectId", String.valueOf(projectId));

      // 模拟数据处理
      Thread.sleep(200);

      // 记录完成事件
      tracingUtil.recordEvent("Data processing completed", "operation", operation);

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new BusinessException("数据处理中断");
    }
  }

  /** 示例：手动创建和管理Span */
  public void complexProjectOperation(Long projectId) {
    String traceId = tracingUtil.getCurrentTraceId();
    log.info("开始复杂项目操作, projectId: {}, traceId: {}", projectId, traceId);

    // 在Span中执行复杂操作
    Map<String, String> tags =
        Map.of(
            "projectId", String.valueOf(projectId),
            "operation", "complex-analysis",
            "userId", "12345");

    try {
      tracingUtil.runInSpan(
          "complexProjectAnalysis",
          tags,
          () -> {
            // 步骤1：数据验证
            validateProjectData(projectId);

            // 步骤2：数据预处理
            preprocessProjectData(projectId);

            // 步骤3：复杂计算
            performComplexCalculation(projectId);

            // 步骤4：结果验证
            validateResults(projectId);

            return null;
          });

    } catch (DataAccessException e) {
      log.error("复杂项目操作失败, projectId: {}", projectId, e);
      tracingUtil.recordException(e);
      throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "复杂项目操作失败");
    }
  }

  /** 示例：异步操作追踪 */
  public void asyncProjectOperation(Long projectId) {
    log.info("开始异步项目操作, projectId: {}", projectId);

    // 创建新的Span用于异步操作
    tracingUtil.runInSpan(
        "asyncProjectOperation",
        Map.of("projectId", String.valueOf(projectId)),
        () -> {
          // 异步处理
          CompletableFuture.runAsync(
              () -> {
                try {
                  // 模拟异步处理
                  Thread.sleep(1000);

                  // 记录异步事件
                  tracingUtil.recordEvent(
                      "Async processing completed", "projectId", String.valueOf(projectId));

                  // 记录活动
                  projectActivityService.recordActivity(
                      projectId, 0L, "ASYNC_OPERATION_COMPLETED", "异步操作完成");

                } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
                  log.error("异步操作中断", e);
                }
              });

          return null;
        });
  }

  /** 示例：带错误处理的追踪 */
  public void riskyProjectOperation(Long projectId) {
    log.info("开始风险项目操作, projectId: {}", projectId);

    try {
      tracingUtil.runInSpan(
          "riskyProjectOperation",
          Map.of("projectId", String.valueOf(projectId)),
          () -> {
            // 步骤1：检查项目状态
            Project project = projectMapper.selectById(projectId);
            if (project == null || project.getDeleted()) {
              throw new BusinessException(ResultCode.PROJECT_NOT_FOUND, "项目不存在");
            }

            // 步骤2：执行风险操作
            performRiskyOperation(project);

            return null;
          });

    } catch (BusinessException e) {
      // 业务异常，记录但不重新抛出
      log.warn("业务异常, projectId: {}, message: {}", projectId, e.getMessage());
      tracingUtil.recordException(e);
      throw e;

    } catch (DataAccessException e) {
      // 系统异常，记录并重新抛出
      log.error("系统异常, projectId: {}", projectId, e);
      tracingUtil.recordException(e);
      throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "系统异常");
    }
  }

  /** 示例：性能监控追踪 */
  @Traceable(
      value = "performanceCriticalOperation",
      tags = {"projectId", "dataSize"})
  public void performanceCriticalOperation(Long projectId, int dataSize) {
    long startTime = System.currentTimeMillis();

    try {
      // 添加性能相关标签
      tracingUtil.addTags(
          Map.of(
              "data.size", String.valueOf(dataSize),
              "operation.type", "performance-critical",
              "expected.duration", "<1000ms"));

      // 执行性能关键操作
      performPerformanceCriticalOperation(projectId, dataSize);

      // 记录性能指标
      long duration = System.currentTimeMillis() - startTime;
      tracingUtil.addTag("actual.duration", duration + "ms");
      tracingUtil.addTag("performance.status", duration < 1000 ? "good" : "slow");

      log.info(
          "性能关键操作完成, projectId: {}, duration: {}ms, dataSize: {}", projectId, duration, dataSize);

    } catch (DataAccessException e) {
      log.error("性能关键操作失败, projectId: {}", projectId, e);
      tracingUtil.recordException(e);
      throw e;
    }
  }

  // ===== 私有辅助方法 =====

  private void validateProjectData(Long projectId) {
    tracingUtil.recordEvent("Data validation started");
    // 模拟数据验证逻辑
    log.debug("验证项目数据, projectId: {}", projectId);
  }

  private void preprocessProjectData(Long projectId) {
    tracingUtil.recordEvent("Data preprocessing started");
    // 模拟数据预处理逻辑
    log.debug("预处理项目数据, projectId: {}", projectId);
  }

  private void performComplexCalculation(Long projectId) {
    tracingUtil.recordEvent("Complex calculation started");
    // 模拟复杂计算逻辑
    log.debug("执行复杂计算, projectId: {}", projectId);
  }

  private void validateResults(Long projectId) {
    tracingUtil.recordEvent("Results validation started");
    // 模拟结果验证逻辑
    log.debug("验证结果, projectId: {}", projectId);
  }

  private void performRiskyOperation(Project project) {
    tracingUtil.recordEvent("Risky operation started");
    // 模拟风险操作
    log.debug("执行风险操作, projectId: {}", project.getId());

    // 模拟可能失败的操作
    if (Math.random() < 0.3) {
      throw new BusinessException(ResultCode.OPERATION_FAILED, "风险操作失败");
    }
  }

  private void performPerformanceCriticalOperation(Long projectId, int dataSize) {
    tracingUtil.recordEvent(
        "Performance critical operation started", "dataSize", String.valueOf(dataSize));

    // 模拟性能关键操作
    try {
      Thread.sleep(dataSize * 10); // 模拟处理时间与数据大小成正比

      tracingUtil.recordEvent("Performance critical operation completed");
      log.debug("性能关键操作完成, projectId: {}, dataSize: {}", projectId, dataSize);

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new BusinessException("操作中断");
    }
  }
}
