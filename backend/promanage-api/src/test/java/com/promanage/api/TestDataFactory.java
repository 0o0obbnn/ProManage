package com.promanage.api;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import com.promanage.common.entity.Organization;
import com.promanage.common.entity.User;
import com.promanage.dto.CreateProjectRequestDTO;
import com.promanage.dto.OrganizationMemberDTO;
import com.promanage.dto.ProjectDTO;
import com.promanage.service.entity.Project;

/**
 * 测试数据工厂类
 * 
 * <p>提供统一的测试数据创建方法，确保测试数据的一致性和可维护性
 * 
 * @author ProManage Team
 * @since 2025-10-22
 */
public class TestDataFactory {

    // ==================== 用户测试数据 ====================
    
    public static User createTestUser() {
        return createTestUser(1L, "testuser", "test@example.com");
    }
    
    public static User createTestUser(Long id, String username, String email) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setRealName("Test User " + id);
        user.setPassword("$2a$10$encoded.password.hash");
        user.setStatus(1); // 正常状态
        user.setCreateTime(LocalDateTime.now().minusDays(30));
        user.setUpdateTime(LocalDateTime.now());
        return user;
    }
    
    public static User createAdminUser() {
        User admin = createTestUser(999L, "admin", "admin@example.com");
        admin.setRealName("Admin User");
        return admin;
    }
    
    // ==================== 组织测试数据 ====================
    
    public static Organization createTestOrganization() {
        return createTestOrganization(1L, "Test Organization");
    }
    
    public static Organization createTestOrganization(Long id, String name) {
        Organization org = new Organization();
        org.setId(id);
        org.setName(name);
        org.setSlug("test-org-" + id);
        org.setDescription("Test organization for unit testing");
        org.setIsActive(true); // 正常状态
        org.setCreatorId(1L); // 通过setCreatorId设置，getOwnerId会返回creatorId
        org.setCreateTime(LocalDateTime.now().minusDays(60));
        org.setUpdateTime(LocalDateTime.now());
        return org;
    }
    
    public static List<OrganizationMemberDTO> createTestOrganizationMembers() {
        return Arrays.asList(
            createOrganizationMember(1L, "John Doe", "ADMIN"),
            createOrganizationMember(2L, "Jane Smith", "MEMBER"),
            createOrganizationMember(3L, "Bob Johnson", "MEMBER")
        );
    }
    
    private static OrganizationMemberDTO createOrganizationMember(Long userId, String name, String role) {
        OrganizationMemberDTO member = new OrganizationMemberDTO();
        member.setId(userId);
        member.setUsername(name);
        member.setRealName(name);
        member.setPosition(role);
        member.setStatus(1);
        member.setLastLoginTime(LocalDateTime.now().minusDays(1));
        return member;
    }
    
    // ==================== 项目测试数据 ====================
    
    public static Project createTestProject() {
        return createTestProject(1L, "Test Project");
    }
    
    public static Project createTestProject(Long id, String name) {
        Project project = new Project();
        project.setId(id);
        project.setName(name);
        project.setCode("TEST_PROJ_" + id);
        project.setDescription("Test project for unit testing");
        project.setStatus(1); // 进行中
        project.setOwnerId(1L);
        project.setOrganizationId(1L);
        project.setStartDate(LocalDate.now().minusDays(30));
        project.setEndDate(LocalDate.now().plusDays(60));
        project.setPriority(2); // 中等优先级
        project.setProgress(45);
        project.setType("SOFTWARE");
        project.setCreateTime(LocalDateTime.now().minusDays(30));
        project.setUpdateTime(LocalDateTime.now());
        return project;
    }
    
    public static ProjectDTO createTestProjectDTO() {
        return createTestProjectDTO(1L, "Test Project DTO");
    }
    
    public static ProjectDTO createTestProjectDTO(Long id, String name) {
        return ProjectDTO.builder()
            .id(id)
            .name(name)
            .code("TEST_PROJ_DTO_" + id)
            .description("Test project DTO for unit testing")
            .status(1)
            .organizationId(1L)
            .ownerId(1L)
            .ownerName("Test Owner")
            .startDate(LocalDate.now().minusDays(30))
            .endDate(LocalDate.now().plusDays(60))
            .priority(2)
            .progress(45)
            .type("SOFTWARE")
            .createdAt(LocalDateTime.now().minusDays(30))
            .updatedAt(LocalDateTime.now())
            .archived(false)
            .build();
    }
    
    public static CreateProjectRequestDTO createTestProjectRequest() {
        return CreateProjectRequestDTO.builder()
            .name("New Test Project")
            .code("NEW_TEST_PROJ")
            .description("New test project for creation testing")
            .organizationId(1L)
            .ownerId(1L)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(90))
            .priority(2)
            .type("SOFTWARE")
            .status(1)
            .build();
    }
    
    // ==================== 分页测试数据 ====================
    
    public static <T> com.promanage.common.result.PageResult<T> createTestPageResult(List<T> data) {
        return createTestPageResult(data, 1, 20, data.size());
    }
    
    public static <T> com.promanage.common.result.PageResult<T> createTestPageResult(
            List<T> data, int page, int pageSize, long total) {
        com.promanage.common.result.PageResult<T> pageResult = new com.promanage.common.result.PageResult<>();
        pageResult.setList(data);
        pageResult.setPage(page);
        pageResult.setPageSize(pageSize);
        pageResult.setTotal(total);
        pageResult.setTotalPages((int) Math.ceil((double) total / pageSize));
        return pageResult;
    }
    
    // ==================== 工具方法 ====================
    
    /**
     * 创建指定数量的测试用户列表
     */
    public static List<User> createTestUsers(int count) {
        return java.util.stream.IntStream.rangeClosed(1, count)
            .mapToObj(i -> createTestUser((long) i, "user" + i, "user" + i + "@example.com"))
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 创建指定数量的测试项目列表
     */
    public static List<Project> createTestProjects(int count) {
        return java.util.stream.IntStream.rangeClosed(1, count)
            .mapToObj(i -> createTestProject((long) i, "Test Project " + i))
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 创建指定数量的测试组织列表
     */
    public static List<Organization> createTestOrganizations(int count) {
        return java.util.stream.IntStream.rangeClosed(1, count)
            .mapToObj(i -> createTestOrganization((long) i, "Test Organization " + i))
            .collect(java.util.stream.Collectors.toList());
    }
}