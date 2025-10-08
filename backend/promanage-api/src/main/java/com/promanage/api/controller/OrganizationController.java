package com.promanage.api.controller;

import com.promanage.common.domain.PageResult;
import com.promanage.common.domain.Result;
import com.promanage.infrastructure.security.SecurityUtils;
import com.promanage.service.IOrganizationService;
import com.promanage.service.entity.Organization;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 组织管理控制器
 * <p>
 * 提供组织管理相关的REST API接口
 * </p>
 *
 * @author ProManage Team
 * @since 2025-10-07
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
@Tag(name = "组织管理", description = "组织管理相关接口")
public class OrganizationController {

    private final IOrganizationService organizationService;

    /**
     * 获取组织列表
     *
     * @param page     页码
     * @param pageSize 每页数量
     * @param keyword  搜索关键词
     * @param isActive 激活状态
     * @return 组织列表
     */
    @GetMapping
    @Operation(summary = "获取组织列表", description = "分页获取组织列表，支持搜索和状态筛选")
    @PreAuthorize("hasAuthority('ORGANIZATION_VIEW')")
    public Result<PageResult<Organization>> listOrganizations(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "激活状态") @RequestParam(required = false) Boolean isActive) {
        
        log.info("获取组织列表, page={}, pageSize={}, keyword={}, isActive={}", page, pageSize, keyword, isActive);
        
        PageResult<Organization> result = organizationService.listOrganizations(page, pageSize, keyword, isActive);
        return Result.success(result);
    }

    /**
     * 获取组织详情
     *
     * @param id 组织ID
     * @return 组织详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取组织详情", description = "根据ID获取组织详细信息")
    @PreAuthorize("hasAuthority('ORGANIZATION_VIEW')")
    public Result<Organization> getOrganization(
            @Parameter(description = "组织ID") @PathVariable @NotNull Long id) {
        
        log.info("获取组织详情, id={}", id);
        
        Organization organization = organizationService.getById(id);
        if (organization == null) {
            return Result.error("组织不存在");
        }
        
        return Result.success(organization);
    }

    /**
     * 创建组织
     *
     * @param organization 组织信息
     * @return 创建的组织
     */
    @PostMapping
    @Operation(summary = "创建组织", description = "创建新的组织")
    @PreAuthorize("hasAuthority('ORGANIZATION_CREATE')")
    public Result<Organization> createOrganization(
            @Parameter(description = "组织信息") @Valid @RequestBody Organization organization) {
        
        log.info("创建组织, name={}", organization.getName());
        
        Long currentUserId = SecurityUtils.getCurrentUserId().orElse(null);
        if (currentUserId == null) {
            return Result.error("用户未登录");
        }
        Organization created = organizationService.createOrganization(organization, currentUserId);
        return Result.success(created);
    }

    /**
     * 更新组织
     *
     * @param id           组织ID
     * @param organization 组织信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新组织", description = "更新组织信息")
    @PreAuthorize("hasAuthority('ORGANIZATION_UPDATE')")
    public Result<Organization> updateOrganization(
            @Parameter(description = "组织ID") @PathVariable @NotNull Long id,
            @Parameter(description = "组织信息") @Valid @RequestBody Organization organization) {
        
        log.info("更新组织, id={}", id);
        
        organization.setId(id);
        Long currentUserId = SecurityUtils.getCurrentUserId().orElse(null);
        if (currentUserId == null) {
            return Result.error("用户未登录");
        }
        Organization updated = organizationService.updateOrganization(organization, currentUserId);
        return Result.success(updated);
    }

    /**
     * 删除组织
     *
     * @param id 组织ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除组织", description = "删除指定组织（逻辑删除）")
    @PreAuthorize("hasAuthority('ORGANIZATION_DELETE')")
    public Result<Void> deleteOrganization(
            @Parameter(description = "组织ID") @PathVariable @NotNull Long id) {
        
        log.info("删除组织, id={}", id);
        
        Long currentUserId = SecurityUtils.getCurrentUserId().orElse(null);
        if (currentUserId == null) {
            return Result.error("用户未登录");
        }
        organizationService.deleteOrganization(id, currentUserId);
        return Result.success();
    }

    /**
     * 获取组织成员列表
     *
     * @param id 组织ID
     * @return 组织成员列表
     */
    @GetMapping("/{id}/members")
    @Operation(summary = "获取组织成员", description = "获取指定组织的成员列表")
    @PreAuthorize("hasAuthority('ORGANIZATION_VIEW')")
    public Result<List<com.promanage.common.entity.User>> getOrganizationMembers(
            @Parameter(description = "组织ID") @PathVariable @NotNull Long id) {
        
        log.info("获取组织成员列表, id={}", id);
        
        // 检查组织是否存在
        Organization organization = organizationService.getById(id);
        if (organization == null) {
            return Result.error("组织不存在");
        }
        
        // 获取组织成员
        List<com.promanage.common.entity.User> members = organizationService.listUserOrganizations(id)
                .stream()
                .map(org -> {
                    com.promanage.common.entity.User user = new com.promanage.common.entity.User();
                    user.setOrganizationId(org.getId());
                    return user;
                })
                .toList();
        
        return Result.success(members);
    }

    /**
     * 激活组织
     *
     * @param id 组织ID
     * @return 操作结果
     */
    @PostMapping("/{id}/activate")
    @Operation(summary = "激活组织", description = "激活指定组织")
    @PreAuthorize("hasAuthority('ORGANIZATION_UPDATE')")
    public Result<Void> activateOrganization(
            @Parameter(description = "组织ID") @PathVariable @NotNull Long id) {
        
        log.info("激活组织, id={}", id);
        
        Long currentUserId = SecurityUtils.getCurrentUserId().orElse(null);
        if (currentUserId == null) {
            return Result.error("用户未登录");
        }
        organizationService.activateOrganization(id, currentUserId);
        return Result.success();
    }

    /**
     * 停用组织
     *
     * @param id 组织ID
     * @return 操作结果
     */
    @PostMapping("/{id}/deactivate")
    @Operation(summary = "停用组织", description = "停用指定组织")
    @PreAuthorize("hasAuthority('ORGANIZATION_UPDATE')")
    public Result<Void> deactivateOrganization(
            @Parameter(description = "组织ID") @PathVariable @NotNull Long id) {
        
        log.info("停用组织, id={}", id);
        
        Long currentUserId = SecurityUtils.getCurrentUserId().orElse(null);
        if (currentUserId == null) {
            return Result.error("用户未登录");
        }
        organizationService.deactivateOrganization(id, currentUserId);
        return Result.success();
    }
}