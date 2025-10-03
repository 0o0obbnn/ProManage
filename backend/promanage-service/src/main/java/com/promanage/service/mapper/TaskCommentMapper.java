package com.promanage.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.promanage.service.entity.TaskComment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务评论Mapper接口
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-03
 */
@Mapper
public interface TaskCommentMapper extends BaseMapper<TaskComment> {
    // 可以在这里添加自定义的SQL方法
}