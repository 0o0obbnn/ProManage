package com.promanage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.promanage.common.domain.ResultCode;
import com.promanage.common.entity.Organization;
import com.promanage.common.entity.User;
import com.promanage.common.exception.BusinessException;
import com.promanage.common.result.PageResult;
import com.promanage.dto.OrganizationMemberDTO;
import com.promanage.dto.OrganizationSettingsDTO;
import com.promanage.dto.mapper.UserMapper;
import com.promanage.service.IOrganizationService;
import com.promanage.service.entity.Project;
import com.promanage.service.mapper.OrganizationMapper;
import com.promanage.service.mapper.ProjectMapper;
import com.promanage.service.service.IPermissionService;
import com.promanage.service.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 组织服务实现类
 * <p>
 * 提供组织管理相关的业务逻辑实现
 * </p>
 *
 * @author ProManage Team
 * @since 2025-10-06
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrganizationServiceImpl extends ServiceImpl<OrganizationMapper, Organization> implements IOrganizationService {

    private static final String DEFAULT_SUBSCRIPTION_PLAN = "FREE";
    private static final boolean DEFAULT_ORGANIZATION_ACTIVE = true;
    private static final int DEFAULT_DIGEST_FREQUENCY_DAYS = 1;
    private static final int DEFAULT_PASSWORD_MIN_LENGTH = 8;
    private static final int DEFAULT_SESSION_TIMEOUT_MINUTES = 60;
    private static final boolean DEFAULT_REQUIRE_SPECIAL_CHAR = true;
    private static final boolean DEFAULT_TWO_FACTOR_ENABLED = false;
    private static final String DEFAULT_PROJECT_VISIBILITY = "PRIVATE";
    private static final boolean DEFAULT_ALLOW_PUBLIC_PROJECTS = true;
    private static final int DEFAULT_MAX_PROJECTS = 100;
    private static final int DEFAULT_MAX_MEMBERS = 50;
    private static final long DEFAULT_STORAGE_LIMIT_MB = 10_240L;

    private final OrganizationMapper organizationMapper;
    private final ProjectMapper projectMapper;
    private final IUserService userService;
    private final IPermissionService permissionService;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    private Organization loadActiveOrganizationOrThrow(Long id) {
        Organization organization = organizationMapper.selectById(id);
        if (organization == null || organization.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "组织不存在");
        }
        return organization;
    }

    private void assertOrganizationMember(Long userId, Long organizationId) {
        if (!permissionService.isOrganizationMember(userId, organizationId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "您不是该组织成员，无权访问");
        }
    }

    private void assertOrganizationAdmin(Long userId, Long organizationId) {
        if (!permissionService.isOrganizationAdmin(userId, organizationId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "您不是该组织管理员，无权操作");
        }
    }

    private void ensureSlugAvailable(String slug, Long ignoreId) {
        if (StringUtils.isBlank(slug)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "组织标识符不能为空");
        }
        LambdaQueryWrapper<Organization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Organization::getSlug, slug)
               .isNull(Organization::getDeletedAt);
        if (ignoreId != null) {
            wrapper.ne(Organization::getId, ignoreId);
        }
        if (organizationMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "组织标识符已存在");
        }
    }

    @Override
    public Organization getOrganizationById(Long id, Long userId) {
        assertOrganizationMember(userId, id);
        return loadActiveOrganizationOrThrow(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Organization createOrganization(Organization organization, Long creatorId) {
        log.info("创建组织: {}, 创建者ID: {}", organization.getName(), creatorId);

        ensureSlugAvailable(organization.getSlug(), null);

        LocalDateTime now = LocalDateTime.now();
        organization.setCreatedBy(creatorId);
        organization.setUpdatedBy(creatorId);
        organization.setCreatedAt(now);
        organization.setUpdatedAt(now);
        if (organization.getIsActive() == null) {
            organization.setIsActive(DEFAULT_ORGANIZATION_ACTIVE);
        }
        if (StringUtils.isBlank(organization.getSubscriptionPlan())) {
            organization.setSubscriptionPlan(DEFAULT_SUBSCRIPTION_PLAN);
        }
        organization.setDeletedAt(null);
        organization.setDeletedBy(null);

        organizationMapper.insert(organization);

        log.info("组织创建成功, ID: {}", organization.getId());
        return organization;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Organization updateOrganization(Organization organization, Long updaterId) {
        assertOrganizationAdmin(updaterId, organization.getId());
        log.info("更新组织: {}, 更新者ID: {}", organization.getId(), updaterId);

        Organization persisted = loadActiveOrganizationOrThrow(organization.getId());

        if (!Objects.equals(persisted.getSlug(), organization.getSlug())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "组织标识符不允许修改");
        }

        organization.setUpdatedBy(updaterId);
        organization.setUpdatedAt(LocalDateTime.now());
        organization.setVersion(persisted.getVersion());

        organizationMapper.updateById(organization);

        log.info("组织更新成功, ID: {}", organization.getId());
        return organization;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOrganization(Long id, Long deleterId) {
        log.info("删除组织: {}, 删除者ID: {}", id, deleterId);

        assertOrganizationAdmin(deleterId, id);
        Organization organization = loadActiveOrganizationOrThrow(id);

        organization.setDeletedAt(LocalDateTime.now());
        organization.setDeletedBy(deleterId);
        organizationMapper.updateById(organization);

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
    public PageResult<Organization> listOrganizations(Long requesterId, Integer page, Integer pageSize, String keyword, Boolean isActive) {
        log.debug("分页查询组织列表, requesterId: {}, page: {}, pageSize: {}, keyword: {}, isActive: {}",
                requesterId, page, pageSize, keyword, isActive);

        int currentPage = page == null || page < 1 ? 1 : page;
        int size = pageSize == null || pageSize < 1 ? 20 : pageSize;

        Page<Organization> pageParam = new Page<>(currentPage, size);
        LambdaQueryWrapper<Organization> wrapper = new LambdaQueryWrapper<>();

        wrapper.isNull(Organization::getDeletedAt);

        if (!permissionService.isSuperAdmin(requesterId)) {
            User requester = userService.getById(requesterId);
            if (requester == null || requester.getOrganizationId() == null) {
                throw new BusinessException(ResultCode.FORBIDDEN, "当前用户未关联组织，无法查询");
            }
            wrapper.eq(Organization::getId, requester.getOrganizationId());
        }

        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Organization::getName, keyword)
                    .or()
                    .like(Organization::getSlug, keyword));
        }

        if (isActive != null) {
            wrapper.eq(Organization::getIsActive, isActive);
        }

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
        
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户不存在");
        }
        
        LambdaQueryWrapper<Organization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Organization::getId, user.getOrganizationId())
               .isNull(Organization::getDeletedAt);
        
        return organizationMapper.selectList(wrapper);
    }

    @Override
    public PageResult<OrganizationMemberDTO> listOrganizationMembers(Long organizationId, Long requesterId, Integer page, Integer pageSize) {
        log.debug("获取组织成员列表, 组织ID: {}, 请求者ID: {}, page: {}, pageSize: {}", organizationId, requesterId, page, pageSize);

        assertOrganizationMember(requesterId, organizationId);
        loadActiveOrganizationOrThrow(organizationId);

        int currentPage = page == null || page < 1 ? 1 : page;
        int size = pageSize == null || pageSize < 1 ? 20 : pageSize;

        List<User> members = userService.listByOrganizationId(organizationId);
        if (members == null || members.isEmpty()) {
            return PageResult.of(Collections.emptyList(), 0L, currentPage, size);
        }

        List<OrganizationMemberDTO> dtoList = members.stream()
                .map(userMapper::toOrganizationMember)
                .collect(Collectors.toList());

        int total = dtoList.size();
        int fromIndex = Math.min((currentPage - 1) * size, total);
        int toIndex = Math.min(fromIndex + size, total);

        List<OrganizationMemberDTO> pageData = fromIndex >= toIndex ? Collections.emptyList() : dtoList.subList(fromIndex, toIndex);

        return PageResult.of(pageData, (long) total, currentPage, size);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void activateOrganization(Long id, Long updaterId) {
        assertOrganizationAdmin(updaterId, id);
        log.info("激活组织: {}, 更新者ID: {}", id, updaterId);

        Organization organization = loadActiveOrganizationOrThrow(id);

        organization.setIsActive(true);
        organization.setUpdatedAt(LocalDateTime.now());
        organization.setUpdatedBy(updaterId);
        organizationMapper.updateById(organization);

        log.info("组织激活成功, ID: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deactivateOrganization(Long id, Long updaterId) {
        assertOrganizationAdmin(updaterId, id);
        log.info("停用组织: {}, 更新者ID: {}", id, updaterId);

        Organization organization = loadActiveOrganizationOrThrow(id);

        organization.setIsActive(false);
        organization.setUpdatedAt(LocalDateTime.now());
        organization.setUpdatedBy(updaterId);
        organizationMapper.updateById(organization);

        log.info("组织停用成功, ID: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSubscriptionPlan(Long id, String subscriptionPlan, LocalDateTime expiresAt, Long updaterId) {
        // Subscription plan changes require admin permission
        if (!permissionService.isOrganizationAdmin(updaterId, id)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "您不是该组织管理员，无权修改订阅计划");
        }
        log.info("更新组织订阅计划: {}, 计划: {}, 过期时间: {}, 更新者ID: {}",
                id, subscriptionPlan, expiresAt, updaterId);

        Organization organization = loadActiveOrganizationOrThrow(id);

        organization.setSubscriptionPlan(subscriptionPlan);
        organization.setSubscriptionExpiresAt(expiresAt);
        organization.setUpdatedAt(LocalDateTime.now());
        organization.setUpdatedBy(updaterId);
        organizationMapper.updateById(organization);
        
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

    @Override
    public OrganizationSettingsDTO getOrganizationSettings(Long organizationId, Long userId) {
        assertOrganizationMember(userId, organizationId);
        log.debug("获取组织设置, 组织ID: {}", organizationId);

        Organization organization = loadActiveOrganizationOrThrow(organizationId);

        if (StringUtils.isBlank(organization.getSettings())) {
            return buildDefaultSettings();
        }

        try {
            OrganizationSettingsDTO parsed = objectMapper.readValue(organization.getSettings(), OrganizationSettingsDTO.class);
            return normalizeSettings(parsed);
        } catch (JsonProcessingException e) {
            log.error("解析组织设置失败, organizationId={}", organizationId, e);
            return buildDefaultSettings();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrganizationSettingsDTO updateOrganizationSettings(Long organizationId,
                                                              OrganizationSettingsDTO settings,
                                                              Long updaterId) {
        // Settings changes require admin permission
        assertOrganizationAdmin(updaterId, organizationId);
        log.info("更新组织设置, 组织ID: {}, 更新者ID: {}", organizationId, updaterId);

        Organization organization = loadActiveOrganizationOrThrow(organizationId);

        OrganizationSettingsDTO normalizedSettings = normalizeSettings(settings);
        validateSettings(normalizedSettings);

        String settingsJson;
        try {
            settingsJson = objectMapper.writeValueAsString(normalizedSettings);
        } catch (JsonProcessingException e) {
            log.error("序列化组织设置失败, organizationId={}", organizationId, e);
            throw new BusinessException(ResultCode.PARAM_ERROR, "设置格式错误");
        }

        organization.setSettings(settingsJson);
        organization.setUpdatedAt(LocalDateTime.now());
        organization.setUpdatedBy(updaterId);
        organizationMapper.updateById(organization);

        log.info("组织设置更新成功, ID: {}", organizationId);
        return normalizedSettings;
    }

    private OrganizationSettingsDTO buildDefaultSettings() {
        return OrganizationSettingsDTO.builder()
                .notification(OrganizationSettingsDTO.NotificationSettings.builder()
                        .emailEnabled(true)
                        .inAppEnabled(true)
                        .websocketEnabled(true)
                        .digestFrequencyDays(DEFAULT_DIGEST_FREQUENCY_DAYS)
                        .build())
                .security(OrganizationSettingsDTO.SecuritySettings.builder()
                        .passwordMinLength(DEFAULT_PASSWORD_MIN_LENGTH)
                        .passwordRequireSpecialChar(DEFAULT_REQUIRE_SPECIAL_CHAR)
                        .sessionTimeoutMinutes(DEFAULT_SESSION_TIMEOUT_MINUTES)
                        .twoFactorAuthEnabled(DEFAULT_TWO_FACTOR_ENABLED)
                        .ipWhitelist(new ArrayList<>())
                        .build())
                .project(OrganizationSettingsDTO.ProjectSettings.builder()
                        .defaultVisibility(DEFAULT_PROJECT_VISIBILITY)
                        .allowPublicProjects(DEFAULT_ALLOW_PUBLIC_PROJECTS)
                        .maxProjects(DEFAULT_MAX_PROJECTS)
                        .maxMembersPerProject(DEFAULT_MAX_MEMBERS)
                        .storageLimitMb(DEFAULT_STORAGE_LIMIT_MB)
                        .build())
                .custom(new HashMap<>())
                .build();
    }

    private OrganizationSettingsDTO normalizeSettings(OrganizationSettingsDTO settings) {
        if (settings == null) {
            return buildDefaultSettings();
        }

        OrganizationSettingsDTO.NotificationSettings notification = settings.getNotification();
        if (notification == null) {
            notification = OrganizationSettingsDTO.NotificationSettings.builder().build();
        }
        OrganizationSettingsDTO.SecuritySettings security = settings.getSecurity();
        if (security == null) {
            security = OrganizationSettingsDTO.SecuritySettings.builder().build();
        }
        OrganizationSettingsDTO.ProjectSettings project = settings.getProject();
        if (project == null) {
            project = OrganizationSettingsDTO.ProjectSettings.builder().build();
        }

        OrganizationSettingsDTO.NotificationSettings normalizedNotification = OrganizationSettingsDTO.NotificationSettings.builder()
                .emailEnabled(notification.getEmailEnabled() != null ? notification.getEmailEnabled() : Boolean.TRUE)
                .inAppEnabled(notification.getInAppEnabled() != null ? notification.getInAppEnabled() : Boolean.TRUE)
                .websocketEnabled(notification.getWebsocketEnabled() != null ? notification.getWebsocketEnabled() : Boolean.TRUE)
                .digestFrequencyDays(notification.getDigestFrequencyDays() != null ? notification.getDigestFrequencyDays() : DEFAULT_DIGEST_FREQUENCY_DAYS)
                .build();

        OrganizationSettingsDTO.SecuritySettings normalizedSecurity = OrganizationSettingsDTO.SecuritySettings.builder()
                .passwordMinLength(security.getPasswordMinLength() != null ? security.getPasswordMinLength() : DEFAULT_PASSWORD_MIN_LENGTH)
                .passwordRequireSpecialChar(security.getPasswordRequireSpecialChar() != null ? security.getPasswordRequireSpecialChar() : DEFAULT_REQUIRE_SPECIAL_CHAR)
                .sessionTimeoutMinutes(security.getSessionTimeoutMinutes() != null ? security.getSessionTimeoutMinutes() : DEFAULT_SESSION_TIMEOUT_MINUTES)
                .twoFactorAuthEnabled(security.getTwoFactorAuthEnabled() != null ? security.getTwoFactorAuthEnabled() : DEFAULT_TWO_FACTOR_ENABLED)
                .ipWhitelist(security.getIpWhitelist() != null ? new ArrayList<>(security.getIpWhitelist()) : new ArrayList<>())
                .build();

        OrganizationSettingsDTO.ProjectSettings normalizedProject = OrganizationSettingsDTO.ProjectSettings.builder()
                .defaultVisibility(StringUtils.isNotBlank(project.getDefaultVisibility()) ? project.getDefaultVisibility() : DEFAULT_PROJECT_VISIBILITY)
                .allowPublicProjects(project.getAllowPublicProjects() != null ? project.getAllowPublicProjects() : DEFAULT_ALLOW_PUBLIC_PROJECTS)
                .maxProjects(project.getMaxProjects() != null ? project.getMaxProjects() : DEFAULT_MAX_PROJECTS)
                .maxMembersPerProject(project.getMaxMembersPerProject() != null ? project.getMaxMembersPerProject() : DEFAULT_MAX_MEMBERS)
                .storageLimitMb(project.getStorageLimitMb() != null ? project.getStorageLimitMb() : DEFAULT_STORAGE_LIMIT_MB)
                .build();

        HashMap<String, Object> custom = settings.getCustom() != null ? new HashMap<>(settings.getCustom()) : new HashMap<>();

        return OrganizationSettingsDTO.builder()
                .notification(normalizedNotification)
                .security(normalizedSecurity)
                .project(normalizedProject)
                .custom(custom)
                .build();
    }

    private void validateSettings(OrganizationSettingsDTO settings) {
        Set<ConstraintViolation<OrganizationSettingsDTO>> violations = validator.validate(settings);
        if (!violations.isEmpty()) {
            String message = violations.iterator().next().getMessage();
            throw new BusinessException(ResultCode.PARAM_ERROR, message);
        }
    }
}
