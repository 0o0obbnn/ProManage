package com.promanage.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.promanage.common.entity.User;
import com.promanage.dto.OrganizationMemberDTO;

/** 用户相关 DTO 转换器。 */
@Mapper(componentModel = "spring")
public interface UserMapper {

  @Mapping(target = "lastLoginTime", source = "lastLoginTime")
  OrganizationMemberDTO toOrganizationMember(User user);
}
