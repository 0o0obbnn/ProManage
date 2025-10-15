package com.promanage.service;

import com.promanage.common.entity.Organization;
import com.promanage.common.result.PageResult;
import com.promanage.dto.OrganizationMemberDTO;
import com.promanage.dto.OrganizationSettingsDTO;

import java.util.List;

/**
 * 组织服务接口
 * <p>
 * 提供组织管理的核心业务功能
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-06
 */
public interface IOrganizationService {

    /**
     * 根据ID获取组织
     *
     * @param id 组织ID
     * @param userId 用户ID
     * @return 组织信息
     */
    Organization getOrganizationById(Long id, Long userId);

    /**
     * 创建组织
     *
     * @param organization 组织信息
     * @param creatorId    创建者ID
     * @return 创建的组织
     */
    Organization createOrganization(Organization organization, Long creatorId);

    /**
     * 更新组织信息
     *
     * @param organization 组织信息
     * @param updaterId    更新者ID
     * @return 更新后的组织
     */
    Organization updateOrganization(Organization organization, Long updaterId);

    /**
     * 删除组织（逻辑删除）
     *
     * @param id        组织ID
     * @param deleterId 删除者ID
     */
    void deleteOrganization(Long id, Long deleterId);

    /**
     * 根据标识符获取组织
     *
     * @param slug 组织标识符
     * @return 组织信息
     */
    Organization getOrganizationBySlug(String slug);

    /**
     * 检查组织标识符是否已存在
     *
     * @param slug 组织标识符
     * @return 是否已存在
     */
    boolean isSlugExists(String slug);

    /**
     * 分页查询组织列表
     *
     * @param page     页码
     * @param pageSize 每页数量
     * @param keyword  搜索关键词（组织名称或标识符）
     * @param isActive 是否激活状态
     * @return 组织分页结果
     */
    PageResult<Organization> listOrganizations(Long requesterId, Integer page, Integer pageSize, String keyword, Boolean isActive);

    /**
     * 获取用户所属的组织列表
     *
     * @param userId 用户ID
     * @return 组织列表
     */
    List<Organization> listUserOrganizations(Long userId);

    /**
     * 获取组织成员列表
     *
     * @param organizationId 组织ID
     * @param requesterId    请求用户ID
     * @return 成员列表
     */
    PageResult<OrganizationMemberDTO> listOrganizationMembers(Long organizationId, Long requesterId, Integer page, Integer pageSize);

    /**
     * 激活组织
     *
     * @param id       组织ID
     * @param updaterId 更新者ID
     */
    void activateOrganization(Long id, Long updaterId);

    /**
     * 停用组织
     *
     * @param id       组织ID
     * @param updaterId 更新者ID
     */
    void deactivateOrganization(Long id, Long updaterId);

    /**
     * 更新组织订阅计划
     *
     * @param id               组织ID
     * @param subscriptionPlan 订阅计划
     * @param expiresAt        过期时间
     * @param updaterId        更新者ID
     */
    void updateSubscriptionPlan(Long id, String subscriptionPlan, java.time.LocalDateTime expiresAt, Long updaterId);

    /**
     * 检查用户是否属于指定组织
     *
     * @param organizationId 组织ID
     * @param userId         用户ID
     * @return 是否属于该组织
     */
    boolean isUserInOrganization(Long organizationId, Long userId);

    /**
     * 获取组织的成员数量
     *
     * @param organizationId 组织ID
     * @return 成员数量
     */
    long getMemberCount(Long organizationId);

    /**
     * 获取组织的项目数量
     *
     * @param organizationId 组织ID
     * @return 项目数量
     */
    long getProjectCount(Long organizationId);

    /**
     * 获取组织设置
     *
     * @param organizationId 组织ID
     * @param userId 用户ID
     * @return 组织设置
     */
    OrganizationSettingsDTO getOrganizationSettings(Long organizationId, Long userId);

    /**
     * 更新组织设置
     *
     * @param organizationId 组织ID
     * @param settings       组织设置
     * @param updaterId      更新者ID
     * @return 更新后的设置
     */
    OrganizationSettingsDTO updateOrganizationSettings(Long organizationId, OrganizationSettingsDTO settings, Long updaterId);
}
