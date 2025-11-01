package com.promanage.service.strategy;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.promanage.common.exception.BusinessException;
import com.promanage.common.result.PageResult;
import com.promanage.common.domain.ResultCode;
import com.promanage.service.entity.TaskComment;
import com.promanage.service.mapper.TaskCommentMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 任务评论策略
 *
 * @author ProManage Team
 * @date 2025-10-22
 */
@Slf4j
@RequiredArgsConstructor
public class TaskCommentStrategy {

    private final TaskCommentMapper taskCommentMapper;

    /**
     * 分页查询任务评论
     */
    public PageResult<TaskComment> listTaskComments(Long taskId, Integer page, Integer size) {
        LambdaQueryWrapper<TaskComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskComment::getTaskId, taskId);
        wrapper.orderByDesc(TaskComment::getCreateTime);
        
        IPage<TaskComment> pageResult = taskCommentMapper.selectPage(
            new Page<>(page, size), wrapper);
        
        return PageResult.<TaskComment>builder()
            .list(pageResult.getRecords())
            .total(pageResult.getTotal())
            .page(page)
            .pageSize(size)
            .build();
    }

    /**
     * 添加任务评论
     */
    public Long addTaskComment(TaskComment comment) {
        validateComment(comment);
        
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());
        
        taskCommentMapper.insert(comment);
        return comment.getId();
    }

    /**
     * 删除任务评论
     */
    public void deleteTaskComment(Long commentId, Long userId) {
        TaskComment comment = getTaskCommentById(commentId);
        if (comment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "评论不存在");
        }
        
        // 检查权限：只有评论作者可以删除
        if (!comment.getAuthorId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "您无权删除此评论");
        }
        
        taskCommentMapper.deleteById(commentId);
    }

    /**
     * 更新任务评论
     */
    public void updateTaskComment(TaskComment comment) {
        validateComment(comment);
        
        TaskComment existingComment = getTaskCommentById(comment.getId());
        if (existingComment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "评论不存在");
        }
        
        // 检查权限：只有评论作者可以更新
        if (!existingComment.getAuthorId().equals(comment.getAuthorId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "您无权修改此评论");
        }
        
        comment.setUpdateTime(LocalDateTime.now());
        taskCommentMapper.updateById(comment);
    }

    /**
     * 根据ID获取任务评论
     */
    public TaskComment getTaskCommentById(Long commentId) {
        return taskCommentMapper.selectById(commentId);
    }

    /**
     * 获取任务评论数量
     */
    public int getCommentCount(Long taskId) {
        LambdaQueryWrapper<TaskComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskComment::getTaskId, taskId);
        return Math.toIntExact(taskCommentMapper.selectCount(wrapper));
    }

    /**
     * 验证评论
     */
    private void validateComment(TaskComment comment) {
        if (comment == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "评论信息不能为空");
        }
        if (comment.getTaskId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "任务ID不能为空");
        }
        if (comment.getAuthorId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }
        if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "评论内容不能为空");
        }
    }
}
