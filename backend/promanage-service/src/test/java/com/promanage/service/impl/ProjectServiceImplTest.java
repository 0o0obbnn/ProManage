package com.promanage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.promanage.common.domain.PageResult;
import com.promanage.common.exception.BusinessException;
import com.promanage.infrastructure.security.SecurityUtils;
import com.promanage.dto.ProjectMemberDTO;
import com.promanage.dto.ProjectStatsDTO;
import com.promanage.service.entity.Project;
import com.promanage.service.entity.ProjectMember;
import com.promanage.common.entity.User;
import com.promanage.service.mapper.ProjectMapper;
import com.promanage.service.mapper.ProjectMemberMapper;
import com.promanage.service.mapper.TaskMapper;
import com.promanage.service.mapper.DocumentMapper;
import com.promanage.service.mapper.ChangeRequestMapper;
import com.promanage.service.IProjectActivityService;
import com.promanage.service.service.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * ProjectServiceImpl 单元测试 - 重新设计版本
 * <p>
 * 基于 ProManage 系统架构和业务需求全面测试项目服务功能
 * </p>
 *
 * @author ProManage Team
 * @version 2.0
 * @since 2025-10-07
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectServiceImpl 单元测试 - 重新设计版本")
@SuppressWarnings("unchecked")
class ProjectServiceImplTest {

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private ProjectMemberMapper projectMemberMapper;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private DocumentMapper documentMapper;

    @Mock
    private ChangeRequestMapper changeRequestMapper;

    @Mock
    private IProjectActivityService projectActivityService;

    @Mock
    private IUserService userService;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private Project testProject;
    private ProjectMember testProjectMember;
    private User testUser;

    @BeforeEach
    void setUp() {
        // 初始化测试项目
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("测试项目");
        testProject.setCode("PROJ001");
        testProject.setDescription("这是一个测试项目");
        testProject.setStatus(0); // 活跃状态
        testProject.setProgress(25);
        testProject.setOwnerId(1L);
        testProject.setDeleted(false);
        testProject.setCreateTime(LocalDateTime.now());
        testProject.setUpdateTime(LocalDateTime.now());

        // 初始化测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRealName("测试用户");
        testUser.setPhone("13800138000");
        testUser.setStatus(0);

        // 初始化项目成员
        testProjectMember = new ProjectMember();
        testProjectMember.setId(1L);
        testProjectMember.setProjectId(1L);
        testProjectMember.setUserId(2L);
        testProjectMember.setRoleId(1L);
        testProjectMember.setJoinTime(LocalDateTime.now());
        testProjectMember.setStatus(0);
        testProjectMember.setCreateTime(LocalDateTime.now());
        testProjectMember.setUpdateTime(LocalDateTime.now());
    }

    // ==================== 项目查询功能测试 ====================

