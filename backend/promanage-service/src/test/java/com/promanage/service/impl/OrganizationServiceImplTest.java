package com.promanage.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.promanage.common.entity.Organization;
import com.promanage.common.entity.User;
import com.promanage.common.exception.BusinessException;
import com.promanage.common.result.PageResult;
import com.promanage.dto.OrganizationMemberDTO;
import com.promanage.dto.OrganizationSettingsDTO;
import com.promanage.dto.mapper.UserMapper;
import com.promanage.service.mapper.OrganizationMapper;
import com.promanage.service.mapper.ProjectMapper;
import com.promanage.service.service.IPermissionService;
import com.promanage.service.service.IUserService;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

/**
 * OrganizationServiceImpl 单元测试
 *
 * <p>基于 ProManage 系统架构全面测试组织服务功能
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-11
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("OrganizationServiceImpl 单元测试")
class OrganizationServiceImplTest {

  @Mock private OrganizationMapper organizationMapper;

  @Mock private ProjectMapper projectMapper;

  @Mock private IUserService userService;

  @Mock private IPermissionService permissionService;

  @Mock private UserMapper userMapper;

  @Spy private ObjectMapper objectMapper = new ObjectMapper();

  @Spy private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @InjectMocks private OrganizationServiceImpl organizationService;

  private Organization testOrganization;
  private User testUser;

  @BeforeEach
  void setUp() {
    // 初始化测试组织
    testOrganization = new Organization();
    testOrganization.setId(1L);
    testOrganization.setName("测试组织");
    testOrganization.setSlug("test-org");
    testOrganization.setDescription("测试组织描述");
    testOrganization.setIsActive(true);
    testOrganization.setSubscriptionPlan("FREE");
    testOrganization.setCreatedAt(LocalDateTime.now());
    testOrganization.setUpdatedAt(LocalDateTime.now());
    testOrganization.setCreatedBy(1L);
    testOrganization.setUpdatedBy(1L);

    // 初始化测试用户
    testUser = new User();
    testUser.setId(1L);
    testUser.setUsername("testuser");
    testUser.setEmail("test@example.com");
    testUser.setOrganizationId(1L);

    when(userService.getById(anyLong())).thenReturn(testUser);
    when(permissionService.isOrganizationMember(anyLong(), anyLong())).thenReturn(true);
    when(permissionService.isOrganizationAdmin(anyLong(), anyLong())).thenReturn(true);
    when(permissionService.isSuperAdmin(anyLong())).thenReturn(false);
    when(userMapper.toOrganizationMember(any(User.class)))
        .thenAnswer(
            invocation -> {
              User user = invocation.getArgument(0);
              return OrganizationMemberDTO.builder()
                  .id(user.getId())
                  .username(user.getUsername())
                  .email(user.getEmail())
                  .realName(user.getRealName())
                  .position(user.getPosition())
                  .status(user.getStatus())
                  .lastLoginTime(user.getLastLoginTime())
                  .build();
            });
  }

  // ==================== 组织CRUD功能测试 ====================

  @Test
  @DisplayName("创建组织 - 成功")
  void shouldCreateOrganization_whenOrganizationIsValid() {
    // given
    Organization newOrg = new Organization();
    newOrg.setName("新组织");
    newOrg.setSlug("new-org");

    when(organizationMapper.selectCount(any())).thenReturn(0L);
    when(organizationMapper.insert(any(Organization.class)))
        .thenAnswer(
            invocation -> {
              Organization org = invocation.getArgument(0);
              org.setId(2L);
              return 1;
            });

    // when
    Organization result = organizationService.createOrganization(newOrg, 1L);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(2L);
    assertThat(result.getCreatedBy()).isEqualTo(1L);
    assertThat(result.getUpdatedBy()).isEqualTo(1L);
    assertThat(result.getIsActive()).isTrue();
    verify(organizationMapper).insert(any(Organization.class));
  }

  @Test
  @DisplayName("创建组织 - slug已存在")
  void shouldThrowException_whenSlugAlreadyExists() {
    // given
    Organization newOrg = new Organization();
    newOrg.setName("新组织");
    newOrg.setSlug("existing-slug");

    when(organizationMapper.selectCount(any())).thenReturn(1L);

    // when & then
    assertThatThrownBy(() -> organizationService.createOrganization(newOrg, 1L))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("组织标识符已存在");

    verify(organizationMapper, never()).insert(any(Organization.class));
  }

  @Test
  @DisplayName("更新组织 - 成功")
  void shouldUpdateOrganization_whenOrganizationExists() {
    // given
    Organization updateOrg = new Organization();
    updateOrg.setId(1L);
    updateOrg.setName("更新后的名称");
    updateOrg.setSlug("test-org"); // 保持slug不变

    when(organizationMapper.selectById(1L)).thenReturn(testOrganization);
    when(organizationMapper.updateById(any(Organization.class))).thenReturn(1);

    // when
    Organization result = organizationService.updateOrganization(updateOrg, 1L);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getUpdatedBy()).isEqualTo(1L);
    verify(organizationMapper).updateById(any(Organization.class));
  }

  @Test
  @DisplayName("更新组织 - 组织不存在")
  void shouldThrowException_whenOrganizationNotExists() {
    // given
    Organization updateOrg = new Organization();
    updateOrg.setId(999L);
    updateOrg.setName("更新后的名称");
    updateOrg.setSlug("test-org");

    when(organizationMapper.selectById(999L)).thenReturn(null);

    // when & then
    assertThatThrownBy(() -> organizationService.updateOrganization(updateOrg, 1L))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("组织不存在");

    verify(organizationMapper, never()).updateById(any(Organization.class));
  }

  @Test
  @DisplayName("更新组织 - slug重复")
  void shouldThrowException_whenUpdatedSlugAlreadyExists() {
    // given
    Organization updateOrg = new Organization();
    updateOrg.setId(1L);
    updateOrg.setName("更新后的名称");
    updateOrg.setSlug("another-slug"); // 修改slug

    when(organizationMapper.selectById(1L)).thenReturn(testOrganization);

    // when & then
    assertThatThrownBy(() -> organizationService.updateOrganization(updateOrg, 1L))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("组织标识符不允许修改");

    verify(organizationMapper, never()).updateById(any(Organization.class));
  }

  @Test
  @DisplayName("更新组织 - 非管理员无权限")
  void shouldThrowForbidden_whenUpdaterNotAdmin() {
    // given
    Organization updateOrg = new Organization();
    updateOrg.setId(1L);
    updateOrg.setSlug("test-org");

    when(permissionService.isOrganizationAdmin(2L, 1L)).thenReturn(false);

    // when & then
    assertThatThrownBy(() -> organizationService.updateOrganization(updateOrg, 2L))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("无权操作");
  }

  @Test
  @DisplayName("删除组织 - 成功")
  void shouldDeleteOrganization_whenOrganizationExists() {
    // given
    when(organizationMapper.selectById(1L)).thenReturn(testOrganization);
    when(organizationMapper.updateById(any(Organization.class))).thenReturn(1);

    // when
    organizationService.deleteOrganization(1L, 1L);

    // then
    verify(organizationMapper).selectById(1L);
    verify(organizationMapper).updateById(any(Organization.class));
  }

  @Test
  @DisplayName("删除组织 - 非管理员无权限")
  void shouldThrowForbidden_whenDeletingWithoutAdminPermission() {
    // given
    when(permissionService.isOrganizationAdmin(2L, 1L)).thenReturn(false);

    // when & then
    assertThatThrownBy(() -> organizationService.deleteOrganization(1L, 2L))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("无权操作");
  }

  @Test
  @DisplayName("删除组织 - 组织不存在")
  void shouldThrowException_whenDeletingNonExistentOrganization() {
    // given
    when(organizationMapper.selectById(999L)).thenReturn(null);

    // when & then
    assertThatThrownBy(() -> organizationService.deleteOrganization(999L, 1L))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("组织不存在");
  }

  // ==================== 组织查询功能测试 ====================

  @Test
  @DisplayName("根据slug获取组织 - 成功")
  void shouldGetOrganizationBySlug_whenOrganizationExists() {
    // given
    when(organizationMapper.selectOne(any())).thenReturn(testOrganization);

    // when
    Organization result = organizationService.getOrganizationBySlug("test-org");

    // then
    assertThat(result).isNotNull();
    assertThat(result.getSlug()).isEqualTo("test-org");
    verify(organizationMapper).selectOne(any());
  }

  @Test
  @DisplayName("检查slug是否存在 - 存在")
  void shouldReturnTrue_whenSlugExists() {
    // given
    when(organizationMapper.selectCount(any())).thenReturn(1L);

    // when
    boolean result = organizationService.isSlugExists("test-org");

    // then
    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("检查slug是否存在 - 不存在")
  void shouldReturnFalse_whenSlugNotExists() {
    // given
    when(organizationMapper.selectCount(any())).thenReturn(0L);

    // when
    boolean result = organizationService.isSlugExists("nonexistent");

    // then
    assertThat(result).isFalse();
  }

  @Test
  @DisplayName("获取组织详情 - 非成员无权限")
  void shouldThrowForbidden_whenUserNotMemberOfOrganization() {
    // given
    when(permissionService.isOrganizationMember(2L, 1L)).thenReturn(false);

    // when & then
    assertThatThrownBy(() -> organizationService.getOrganizationById(1L, 2L))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("无权访问");
  }

  @Test
  @DisplayName("分页查询组织列表 - 成功")
  void shouldListOrganizations_withPagination() {
    // given
    Page<Organization> pageResult = new Page<>(1, 20);
    pageResult.setRecords(Arrays.asList(testOrganization));
    pageResult.setTotal(1L);

    when(organizationMapper.selectPage(any(), any())).thenReturn(pageResult);

    // when
    PageResult<Organization> result = organizationService.listOrganizations(1L, 1, 20, null, null);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getList()).hasSize(1);
    assertThat(result.getTotal()).isEqualTo(1L);
    assertThat(result.getPage()).isEqualTo(1);
    verify(organizationMapper).selectPage(any(), any());
  }

  @Test
  @DisplayName("分页查询组织列表 - 带关键词搜索")
  void shouldListOrganizations_withKeywordFilter() {
    // given
    Page<Organization> pageResult = new Page<>(1, 20);
    pageResult.setRecords(Arrays.asList(testOrganization));
    pageResult.setTotal(1L);

    when(organizationMapper.selectPage(any(), any())).thenReturn(pageResult);

    // when
    PageResult<Organization> result = organizationService.listOrganizations(1L, 1, 20, "测试", null);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getList()).hasSize(1);
    verify(organizationMapper).selectPage(any(), any());
  }

  @Test
  @DisplayName("分页查询组织列表 - 按激活状态过滤")
  void shouldListOrganizations_withActiveStatusFilter() {
    // given
    Page<Organization> pageResult = new Page<>(1, 20);
    pageResult.setRecords(Arrays.asList(testOrganization));
    pageResult.setTotal(1L);

    when(organizationMapper.selectPage(any(), any())).thenReturn(pageResult);

    // when
    PageResult<Organization> result = organizationService.listOrganizations(1L, 1, 20, null, true);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getList()).hasSize(1);
    verify(organizationMapper).selectPage(any(), any());
  }

  @Test
  @DisplayName("获取组织成员列表 - 成功")
  void shouldListOrganizationMembers_withPagination() {
    // given
    User member = new User();
    member.setId(10L);
    member.setUsername("member");
    member.setEmail("member@example.com");
    member.setOrganizationId(1L);

    when(organizationMapper.selectById(1L)).thenReturn(testOrganization);
    
    // Mock the paginated method that is actually called
    PageResult<User> userPageResult = PageResult.of(Arrays.asList(member), 1L, 1, 10);
    when(userService.listByOrganizationId(1L, 1, 10)).thenReturn(userPageResult);

    // when
    PageResult<OrganizationMemberDTO> result =
        organizationService.listOrganizationMembers(1L, 1L, 1, 10);

    // then
    assertThat(result.getList()).hasSize(1);
    OrganizationMemberDTO dto = result.getList().get(0);
    assertThat(dto.getId()).isEqualTo(member.getId());
    assertThat(dto.getUsername()).isEqualTo(member.getUsername());
    assertThat(result.getTotal()).isEqualTo(1L);
  }

  // ==================== 组织状态管理测试 ====================

  @Test
  @DisplayName("激活组织 - 成功")
  void shouldActivateOrganization_whenOrganizationExists() {
    // given
    when(organizationMapper.selectById(1L)).thenReturn(testOrganization);
    when(organizationMapper.updateById(any(Organization.class))).thenReturn(1);

    // when
    organizationService.activateOrganization(1L, 1L);

    // then
    verify(organizationMapper).selectById(1L);
    verify(organizationMapper).updateById(any(Organization.class));
  }

  @Test
  @DisplayName("激活组织 - 组织不存在")
  void shouldThrowException_whenActivatingNonExistentOrganization() {
    // given
    when(organizationMapper.selectById(999L)).thenReturn(null);

    // when & then
    assertThatThrownBy(() -> organizationService.activateOrganization(999L, 1L))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("组织不存在");
  }

  @Test
  @DisplayName("停用组织 - 成功")
  void shouldDeactivateOrganization_whenOrganizationExists() {
    // given
    when(organizationMapper.selectById(1L)).thenReturn(testOrganization);
    when(organizationMapper.updateById(any(Organization.class))).thenReturn(1);

    // when
    organizationService.deactivateOrganization(1L, 1L);

    // then
    verify(organizationMapper).selectById(1L);
    verify(organizationMapper).updateById(any(Organization.class));
  }

  @Test
  @DisplayName("停用组织 - 组织不存在")
  void shouldThrowException_whenDeactivatingNonExistentOrganization() {
    // given
    when(organizationMapper.selectById(999L)).thenReturn(null);

    // when & then
    assertThatThrownBy(() -> organizationService.deactivateOrganization(999L, 1L))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("组织不存在");
  }

  // ==================== 订阅计划管理测试 ====================

  @Test
  @DisplayName("更新订阅计划 - 成功")
  void shouldUpdateSubscriptionPlan_whenOrganizationExists() {
    // given
    LocalDateTime expiresAt = LocalDateTime.now().plusDays(30);
    when(organizationMapper.selectById(1L)).thenReturn(testOrganization);
    when(organizationMapper.updateById(any(Organization.class))).thenReturn(1);

    // when
    organizationService.updateSubscriptionPlan(1L, "PRO", expiresAt, 1L);

    // then
    verify(organizationMapper).selectById(1L);
    verify(organizationMapper).updateById(any(Organization.class));
  }

  @Test
  @DisplayName("更新订阅计划 - 组织不存在")
  void shouldThrowException_whenUpdatingSubscriptionForNonExistentOrganization() {
    // given
    LocalDateTime expiresAt = LocalDateTime.now().plusDays(30);
    when(organizationMapper.selectById(999L)).thenReturn(null);

    // when & then
    assertThatThrownBy(() -> organizationService.updateSubscriptionPlan(999L, "PRO", expiresAt, 1L))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("组织不存在");
  }

  // ==================== 用户组织关系测试 ====================

  @Test
  @DisplayName("获取用户所属组织列表 - 成功")
  void shouldListUserOrganizations_whenUserExists() {
    // given
    when(userService.getById(1L)).thenReturn(testUser);
    when(organizationMapper.selectList(any())).thenReturn(Arrays.asList(testOrganization));

    // when
    List<Organization> result = organizationService.listUserOrganizations(1L);

    // then
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);
    verify(userService).getById(1L);
  }

  @Test
  @DisplayName("获取用户所属组织列表 - 用户不存在")
  void shouldThrowException_whenUserNotExistsForListUserOrganizations() {
    // given
    when(userService.getById(999L)).thenReturn(null);

    // when & then
    assertThatThrownBy(() -> organizationService.listUserOrganizations(999L))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("用户不存在");
  }

  @Test
  @DisplayName("检查用户是否属于组织 - 属于")
  void shouldReturnTrue_whenUserBelongsToOrganization() {
    // given
    when(userService.getById(1L)).thenReturn(testUser);

    // when
    boolean result = organizationService.isUserInOrganization(1L, 1L);

    // then
    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("检查用户是否属于组织 - 不属于")
  void shouldReturnFalse_whenUserNotBelongsToOrganization() {
    // given
    when(userService.getById(1L)).thenReturn(testUser);

    // when
    boolean result = organizationService.isUserInOrganization(2L, 1L);

    // then
    assertThat(result).isFalse();
  }

  @Test
  @DisplayName("检查用户是否属于组织 - 用户不存在")
  void shouldReturnFalse_whenUserNotExists() {
    // given
    when(userService.getById(999L)).thenReturn(null);

    // when
    boolean result = organizationService.isUserInOrganization(1L, 999L);

    // then
    assertThat(result).isFalse();
  }

  // ==================== 统计功能测试 ====================

  @Test
  @DisplayName("获取组织成员数量")
  void shouldGetMemberCount() {
    // given
    when(userService.countByOrganizationId(1L)).thenReturn(5L);

    // when
    long result = organizationService.getMemberCount(1L);

    // then
    assertThat(result).isEqualTo(5L);
    verify(userService).countByOrganizationId(1L);
  }

  @Test
  @DisplayName("获取组织项目数量")
  void shouldGetProjectCount() {
    // given
    when(projectMapper.selectCount(any())).thenReturn(10L);

    // when
    long result = organizationService.getProjectCount(1L);

    // then
    assertThat(result).isEqualTo(10L);
    verify(projectMapper).selectCount(any());
  }

  // ==================== 组织设置管理测试 ====================

  @Test
  @DisplayName("获取组织设置 - 返回默认设置（settings为空）")
  void shouldGetDefaultOrganizationSettings_whenSettingsIsEmpty() {
    // given
    testOrganization.setSettings(null);
    when(organizationMapper.selectById(1L)).thenReturn(testOrganization);

    // when
    OrganizationSettingsDTO result = organizationService.getOrganizationSettings(1L, 1L);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getNotification()).isNotNull();
    assertThat(result.getSecurity()).isNotNull();
    assertThat(result.getProject()).isNotNull();
    assertThat(result.getNotification().getEmailEnabled()).isTrue();
    assertThat(result.getSecurity().getPasswordMinLength()).isEqualTo(8);
  }

  @Test
  @DisplayName("获取组织设置 - 从JSON解析")
  void shouldGetOrganizationSettings_whenSettingsExists() throws Exception {
    // given
    OrganizationSettingsDTO settings =
        OrganizationSettingsDTO.builder()
            .notification(
                OrganizationSettingsDTO.NotificationSettings.builder()
                    .emailEnabled(false)
                    .inAppEnabled(true)
                    .websocketEnabled(false)
                    .digestFrequencyDays(7)
                    .build())
            .security(
                OrganizationSettingsDTO.SecuritySettings.builder()
                    .passwordMinLength(10)
                    .passwordRequireSpecialChar(true)
                    .sessionTimeoutMinutes(120)
                    .twoFactorAuthEnabled(true)
                    .ipWhitelist(Arrays.asList("192.168.1.1"))
                    .build())
            .project(
                OrganizationSettingsDTO.ProjectSettings.builder()
                    .defaultVisibility("PUBLIC")
                    .allowPublicProjects(false)
                    .maxProjects(50)
                    .maxMembersPerProject(20)
                    .storageLimitMb(5120L)
                    .build())
            .build();

    ObjectMapper mapper = new ObjectMapper();
    String settingsJson = mapper.writeValueAsString(settings);
    testOrganization.setSettings(settingsJson);

    when(organizationMapper.selectById(1L)).thenReturn(testOrganization);

    // when
    OrganizationSettingsDTO result = organizationService.getOrganizationSettings(1L, 1L);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getNotification().getEmailEnabled()).isFalse();
    assertThat(result.getSecurity().getPasswordMinLength()).isEqualTo(10);
    assertThat(result.getProject().getDefaultVisibility()).isEqualTo("PUBLIC");
  }

  @Test
  @DisplayName("获取组织设置 - 非成员无权限")
  void shouldThrowForbidden_whenGettingSettingsWithoutMembership() {
    // given
    when(permissionService.isOrganizationMember(2L, 1L)).thenReturn(false);

    // when & then
    assertThatThrownBy(() -> organizationService.getOrganizationSettings(1L, 2L))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("无权访问");
  }

  @Test
  @DisplayName("获取组织设置 - 组织不存在")
  void shouldThrowException_whenOrganizationNotExistsForGetSettings() {
    // given
    when(organizationMapper.selectById(999L)).thenReturn(null);

    // when & then
    assertThatThrownBy(() -> organizationService.getOrganizationSettings(999L, 1L))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("组织不存在");
  }

  @Test
  @DisplayName("更新组织设置 - 成功")
  void shouldUpdateOrganizationSettings_whenOrganizationExists() {
    // given
    OrganizationSettingsDTO settings =
        OrganizationSettingsDTO.builder()
            .notification(
                OrganizationSettingsDTO.NotificationSettings.builder()
                    .emailEnabled(true)
                    .inAppEnabled(true)
                    .websocketEnabled(true)
                    .digestFrequencyDays(1)
                    .build())
            .security(
                OrganizationSettingsDTO.SecuritySettings.builder()
                    .passwordMinLength(12)
                    .passwordRequireSpecialChar(true)
                    .sessionTimeoutMinutes(60)
                    .twoFactorAuthEnabled(false)
                    .ipWhitelist(Arrays.asList())
                    .build())
            .project(
                OrganizationSettingsDTO.ProjectSettings.builder()
                    .defaultVisibility("PRIVATE")
                    .allowPublicProjects(true)
                    .maxProjects(100)
                    .maxMembersPerProject(50)
                    .storageLimitMb(10240L)
                    .build())
            .build();

    when(organizationMapper.selectById(1L)).thenReturn(testOrganization);
    when(organizationMapper.updateById(any(Organization.class))).thenReturn(1);

    // when
    OrganizationSettingsDTO result =
        organizationService.updateOrganizationSettings(1L, settings, 1L);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getNotification()).isEqualTo(settings.getNotification());
    assertThat(result.getSecurity()).isEqualTo(settings.getSecurity());
    assertThat(result.getProject()).isEqualTo(settings.getProject());
    verify(organizationMapper).selectById(1L);
    verify(organizationMapper).updateById(any(Organization.class));
  }

  @Test
  @DisplayName("更新组织设置 - 缺失部分字段时填充默认值")
  void shouldApplyDefaultsWhenSettingsMissingSections() {
    // given
    OrganizationSettingsDTO settings =
        OrganizationSettingsDTO.builder()
            .notification(
                OrganizationSettingsDTO.NotificationSettings.builder()
                    .emailEnabled(false)
                    .inAppEnabled(null)
                    .websocketEnabled(null)
                    .build())
            .build();

    when(organizationMapper.selectById(1L)).thenReturn(testOrganization);
    when(organizationMapper.updateById(any(Organization.class))).thenReturn(1);

    // when
    OrganizationSettingsDTO result =
        organizationService.updateOrganizationSettings(1L, settings, 1L);

    // then
    assertThat(result.getNotification().getEmailEnabled()).isFalse();
    assertThat(result.getNotification().getInAppEnabled()).isTrue();
    assertThat(result.getNotification().getWebsocketEnabled()).isTrue();
    assertThat(result.getSecurity().getPasswordMinLength()).isEqualTo(8);
    assertThat(result.getProject().getDefaultVisibility()).isEqualTo("PRIVATE");
    verify(organizationMapper).updateById(any(Organization.class));
  }

  @Test
  @DisplayName("更新组织设置 - 校验失败")
  void shouldThrowExceptionWhenSettingsInvalid() {
    // given
    OrganizationSettingsDTO settings =
        OrganizationSettingsDTO.builder()
            .notification(
                OrganizationSettingsDTO.NotificationSettings.builder()
                    .emailEnabled(true)
                    .inAppEnabled(true)
                    .websocketEnabled(true)
                    .digestFrequencyDays(0)
                    .build())
            .security(
                OrganizationSettingsDTO.SecuritySettings.builder()
                    .passwordMinLength(8)
                    .passwordRequireSpecialChar(true)
                    .sessionTimeoutMinutes(60)
                    .twoFactorAuthEnabled(false)
                    .build())
            .project(
                OrganizationSettingsDTO.ProjectSettings.builder()
                    .defaultVisibility("PRIVATE")
                    .allowPublicProjects(true)
                    .maxProjects(100)
                    .maxMembersPerProject(50)
                    .storageLimitMb(10240L)
                    .build())
            .build();

    when(organizationMapper.selectById(1L)).thenReturn(testOrganization);

    // when & then
    assertThatThrownBy(() -> organizationService.updateOrganizationSettings(1L, settings, 1L))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("通知摘要频率");
    verify(organizationMapper, never()).updateById(any(Organization.class));
  }

  @Test
  @DisplayName("更新组织设置 - 非管理员无权限")
  void shouldThrowForbidden_whenUpdatingSettingsWithoutAdminPermission() {
    // given
    OrganizationSettingsDTO settings = OrganizationSettingsDTO.builder().build();
    when(permissionService.isOrganizationAdmin(2L, 1L)).thenReturn(false);

    // when & then
    assertThatThrownBy(() -> organizationService.updateOrganizationSettings(1L, settings, 2L))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("无权操作");
  }

  @Test
  @DisplayName("更新组织设置 - 组织不存在")
  void shouldThrowException_whenOrganizationNotExistsForUpdateSettings() {
    // given
    OrganizationSettingsDTO settings = OrganizationSettingsDTO.builder().build();
    when(organizationMapper.selectById(999L)).thenReturn(null);

    // when & then
    assertThatThrownBy(() -> organizationService.updateOrganizationSettings(999L, settings, 1L))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("组织不存在");
  }
}
