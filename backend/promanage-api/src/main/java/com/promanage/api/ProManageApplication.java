package com.promanage.api;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * ProManage 项目管理系统主应用类
 *
 * <p>这是Spring Boot应用程序的入口点，负责启动整个ProManage系统。 该系统提供了完整的项目管理和文档管理功能，支持多用户协作、 
 * 权限控制、版本管理等企业级特性。
 *
 * <p>主要功能模块：
 *
 * <ul>
 *   <li>用户认证与授权 - 基于JWT的安全认证机制
 *   <li>项目管理 - 项目创建、更新、成员管理
 *   <li>文档管理 - 文档CRUD、版本控制、全文搜索
 *   <li>角色权限管理 - 灵活的RBAC权限模型
 *   <li>文件存储 - 支持MinIO/S3对象存储
 *   <li>缓存管理 - Redis缓存提升性能
 *   <li>全文搜索 - Elasticsearch文档搜索
 * </ul>
 *
 * <p>配置说明：
 *
 * <ul>
 *   <li>@SpringBootApplication - 启用Spring Boot自动配置
 *   <li>@MapperScan - 扫描MyBatis Mapper接口
 *   <li>@EnableCaching - 启用Spring Cache抽象
 *   <li>@EnableAsync - 启用异步方法执行支持
 *   <li>@EnableTransactionManagement - 启用声明式事务管理
 * </ul>
 *
 * <p>扫描包范围：com.promanage
 *
 * <ul>
 *   <li>com.promanage.api - API控制器和DTO
 *   <li>com.promanage.service - 业务逻辑层
 *   <li>com.promanage.infrastructure - 基础设施配置
 *   <li>com.promanage.common - 公共工具和常量
 * </ul>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-09-30
 */
@SpringBootApplication(
    scanBasePackages = {
      "com.promanage.api",
      "com.promanage.service",
      "com.promanage.infrastructure",
      "com.promanage.common"
    },
    exclude = {
      org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration.class,
      org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration
          .class,
      org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration
          .class,
      org.springframework.boot.autoconfigure.data.elasticsearch
          .ElasticsearchRepositoriesAutoConfiguration.class
    })
@MapperScan("com.promanage.service.mapper")
@EnableCaching
@EnableAsync
@EnableTransactionManagement
public class ProManageApplication {

  private static final Logger log = LoggerFactory.getLogger(ProManageApplication.class);

  /**
   * 应用程序主入口方法
   *
   * <p>启动Spring Boot应用程序，初始化所有必要的Bean和配置
   *
   * @param args 命令行参数，可用于传递启动配置 例如：--spring.profiles.active=prod
   */
  public static void main(String[] args) {
    // 打印启动Banner
    printStartupBanner();

    // 启动Spring Boot应用
    SpringApplication.run(ProManageApplication.class, args);

    // 打印启动成功信息
    printStartupSuccess();
  }

  /**
   * 打印启动Banner
   *
   * <p>在应用启动时打印欢迎信息
   */
  private static void printStartupBanner() {
    log.info("""

        ================================================
           _____           __  __
          |  __ \\         |  \\/  |
          | |__) | __ ___ | \\  / | __ _ _ __   __ _  __ _  ___
          |  ___/ '__/ _ \\| |\\/| |/ _` | '_ \\ / _` |/ _` |/ _ \\
          | |   | | | (_) | |  | | (_| | | | | (_| | (_| |  __/
          |_|   |_|  \\___/|_|  |_|\\__,_|_| |_|\\__,_|\\__, |\\___|
                                                     __/ |
                                                    |___/
           Project & Document Management System
           Version: 1.0.0-SNAPSHOT
           Spring Boot: 3.2.10
        ================================================
        """);
  }

  /**
   * 打印启动成功信息
   *
   * <p>在应用成功启动后打印访问地址
   */
  private static void printStartupSuccess() {
    log.info(
        "\n"
            + "================================================\n"
            + "  ProManage Application Started Successfully!\n"
            + "  \n"
            + "  Application URL: http://localhost:8080\n"
            + "  Swagger UI: http://localhost:8080/swagger-ui.html\n"
            + "  API Docs: http://localhost:8080/v3/api-docs\n"
            + "  Actuator Health: http://localhost:8080/actuator/health\n"
            + "  \n"
            + "  Ready to accept requests...\n"
            + "================================================\n");
  }
}