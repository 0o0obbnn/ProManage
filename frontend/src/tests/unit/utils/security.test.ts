/**
 * 安全工具函数测试
 */
import { describe, it, expect } from 'vitest'
import {
  sanitizeHtml,
  sanitizeText,
  isSafeInput,
  escapeHtml,
  isSafeUrl,
  cleanUserInput,
  generateSecureRandom,
  isSafeFileType,
  CSP
} from '@/utils/security'

describe('security utils', () => {
  describe('sanitizeHtml', () => {
    it('应该清理危险的HTML标签', () => {
      const maliciousHtml = '<script>alert("xss")</script><p>正常内容</p>'
      const result = sanitizeHtml(maliciousHtml)
      expect(result).toBe('<p>正常内容</p>')
    })

    it('应该保留安全的HTML标签', () => {
      const safeHtml = '<p>段落</p><b>粗体</b><i>斜体</i>'
      const result = sanitizeHtml(safeHtml)
      expect(result).toContain('<p>段落</p>')
      expect(result).toContain('<b>粗体</b>')
      expect(result).toContain('<i>斜体</i>')
    })

    it('应该处理空字符串和null', () => {
      expect(sanitizeHtml('')).toBe('')
      expect(sanitizeHtml(null as any)).toBe('')
      expect(sanitizeHtml(undefined as any)).toBe('')
    })

    it('应该清理事件处理器', () => {
      const htmlWithEvents = '<div onclick="alert(1)">内容</div>'
      const result = sanitizeHtml(htmlWithEvents)
      expect(result).not.toContain('onclick')
    })
  })

  describe('sanitizeText', () => {
    it('应该移除所有HTML标签', () => {
      const htmlText = '<p>段落</p><script>alert(1)</script>'
      const result = sanitizeText(htmlText)
      expect(result).toBe('段落')
    })

    it('应该处理空输入', () => {
      expect(sanitizeText('')).toBe('')
      expect(sanitizeText(null as any)).toBe('')
    })
  })

  describe('isSafeInput', () => {
    it('应该识别安全的输入', () => {
      expect(isSafeInput('正常文本')).toBe(true)
      expect(isSafeInput('123456')).toBe(true)
      expect(isSafeInput('')).toBe(true)
    })

    it('应该识别危险的输入', () => {
      expect(isSafeInput('<script>alert(1)</script>')).toBe(false)
      expect(isSafeInput('javascript:alert(1)')).toBe(false)
      expect(isSafeInput('<img src=x onerror=alert(1)>')).toBe(false)
      expect(isSafeInput('<iframe src="javascript:alert(1)"></iframe>')).toBe(false)
    })
  })

  describe('escapeHtml', () => {
    it('应该转义HTML特殊字符', () => {
      expect(escapeHtml('<div>test</div>')).toBe('&lt;div&gt;test&lt;/div&gt;')
      expect(escapeHtml('"quoted"')).toBe('&quot;quoted&quot;')
      expect(escapeHtml("'single'")).toBe('&#x27;single&#x27;')
      expect(escapeHtml('&amp;')).toBe('&amp;amp;')
    })

    it('应该处理空输入', () => {
      expect(escapeHtml('')).toBe('')
      expect(escapeHtml(null as any)).toBe('')
    })
  })

  describe('isSafeUrl', () => {
    it('应该识别安全的URL', () => {
      expect(isSafeUrl('https://example.com')).toBe(true)
      expect(isSafeUrl('http://example.com')).toBe(true)
      expect(isSafeUrl('https://subdomain.example.com/path')).toBe(true)
    })

    it('应该识别危险的URL', () => {
      expect(isSafeUrl('javascript:alert(1)')).toBe(false)
      expect(isSafeUrl('data:text/html,<script>alert(1)</script>')).toBe(false)
      expect(isSafeUrl('vbscript:msgbox(1)')).toBe(false)
      expect(isSafeUrl('ftp://example.com')).toBe(false)
    })

    it('应该处理无效URL', () => {
      expect(isSafeUrl('')).toBe(false)
      expect(isSafeUrl('not-a-url')).toBe(false)
      expect(isSafeUrl(null as any)).toBe(false)
    })
  })

  describe('cleanUserInput', () => {
    it('应该清理用户输入', () => {
      const input = '  \n\t  正常文本  \n\t  '
      const result = cleanUserInput(input)
      expect(result).toBe('正常文本')
    })

    it('应该限制输入长度', () => {
      const longInput = 'a'.repeat(2000)
      const result = cleanUserInput(longInput, 100)
      expect(result.length).toBe(100)
    })

    it('应该移除控制字符', () => {
      const inputWithControl = '正常\x00控制\x1F字符'
      const result = cleanUserInput(inputWithControl)
      expect(result).toBe('正常控制字符')
    })
  })

  describe('generateSecureRandom', () => {
    it('应该生成指定长度的随机字符串', () => {
      const result = generateSecureRandom(16)
      expect(result).toHaveLength(16)
      expect(typeof result).toBe('string')
    })

    it('应该生成不同的随机字符串', () => {
      const result1 = generateSecureRandom(32)
      const result2 = generateSecureRandom(32)
      expect(result1).not.toBe(result2)
    })

    it('应该使用默认长度', () => {
      const result = generateSecureRandom()
      expect(result).toHaveLength(32)
    })
  })

  describe('isSafeFileType', () => {
    it('应该识别安全的文件类型', () => {
      expect(isSafeFileType('document.pdf')).toBe(true)
      expect(isSafeFileType('image.jpg')).toBe(true)
      expect(isSafeFileType('data.json')).toBe(true)
    })

    it('应该识别危险的文件类型', () => {
      expect(isSafeFileType('malware.exe')).toBe(false)
      expect(isSafeFileType('script.js')).toBe(false)
      expect(isSafeFileType('virus.bat')).toBe(false)
      expect(isSafeFileType('hack.php')).toBe(false)
    })

    it('应该验证允许的文件类型', () => {
      const allowedTypes = ['pdf', 'jpg', 'png']
      expect(isSafeFileType('document.pdf', allowedTypes)).toBe(true)
      expect(isSafeFileType('image.gif', allowedTypes)).toBe(false)
    })

    it('应该处理无效文件名', () => {
      expect(isSafeFileType('')).toBe(false)
      expect(isSafeFileType('noextension')).toBe(false)
      expect(isSafeFileType(null as any)).toBe(false)
    })
  })

  describe('CSP', () => {
    describe('generateNonce', () => {
      it('应该生成有效的nonce', () => {
        const nonce = CSP.generateNonce()
        expect(nonce).toHaveLength(16)
        expect(CSP.validateNonce(nonce)).toBe(true)
      })
    })

    describe('validateNonce', () => {
      it('应该验证有效的nonce', () => {
        expect(CSP.validateNonce('ABCDEFGHIJKLMNOP')).toBe(true)
        expect(CSP.validateNonce('abcdefghijklmnop')).toBe(true)
        expect(CSP.validateNonce('1234567890+/===')).toBe(true)
      })

      it('应该拒绝无效的nonce', () => {
        expect(CSP.validateNonce('')).toBe(false)
        expect(CSP.validateNonce('short')).toBe(false)
        expect(CSP.validateNonce('invalid-chars!@#')).toBe(false)
        expect(CSP.validateNonce(null as any)).toBe(false)
      })
    })
  })
})
