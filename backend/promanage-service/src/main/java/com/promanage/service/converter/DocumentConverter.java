package com.promanage.service.converter;

import com.promanage.service.entity.Document;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * 文档对象转换器
 * <p>
 * 使用MapStruct实现Document实体和DTO之间的转换
 * </p>
 *
 * @author ProManage Team
 * @date 2025-09-30
 */
@Mapper(componentModel = "spring")
public interface DocumentConverter {

    /**
     * 实体转实体 (用于复制属性)
     *
     * @param source 源Document对象
     * @return 新Document对象
     */
    Document toEntity(Document source);

    /**
     * 批量转换实体列表
     *
     * @param documents 文档列表
     * @return 文档列表
     */
    List<Document> toEntityList(List<Document> documents);

    /**
     * 更新实体属性 (将source的非空属性复制到target)
     *
     * @param source 源Document对象
     * @param target 目标Document对象
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "currentVersion", ignore = true)
    void updateEntity(Document source, @MappingTarget Document target);
}