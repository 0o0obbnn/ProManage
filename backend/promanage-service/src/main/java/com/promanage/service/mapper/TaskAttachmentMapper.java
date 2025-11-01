package com.promanage.service.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.promanage.service.entity.TaskAttachment;

/**
 * 任务附件Mapper接口
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-07
 */
@Mapper
public interface TaskAttachmentMapper extends BaseMapper<TaskAttachment> {

  /**
   * 统计指定任务的附件数量
   *
   * @param taskId 任务ID
   * @return 附件数量
   */
  @Select(
      "SELECT COUNT(*) FROM tb_task_attachment WHERE task_id = #{taskId} AND deleted_at IS NULL")
  int countByTaskId(@Param("taskId") Long taskId);
}
