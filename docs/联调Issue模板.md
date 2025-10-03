# 联调 Issue 模板

- 标题: [模块] [接口] 联调 - 描述
- 模块: AUTH / DOC / DASH / TASK / TEST / SECURITY
- 责任人: FE: @xxx / BE: @yyy
- 关联任务ID: (如 AUTH-002, DOC-003)

## 1. 接口清单
- [ ] 方法: GET|POST|PUT|DELETE
- [ ] 路径: /api/...
- [ ] 权限: 需要登录 | 需要权限(code:)
- [ ] 说明: 简述业务场景

### 1.1 请求参数
- Query:
- Path:
- Body (JSON):
- Header:
- 示例:
```json
{
  "": ""
}
```

### 1.2 返回结构
- HTTP Status: 200 / 4xx / 5xx
- 统一返回: Result<T>
- 字段:
```json
{
  "code": 0,
  "msg": "success",
  "data": {}
}
```

## 2. 成功与失败用例
- 成功用例:
- 失败用例: (参数缺失/权限不足/资源不存在/校验失败)

## 3. 环境与鉴权
- 环境: dev / test
- 鉴权: Bearer {{accessToken}}
- 刷新: /auth/refresh

## 4. 对齐项
- [ ] 错误码约定
- [ ] 时间/数字格式
- [ ] 分页/排序约定
- [ ] 幂等性/重试策略
- [ ] 文件上传/下载（若有）

## 5. Mock & 验收
- Mock 数据/脚本:
- Postman/Apifox 集合:
- 验收标准:
- 截图/日志:

## 6. 阻塞项
- 依赖接口:
- 后端/前端待实现:
- 其他风险: