package com.promanage.service.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.promanage.service.dto.SearchResultDTO;
import com.promanage.service.entity.Task;

/**
 * 任务Mapper接口
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-03
 */
@Mapper
public interface TaskMapper extends BaseMapper<Task> {

  /**
   * 搜索任务（用于搜索服务）
   *
   * @param params 搜索参数
   * @return 任务列表
   */
  List<Task> searchTasks(@Param("params") Map<String, Object> params);

  /**
   * 统计搜索任务数量
   *
   * @param params 搜索参数
   * @return 任务数量
   */
  long countSearchTasks(@Param("params") Map<String, Object> params);

  /**
   * 根据关键词获取不重复的任务标题
   *
   * @param keyword 关键词
   * @param limit 限制数量
   * @return 任务标题列表
   */
  List<String> getDistinctTitlesByKeyword(
      @Param("keyword") String keyword, @Param("limit") Integer limit);

  /**
   * 搜索任务（用于搜索服务）
   *
   * @param keyword 关键词
   * @param projectId 项目ID（可选）
   * @param offset 偏移量
   * @param limit 限制数量
   * @return 任务搜索结果列表
   */
  List<SearchResultDTO> searchTasks(
      @Param("keyword") String keyword,
      @Param("projectId") Long projectId,
      @Param("offset") Integer offset,
      @Param("limit") Integer limit);

  /**
   * 统计搜索任务数量
   *
   * @param keyword 关键词
   * @param projectId 项目ID（可选）
   * @return 任务数量
   */
  Long countSearchTasks(@Param("keyword") String keyword, @Param("projectId") Long projectId);

  /**
   * 根据关键词获取不重复的任务标题（用于搜索建议）
   *
   * @param keyword 关键词
   * @return 任务标题列表
   */
  List<String> getDistinctTitlesByKeyword(@Param("keyword") String keyword);
}
