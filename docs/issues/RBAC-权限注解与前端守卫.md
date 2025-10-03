# [RBAC] 权限注解与前端守卫联动（DOC-009/FE-009）

- 模块: SECURITY/FE
- 责任人: BE: @yyy / FE: @xxx
- 关联任务: DOC-009, FE-009

## 目标
- 后端使用 @PreAuthorize / 自定义注解校验权限
- 前端路由 meta.permissions + 指令控制按钮

## 验收
- 无权限访问跳转 403；按钮级权限按规则隐藏