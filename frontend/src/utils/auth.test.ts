import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import {
  getToken,
  setToken,
  removeToken,
  getRefreshToken,
  setRefreshToken,
  removeRefreshToken,
  clearAuth,
  parseJWT,
  isTokenExpired,
  isTokenExpiringSoon,
  getTokenRemainingTime,
  hasValidToken,
  TOKEN_KEY,
  REFRESH_TOKEN_KEY,
  USER_INFO_KEY
} from './auth'

// Mock localStorage
const localStorageMock = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn()
}
Object.defineProperty(window, 'localStorage', {
  value: localStorageMock
})

// Mock atob for JWT parsing
global.atob = vi.fn((str: string) => {
  // Simple mock implementation for basic base64 decoding
  // This is a simplified version for testing purposes
  if (str === 'eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjk5OTk5OTk5OTl9') {
    return '{"sub":"1234567890","name":"John Doe","iat":1516239022,"exp":9999999999}'
  }
  if (str === 'eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjE1MTYyMzkwMjJ9') {
    return '{"sub":"1234567890","name":"John Doe","iat":1516239022,"exp":1516239022}'
  }
  return ''
})

describe('auth utils', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.resetAllMocks()
  })

  describe('Token Management', () => {
    it('gets token from localStorage', () => {
      const mockToken = 'test-token'
      localStorageMock.getItem.mockReturnValue(mockToken)
      
      const result = getToken()
      
      expect(localStorageMock.getItem).toHaveBeenCalledWith(TOKEN_KEY)
      expect(result).toBe(mockToken)
    })

    it('returns null when no token in localStorage', () => {
      localStorageMock.getItem.mockReturnValue(null)
      
      const result = getToken()
      
      expect(result).toBeNull()
    })

    it('sets token to localStorage', () => {
      const mockToken = 'test-token'
      
      setToken(mockToken)
      
      expect(localStorageMock.setItem).toHaveBeenCalledWith(TOKEN_KEY, mockToken)
    })

    it('removes token from localStorage', () => {
      removeToken()
      
      expect(localStorageMock.removeItem).toHaveBeenCalledWith(TOKEN_KEY)
    })

    it('gets refresh token from localStorage', () => {
      const mockRefreshToken = 'refresh-token'
      localStorageMock.getItem.mockReturnValue(mockRefreshToken)
      
      const result = getRefreshToken()
      
      expect(localStorageMock.getItem).toHaveBeenCalledWith(REFRESH_TOKEN_KEY)
      expect(result).toBe(mockRefreshToken)
    })

    it('returns null when no refresh token in localStorage', () => {
      localStorageMock.getItem.mockReturnValue(null)
      
      const result = getRefreshToken()
      
      expect(result).toBeNull()
    })

    it('sets refresh token to localStorage', () => {
      const mockRefreshToken = 'refresh-token'
      
      setRefreshToken(mockRefreshToken)
      
      expect(localStorageMock.setItem).toHaveBeenCalledWith(REFRESH_TOKEN_KEY, mockRefreshToken)
    })

    it('removes refresh token from localStorage', () => {
      removeRefreshToken()
      
      expect(localStorageMock.removeItem).toHaveBeenCalledWith(REFRESH_TOKEN_KEY)
    })

    it('clears all auth data', () => {
      clearAuth()
      
      expect(localStorageMock.removeItem).toHaveBeenCalledWith(TOKEN_KEY)
      expect(localStorageMock.removeItem).toHaveBeenCalledWith(REFRESH_TOKEN_KEY)
      expect(localStorageMock.removeItem).toHaveBeenCalledWith(USER_INFO_KEY)
    })
  })

  describe('JWT Parsing', () => {
    it('parses valid JWT token', () => {
      const token = 'header.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjk5OTk5OTk5OTl9.signature'
      
      const result = parseJWT(token)
      
      expect(result).toEqual({
        sub: '1234567890',
        name: 'John Doe',
        iat: 1516239022,
        exp: 9999999999
      })
    })

    it('returns null for invalid JWT token', () => {
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      
      const token = 'invalid.token'
      
      const result = parseJWT(token)
      
      expect(result).toBeNull()
      expect(consoleSpy).toHaveBeenCalledWith('Failed to parse JWT:', expect.any(Error))
      
      consoleSpy.mockRestore()
    })

    it('returns null for malformed token', () => {
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      
      const token = 'not.a.jwt'
      
      const result = parseJWT(token)
      
      expect(result).toBeNull()
      expect(consoleSpy).toHaveBeenCalledWith('Failed to parse JWT:', expect.any(Error))
      
      consoleSpy.mockRestore()
    })
  })

  describe('Token Expiration', () => {
    // Mock current time
    const mockCurrentTime = 1516239022 // 2018-01-18 01:30:22 UTC
    
    beforeEach(() => {
      vi.spyOn(Date, 'now').mockImplementation(() => mockCurrentTime * 1000)
    })

    afterEach(() => {
      vi.restoreAllMocks()
    })

    it('returns false for non-expired token', () => {
      const token = 'header.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjk5OTk5OTk5OTl9.signature'
      
      const result = isTokenExpired(token, 0)
      
      expect(result).toBe(false)
    })

    it('returns true for expired token', () => {
      const token = 'header.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjE1MTYyMzkwMjJ9.signature'
      
      const result = isTokenExpired(token, 0)
      
      expect(result).toBe(true)
    })

    it('returns true for token without exp claim', () => {
      const token = 'header.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.signature'
      
      const result = isTokenExpired(token, 0)
      
      expect(result).toBe(true)
    })

    it('returns true for invalid token', () => {
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      
      const token = 'invalid.token'
      
      const result = isTokenExpired(token, 0)
      
      expect(result).toBe(true)
      
      consoleSpy.mockRestore()
    })

    it('returns true for token expiring within buffer time', () => {
      // Token expires at current time + 100 seconds
      const expTime = mockCurrentTime + 100
      const payload = btoa(JSON.stringify({ exp: expTime }))
      const token = `header.${payload}.signature`
      
      const result = isTokenExpired(token, 300) // 5 minute buffer
      
      expect(result).toBe(true)
    })

    it('returns false for token not expiring within buffer time', () => {
      // Token expires at current time + 1000 seconds
      const expTime = mockCurrentTime + 1000
      const payload = btoa(JSON.stringify({ exp: expTime }))
      const token = `header.${payload}.signature`
      
      const result = isTokenExpired(token, 300) // 5 minute buffer
      
      expect(result).toBe(false)
    })

    it('checks if token is expiring soon (5 minutes)', () => {
      // Token expires at current time + 100 seconds
      const expTime = mockCurrentTime + 100
      const payload = btoa(JSON.stringify({ exp: expTime }))
      const token = `header.${payload}.signature`
      
      const result = isTokenExpiringSoon(token)
      
      expect(result).toBe(true)
    })

    it('checks if token is not expiring soon', () => {
      // Token expires at current time + 1000 seconds
      const expTime = mockCurrentTime + 1000
      const payload = btoa(JSON.stringify({ exp: expTime }))
      const token = `header.${payload}.signature`
      
      const result = isTokenExpiringSoon(token)
      
      expect(result).toBe(false)
    })

    it('gets remaining time for valid token', () => {
      // Token expires at current time + 1000 seconds
      const expTime = mockCurrentTime + 1000
      const payload = btoa(JSON.stringify({ exp: expTime }))
      const token = `header.${payload}.signature`
      
      const result = getTokenRemainingTime(token)
      
      expect(result).toBe(1000)
    })

    it('returns 0 for expired token', () => {
      // Token expires at current time - 100 seconds
      const expTime = mockCurrentTime - 100
      const payload = btoa(JSON.stringify({ exp: expTime }))
      const token = `header.${payload}.signature`
      
      const result = getTokenRemainingTime(token)
      
      expect(result).toBe(0)
    })

    it('returns 0 for token without exp claim', () => {
      const payload = btoa(JSON.stringify({ sub: '123' }))
      const token = `header.${payload}.signature`
      
      const result = getTokenRemainingTime(token)
      
      expect(result).toBe(0)
    })

    it('returns 0 for invalid token', () => {
      const token = 'invalid.token'
      
      const result = getTokenRemainingTime(token)
      
      expect(result).toBe(0)
    })
  })

  describe('Token Validation', () => {
    // Mock current time
    const mockCurrentTime = 1516239022 // 2018-01-18 01:30:22 UTC
    
    beforeEach(() => {
      vi.spyOn(Date, 'now').mockImplementation(() => mockCurrentTime * 1000)
    })

    afterEach(() => {
      vi.restoreAllMocks()
    })

    it('returns true for valid token', () => {
      // Token expires at current time + 1000 seconds
      const expTime = mockCurrentTime + 1000
      const payload = btoa(JSON.stringify({ exp: expTime }))
      const token = `header.${payload}.signature`
      
      localStorageMock.getItem.mockReturnValue(token)
      
      const result = hasValidToken()
      
      expect(result).toBe(true)
      expect(localStorageMock.getItem).toHaveBeenCalledWith(TOKEN_KEY)
    })

    it('returns false when no token', () => {
      localStorageMock.getItem.mockReturnValue(null)
      
      const result = hasValidToken()
      
      expect(result).toBe(false)
    })

    it('returns false for expired token', () => {
      // Token expires at current time - 100 seconds
      const expTime = mockCurrentTime - 100
      const payload = btoa(JSON.stringify({ exp: expTime }))
      const token = `header.${payload}.signature`
      
      localStorageMock.getItem.mockReturnValue(token)
      
      const result = hasValidToken()
      
      expect(result).toBe(false)
    })

    it('returns false for invalid token', () => {
      localStorageMock.getItem.mockReturnValue('invalid.token')
      
      const result = hasValidToken()
      
      expect(result).toBe(false)
    })
  })
})