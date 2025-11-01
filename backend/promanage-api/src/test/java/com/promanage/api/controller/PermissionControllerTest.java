package com.promanage.api.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.promanage.service.dto.response.PermissionResponse;
import com.promanage.service.service.IPermissionManagementService;
import com.promanage.service.service.IRolePermissionService;
import com.promanage.service.service.IUserPermissionService;

/**
 * Integration tests for the {@link PermissionController}.
 *
 * <p>These tests validate the security constraints of the permission management endpoints,
 * particularly focusing on multi-tenant data isolation.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PermissionControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private IPermissionManagementService permissionManagementService;

  @MockBean private IRolePermissionService rolePermissionService;

  @MockBean private IUserPermissionService userPermissionService;

  private static final Long ORG_A_ID = 1L;
  private static final Long ORG_B_ID = 2L;

  @BeforeEach
  void setUp() {
    // Common setup can go here if needed
  }

  @Test
  @DisplayName("Admin from one organization cannot view permissions of another organization")
  @WithMockUser(
      username = "admin_org_a",
      authorities = {"permission:view"})
  void getOrganizationPermissions_WhenAdminFromDifferentOrg_ShouldReturnForbidden()
      throws Exception {
    // Arrange
    // The user is authenticated as a member of Org A (implicitly).
    // We will attempt to access permissions for Org B.
    // The service layer should have security checks that prevent this.
    // For this controller test, we assume the @PreAuthorize check delegates correctly
    // and the service layer would throw a Forbidden exception, resulting in a 403.
    // No mocking is needed as the security context should prevent the call.
    // The test setup implicitly assumes a mechanism (like a custom PermissionEvaluator or
    // service-layer check)
    // that links 'admin_org_a' to ORG_A_ID and denies access to ORG_B_ID.

    // Act & Assert
    mockMvc
        .perform(
            get("/api/v1/organizations/{organizationId}/permissions", ORG_B_ID)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Admin from an organization can view its own permissions")
  @WithMockUser(
      username = "admin_org_a",
      authorities = {"permission:view"})
  void getOrganizationPermissions_WhenAdminFromSameOrg_ShouldReturnOk() throws Exception {
    // Arrange
    // The user is from Org A and is requesting permissions for Org A.
    PermissionResponse permission = new PermissionResponse();
    permission.setId(100L);
    permission.setPermissionName("test_permission");
    permission.setOrganizationId(ORG_A_ID);

    List<PermissionResponse> permissions = Collections.singletonList(permission);

    // Mock the service to return data for ORG_A_ID
    when(permissionManagementService.listPermissions(ORG_A_ID)).thenReturn(permissions);

    // Act & Assert
    mockMvc
        .perform(
            get("/api/v1/organizations/{organizationId}/permissions", ORG_A_ID)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data", hasSize(1)))
        .andExpect(jsonPath("$.data[0].permissionName").value("test_permission"))
        .andExpect(jsonPath("$.data[0].organizationId").value(ORG_A_ID));
  }

  @Test
  @DisplayName("User without permission:view authority cannot view permissions")
  @WithMockUser(username = "user_without_permission") // No authorities granted
  void getOrganizationPermissions_WhenUserLacksPermission_ShouldReturnForbidden() throws Exception {
    // Act & Assert
    mockMvc
        .perform(
            get("/api/v1/organizations/{organizationId}/permissions", ORG_A_ID)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }
}
