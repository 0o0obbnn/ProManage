# [SECURITY] 全局异常处理器（BE-003）

- 模块: SECURITY
- 责任人: BE: @yyy
- 关联任务: BE-003

## 背景
- 未检出 @RestControllerAdvice/@ExceptionHandler 实现

## 目标
- 统一异常返回 Result<Void>，映射业务码（ResultCode）

## 建议
- GlobalExceptionHandler：处理 BusinessException、MethodArgumentNotValidException、AccessDeniedException、Throwable
- 统一日志脱敏、TraceId 透传

## 验收
- 常见错误返回统一、带错误码与消息