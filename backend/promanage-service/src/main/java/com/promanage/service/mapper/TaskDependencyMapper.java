package com.promanage.service.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.promanage.service.entity.TaskDependency;

/**
 * 任务依赖关系Mapper接口
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-03
 */
@Mapper
public interface TaskDependencyMapper extends BaseMapper<TaskDependency> {

  /**
   * 查询任务的所有前置任务ID列表
   *
   * @param taskId 任务ID
   * @return 前置任务ID列表
   */
  @Select("SELECT prerequisite_task_id FROM tb_task_dependency WHERE dependent_task_id = #{taskId}")
  List<Long> findPrerequisiteTaskIds(@Param("taskId") Long taskId);

  /**
   * 查询任务的所有依赖任务ID列表（依赖当前任务的任务）
   *
   * @param taskId 任务ID
   * @return 依赖任务ID列表
   */
  @Select("SELECT dependent_task_id FROM tb_task_dependency WHERE prerequisite_task_id = #{taskId}")
  List<Long> findDependentTaskIds(@Param("taskId") Long taskId);

  /**
   * 检查两个任务之间是否存在依赖关系
   *
   * @param prerequisiteTaskId 前置任务ID
   * @param dependentTaskId 依赖任务ID
   * @return true表示存在依赖关系
   */
  @Select(
      "SELECT COUNT(*) > 0 FROM tb_task_dependency WHERE prerequisite_task_id = #{prerequisiteTaskId} AND dependent_task_id = #{dependentTaskId}")
  boolean existsDependency(
      @Param("prerequisiteTaskId") Long prerequisiteTaskId,
      @Param("dependentTaskId") Long dependentTaskId);

  /**
   * 删除任务依赖关系
   *
   * @param prerequisiteTaskId 前置任务ID
   * @param dependentTaskId 依赖任务ID
   * @return 删除的记录数
   */
  @Delete(
      "DELETE FROM tb_task_dependency WHERE prerequisite_task_id = #{prerequisiteTaskId} AND dependent_task_id = #{dependentTaskId}")
  int deleteDependency(
      @Param("prerequisiteTaskId") Long prerequisiteTaskId,
      @Param("dependentTaskId") Long dependentTaskId);

  /**
   * 删除任务的所有依赖关系（作为前置任务或依赖任务）
   *
   * @param taskId 任务ID
   * @return 删除的记录数
   */
  @Delete(
      "DELETE FROM tb_task_dependency WHERE prerequisite_task_id = #{taskId} OR dependent_task_id = #{taskId}")
  int deleteAllDependenciesByTaskId(@Param("taskId") Long taskId);
}
