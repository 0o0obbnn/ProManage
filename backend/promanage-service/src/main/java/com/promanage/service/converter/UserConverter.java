package com.promanage.service.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.promanage.common.entity.User;

/**
 * 用户对象转换器
 *
 * <p>使用MapStruct实现User实体和DTO之间的转换
 *
 * @author ProManage Team
 * @date 2025-09-30
 */
@Mapper(componentModel = "spring")
public interface UserConverter {

  /**
   * 更新实体属性 (将source的非空属性复制到target)
   *
   * @param source 源User对象
   * @param target 目标User对象
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createTime", ignore = true)
  @Mapping(target = "updateTime", ignore = true)
  @Mapping(target = "deletedAt", ignore = true)
  @Mapping(target = "password", ignore = true) // 密码不允许通过此方法更新
  void updateEntity(User source, @MappingTarget User target);
}
