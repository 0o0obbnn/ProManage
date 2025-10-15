/**
 * 安全工具函数
 * 提供XSS防护、输入验证等安全功能
 */

import DOMPurify from 'dompurify'

/**
 * 清理HTML内容，防止XSS攻击
 * @param html 需要清理的HTML字符串
 * @param options 清理选项
 * @returns 清理后的安全HTML
 */
export function sanitizeHtml(html: string, options?: DOMPurify.Config): string {
  if (!html || typeof html !== 'string') {
    return ''
  }

  // 默认配置：只允许基本的文本格式
  const defaultOptions: DOMPurify.Config = {
    ALLOWED_TAGS: ['b', 'i', 'em', 'strong', 'p', 'br', 'ul', 'ol', 'li'],
    ALLOWED_ATTR: [],
    KEEP_CONTENT: true,
    ...options
  }

  return DOMPurify.sanitize(html, defaultOptions)
}

/**
 * 清理纯文本，移除所有HTML标签
 * @param text 需要清理的文本
 * @returns 清理后的纯文本
 */
export function sanitizeText(text: string): string {
  if (!text || typeof text !== 'string') {
    return ''
  }

  return DOMPurify.sanitize(text, { 
    ALLOWED_TAGS: [],
    KEEP_CONTENT: true 
  })
}

/**
 * 验证输入是否包含恶意内容
 * @param input 用户输入
 * @returns 是否安全
 */
export function isSafeInput(input: string): boolean {
  if (!input || typeof input !== 'string') {
    return true
  }

  // 检查常见的XSS攻击模式
  const xssPatterns = [
    /<script[^>]*>.*?<\/script>/gi,
    /<iframe[^>]*>.*?<\/iframe>/gi,
    /javascript:/gi,
    /on\w+\s*=/gi,
    /<object[^>]*>.*?<\/object>/gi,
    /<embed[^>]*>.*?<\/embed>/gi,
    /<link[^>]*>.*?<\/link>/gi,
    /<meta[^>]*>.*?<\/meta>/gi
  ]

  return !xssPatterns.some(pattern => pattern.test(input))
}

/**
 * 转义HTML特殊字符
 * @param text 需要转义的文本
 * @returns 转义后的文本
 */
export function escapeHtml(text: string): string {
  if (!text || typeof text !== 'string') {
    return ''
  }

  const htmlEscapes: Record<string, string> = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#x27;',
    '/': '&#x2F;'
  }

  return text.replace(/[&<>"'/]/g, (match) => htmlEscapes[match])
}

/**
 * 验证URL是否安全
 * @param url 需要验证的URL
 * @returns 是否安全
 */
export function isSafeUrl(url: string): boolean {
  if (!url || typeof url !== 'string') {
    return false
  }

  try {
    const urlObj = new URL(url, window.location.origin)
    
    // 只允许http和https协议
    if (!['http:', 'https:'].includes(urlObj.protocol)) {
      return false
    }

    // 检查是否包含javascript:等危险协议
    if (url.toLowerCase().includes('javascript:') || 
        url.toLowerCase().includes('data:') ||
        url.toLowerCase().includes('vbscript:')) {
      return false
    }

    return true
  } catch {
    return false
  }
}

/**
 * 检查是否为内部URL
 */
export function isInternalUrl(url: string): boolean {
  try {
    const urlObj = new URL(url, window.location.origin)
    return urlObj.origin === window.location.origin
  } catch {
    return false
  }
}

/**
 * 安全跳转
 */
export function safeRedirect(url: string, newWindow: boolean = false): void {
  if (!isSafeUrl(url)) {
    console.warn('Unsafe redirect blocked:', url)
    return
  }

  // 外部链接警告
  if (!isInternalUrl(url)) {
    const confirmed = confirm('您将跳转到外部网站，是否继续？')
    if (!confirmed) return
  }

  if (newWindow) {
    window.open(url, '_blank', 'noopener,noreferrer')
  } else {
    window.location.href = url
  }
}

/**
 * 清理用户输入，移除危险字符
 * @param input 用户输入
 * @param maxLength 最大长度
 * @returns 清理后的输入
 */
export function cleanUserInput(input: string, maxLength: number = 1000): string {
  if (!input || typeof input !== 'string') {
    return ''
  }

  // 限制长度
  let cleaned = input.slice(0, maxLength)
  
  // 移除控制字符
  cleaned = cleaned.replace(/[\x00-\x1F\x7F]/g, '')
  
  // 移除多余的空白字符
  cleaned = cleaned.replace(/\s+/g, ' ').trim()
  
  return cleaned
}

/**
 * 生成安全的随机字符串
 * @param length 长度
 * @returns 随机字符串
 */
export function generateSecureRandom(length: number = 32): string {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
  let result = ''
  
  if (typeof window !== 'undefined' && window.crypto) {
    // 使用Web Crypto API生成更安全的随机数
    const array = new Uint8Array(length)
    window.crypto.getRandomValues(array)
    
    for (let i = 0; i < length; i++) {
      result += chars[array[i] % chars.length]
    }
  } else {
    // 降级到Math.random
    for (let i = 0; i < length; i++) {
      result += chars[Math.floor(Math.random() * chars.length)]
    }
  }
  
  return result
}

/**
 * 验证文件类型是否安全
 * @param fileName 文件名
 * @param allowedTypes 允许的文件类型
 * @returns 是否安全
 */
export function isSafeFileType(fileName: string, allowedTypes: string[] = []): boolean {
  if (!fileName || typeof fileName !== 'string') {
    return false
  }

  const extension = fileName.split('.').pop()?.toLowerCase()
  
  if (!extension) {
    return false
  }

  // 危险文件类型
  const dangerousTypes = [
    'exe', 'bat', 'cmd', 'com', 'pif', 'scr', 'vbs', 'js', 'jar',
    'php', 'asp', 'jsp', 'py', 'rb', 'pl', 'sh', 'ps1'
  ]

  if (dangerousTypes.includes(extension)) {
    return false
  }

  // 如果指定了允许的类型，检查是否在允许列表中
  if (allowedTypes.length > 0) {
    return allowedTypes.includes(extension)
  }

  return true
}

/**
 * 安全链接组件辅助函数
 */
export function sanitizeHref(href: string): string {
  if (!href) return '#'
  
  // 移除危险协议
  const dangerousProtocols = ['javascript:', 'data:', 'vbscript:', 'file:']
  const lowerHref = href.toLowerCase().trim()
  
  for (const protocol of dangerousProtocols) {
    if (lowerHref.startsWith(protocol)) {
      return '#'
    }
  }
  
  return href
}

/**
 * 内容安全策略（CSP）相关工具
 */
export const CSP = {
  /**
   * 生成nonce值
   */
  generateNonce: (): string => generateSecureRandom(16),
  
  /**
   * 验证nonce值
   */
  validateNonce: (nonce: string): boolean => {
    return /^[A-Za-z0-9+/]{16,}$/.test(nonce)
  }
}
