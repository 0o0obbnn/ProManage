package com.promanage.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.promanage.service.entity.ProjectMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 项目成员Mapper接口
 * <p>
 * 提供项目成员数据访问方法,管理项目团队成员信息
 * </p>
 *
 * @author ProManage Team
 * @date 2025-09-30
 */
@Mapper
public interface ProjectMemberMapper extends BaseMapper<ProjectMember> {

    /**
     * 根据项目ID查找成员列表
     * <p>
     * 查询项目的所有成员
     * </p>
     *
     * @param projectId 项目ID
     * @return 项目成员列表
     */
    List<ProjectMember> findByProjectId(@Param("projectId") Long projectId);

    /**
     * 根据用户ID查找项目成员记录
     * <p>
     * 查询用户参与的所有项目成员记录
     * </p>
     *
     * @param userId 用户ID
     * @return 项目成员列表
     */
    List<ProjectMember> findByUserId(@Param("userId") Long userId);

    /**
     * 根据项目ID和用户ID查找成员记录
     * <p>
     * 用于检查用户是否为项目成员
     * </p>
     *
     * @param projectId 项目ID
     * @param userId 用户ID
     * @return 项目成员实体,如果不存在返回null
     */
    ProjectMember findByProjectIdAndUserId(@Param("projectId") Long projectId, @Param("userId") Long userId);

    /**
     * 根据项目ID删除所有成员
     * <p>
     * 用于删除项目时清理成员关系
     * </p>
     *
     * @param projectId 项目ID
     * @return 删除的记录数
     */
    int deleteByProjectId(@Param("projectId") Long projectId);

    /**
     * 批量插入项目成员
     * <p>
     * 用于批量添加项目成员
     * </p>
     *
     * @param members 项目成员列表
     * @return 插入的记录数
     */
    int batchInsert(@Param("list") List<ProjectMember> members);

    /**
     * 检查用户是否为项目成员
     * <p>
     * 用于权限验证
     * </p>
     *
     * @param projectId 项目ID
     * @param userId 用户ID
     * @return true表示是成员,false表示不是成员
     */
    boolean existsByProjectIdAndUserId(@Param("projectId") Long projectId, @Param("userId") Long userId);
}