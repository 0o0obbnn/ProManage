package com.promanage.service.security;

import com.promanage.infrastructure.security.SecurityUtils;
import com.promanage.service.IProjectService; // Corrected import
import com.promanage.service.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Centralized permission checking service for use in Spring Security expressions.
 *
 * @author ProManage Team (Remediation)
 * @since 2025-10-20
 */
@Service("permissionCheck") // The bean name used in @PreAuthorize SpEL expressions
@RequiredArgsConstructor
public class PermissionCheckService {

    private final IUserService userService;
    private final IProjectService projectService; // Injected IProjectService

    /**
     * Checks if the current authenticated user has a specific permission.
     *
     * @param permissionCode The permission string to check (e.g., "document:view").
     * @return true if the user has the permission, false otherwise.
     */
    public boolean hasPermission(String permissionCode) {
        // Get the current user's ID from the security context.
        Long userId = SecurityUtils.getCurrentUserId().orElse(null);
        if (userId == null) {
            return false; // No authenticated user, no permission.
        }
        // Delegate the check to the existing user service.
        return userService.hasPermission(userId, permissionCode);
    }

    /**
     * Example of a more specific, resource-based check. Checks if the current user is a member of a
     * specific project.
     *
     * @param projectId The ID of the project to check.
     * @return true if the user is a member, false otherwise.
     */
    public boolean isProjectMember(Long projectId) {
        Long userId = SecurityUtils.getCurrentUserId().orElse(null);
        if (userId == null || projectId == null) {
            return false;
        }
        // Delegate to the actual implementation in ProjectService.
        return projectService.isMember(projectId, userId);
    }
}
