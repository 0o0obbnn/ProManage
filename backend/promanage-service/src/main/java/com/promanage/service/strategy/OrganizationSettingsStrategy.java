package com.promanage.service.strategy;

import java.time.LocalDateTime;

import com.promanage.common.exception.BusinessException;
import com.promanage.common.domain.ResultCode;
import com.promanage.dto.OrganizationSettingsDTO;
import com.promanage.common.entity.Organization;
import com.promanage.common.entity.OrganizationSettings;
import com.promanage.service.mapper.OrganizationMapper;
import com.promanage.service.mapper.OrganizationSettingsMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 组织设置管理策略
 *
 * @author ProManage Team
 * @date 2025-10-22
 */
@Slf4j
@RequiredArgsConstructor
public class OrganizationSettingsStrategy {

    private final OrganizationSettingsMapper organizationSettingsMapper;
    private final OrganizationMapper organizationMapper;

    /**
     * 获取组织设置
     */
    public OrganizationSettingsDTO getOrganizationSettings(Long organizationId, Long userId) {
        // 验证组织存在
        Organization organization = organizationMapper.selectById(organizationId);
        if (organization == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "组织不存在");
        }
        
        // 验证用户权限
        if (!hasSettingsPermission(organizationId, userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "您无权查看组织设置");
        }
        
        // 获取设置
        OrganizationSettings settings = getSettingsByOrganizationId(organizationId);
        if (settings == null) {
            // 创建默认设置
            settings = createDefaultSettings(organizationId);
        }
        
        return convertToDTO(settings);
    }

    /**
     * 更新组织设置
     */
    public OrganizationSettingsDTO updateOrganizationSettings(OrganizationSettingsDTO settingsDTO, 
                                                            Long organizationId, Long userId) {
        // 验证组织存在
        Organization organization = organizationMapper.selectById(organizationId);
        if (organization == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "组织不存在");
        }
        
        // 验证用户权限
        if (!hasSettingsPermission(organizationId, userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "您无权修改组织设置");
        }
        
        // 验证设置
        validateSettings(settingsDTO);
        
        // 获取或创建设置
        OrganizationSettings settings = getSettingsByOrganizationId(organizationId);
        if (settings == null) {
            settings = new OrganizationSettings();
            settings.setOrganizationId(organizationId);
            settings.setCreateTime(LocalDateTime.now());
        }
        
        // 更新设置
        updateSettingsFromDTO(settings, settingsDTO);
        settings.setUpdateTime(LocalDateTime.now());
        settings.setUpdaterId(userId);
        
        if (settings.getId() == null) {
            organizationSettingsMapper.insert(settings);
        } else {
            organizationSettingsMapper.updateById(settings);
        }
        
        return convertToDTO(settings);
    }

    /**
     * 更新订阅计划
     */
    public void updateSubscriptionPlan(Long organizationId, String plan, Long updaterId) {
        // 验证组织存在
        Organization organization = organizationMapper.selectById(organizationId);
        if (organization == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "组织不存在");
        }
        
        // 验证用户权限
        if (!hasAdminPermission(organizationId, updaterId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "您无权修改订阅计划");
        }
        
        // 验证计划
        if (plan == null || plan.trim().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "订阅计划不能为空");
        }
        
        // 更新组织订阅计划
        organization.setSubscriptionPlan(plan);
        organization.setUpdateTime(LocalDateTime.now());
        organization.setUpdaterId(updaterId);
        
        organizationMapper.updateById(organization);
    }

    /**
     * 激活组织
     */
    public void activateOrganization(Long organizationId, Long updaterId) {
        updateOrganizationStatus(organizationId, "ACTIVE", updaterId);
    }

    /**
     * 停用组织
     */
    public void deactivateOrganization(Long organizationId, Long updaterId) {
        updateOrganizationStatus(organizationId, "INACTIVE", updaterId);
    }

    /**
     * 根据组织ID获取设置
     */
    private OrganizationSettings getSettingsByOrganizationId(Long organizationId) {
        // 这里应该通过OrganizationSettingsMapper查询
        // 简化实现，返回null
        return null;
    }

    /**
     * 创建默认设置
     */
    private OrganizationSettings createDefaultSettings(Long organizationId) {
        OrganizationSettings settings = new OrganizationSettings();
        settings.setOrganizationId(organizationId);
        settings.setAllowPublicProjects(false);
        settings.setMaxProjects(10);
        settings.setMaxMembers(50);
        settings.setCreateTime(LocalDateTime.now());
        return settings;
    }

    /**
     * 转换为DTO
     */
    private OrganizationSettingsDTO convertToDTO(OrganizationSettings settings) {
        OrganizationSettingsDTO dto = new OrganizationSettingsDTO();
        dto.setOrganizationId(settings.getOrganizationId());
        dto.setAllowPublicProjects(settings.getAllowPublicProjects());
        dto.setMaxProjects(settings.getMaxProjects());
        dto.setMaxMembers(settings.getMaxMembers());
        return dto;
    }

    /**
     * 从DTO更新设置
     */
    private void updateSettingsFromDTO(OrganizationSettings settings, OrganizationSettingsDTO dto) {
        if (dto.getAllowPublicProjects() != null) {
            settings.setAllowPublicProjects(dto.getAllowPublicProjects());
        }
        if (dto.getMaxProjects() != null) {
            settings.setMaxProjects(dto.getMaxProjects());
        }
        if (dto.getMaxMembers() != null) {
            settings.setMaxMembers(dto.getMaxMembers());
        }
    }

    /**
     * 验证设置
     */
    private void validateSettings(OrganizationSettingsDTO settingsDTO) {
        if (settingsDTO == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "设置信息不能为空");
        }
        if (settingsDTO.getMaxProjects() != null && settingsDTO.getMaxProjects() < 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "最大项目数不能为负数");
        }
        if (settingsDTO.getMaxMembers() != null && settingsDTO.getMaxMembers() < 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "最大成员数不能为负数");
        }
    }

    /**
     * 检查设置权限
     */
    private boolean hasSettingsPermission(Long organizationId, Long userId) {
        // 这里应该检查用户是否有组织设置权限
        // 简化实现，返回true
        return true;
    }

    /**
     * 检查管理员权限
     */
    private boolean hasAdminPermission(Long organizationId, Long userId) {
        // 这里应该检查用户是否是组织管理员
        // 简化实现，返回true
        return true;
    }

    /**
     * 更新组织状态
     */
    private void updateOrganizationStatus(Long organizationId, String status, Long updaterId) {
        Organization organization = organizationMapper.selectById(organizationId);
        if (organization == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "组织不存在");
        }
        
        organization.setStatus(status);
        organization.setUpdateTime(LocalDateTime.now());
        organization.setUpdaterId(updaterId);
        
        organizationMapper.updateById(organization);
    }
}
