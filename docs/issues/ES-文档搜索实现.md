# [ES] 文档搜索实现（DOC-008）

- 模块: DOC/SEARCH
- 责任人: BE: @yyy / FE: @xxx
- 关联任务: DOC-008

## 目标
- 基于 Elasticsearch 或替代方案实现全文检索（标题/摘要/内容）

## 后端
- 索引结构、同步策略（新增/更新/删除）
- /api/v1/documents/search?q=&page=&size=

## 前端
- 搜索框与结果页，分页与高亮

## 验收
- 搜索响应<=300ms（P95），结果准确，高亮正常