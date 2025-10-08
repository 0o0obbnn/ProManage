package com.promanage.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.promanage.service.entity.DocumentTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文档标签关联Mapper接口
 * <p>
 * 处理文档和标签的关联关系数据访问
 * </p>
 *
 * @author ProManage Team
 * @since 2025-10-07
 */
@Mapper
public interface DocumentTagMapper extends BaseMapper<DocumentTag> {

    /**
     * 根据文档ID查询关联的标签ID列表
     *
     * @param documentId 文档ID
     * @return 标签ID列表
     */
    List<Long> findTagIdsByDocumentId(@Param("documentId") Long documentId);

    /**
     * 根据标签ID查询关联的文档ID列表
     *
     * @param tagId 标签ID
     * @return 文档ID列表
     */
    List<Long> findDocumentIdsByTagId(@Param("tagId") Long tagId);

    /**
     * 删除文档的所有标签关联
     *
     * @param documentId 文档ID
     * @return 影响行数
     */
    int deleteByDocumentId(@Param("documentId") Long documentId);

    /**
     * 删除标签的所有文档关联
     *
     * @param tagId 标签ID
     * @return 影响行数
     */
    int deleteByTagId(@Param("tagId") Long tagId);

    /**
     * 批量创建文档标签关联
     *
     * @param documentTags 文档标签关联列表
     * @return 影响行数
     */
    int batchInsert(@Param("documentTags") List<DocumentTag> documentTags);

    /**
     * 检查文档是否已关联指定标签
     *
     * @param documentId 文档ID
     * @param tagId      标签ID
     * @return 关联数量
     */
    int countByDocumentIdAndTagId(@Param("documentId") Long documentId, @Param("tagId") Long tagId);
}