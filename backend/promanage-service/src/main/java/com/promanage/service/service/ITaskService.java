package com.promanage.service.service;

import com.promanage.common.domain.PageResult;
import com.promanage.service.entity.Task;
import com.promanage.service.entity.TaskComment;

import java.util.List;

/**
 * 任务服务接口
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-03
 */
public interface ITaskService {

    /**
     * 创建任务
     *
     * @param task 任务实体
     * @return 任务ID
     */
    Long createTask(Task task);

    /**
     * 根据ID获取任务
     *
     * @param taskId 任务ID
     * @return 任务实体
     */
    Task getTaskById(Long taskId);

    /**
     * 更新任务
     *
     * @param task 任务实体
     */
    void updateTask(Task task);

    /**
     * 删除任务（软删除）
     *
     * @param taskId 任务ID
     * @param userId 操作人ID
     */
    void deleteTask(Long taskId, Long userId);

    /**
     * 获取项目任务列表
     *
     * @param projectId 项目ID
     * @param page 页码
     * @param size 每页大小
     * @param status 任务状态（可选）
     * @param priority 任务优先级（可选）
     * @param assigneeId 指派人ID（可选）
     * @param reporterId 报告人ID（可选）
     * @return 分页结果
     */
    PageResult<Task> listTasks(Long projectId, Integer page, Integer size,
                              Integer status, Integer priority, Long assigneeId, Long reporterId);

    /**
     * 获取用户负责的任务列表
     *
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @param status 任务状态（可选）
     * @return 分页结果
     */
    PageResult<Task> listTasksByAssignee(Long userId, Integer page, Integer size, Integer status);

    /**
     * 获取用户创建的任务列表
     *
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @param status 任务状态（可选）
     * @return 分页结果
     */
    PageResult<Task> listTasksByReporter(Long userId, Integer page, Integer size, Integer status);

    /**
     * 更新任务状态
     *
     * @param taskId 任务ID
     * @param status 新状态
     * @param userId 操作人ID
     */
    void updateTaskStatus(Long taskId, Integer status, Long userId);

    /**
     * 分配任务
     *
     * @param taskId 任务ID
     * @param assigneeId 指派人ID
     * @param userId 操作人ID
     */
    void assignTask(Long taskId, Long assigneeId, Long userId);

    /**
     * 更新任务进度
     *
     * @param taskId 任务ID
     * @param progress 进度百分比（0-100）
     * @param userId 操作人ID
     */
    void updateTaskProgress(Long taskId, Integer progress, Long userId);

    /**
     * 获取任务的子任务列表
     *
     * @param parentTaskId 父任务ID
     * @return 子任务列表
     */
    List<Task> listSubtasks(Long parentTaskId);

    /**
     * 获取任务的依赖任务列表
     *
     * @param taskId 任务ID
     * @return 依赖任务列表
     */
    List<Task> listTaskDependencies(Long taskId);

    /**
     * 添加任务依赖
     *
     * @param taskId 任务ID
     * @param dependencyTaskId 依赖任务ID
     */
    void addTaskDependency(Long taskId, Long dependencyTaskId);

    /**
     * 移除任务依赖
     *
     * @param taskId 任务ID
     * @param dependencyTaskId 依赖任务ID
     */
    void removeTaskDependency(Long taskId, Long dependencyTaskId);

    /**
     * 统计项目任务数量
     *
     * @param projectId 项目ID
     * @return 任务数量
     */
    int countTasksByProject(Long projectId);

    /**
     * 统计用户负责的任务数量
     *
     * @param userId 用户ID
     * @param status 任务状态（可选）
     * @return 任务数量
     */
    int countTasksByAssignee(Long userId, Integer status);

    /**
     * 获取任务评论列表
     *
     * @param taskId 任务ID
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    PageResult<TaskComment> listTaskComments(Long taskId, Integer page, Integer size);

    /**
     * 添加任务评论
     *
     * @param comment 评论实体
     * @return 评论ID
     */
    Long addTaskComment(TaskComment comment);

    /**
     * 删除任务评论
     *
     * @param commentId 评论ID
     * @param userId 操作人ID
     */
    void deleteTaskComment(Long commentId, Long userId);

    /**
     * 更新任务评论
     *
     * @param comment 评论实体
     */
    void updateTaskComment(TaskComment comment);

    /**
     * 根据ID获取任务评论
     *
     * @param commentId 评论ID
     * @return 评论实体
     */
    TaskComment getTaskCommentById(Long commentId);

    /**
     * 检查用户是否有权限操作任务
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return 是否有权限
     */
    boolean hasTaskPermission(Long taskId, Long userId);

    /**
     * 检查用户是否有权限查看任务
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return 是否有权限
     */
    boolean hasTaskViewPermission(Long taskId, Long userId);
}