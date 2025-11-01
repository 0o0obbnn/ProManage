package com.promanage.service.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.promanage.service.entity.DocumentVersion;

/**
 * 文档版本Mapper接口
 *
 * <p>提供文档版本数据访问方法,支持版本历史的查询和管理
 *
 * @author ProManage Team
 * @date 2025-09-30
 */
@Mapper
public interface DocumentVersionMapper extends BaseMapper<DocumentVersion> {

  /**
   * 根据文档ID查找版本列表
   *
   * <p>查询文档的所有历史版本,按创建时间倒序排列
   *
   * @param documentId 文档ID
   * @return 文档版本列表
   */
  List<DocumentVersion> findByDocumentId(@Param("documentId") Long documentId);

  /**
   * 根据文档ID和版本号查找版本
   *
   * <p>精确匹配文档和版本号
   *
   * @param documentId 文档ID
   * @param version 版本号 (例如: 1.0.0)
   * @return 文档版本实体,如果不存在返回null
   */
  DocumentVersion findByDocumentIdAndVersion(
      @Param("documentId") Long documentId, @Param("version") String version);

  /**
   * 获取文档的最新版本
   *
   * <p>根据创建时间获取最新的版本记录
   *
   * @param documentId 文档ID
   * @return 最新版本实体,如果不存在返回null
   */
  DocumentVersion findLatestByDocumentId(@Param("documentId") Long documentId);

  /**
   * 统计文档的版本数量
   *
   * <p>统计文档的历史版本总数
   *
   * @param documentId 文档ID
   * @return 版本数量
   */
  int countByDocumentId(@Param("documentId") Long documentId);

  /**
   * 根据文档ID删除所有版本
   *
   * <p>用于删除文档时清理版本历史
   *
   * @param documentId 文档ID
   * @return 删除的记录数
   */
  int deleteByDocumentId(@Param("documentId") Long documentId);
}
