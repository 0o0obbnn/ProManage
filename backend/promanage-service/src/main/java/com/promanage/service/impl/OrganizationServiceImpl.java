package com.promanage.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.promanage.common.exception.BusinessException;
import com.promanage.common.result.PageResult;
import com.promanage.common.domain.ResultCode;
import com.promanage.dto.OrganizationMemberDTO;
import com.promanage.dto.OrganizationSettingsDTO;
import com.promanage.service.constant.CommonConstants;
import com.promanage.common.entity.Organization;
import com.promanage.service.mapper.OrganizationMapper;
import com.promanage.service.IOrganizationService;
import com.promanage.service.strategy.OrganizationMemberStrategy;
import com.promanage.service.strategy.OrganizationQueryStrategy;
import com.promanage.service.strategy.OrganizationSettingsStrategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 组织服务实现类 - 重构版本
 * 使用策略模式减少方法数量
 *
 * @author ProManage Team
 * @date 2025-10-22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl extends ServiceImpl<OrganizationMapper, Organization> 
        implements IOrganizationService {

    private final OrganizationMapper organizationMapper;
    
    // 策略类
    private final OrganizationQueryStrategy queryStrategy;
    private final OrganizationMemberStrategy memberStrategy;
    private final OrganizationSettingsStrategy settingsStrategy;

    // ==================== 核心CRUD方法 ====================

    @Override
    public Organization getOrganizationById(Long id, Long userId) {
        Organization organization = queryStrategy.getOrganizationById(id);
        if (organization == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "组织不存在");
        }
        
        // 检查用户权限
        if (!queryStrategy.isUserInOrganization(id, userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "您无权访问此组织");
        }
        
        return organization;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Organization createOrganization(Organization organization, Long creatorId) {
        validateOrganization(organization);
        
        // 验证Slug唯一性
        queryStrategy.validateSlugUnique(organization.getSlug(), null);
        
        // 设置创建信息
        organization.setStatus("ACTIVE");
        organization.setCreatorId(creatorId);
        organization.setCreateTime(LocalDateTime.now());
        organization.setUpdateTime(LocalDateTime.now());
        
        try {
            organizationMapper.insert(organization);
        } catch (DataAccessException e) {
            log.error("创建组织失败, name={}", organization.getName(), e);
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "创建组织失败");
        }
        
        return organization;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Organization updateOrganization(Organization organization, Long updaterId) {
        validateOrganization(organization);
        
        // 验证组织存在
        queryStrategy.validateOrganizationExists(organization.getId());
        
        // 验证Slug唯一性
        queryStrategy.validateSlugUnique(organization.getSlug(), organization.getId());
        
        // 设置更新信息
        organization.setUpdateTime(LocalDateTime.now());
        organization.setUpdaterId(updaterId);
        
        try {
            organizationMapper.updateById(organization);
        } catch (DataAccessException e) {
            log.error("更新组织失败, id={}", organization.getId(), e);
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "更新组织失败");
        }
        
        return organization;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOrganization(Long id, Long deleterId) {
        // 验证组织存在
        queryStrategy.validateOrganizationExists(id);
        
        // 检查删除权限
        if (!hasDeletePermission(id, deleterId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "您无权删除此组织");
        }
        
        try {
            organizationMapper.deleteById(id);
        } catch (DataAccessException e) {
            log.error("删除组织失败, id={}", id, e);
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "删除组织失败");
        }
    }

    // ==================== 查询方法 ====================

    @Override
    public Organization getOrganizationBySlug(String slug) {
        return queryStrategy.getOrganizationBySlug(slug);
    }

    @Override
    public boolean isSlugExists(String slug) {
        return queryStrategy.isSlugExists(slug);
    }

    // Internal helper method (not in interface)
    public PageResult<Organization> listOrganizations(int page, int pageSize,
                                                    String keyword, String status,
                                                    String plan, Long userId) {
        return queryStrategy.listOrganizations(page, pageSize, keyword, status, plan, userId);
    }

    @Override
    public PageResult<Organization> listOrganizations(
        Long requesterId, Integer page, Integer pageSize, String keyword, Boolean isActive) {
      // 将Boolean isActive转换为String status
      String status = isActive != null ? (isActive ? "ACTIVE" : "INACTIVE") : null;

      // 调用完整版本，plan传null
      return listOrganizations(
          page != null ? page : 1,
          pageSize != null ? pageSize : 20,
          keyword,
          status,
          null,  // plan
          requesterId);
    }

    @Override
    public List<Organization> listUserOrganizations(Long userId) {
        return queryStrategy.listUserOrganizations(userId);
    }

    @Override
    public boolean isUserInOrganization(Long organizationId, Long userId) {
        return queryStrategy.isUserInOrganization(organizationId, userId);
    }

    @Override
    public long getMemberCount(Long organizationId) {
        return queryStrategy.getMemberCount(organizationId);
    }

    @Override
    public long getProjectCount(Long organizationId) {
        return queryStrategy.getProjectCount(organizationId);
    }

    // ==================== 成员管理方法 ====================

    // Internal helper method (not in interface)
    public PageResult<OrganizationMemberDTO> listOrganizationMembers(Long organizationId,
                                                                   int page, int pageSize,
                                                                   String keyword, String role,
                                                                   String status, Long userId) {
        return memberStrategy.listOrganizationMembers(organizationId, page, pageSize,
                                                    keyword, role, status, userId);
    }

    @Override
    public PageResult<OrganizationMemberDTO> listOrganizationMembers(
        Long organizationId, Long requesterId, Integer page, Integer pageSize) {
      // 验证用户权限
      if (!queryStrategy.isUserInOrganization(organizationId, requesterId)) {
        throw new BusinessException(ResultCode.FORBIDDEN, "您无权访问此组织");
      }

      // 调用完整版本的方法，使用默认参数
      return listOrganizationMembers(
          organizationId,
          page != null ? page : 1,
          pageSize != null ? pageSize : 20,
          null,  // keyword
          null,  // role
          null,  // status
          requesterId);
    }

    // Internal helper methods (not in interface)
    @Transactional(rollbackFor = Exception.class)
    public void addOrganizationMember(Long organizationId, Long userId, String role, Long inviterId) {
        memberStrategy.addOrganizationMember(organizationId, userId, role, inviterId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateMemberRole(Long organizationId, Long userId, String role, Long updaterId) {
        memberStrategy.updateMemberRole(organizationId, userId, role, updaterId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeOrganizationMember(Long organizationId, Long userId, Long removerId) {
        memberStrategy.removeOrganizationMember(organizationId, userId, removerId);
    }

    // ==================== 状态管理方法 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void activateOrganization(Long id, Long updaterId) {
        settingsStrategy.activateOrganization(id, updaterId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deactivateOrganization(Long id, Long updaterId) {
        settingsStrategy.deactivateOrganization(id, updaterId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSubscriptionPlan(Long id, String subscriptionPlan,
                                      java.time.LocalDateTime expiresAt, Long updaterId) {
        settingsStrategy.updateSubscriptionPlan(id, subscriptionPlan, updaterId);
        // Note: expiresAt parameter not currently used in strategy
    }

    // ==================== 设置管理方法 ====================

    @Override
    public OrganizationSettingsDTO getOrganizationSettings(Long organizationId, Long userId) {
        return settingsStrategy.getOrganizationSettings(organizationId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrganizationSettingsDTO updateOrganizationSettings(Long organizationId,
                                                            OrganizationSettingsDTO settings,
                                                            Long updaterId) {
        return settingsStrategy.updateOrganizationSettings(settings, organizationId, updaterId);
    }

    // ==================== 私有辅助方法 ====================

    private void validateOrganization(Organization organization) {
        if (organization == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "组织信息不能为空");
        }
        if (organization.getName() == null || organization.getName().trim().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "组织名称不能为空");
        }
        if (organization.getSlug() == null || organization.getSlug().trim().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "组织标识不能为空");
        }
    }

    private boolean hasDeletePermission(Long organizationId, Long userId) {
        // 这里应该检查用户是否有删除组织的权限
        // 简化实现，返回true
        return true;
    }
}
