package com.promanage.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.promanage.common.entity.User;
import com.promanage.common.exception.BusinessException;
import com.promanage.common.result.PageResult;
import com.promanage.dto.CreateProjectRequestDTO;
import com.promanage.dto.ProjectMemberDTO;
import com.promanage.service.IProjectActivityService;
import com.promanage.domain.entity.Role;
import com.promanage.service.entity.Project;
import com.promanage.service.entity.ProjectMember;
import com.promanage.service.mapper.ChangeRequestMapper;
import com.promanage.service.mapper.DocumentMapper;
import com.promanage.service.mapper.ProjectDtoMapper;
import com.promanage.service.mapper.ProjectMapper;
import com.promanage.service.mapper.ProjectMemberMapper;
import com.promanage.service.mapper.TaskMapper;
import com.promanage.service.service.IPermissionService;
import com.promanage.service.service.IRoleService;
import com.promanage.service.service.IUserService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private ProjectMemberMapper projectMemberMapper;

    @Mock
    private IUserService userService;

    @Mock
    private IRoleService roleService;

    @Mock
    private IPermissionService permissionService;

    @Mock
    private ProjectDtoMapper projectDtoMapper;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private DocumentMapper documentMapper;

    @Mock
    private ChangeRequestMapper changeRequestMapper;

    @Mock
    private IProjectActivityService projectActivityService;

    @InjectMocks
    private ProjectServiceImpl projectService;

    @BeforeEach
    void setUpSecurityContext() {
        when(permissionService.isSuperAdmin(any())).thenReturn(false);
        // SecurityUtils is static; we rely on service method parameters instead of static context in
        // tests.
    }

    @Test
    void shouldCreateProjectAndAssignOwnerWhenNonSuperAdmin() {
        Long creatorId = 10L;
        CreateProjectRequestDTO request = CreateProjectRequestDTO.builder()
            .name("Backend Refactor")
            .code("PROMANAGE_BACKEND")
            .description("Refactor backend module")
            .startDate(LocalDate.of(2025, 10, 1))
            .endDate(LocalDate.of(2025, 12, 1))
            .priority(2)
            .type("SOFTWARE")
            .build();

        User currentUser = new User();
        currentUser.setId(creatorId);
        currentUser.setOrganizationId(2L);
        currentUser.setRealName("Alice");

        Project newProject = new Project();
        newProject.setOwnerId(creatorId);

        given(userService.getById(creatorId)).willReturn(currentUser);
        given(projectDtoMapper.toEntity(request)).willReturn(newProject);
        given(projectMemberMapper.selectCount(any())).willReturn(0L);
        doAnswer(invocation -> {
            Project arg = invocation.getArgument(0);
            arg.setId(99L);
            return 1;
        })
            .when(projectMapper)
            .insert(any(Project.class));

        projectService.createProject(request, creatorId);

        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(
            Project.class
        );
        verify(projectMapper).insert(projectCaptor.capture());
        Project saved = projectCaptor.getValue();
        assertThat(saved.getOrganizationId()).isEqualTo(2L);
        assertThat(saved.getStatus()).isEqualTo(1);
        assertThat(saved.getCreateTime()).isNotNull();

        ArgumentCaptor<ProjectMember> memberCaptor = ArgumentCaptor.forClass(
            ProjectMember.class
        );
        verify(projectMemberMapper).insert(memberCaptor.capture());
        ProjectMember ownerMember = memberCaptor.getValue();
        assertThat(ownerMember.getUserId()).isEqualTo(creatorId);
        assertThat(ownerMember.getProjectId()).isEqualTo(saved.getId());
        assertThat(ownerMember.getRoleId()).isEqualTo(1L);
        assertThat(ownerMember.getStatus()).isEqualTo(1);
    }

    @Test
    void shouldRejectAddingMemberFromDifferentOrganization() {
        Long projectId = 20L;
        Long operatorId = 5L;
        Long targetUserId = 8L;

        Project project = new Project();
        project.setId(projectId);
        project.setOrganizationId(3L);
        project.setOwnerId(operatorId);

        User anotherUser = new User();
        anotherUser.setId(targetUserId);
        anotherUser.setOrganizationId(6L);

        given(
            permissionService.isProjectAdmin(operatorId, projectId)
        ).willReturn(true);
        given(projectMapper.selectById(projectId)).willReturn(project);
        given(userService.getById(targetUserId)).willReturn(anotherUser);

        assertThatThrownBy(() ->
            projectService.addProjectMember(
                projectId,
                targetUserId,
                2L,
                operatorId
            )
        )
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("用户不属于该项目所在组织");

        verify(projectMemberMapper, never()).insert(any(ProjectMember.class));
    }

    @Test
    void shouldListProjectMembersWithUserAndRoleInfo() {
        Long projectId = 100L;
        Long requesterId = 30L;

        given(
            permissionService.isProjectMember(requesterId, projectId)
        ).willReturn(true);
        Project project = new Project();
        project.setId(projectId);
        project.setOrganizationId(1L);
        given(projectMapper.selectById(projectId)).willReturn(project);
        ProjectMember member = new ProjectMember();
        member.setId(1L);
        member.setProjectId(projectId);
        member.setUserId(55L);
        member.setRoleId(7L);
        member.setJoinTime(LocalDateTime.now());
        member.setStatus(1);

        Page<ProjectMember> page = new Page<>(1, 20);
        page.setRecords(Collections.singletonList(member));
        page.setTotal(1);

        given(
            projectMemberMapper.selectPage(any(Page.class), any())
        ).willReturn(page);

        User user = new User();
        user.setId(55L);
        user.setUsername("jdoe");
        user.setRealName("John Doe");
        user.setEmail("john@example.com");
        given(userService.getByIds(any())).willReturn(
            Collections.singletonMap(55L, user)
        );

        Role role = new Role();
        role.setId(7L);
        role.setRoleName("Developer");
        role.setRoleCode("PROJECT_DEV");
        given(roleService.getByIds(any())).willReturn(
            Collections.singletonMap(7L, role)
        );

        PageResult<ProjectMemberDTO> result = projectService.listProjectMembers(
            projectId,
            requesterId,
            1,
            20,
            null
        );

        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getList()).hasSize(1);
        ProjectMemberDTO dto = result.getList().get(0);
        assertThat(dto.getUserId()).isEqualTo(55L);
        assertThat(dto.getRoleCode()).isEqualTo("PROJECT_DEV");
        assertThat(dto.getUsername()).isEqualTo("jdoe");
    }
}
