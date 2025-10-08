package com.promanage.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.promanage.service.entity.TaskComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 任务评论Mapper接口
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-03
 */
@Mapper
public interface TaskCommentMapper extends BaseMapper<TaskComment> {

    /**
     * 统计评论的回复数量
     *
     * @param parentCommentId 父评论ID
     * @return 回复数量
     */
    int countReplies(@Param("parentCommentId") Long parentCommentId);
}