package com.promanage.service.converter;

import com.promanage.service.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * 用户对象转换器
 * <p>
 * 使用MapStruct实现User实体和DTO之间的转换
 * </p>
 *
 * @author ProManage Team
 * @date 2025-09-30
 */
@Mapper(componentModel = "spring")
public interface UserConverter {

    /**
     * 实体转实体 (用于复制属性)
     *
     * @param source 源User对象
     * @return 新User对象
     */
    User toEntity(User source);

    /**
     * 批量转换实体列表
     *
     * @param users 用户列表
     * @return 用户列表
     */
    List<User> toEntityList(List<User> users);

    /**
     * 更新实体属性 (将source的非空属性复制到target)
     *
     * @param source 源User对象
     * @param target 目标User对象
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "password", ignore = true) // 密码不允许通过此方法更新
    void updateEntity(User source, @MappingTarget User target);
}