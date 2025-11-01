package com.promanage.service.mapper;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.promanage.service.entity.TestExecution;

/**
 * 测试执行历史Mapper接口
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-09
 */
@Mapper
public interface TestExecutionMapper extends BaseMapper<TestExecution> {

  /**
   * 获取测试用例的执行统计信息
   *
   * @param testCaseId 测试用例ID
   * @return 统计信息Map，包含total, pass, fail, block, skip, avgTime
   */
  @Select(
      "SELECT "
          + "COUNT(*) as total, "
          + "SUM(CASE WHEN result = 0 THEN 1 ELSE 0 END) as pass, "
          + "SUM(CASE WHEN result = 1 THEN 1 ELSE 0 END) as fail, "
          + "SUM(CASE WHEN result = 2 THEN 1 ELSE 0 END) as block, "
          + "SUM(CASE WHEN result = 3 THEN 1 ELSE 0 END) as skip, "
          + "AVG(CASE WHEN execution_time IS NOT NULL THEN execution_time ELSE 0 END) as avgTime "
          + "FROM tb_test_execution "
          + "WHERE test_case_id = #{testCaseId} AND deleted = FALSE")
  Map<String, Object> getExecutionStatistics(@Param("testCaseId") Long testCaseId);

  /**
   * 获取项目的测试用例统计信息
   *
   * @param projectId 项目ID
   * @return 统计信息Map
   */
  @Select(
      "SELECT "
          + "COUNT(DISTINCT tc.id) as totalCount, "
          + "SUM(CASE WHEN tc.status = 0 THEN 1 ELSE 0 END) as draftCount, "
          + "SUM(CASE WHEN tc.status = 1 THEN 1 ELSE 0 END) as pendingCount, "
          + "SUM(CASE WHEN tc.status = 2 THEN 1 ELSE 0 END) as inProgressCount, "
          + "SUM(CASE WHEN tc.status = 3 THEN 1 ELSE 0 END) as passedCount, "
          + "SUM(CASE WHEN tc.status = 4 THEN 1 ELSE 0 END) as failedCount, "
          + "SUM(CASE WHEN tc.status = 5 THEN 1 ELSE 0 END) as blockedCount, "
          + "SUM(CASE WHEN tc.status = 6 THEN 1 ELSE 0 END) as skippedCount, "
          + "COUNT(te.id) as totalExecutions, "
          + "MAX(te.create_time) as lastExecutionTime "
          + "FROM tb_test_case tc "
          + "LEFT JOIN tb_test_execution te ON tc.id = te.test_case_id AND te.deleted = FALSE "
          + "WHERE tc.project_id = #{projectId} AND tc.deleted = FALSE")
  Map<String, Object> getProjectStatistics(@Param("projectId") Long projectId);
}