    @Test
    @DisplayName("根据ID获取项目 - 成功")
    void shouldGetProjectById_whenProjectExists() {
        // given
        Long projectId = 1L;
        when(projectMapper.selectById(projectId)).thenReturn(testProject);

        // when
        Project result = projectService.getById(projectId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(projectId);
        assertThat(result.getName()).isEqualTo("测试项目");
        assertThat(result.getCode()).isEqualTo("PROJ001");
        verify(projectMapper).selectById(projectId);
    }

    @Test
    @DisplayName("根据ID获取项目 - 项目不存在")
    void shouldReturnNull_whenProjectNotExists() {
        // given
        Long projectId = 999L;
        when(projectMapper.selectById(projectId)).thenReturn(null);

        // when
        Project result = projectService.getById(projectId);

        // then
        assertThat(result).isNull();
        verify(projectMapper).selectById(projectId);
    }

    @Test
    @DisplayName("根据ID获取项目 - ID为null")
    void shouldReturnNull_whenProjectIdIsNull() {
        // when
        Project result = projectService.getById(null);

        // then
        assertThat(result).isNull();
        verify(projectMapper, never()).selectById(any());
    }

    @Test
    @DisplayName("根据编码获取项目 - 成功")
    void shouldGetProjectByCode_whenProjectExists() {
        // given
        String code = "PROJ001";
        ArgumentCaptor<LambdaQueryWrapper<Project>> queryWrapperCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        when(projectMapper.selectOne(queryWrapperCaptor.capture())).thenReturn(testProject);

        // when
        Project result = projectService.getByCode(code);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo(code);
        verify(projectMapper).selectOne(queryWrapperCaptor.capture());
        LambdaQueryWrapper<Project> capturedWrapper = queryWrapperCaptor.getValue();
        assertThat(capturedWrapper).isNotNull();
    }

    @Test
    @DisplayName("根据编码获取项目 - 项目不存在")
    void shouldReturnNull_whenProjectNotExistsByCode() {
        // given
        String code = "NONEXISTENT";
        ArgumentCaptor<LambdaQueryWrapper<Project>> queryWrapperCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        when(projectMapper.selectOne(queryWrapperCaptor.capture())).thenReturn(null);

        // when
        Project result = projectService.getByCode(code);

        // then
        assertThat(result).isNull();
        verify(projectMapper).selectOne(queryWrapperCaptor.capture());
        LambdaQueryWrapper<Project> capturedWrapper = queryWrapperCaptor.getValue();
        assertThat(capturedWrapper).isNotNull();
    }

    @Test
    @DisplayName("根据编码获取项目 - 编码为空")
    void shouldReturnNull_whenCodeIsBlank() {
        // when
        Project result = projectService.getByCode(null);

        // then
        assertThat(result).isNull();
        verify(projectMapper, never()).selectOne(any(LambdaQueryWrapper.class));
    }

    // ==================== 项目列表功能测试 ====================

    @Test
    @DisplayName("列出项目 - 成功（带分页和筛选）")
    void shouldListProjects_whenParametersAreValid() {
        // given
        Integer page = 1;
        Integer pageSize = 10;
        String keyword = "测试";
        Integer status = 0;

        Page<Project> pageResult = new Page<>(page, pageSize);
        pageResult.setRecords(Arrays.asList(testProject));
        pageResult.setTotal(1L);

        when(projectMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(pageResult);

        // when
        PageResult<Project> result = projectService.listProjects(page, pageSize, keyword, status);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getList()).hasSize(1);
        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getPage()).isEqualTo(page);
        assertThat(result.getPageSize()).isEqualTo(pageSize);
        verify(projectMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("列出项目 - 使用默认分页参数")
    void shouldListProjectsWithDefaultPagination_whenPageParametersAreNull() {
        // given
        Page<Project> pageResult = new Page<>(1, 20); // 默认分页
        pageResult.setRecords(Arrays.asList(testProject));
        pageResult.setTotal(1L);

        when(projectMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(pageResult);

        // when
        PageResult<Project> result = projectService.listProjects(null, null, null, null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPage()).isEqualTo(1);
        assertThat(result.getPageSize()).isEqualTo(20);
        verify(projectMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("列出项目 - 空结果")
    void shouldReturnEmptyList_whenNoProjectsMatchCriteria() {
        // given
        Page<Project> pageResult = new Page<>(1, 10);
        pageResult.setRecords(List.of());
        pageResult.setTotal(0L);

        when(projectMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(pageResult);

        // when
        PageResult<Project> result = projectService.listProjects(1, 10, "nonexistent", 1);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getList()).isEmpty();
        assertThat(result.getTotal()).isEqualTo(0L);
        verify(projectMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    // ==================== 项目创建功能测试 ====================

    @Test
    @DisplayName("创建项目 - 成功")
    void shouldCreateProject_whenProjectIsValid() {
        // given
        Project project = new Project();
        project.setName("新项目");
        project.setCode("NEW001");
        project.setDescription("新项目描述");

        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of(1L));
            when(projectMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
            when(projectMapper.insert(any(Project.class))).thenAnswer(invocation -> {
                Project p = invocation.getArgument(0);
                p.setId(2L); // 设置ID
                return 1; // 返回成功
            });

            // when
            Long resultId = projectService.create(project);

            // then
            assertThat(resultId).isNotNull();
            assertThat(resultId).isEqualTo(2L);
            verify(projectMapper).insert(any(Project.class));
            verify(projectMapper).selectCount(any(LambdaQueryWrapper.class));
        }
    }

    @Test
    @DisplayName("创建项目 - 项目名称为空")
    void shouldThrowException_whenProjectNameIsNull() {
        // given
        Project project = new Project();
        project.setName(null);
        project.setCode("PROJ001");

        // when & then
        assertThatThrownBy(() -> projectService.create(project))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("项目名称不能为空");
    }

    @Test
    @DisplayName("创建项目 - 项目编码为空")
    void shouldThrowException_whenProjectCodeIsNull() {
        // given
        Project project = new Project();
        project.setName("测试项目");
        project.setCode(null);

        // when & then
        assertThatThrownBy(() -> projectService.create(project))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("项目编码不能为空");
    }

    @Test
    @DisplayName("创建项目 - 项目编码已存在")
    void shouldThrowException_whenProjectCodeAlreadyExists() {
        // given
        Project project = new Project();
        project.setName("新项目");
        project.setCode("EXISTING001");

        when(projectMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        // when & then
        assertThatThrownBy(() -> projectService.create(project))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("项目编码已存在");
        verify(projectMapper, never()).insert(any(Project.class));
    }

    @Test
    @DisplayName("创建项目 - 无当前用户")
    void shouldThrowException_whenNoCurrentUser() {
        // given
        Project project = new Project();
        project.setName("新项目");
        project.setCode("NEW001");

        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> projectService.create(project))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("用户未登录");
            verify(projectMapper, never()).insert(any(Project.class));
        }
    }

    // ==================== 项目更新功能测试 ====================

    @Test
    @DisplayName("更新项目 - 成功")
    void shouldUpdateProject_whenProjectExistsAndNotArchived() {
        // given
        Long projectId = 1L;
        Project existingProject = new Project();
        existingProject.setId(projectId);
        existingProject.setName("旧名称");
        existingProject.setStatus(0); // 未归档

        Project updateProject = new Project();
        updateProject.setName("更新后的名称");
        updateProject.setDescription("更新后的描述");

        when(projectMapper.selectById(projectId)).thenReturn(existingProject);
        when(projectMapper.updateById(any(Project.class))).thenReturn(1);

        // when
        projectService.update(projectId, updateProject);

        // then
        verify(projectMapper).updateById(any(Project.class));
        verify(projectActivityService).recordActivity(anyLong(), anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("更新项目 - 项目不存在")
    void shouldThrowException_whenProjectNotExistsForUpdate() {
        // given
        Long projectId = 999L;
        Project updateProject = new Project();
        updateProject.setName("更新后的名称");

        when(projectMapper.selectById(projectId)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> projectService.update(projectId, updateProject))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("项目不存在");
        verify(projectMapper, never()).updateById(any(Project.class));
    }

    @Test
    @DisplayName("更新项目 - 项目已归档")
    void shouldThrowException_whenProjectIsArchived() {
        // given
        Long projectId = 1L;
        Project existingProject = new Project();
        existingProject.setId(projectId);
        existingProject.setName("已归档项目");
        existingProject.setStatus(3); // 已归档

        Project updateProject = new Project();
        updateProject.setName("新名称");

        when(projectMapper.selectById(projectId)).thenReturn(existingProject);

        // when & then
        assertThatThrownBy(() -> projectService.update(projectId, updateProject))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("项目已归档，无法修改");
        verify(projectMapper, never()).updateById(any(Project.class));
    }

    // ==================== 项目删除功能测试 ====================

    @Test
    @DisplayName("删除项目 - 成功")
    void shouldDeleteProject_whenProjectExists() {
        // given
        Long projectId = 1L;
        Project existingProject = new Project();
        existingProject.setId(projectId);
        existingProject.setName("测试项目");

        when(projectService.getById(projectId)).thenReturn(existingProject);
        when(projectService.removeById(anyLong())).thenReturn(true);

        // when
        projectService.delete(projectId);

        // then
        verify(projectService).removeById(anyLong());
        verify(projectActivityService).recordActivity(anyLong(), anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("删除项目 - 项目不存在")
    void shouldThrowException_whenProjectNotExistsForDelete() {
        // given
        Long projectId = 999L;

        when(projectService.getById(projectId)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> projectService.delete(projectId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("项目不存在");
        verify(projectService, never()).removeById(anyLong());
    }

    // ==================== 项目成员管理功能测试 ====================

    @Test
    @DisplayName("添加项目成员 - 成功")
    void shouldAddMember_whenParametersAreValid() {
        // given
        Long projectId = 1L;
        Long userId = 2L;
        Long roleId = 1L;

        when(userService.getById(userId)).thenReturn(testUser);
        when(projectMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(projectMemberMapper.insert(any(ProjectMember.class))).thenReturn(1);

        // when
        projectService.addMember(projectId, userId, roleId);

        // then
        verify(projectMemberMapper).insert(any(ProjectMember.class));
        verify(projectActivityService).recordActivity(anyLong(), anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("添加项目成员 - 用户不存在")
    void shouldThrowException_whenUserNotExistsForAddMember() {
        // given
        Long projectId = 1L;
        Long userId = 2L;
        Long roleId = 1L;

        when(userService.getById(userId)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> projectService.addMember(projectId, userId, roleId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户不存在");
        verify(projectMemberMapper, never()).insert(any(ProjectMember.class));
    }

    @Test
    @DisplayName("添加项目成员 - 用户已是成员")
    void shouldThrowException_whenUserIsAlreadyMember() {
        // given
        Long projectId = 1L;
        Long userId = 2L;
        Long roleId = 1L;

        when(userService.getById(userId)).thenReturn(testUser);
        when(projectMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(new ProjectMember());

        // when & then
        assertThatThrownBy(() -> projectService.addMember(projectId, userId, roleId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户已经是项目成员");
        verify(projectMemberMapper, never()).insert(any(ProjectMember.class));
    }

    @Test
    @DisplayName("移除项目成员 - 成功")
    void shouldRemoveMember_whenUserIsMember() {
        // given
        Long projectId = 1L;
        Long userId = 2L;

        when(projectMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(new ProjectMember());
        when(projectMemberMapper.deleteById(anyLong())).thenReturn(1);
        when(userService.getById(userId)).thenReturn(testUser);

        // when
        projectService.removeMember(projectId, userId);

        // then
        verify(projectMemberMapper).deleteById(anyLong());
        verify(projectActivityService).recordActivity(anyLong(), anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("移除项目成员 - 用户不是成员")
    void shouldThrowException_whenUserIsNotMember() {
        // given
        Long projectId = 1L;
        Long userId = 2L;

        when(projectMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> projectService.removeMember(projectId, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("成员不存在");
        verify(projectMemberMapper, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("列出项目成员 - 成功")
    void shouldListMembers_whenProjectExists() {
        // given
        Long projectId = 1L;
        Page<ProjectMember> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(testProjectMember));
        page.setTotal(1L);

        when(projectMemberMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        // when
        PageResult<ProjectMemberDTO> result = projectService.listMembers(projectId, 1, 10, null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getList()).hasSize(1);
        assertThat(result.getTotal()).isEqualTo(1L);
        verify(projectMemberMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    // ==================== 项目统计功能测试 ====================

    @Test
    @DisplayName("获取项目统计数据 - 成功")
    void shouldGetProjectStats_whenProjectExists() {
        // given
        Long projectId = 1L;
        when(projectService.getById(projectId)).thenReturn(testProject);
        when(taskMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);
        when(documentMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(10L);
        when(projectMemberMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(3L);
        when(changeRequestMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(2L);

        // when
        ProjectStatsDTO stats = projectService.getProjectStats(projectId);

        // then
        assertThat(stats).isNotNull();
        assertThat(stats.getTotalTasks()).isEqualTo(5);
        assertThat(stats.getTotalDocuments()).isEqualTo(10);
        assertThat(stats.getMemberCount()).isEqualTo(3);
        assertThat(stats.getChangeRequests()).isEqualTo(2);
    }

    @Test
    @DisplayName("获取项目统计数据 - 项目不存在")
    void shouldThrowException_whenProjectNotExistsForStats() {
        // given
        Long projectId = 999L;
        when(projectService.getById(projectId)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> projectService.getProjectStats(projectId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("项目不存在");
    }

    // ==================== 归档/取消归档功能测试 ====================

    @Test
    @DisplayName("归档项目 - 成功")
    void shouldArchiveProject_whenProjectExists() {
        // given
        Long projectId = 1L;
        when(projectMapper.selectById(projectId)).thenReturn(testProject);
        when(projectMapper.updateById(any(Project.class))).thenReturn(1);

        // when
        projectService.archive(projectId);

        // then
        verify(projectMapper).updateById(argThat((Project p) -> p.getStatus() == 3));
        verify(projectActivityService).recordActivity(anyLong(), anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("取消归档项目 - 成功")
    void shouldUnarchiveProject_whenProjectIsArchived() {
        // given
        Long projectId = 1L;
        testProject.setStatus(3); // 设置为已归档
        when(projectMapper.selectById(projectId)).thenReturn(testProject);
        when(projectMapper.updateById(any(Project.class))).thenReturn(1);

        // when
        projectService.unarchive(projectId);

        // then
        verify(projectMapper).updateById(argThat((Project p) -> p.getStatus() == 0));
        verify(projectActivityService).recordActivity(anyLong(), anyLong(), anyString(), anyString());
    }

    // ==================== 权限检查功能测试 ====================

    @Test
    @DisplayName("检查是否为项目成员 - 是成员")
    void shouldReturnTrue_whenUserIsMember() {
        // given
        Long projectId = 1L;
        Long userId = 2L;
        when(projectMemberMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        // when
        boolean result = projectService.isMember(projectId, userId);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("检查是否为项目成员 - 不是成员")
    void shouldReturnFalse_whenUserIsNotMember() {
        // given
        Long projectId = 1L;
        Long userId = 2L;
        when(projectMemberMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        // when
        boolean result = projectService.isMember(projectId, userId);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("检查项目编码是否存在 - 存在")
    void shouldReturnTrue_whenProjectCodeExists() {
        // given
        String code = "PROJ001";
        when(projectService.count(any(LambdaQueryWrapper.class))).thenReturn(1L);

        // when
        boolean result = projectService.existsByCode(code);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("检查项目编码是否存在 - 不存在")
    void shouldReturnFalse_whenProjectCodeNotExists() {
        // given
        String code = "NONEXISTENT";
        when(projectService.count(any(LambdaQueryWrapper.class))).thenReturn(0L);

        // when
        boolean result = projectService.existsByCode(code);

        // then
        assertThat(result).isFalse();
    }

    // ==================== 用户项目列表功能测试 ====================

    @Test
    @DisplayName("列出用户项目 - 成功")
    void shouldListUserProjects_whenParametersAreValid() {
        // given
        Long userId = 1L;
        Integer page = 1;
        Integer pageSize = 10;
        Integer status = 0;

        Page<Project> pageResult = new Page<>(page, pageSize);
        pageResult.setRecords(Arrays.asList(testProject));
        pageResult.setTotal(1L);

        // Mock the behavior for listUserProjects
        doReturn(pageResult).when(projectService).page(any(Page.class), any(LambdaQueryWrapper.class));

        // when
        PageResult<Project> result = projectService.listUserProjects(userId, page, pageSize, status, null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getList()).hasSize(1);
        assertThat(result.getTotal()).isEqualTo(1L);
        verify(projectService).page(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("列出用户项目 - 带关键字搜索")
    void shouldListUserProjects_withKeyword() {
        // given
        Long userId = 1L;
        Integer page = 1;
        Integer pageSize = 10;
        String keyword = "测试";

        Page<Project> pageResult = new Page<>(page, pageSize);
        pageResult.setRecords(Arrays.asList(testProject));
        pageResult.setTotal(1L);

        // Mock the behavior for listUserProjects with a keyword
        when(userService.listAll()).thenReturn(List.of()); // 假设没有匹配的所有者
        doReturn(pageResult).when(projectService).page(any(Page.class), any(LambdaQueryWrapper.class));

        // when
        PageResult<Project> result = projectService.listUserProjects(userId, page, pageSize, null, keyword);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getList()).hasSize(1);
        assertThat(result.getTotal()).isEqualTo(1L);
        verify(projectService).page(any(Page.class), any(LambdaQueryWrapper.class));
        verify(userService).listAll();
    }

    // ==================== 成员统计功能测试 ====================

    @Test
    @DisplayName("统计项目成员数量")
    void shouldCountProjectMembers() {
        // given
        Long projectId = 1L;
        when(projectMemberMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

        // when
        int result = projectService.countProjectMembers(projectId);

        // then
        assertThat(result).isEqualTo(5);
        verify(projectMemberMapper).selectCount(any(LambdaQueryWrapper.class));
    }

    // ==================== 权限检查高级功能测试 ====================

    @Test
    @DisplayName("检查项目查看权限 - 用户是项目负责人")
    void shouldReturnTrue_whenUserIsProjectOwnerForViewPermission() {
        // given
        Long projectId = 1L;
        Long userId = 1L;

        Project project = new Project();
        project.setId(projectId);
        project.setOwnerId(userId);

        when(projectService.getById(projectId)).thenReturn(project);

        // when
        boolean result = projectService.isMemberOrAdmin(projectId, userId);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("检查项目查看权限 - 用户是项目成员")
    void shouldReturnTrue_whenUserIsProjectMemberForViewPermission() {
        // given
        Long projectId = 1L;
        Long userId = 2L; // 不是项目负责人

        when(projectService.isMember(projectId, userId)).thenReturn(true);
        when(projectService.isAdmin(projectId, userId)).thenReturn(false);

        // when
        boolean result = projectService.isMemberOrAdmin(projectId, userId);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("检查项目编辑权限 - 用户是项目负责人")
    void shouldReturnTrue_whenUserIsProjectOwnerForEditPermission() {
        // given
        Long projectId = 1L;
        Long userId = 1L;

        Project project = new Project();
        project.setId(projectId);
        project.setOwnerId(userId);

        when(projectService.getById(projectId)).thenReturn(project);

        // when
        boolean result = projectService.isAdmin(projectId, userId);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("检查项目编辑权限 - 用户不是项目负责人")
    void shouldReturnFalse_whenUserIsNotProjectOwnerForEditPermission() {
        // given
        Long projectId = 1L;
        Long userId = 2L; // 不是项目负责人

        Project project = new Project();
        project.setId(projectId);
        project.setOwnerId(1L); // 其他用户是负责人

        when(projectService.getById(projectId)).thenReturn(project);

        // when
        boolean result = projectService.isAdmin(projectId, userId);

        // then
        assertThat(result).isFalse();
    }

    // ==================== 边界条件测试 ====================

    @Test
    @DisplayName("创建项目 - 项目名称过长")
    void shouldThrowException_whenProjectNameTooLong() {
        // given
        Project project = new Project();
        project.setName("这是一个非常非常非常非常非常非常非常非常非常非常非常非常非常非常非常非常非常非常长的项目名称");
        project.setCode("LONG001");

        // when & then
        assertThatThrownBy(() -> projectService.create(project))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("项目名称长度超出限制");
    }

    @Test
    @DisplayName("创建项目 - 项目编码格式无效")
    void shouldThrowException_whenProjectCodeInvalidFormat() {
        // given
        Project project = new Project();
        project.setName("测试项目");
        project.setCode("invalid_code_with_special_chars!");

        // when & then
        assertThatThrownBy(() -> projectService.create(project))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("项目编码格式无效");
    }

    @Test
    @DisplayName("更新项目 - 更新已删除的项目")
    void shouldThrowException_whenUpdateDeletedProject() {
        // given
        Long projectId = 1L;
        Project existingProject = new Project();
        existingProject.setId(projectId);
        existingProject.setName("已删除项目");
        existingProject.setDeleted(true);

        Project updateProject = new Project();
        updateProject.setName("新名称");

        when(projectMapper.selectById(projectId)).thenReturn(existingProject);

        // when & then
        assertThatThrownBy(() -> projectService.update(projectId, updateProject))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("项目不存在");
        verify(projectMapper, never()).updateById(any(Project.class));
    }

    @Test
    @DisplayName("删除项目 - 删除已归档的项目")
    void shouldDeleteArchivedProject() {
        // given
        Long projectId = 1L;
        Project existingProject = new Project();
        existingProject.setId(projectId);
        existingProject.setName("已归档项目");
        existingProject.setStatus(3); // 已归档

        when(projectService.getById(projectId)).thenReturn(existingProject);
        when(projectService.removeById(anyLong())).thenReturn(true);

        // when
        projectService.delete(projectId);

        // then
        verify(projectService).removeById(anyLong());
        verify(projectActivityService).recordActivity(anyLong(), anyLong(), anyString(), anyString());
    }
}
