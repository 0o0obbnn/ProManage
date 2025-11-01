package com.promanage.service.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import com.promanage.service.constant.CommonConstants;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.promanage.service.entity.DocumentFavorite;

/**
 * 文档收藏Mapper接口
 *
 * <p>处理文档收藏数据访问
 *
 * @author ProManage Team
 * @since 2025-10-07
 */
@Mapper
public interface DocumentFavoriteMapper extends BaseMapper<DocumentFavorite> {

  /**
   * 根据用户ID查询收藏的文档ID列表
   *
   * @param userId 用户ID
   * @return 文档ID列表
   */
  List<Long> findDocumentIdsByUserId(@Param("userId") Long userId);

  /**
   * 根据文档ID查询收藏的用户ID列表
   *
   * @param documentId 文档ID
   * @return 用户ID列表
   */
  List<Long> findUserIdsByDocumentId(@Param("documentId") Long documentId);

  /**
   * 统计文档的收藏数量
   *
   * @param documentId 文档ID
   * @return 收藏数量
   */
  int countByDocumentId(@Param("documentId") Long documentId);

  /**
   * 检查用户是否已收藏文档
   *
   * @param documentId 文档ID
   * @param userId 用户ID
   * @return 收藏记录数量
   */
  int countByDocumentIdAndUserId(
      @Param("documentId") Long documentId, @Param("userId") Long userId);

  /**
   * 根据用户ID和文档ID查询收藏记录
   *
   * @param documentId 文档ID
   * @param userId 用户ID
   * @return 收藏记录
   */
  DocumentFavorite findByDocumentIdAndUserId(
      @Param("documentId") Long documentId, @Param("userId") Long userId);

  /**
   * 删除用户对文档的收藏
   *
   * @param documentId 文档ID
   * @param userId 用户ID
   * @return 影响行数
   */
  int deleteByDocumentIdAndUserId(
      @Param("documentId") Long documentId, @Param("userId") Long userId);

  /**
   * 删除文档的所有收藏记录
   *
   * @param documentId 文档ID
   * @return 影响行数
   */
  int deleteByDocumentId(@Param("documentId") Long documentId);

  /**
   * 删除用户的所有收藏记录
   *
   * @param userId 用户ID
   * @return 影响行数
   */
  int deleteByUserId(@Param("userId") Long userId);
}
