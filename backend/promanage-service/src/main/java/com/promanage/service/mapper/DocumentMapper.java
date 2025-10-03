package com.promanage.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.promanage.service.entity.Document;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文档Mapper接口
 * <p>
 * 提供文档数据访问方法,支持文档的查询、创建、更新和删除
 * 支持全文搜索和统计功能
 * </p>
 *
 * @author ProManage Team
 * @date 2025-09-30
 */
@Mapper
public interface DocumentMapper extends BaseMapper<Document> {

    /**
     * 根据项目ID查找文档列表
     * <p>
     * 查询指定项目的所有文档
     * </p>
     *
     * @param projectId 项目ID
     * @return 文档列表
     */
    List<Document> findByProjectId(@Param("projectId") Long projectId);

    /**
     * 根据关键词搜索文档
     * <p>
     * 在文档标题、内容和摘要中搜索关键词
     * </p>
     *
     * @param keyword 搜索关键词
     * @return 文档列表
     */
    List<Document> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 根据创建人ID查找文档列表
     * <p>
     * 查询用户创建的所有文档
     * </p>
     *
     * @param creatorId 创建人ID
     * @return 文档列表
     */
    List<Document> findByCreatorId(@Param("creatorId") Long creatorId);

    /**
     * 根据文档类型查找文档列表
     * <p>
     * 查询指定类型的所有文档
     * </p>
     *
     * @param type 文档类型 (PRD/Design/API/Test/Other)
     * @return 文档列表
     */
    List<Document> findByType(@Param("type") String type);

    /**
     * 根据文件夹ID查找文档列表
     * <p>
     * 查询指定文件夹下的所有文档
     * </p>
     *
     * @param folderId 文件夹ID
     * @return 文档列表
     */
    List<Document> findByFolderId(@Param("folderId") Long folderId);

    /**
     * 增加文档浏览次数
     * <p>
     * 每次查看文档时调用,将浏览次数加1
     * </p>
     *
     * @param id 文档ID
     * @return 更新的记录数
     */
    int incrementViewCount(@Param("id") Long id);

    /**
     * 统计项目文档数量
     * <p>
     * 统计指定项目的文档总数
     * </p>
     *
     * @param projectId 项目ID
     * @return 文档数量
     */
    int countByProjectId(@Param("projectId") Long projectId);

    /**
     * 统计用户创建的文档数量
     * <p>
     * 统计用户创建的文档总数
     * </p>
     *
     * @param creatorId 创建人ID
     * @return 文档数量
     */
    int countByCreatorId(@Param("creatorId") Long creatorId);
}