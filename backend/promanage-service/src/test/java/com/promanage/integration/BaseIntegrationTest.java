package com.promanage.integration;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.promanage.service.mapper.PermissionMapper;
import com.promanage.service.mapper.RolePermissionMapper;
import com.promanage.service.mapper.UserMapper;
import com.promanage.service.mapper.UserRoleMapper;

/**
 * 集成测试基类
 *
 * <p>提供集成测试的基础配置和通用方法
 *
 * @author ProManage Team
 * @date 2025-10-08
 */
@SpringBootTest(classes = {
    com.promanage.service.config.TestConfiguration.class
})
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

  @Autowired protected UserMapper userMapper;

  @Autowired protected UserRoleMapper userRoleMapper;

  @Autowired protected RolePermissionMapper rolePermissionMapper;

  @Autowired protected PermissionMapper permissionMapper;

  @BeforeEach
  void setUp() {
    // 清理测试数据
    cleanTestData();
  }

  /** 清理测试数据 */
  protected void cleanTestData() {
    // 按依赖关系逆序删除
    rolePermissionMapper.delete(null);
    userRoleMapper.delete(null);
    permissionMapper.delete(null);
    userMapper.delete(null);
  }
}
