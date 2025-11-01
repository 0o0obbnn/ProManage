package com.promanage.service.service;

import java.util.List;

import com.promanage.common.result.PageResult;
import com.promanage.service.entity.Task;
import com.promanage.service.entity.TaskActivity;
import com.promanage.service.entity.TaskAttachment;
import com.promanage.service.entity.TaskCheckItem;
import com.promanage.service.entity.TaskComment;

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
  PageResult<Task> listTasks(
      Long projectId,
      Integer page,
      Integer size,
      Integer status,
      Integer priority,
      Long assigneeId,
      Long reporterId);

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
   * 获取任务活动列表
   *
   * @param taskId 任务ID
   * @param page 页码
   * @param size 每页大小
   * @return 分页结果
   */
  PageResult<TaskActivity> listTaskActivities(Long taskId, Integer page, Integer size);

  /**
   * 添加任务活动
   *
   * @param activity 活动实体
   * @return 活动ID
   */
  Long addTaskActivity(TaskActivity activity);

  /**
   * 获取任务附件列表
   *
   * @param taskId 任务ID
   * @return 附件列表
   */
  List<TaskAttachment> listTaskAttachments(Long taskId);

  /**
   * 添加任务附件
   *
   * @param attachment 附件实体
   * @return 附件ID
   */
  Long addTaskAttachment(TaskAttachment attachment);

  /**
   * 删除任务附件
   *
   * @param attachmentId 附件ID
   * @param userId 操作人ID
   */
  void deleteTaskAttachment(Long attachmentId, Long userId);

  /**
   * 获取任务检查项列表
   *
   * @param taskId 任务ID
   * @return 检查项列表
   */
  List<TaskCheckItem> listTaskCheckItems(Long taskId);

  /**
   * 添加任务检查项
   *
   * @param checkItem 检查项实体
   * @return 检查项ID
   */
  Long addTaskCheckItem(TaskCheckItem checkItem);

  /**
   * 更新任务检查项
   *
   * @param checkItem 检查项实体
   */
  void updateTaskCheckItem(TaskCheckItem checkItem);

  /**
   * 删除任务检查项
   *
   * @param checkItemId 检查项ID
   * @param userId 操作人ID
   */
  void deleteTaskCheckItem(Long checkItemId, Long userId);

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

  /**
   * 批量更新任务
   *
   * @param taskIds 任务ID列表
   * @param status 新状态（可选）
   * @param priority 新优先级（可选）
   * @param assigneeId 新指派人ID（可选）
   * @param tags 标签（可选）
   * @param userId 操作人ID
   * @return 成功更新的任务数量
   */
  int batchUpdateTasks(
      List<Long> taskIds,
      Integer status,
      Integer priority,
      Long assigneeId,
      String tags,
      Long userId);

  /**
   * 批量删除任务
   *
   * @param taskIds 任务ID列表
   * @param userId 操作人ID
   * @return 成功删除的任务数量
   */
  int batchDeleteTasks(List<Long> taskIds, Long userId);

  /**
   * 批量分配任务
   *
   * @param taskIds 任务ID列表
   * @param assigneeId 指派人ID
   * @param userId 操作人ID
   * @return 成功分配的任务数量
   */
  int batchAssignTasks(List<Long> taskIds, Long assigneeId, Long userId);
}
