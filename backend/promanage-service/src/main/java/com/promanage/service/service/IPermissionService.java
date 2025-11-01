package com.promanage.service.service;

/**
 * 权限校验服务接口
 *
 * <p>用于集中处理整个应用中的权限检查逻辑，防止业务逻辑中的权限漏洞。
 *
 * @author ProManage Team
 */
public interface IPermissionService {

  /**
   * 检查用户是否是指定组织的成员。
   *
   * @param userId 用户ID
   * @param organizationId 组织ID
   * @return 如果是成员则返回 true，否则返回 false
   */
  boolean isOrganizationMember(Long userId, Long organizationId);

  /**
   * 检查用户是否是指定项目的成员。
   *
   * @param userId 用户ID
   * @param projectId 项目ID
   * @return 如果是成员则返回 true，否则返回 false
   */
  boolean isProjectMember(Long userId, Long projectId);

  /**
   * 检查用户是否是指定项目的管理员或所有者。
   *
   * @param userId 用户ID
   * @param projectId 项目ID
   * @return 如果是管理员或所有者则返回 true，否则返回 false
   */
  boolean isProjectAdmin(Long userId, Long projectId);

  /**
   * 检查用户是否有权访问指定任务。
   *
   * <p>通常意味着用户是任务所在项目的成员。
   *
   * @param userId 用户ID
   * @param taskId 任务ID
   * @return 如果有权访问则返回 true，否则返回 false
   */
  boolean canAccessTask(Long userId, Long taskId);

  /**
   * 检查用户是否有权访问指定文档。
   *
   * <p>通常意味着用户是文档所在项目的成员。
   *
   * @param userId 用户ID
   * @param documentId 文档ID
   * @return 如果有权访问则返回 true，否则返回 false
   */
  boolean canAccessDocument(Long userId, Long documentId);

  /**
   * 检查用户是否有权操作指定通知。
   *
   * @param userId 用户ID
   * @param notificationId 通知ID
   * @return 如果有权操作则返回 true，否则返回 false
   */
  boolean canAccessNotification(Long userId, Long notificationId);

  /**
   * 检查用户是否是指定组织的管理员。
   *
   * <p>组织管理员拥有更高的权限，可以执行删除组织、更新订阅计划等敏感操作。
   *
   * @param userId 用户ID
   * @param organizationId 组织ID
   * @return 如果是管理员则返回 true，否则返回 false
   */
  boolean isOrganizationAdmin(Long userId, Long organizationId);

  /**
   * 检查用户是否可以访问指定变更请求。
   *
   * <p>用户必须是变更请求所属项目的成员。
   *
   * @param userId 用户ID
   * @param changeRequestId 变更请求ID
   * @return 如果可以访问则返回 true，否则返回 false
   */
  boolean canAccessChangeRequest(Long userId, Long changeRequestId);

  /**
   * 检查用户是否可以审批变更请求。
   *
   * <p>只有项目管理员或所有者可以审批变更请求。
   *
   * @param userId 用户ID
   * @param changeRequestId 变更请求ID
   * @return 如果可以审批则返回 true，否则返回 false
   */
  boolean canApproveChangeRequest(Long userId, Long changeRequestId);

  /**
   * 检查用户是否可以修改指定用户的信息。
   *
   * <p>规则：用户只能修改自己的信息，除非是系统超级管理员。
   *
   * @param actorId 操作者用户ID
   * @param targetUserId 目标用户ID
   * @return 如果可以修改则返回 true，否则返回 false
   */
  boolean canModifyUser(Long actorId, Long targetUserId);

  /**
   * 检查用户是否是系统超级管理员。
   *
   * <p>超级管理员拥有系统最高权限。
   *
   * @param userId 用户ID
   * @return 如果是超级管理员则返回 true，否则返回 false
   */
  boolean isSuperAdmin(Long userId);
}
