package com.promanage.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.promanage.service.entity.DocumentFolder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 文档文件夹Mapper接口
 * <p>
 * 提供文档文件夹数据访问方法
 * </p>
 *
 * @author ProManage Team
 * @date 2025-10-04
 */
@Mapper
public interface DocumentFolderMapper extends BaseMapper<DocumentFolder> {

    /**
     * 根据项目ID查找文件夹列表
     * <p>
     * 查询指定项目的所有文件夹
     * </p>
     *
     * @param projectId 项目ID
     * @return 文件夹列表
     */
    List<DocumentFolder> findByProjectId(@Param("projectId") Long projectId);

    /**
     * 根据父文件夹ID查找子文件夹列表
     * <p>
     * 查询指定文件夹下的所有子文件夹
     * </p>
     *
     * @param parentId 父文件夹ID
     * @return 文件夹列表
     */
    List<DocumentFolder> findByParentId(@Param("parentId") Long parentId);

    /**
     * 根据项目ID和父文件夹ID查找文件夹列表
     *
     * @param projectId 项目ID
     * @param parentId  父文件夹ID
     * @return 文件夹列表
     */
    List<DocumentFolder> findByProjectIdAndParentId(@Param("projectId") Long projectId,
                                                    @Param("parentId") Long parentId);

    /**
     * 统计文件夹下的文档数量
     *
     * @param folderId 文件夹ID
     * @return 文档数量
     */
    @Select("SELECT COUNT(*) FROM tb_document WHERE folder_id = #{folderId} AND deleted = false")
    int countDocumentsInFolder(@Param("folderId") Long folderId);

    /**
     * 统计项目中的文件夹数量
     *
     * @param projectId 项目ID
     * @return 文件夹数量
     */
    @Select("SELECT COUNT(*) FROM tb_document_folder WHERE project_id = #{projectId} AND deleted = false")
    int countByProjectId(@Param("projectId") Long projectId);
}