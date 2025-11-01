package com.promanage.service.strategy;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.promanage.common.exception.BusinessException;
import com.promanage.common.result.PageResult;
import com.promanage.common.domain.ResultCode;
import com.promanage.service.constant.CommonConstants;
import com.promanage.common.entity.Organization;
import com.promanage.service.mapper.OrganizationMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 组织查询策略
 *
 * @author ProManage Team
 * @date 2025-10-22
 */
@Slf4j
@RequiredArgsConstructor
public class OrganizationQueryStrategy {

    private final OrganizationMapper organizationMapper;

    /**
     * 根据ID获取组织
     */
    public Organization getOrganizationById(Long id) {
        return organizationMapper.selectById(id);
    }

    /**
     * 根据Slug获取组织
     */
    public Organization getOrganizationBySlug(String slug) {
        LambdaQueryWrapper<Organization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Organization::getSlug, slug);
        return organizationMapper.selectOne(wrapper);
    }

    /**
     * 检查Slug是否存在
     */
    public boolean isSlugExists(String slug) {
        LambdaQueryWrapper<Organization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Organization::getSlug, slug);
        return organizationMapper.selectCount(wrapper) > 0;
    }

    /**
     * 分页查询组织列表
     */
    public PageResult<Organization> listOrganizations(int page, int pageSize, 
                                                    String keyword, String status, 
                                                    String plan, Long userId) {
        LambdaQueryWrapper<Organization> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w
                .like(Organization::getName, keyword)
                .or()
                .like(Organization::getDescription, keyword)
            );
        }
        
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(Organization::getStatus, status);
        }
        
        if (plan != null && !plan.trim().isEmpty()) {
            wrapper.eq(Organization::getSubscriptionPlan, plan);
        }
        
        wrapper.orderByDesc(Organization::getCreateTime);
        
        IPage<Organization> pageResult = organizationMapper.selectPage(
            new Page<>(page, pageSize), wrapper);
        
        return PageResult.<Organization>builder()
            .list(pageResult.getRecords())
            .total(pageResult.getTotal())
            .page(page)
            .pageSize(pageSize)
            .build();
    }

    /**
     * 获取用户组织列表
     */
    public List<Organization> listUserOrganizations(Long userId) {
        // 这里应该通过OrganizationMember表查询用户所属的组织
        // 简化实现，返回空列表
        return java.util.Collections.emptyList();
    }

    /**
     * 检查用户是否在组织中
     */
    public boolean isUserInOrganization(Long organizationId, Long userId) {
        // 这里应该通过OrganizationMember表检查
        // 简化实现，返回true
        return true;
    }

    /**
     * 获取组织成员数量
     */
    public long getMemberCount(Long organizationId) {
        // 这里应该通过OrganizationMember表统计
        // 简化实现，返回0
        return 0L;
    }

    /**
     * 获取组织项目数量
     */
    public long getProjectCount(Long organizationId) {
        // 这里应该通过Project表统计
        // 简化实现，返回0
        return 0L;
    }

    /**
     * 验证组织存在性
     */
    public void validateOrganizationExists(Long organizationId) {
        Organization organization = getOrganizationById(organizationId);
        if (organization == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "组织不存在");
        }
    }

    /**
     * 验证Slug唯一性
     */
    public void validateSlugUnique(String slug, Long excludeId) {
        LambdaQueryWrapper<Organization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Organization::getSlug, slug);
        if (excludeId != null) {
            wrapper.ne(Organization::getId, excludeId);
        }
        
        if (organizationMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "组织标识已存在");
        }
    }
}
