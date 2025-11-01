package com.promanage.service.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.promanage.service.entity.TaskTimeTracking;

/**
 * 任务时间追踪 Mapper 接口
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-05
 */
@Mapper
public interface TaskTimeTrackingMapper extends BaseMapper<TaskTimeTracking> {

  /**
   * 根据任务ID获取时间追踪记录
   *
   * @param taskId 任务ID
   * @return 时间追踪记录列表
   */
  List<TaskTimeTracking> selectByTaskId(@Param("taskId") Long taskId);

  /**
   * 根据用户ID获取时间追踪记录
   *
   * @param userId 用户ID
   * @return 时间追踪记录列表
   */
  List<TaskTimeTracking> selectByUserId(@Param("userId") Long userId);

  /**
   * 根据任务ID和用户ID获取时间追踪记录
   *
   * @param taskId 任务ID
   * @param userId 用户ID
   * @return 时间追踪记录列表
   */
  List<TaskTimeTracking> selectByTaskIdAndUserId(
      @Param("taskId") Long taskId, @Param("userId") Long userId);

  /**
   * 统计任务的总预估工时
   *
   * @param taskId 任务ID
   * @return 总预估工时
   */
  Double sumEstimatedHoursByTaskId(@Param("taskId") Long taskId);

  /**
   * 统计任务的总实际工时
   *
   * @param taskId 任务ID
   * @return 总实际工时
   */
  Double sumActualHoursByTaskId(@Param("taskId") Long taskId);

  /**
   * 统计用户在指定时间范围内的工时
   *
   * @param userId 用户ID
   * @param startTime 开始时间
   * @param endTime 结束时间
   * @return 总工时
   */
  Double sumHoursByUserIdAndTimeRange(
      @Param("userId") Long userId,
      @Param("startTime") LocalDateTime startTime,
      @Param("endTime") LocalDateTime endTime);
}
