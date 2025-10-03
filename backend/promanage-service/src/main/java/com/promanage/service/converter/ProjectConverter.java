package com.promanage.service.converter;

import com.promanage.service.entity.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * 项目对象转换器
 * <p>
 * 使用MapStruct实现Project实体和DTO之间的转换
 * </p>
 *
 * @author ProManage Team
 * @date 2025-09-30
 */
@Mapper(componentModel = "spring")
public interface ProjectConverter {

    /**
     * 实体转实体 (用于复制属性)
     *
     * @param source 源Project对象
     * @return 新Project对象
     */
    Project toEntity(Project source);

    /**
     * 批量转换实体列表
     *
     * @param projects 项目列表
     * @return 项目列表
     */
    List<Project> toEntityList(List<Project> projects);

    /**
     * 更新实体属性 (将source的非空属性复制到target)
     *
     * @param source 源Project对象
     * @param target 目标Project对象
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    // 项目编码字段不存在，已移除
    void updateEntity(Project source, @MappingTarget Project target);
}