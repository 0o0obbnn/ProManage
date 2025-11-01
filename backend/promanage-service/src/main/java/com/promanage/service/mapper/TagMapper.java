package com.promanage.service.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.promanage.service.entity.Tag;

/**
 * 标签Mapper接口
 *
 * <p>处理标签数据访问
 *
 * @author ProManage Team
 * @since 2025-10-07
 */
@Mapper
public interface TagMapper extends BaseMapper<Tag> {

  /**
   * 根据项目ID查询标签列表
   *
   * @param projectId 项目ID（null表示查询全局标签）
   * @return 标签列表
   */
  List<Tag> findByProjectId(@Param("projectId") Long projectId);

  /**
   * 根据文档ID查询关联的标签列表
   *
   * @param documentId 文档ID
   * @return 标签列表
   */
  List<Tag> findByDocumentId(@Param("documentId") Long documentId);

  /**
   * 根据名称查询标签
   *
   * @param name 标签名称
   * @param projectId 项目ID（null表示全局标签）
   * @return 标签实体
   */
  Tag findByName(@Param("name") String name, @Param("projectId") Long projectId);

  /**
   * 增加标签使用次数
   *
   * @param tagId 标签ID
   * @return 影响行数
   */
  int incrementUsageCount(@Param("tagId") Long tagId);

  /**
   * 减少标签使用次数
   *
   * @param tagId 标签ID
   * @return 影响行数
   */
  int decrementUsageCount(@Param("tagId") Long tagId);

  /**
   * 查询热门标签（按使用次数排序）
   *
   * @param projectId 项目ID（null表示全局标签）
   * @param limit 限制数量
   * @return 标签列表
   */
  List<Tag> findPopularTags(@Param("projectId") Long projectId, @Param("limit") Integer limit);
}
