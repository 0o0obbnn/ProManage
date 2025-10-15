package com.promanage.service.converter;

import com.promanage.service.entity.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

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
     * 更新实体属性 (将source的非空属性复制到target)
     *
     * @param source 源Project对象
     * @param target 目标Project对象
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(Project source, @MappingTarget Project target);
}