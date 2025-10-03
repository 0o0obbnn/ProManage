package com.promanage.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.promanage.service.entity.Task;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务Mapper接口
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-03
 */
@Mapper
public interface TaskMapper extends BaseMapper<Task> {
    // 可以在这里添加自定义的SQL方法
}