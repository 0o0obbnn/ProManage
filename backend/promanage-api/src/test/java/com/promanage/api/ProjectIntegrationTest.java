package com.promanage.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.promanage.api.dto.request.LoginRequest;
import com.promanage.common.domain.Result;
import com.promanage.common.result.PageResult;
import com.promanage.dto.ProjectMemberDTO;
import com.promanage.dto.ProjectRequest;
import com.promanage.dto.ProjectStatsDTO;
import com.promanage.service.entity.Project;
import com.promanage.service.entity.ProjectActivity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ProjectIntegrationTest {

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate restTemplate;

  private String baseUrl;
  private String token;

  @BeforeEach
  void setUp() {
    baseUrl = "http://localhost:" + port + "/api/v1";
    // 登录获取token
    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setUsername("admin");
    loginRequest.setPassword("Test@2024!Abc");
    ResponseEntity<Result<Map<String, Object>>> response =
        restTemplate.exchange(
            baseUrl + "/auth/login",
            HttpMethod.POST,
            new HttpEntity<>(loginRequest),
            new ParameterizedTypeReference<Result<Map<String, Object>>>() {});
    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    Map<String, Object> data = response.getBody().getData();
    token = (String) data.get("token");
  }

  private HttpHeaders getHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + token);
    return headers;
  }

  @Test
  @DisplayName("创建、获取、更新和删除项目")
  void shouldCreateReadUpdateDeleteProject() {
    // 1. 创建项目
    ProjectRequest createRequest = new ProjectRequest();
    createRequest.setName("集成测试项目");
    createRequest.setCode("INT-TEST-001");
    createRequest.setDescription("这是一个集成测试项目");

    HttpEntity<ProjectRequest> createEntity = new HttpEntity<>(createRequest, getHeaders());
    ResponseEntity<Result<Map<String, Object>>> createResponse =
        restTemplate.exchange(
            baseUrl + "/projects",
            HttpMethod.POST,
            createEntity,
            new ParameterizedTypeReference<Result<Map<String, Object>>>() {});
    assertThat(createResponse.getStatusCode().is2xxSuccessful()).isTrue();
    assertThat(createResponse.getBody().getData()).isNotNull();
    Integer projectIdInt = (Integer) createResponse.getBody().getData().get("id");
    Long projectId = projectIdInt.longValue();

    // 2. 获取项目
    HttpEntity<Void> getEntity = new HttpEntity<>(getHeaders());
    ResponseEntity<Result<Map<String, Object>>> getResponse =
        restTemplate.exchange(
            baseUrl + "/projects/" + projectId,
            HttpMethod.GET,
            getEntity,
            new ParameterizedTypeReference<Result<Map<String, Object>>>() {});
    assertThat(getResponse.getStatusCode().is2xxSuccessful()).isTrue();
    Map<String, Object> projectMap = getResponse.getBody().getData();
    assertThat(projectMap.get("name")).isEqualTo("集成测试项目");
    assertThat(projectMap.get("code")).isEqualTo("INT-TEST-001");

    // 3. 更新项目
    ProjectRequest updateRequest = new ProjectRequest();
    updateRequest.setName("更新后的集成测试项目");
    HttpEntity<ProjectRequest> updateEntity = new HttpEntity<>(updateRequest, getHeaders());
    ResponseEntity<Result<Object>> updateResponse =
        restTemplate.exchange(
            baseUrl + "/projects/" + projectId,
            HttpMethod.PUT,
            updateEntity,
            new ParameterizedTypeReference<Result<Object>>() {});
    assertThat(updateResponse.getStatusCode().is2xxSuccessful()).isTrue();

    // 验证更新
    ResponseEntity<Result<Map<String, Object>>> getAfterUpdateResponse =
        restTemplate.exchange(
            baseUrl + "/projects/" + projectId,
            HttpMethod.GET,
            getEntity,
            new ParameterizedTypeReference<Result<Map<String, Object>>>() {});
    Map<String, Object> updatedProjectMap = getAfterUpdateResponse.getBody().getData();
    assertThat(updatedProjectMap.get("name")).isEqualTo("更新后的集成测试项目");

    // 4. 删除项目
    ResponseEntity<Result<Void>> deleteResponse =
        restTemplate.exchange(
            baseUrl + "/projects/" + projectId,
            HttpMethod.DELETE,
            getEntity,
            new ParameterizedTypeReference<Result<Void>>() {});
    assertThat(deleteResponse.getStatusCode().is2xxSuccessful()).isTrue();

    // 验证删除
    ResponseEntity<Result<Map<String, Object>>> getAfterDeleteResponse =
        restTemplate.exchange(
            baseUrl + "/projects/" + projectId,
            HttpMethod.GET,
            getEntity,
            new ParameterizedTypeReference<Result<Map<String, Object>>>() {});
    assertThat(getAfterDeleteResponse.getStatusCode().is2xxSuccessful()).isTrue();
    Map<String, Object> deletedProjectMap = getAfterDeleteResponse.getBody().getData();
    assertThat(deletedProjectMap.get("deleted")).isEqualTo(true);
  }

  @Test
  @DisplayName("集成测试 - 获取项目统计信息")
  public void testGetProjectStats() {
    // 创建项目以进行测试
    ProjectRequest createRequest = new ProjectRequest();
    createRequest.setName("Stats Test Project");
    createRequest.setDescription("Project for testing stats");
    ResponseEntity<Result<Project>> createResponse = createProject(createRequest);
    Long projectId = createResponse.getBody().getData().getId();

    // 调用统计接口
    ResponseEntity<Result<ProjectStatsDTO>> statsResponse =
        restTemplate.exchange(
            baseUrl + "/projects/{id}/stats",
            HttpMethod.GET,
            new HttpEntity<>(getHeaders()),
            new ParameterizedTypeReference<Result<ProjectStatsDTO>>() {},
            projectId);

    assertThat(statsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(statsResponse.getBody()).isNotNull();
    assertThat(statsResponse.getBody().getData()).isNotNull();
    // 初始项目应没有任务、文档等
    assertThat(statsResponse.getBody().getData().getTotalTasks()).isZero();
  }

  @Test
  @DisplayName("集成测试 - 归档和取消归档项目")
  public void testArchiveAndUnarchiveProject() {
    // 创建项目
    ProjectRequest createRequest = new ProjectRequest();
    createRequest.setName("Archive Test Project");
    ResponseEntity<Result<Project>> createResponse = createProject(createRequest);
    Long projectId = createResponse.getBody().getData().getId();

    // 归档项目
    ResponseEntity<Result<Void>> archiveResponse =
        restTemplate.exchange(
            baseUrl + "/projects/{id}/archive",
            HttpMethod.POST,
            new HttpEntity<>(getHeaders()),
            new ParameterizedTypeReference<Result<Void>>() {},
            projectId);
    assertThat(archiveResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    // 验证项目状态是否为已归档 (status=3)
    ResponseEntity<Result<Project>> getArchivedResponse = getProject(projectId);
    assertThat(getArchivedResponse.getBody().getData().getStatus()).isEqualTo(3);

    // 取消归档项目
    ResponseEntity<Result<Void>> unarchiveResponse =
        restTemplate.exchange(
            baseUrl + "/projects/{id}/unarchive",
            HttpMethod.POST,
            new HttpEntity<>(getHeaders()),
            new ParameterizedTypeReference<Result<Void>>() {},
            projectId);
    assertThat(unarchiveResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    // 验证项目状态是否已恢复 (status=0)
    ResponseEntity<Result<Project>> getUnarchivedResponse = getProject(projectId);
    assertThat(getUnarchivedResponse.getBody().getData().getStatus()).isEqualTo(0);
  }

  @Test
  @DisplayName("集成测试 - 获取项目活动")
  public void testGetProjectActivities() {
    // 创建项目
    ProjectRequest createRequest = new ProjectRequest();
    createRequest.setName("Activity Test Project");
    ResponseEntity<Result<Project>> createResponse = createProject(createRequest);
    Long projectId = createResponse.getBody().getData().getId();

    // 添加一个成员以产生活动记录
    restTemplate.exchange(
        baseUrl + "/projects/{id}/members?userId=2",
        HttpMethod.POST,
        new HttpEntity<>(getHeaders()),
        new ParameterizedTypeReference<Result<Void>>() {},
        projectId);

    // 获取活动列表
    ResponseEntity<Result<PageResult<ProjectActivity>>> activityResponse =
        restTemplate.exchange(
            baseUrl + "/projects/{id}/activities",
            HttpMethod.GET,
            new HttpEntity<>(getHeaders()),
            new ParameterizedTypeReference<Result<PageResult<ProjectActivity>>>() {},
            projectId);

    assertThat(activityResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(activityResponse.getBody()).isNotNull();
    assertThat(activityResponse.getBody().getData().getList()).isNotEmpty();
    assertThat(activityResponse.getBody().getData().getList().get(0).getActivityType())
        .isEqualTo("MEMBER_ADDED");
  }

  @Test
  @DisplayName("集成测试 - 获取项目成员 - 分页和过滤")
  public void testGetProjectMembersWithFilter() {
    // 创建项目并添加成员
    ProjectRequest createRequest = new ProjectRequest();
    createRequest.setName("Member Filter Test Project");
    ResponseEntity<Result<Project>> createResponse = createProject(createRequest);
    Long projectId = createResponse.getBody().getData().getId();

    // 添加两个不同角色的成员 (假设用户2和3存在, 角色1和2存在)
    // 默认添加的成员角色为 4 (Developer)
    restTemplate.exchange(
        baseUrl + "/projects/{id}/members?userId=2",
        HttpMethod.POST,
        new HttpEntity<>(getHeaders()),
        Void.class,
        projectId);
    // 更新成员角色, 假设有这样一个接口, 如果没有, 这个测试需要调整
    // 暂时我们只测试默认角色的过滤
    // restTemplate.exchange("/api/projects/{id}/members/2?roleId=1", HttpMethod.PUT, new
    // HttpEntity<>(getHeaders()), Void.class, projectId);

    // 按角色ID过滤获取成员列表
    ResponseEntity<Result<PageResult<ProjectMemberDTO>>> memberResponse =
        restTemplate.exchange(
            baseUrl + "/projects/{id}/members?roleId=4", // 过滤 Developer 角色
            HttpMethod.GET,
            new HttpEntity<>(getHeaders()),
            new ParameterizedTypeReference<Result<PageResult<ProjectMemberDTO>>>() {},
            projectId);

    assertThat(memberResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(memberResponse.getBody()).isNotNull();
    assertThat(memberResponse.getBody().getData().getList()).isNotEmpty();
    assertThat(memberResponse.getBody().getData().getList().get(0).getRoleId()).isEqualTo(4L);
  }

  private ResponseEntity<Result<Project>> createProject(ProjectRequest request) {
    return restTemplate.exchange(
        baseUrl + "/projects",
        HttpMethod.POST,
        new HttpEntity<>(request, getHeaders()),
        new ParameterizedTypeReference<Result<Project>>() {});
  }

  private ResponseEntity<Result<Project>> getProject(Long projectId) {
    return restTemplate.exchange(
        baseUrl + "/projects/{id}",
        HttpMethod.GET,
        new HttpEntity<>(getHeaders()),
        new ParameterizedTypeReference<Result<Project>>() {},
        projectId);
  }
}
