package com.promanage.dto.mapper;

import com.promanage.common.entity.Organization;
import com.promanage.dto.CreateOrganizationRequestDTO;
import com.promanage.dto.OrganizationDTO;
import com.promanage.dto.UpdateOrganizationRequestDTO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * 组织实体与DTO转换器
 *
 * <p>使用MapStruct实现Organization实体与DTO之间的转换
 *
 * @author ProManage Team
 * @since 2025-10-12
 */
@Mapper(
    componentModel = "spring",
    unmappedSourcePolicy = ReportingPolicy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface OrganizationMapper {
    OrganizationMapper INSTANCE = Mappers.getMapper(OrganizationMapper.class);

    /** 实体转DTO */
    @Mapping(source = "createTime", target = "createdAt")
    @Mapping(source = "updateTime", target = "updatedAt")
    OrganizationDTO toDto(Organization organization);

    /** 创建请求DTO转实体 */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "subscriptionPlan", constant = "FREE")
    @Mapping(target = "status", ignore = true) // CreateOrganizationRequestDTO不包含status字段
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "creatorId", ignore = true)
    @Mapping(target = "updaterId", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "subscriptionExpiresAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "settings", ignore = true)
    Organization toEntity(CreateOrganizationRequestDTO request);

    /** 更新请求DTO转实体 */
    @BeanMapping(
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
    )
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "subscriptionPlan", ignore = true)
    @Mapping(target = "status", ignore = true) // UpdateOrganizationRequestDTO不包含status字段
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "creatorId", ignore = true)
    @Mapping(target = "updaterId", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "subscriptionExpiresAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "settings", ignore = true)
    void updateEntityFromDto(
        UpdateOrganizationRequestDTO request,
        @MappingTarget Organization organization
    );
}
