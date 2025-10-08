package com.promanage.service.service;

import com.promanage.common.domain.PageResult;
import com.promanage.service.entity.Project;
import com.promanage.service.entity.ProjectMember;
// import com.promanage.service.entity.ProjectMember;

import java.util.List;

/**
 * 项目服务接口
 * <p>
 * 提供项目管理的业务逻辑,包括项目的CRUD操作和成员管理
 * </p>
 *
 * @author ProManage Team
 * @date 2025-09-30
 */
public interface IProjectService {

    /**
     * 根据ID查询项目详情
     *
     * @param id 项目ID
     * @return 项目实体
     */
    Project getById(Long id);

    /**
     * 根据项目编码查询项目
     *
     * @param code 项目编码
     * @return 项目实体
     */
    Project getByCode(String code);

    /**
     * 分页查询项目列表
     *
     * @param page 页码
     * @param pageSize 每页大小
     * @param keyword 搜索关键词
     * @param status 项目状态 (可选)
     * @return 分页结果
     */
    PageResult<Project> listProjects(Integer page, Integer pageSize, String keyword, Integer status);

    /**
     * 查询用户作为负责人的项目列表
     *
     * @param ownerId 负责人ID
     * @return 项目列表
     */
    List<Project> listByOwnerId(Long ownerId);

    /**
     * 查询用户参与的项目列表
     *
     * @param userId 用户ID
     * @return 项目列表
     */
    List<Project> listByMemberId(Long userId);

    /**
     * 创建项目
     *
     * @param project 项目实体
     * @return 项目ID
     */
    Long create(Project project);

    /**
     * 更新项目
     *
     * @param id 项目ID
     * @param project 项目实体
     */
    void update(Long id, Project project);

    /**
     * 删除项目
     *
     * @param id 项目ID
     */
    void delete(Long id);

    /**
     * 批量删除项目
     *
     * @param ids 项目ID列表
     * @return 删除的记录数
     */
    int batchDelete(List<Long> ids);

    /**
     * 更新项目状态
     *
     * @param id 项目ID
     * @param status 状态值
     */
    void updateStatus(Long id, Integer status);

    /**
     * 添加项目成员
     *
     * @param projectId 项目ID
     * @param userId 用户ID
     * @param roleId 项目角色ID
     */
    void addMember(Long projectId, Long userId, Long roleId);

    /**
     * 批量添加项目成员
     *
     * @param projectId 项目ID
     * @param members 成员列表
     */
    void addMembers(Long projectId, List<com.promanage.service.dto.ProjectMemberDTO> members);

    /**
     * 移除项目成员
     *
     * @param projectId 项目ID
     * @param userId 用户ID
     */
    void removeMember(Long projectId, Long userId);

    /**
     * 查询项目成员列表
     *
     * @param projectId 项目ID
     * @return 成员列表
     */
    List<com.promanage.service.dto.ProjectMemberDTO> listMembers(Long projectId);

    /**
     * 检查用户是否为项目成员
     *
     * @param projectId 项目ID
     * @param userId 用户ID
     * @return true表示是成员
     */
    boolean isMember(Long projectId, Long userId);

    /**
     * 检查项目编码是否存在
     *
     * @param code 项目编码
     * @return true表示存在
     */
    boolean existsByCode(String code);

    /**
     * 统计项目成员数量
     *
     * @param projectId 项目ID
     * @return 成员数量
     */
    int countProjectMembers(Long projectId);

    /**
     * 查询用户项目列表
     *
     * @param userId 用户ID
     * @param page 页码
     * @param pageSize 每页大小
     * @param status 项目状态
     * @return 项目分页结果
     */
    PageResult<Project> listUserProjects(Long userId, Integer page, Integer pageSize, Integer status);

    /**
     * 检查用户是否有项目查看权限
     *
     * @param projectId 项目ID
     * @param userId 用户ID
     * @return true表示有权限
     */
    boolean hasProjectViewPermission(Long projectId, Long userId);

    /**
     * 检查用户是否有项目编辑权限
     *
     * @param projectId 项目ID
     * @param userId 用户ID
     * @return true表示有权限
     */
    boolean hasProjectEditPermission(Long projectId, Long userId);

    /**
     * 检查用户是否有项目删除权限
     *
     * @param projectId 项目ID
     * @param userId 用户ID
     * @return true表示有权限
     */
    boolean hasProjectDeletePermission(Long projectId, Long userId);

    /**
     * 检查用户是否有项目成员管理权限
     *
     * @param projectId 项目ID
     * @param userId 用户ID
     * @return true表示有权限
     */
    boolean hasProjectMemberManagePermission(Long projectId, Long userId);

    /**
     * 检查用户是否为项目成员
     *
     * @param projectId 项目ID
     * @param userId 用户ID
     * @return true表示是成员
     */
    boolean isProjectMember(Long projectId, Long userId);

    /**
     * 分页查询项目成员
     *
     * @param projectId 项目ID
     * @param page 页码
     * @param pageSize 每页大小
     * @return 成员分页结果
     */
    PageResult<ProjectMember> listProjectMembers(Long projectId, Integer page, Integer pageSize);

    /**
     * 获取项目成员角色信息
     *
     * @param projectId 项目ID
     * @param userId 用户ID
     * @return 项目成员信息
     */
    ProjectMember getProjectMemberRole(Long projectId, Long userId);
}