package com.promanage.service.impl;

import com.promanage.common.entity.User;
import com.promanage.service.entity.Document;
import com.promanage.service.entity.Notification;
import com.promanage.service.entity.ProjectMember;
import com.promanage.service.entity.Task;
import com.promanage.service.mapper.ChangeRequestMapper;
import com.promanage.service.mapper.DocumentMapper;
import com.promanage.service.mapper.NotificationMapper;
import com.promanage.service.mapper.OrganizationMapper;
import com.promanage.service.mapper.PermissionMapper;
import com.promanage.service.mapper.ProjectMapper;
import com.promanage.service.mapper.ProjectMemberMapper;
import com.promanage.service.mapper.RoleMapper;
import com.promanage.service.mapper.RolePermissionMapper;
import com.promanage.service.mapper.TaskMapper;
import com.promanage.service.mapper.UserMapper;
import com.promanage.service.mapper.UserRoleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PermissionServiceImpl 单元测试")
class PermissionServiceImplTest {

    @Mock
    private PermissionMapper permissionMapper;
    @Mock
    private RolePermissionMapper rolePermissionMapper;
    @Mock
    private UserRoleMapper userRoleMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private ProjectMemberMapper projectMemberMapper;
    @Mock
    private ProjectMapper projectMapper;
    @Mock
    private TaskMapper taskMapper;
    @Mock
    private DocumentMapper documentMapper;
    @Mock
    private NotificationMapper notificationMapper;
    @Mock
    private OrganizationMapper organizationMapper;
    @Mock
    private ChangeRequestMapper changeRequestMapper;
    @Mock
    private RoleMapper roleMapper;

    @InjectMocks
    private PermissionServiceImpl permissionService;

    private User testUser;
    private ProjectMember testProjectMember;
    private ProjectMember testProjectAdmin;
    private Task testTask;
    private Document testDocument;
    private Notification testNotification;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setOrganizationId(10L);

        testProjectMember = new ProjectMember();
        testProjectMember.setUserId(1L);
        testProjectMember.setProjectId(100L);
        testProjectMember.setRoleId(2L);
        testProjectMember.setStatus(1);

        testProjectAdmin = new ProjectMember();
        testProjectAdmin.setUserId(1L);
        testProjectAdmin.setProjectId(100L);
        testProjectAdmin.setRoleId(1L);
        testProjectAdmin.setStatus(1);

        testTask = new Task();
        testTask.setId(1000L);
        testTask.setProjectId(100L);

        testDocument = new Document();
        testDocument.setId(2000L);
        testDocument.setProjectId(100L);

        testNotification = new Notification();
        testNotification.setId(3000L);
        testNotification.setUserId(1L);
        testNotification.setDeleted(false);
    }

    @Test
    @DisplayName("isOrganizationMember - 用户属于组织时返回 true")
    void shouldReturnTrue_whenUserIsInOrganization() {
        when(userMapper.selectById(1L)).thenReturn(testUser);
        boolean result = permissionService.isOrganizationMember(1L, 10L);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isOrganizationMember - 用户不属于组织时返回 false")
    void shouldReturnFalse_whenUserIsNotInOrganization() {
        when(userMapper.selectById(1L)).thenReturn(testUser);
        boolean result = permissionService.isOrganizationMember(1L, 99L);
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isProjectMember - 用户是项目成员时返回 true")
    void shouldReturnTrue_whenUserIsProjectMember() {
        when(projectMemberMapper.selectCount(any())).thenReturn(1L);
        boolean result = permissionService.isProjectMember(1L, 100L);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isProjectMember - 用户不是项目成员时返回 false")
    void shouldReturnFalse_whenUserIsNotProjectMember() {
        when(projectMemberMapper.selectCount(any())).thenReturn(0L);
        boolean result = permissionService.isProjectMember(1L, 999L);
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isProjectAdmin - 用户是项目管理员时返回 true")
    void shouldReturnTrue_whenUserIsProjectAdmin() {
        when(projectMemberMapper.selectOne(any())).thenReturn(testProjectAdmin);
        boolean result = permissionService.isProjectAdmin(1L, 100L);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isProjectAdmin - 普通成员不是管理员时返回 false")
    void shouldReturnFalse_whenUserIsProjectMemberButNotAdmin() {
        when(projectMemberMapper.selectOne(any())).thenReturn(testProjectMember);
        boolean result = permissionService.isProjectAdmin(1L, 100L);
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("canAccessTask - 用户是项目成员时可以访问任务")
    void shouldReturnTrue_forTaskAccess_whenUserIsProjectMember() {
        when(taskMapper.selectById(1000L)).thenReturn(testTask);
        when(projectMemberMapper.selectCount(any())).thenReturn(1L);
        boolean result = permissionService.canAccessTask(1L, 1000L);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("canAccessTask - 用户不是项目成员时不能访问任务")
    void shouldReturnFalse_forTaskAccess_whenUserIsNotProjectMember() {
        when(taskMapper.selectById(1000L)).thenReturn(testTask);
        when(projectMemberMapper.selectCount(any())).thenReturn(0L);
        boolean result = permissionService.canAccessTask(1L, 1000L);
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("canAccessDocument - 用户是项目成员时可以访问文档")
    void shouldReturnTrue_forDocumentAccess_whenUserIsProjectMember() {
        when(documentMapper.selectById(2000L)).thenReturn(testDocument);
        when(projectMemberMapper.selectCount(any())).thenReturn(1L);
        boolean result = permissionService.canAccessDocument(1L, 2000L);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("canAccessDocument - 任务不存在时返回 false")
    void shouldReturnFalse_forDocumentAccess_whenDocumentNotFound() {
        when(documentMapper.selectById(9999L)).thenReturn(null);
        boolean result = permissionService.canAccessDocument(1L, 9999L);
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("canAccessNotification - 用户是通知接收者时返回 true")
    void shouldReturnTrue_forNotificationAccess_whenUserIsRecipient() {
        when(notificationMapper.selectById(3000L)).thenReturn(testNotification);
        boolean result = permissionService.canAccessNotification(1L, 3000L);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("canAccessNotification - 用户不是通知接收者时返回 false")
    void shouldReturnFalse_forNotificationAccess_whenUserIsNotRecipient() {
        when(notificationMapper.selectById(3000L)).thenReturn(testNotification);
        boolean result = permissionService.canAccessNotification(2L, 3000L);
        assertThat(result).isFalse();
    }
}
