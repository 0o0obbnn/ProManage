package com.promanage.service.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.promanage.service.dto.SearchResultDTO;
import com.promanage.service.entity.Document;

/**
 * 文档Mapper接口
 *
 * <p>提供文档数据访问方法,支持文档的查询、创建、更新和删除 支持全文搜索和统计功能
 *
 * @author ProManage Team
 * @date 2025-09-30
 */
@Mapper
public interface DocumentMapper extends BaseMapper<Document> {

  /**
   * 根据项目ID查找文档列表
   *
   * <p>查询指定项目的所有文档
   *
   * @param projectId 项目ID
   * @return 文档列表
   */
  List<Document> findByProjectId(@Param("projectId") Long projectId);

  /**
   * 根据关键词搜索文档
   *
   * <p>在文档标题、内容和摘要中搜索关键词
   *
   * @param keyword 搜索关键词
   * @return 文档列表
   */
  List<Document> searchByKeyword(@Param("keyword") String keyword);

  /**
   * 根据创建人ID查找文档列表
   *
   * <p>查询用户创建的所有文档
   *
   * @param creatorId 创建人ID
   * @return 文档列表
   */
  List<Document> findByCreatorId(@Param("creatorId") Long creatorId);

  /**
   * 根据文档类型查找文档列表
   *
   * <p>查询指定类型的所有文档
   *
   * @param type 文档类型 (PRD/Design/API/Test/Other)
   * @return 文档列表
   */
  List<Document> findByType(@Param("type") String type);

  /**
   * 根据文件夹ID查找文档列表
   *
   * <p>查询指定文件夹下的所有文档
   *
   * @param folderId 文件夹ID
   * @return 文档列表
   */
  List<Document> findByFolderId(@Param("folderId") Long folderId);

  /**
   * 增加文档浏览次数
   *
   * <p>每次查看文档时调用,将浏览次数加1
   *
   * @param id 文档ID
   * @return 更新的记录数
   */
  int incrementViewCount(@Param("id") Long id);

  /**
   * 统计项目文档数量
   *
   * <p>统计指定项目的文档总数
   *
   * @param projectId 项目ID
   * @return 文档数量
   */
  int countByProjectId(@Param("projectId") Long projectId);

  /**
   * 统计用户创建的文档数量
   *
   * <p>统计用户创建的文档总数
   *
   * @param creatorId 创建人ID
   * @return 文档数量
   */
  int countByCreatorId(@Param("creatorId") Long creatorId);

  /**
   * 根据多个条件搜索文档
   *
   * @param page 页码
   * @param size 每页大小
   * @param projectId 项目ID（可选）
   * @param status 文档状态（可选）
   * @param keyword 搜索关键词（可选）
   * @param folderId 文件夹ID（可选）
   * @param creatorId 创建人ID（可选）
   * @param type 文档类型（可选）
   * @param startTime 创建时间开始（可选）
   * @param endTime 创建时间结束（可选）
   * @return 分页结果
   */
  IPage<Document> searchDocuments(
      Page<Document> page,
      @Param("projectId") Long projectId,
      @Param("status") Integer status,
      @Param("keyword") String keyword,
      @Param("folderId") Long folderId,
      @Param("creatorId") Long creatorId,
      @Param("type") String type,
      @Param("startTime") LocalDateTime startTime,
      @Param("endTime") LocalDateTime endTime);

  /**
   * 搜索文档（用于搜索服务）
   *
   * @param params 搜索参数
   * @return 文档列表
   */
  List<Document> searchDocuments(@Param("params") java.util.Map<String, Object> params);

  /**
   * 统计搜索文档数量
   *
   * @param params 搜索参数
   * @return 文档数量
   */
  long countSearchDocuments(@Param("params") java.util.Map<String, Object> params);

  /**
   * 根据关键词获取不重复的文档标题
   *
   * @param keyword 关键词
   * @param limit 限制数量
   * @return 文档标题列表
   */
  List<String> getDistinctTitlesByKeyword(
      @Param("keyword") String keyword, @Param("limit") Integer limit);

  /**
   * 搜索文档（用于搜索服务）
   *
   * @param keyword 关键词
   * @param projectId 项目ID（可选）
   * @param offset 偏移量
   * @param limit 限制数量
   * @return 文档搜索结果列表
   */
  List<SearchResultDTO> searchDocuments(
      @Param("keyword") String keyword,
      @Param("projectId") Long projectId,
      @Param("offset") Integer offset,
      @Param("limit") Integer limit);

  /**
   * 统计搜索文档数量
   *
   * @param keyword 关键词
   * @param projectId 项目ID（可选）
   * @return 文档数量
   */
  Long countSearchDocuments(@Param("keyword") String keyword, @Param("projectId") Long projectId);

  /**
   * 根据关键词获取不重复的文档标题（用于搜索建议）
   *
   * @param keyword 关键词
   * @return 文档标题列表
   */
  List<String> getDistinctTitlesByKeyword(@Param("keyword") String keyword);
}
