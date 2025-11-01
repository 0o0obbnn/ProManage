package com.promanage.service.strategy;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.promanage.common.exception.BusinessException;
import com.promanage.common.domain.ResultCode;
import com.promanage.service.entity.TaskAttachment;
import com.promanage.service.mapper.TaskAttachmentMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 任务附件策略
 *
 * @author ProManage Team
 * @date 2025-10-22
 */
@Slf4j
@RequiredArgsConstructor
public class TaskAttachmentStrategy {

    private final TaskAttachmentMapper taskAttachmentMapper;

    /**
     * 获取任务附件列表
     */
    public List<TaskAttachment> listTaskAttachments(Long taskId) {
        LambdaQueryWrapper<TaskAttachment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskAttachment::getTaskId, taskId);
        wrapper.orderByDesc(TaskAttachment::getCreateTime);
        return taskAttachmentMapper.selectList(wrapper);
    }

    /**
     * 添加任务附件
     */
    public Long addTaskAttachment(TaskAttachment attachment) {
        validateAttachment(attachment);
        
        attachment.setCreateTime(LocalDateTime.now());
        attachment.setUpdateTime(LocalDateTime.now());
        
        taskAttachmentMapper.insert(attachment);
        return attachment.getId();
    }

    /**
     * 删除任务附件
     */
    public void deleteTaskAttachment(Long attachmentId, Long userId) {
        TaskAttachment attachment = getTaskAttachmentById(attachmentId);
        if (attachment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "附件不存在");
        }
        
        // 检查权限：只有附件上传者可以删除
        if (!attachment.getUploaderId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "您无权删除此附件");
        }
        
        taskAttachmentMapper.deleteById(attachmentId);
    }

    /**
     * 根据ID获取任务附件
     */
    public TaskAttachment getTaskAttachmentById(Long attachmentId) {
        return taskAttachmentMapper.selectById(attachmentId);
    }

    /**
     * 获取任务附件数量
     */
    public int getAttachmentCount(Long taskId) {
        LambdaQueryWrapper<TaskAttachment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskAttachment::getTaskId, taskId);
        return Math.toIntExact(taskAttachmentMapper.selectCount(wrapper));
    }

    /**
     * 验证附件
     */
    private void validateAttachment(TaskAttachment attachment) {
        if (attachment == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "附件信息不能为空");
        }
        if (attachment.getTaskId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "任务ID不能为空");
        }
        if (attachment.getUploaderId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }
        if (attachment.getFileName() == null || attachment.getFileName().trim().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "文件名不能为空");
        }
        if (attachment.getFilePath() == null || attachment.getFilePath().trim().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "文件URL不能为空");
        }
    }
}
