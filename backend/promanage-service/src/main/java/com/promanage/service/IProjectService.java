package com.promanage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.promanage.common.domain.PageResult;
import com.promanage.dto.ProjectRequest;
import com.promanage.dto.ProjectStatsDTO;
import com.promanage.service.entity.Project;
import com.promanage.dto.ProjectMemberDTO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author nifa
 * @since 2024-10-04
 */
public interface IProjectService extends IService<Project> {

    /**
     * 添加项目成员
     *
     * @param projectId 项目ID
     * @param userId    用户ID
     * @param roleId    角色ID
     */
    void addMember(Long projectId, Long userId, Long roleId);

    /**
     * 移除项目成员
     *
     * @param projectId 项目ID
     * @param userId    用户ID
     */
    void removeMember(Long projectId, Long userId);

    /**
     * 获取项目成员列表
     * @param projectId 项目ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param roleId 角色ID (可选)
     * @return 成员列表
     */
    PageResult<ProjectMemberDTO> listMembers(Long projectId, Integer pageNum, Integer pageSize, Long roleId);

    /**
     * 获取项目成员列表（按角色过滤）
     *
     * @param projectId 项目ID
     * @param userId    用户ID
     * @param roleId    角色ID
     * @return 成员列表
     */
    List<ProjectMemberDTO> listMembersByRole(Long projectId, Long userId, Long roleId);

    /**
     * 获取项目成员列表（按用户）
     *
     * @param userId    用户ID
     * @param page      页码
     * @param pageSize  每页数量
     * @param status    项目状态
     * @param keyword   搜索关键词 (可选, 对项目名称、编码、负责人进行模糊搜索)
     * @return 项目分页结果
     */
    PageResult<Project> listUserProjects(Long userId, Integer page, Integer pageSize, Integer status, String keyword);

    /**
     * 获取项目成员列表（按用户）
     *
     * @param userId    用户ID
     * @param page      页码
     * @param pageSize  每页数量
     * @param status    项目状态
     * @return 项目分页结果
     */
    PageResult<Project> listUserProjects(Long userId, Integer page, Integer pageSize, Integer status);

    /**
     * 获取项目统计数据
     * @param projectId 项目ID
     * @return 统计数据
     */
    ProjectStatsDTO getProjectStats(Long projectId);

    /**
     * 归档项目
     * @param projectId 项目ID
     */
    void archive(Long projectId);

    /**
     * 取消归档项目
     * @param projectId 项目ID
     */
    void unarchive(Long projectId);

    // --- 权限检查 ---

    /**
     * 检查用户是否为项目管理员
     *
     * @param projectId 项目ID
     * @param userId    用户ID
     * @return 是否为管理员
     */
    boolean isAdmin(Long projectId, Long userId);

    /**
     * 检查用户是否为项目成员或管理员
     *
     * @param projectId 项目ID
     * @param userId    用户ID
     * @return 是否为成员或管理员
     */
    boolean isMemberOrAdmin(Long projectId, Long userId);

    /**
     * 检查用户是否为项目成员
     *
     * @param projectId 项目ID
     * @param userId    用户ID
     * @return 是否为成员
     */
    boolean isMember(Long projectId, Long userId);

}