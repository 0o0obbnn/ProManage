package com.promanage.api.controller;

import com.promanage.common.domain.Result;
import com.promanage.common.result.PageResult;
import com.promanage.infrastructure.security.SecurityUtils;
import com.promanage.service.IOrganizationService;
import com.promanage.dto.OrganizationSettingsDTO;
import com.promanage.dto.OrganizationDTO;
import com.promanage.dto.CreateOrganizationRequestDTO;
import com.promanage.dto.OrganizationMemberDTO;
import com.promanage.dto.UpdateOrganizationRequestDTO;
import com.promanage.dto.mapper.OrganizationMapper;
import com.promanage.common.entity.Organization;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

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
    private final OrganizationMapper organizationMapper;

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
    public Result<PageResult<OrganizationDTO>> listOrganizations(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "激活状态") @RequestParam(required = false) Boolean isActive) {
        
        log.info("获取组织列表, page={}, pageSize={}, keyword={}, isActive={}", page, pageSize, keyword, isActive);
        Long currentUserId = requireCurrentUserId();

        PageResult<Organization> result = organizationService.listOrganizations(currentUserId, page, pageSize, keyword, isActive);

        List<Organization> organizations = result.getList() == null
                ? Collections.emptyList()
                : result.getList();

        List<OrganizationDTO> dtoList = organizations.stream()
                .map(organizationMapper::toDto)
                .collect(Collectors.toList());

        PageResult<OrganizationDTO> dtoResult = PageResult.<OrganizationDTO>builder()
                .list(dtoList)
                .total(result.getTotal())
                .page(result.getPage())
                .pageSize(result.getPageSize())
                .totalPages(result.getTotalPages())
                .hasNext(Boolean.TRUE.equals(result.getHasNext()))
                .hasPrevious(Boolean.TRUE.equals(result.getHasPrevious()))
                .build();

        return Result.success(dtoResult);
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
    public Result<OrganizationDTO> getOrganization(
            @Parameter(description = "组织ID") @PathVariable @NotNull Long id) {

        log.info("获取组织详情, id={}", id);

        Long currentUserId = requireCurrentUserId();

        Organization organization = organizationService.getOrganizationById(id, currentUserId);
        
        OrganizationDTO organizationDTO = organizationMapper.toDto(organization);
        return Result.success(organizationDTO);
    }

    /**
     * 创建组织
     *
     * @param request 创建组织请求
     * @return 创建的组织
     */
    @PostMapping
    @Operation(summary = "创建组织", description = "创建新的组织")
    @PreAuthorize("hasAuthority('ORGANIZATION_CREATE')")
    public Result<OrganizationDTO> createOrganization(
            @Parameter(description = "组织信息") @Valid @RequestBody CreateOrganizationRequestDTO request) {
        
        log.info("创建组织, name={}", request.getName());
        Long currentUserId = requireCurrentUserId();

        // 转换DTO为实体
        Organization organization = organizationMapper.toEntity(request);
        Organization created = organizationService.createOrganization(organization, currentUserId);
        
        // 转换实体为DTO
        OrganizationDTO response = organizationMapper.toDto(created);
        return Result.success(response);
    }

    /**
     * 更新组织
     *
     * @param id      组织ID
     * @param request 更新组织请求
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新组织", description = "更新组织信息")
    @PreAuthorize("hasAuthority('ORGANIZATION_UPDATE')")
    public Result<OrganizationDTO> updateOrganization(
            @Parameter(description = "组织ID") @PathVariable @NotNull Long id,
            @Parameter(description = "组织信息") @Valid @RequestBody UpdateOrganizationRequestDTO request) {
        
        log.info("更新组织, id={}", id);
        Long currentUserId = requireCurrentUserId();

        // 先获取现有组织
        Organization existingOrganization = organizationService.getOrganizationById(id, currentUserId);
        
        // 更新实体
        organizationMapper.updateEntityFromDto(request, existingOrganization);
        existingOrganization.setId(id);
        Organization updated = organizationService.updateOrganization(existingOrganization, currentUserId);
        
        // 转换为DTO
        OrganizationDTO response = organizationMapper.toDto(updated);
        return Result.success(response);
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
        Long currentUserId = requireCurrentUserId();
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
    public Result<PageResult<OrganizationMemberDTO>> getOrganizationMembers(
            @Parameter(description = "组织ID") @PathVariable @NotNull Long id,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize) {

        log.info("获取组织成员列表, id={}, page={}, pageSize={}", id, page, pageSize);

        Long currentUserId = requireCurrentUserId();

        PageResult<OrganizationMemberDTO> members =
                organizationService.listOrganizationMembers(id, currentUserId, page, pageSize);
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
        Long currentUserId = requireCurrentUserId();
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

        Long currentUserId = requireCurrentUserId();
        organizationService.deactivateOrganization(id, currentUserId);
        return Result.success();
    }

    /**
     * 获取组织设置
     *
     * @param id 组织ID
     * @return 组织设置
     */
    @GetMapping("/{id}/settings")
    @Operation(summary = "获取组织设置", description = "获取指定组织的配置设置")
    @PreAuthorize("hasAuthority('ORGANIZATION_VIEW')")
    public Result<OrganizationSettingsDTO> getOrganizationSettings(
            @Parameter(description = "组织ID") @PathVariable @NotNull Long id) {

        log.info("获取组织设置, id={}", id);

        Long currentUserId = requireCurrentUserId();

        OrganizationSettingsDTO settings = organizationService.getOrganizationSettings(id, currentUserId);
        return Result.success(settings);
    }

    /**
     * 更新组织设置
     *
     * @param id       组织ID
     * @param settings 组织设置
     * @return 更新后的设置
     */
    @PutMapping("/{id}/settings")
    @Operation(summary = "更新组织设置", description = "更新指定组织的配置设置")
    @PreAuthorize("hasAuthority('ORGANIZATION_UPDATE')")
    public Result<OrganizationSettingsDTO> updateOrganizationSettings(
            @Parameter(description = "组织ID") @PathVariable @NotNull Long id,
            @Parameter(description = "组织设置") @Valid @RequestBody OrganizationSettingsDTO settings) {

        log.info("更新组织设置, id={}", id);

        Long currentUserId = requireCurrentUserId();

        OrganizationSettingsDTO updated = organizationService.updateOrganizationSettings(id, settings, currentUserId);
        return Result.success(updated);
    }

    private Long requireCurrentUserId() {
        return SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("用户未登录"));
    }
}
