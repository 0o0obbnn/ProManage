package com.promanage.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.promanage.service.entity.TaskCheckItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务检查项Mapper接口
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-07
 */
@Mapper
public interface TaskCheckItemMapper extends BaseMapper<TaskCheckItem> {
    
}