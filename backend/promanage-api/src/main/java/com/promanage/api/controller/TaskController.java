package com.promanage.api.controller;

import com.promanage.api.dto.request.CreateTaskCommentRequest;
import com.promanage.api.dto.request.CreateTaskRequest;
import com.promanage.api.dto.request.UpdateTaskRequest;
import com.promanage.api.dto.request.BatchUpdateTasksRequest;
import com.promanage.api.dto.request.BatchDeleteTasksRequest;
import com.promanage.api.dto.request.BatchAssignTasksRequest;
import com.promanage.api.dto.response.TaskCommentResponse;
import com.promanage.api.dto.response.TaskDetailResponse;
import com.promanage.api.dto.response.TaskResponse;
import com.promanage.common.result.PageResult;
import com.promanage.common.domain.Result;
import com.promanage.common.exception.BusinessException;
import com.promanage.infrastructure.utils.SecurityUtils;
import com.promanage.api.dto.response.TaskActivityResponse;
import com.promanage.api.dto.response.TaskAttachmentResponse;
import com.promanage.api.dto.response.TaskCheckItemResponse;
import com.promanage.service.entity.Task;
import com.promanage.service.entity.TaskActivity;
import com.promanage.service.entity.TaskAttachment;
import com.promanage.service.entity.TaskCheckItem;
import com.promanage.service.entity.TaskComment;
import com.promanage.common.entity.User;
import com.promanage.service.mapper.TaskCommentMapper;
import com.promanage.service.service.ITaskService;
import com.promanage.service.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 任务管理控制器
 * <p>
 * 提供任务的创建、查询、更新、删除以及评论管理功能
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-03
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@Tag(name = "任务管理", description = "任务创建、查询、更新、删除以及评论管理接口")
@RequiredArgsConstructor
public class TaskController {

    private final ITaskService taskService;
    private final IUserService userService;
    private final TaskCommentMapper taskCommentMapper;
    private final com.promanage.service.mapper.TaskAttachmentMapper taskAttachmentMapper;

