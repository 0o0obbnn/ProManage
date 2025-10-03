# [AUTH] 刷新令牌参数统一（JSON DTO）

- 模块: AUTH
- 责任人: FE: @xxx / BE: @yyy
- 关联任务: AUTH-002

## 背景
- 前端发送 { refreshToken }
- 后端签名: @RequestBody String refreshToken

## 目标
- 统一为 JSON DTO: { "refreshToken": "..." }

## 后端改动
- 修改签名为 DTO 类 RefreshTokenRequest { String refreshToken; }
- 校验与日志保持不变

## 前端改动
- 无（已发送 JSON）

## 验收
- 发送 JSON 体成功刷新，异常返回统一 Result 错误码