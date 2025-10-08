package com.promanage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.promanage.common.domain.PageResult;
import com.promanage.dto.TaskTimeTrackingDTO;
import com.promanage.service.entity.TaskTimeTracking;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 任务时间追踪服务类
 * </p>
 *
 * @author ProManage Team
 * @since 2025-10-05
 */
public interface ITaskTimeTrackingService extends IService<TaskTimeTracking> {

    /**
     * 分页查询任务时间追踪记录
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     * @param projectId 项目ID
     * @param status 状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    PageResult<TaskTimeTrackingDTO> listTimeTracking(Long taskId, Long userId, Long projectId, 
                                                   Integer status, LocalDate startDate, LocalDate endDate,
                                                   Integer page, Integer size);

    /**
     * 根据任务ID获取时间追踪记录
     *
     * @param taskId 任务ID
     * @return 时间追踪记录列表
     */
    List<TaskTimeTrackingDTO> getTimeTrackingByTaskId(Long taskId);

    /**
     * 根据用户ID获取时间追踪记录
     *
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 时间追踪记录列表
     */
    List<TaskTimeTrackingDTO> getTimeTrackingByUserId(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * 根据ID获取时间追踪记录
     *
     * @param id 记录ID
     * @return 时间追踪DTO
     */
    TaskTimeTrackingDTO getTimeTrackingById(Long id);

    /**
     * 创建时间追踪记录
     *
     * @param timeTrackingDTO 时间追踪DTO
     * @return 创建的记录ID
     */
    Long createTimeTracking(TaskTimeTrackingDTO timeTrackingDTO);

    /**
     * 更新时间追踪记录
     *
     * @param id 记录ID
     * @param timeTrackingDTO 时间追踪DTO
     * @return 是否成功
     */
    boolean updateTimeTracking(Long id, TaskTimeTrackingDTO timeTrackingDTO);

    /**
     * 删除时间追踪记录
     *
     * @param id 记录ID
     * @return 是否成功
     */
    boolean deleteTimeTracking(Long id);

    /**
     * 开始任务计时
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     * @param workDescription 工作描述
     * @return 记录ID
     */
    Long startTimeTracking(Long taskId, Long userId, String workDescription);

    /**
     * 结束任务计时
     *
     * @param id 记录ID
     * @return 是否成功
     */
    boolean endTimeTracking(Long id);

    /**
     * 暂停任务计时
     *
     * @param id 记录ID
     * @return 是否成功
     */
    boolean pauseTimeTracking(Long id);

    /**
     * 恢复任务计时
     *
     * @param id 记录ID
     * @return 是否成功
     */
    boolean resumeTimeTracking(Long id);

    /**
     * 获取任务总工时
     *
     * @param taskId 任务ID
     * @return 总工时
     */
    Double getTotalHoursByTask(Long taskId);

    /**
     * 获取用户在指定时间范围内的总工时
     *
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 总工时
     */
    Double getTotalHoursByUser(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取项目总工时
     *
     * @param projectId 项目ID
     * @return 总工时
     */
    Double getTotalHoursByProject(Long projectId);
}