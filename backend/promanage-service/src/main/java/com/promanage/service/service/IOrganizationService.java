package com.promanage.service.service;

import com.promanage.common.domain.PageResult;
import com.promanage.common.entity.User;
import com.promanage.service.entity.Organization;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 组织服务接口
 * <p>
 * 定义组织管理相关的业务接口
 * </p>
 *
 * @author ProManage Team
 * @since 2025-10-07
 */
public interface IOrganizationService {

    /**
     * 创建组织
     *
     * @param organization 组织信息
     * @param creatorId    创建者ID
     * @return 创建的组织
     */
    Organization createOrganization(Organization organization, Long creatorId);

    /**
     * 更新组织
     *
     * @param organization 组织信息
     * @param updaterId    更新者ID
     * @return 更新后的组织
     */
    Organization updateOrganization(Organization organization, Long updaterId);

    /**
     * 删除组织（逻辑删除）
     *
     * @param id       组织ID
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
     * 检查组织标识符是否存在
     *
     * @param slug 组织标识符
     * @return true表示存在，false表示不存在
     */
    boolean isSlugExists(String slug);

    /**
     * 分页查询组织列表
     *
     * @param page     页码
     * @param pageSize 每页数量
     * @param keyword  搜索关键词
     * @param isActive 激活状态
     * @return 组织分页结果
     */
    PageResult<Organization> listOrganizations(Integer page, Integer pageSize, String keyword, Boolean isActive);

    /**
     * 获取用户所属的组织列表
     *
     * @param userId 用户ID
     * @return 组织列表
     */
    List<Organization> listUserOrganizations(Long userId);

    /**
     * 激活组织
     *
     * @param id        组织ID
     * @param updaterId 更新者ID
     */
    void activateOrganization(Long id, Long updaterId);

    /**
     * 停用组织
     *
     * @param id        组织ID
     * @param updaterId 更新者ID
     */
    void deactivateOrganization(Long id, Long updaterId);

    /**
     * 更新订阅计划
     *
     * @param id              组织ID
     * @param subscriptionPlan 订阅计划
     * @param expiresAt       过期时间
     * @param updaterId       更新者ID
     */
    void updateSubscriptionPlan(Long id, String subscriptionPlan, LocalDateTime expiresAt, Long updaterId);

    /**
     * 检查用户是否属于指定组织
     *
     * @param organizationId 组织ID
     * @param userId         用户ID
     * @return true表示属于，false表示不属于
     */
    boolean isUserInOrganization(Long organizationId, Long userId);

    /**
     * 获取组织成员数量
     *
     * @param organizationId 组织ID
     * @return 成员数量
     */
    long getMemberCount(Long organizationId);

    /**
     * 获取组织项目数量
     *
     * @param organizationId 组织ID
     * @return 项目数量
     */
    long getProjectCount(Long organizationId);
}