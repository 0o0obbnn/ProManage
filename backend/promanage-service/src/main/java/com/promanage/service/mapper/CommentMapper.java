package com.promanage.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.promanage.service.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 评论数据访问层
 * <p>
 * 提供通用评论的数据库操作，支持多种实体类型
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-03
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 根据实体类型和实体ID查询评论列表
     * <p>
     * 按创建时间倒序排列
     * </p>
     *
     * @param entityType 实体类型
     * @param entityId 实体ID
     * @return 评论列表
     */
    List<Comment> findByEntityTypeAndEntityId(@Param("entityType") String entityType, 
                                               @Param("entityId") Long entityId);

    /**
     * 统计实体的评论数量
     *
     * @param entityType 实体类型
     * @param entityId 实体ID
     * @return 评论数量
     */
    int countByEntityTypeAndEntityId(@Param("entityType") String entityType, 
                                     @Param("entityId") Long entityId);

    /**
     * 根据作者ID查询评论列表
     *
     * @param authorId 作者ID
     * @return 评论列表
     */
    List<Comment> findByAuthorId(@Param("authorId") Long authorId);

    /**
     * 根据父评论ID查询回复列表
     *
     * @param parentCommentId 父评论ID
     * @return 回复列表
     */
    List<Comment> findByParentCommentId(@Param("parentCommentId") Long parentCommentId);
}

