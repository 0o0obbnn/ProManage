package com.promanage.service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.promanage.service.impl.*;

/**
 * 服务配置类
 *
 * <p>用于导入新创建的服务类
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-16
 */
@Configuration
@Import({
  PermissionManagementServiceImpl.class,
  RolePermissionServiceImpl.class,
  UserPermissionServiceImpl.class,
  ProjectPermissionServiceImpl.class,
  OrganizationPermissionServiceImpl.class
})
public class ServiceConfig {}
