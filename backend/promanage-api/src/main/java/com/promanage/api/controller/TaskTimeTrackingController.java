package com.promanage.api.controller;

import com.promanage.api.dto.request.CreateTimeTrackingRequest;
import com.promanage.api.dto.request.UpdateTimeTrackingRequest;
import com.promanage.common.result.PageResult;
import com.promanage.common.domain.Result;
import com.promanage.common.exception.BusinessException;
import com.promanage.infrastructure.utils.SecurityUtils;
import com.promanage.dto.TaskTimeTrackingDTO;
import com.promanage.service.ITaskTimeTrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 任务时间追踪控制器
 * <p>
 * 提供任务时间追踪的创建、查询、更新、删除以及计时控制功能
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-05
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/time-tracking")
@Tag(name = "任务时间追踪", description = "任务时间追踪相关接口")
@RequiredArgsConstructor
public class TaskTimeTrackingController {

    private final ITaskTimeTrackingService timeTrackingService;

    /**
     * 分页查询时间追踪记录
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     * @param projectId 项目ID
     * @param status 状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param page 页码
     * @param size 每页大小
     * @return 时间追踪记录列表
     */
    @GetMapping
    @Operation(summary = "分页查询时间追踪记录", description = "根据条件分页查询任务时间追踪记录")
    public Result<PageResult<TaskTimeTrackingDTO>> getTimeTrackingList(
            @Parameter(description = "任务ID") @RequestParam(required = false) Long taskId,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "项目ID") @RequestParam(required = false) Long projectId,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "开始日期") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer size) {

        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("获取时间追踪记录列表, currentUserId={}, taskId={}, userId={}, projectId={}, status={}, startDate={}, endDate={}, page={}, size={}", 
                currentUserId, taskId, userId, projectId, status, startDate, endDate, page, size);

        // 如果没有指定userId，则默认查询当前用户的记录
        if (userId == null) {
            userId = currentUserId;
        }

        PageResult<TaskTimeTrackingDTO> result = timeTrackingService.listTimeTracking(
                taskId, userId, projectId, status, startDate, endDate, page, size);

        log.info("获取时间追踪记录列表成功, 总数: {}", result.getTotal());
        return Result.success(result);
    }

    /**
     * 根据任务ID获取时间追踪记录
     *
     * @param taskId 任务ID
     * @return 时间追踪记录列表
     */
    @GetMapping("/task/{taskId}")
    @Operation(summary = "根据任务ID获取时间追踪记录", description = "获取指定任务的所有时间追踪记录")
    public Result<List<TaskTimeTrackingDTO>> getTimeTrackingByTaskId(
            @Parameter(description = "任务ID") @PathVariable Long taskId) {

        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("根据任务ID获取时间追踪记录, taskId={}, currentUserId={}", taskId, currentUserId);

        List<TaskTimeTrackingDTO> result = timeTrackingService.getTimeTrackingByTaskId(taskId);

        log.info("根据任务ID获取时间追踪记录成功, 记录数: {}", result.size());
        return Result.success(result);
    }

    /**
     * 根据用户ID获取时间追踪记录
     *
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 时间追踪记录列表
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "根据用户ID获取时间追踪记录", description = "获取指定用户的时间追踪记录")
    public Result<List<TaskTimeTrackingDTO>> getTimeTrackingByUserId(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "开始日期") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("根据用户ID获取时间追踪记录, userId={}, currentUserId={}, startDate={}, endDate={}", 
                userId, currentUserId, startDate, endDate);

        // 普通用户只能查询自己的记录
        if (!currentUserId.equals(userId)) {
            throw new BusinessException("没有权限查询其他用户的记录");
        }

        List<TaskTimeTrackingDTO> result = timeTrackingService.getTimeTrackingByUserId(userId, startDate, endDate);

        log.info("根据用户ID获取时间追踪记录成功, 记录数: {}", result.size());
        return Result.success(result);
    }

    /**
     * 创建时间追踪记录
     *
     * @param request 创建时间追踪请求
     * @return 创建的记录信息
     */
    @PostMapping
    @Operation(summary = "创建时间追踪记录", description = "创建一个新的时间追踪记录")
    public Result<TaskTimeTrackingDTO> createTimeTracking(@Valid @RequestBody CreateTimeTrackingRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("创建时间追踪记录, currentUserId={}, taskId={}", currentUserId, request.getTaskId());

        // 设置当前用户ID
        TaskTimeTrackingDTO dto = new TaskTimeTrackingDTO();
        dto.setTaskId(request.getTaskId());
        dto.setUserId(currentUserId);
        dto.setEstimatedHours(request.getEstimatedHours());
        dto.setWorkDate(request.getWorkDate());
        dto.setWorkDescription(request.getWorkDescription());

        Long id = timeTrackingService.createTimeTracking(dto);
        TaskTimeTrackingDTO result = timeTrackingService.getTimeTrackingById(id);

        log.info("创建时间追踪记录成功, id={}", id);
        return Result.success(result);
    }

    /**
     * 更新时间追踪记录
     *
     * @param id 记录ID
     * @param request 更新时间追踪请求
     * @return 更新后的记录信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新时间追踪记录", description = "更新时间追踪记录信息")
    public Result<TaskTimeTrackingDTO> updateTimeTracking(
            @Parameter(description = "记录ID") @PathVariable Long id,
            @Valid @RequestBody UpdateTimeTrackingRequest request) {

        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("更新时间追踪记录, id={}, currentUserId={}", id, currentUserId);

        // 检查权限
        TaskTimeTrackingDTO existing = timeTrackingService.getTimeTrackingById(id);
        if (existing == null) {
            throw new BusinessException("时间追踪记录不存在");
        }
        if (!currentUserId.equals(existing.getUserId())) {
            throw new BusinessException("没有权限修改此记录");
        }

        TaskTimeTrackingDTO dto = new TaskTimeTrackingDTO();
        dto.setEstimatedHours(request.getEstimatedHours());
        dto.setActualHours(request.getActualHours());
        dto.setWorkDate(request.getWorkDate());
        dto.setWorkDescription(request.getWorkDescription());

        boolean success = timeTrackingService.updateTimeTracking(id, dto);
        if (!success) {
            throw new BusinessException("更新失败");
        }

        TaskTimeTrackingDTO result = timeTrackingService.getTimeTrackingById(id);

        log.info("更新时间追踪记录成功, id={}", id);
        return Result.success(result);
    }

    /**
     * 删除时间追踪记录
     *
     * @param id 记录ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除时间追踪记录", description = "删除指定的时间追踪记录")
    public Result<Void> deleteTimeTracking(@Parameter(description = "记录ID") @PathVariable Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("删除时间追踪记录, id={}, currentUserId={}", id, currentUserId);

        // 检查权限
        TaskTimeTrackingDTO existing = timeTrackingService.getTimeTrackingById(id);
        if (existing == null) {
            throw new BusinessException("时间追踪记录不存在");
        }
        if (!currentUserId.equals(existing.getUserId())) {
            throw new BusinessException("没有权限删除此记录");
        }

        boolean success = timeTrackingService.deleteTimeTracking(id);
        if (!success) {
            throw new BusinessException("删除失败");
        }

        log.info("删除时间追踪记录成功, id={}", id);
        return Result.success();
    }

    /**
     * 开始任务计时
     *
     * @param taskId 任务ID
     * @param workDescription 工作描述
     * @return 记录ID
     */
    @PostMapping("/start")
    @Operation(summary = "开始任务计时", description = "开始对指定任务进行计时")
    public Result<Long> startTimeTracking(
            @Parameter(description = "任务ID") @RequestParam Long taskId,
            @Parameter(description = "工作描述") @RequestParam(required = false) String workDescription) {

        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("开始任务计时, taskId={}, currentUserId={}", taskId, currentUserId);

        Long id = timeTrackingService.startTimeTracking(taskId, currentUserId, workDescription);

        log.info("开始任务计时成功, id={}", id);
        return Result.success(id);
    }

    /**
     * 结束任务计时
     *
     * @param id 记录ID
     * @return 操作结果
     */
    @PostMapping("/end/{id}")
    @Operation(summary = "结束任务计时", description = "结束指定的时间追踪计时")
    public Result<Void> endTimeTracking(@Parameter(description = "记录ID") @PathVariable Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("结束任务计时, id={}, currentUserId={}", id, currentUserId);

        // 检查权限
        TaskTimeTrackingDTO existing = timeTrackingService.getTimeTrackingById(id);
        if (existing == null) {
            throw new BusinessException("时间追踪记录不存在");
        }
        if (!currentUserId.equals(existing.getUserId())) {
            throw new BusinessException("没有权限操作此记录");
        }

        boolean success = timeTrackingService.endTimeTracking(id);
        if (!success) {
            throw new BusinessException("结束计时失败");
        }

        log.info("结束任务计时成功, id={}", id);
        return Result.success();
    }

    /**
     * 暂停任务计时
     *
     * @param id 记录ID
     * @return 操作结果
     */
    @PostMapping("/pause/{id}")
    @Operation(summary = "暂停任务计时", description = "暂停指定的时间追踪计时")
    public Result<Void> pauseTimeTracking(@Parameter(description = "记录ID") @PathVariable Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("暂停任务计时, id={}, currentUserId={}", id, currentUserId);

        // 检查权限
        TaskTimeTrackingDTO existing = timeTrackingService.getTimeTrackingById(id);
        if (existing == null) {
            throw new BusinessException("时间追踪记录不存在");
        }
        if (!currentUserId.equals(existing.getUserId())) {
            throw new BusinessException("没有权限操作此记录");
        }

        boolean success = timeTrackingService.pauseTimeTracking(id);
        if (!success) {
            throw new BusinessException("暂停计时失败");
        }

        log.info("暂停任务计时成功, id={}", id);
        return Result.success();
    }

    /**
     * 恢复任务计时
     *
     * @param id 记录ID
     * @return 操作结果
     */
    @PostMapping("/resume/{id}")
    @Operation(summary = "恢复任务计时", description = "恢复指定的时间追踪计时")
    public Result<Void> resumeTimeTracking(@Parameter(description = "记录ID") @PathVariable Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("恢复任务计时, id={}, currentUserId={}", id, currentUserId);

        // 检查权限
        TaskTimeTrackingDTO existing = timeTrackingService.getTimeTrackingById(id);
        if (existing == null) {
            throw new BusinessException("时间追踪记录不存在");
        }
        if (!currentUserId.equals(existing.getUserId())) {
            throw new BusinessException("没有权限操作此记录");
        }

        boolean success = timeTrackingService.resumeTimeTracking(id);
        if (!success) {
            throw new BusinessException("恢复计时失败");
        }

        log.info("恢复任务计时成功, id={}", id);
        return Result.success();
    }

    /**
     * 获取任务总工时
     *
     * @param taskId 任务ID
     * @return 总工时
     */
    @GetMapping("/total-hours/task/{taskId}")
    @Operation(summary = "获取任务总工时", description = "获取指定任务的总工时")
    public Result<Double> getTotalHoursByTask(@Parameter(description = "任务ID") @PathVariable Long taskId) {
        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("获取任务总工时, taskId={}, currentUserId={}", taskId, currentUserId);

        Double totalHours = timeTrackingService.getTotalHoursByTask(taskId);

        log.info("获取任务总工时成功, taskId={}, totalHours={}", taskId, totalHours);
        return Result.success(totalHours);
    }

    /**
     * 获取用户在指定时间范围内的总工时
     *
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 总工时
     */
    @GetMapping("/total-hours/user/{userId}")
    @Operation(summary = "获取用户总工时", description = "获取指定用户在时间范围内的总工时")
    public Result<Double> getTotalHoursByUser(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "开始日期") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("获取用户总工时, userId={}, currentUserId={}, startDate={}, endDate={}", 
                userId, currentUserId, startDate, endDate);

        // 普通用户只能查询自己的记录
        if (!currentUserId.equals(userId)) {
            throw new BusinessException("没有权限查询其他用户的记录");
        }

        Double totalHours = timeTrackingService.getTotalHoursByUser(userId, startDate, endDate);

        log.info("获取用户总工时成功, userId={}, totalHours={}", userId, totalHours);
        return Result.success(totalHours);
    }

    /**
     * 获取项目总工时
     *
     * @param projectId 项目ID
     * @return 总工时
     */
    @GetMapping("/total-hours/project/{projectId}")
    @Operation(summary = "获取项目总工时", description = "获取指定项目的总工时")
    public Result<Double> getTotalHoursByProject(@Parameter(description = "项目ID") @PathVariable Long projectId) {
        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("获取项目总工时, projectId={}, currentUserId={}", projectId, currentUserId);

        Double totalHours = timeTrackingService.getTotalHoursByProject(projectId);

        log.info("获取项目总工时成功, projectId={}, totalHours={}", projectId, totalHours);
        return Result.success(totalHours);
    }
}
