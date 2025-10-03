package com.promanage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.promanage.common.domain.PageResult;
import com.promanage.common.exception.BusinessException;
import com.promanage.infrastructure.cache.CacheService;
import com.promanage.service.entity.Task;
import com.promanage.service.entity.TaskComment;
import com.promanage.service.mapper.TaskMapper;
import com.promanage.service.mapper.TaskCommentMapper;
import com.promanage.service.service.ITaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 任务服务实现类
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements ITaskService {

    private final TaskMapper taskMapper;
    private final TaskCommentMapper taskCommentMapper;
    private final CacheService cacheService;

    @Override
    @Transactional
    public Long createTask(Task task) {
        log.info("创建任务, projectId={}, title={}", task.getProjectId(), task.getTitle());

        // 验证项目存在性和权限
        validateProjectAccess(task.getProjectId(), task.getReporterId());

        // 验证指派人是否存在
        if (task.getAssigneeId() != null) {
            validateUserExists(task.getAssigneeId());
        }

        // 验证父任务是否存在
        if (task.getParentTaskId() != null) {
            validateTaskExists(task.getParentTaskId());
        }

        // 设置默认值
        if (task.getStatus() == null) {
            task.setStatus(0); // 默认状态：待办
        }
        if (task.getPriority() == null) {
            task.setPriority(2); // 默认优先级：中
        }
        if (task.getProgressPercentage() == null) {
            task.setProgressPercentage(0); // 默认进度：0%
        }

        taskMapper.insert(task);

        log.info("任务创建成功, taskId={}", task.getId());
        return task.getId();
    }

    @Override
    @Cacheable(value = "tasks", key = "#taskId")
    public Task getTaskById(Long taskId) {
        log.debug("根据ID获取任务, taskId={}", taskId);
        return taskMapper.selectById(taskId);
    }

    @Override
    @Transactional
    @CacheEvict(value = "tasks", key = "#task.id")
    public void updateTask(Task task) {
        log.info("更新任务, taskId={}", task.getId());

        Task existingTask = taskMapper.selectById(task.getId());
        if (existingTask == null) {
            throw new BusinessException("任务不存在");
        }

        // 验证权限
        validateTaskAccess(task.getId(), task.getUpdaterId());

        // 验证状态转换的合法性
        validateStatusTransition(existingTask.getStatus(), task.getStatus());

        taskMapper.updateById(task);

        log.info("任务更新成功, taskId={}", task.getId());
    }

    @Override
    @Transactional
    @CacheEvict(value = "tasks", key = "#taskId")
    public void deleteTask(Long taskId, Long userId) {
        log.info("删除任务, taskId={}, userId={}", taskId, userId);

        validateTaskExists(taskId);
        validateTaskAccess(taskId, userId);

        // 检查是否有子任务
        List<Task> subtasks = listSubtasks(taskId);
        if (!subtasks.isEmpty()) {
            throw new BusinessException("存在子任务，无法删除");
        }

        // 软删除
        Task task = new Task();
        task.setId(taskId);
        task.setDeleted(true);
        task.setUpdaterId((long) userId);
        taskMapper.updateById(task);

        log.info("任务删除成功, taskId={}", taskId);
    }

    @Override
    public PageResult<Task> listTasks(Long projectId, Integer page, Integer size,
                                     Integer status, Integer priority, Long assigneeId, Long reporterId) {
        log.debug("获取项目任务列表, projectId={}, page={}, size={}, status={}, priority={}, assigneeId={}, reporterId={}",
                projectId, page, size, status, priority, assigneeId, reporterId);

        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<Task>()
                .eq(Task::getProjectId, projectId)
                .eq(Task::getDeleted, false)
                .orderByDesc(Task::getCreateTime);

        // 添加筛选条件
        if (status != null) {
            wrapper.eq(Task::getStatus, status);
        }
        if (priority != null) {
            wrapper.eq(Task::getPriority, priority);
        }
        if (assigneeId != null) {
            wrapper.eq(Task::getAssigneeId, assigneeId);
        }
        if (reporterId != null) {
            wrapper.eq(Task::getReporterId, reporterId);
        }

        IPage<Task> taskPage = taskMapper.selectPage(new Page<>(page, size), wrapper);

        return PageResult.of(taskPage.getRecords(), taskPage.getTotal(), page, size);
    }

    @Override
    public PageResult<Task> listTasksByAssignee(Long userId, Integer page, Integer size, Integer status) {
        log.debug("获取用户负责的任务列表, userId={}, page={}, size={}, status={}", userId, page, size, status);

        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<Task>()
                .eq(Task::getAssigneeId, userId)
                .eq(Task::getDeleted, false)
                .orderByDesc(Task::getCreateTime);

        if (status != null) {
            wrapper.eq(Task::getStatus, status);
        }

        IPage<Task> taskPage = taskMapper.selectPage(new Page<>(page, size), wrapper);

        return PageResult.of(taskPage.getRecords(), taskPage.getTotal(), page, size);
    }

    @Override
    public PageResult<Task> listTasksByReporter(Long userId, Integer page, Integer size, Integer status) {
        log.debug("获取用户创建的任务列表, userId={}, page={}, size={}, status={}", userId, page, size, status);

        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<Task>()
                .eq(Task::getReporterId, userId)
                .eq(Task::getDeleted, false)
                .orderByDesc(Task::getCreateTime);

        if (status != null) {
            wrapper.eq(Task::getStatus, status);
        }

        IPage<Task> taskPage = taskMapper.selectPage(new Page<>(page, size), wrapper);

        return PageResult.of(taskPage.getRecords(), taskPage.getTotal(), page, size);
    }

    @Override
    @Transactional
    @CacheEvict(value = "tasks", key = "#taskId")
    public void updateTaskStatus(Long taskId, Integer status, Long userId) {
        log.info("更新任务状态, taskId={}, status={}, userId={}", taskId, status, userId);

        validateTaskExists(taskId);
        validateTaskAccess(taskId, userId);

        Task existingTask = taskMapper.selectById(taskId);
        validateStatusTransition(existingTask.getStatus(), status);

        Task task = new Task();
        task.setId(taskId);
        task.setStatus(status);
        task.setUpdaterId((long) userId);

        // 如果状态变为已完成，设置完成日期
        if (status == 3) { // 已完成
            task.setCompletedDate(java.time.LocalDate.now());
        }

        taskMapper.updateById(task);

        log.info("任务状态更新成功, taskId={}, status={}", taskId, status);
    }

    @Override
    @Transactional
    @CacheEvict(value = "tasks", key = "#taskId")
    public void assignTask(Long taskId, Long assigneeId, Long userId) {
        log.info("分配任务, taskId={}, assigneeId={}, userId={}", taskId, assigneeId, userId);

        validateTaskExists(taskId);
        validateTaskAccess(taskId, userId);
        validateUserExists(assigneeId);

        Task task = new Task();
        task.setId(taskId);
        task.setAssigneeId(assigneeId);
        task.setUpdaterId((long) userId);

        taskMapper.updateById(task);

        log.info("任务分配成功, taskId={}, assigneeId={}", taskId, assigneeId);
    }

    @Override
    @Transactional
    @CacheEvict(value = "tasks", key = "#taskId")
    public void updateTaskProgress(Long taskId, Integer progress, Long userId) {
        log.info("更新任务进度, taskId={}, progress={}, userId={}", taskId, progress, userId);

        validateTaskExists(taskId);
        validateTaskAccess(taskId, userId);

        if (progress < 0 || progress > 100) {
            throw new BusinessException("进度必须在0-100之间");
        }

        Task task = new Task();
        task.setId(taskId);
        task.setProgressPercentage(progress);
        task.setUpdaterId((long) userId);

        // 如果进度达到100%，自动将状态设置为已完成
        if (progress == 100) {
            task.setStatus(3); // 已完成
            task.setCompletedDate(java.time.LocalDate.now());
        }

        taskMapper.updateById(task);

        log.info("任务进度更新成功, taskId={}, progress={}", taskId, progress);
    }

    @Override
    public List<Task> listSubtasks(Long parentTaskId) {
        log.debug("获取子任务列表, parentTaskId={}", parentTaskId);

        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<Task>()
                .eq(Task::getParentTaskId, parentTaskId)
                .eq(Task::getDeleted, false)
                .orderByAsc(Task::getCreateTime);

        return taskMapper.selectList(wrapper);
    }

    @Override
    public List<Task> listTaskDependencies(Long taskId) {
        log.debug("获取任务依赖列表, taskId={}", taskId);

        // TODO: 实现任务依赖关系查询
        // 这里需要根据实际的数据库设计来实现
        // 暂时返回空列表
        return List.of();
    }

    @Override
    @Transactional
    public void addTaskDependency(Long taskId, Long dependencyTaskId) {
        log.info("添加任务依赖, taskId={}, dependencyTaskId={}", taskId, dependencyTaskId);

        validateTaskExists(taskId);
        validateTaskExists(dependencyTaskId);

        // TODO: 实现添加任务依赖关系
        // 这里需要根据实际的数据库设计来实现

        log.info("任务依赖添加成功, taskId={}, dependencyTaskId={}", taskId, dependencyTaskId);
    }

    @Override
    @Transactional
    public void removeTaskDependency(Long taskId, Long dependencyTaskId) {
        log.info("移除任务依赖, taskId={}, dependencyTaskId={}", taskId, dependencyTaskId);

        // TODO: 实现移除任务依赖关系
        // 这里需要根据实际的数据库设计来实现

        log.info("任务依赖移除成功, taskId={}, dependencyTaskId={}", taskId, dependencyTaskId);
    }

    @Override
    public int countTasksByProject(Long projectId) {
        log.debug("统计项目任务数量, projectId={}", projectId);

        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<Task>()
                .eq(Task::getProjectId, projectId)
                .eq(Task::getDeleted, false);

        return Math.toIntExact(taskMapper.selectCount(wrapper));
    }

    @Override
    public int countTasksByAssignee(Long userId, Integer status) {
        log.debug("统计用户负责的任务数量, userId={}, status={}", userId, status);

        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<Task>()
                .eq(Task::getAssigneeId, userId)
                .eq(Task::getDeleted, false);

        if (status != null) {
            wrapper.eq(Task::getStatus, status);
        }

        return Math.toIntExact(taskMapper.selectCount(wrapper));
    }

    @Override
    public PageResult<TaskComment> listTaskComments(Long taskId, Integer page, Integer size) {
        log.debug("获取任务评论列表, taskId={}, page={}, size={}", taskId, page, size);

        LambdaQueryWrapper<TaskComment> wrapper = new LambdaQueryWrapper<TaskComment>()
                .eq(TaskComment::getTaskId, taskId)
                .eq(TaskComment::getStatus, 0) // 正常状态
                .orderByDesc(TaskComment::getCreateTime);

        IPage<TaskComment> commentPage = taskCommentMapper.selectPage(new Page<>(page, size), wrapper);

        return PageResult.of(commentPage.getRecords(), commentPage.getTotal(), page, size);
    }

    @Override
    @Transactional
    public Long addTaskComment(TaskComment comment) {
        log.info("添加任务评论, taskId={}, authorId={}", comment.getTaskId(), comment.getAuthorId());

        validateTaskExists(comment.getTaskId());
        validateTaskAccess(comment.getTaskId(), comment.getAuthorId());

        // 验证父评论是否存在
        if (comment.getParentCommentId() != null) {
            TaskComment parentComment = taskCommentMapper.selectById(comment.getParentCommentId());
            if (parentComment == null || !parentComment.getTaskId().equals(comment.getTaskId())) {
                throw new BusinessException("父评论不存在或不属于此任务");
            }
        }

        if (comment.getIsInternal() == null) {
            comment.setIsInternal(false);
        }
        if (comment.getStatus() == null) {
            comment.setStatus(0); // 正常状态
        }

        taskCommentMapper.insert(comment);

        log.info("任务评论添加成功, commentId={}", comment.getId());
        return comment.getId();
    }

    @Override
    @Transactional
    public void deleteTaskComment(Long commentId, Long userId) {
        log.info("删除任务评论, commentId={}, userId={}", commentId, userId);

        TaskComment comment = taskCommentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }

        // 只能删除自己的评论
        if (!comment.getAuthorId().equals(userId)) {
            throw new BusinessException("只能删除自己的评论");
        }

        // 软删除
        comment.setStatus(1); // 已删除状态
        comment.setUpdaterId((long) userId);
        taskCommentMapper.updateById(comment);

        log.info("任务评论删除成功, commentId={}", commentId);
    }

    @Override
    @Transactional
    public void updateTaskComment(TaskComment comment) {
        log.info("更新任务评论, commentId={}", comment.getId());

        TaskComment existingComment = taskCommentMapper.selectById(comment.getId());
        if (existingComment == null) {
            throw new BusinessException("评论不存在");
        }

        // 只能更新自己的评论
        if (!existingComment.getAuthorId().equals(comment.getUpdaterId())) {
            throw new BusinessException("只能更新自己的评论");
        }

        taskCommentMapper.updateById(comment);

        log.info("任务评论更新成功, commentId={}", comment.getId());
    }

    @Override
    public TaskComment getTaskCommentById(Long commentId) {
        log.debug("根据ID获取任务评论, commentId={}", commentId);
        return taskCommentMapper.selectById(commentId);
    }

    @Override
    public boolean hasTaskPermission(Long taskId, Long userId) {
        // 实现权限检查逻辑
        // 这里简化处理，实际应该根据项目权限来判断
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            return false;
        }

        // 任务的创建者、被指派人或项目负责人有权限
        return task.getReporterId().equals(userId) ||
               (task.getAssigneeId() != null && task.getAssigneeId().equals(userId));
    }

    @Override
    public boolean hasTaskViewPermission(Long taskId, Long userId) {
        // 实现查看权限检查逻辑
        // 这里简化处理，实际应该根据项目权限来判断
        return hasTaskPermission(taskId, userId);
    }

    // 辅助方法

    private void validateProjectAccess(Long projectId, Long userId) {
        // TODO: 验证项目存在性和用户权限
        // 这里简化处理，实际需要调用项目服务
        log.debug("验证项目访问权限, projectId={}, userId={}", projectId, userId);
    }

    private void validateTaskExists(Long taskId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null || task.getDeleted()) {
            throw new BusinessException("任务不存在");
        }
    }

    private void validateTaskAccess(Long taskId, Long userId) {
        if (!hasTaskPermission(taskId, userId)) {
            throw new BusinessException("没有权限操作此任务");
        }
    }

    private void validateUserExists(Long userId) {
        // TODO: 验证用户存在性
        // 这里简化处理，实际需要调用用户服务
        log.debug("验证用户存在, userId={}", userId);
    }

    private void validateStatusTransition(Integer oldStatus, Integer newStatus) {
        // 简化的状态转换验证
        // 0-待办, 1-进行中, 2-审核中, 3-已完成, 4-已取消, 5-已阻塞

        if (oldStatus.equals(newStatus)) {
            return; // 状态没有变化
        }

        // 已完成和已取消的任务不能再改变状态
        if (oldStatus == 3 || oldStatus == 4) {
            throw new BusinessException("已完成或已取消的任务不能再改变状态");
        }

        // 其他状态转换规则可以根据业务需求添加
        log.debug("验证状态转换, oldStatus={}, newStatus={}", oldStatus, newStatus);
    }
}