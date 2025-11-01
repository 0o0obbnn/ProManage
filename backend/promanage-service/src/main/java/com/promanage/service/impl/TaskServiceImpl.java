package com.promanage.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.promanage.common.exception.BusinessException;
import com.promanage.common.result.PageResult;
import com.promanage.common.domain.ResultCode;
import com.promanage.service.entity.Task;
import com.promanage.service.entity.TaskActivity;
import com.promanage.service.entity.TaskAttachment;
import com.promanage.service.entity.TaskCheckItem;
import com.promanage.service.entity.TaskComment;
import com.promanage.service.mapper.TaskMapper;
import com.promanage.service.service.ITaskService;
import com.promanage.service.strategy.TaskAttachmentStrategy;
import com.promanage.service.strategy.TaskCheckItemStrategy;
import com.promanage.service.strategy.TaskCommentStrategy;
import com.promanage.service.strategy.TaskQueryStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 任务服务实现类 - 重构版本
 * 使用策略模式减少方法数量
 *
 * @author ProManage Team
 * @date 2025-10-22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements ITaskService {

    private final TaskMapper taskMapper;
    
    // 策略类
    private final TaskQueryStrategy queryStrategy;
    private final TaskCommentStrategy commentStrategy;
    private final TaskAttachmentStrategy attachmentStrategy;
    private final TaskCheckItemStrategy checkItemStrategy;

    // ==================== 核心CRUD方法 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTask(Task task) {
        validateTask(task);
        
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        
        taskMapper.insert(task);
        return task.getId();
    }

    @Override
    public Task getTaskById(Long taskId) {
        return queryStrategy.getTaskById(taskId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTask(Task task) {
        validateTask(task);
        
        Task existingTask = queryStrategy.getTaskById(task.getId());
        if (existingTask == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "任务不存在");
        }
        
        task.setUpdateTime(LocalDateTime.now());
        taskMapper.updateById(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTask(Long taskId, Long userId) {
        Task task = queryStrategy.getTaskById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "任务不存在");
        }
        
        // 检查权限
        if (!queryStrategy.hasTaskPermission(taskId, userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "您无权删除此任务");
        }
        
        // 删除相关数据
        commentStrategy.deleteTaskComment(taskId, userId);
        attachmentStrategy.deleteTaskAttachment(taskId, userId);
        checkItemStrategy.deleteTaskCheckItem(taskId, userId);
        
        taskMapper.deleteById(taskId);
    }

    // ==================== 查询方法 ====================

    @Override
    public PageResult<Task> listTasks(
        Long projectId,
        Integer page,
        Integer size,
        Integer status,
        Integer priority,
        Long assigneeId,
        Long reporterId) {
        // Convert Integer priority to String if needed
        String priorityStr = priority != null ? String.valueOf(priority) : null;
        // Call internal implementation with null keyword and userId
        return listTasksInternal(
            projectId,
            page != null ? page : 1,
            size != null ? size : 20,
            null, // keyword
            status,
            assigneeId,
            reporterId,
            priorityStr,
            null); // userId
    }

    // Internal helper method with additional parameters
    private PageResult<Task> listTasksInternal(
        Long projectId, Integer page, Integer pageSize,
        String keyword, Integer status, Long assigneeId,
        Long reporterId, String priority, Long userId) {
        return queryStrategy.listTasks(projectId, page, pageSize,
            keyword, status, assigneeId, reporterId, priority, userId);
    }

    // 4-parameter overload required by interface
    @Override
    public PageResult<Task> listTasksByAssignee(Long userId, Integer page, Integer size, Integer status) {
        return listTasksByAssigneeInternal(userId, page, size, status, null);
    }

    // Internal helper method with additional parameters
    private PageResult<Task> listTasksByAssigneeInternal(
        Long assigneeId, Integer page, Integer pageSize,
        Integer status, String priority) {
        return queryStrategy.listTasksByAssignee(assigneeId,
            page != null ? page : 1,
            pageSize != null ? pageSize : 20,
            status, priority, assigneeId);
    }

    // Internal helper method with additional parameters
    private PageResult<Task> listTasksByReporterInternal(
        Long reporterId, Integer page, Integer pageSize,
        Integer status, String priority) {
        return queryStrategy.listTasksByReporter(reporterId,
            page != null ? page : 1,
            pageSize != null ? pageSize : 20,
            status, priority, reporterId);
    }

    @Override
    public PageResult<Task> listTasksByReporter(Long userId, Integer page, Integer size, Integer status) {
        return listTasksByReporterInternal(userId, page, size, status, null);
    }

    @Override
    public List<Task> listSubtasks(Long parentTaskId) {
        return queryStrategy.listSubtasks(parentTaskId);
    }

    @Override
    public List<Task> listTaskDependencies(Long taskId) {
        return queryStrategy.listTaskDependencies(taskId);
    }

    @Override
    public int countTasksByProject(Long projectId) {
        return queryStrategy.countTasksByProject(projectId);
    }

    @Override
    public int countTasksByAssignee(Long userId, Integer status) {
        return queryStrategy.countTasksByAssignee(userId, status);
    }

    // ==================== 状态和进度管理 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTaskStatus(Long taskId, Integer status, Long userId) {
        updateTaskStatusInternal(taskId, status, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignTask(Long taskId, Long assigneeId, Long userId) {
        updateTaskField(taskId, "assigneeId", assigneeId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTaskProgress(Long taskId, Integer progress, Long userId) {
        updateTaskField(taskId, "progress", progress, userId);
    }

    // ==================== 依赖管理 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addTaskDependency(Long taskId, Long dependencyTaskId) {
        // 这里应该通过任务依赖表添加依赖关系
        // 简化实现
        log.info("添加任务依赖, taskId={}, dependencyTaskId={}", taskId, dependencyTaskId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeTaskDependency(Long taskId, Long dependencyTaskId) {
        // 这里应该通过任务依赖表删除依赖关系
        // 简化实现
        log.info("删除任务依赖, taskId={}, dependencyTaskId={}", taskId, dependencyTaskId);
    }

    // ==================== 评论管理 ====================

    @Override
    public PageResult<TaskComment> listTaskComments(Long taskId, Integer page, Integer size) {
        return commentStrategy.listTaskComments(taskId, page, size);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addTaskComment(TaskComment comment) {
        return commentStrategy.addTaskComment(comment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTaskComment(Long commentId, Long userId) {
        commentStrategy.deleteTaskComment(commentId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTaskComment(TaskComment comment) {
        commentStrategy.updateTaskComment(comment);
    }

    @Override
    public TaskComment getTaskCommentById(Long commentId) {
        return commentStrategy.getTaskCommentById(commentId);
    }

    // ==================== 权限管理 ====================

    @Override
    public boolean hasTaskPermission(Long taskId, Long userId) {
        return queryStrategy.hasTaskPermission(taskId, userId);
    }

    @Override
    public boolean hasTaskViewPermission(Long taskId, Long userId) {
        return queryStrategy.hasTaskViewPermission(taskId, userId);
    }

    // ==================== 批量操作 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateTasks(
        List<Long> taskIds,
        Integer status,
        Integer priority,
        Long assigneeId,
        String tags,
        Long userId) {

      if (taskIds == null || taskIds.isEmpty()) {
        log.warn("批量更新任务: 任务ID列表为空");
        return 0;
      }

      int updatedCount = 0;
      for (Long taskId : taskIds) {
        try {
          Task task = queryStrategy.getTaskById(taskId);
          if (task != null) {
            boolean updated = false;

            // 更新状态（如果提供）
            if (status != null) {
              task.setStatus(status);
              updated = true;
            }

            // 更新优先级（如果提供）
            if (priority != null) {
              task.setPriority(priority);
              updated = true;
            }

            // 更新指派人（如果提供）
            if (assigneeId != null) {
              task.setAssigneeId(assigneeId);
              updated = true;
            }

            // 更新标签（如果提供）
            if (tags != null && !tags.isEmpty()) {
              task.setTags(tags);
              updated = true;
            }

            // 如果有更新，保存任务
            if (updated) {
              task.setUpdateTime(LocalDateTime.now());
              task.setUpdatedBy(userId);
              taskMapper.updateById(task);
              updatedCount++;

              log.debug("批量更新任务成功, taskId={}", taskId);
            }
          } else {
            log.warn("批量更新任务: 任务不存在, taskId={}", taskId);
          }
        } catch (DataAccessException e) {
          log.error("批量更新任务失败, taskId={}", taskId, e);
        }
      }

      log.info("批量更新任务完成 - 总数: {}, 成功: {}, userId: {}",
          taskIds.size(), updatedCount, userId);

      return updatedCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteTasks(List<Long> taskIds, Long userId) {
        int deletedCount = 0;
        for (Long taskId : taskIds) {
            try {
                deleteTask(taskId, userId);
                deletedCount++;
            } catch (DataAccessException e) {
                log.error("批量删除任务失败, taskId={}", taskId, e);
            }
        }
        return deletedCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchAssignTasks(List<Long> taskIds, Long assigneeId, Long userId) {
        int assignedCount = 0;
        for (Long taskId : taskIds) {
            try {
                assignTask(taskId, assigneeId, userId);
                assignedCount++;
            } catch (DataAccessException e) {
                log.error("批量分配任务失败, taskId={}", taskId, e);
            }
        }
        return assignedCount;
    }

    // ==================== 活动管理 ====================

    @Override
    public PageResult<TaskActivity> listTaskActivities(Long taskId, Integer page, Integer size) {
        // 这里应该实现任务活动查询
        // 简化实现，返回空结果
        return PageResult.<TaskActivity>builder()
            .list(java.util.Collections.emptyList())
            .total(0L)
            .page(page)
            .pageSize(size)
            .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addTaskActivity(TaskActivity activity) {
        // 这里应该实现任务活动添加
        // 简化实现，返回0
        return 0L;
    }

    // ==================== 附件管理 ====================

    @Override
    public List<TaskAttachment> listTaskAttachments(Long taskId) {
        return attachmentStrategy.listTaskAttachments(taskId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addTaskAttachment(TaskAttachment attachment) {
        return attachmentStrategy.addTaskAttachment(attachment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTaskAttachment(Long attachmentId, Long userId) {
        attachmentStrategy.deleteTaskAttachment(attachmentId, userId);
    }

    // ==================== 检查项管理 ====================

    @Override
    public List<TaskCheckItem> listTaskCheckItems(Long taskId) {
        return checkItemStrategy.listTaskCheckItems(taskId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addTaskCheckItem(TaskCheckItem checkItem) {
        return checkItemStrategy.addTaskCheckItem(checkItem);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTaskCheckItem(TaskCheckItem checkItem) {
        checkItemStrategy.updateTaskCheckItem(checkItem);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTaskCheckItem(Long checkItemId, Long userId) {
        checkItemStrategy.deleteTaskCheckItem(checkItemId, userId);
    }

    // ==================== 私有辅助方法 ====================

    private void validateTask(Task task) {
        if (task == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "任务信息不能为空");
        }
        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "任务标题不能为空");
        }
        if (task.getProjectId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "项目ID不能为空");
        }
    }

    private void updateTaskStatusInternal(Long taskId, Integer status, Long userId) {
        Task task = queryStrategy.getTaskById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "任务不存在");
        }
        
        task.setStatus(status);
        task.setUpdateTime(LocalDateTime.now());
        task.setUpdaterId(userId);
        
        try {
            taskMapper.updateById(task);
        } catch (DataAccessException e) {
            log.error("更新任务状态失败, taskId={}, status={}", taskId, status, e);
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "更新任务状态失败");
        }
    }

    private void updateTaskField(Task task, String field, Object value) {
        switch (field) {
            case "status":
                task.setStatus((Integer) value);
                break;
            case "priority":
                task.setPriority((Integer) value);
                break;
            case "assigneeId":
                task.setAssigneeId((Long) value);
                break;
            case "progress":
                task.setProgressPercentage((Integer) value);
                break;
            default:
                log.warn("未知的字段名: {}", field);
        }
    }

    private void updateTaskField(Long taskId, String field, Object value, Long userId) {
        Task task = queryStrategy.getTaskById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "任务不存在");
        }
        
        updateTaskField(task, field, value);
        task.setUpdateTime(LocalDateTime.now());
        task.setUpdaterId(userId);
        
        try {
            taskMapper.updateById(task);
        } catch (DataAccessException e) {
            log.error("更新任务字段失败, taskId={}, field={}", taskId, field, e);
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "更新任务失败");
        }
    }
}
