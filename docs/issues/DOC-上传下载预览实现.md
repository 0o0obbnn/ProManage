# [DOC] 文件上传/下载/预览实现（BE-010/DOC-002/003/004）

- 模块: DOC
- 责任人: BE: @yyy / FE: @xxx
- 关联任务: BE-010, DOC-002, DOC-003, DOC-004

## 目标
- 抽象 StorageService（MinIO/S3 可插拔）
- 提供分片上传、断点续传；支持下载与在线预览（PDF/图片）

## 后端
- /api/v1/files/upload|download|preview
- 存储元数据与版本关联（tb_document/file_url）

## 前端
- 上传组件封装，进度/重试；预览页接入

## 验收
- >=500MB 文件可稳定上传；下载带 content-disposition；预览正常