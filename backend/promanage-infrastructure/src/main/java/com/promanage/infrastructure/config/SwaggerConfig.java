package com.promanage.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger API文档配置
 * <p>
 * 配置OpenAPI 3.0规范，自动生成API文档
 * </p>
 *
 * @author ProManage Team
 * @date 2025-10-08
 */
@Configuration
public class SwaggerConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ProManage API")
                        .description("""
                                ProManage 项目管理系统 RESTful API 规范
                                
                                ## 功能概述
                                - 统一知识库管理
                                - 智能变更管理
                                - 任务和测试用例管理
                                - 实时通知和协作
                                - 基于角色的访问控制
                                
                                ## 认证说明
                                API 使用 JWT Bearer Token 进行认证。获取 token 后，在请求头中添加：
                                ```
                                Authorization: Bearer {your_jwt_token}
                                ```
                                
                                ## 错误处理
                                API 使用标准 HTTP 状态码，错误响应格式统一为：
                                ```json
                                {
                                  "error": {
                                    "code": "ERROR_CODE",
                                    "message": "错误描述",
                                    "details": {}
                                  }
                                }
                                ```
                                
                                ## 分页
                                列表接口支持分页，参数包括：
                                - `page`: 页码（从1开始）
                                - `size`: 每页数量（默认20，最大100）
                                - `sort`: 排序字段
                                - `order`: 排序方向（asc/desc）
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("ProManage Development Team")
                                .email("dev@promanage.com"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort + "/api/v1")
                                .description("本地开发环境"),
                        new Server()
                                .url("https://staging-api.promanage.com/v1")
                                .description("测试环境"),
                        new Server()
                                .url("https://api.promanage.com/v1")
                                .description("生产环境")
                ))
                .tags(List.of(
                        new Tag().name("认证管理").description("用户登录、登出、令牌管理相关接口"),
                        new Tag().name("用户管理").description("用户信息的增删改查和角色管理"),
                        new Tag().name("项目管理").description("项目创建、查询、更新、删除以及成员管理"),
                        new Tag().name("文档管理").description("文档创建、查询、更新、删除以及版本控制"),
                        new Tag().name("变更管理").description("变更请求的创建、审批、执行和跟踪"),
                        new Tag().name("任务管理").description("任务创建、分配、更新和状态跟踪"),
                        new Tag().name("测试用例管理").description("测试用例创建、查询、更新、删除以及执行管理"),
                        new Tag().name("通知系统").description("通知消息的发送、查询和状态管理"),
                        new Tag().name("搜索系统").description("全文搜索和智能推荐功能"),
                        new Tag().name("权限管理").description("权限创建、查询、更新、删除以及权限分配管理")
                ));
    }
}