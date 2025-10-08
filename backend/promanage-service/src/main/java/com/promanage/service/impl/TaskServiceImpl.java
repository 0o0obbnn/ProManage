package com.promanage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.promanage.common.domain.PageResult;
import com.promanage.common.exception.BusinessException;
import com.promanage.service.entity.Project;
import com.promanage.service.entity.Task;
import com.promanage.service.entity.TaskActivity;
import com.promanage.service.entity.TaskAttachment;
import com.promanage.service.entity.TaskCheckItem;
import com.promanage.service.entity.TaskComment;
import com.promanage.service.entity.TaskDependency;
import com.promanage.common.entity.User;
import com.promanage.service.mapper.ProjectMapper;
import com.promanage.service.mapper.TaskMapper;
import com.promanage.service.mapper.TaskCommentMapper;
import com.promanage.service.mapper.TaskDependencyMapper;
import com.promanage.service.mapper.UserMapper;
import com.promanage.service.mapper.TaskActivityMapper;
import com.promanage.service.mapper.TaskAttachmentMapper;
import com.promanage.service.mapper.TaskCheckItemMapper;
import com.promanage.service.service.ITaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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
    private final TaskDependencyMapper taskDependencyMapper;
    private final ProjectMapper projectMapper;
    private final UserMapper userMapper;
    private final TaskActivityMapper taskActivityMapper;
    private final TaskAttachmentMapper taskAttachmentMapper;
    private final TaskCheckItemMapper taskCheckItemMapper;

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

        // 查询该任务依赖的所有前置任务ID
        List<Long> prerequisiteTaskIds = taskDependencyMapper.findPrerequisiteTaskIds(taskId);

        if (prerequisiteTaskIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 批量查询前置任务详情（使用 LambdaQueryWrapper 替代 deprecated 方法）
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<Task>()
                .in(Task::getId, prerequisiteTaskIds)
                .eq(Task::getDeleted, false);
        List<Task> prerequisiteTasks = taskMapper.selectList(wrapper);

        log.debug("任务依赖查询完成, taskId={}, 依赖任务数={}", taskId, prerequisiteTasks.size());
        return prerequisiteTasks;
    }

    @Override
    @Transactional
    public void addTaskDependency(Long taskId, Long dependencyTaskId) {
        log.info("添加任务依赖, taskId={}, dependencyTaskId={}", taskId, dependencyTaskId);

        // 验证两个任务都存在
        validateTaskExists(taskId);
        validateTaskExists(dependencyTaskId);

        // 验证不能依赖自己
        if (taskId.equals(dependencyTaskId)) {
            throw new BusinessException("任务不能依赖自己");
        }

        // 检查是否已存在依赖关系
        if (taskDependencyMapper.existsDependency(dependencyTaskId, taskId)) {
            log.warn("任务依赖关系已存在, taskId={}, dependencyTaskId={}", taskId, dependencyTaskId);
            throw new BusinessException("任务依赖关系已存在");
        }

        // 检查是否会形成循环依赖
        if (wouldCreateCircularDependency(taskId, dependencyTaskId)) {
            throw new BusinessException("添加此依赖会形成循环依赖");
        }

        // 创建依赖关系
        TaskDependency dependency = new TaskDependency();
        dependency.setPrerequisiteTaskId(dependencyTaskId); // dependencyTaskId 是前置任务
        dependency.setDependentTaskId(taskId); // taskId 依赖于 dependencyTaskId
        dependency.setDependencyType("FINISH_TO_START"); // 默认类型：完成-开始
        dependency.setCreateTime(java.time.LocalDateTime.now());

        taskDependencyMapper.insert(dependency);

        log.info("任务依赖添加成功, taskId={}, dependencyTaskId={}", taskId, dependencyTaskId);
    }

    @Override
    @Transactional
    public void removeTaskDependency(Long taskId, Long dependencyTaskId) {
        log.info("移除任务依赖, taskId={}, dependencyTaskId={}", taskId, dependencyTaskId);

        // 验证两个任务都存在
        validateTaskExists(taskId);
        validateTaskExists(dependencyTaskId);

        // 删除依赖关系
        int deleted = taskDependencyMapper.deleteDependency(dependencyTaskId, taskId);

        if (deleted == 0) {
            log.warn("任务依赖关系不存在, taskId={}, dependencyTaskId={}", taskId, dependencyTaskId);
            throw new BusinessException("任务依赖关系不存在");
        }

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

    /**
     * 验证项目访问权限
     */
    private void validateProjectAccess(Long projectId, Long userId) {
        log.debug("验证项目访问权限, projectId={}, userId={}", projectId, userId);

        // 检查项目是否存在
        Project project = projectMapper.selectById(projectId);
        if (project == null || project.getDeleted()) {
            throw new BusinessException("项目不存在");
        }

        // 检查用户是否有权限访问该项目
        // 项目负责人或项目成员都有权限
        // 这里简化处理，实际应该通过 ProjectService 检查
        if (!project.getOwnerId().equals(userId)) {
            // 可以进一步检查是否为项目成员
            log.debug("用户不是项目负责人, projectId={}, userId={}", projectId, userId);
        }
    }

    /**
     * 验证任务是否存在
     */
    private void validateTaskExists(Long taskId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null || task.getDeleted()) {
            throw new BusinessException("任务不存在");
        }
    }

    /**
     * 验证任务访问权限
     */
    private void validateTaskAccess(Long taskId, Long userId) {
        if (!hasTaskPermission(taskId, userId)) {
            throw new BusinessException("没有权限操作此任务");
        }
    }

    /**
     * 验证用户是否存在
     */
    private void validateUserExists(Long userId) {
        log.debug("验证用户存在, userId={}", userId);

        // 检查用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null || user.getDeleted()) {
            throw new BusinessException("用户不存在");
        }

        // 检查用户状态是否正常
        if (user.getStatus() != 0) { // 0-正常, 1-禁用
            throw new BusinessException("用户状态异常，无法分配任务");
        }
    }

    /**
     * 验证状态转换的合法性
     */
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

    /**
     * 检查是否会形成循环依赖
     * 使用深度优先搜索（DFS）检测循环
     */
    private boolean wouldCreateCircularDependency(Long taskId, Long dependencyTaskId) {
        log.debug("检查循环依赖, taskId={}, dependencyTaskId={}", taskId, dependencyTaskId);

        // 如果 dependencyTaskId 依赖于 taskId（直接或间接），则会形成循环
        return hasTransitiveDependency(dependencyTaskId, taskId);
    }

    /**
     * 检查 fromTask 是否（直接或间接）依赖于 toTask
     * 使用深度优先搜索
     */
    private boolean hasTransitiveDependency(Long fromTask, Long toTask) {
        // 获取 fromTask 的所有前置任务
        List<Long> prerequisites = taskDependencyMapper.findPrerequisiteTaskIds(fromTask);

        // 如果直接依赖，返回 true
        if (prerequisites.contains(toTask)) {
            return true;
        }

        // 递归检查间接依赖
        for (Long prerequisite : prerequisites) {
            if (hasTransitiveDependency(prerequisite, toTask)) {
                return true;
            }
        }

        return false;
    }

    @Override
    @Transactional
    public int batchUpdateTasks(List<Long> taskIds, Integer status, Integer priority,
                               Long assigneeId, String tags, Long userId) {
        log.info("批量更新任务, taskIds={}, status={}, priority={}, assigneeId={}, tags={}, userId={}",
                taskIds, status, priority, assigneeId, tags, userId);

        if (taskIds == null || taskIds.isEmpty()) {
            throw new BusinessException("任务ID列表不能为空");
        }

        // 验证指派人是否存在
        if (assigneeId != null) {
            validateUserExists(assigneeId);
        }

        int successCount = 0;
        for (Long taskId : taskIds) {
            try {
                // 验证任务存在
                Task task = taskMapper.selectById(taskId);
                if (task == null || task.getDeleted()) {
                    log.warn("任务不存在, taskId={}", taskId);
                    continue;
                }

                // 检查权限
                if (!hasTaskPermission(taskId, userId)) {
                    log.warn("没有权限操作任务, taskId={}, userId={}", taskId, userId);
                    continue;
                }

                // 构建更新对象
                Task updateTask = new Task();
                updateTask.setId(taskId);
                updateTask.setUpdaterId((long) userId);

                boolean needUpdate = false;

                if (status != null) {
                    validateStatusTransition(task.getStatus(), status);
                    updateTask.setStatus(status);
                    needUpdate = true;

                    // 如果状态变为已完成，设置完成日期
                    if (status == 3) {
                        updateTask.setCompletedDate(java.time.LocalDate.now());
                    }
                }

                if (priority != null) {
                    updateTask.setPriority(priority);
                    needUpdate = true;
                }

                if (assigneeId != null) {
                    updateTask.setAssigneeId(assigneeId);
                    needUpdate = true;
                }

                if (tags != null) {
                    updateTask.setTags(tags);
                    needUpdate = true;
                }

                if (needUpdate) {
                    taskMapper.updateById(updateTask);
                    successCount++;
                }
            } catch (Exception e) {
                log.error("批量更新任务失败, taskId={}", taskId, e);
            }
        }

        log.info("批量更新任务完成, 总数={}, 成功={}", taskIds.size(), successCount);
        return successCount;
    }

    @Override
    @Transactional
    public int batchDeleteTasks(List<Long> taskIds, Long userId) {
        log.info("批量删除任务, taskIds={}, userId={}", taskIds, userId);

        if (taskIds == null || taskIds.isEmpty()) {
            throw new BusinessException("任务ID列表不能为空");
        }

        int successCount = 0;
        for (Long taskId : taskIds) {
            try {
                // 验证任务存在
                Task task = taskMapper.selectById(taskId);
                if (task == null || task.getDeleted()) {
                    log.warn("任务不存在或已删除, taskId={}", taskId);
                    continue;
                }

                // 检查权限
                if (!hasTaskPermission(taskId, userId)) {
                    log.warn("没有权限删除任务, taskId={}, userId={}", taskId, userId);
                    continue;
                }

                // 检查是否有子任务
                List<Task> subtasks = listSubtasks(taskId);
                if (!subtasks.isEmpty()) {
                    log.warn("任务存在子任务，无法删除, taskId={}", taskId);
                    continue;
                }

                // 软删除
                Task deleteTask = new Task();
                deleteTask.setId(taskId);
                deleteTask.setDeleted(true);
                deleteTask.setUpdaterId((long) userId);
                taskMapper.updateById(deleteTask);

                successCount++;
            } catch (Exception e) {
                log.error("批量删除任务失败, taskId={}", taskId, e);
            }
        }

        log.info("批量删除任务完成, 总数={}, 成功={}", taskIds.size(), successCount);
        return successCount;
    }

    @Override
    @Transactional
    public int batchAssignTasks(List<Long> taskIds, Long assigneeId, Long userId) {
        log.info("批量分配任务, taskIds={}, assigneeId={}, userId={}", taskIds, assigneeId, userId);

        if (taskIds == null || taskIds.isEmpty()) {
            throw new BusinessException("任务ID列表不能为空");
        }

        if (assigneeId == null) {
            throw new BusinessException("指派人ID不能为空");
        }

        // 验证指派人是否存在
        validateUserExists(assigneeId);

        int successCount = 0;
        for (Long taskId : taskIds) {
            try {
                // 验证任务存在
                validateTaskExists(taskId);

                // 检查权限
                if (!hasTaskPermission(taskId, userId)) {
                    log.warn("没有权限分配任务, taskId={}, userId={}", taskId, userId);
                    continue;
                }

                // 更新指派人
                Task task = new Task();
                task.setId(taskId);
                task.setAssigneeId(assigneeId);
                task.setUpdaterId((long) userId);
                taskMapper.updateById(task);

                successCount++;
            } catch (Exception e) {
                log.error("批量分配任务失败, taskId={}", taskId, e);
            }
        }

        log.info("批量分配任务完成, 总数={}, 成功={}", taskIds.size(), successCount);
        return successCount;
    }

    @Override
    public PageResult<TaskActivity> listTaskActivities(Long taskId, Integer page, Integer size) {
        log.debug("获取任务活动列表, taskId={}, page={}, size={}", taskId, page, size);
        
        validateTaskExists(taskId);
        
        LambdaQueryWrapper<TaskActivity> wrapper = new LambdaQueryWrapper<TaskActivity>()
                .eq(TaskActivity::getTaskId, taskId)
                .orderByDesc(TaskActivity::getCreateTime);
        
        IPage<TaskActivity> activityPage = taskActivityMapper.selectPage(new Page<>(page, size), wrapper);
        
        return PageResult.of(activityPage.getRecords(), activityPage.getTotal(), page, size);
    }

    @Override
    @Transactional
    public Long addTaskActivity(TaskActivity activity) {
        log.info("添加任务活动, taskId={}, activityType={}", activity.getTaskId(), activity.getActivityType());
        
        validateTaskExists(activity.getTaskId());
        
        taskActivityMapper.insert(activity);
        
        log.info("任务活动添加成功, activityId={}", activity.getId());
        return activity.getId();
    }

    @Override
    public List<TaskAttachment> listTaskAttachments(Long taskId) {
        log.debug("获取任务附件列表, taskId={}", taskId);
        
        validateTaskExists(taskId);
        
        LambdaQueryWrapper<TaskAttachment> wrapper = new LambdaQueryWrapper<TaskAttachment>()
                .eq(TaskAttachment::getTaskId, taskId)
                .eq(TaskAttachment::getDeleted, false)
                .orderByDesc(TaskAttachment::getCreateTime);
        
        return taskAttachmentMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public Long addTaskAttachment(TaskAttachment attachment) {
        log.info("添加任务附件, taskId={}, fileName={}", attachment.getTaskId(), attachment.getFileName());
        
        validateTaskExists(attachment.getTaskId());
        
        taskAttachmentMapper.insert(attachment);
        
        log.info("任务附件添加成功, attachmentId={}", attachment.getId());
        return attachment.getId();
    }

    @Override
    @Transactional
    public void deleteTaskAttachment(Long attachmentId, Long userId) {
        log.info("删除任务附件, attachmentId={}, userId={}", attachmentId, userId);
        
        TaskAttachment attachment = taskAttachmentMapper.selectById(attachmentId);
        if (attachment == null) {
            throw new BusinessException("附件不存在");
        }
        
        validateTaskAccess(attachment.getTaskId(), userId);
        
        attachment.setDeleted(true);
        attachment.setUpdaterId(userId);
        taskAttachmentMapper.updateById(attachment);
        
        log.info("任务附件删除成功, attachmentId={}", attachmentId);
    }

    @Override
    public List<TaskCheckItem> listTaskCheckItems(Long taskId) {
        log.debug("获取任务检查项列表, taskId={}", taskId);
        
        validateTaskExists(taskId);
        
        LambdaQueryWrapper<TaskCheckItem> wrapper = new LambdaQueryWrapper<TaskCheckItem>()
                .eq(TaskCheckItem::getTaskId, taskId)
                .orderByAsc(TaskCheckItem::getSortOrder);
        
        return taskCheckItemMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public Long addTaskCheckItem(TaskCheckItem checkItem) {
        log.info("添加任务检查项, taskId={}, content={}", checkItem.getTaskId(), checkItem.getContent());
        
        validateTaskExists(checkItem.getTaskId());
        
        taskCheckItemMapper.insert(checkItem);
        
        log.info("任务检查项添加成功, checkItemId={}", checkItem.getId());
        return checkItem.getId();
    }

    @Override
    @Transactional
    public void updateTaskCheckItem(TaskCheckItem checkItem) {
        log.info("更新任务检查项, checkItemId={}", checkItem.getId());
        
        TaskCheckItem existingCheckItem = taskCheckItemMapper.selectById(checkItem.getId());
        if (existingCheckItem == null) {
            throw new BusinessException("检查项不存在");
        }
        
        validateTaskAccess(existingCheckItem.getTaskId(), checkItem.getUpdaterId());
        
        taskCheckItemMapper.updateById(checkItem);
        
        log.info("任务检查项更新成功, checkItemId={}", checkItem.getId());
    }

    @Override
    @Transactional
    public void deleteTaskCheckItem(Long checkItemId, Long userId) {
        log.info("删除任务检查项, checkItemId={}, userId={}", checkItemId, userId);
        
        TaskCheckItem checkItem = taskCheckItemMapper.selectById(checkItemId);
        if (checkItem == null) {
            throw new BusinessException("检查项不存在");
        }
        
        validateTaskAccess(checkItem.getTaskId(), userId);
        
        taskCheckItemMapper.deleteById(checkItemId);
        
        log.info("任务检查项删除成功, checkItemId={}", checkItemId);
    }
}