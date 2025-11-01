package com.promanage.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promanage.common.entity.User;
import com.promanage.common.result.PageResult;
import com.promanage.service.entity.ChangeRequest;
import com.promanage.service.entity.ChangeRequestApproval;
import com.promanage.service.entity.ChangeRequestImpact;
import com.promanage.service.service.IChangeRequestService;
import com.promanage.service.service.IUserService;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;

import java.util.HashMap;
import java.util.Map;

/**
 * ChangeRequestController 单元测试
 *
 * <p>测试N+1查询重构后的批量查询逻辑和转换方法
 *
 * @author ProManage Team
 * @date 2025-10-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ChangeRequestController 单元测试")
class ChangeRequestControllerTest {

  @Mock private IChangeRequestService changeRequestService;

  @Mock private IUserService userService;

  @InjectMocks private ChangeRequestController controller;

  private User user1;
  private User user2;
  private User user3;

  @BeforeEach
  void setUp() {
    user1 = createUser(1L, "User 1", "user1@example.com");
    user2 = createUser(2L, "User 2", "user2@example.com");
    user3 = createUser(3L, "User 3", "user3@example.com");
    
    // 设置SecurityContext以便SecurityUtils.getCurrentUserId()能正常工作
    setSecurityContext(1L);
  }

  @org.junit.jupiter.api.AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void setSecurityContext(Long userId) {
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken("testUser", "password");
    Map<String, Object> details = new HashMap<>();
    details.put("userId", userId);
    authentication.setDetails(details);
    context.setAuthentication(authentication);
    SecurityContextHolder.setContext(context);
  }

  @Test
  @DisplayName("collectUserIds - 应该正确收集所有用户ID")
  void testCollectUserIds_shouldCollectAllUserIds() {
    // Given
    ChangeRequest cr1 = createChangeRequest(1L, 1L, 2L, 3L);
    ChangeRequest cr2 = createChangeRequest(2L, 2L, 3L, null);
    ChangeRequest cr3 = createChangeRequest(3L, 1L, null, null);
    List<ChangeRequest> changeRequests = Arrays.asList(cr1, cr2, cr3);

    // When
    Set<Long> userIds = controller.collectUserIds(changeRequests);

    // Then
    assertThat(userIds).hasSize(3);
    assertThat(userIds).contains(1L, 2L, 3L);
  }

  @Test
  @DisplayName("collectUserIds - 空列表应该返回空集合")
  void testCollectUserIds_withEmptyList_shouldReturnEmptySet() {
    // Given
    List<ChangeRequest> changeRequests = Collections.emptyList();

    // When
    Set<Long> userIds = controller.collectUserIds(changeRequests);

    // Then
    assertThat(userIds).isEmpty();
  }

  @Test
  @DisplayName("collectUserIds - null列表应该返回空集合")
  void testCollectUserIds_withNullList_shouldReturnEmptySet() {
    // When
    Set<Long> userIds = controller.collectUserIds(null);

    // Then
    assertThat(userIds).isEmpty();
  }

  @Test
  @DisplayName("collectUserIdsFromImpacts - 应该正确收集验证人ID")
  void testCollectUserIdsFromImpacts_shouldCollectVerifierIds() {
    // Given
    ChangeRequestImpact impact1 = createImpact(1L, 1L);
    ChangeRequestImpact impact2 = createImpact(2L, 2L);
    ChangeRequestImpact impact3 = createImpact(3L, null);
    List<ChangeRequestImpact> impacts = Arrays.asList(impact1, impact2, impact3);

    // When
    Set<Long> userIds = controller.collectUserIdsFromImpacts(impacts);

    // Then
    assertThat(userIds).hasSize(2);
    assertThat(userIds).contains(1L, 2L);
  }

  @Test
  @DisplayName("collectUserIdsFromApprovals - 应该正确收集审批人ID")
  void testCollectUserIdsFromApprovals_shouldCollectApproverIds() {
    // Given
    ChangeRequestApproval approval1 = createApproval(1L, 1L);
    ChangeRequestApproval approval2 = createApproval(2L, 2L);
    ChangeRequestApproval approval3 = createApproval(3L, null);
    List<ChangeRequestApproval> approvals = Arrays.asList(approval1, approval2, approval3);

    // When
    Set<Long> userIds = controller.collectUserIdsFromApprovals(approvals);

    // Then
    assertThat(userIds).hasSize(2);
    assertThat(userIds).contains(1L, 2L);
  }

  @Test
  @DisplayName("convertToChangeRequestResponse - 应该从Map获取用户信息")
  void testConvertToChangeRequestResponse_shouldUseUserMap() {
    // Given
    ChangeRequest changeRequest = createChangeRequest(1L, 1L, 2L, 3L);
    Map<Long, User> userMap = Map.of(1L, user1, 2L, user2, 3L, user3);

    // When
    var response = controller.convertToChangeRequestResponse(changeRequest, userMap);

    // Then
    assertThat(response.getRequesterName()).isEqualTo("User 1");
    assertThat(response.getAssigneeName()).isEqualTo("User 2");
    assertThat(response.getReviewerName()).isEqualTo("User 3");
  }

  @Test
  @DisplayName("convertToChangeRequestResponse - 部分用户不存在应该返回null")
  void testConvertToChangeRequestResponse_withMissingUsers_shouldReturnNull() {
    // Given
    ChangeRequest changeRequest = createChangeRequest(1L, 1L, 2L, 999L);
    Map<Long, User> userMap = Map.of(1L, user1, 2L, user2);

    // When
    var response = controller.convertToChangeRequestResponse(changeRequest, userMap);

    // Then
    assertThat(response.getRequesterName()).isEqualTo("User 1");
    assertThat(response.getAssigneeName()).isEqualTo("User 2");
    assertThat(response.getReviewerName()).isNull();
  }

  @Test
  @DisplayName("convertToImpactResponse - 应该从Map获取验证人信息")
  void testConvertToImpactResponse_shouldUseUserMap() {
    // Given
    ChangeRequestImpact impact = createImpact(1L, 1L);
    Map<Long, User> userMap = Map.of(1L, user1);

    // When
    var response = controller.convertToImpactResponse(impact, userMap);

    // Then
    assertThat(response.getVerifiedBy()).isEqualTo("User 1");
  }

  @Test
  @DisplayName("convertToApprovalResponse - 应该从Map获取审批人信息")
  void testConvertToApprovalResponse_shouldUseUserMap() {
    // Given
    ChangeRequestApproval approval = createApproval(1L, 1L);
    approval.setApproverName("Fallback Name");
    Map<Long, User> userMap = Map.of(1L, user1);

    // When
    var response = controller.convertToApprovalResponse(approval, userMap);

    // Then
    assertThat(response.getApproverName()).isEqualTo("User 1");
    assertThat(response.getApproverAvatar()).isEqualTo(user1.getAvatar());
  }

  @Test
  @DisplayName("getChangeRequests - 应该使用批量查询而非单个查询")
  void testGetChangeRequests_shouldUseBatchQuery() {
    // Given
    ChangeRequest cr1 = createChangeRequest(1L, 1L, 2L, 3L);
    ChangeRequest cr2 = createChangeRequest(2L, 2L, 3L, null);
    List<ChangeRequest> changeRequests = Arrays.asList(cr1, cr2);
    PageResult<ChangeRequest> pageResult =
        PageResult.of(changeRequests, 2L, 1, 20);

    when(changeRequestService.hasChangeRequestViewPermission(anyLong(), anyLong()))
        .thenReturn(true);
    when(changeRequestService.listChangeRequests(
            anyLong(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(pageResult);
    when(userService.getByIds(anyList()))
        .thenReturn(Map.of(1L, user1, 2L, user2, 3L, user3));
    when(changeRequestService.getChangeRequestCommentCount(anyLong())).thenReturn(0);
    when(changeRequestService.getChangeRequestImpactCount(anyLong())).thenReturn(0);

    // When
    var result = controller.getChangeRequests(1L, 1, 20, null, null, null, null, null, null, null, null);

    // Then
    verify(userService, times(1)).getByIds(anyList());
    verify(userService, never()).getById(anyLong());
    assertThat(result.getCode()).isEqualTo(200);
  }

  @Test
  @DisplayName("getChangeRequests - 空列表应该不调用批量查询")
  void testGetChangeRequests_withEmptyList_shouldNotCallBatchQuery() {
    // Given
    PageResult<ChangeRequest> pageResult = PageResult.of(Collections.emptyList(), 0L, 1, 20);

    when(changeRequestService.hasChangeRequestViewPermission(anyLong(), anyLong()))
        .thenReturn(true);
    when(changeRequestService.listChangeRequests(
            anyLong(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(pageResult);

    // When
    controller.getChangeRequests(1L, 1, 20, null, null, null, null, null, null, null, null);

    // Then
    verify(userService, never()).getByIds(anyList());
    verify(userService, never()).getById(anyLong());
  }

  // 辅助方法

  private User createUser(Long id, String realName, String email) {
    User user = new User();
    user.setId(id);
    user.setRealName(realName);
    user.setEmail(email);
    user.setAvatar("avatar" + id + ".jpg");
    return user;
  }

  private ChangeRequest createChangeRequest(Long id, Long requesterId, Long assigneeId, Long reviewerId) {
    ChangeRequest cr = new ChangeRequest();
    cr.setId(id);
    cr.setTitle("Change Request " + id);
    cr.setRequesterId(requesterId);
    cr.setAssigneeId(assigneeId);
    cr.setReviewerId(reviewerId);
    return cr;
  }

  private ChangeRequestImpact createImpact(Long id, Long verifiedBy) {
    ChangeRequestImpact impact = new ChangeRequestImpact();
    impact.setId(id);
    impact.setVerifiedBy(verifiedBy);
    return impact;
  }

  private ChangeRequestApproval createApproval(Long id, Long approverId) {
    ChangeRequestApproval approval = new ChangeRequestApproval();
    approval.setId(id);
    approval.setApproverId(approverId);
    return approval;
  }
}