    /**
     * 获取项目任务列表
     *
     * @param projectId 项目ID
     * @param page 页码
     * @param size 每页大小
     * @param status 任务状态
     * @param priority 任务优先级
     * @param assigneeId 指派人ID
     * @param reporterId 报告人ID
     * @return 任务列表
     */
    @GetMapping("/projects/{projectId}/tasks")
    @Operation(summary = "获取项目任务列表", description = "获取指定项目的任务列表")
    public Result<PageResult<TaskResponse>> getTasks(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer priority,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) Long reporterId) {

        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("获取项目任务列表请求, projectId={}, userId={}, page={}, size={}, status={}, priority={}, assigneeId={}, reporterId={}",
                projectId, userId, page, size, status, priority, assigneeId, reporterId);

        // 检查权限
        if (!taskService.hasTaskViewPermission(projectId, userId)) {
            throw new BusinessException("没有权限查看此项目的任务");
        }

        PageResult<Task> taskPage = taskService.listTasks(projectId, page, size, status, priority, assigneeId, reporterId);

        List<TaskResponse> taskResponses = taskPage.getList().stream()
                .map(this::convertToTaskResponse)
                .collect(Collectors.toList());

        PageResult<TaskResponse> response = PageResult.of(
                taskResponses,
                taskPage.getTotal(),
                taskPage.getPage(),
                taskPage.getPageSize()
        );

        log.info("获取项目任务列表成功, projectId={}, total={}", projectId, response.getTotal());
        return Result.success(response);
    }

    /**
     * 创建任务
     *
     * @param projectId 项目ID
     * @param request 创建任务请求
     * @return 创建的任务信息
     */
    @PostMapping("/projects/{projectId}/tasks")
    @Operation(summary = "创建任务", description = "在项目中创建新任务")
    public Result<TaskDetailResponse> createTask(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateTaskRequest request) {

        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("创建任务请求, projectId={}, userId={}, title={}", projectId, userId, request.getTitle());

        // 检查权限
        if (!taskService.hasTaskPermission(projectId, userId)) {
            throw new BusinessException("没有权限在此项目中创建任务");
        }

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());
        task.setAssigneeId(request.getAssigneeId());
        task.setReporterId(userId); // 创建者为报告人
        task.setParentTaskId(request.getParentTaskId());
        task.setEstimatedHours(request.getEstimatedHours());
        task.setStartDate(request.getStartDate());
        task.setDueDate(request.getDueDate());
        task.setTags(request.getTags());
        task.setProjectId(projectId);
        task.setProgressPercentage(0); // 默认进度0%

        Long taskId = taskService.createTask(task);

        Task createdTask = taskService.getTaskById(taskId);
        TaskDetailResponse response = convertToTaskDetailResponse(createdTask);

        log.info("任务创建成功, taskId={}, title={}", taskId, request.getTitle());
        return Result.success(response);
    }

    /**
     * 获取任务详情
     *
     * @param taskId 任务ID
     * @return 任务详情
     */
    @GetMapping("/tasks/{taskId}")
    @Operation(summary = "获取任务详情", description = "获取任务的详细信息")
    public Result<TaskDetailResponse> getTask(@PathVariable Long taskId) {
        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("获取任务详情请求, taskId={}, userId={}", taskId, userId);

        // 检查权限
        if (!taskService.hasTaskViewPermission(taskId, userId)) {
            throw new BusinessException("没有权限查看此任务");
        }

        Task task = taskService.getTaskById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        TaskDetailResponse response = convertToTaskDetailResponse(task);

        log.info("获取任务详情成功, taskId={}", taskId);
        return Result.success(response);
    }

    /**
     * 更新任务
     *
     * @param taskId 任务ID
     * @param request 更新任务请求
     * @return 更新后的任务信息
     */
    @PutMapping("/tasks/{taskId}")
    @Operation(summary = "更新任务", description = "更新任务信息")
    public Result<TaskDetailResponse> updateTask(
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskRequest request) {

        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("更新任务请求, taskId={}, userId={}", taskId, userId);

        // 检查权限
        if (!taskService.hasTaskPermission(taskId, userId)) {
            throw new BusinessException("没有权限编辑此任务");
        }

        Task task = taskService.getTaskById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        // 更新任务信息
        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getAssigneeId() != null) {
            task.setAssigneeId(request.getAssigneeId());
        }
        if (request.getEstimatedHours() != null) {
            task.setEstimatedHours(request.getEstimatedHours());
        }
        if (request.getActualHours() != null) {
            task.setActualHours(request.getActualHours());
        }
        if (request.getProgressPercentage() != null) {
            task.setProgressPercentage(request.getProgressPercentage());
        }
        if (request.getStartDate() != null) {
            task.setStartDate(request.getStartDate());
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }
        if (request.getCompletedDate() != null) {
            task.setCompletedDate(request.getCompletedDate());
        }
        if (request.getTags() != null) {
            task.setTags(request.getTags());
        }

        taskService.updateTask(task);

        Task updatedTask = taskService.getTaskById(taskId);
        TaskDetailResponse response = convertToTaskDetailResponse(updatedTask);

        log.info("任务更新成功, taskId={}", taskId);
        return Result.success(response);
    }

    /**
     * 删除任务
     *
     * @param taskId 任务ID
     * @return 操作结果
     */
    @DeleteMapping("/tasks/{taskId}")
    @Operation(summary = "删除任务", description = "软删除任务")
    public Result<Void> deleteTask(@PathVariable Long taskId) {
        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("删除任务请求, taskId={}, userId={}", taskId, userId);

        // 检查权限
        if (!taskService.hasTaskPermission(taskId, userId)) {
            throw new BusinessException("没有权限删除此任务");
        }

        taskService.deleteTask(taskId, userId);

        log.info("任务删除成功, taskId={}", taskId);
        return Result.success();
    }

    /**
     * 获取任务评论列表
     *
     * @param taskId 任务ID
     * @param page 页码
     * @param size 每页大小
     * @return 评论列表
     */
    @GetMapping("/tasks/{taskId}/comments")
    @Operation(summary = "获取任务评论", description = "获取任务的评论列表")
    public Result<PageResult<TaskCommentResponse>> getTaskComments(
            @PathVariable Long taskId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("获取任务评论列表请求, taskId={}, userId={}, page={}, size={}", taskId, userId, page, size);

        // 检查权限
        if (!taskService.hasTaskViewPermission(taskId, userId)) {
            throw new BusinessException("没有权限查看此任务的评论");
        }

        PageResult<TaskComment> commentPage = taskService.listTaskComments(taskId, page, size);

        List<TaskCommentResponse> commentResponses = commentPage.getList().stream()
                .map(this::convertToTaskCommentResponse)
                .collect(Collectors.toList());

        PageResult<TaskCommentResponse> response = PageResult.of(
                commentResponses,
                commentPage.getTotal(),
                commentPage.getPage(),
                commentPage.getPageSize()
        );

        log.info("获取任务评论列表成功, taskId={}, total={}", taskId, response.getTotal());
        return Result.success(response);
    }

    /**
     * 添加任务评论
     *
     * @param taskId 任务ID
     * @param request 创建评论请求
     * @return 创建的评论信息
     */
    @PostMapping("/tasks/{taskId}/comments")
    @Operation(summary = "添加任务评论", description = "为任务添加评论")
    public Result<TaskCommentResponse> addTaskComment(
            @PathVariable Long taskId,
            @Valid @RequestBody CreateTaskCommentRequest request) {

        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("添加任务评论请求, taskId={}, userId={}", taskId, userId);

        // 检查权限
        if (!taskService.hasTaskPermission(taskId, userId)) {
            throw new BusinessException("没有权限为此任务添加评论");
        }

        TaskComment comment = new TaskComment();
        comment.setTaskId(taskId);
        comment.setContent(request.getContent());
        comment.setAuthorId(userId);
        comment.setParentCommentId(request.getParentCommentId());
        comment.setIsInternal(request.getIsInternal());
        comment.setStatus(0); // 正常状态

        Long commentId = taskService.addTaskComment(comment);

        TaskComment createdComment = taskService.getTaskCommentById(commentId);
        TaskCommentResponse response = convertToTaskCommentResponse(createdComment);

        log.info("添加任务评论成功, taskId={}, commentId={}", taskId, commentId);
        return Result.success(response);
    }

    /**
     * 更新任务状态
     *
     * @param taskId 任务ID
     * @param status 新状态
     * @return 操作结果
     */
    @PutMapping("/tasks/{taskId}/status")
    @Operation(summary = "更新任务状态", description = "更新任务状态")
    public Result<Void> updateTaskStatus(
            @PathVariable Long taskId,
            @RequestParam Integer status) {

        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("更新任务状态请求, taskId={}, userId={}, status={}", taskId, userId, status);

        // 检查权限
        if (!taskService.hasTaskPermission(taskId, userId)) {
            throw new BusinessException("没有权限更新此任务状态");
        }

        taskService.updateTaskStatus(taskId, status, userId);

        log.info("更新任务状态成功, taskId={}, status={}", taskId, status);
        return Result.success();
    }

    /**
     * 分配任务
     *
     * @param taskId 任务ID
     * @param assigneeId 指派人ID
     * @return 操作结果
     */
    @PutMapping("/tasks/{taskId}/assign")
    @Operation(summary = "分配任务", description = "为任务分配执行人")
    public Result<Void> assignTask(
            @PathVariable Long taskId,
            @RequestParam Long assigneeId) {

        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("分配任务请求, taskId={}, userId={}, assigneeId={}", taskId, userId, assigneeId);

        // 检查权限
        if (!taskService.hasTaskPermission(taskId, userId)) {
            throw new BusinessException("没有权限分配此任务");
        }

        taskService.assignTask(taskId, assigneeId, userId);

        log.info("分配任务成功, taskId={}, assigneeId={}", taskId, assigneeId);
        return Result.success();
    }

    /**
     * 更新任务进度
     *
     * @param taskId 任务ID
     * @param progress 进度百分比
     * @return 操作结果
     */
    @PutMapping("/tasks/{taskId}/progress")
    @Operation(summary = "更新任务进度", description = "更新任务完成进度")
    public Result<Void> updateTaskProgress(
            @PathVariable Long taskId,
            @RequestParam Integer progress) {

        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("更新任务进度请求, taskId={}, userId={}, progress={}", taskId, userId, progress);

        // 检查权限
        if (!taskService.hasTaskPermission(taskId, userId)) {
            throw new BusinessException("没有权限更新此任务进度");
        }

        taskService.updateTaskProgress(taskId, progress, userId);

        log.info("更新任务进度成功, taskId={}, progress={}", taskId, progress);
        return Result.success();
    }

    /**
     * 获取任务依赖列表
     *
     * @param taskId 任务ID
     * @return 依赖任务列表
     */
    @GetMapping("/tasks/{taskId}/dependencies")
    @Operation(summary = "获取任务依赖列表", description = "获取任务的所有前置任务（依赖关系）")
    public Result<List<TaskResponse>> getTaskDependencies(@PathVariable Long taskId) {
        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("获取任务依赖列表请求, taskId={}, userId={}", taskId, userId);

        // 检查权限
        if (!taskService.hasTaskViewPermission(taskId, userId)) {
            throw new BusinessException("没有权限查看此任务的依赖关系");
        }

        List<Task> dependencies = taskService.listTaskDependencies(taskId);
        List<TaskResponse> response = dependencies.stream()
                .map(this::convertToTaskResponse)
                .collect(Collectors.toList());

        log.info("获取任务依赖列表成功, taskId={}, 依赖数={}", taskId, response.size());
        return Result.success(response);
    }

    /**
     * 添加任务依赖
     *
     * @param taskId 任务ID
     * @param dependencyTaskId 依赖任务ID（前置任务ID）
     * @return 操作结果
     */
    @PostMapping("/tasks/{taskId}/dependencies")
    @Operation(summary = "添加任务依赖", description = "为任务添加前置任务依赖关系")
    public Result<Void> addTaskDependency(
            @PathVariable Long taskId,
            @RequestParam Long dependencyTaskId) {

        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("添加任务依赖请求, taskId={}, dependencyTaskId={}, userId={}", taskId, dependencyTaskId, userId);

        // 检查权限
        if (!taskService.hasTaskPermission(taskId, userId)) {
            throw new BusinessException("没有权限为此任务添加依赖关系");
        }

        taskService.addTaskDependency(taskId, dependencyTaskId);

        log.info("添加任务依赖成功, taskId={}, dependencyTaskId={}", taskId, dependencyTaskId);
        return Result.success();
    }

    /**
     * 删除任务依赖
     *
     * @param taskId 任务ID
     * @param dependencyTaskId 依赖任务ID（前置任务ID）
     * @return 操作结果
     */
    @DeleteMapping("/tasks/{taskId}/dependencies/{dependencyTaskId}")
    @Operation(summary = "删除任务依赖", description = "删除任务的前置任务依赖关系")
    public Result<Void> removeTaskDependency(
            @PathVariable Long taskId,
            @PathVariable Long dependencyTaskId) {

        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("删除任务依赖请求, taskId={}, dependencyTaskId={}, userId={}", taskId, dependencyTaskId, userId);

        // 检查权限
        if (!taskService.hasTaskPermission(taskId, userId)) {
            throw new BusinessException("没有权限删除此任务的依赖关系");
        }

        taskService.removeTaskDependency(taskId, dependencyTaskId);

        log.info("删除任务依赖成功, taskId={}, dependencyTaskId={}", taskId, dependencyTaskId);
        return Result.success();
    }

    /**
     * 批量更新任务
     *
     * @param request 批量更新请求
     * @return 操作结果
     */
    @PostMapping("/tasks/batch-update")
    @Operation(summary = "批量更新任务", description = "批量更新任务的状态、优先级、指派人或标签")
    public Result<Integer> batchUpdateTasks(@Valid @RequestBody BatchUpdateTasksRequest request) {
        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("批量更新任务请求, taskIds={}, userId={}", request.getTaskIds(), userId);

        int successCount = taskService.batchUpdateTasks(
                request.getTaskIds(),
                request.getStatus(),
                request.getPriority(),
                request.getAssigneeId(),
                request.getTags(),
                userId
        );

        log.info("批量更新任务完成, 总数={}, 成功={}", request.getTaskIds().size(), successCount);
        return Result.success(successCount);
    }

    /**
     * 批量删除任务
     *
     * @param request 批量删除请求
     * @return 操作结果
     */
    @PostMapping("/tasks/batch-delete")
    @Operation(summary = "批量删除任务", description = "批量软删除任务")
    public Result<Integer> batchDeleteTasks(@Valid @RequestBody BatchDeleteTasksRequest request) {
        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("批量删除任务请求, taskIds={}, userId={}", request.getTaskIds(), userId);

        int successCount = taskService.batchDeleteTasks(request.getTaskIds(), userId);

        log.info("批量删除任务完成, 总数={}, 成功={}", request.getTaskIds().size(), successCount);
        return Result.success(successCount);
    }

    /**
     * 批量分配任务
     *
     * @param request 批量分配请求
     * @return 操作结果
     */
    @PostMapping("/tasks/batch-assign")
    @Operation(summary = "批量分配任务", description = "批量为任务分配执行人")
    public Result<Integer> batchAssignTasks(@Valid @RequestBody BatchAssignTasksRequest request) {
        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException("请先登录"));

        log.info("批量分配任务请求, taskIds={}, assigneeId={}, userId={}",
                request.getTaskIds(), request.getAssigneeId(), userId);

        int successCount = taskService.batchAssignTasks(
                request.getTaskIds(),
                request.getAssigneeId(),
                userId
        );

        log.info("批量分配任务完成, 总数={}, 成功={}", request.getTaskIds().size(), successCount);
        return Result.success(successCount);
    }

    // 辅助方法

    private TaskResponse convertToTaskResponse(Task task) {
        User assignee = task.getAssigneeId() != null ? userService.getById(task.getAssigneeId()) : null;
        User reporter = task.getReporterId() != null ? userService.getById(task.getReporterId()) : null;

        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .assigneeId(task.getAssigneeId())
                .assigneeName(assignee != null ? assignee.getRealName() : null)
                .assigneeAvatar(assignee != null ? assignee.getAvatar() : null)
                .reporterId(task.getReporterId())
                .reporterName(reporter != null ? reporter.getRealName() : null)
                .reporterAvatar(reporter != null ? reporter.getAvatar() : null)
                .parentTaskId(task.getParentTaskId())
                .projectId(task.getProjectId())
                .estimatedHours(task.getEstimatedHours())
                .actualHours(task.getActualHours())
                .progressPercentage(task.getProgressPercentage())
                .startDate(task.getStartDate())
                .dueDate(task.getDueDate())
                .completedDate(task.getCompletedDate())
                .tags(task.getTags())
                .commentCount(taskService.listTaskComments(task.getId(), 1, 1).getTotal().intValue())
                .attachmentCount(taskAttachmentMapper.countByTaskId(task.getId()))
                .subtaskCount(taskService.listSubtasks(task.getId()).size())
                .createTime(task.getCreateTime())
                .updateTime(task.getUpdateTime())
                .build();
    }

    private TaskDetailResponse convertToTaskDetailResponse(Task task) {
        TaskResponse basicInfo = convertToTaskResponse(task);

        TaskDetailResponse detailResponse = new TaskDetailResponse();
        // 复制基础信息
        detailResponse.setId(basicInfo.getId());
        detailResponse.setTitle(basicInfo.getTitle());
        detailResponse.setDescription(basicInfo.getDescription());
        detailResponse.setStatus(basicInfo.getStatus());
        detailResponse.setPriority(basicInfo.getPriority());
        detailResponse.setAssigneeId(basicInfo.getAssigneeId());
        detailResponse.setAssigneeName(basicInfo.getAssigneeName());
        detailResponse.setAssigneeAvatar(basicInfo.getAssigneeAvatar());
        detailResponse.setReporterId(basicInfo.getReporterId());
        detailResponse.setReporterName(basicInfo.getReporterName());
        detailResponse.setReporterAvatar(basicInfo.getReporterAvatar());
        detailResponse.setParentTaskId(basicInfo.getParentTaskId());
        detailResponse.setProjectId(basicInfo.getProjectId());
        detailResponse.setEstimatedHours(basicInfo.getEstimatedHours());
        detailResponse.setActualHours(basicInfo.getActualHours());
        detailResponse.setProgressPercentage(basicInfo.getProgressPercentage());
        detailResponse.setStartDate(basicInfo.getStartDate());
        detailResponse.setDueDate(basicInfo.getDueDate());
        detailResponse.setCompletedDate(basicInfo.getCompletedDate());
        detailResponse.setTags(basicInfo.getTags());
        detailResponse.setCommentCount(basicInfo.getCommentCount());
        detailResponse.setAttachmentCount(basicInfo.getAttachmentCount());
        detailResponse.setSubtaskCount(basicInfo.getSubtaskCount());
        detailResponse.setCreateTime(basicInfo.getCreateTime());
        detailResponse.setUpdateTime(basicInfo.getUpdateTime());

        // 设置详情信息
        detailResponse.setSubtasks(taskService.listSubtasks(task.getId()).stream()
                .map(this::convertToTaskResponse)
                .collect(Collectors.toList()));
        detailResponse.setDependencies(taskService.listTaskDependencies(task.getId()).stream()
                .map(this::convertToTaskResponse)
                .collect(Collectors.toList()));

        // 获取最近的评论（最多5条）
        PageResult<TaskComment> recentCommentsPage = taskService.listTaskComments(task.getId(), 1, 5);
        detailResponse.setRecentComments(recentCommentsPage.getList().stream()
                .map(this::convertToTaskCommentResponse)
                .collect(Collectors.toList()));

        // 获取最近活动（最多10条）
        PageResult<TaskActivity> recentActivitiesPage = taskService.listTaskActivities(task.getId(), 1, 10);
        detailResponse.setRecentActivities(recentActivitiesPage.getList().stream()
                .map(activity -> {
                    User activityUser = activity.getUserId() != null ? userService.getById(activity.getUserId()) : null;
                    return TaskActivityResponse.builder()
                            .id(activity.getId())
                            .activityType(activity.getActivityType())
                            .content(activity.getContent())
                            .oldValue(activity.getOldValue())
                            .newValue(activity.getNewValue())
                            .userId(activity.getUserId())
                            .userName(activityUser != null ? activityUser.getRealName() : null)
                            .userAvatar(activityUser != null ? activityUser.getAvatar() : null)
                            .createTime(activity.getCreateTime())
                            .build();
                })
                .collect(Collectors.toList()));

        detailResponse.setFullDescription(task.getDescription()); // 完整描述

        // 获取附件列表
        List<TaskAttachment> attachments = taskService.listTaskAttachments(task.getId());
        detailResponse.setAttachments(attachments.stream()
                .map(attachment -> TaskAttachmentResponse.builder()
                        .id(attachment.getId())
                        .fileName(attachment.getFileName())
                        .filePath(attachment.getFilePath())
                        .fileSize(attachment.getFileSize())
                        .mimeType(attachment.getMimeType())
                        .uploaderId(attachment.getUploaderId())
                        .isImage(attachment.getIsImage())
                        .createTime(attachment.getCreateTime())
                        .build())
                .collect(Collectors.toList()));

        // 获取检查项列表
        List<TaskCheckItem> checkItems = taskService.listTaskCheckItems(task.getId());
        detailResponse.setCheckItems(checkItems.stream()
                .map(checkItem -> TaskCheckItemResponse.builder()
                        .id(checkItem.getId())
                        .content(checkItem.getContent())
                        .isCompleted(checkItem.getIsCompleted())
                        .completedById(checkItem.getCompletedById())
                        .completedTime(checkItem.getCompletedTime())
                        .sortOrder(checkItem.getSortOrder())
                        .createTime(checkItem.getCreateTime())
                        .build())
                .collect(Collectors.toList()));

        return detailResponse;
    }

    private TaskCommentResponse convertToTaskCommentResponse(TaskComment comment) {
        User author = comment.getAuthorId() != null ? userService.getById(comment.getAuthorId()) : null;

        // 计算回复数量 - 查询以当前评论为父评论的评论数量
        int replyCount = taskCommentMapper.countReplies(comment.getId());

        // 获取父评论信息（如果存在）
        TaskCommentResponse parentCommentResponse = null;
        if (comment.getParentCommentId() != null) {
            TaskComment parentComment = taskCommentMapper.selectById(comment.getParentCommentId());
            if (parentComment != null) {
                User parentAuthor = parentComment.getAuthorId() != null ?
                        userService.getById(parentComment.getAuthorId()) : null;
                parentCommentResponse = TaskCommentResponse.builder()
                        .id(parentComment.getId())
                        .content(parentComment.getContent())
                        .authorId(parentComment.getAuthorId())
                        .authorName(parentAuthor != null ? parentAuthor.getRealName() : null)
                        .authorAvatar(parentAuthor != null ? parentAuthor.getAvatar() : null)
                        .createTime(parentComment.getCreateTime())
                        .build();
            }
        }

        return TaskCommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorId(comment.getAuthorId())
                .authorName(author != null ? author.getRealName() : null)
                .authorAvatar(author != null ? author.getAvatar() : null)
                .parentCommentId(comment.getParentCommentId())
                .isInternal(comment.getIsInternal())
                .createTime(comment.getCreateTime())
                .updateTime(comment.getUpdateTime())
                .parentComment(parentCommentResponse)
                .replyCount(replyCount)
                .isEdited(!comment.getCreateTime().equals(comment.getUpdateTime()))
                .build();
    }
}