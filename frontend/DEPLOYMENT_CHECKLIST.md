# ProManage 前端部署检查清单

## 📋 部署前检查

### 1. 环境配置 ✅

- [ ] 修改 `.env.production` 中的 `VITE_STORAGE_SECRET`
- [ ] 配置正确的 `VITE_API_BASE_URL`
- [ ] 配置正确的 `VITE_WS_URL`
- [ ] 确认所有环境变量已设置

### 2. 代码质量 ✅

- [ ] 运行 `npm run lint` 无错误
- [ ] 运行 `npm run type-check` 无错误
- [ ] 运行 `npm run test` 全部通过
- [ ] 测试覆盖率 >= 40%

### 3. 安全检查 ✅

- [ ] CSRF Token配置正确
- [ ] CSP策略已配置
- [ ] 生产环境console已禁用
- [ ] 敏感信息已加密
- [ ] Token刷新机制正常
- [ ] WebSocket连接安全

### 4. 性能优化 ✅

- [ ] 运行 `npm run build` 成功
- [ ] 打包体积 < 2MB
- [ ] 运行 `npm run lighthouse` 评分 > 90
- [ ] 图片已优化（WebP格式）
- [ ] 代码已分割
- [ ] 懒加载已启用

### 5. 功能测试 ✅

- [ ] 登录/注册功能正常
- [ ] 项目管理功能正常
- [ ] 文档管理功能正常
- [ ] 任务管理功能正常
- [ ] 通知功能正常
- [ ] 搜索功能正常
- [ ] WebSocket实时推送正常

### 6. 浏览器兼容性 ✅

- [ ] Chrome最新版测试通过
- [ ] Firefox最新版测试通过
- [ ] Edge最新版测试通过
- [ ] Safari最新版测试通过（Mac）

### 7. 服务器配置 ✅

- [ ] HTTPS已启用
- [ ] Gzip压缩已启用
- [ ] HTTP/2已启用
- [ ] 缓存策略已配置
- [ ] CDN已配置（可选）

## 🚀 部署步骤

### 步骤1: 准备

```bash
# 1. 拉取最新代码
git pull origin main

# 2. 安装依赖
npm ci

# 3. 检查环境变量
cat .env.production
```

### 步骤2: 测试

```bash
# 1. 运行测试
npm run test:run

# 2. 类型检查
npm run type-check

# 3. 代码检查
npm run lint
```

### 步骤3: 构建

```bash
# 1. 构建生产版本
npm run build

# 2. 检查构建产物
ls -lh dist/

# 3. 预览（可选）
npm run preview
```

### 步骤4: 部署

```bash
# 方式1: 使用脚本
./scripts/deploy.sh

# 方式2: 手动上传
rsync -avz --delete dist/ user@server:/var/www/promanage/

# 方式3: Docker
docker build -t promanage-frontend .
docker push promanage-frontend
```

### 步骤5: 验证

```bash
# 1. 访问生产环境
curl -I https://your-domain.com

# 2. 检查API连接
curl https://your-domain.com/api/v1/health

# 3. 检查WebSocket
# 使用浏览器开发者工具测试
```

## 🔍 部署后检查

### 1. 功能验证

- [ ] 访问首页正常
- [ ] 登录功能正常
- [ ] API请求正常
- [ ] WebSocket连接正常
- [ ] 静态资源加载正常

### 2. 性能验证

- [ ] 首屏加载 < 3秒
- [ ] Lighthouse Performance > 90
- [ ] 无控制台错误
- [ ] 无内存泄漏

### 3. 安全验证

- [ ] HTTPS证书有效
- [ ] CSP策略生效
- [ ] CSRF Token正常
- [ ] 无敏感信息泄露

### 4. 监控配置

- [ ] 错误监控已启用（Sentry）
- [ ] 性能监控已启用
- [ ] 日志收集已配置
- [ ] 告警规则已设置

## 🔄 回滚计划

### 快速回滚

```bash
# 1. 回滚到上一个版本
git checkout <previous-commit>

# 2. 重新构建
npm run build

# 3. 重新部署
./scripts/deploy.sh
```

### 数据库回滚

```bash
# 如果有数据库变更，执行回滚脚本
# 联系后端团队执行数据库回滚
```

## 📊 监控指标

### 关键指标

- **可用性**: > 99.9%
- **响应时间**: P95 < 300ms
- **错误率**: < 0.1%
- **首屏加载**: < 3秒

### 告警阈值

- 错误率 > 1%
- 响应时间 P95 > 500ms
- 可用性 < 99%
- CPU使用率 > 80%

## 📝 部署记录

### 部署信息

- **部署时间**: ___________
- **部署人员**: ___________
- **版本号**: v1.1.0
- **Git Commit**: ___________

### 检查结果

- **代码质量**: ✅ / ❌
- **测试通过**: ✅ / ❌
- **性能达标**: ✅ / ❌
- **安全检查**: ✅ / ❌

### 问题记录

| 问题 | 严重程度 | 解决方案 | 状态 |
|------|----------|----------|------|
|      |          |          |      |

## 🆘 紧急联系

- **开发负责人**: ___________
- **运维负责人**: ___________
- **技术支持**: ___________

## 📚 相关文档

- [README.md](./README.md)
- [SECURITY_FIX_REPORT.md](./SECURITY_FIX_REPORT.md)
- [FINAL_FIX_SUMMARY.md](./FINAL_FIX_SUMMARY.md)

---

**最后更新**: 2025-01-11
**版本**: v1.1.0
