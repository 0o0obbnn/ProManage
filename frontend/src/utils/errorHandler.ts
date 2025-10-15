/**
 * 统一错误处理层
 */

import { message } from 'ant-design-vue'

export enum ErrorType {
  NETWORK = 'NETWORK',
  VALIDATION = 'VALIDATION',
  AUTHENTICATION = 'AUTHENTICATION',
  AUTHORIZATION = 'AUTHORIZATION',
  BUSINESS = 'BUSINESS',
  UNKNOWN = 'UNKNOWN'
}

export class AppError extends Error {
  type: ErrorType
  code?: string
  details?: any

  constructor(message: string, type: ErrorType = ErrorType.UNKNOWN, code?: string, details?: any) {
    super(message)
    this.name = 'AppError'
    this.type = type
    this.code = code
    this.details = details
  }
}

export class NetworkError extends AppError {
  constructor(message: string = '网络错误，请检查网络连接', details?: any) {
    super(message, ErrorType.NETWORK, 'NETWORK_ERROR', details)
    this.name = 'NetworkError'
  }
}

export class ValidationError extends AppError {
  constructor(message: string = '数据验证失败', details?: any) {
    super(message, ErrorType.VALIDATION, 'VALIDATION_ERROR', details)
    this.name = 'ValidationError'
  }
}

export class AuthenticationError extends AppError {
  constructor(message: string = '认证失败，请重新登录', details?: any) {
    super(message, ErrorType.AUTHENTICATION, 'AUTH_ERROR', details)
    this.name = 'AuthenticationError'
  }
}

export class AuthorizationError extends AppError {
  constructor(message: string = '权限不足', details?: any) {
    super(message, ErrorType.AUTHORIZATION, 'PERMISSION_ERROR', details)
    this.name = 'AuthorizationError'
  }
}

export class BusinessError extends AppError {
  constructor(message: string, code?: string, details?: any) {
    super(message, ErrorType.BUSINESS, code, details)
    this.name = 'BusinessError'
  }
}

/**
 * 错误处理器
 */
export class ErrorHandler {
  /**
   * 处理错误
   */
  static handle(error: Error | AppError, context?: string): void {
    // 开发环境输出详细错误
    if (import.meta.env.DEV) {
      console.error(`[ErrorHandler] ${context || 'Unknown'}:`, error)
    }

    // 根据错误类型处理
    if (error instanceof AppError) {
      this.handleAppError(error)
    } else {
      this.handleUnknownError(error)
    }

    // 上报错误
    this.report(error, context)
  }

  /**
   * 处理应用错误
   */
  private static handleAppError(error: AppError): void {
    switch (error.type) {
      case ErrorType.NETWORK:
        message.error(error.message || '网络错误')
        break
      case ErrorType.VALIDATION:
        message.warning(error.message || '数据验证失败')
        break
      case ErrorType.AUTHENTICATION:
        message.error(error.message || '认证失败')
        // 跳转到登录页
        setTimeout(() => {
          window.location.href = '/login'
        }, 1500)
        break
      case ErrorType.AUTHORIZATION:
        message.error(error.message || '权限不足')
        break
      case ErrorType.BUSINESS:
        message.error(error.message)
        break
      default:
        message.error('操作失败，请稍后重试')
    }
  }

  /**
   * 处理未知错误
   */
  private static handleUnknownError(error: Error): void {
    console.error('Unknown error:', error)
    message.error('系统错误，请稍后重试')
  }

  /**
   * 上报错误到监控系统
   */
  private static report(error: Error | AppError, context?: string): void {
    // 生产环境上报到监控系统
    if (import.meta.env.PROD) {
      // TODO: 集成 Sentry 或其他监控服务
      // Sentry.captureException(error, {
      //   contexts: {
      //     app: {
      //       context,
      //       timestamp: new Date().toISOString()
      //     }
      //   }
      // })
    }
  }

  /**
   * 创建错误
   */
  static createError(
    message: string,
    type: ErrorType = ErrorType.UNKNOWN,
    code?: string,
    details?: any
  ): AppError {
    return new AppError(message, type, code, details)
  }
}

/**
 * 全局错误处理函数
 */
export function handleError(error: any, context?: string): void {
  ErrorHandler.handle(error, context)
}

/**
 * Promise错误处理包装器
 */
export function withErrorHandler<T>(
  promise: Promise<T>,
  context?: string
): Promise<T> {
  return promise.catch((error) => {
    ErrorHandler.handle(error, context)
    throw error
  })
}

/**
 * 异步函数错误处理装饰器
 */
export function catchError(context?: string) {
  return function (
    target: any,
    propertyKey: string,
    descriptor: PropertyDescriptor
  ) {
    const originalMethod = descriptor.value

    descriptor.value = async function (...args: any[]) {
      try {
        return await originalMethod.apply(this, args)
      } catch (error) {
        ErrorHandler.handle(error as Error, context || propertyKey)
        throw error
      }
    }

    return descriptor
  }
}
