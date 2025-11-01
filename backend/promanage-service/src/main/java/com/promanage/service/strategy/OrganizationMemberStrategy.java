package com.promanage.service.strategy;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.promanage.common.exception.BusinessException;
import com.promanage.common.result.PageResult;
import com.promanage.common.domain.ResultCode;
import com.promanage.dto.OrganizationMemberDTO;
import com.promanage.common.entity.OrganizationMember;
import com.promanage.service.mapper.OrganizationMemberMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 组织成员管理策略
 *
 * @author ProManage Team
 * @date 2025-10-22
 */
@Slf4j
@RequiredArgsConstructor
public class OrganizationMemberStrategy {

    private final OrganizationMemberMapper organizationMemberMapper;

    /**
     * 分页查询组织成员
     */
    public PageResult<OrganizationMemberDTO> listOrganizationMembers(Long organizationId, 
                                                                   int page, int pageSize, 
                                                                   String keyword, String role, 
                                                                   String status, Long userId) {
        LambdaQueryWrapper<OrganizationMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrganizationMember::getOrganizationId, organizationId);
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            // 这里应该通过用户表关联查询
            // 简化实现
        }
        
        if (role != null && !role.trim().isEmpty()) {
            wrapper.eq(OrganizationMember::getRole, role);
        }
        
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(OrganizationMember::getStatus, status);
        }
        
        wrapper.orderByDesc(OrganizationMember::getJoinTime);
        
        IPage<OrganizationMember> pageResult = organizationMemberMapper.selectPage(
            new Page<>(page, pageSize), wrapper);
        
        // 转换为DTO
        List<OrganizationMemberDTO> memberDTOs = convertToDTOs(pageResult.getRecords());
        
        return PageResult.<OrganizationMemberDTO>builder()
            .list(memberDTOs)
            .total(pageResult.getTotal())
            .page(page)
            .pageSize(pageSize)
            .build();
    }

    /**
     * 添加组织成员
     */
    public void addOrganizationMember(Long organizationId, Long userId, String role, Long inviterId) {
        validateAddMember(organizationId, userId, role);
        
        OrganizationMember member = new OrganizationMember();
        member.setOrganizationId(organizationId);
        member.setUserId(userId);
        member.setRole(role);
        member.setStatus("ACTIVE");
        member.setInviterId(inviterId);
        member.setJoinTime(java.time.LocalDateTime.now());
        
        organizationMemberMapper.insert(member);
    }

    /**
     * 更新成员角色
     */
    public void updateMemberRole(Long organizationId, Long userId, String role, Long updaterId) {
        validateUpdateMember(organizationId, userId, role);
        
        LambdaQueryWrapper<OrganizationMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrganizationMember::getOrganizationId, organizationId);
        wrapper.eq(OrganizationMember::getUserId, userId);
        
        OrganizationMember member = organizationMemberMapper.selectOne(wrapper);
        if (member == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "成员不存在");
        }
        
        member.setRole(role);
        member.setUpdateTime(java.time.LocalDateTime.now());
        member.setUpdaterId(updaterId);
        
        organizationMemberMapper.updateById(member);
    }

    /**
     * 移除组织成员
     */
    public void removeOrganizationMember(Long organizationId, Long userId, Long removerId) {
        LambdaQueryWrapper<OrganizationMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrganizationMember::getOrganizationId, organizationId);
        wrapper.eq(OrganizationMember::getUserId, userId);
        
        OrganizationMember member = organizationMemberMapper.selectOne(wrapper);
        if (member == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "成员不存在");
        }
        
        organizationMemberMapper.delete(wrapper);
    }

    /**
     * 检查用户是否是组织成员
     */
    public boolean isUserInOrganization(Long organizationId, Long userId) {
        LambdaQueryWrapper<OrganizationMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrganizationMember::getOrganizationId, organizationId);
        wrapper.eq(OrganizationMember::getUserId, userId);
        wrapper.eq(OrganizationMember::getStatus, "ACTIVE");
        
        return organizationMemberMapper.selectCount(wrapper) > 0;
    }

    /**
     * 获取组织成员数量
     */
    public long getMemberCount(Long organizationId) {
        LambdaQueryWrapper<OrganizationMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrganizationMember::getOrganizationId, organizationId);
        wrapper.eq(OrganizationMember::getStatus, "ACTIVE");
        
        return organizationMemberMapper.selectCount(wrapper);
    }

    /**
     * 检查用户角色
     */
    public String getUserRole(Long organizationId, Long userId) {
        LambdaQueryWrapper<OrganizationMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrganizationMember::getOrganizationId, organizationId);
        wrapper.eq(OrganizationMember::getUserId, userId);
        wrapper.eq(OrganizationMember::getStatus, "ACTIVE");
        
        OrganizationMember member = organizationMemberMapper.selectOne(wrapper);
        return member != null ? member.getRole() : null;
    }

    /**
     * 验证添加成员
     */
    private void validateAddMember(Long organizationId, Long userId, String role) {
        if (organizationId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "组织ID不能为空");
        }
        if (userId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }
        if (role == null || role.trim().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "角色不能为空");
        }
        
        // 检查是否已经是成员
        if (isUserInOrganization(organizationId, userId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "用户已经是组织成员");
        }
    }

    /**
     * 验证更新成员
     */
    private void validateUpdateMember(Long organizationId, Long userId, String role) {
        if (organizationId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "组织ID不能为空");
        }
        if (userId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }
        if (role == null || role.trim().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "角色不能为空");
        }
    }

    /**
     * 转换为DTO
     */
    private List<OrganizationMemberDTO> convertToDTOs(List<OrganizationMember> members) {
        // 这里应该将OrganizationMember转换为OrganizationMemberDTO
        // 简化实现，返回空列表
        return java.util.Collections.emptyList();
    }
}
