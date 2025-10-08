package com.promanage.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.promanage.service.entity.TaskAttachment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务附件Mapper接口
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-07
 */
@Mapper
public interface TaskAttachmentMapper extends BaseMapper<TaskAttachment> {
    
}