package com.promanage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.promanage.common.domain.PageResult;
import com.promanage.common.domain.ResultCode;
import com.promanage.common.exception.BusinessException;
import com.promanage.service.IOrganizationService;
import com.promanage.service.service.IUserService;
import com.promanage.service.entity.Organization;
import com.promanage.service.entity.Project;
import com.promanage.common.entity.User;
import com.promanage.service.mapper.OrganizationMapper;
import com.promanage.service.mapper.ProjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 组织服务实现类
 * <p>
 * 提供组织管理相关的业务逻辑实现
 * </p>
 *
 * @author ProManage Team
 * @since 2025-10-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl extends ServiceImpl<OrganizationMapper, Organization> implements IOrganizationService {

    private final OrganizationMapper organizationMapper;
    private final ProjectMapper projectMapper;
    private final IUserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Organization createOrganization(Organization organization, Long creatorId) {
        log.info("创建组织: {}, 创建者ID: {}", organization.getName(), creatorId);
        
        // 验证组织标识符是否已存在
        if (isSlugExists(organization.getSlug())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "组织标识符已存在");
        }
        
        // 设置创建者和时间信息
        organization.setCreatorId(creatorId);
        organization.setUpdaterId(creatorId);
        organization.setCreatedAt(LocalDateTime.now());
        organization.setUpdatedAt(LocalDateTime.now());
        organization.setIsActive(true);
        
        // 保存组织
        organizationMapper.insert(organization);
        
        log.info("组织创建成功, ID: {}", organization.getId());
        return organization;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Organization updateOrganization(Organization organization, Long updaterId) {
        log.info("更新组织: {}, 更新者ID: {}", organization.getId(), updaterId);
        
        // 检查组织是否存在
        Organization existingOrg = organizationMapper.selectById(organization.getId());
        if (existingOrg == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "组织不存在");
        }
        
        // 如果修改了slug，需要检查是否重复
        if (!existingOrg.getSlug().equals(organization.getSlug()) && 
            isSlugExists(organization.getSlug())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "组织标识符已存在");
        }
        
        // 设置更新信息
        organization.setUpdaterId(updaterId);
        organization.setUpdatedAt(LocalDateTime.now());
        
        // 更新组织
        organizationMapper.updateById(organization);
        
        log.info("组织更新成功, ID: {}", organization.getId());
        return organization;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOrganization(Long id, Long deleterId) {
        log.info("删除组织: {}, 删除者ID: {}", id, deleterId);
        
        // 检查组织是否存在
        Organization organization = organizationMapper.selectById(id);
        if (organization == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "组织不存在");
        }
        
        // 逻辑删除
        LambdaUpdateWrapper<Organization> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(Organization::getDeletedAt, LocalDateTime.now())
               .set(Organization::getDeletedBy, deleterId)
               .eq(Organization::getId, id);
        
        organizationMapper.update(null, wrapper);
        
        log.info("组织删除成功, ID: {}", id);
    }

    @Override
    public Organization getOrganizationBySlug(String slug) {
        log.debug("根据标识符获取组织: {}", slug);
        
        LambdaQueryWrapper<Organization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Organization::getSlug, slug)
               .isNull(Organization::getDeletedAt);
        
        return organizationMapper.selectOne(wrapper);
    }

    @Override
    public boolean isSlugExists(String slug) {
        LambdaQueryWrapper<Organization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Organization::getSlug, slug)
               .isNull(Organization::getDeletedAt);
        
        return organizationMapper.selectCount(wrapper) > 0;
    }

    @Override
    public PageResult<Organization> listOrganizations(Integer page, Integer pageSize, String keyword, Boolean isActive) {
        log.debug("分页查询组织列表, page: {}, pageSize: {}, keyword: {}, isActive: {}", 
                 page, pageSize, keyword, isActive);
        
        Page<Organization> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<Organization> wrapper = new LambdaQueryWrapper<>();
        
        // 条件查询
        wrapper.isNull(Organization::getDeletedAt);
        
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Organization::getName, keyword)
                            .or()
                            .like(Organization::getSlug, keyword));
        }
        
        if (isActive != null) {
            wrapper.eq(Organization::getIsActive, isActive);
        }
        
        // 按创建时间倒序排列
        wrapper.orderByDesc(Organization::getCreatedAt);
        
        IPage<Organization> result = organizationMapper.selectPage(pageParam, wrapper);
        
        return PageResult.of(
            result.getRecords(),
            result.getTotal(),
            (int) result.getCurrent(),
            (int) result.getSize()
        );
    }

    @Override
    public List<Organization> listUserOrganizations(Long userId) {
        log.debug("获取用户所属的组织列表, 用户ID: {}", userId);
        
        // 获取用户信息
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户不存在");
        }
        
        // 根据用户的organization_id字段获取组织
        LambdaQueryWrapper<Organization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Organization::getId, user.getOrganizationId())
               .isNull(Organization::getDeletedAt);
        
        return organizationMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void activateOrganization(Long id, Long updaterId) {
        log.info("激活组织: {}, 更新者ID: {}", id, updaterId);
        
        Organization organization = organizationMapper.selectById(id);
        if (organization == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "组织不存在");
        }
        
        LambdaUpdateWrapper<Organization> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(Organization::getIsActive, true)
               .set(Organization::getUpdatedAt, LocalDateTime.now())
               .set(Organization::getUpdaterId, updaterId)
               .eq(Organization::getId, id);
        
        organizationMapper.update(null, wrapper);
        
        log.info("组织激活成功, ID: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deactivateOrganization(Long id, Long updaterId) {
        log.info("停用组织: {}, 更新者ID: {}", id, updaterId);
        
        Organization organization = organizationMapper.selectById(id);
        if (organization == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "组织不存在");
        }
        
        LambdaUpdateWrapper<Organization> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(Organization::getIsActive, false)
               .set(Organization::getUpdatedAt, LocalDateTime.now())
               .set(Organization::getUpdaterId, updaterId)
               .eq(Organization::getId, id);
        
        organizationMapper.update(null, wrapper);
        
        log.info("组织停用成功, ID: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSubscriptionPlan(Long id, String subscriptionPlan, LocalDateTime expiresAt, Long updaterId) {
        log.info("更新组织订阅计划: {}, 计划: {}, 过期时间: {}, 更新者ID: {}", 
                id, subscriptionPlan, expiresAt, updaterId);
        
        Organization organization = organizationMapper.selectById(id);
        if (organization == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "组织不存在");
        }
        
        LambdaUpdateWrapper<Organization> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(Organization::getSubscriptionPlan, subscriptionPlan)
               .set(Organization::getSubscriptionExpiresAt, expiresAt)
               .set(Organization::getUpdatedAt, LocalDateTime.now())
               .set(Organization::getUpdaterId, updaterId)
               .eq(Organization::getId, id);
        
        organizationMapper.update(null, wrapper);
        
        log.info("组织订阅计划更新成功, ID: {}", id);
    }

    @Override
    public boolean isUserInOrganization(Long organizationId, Long userId) {
        log.debug("检查用户是否属于组织, 组织ID: {}, 用户ID: {}", organizationId, userId);
        
        User user = userService.getById(userId);
        if (user == null) {
            return false;
        }
        
        return organizationId.equals(user.getOrganizationId());
    }

    @Override
    public long getMemberCount(Long organizationId) {
        log.debug("获取组织成员数量, 组织ID: {}", organizationId);

        return userService.countByOrganizationId(organizationId);
    }

    @Override
    public long getProjectCount(Long organizationId) {
        log.debug("获取组织项目数量, 组织ID: {}", organizationId);
        
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Project::getOrganizationId, organizationId)
               .isNull(Project::getDeletedAt);
        
        return projectMapper.selectCount(wrapper);
    }
}