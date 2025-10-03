# [SECURITY] JWT 过滤器与鉴权（BE-004/DOC-009）

- 模块: SECURITY
- 责任人: BE: @yyy
- 关联任务: BE-004, DOC-009

## 背景
- 未检出 Jwt 过滤器/权限注解

## 目标
- 基于 OncePerRequestFilter 解析 Bearer Token，注入 Authentication
- 关键接口添加 @PreAuthorize 权限校验

## 验收
- 无 Token 返回 401；权限不足返回 403（统一格式）