
import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
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
  USER_INFO_KEY,
} from '../auth';

// Mock localStorage
const localStorageMock = (() => {
  let store: Record<string, string> = {};
  return {
    getItem: (key: string) => store[key] || null,
    setItem: (key: string, value: string) => {
      store[key] = value.toString();
    },
    removeItem: (key: string) => {
      delete store[key];
    },
    clear: () => {
      store = {};
    },
  };
})();
Object.defineProperty(window, 'localStorage', {
  value: localStorageMock,
});

describe('Auth Utils', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  describe('Token Management', () => {
    it('should set and get token', () => {
      setToken('my-token');
      expect(getToken()).toBe('my-token');
    });

    it('should remove token', () => {
      setToken('my-token');
      removeToken();
      expect(getToken()).toBeNull();
    });

    it('should set and get refresh token', () => {
      setRefreshToken('my-refresh-token');
      expect(getRefreshToken()).toBe('my-refresh-token');
    });

    it('should remove refresh token', () => {
      setRefreshToken('my-refresh-token');
      removeRefreshToken();
      expect(getRefreshToken()).toBeNull();
    });

    it('should clear all auth info', () => {
      setToken('my-token');
      setRefreshToken('my-refresh-token');
      localStorage.setItem(USER_INFO_KEY, 'some-user-info');
      
      clearAuth();

      expect(getToken()).toBeNull();
      expect(getRefreshToken()).toBeNull();
      expect(localStorage.getItem(USER_INFO_KEY)).toBeNull();
    });
  });

  describe('JWT Parsing and Expiration', () => {
    // Helper to create a mock JWT
    const createMockToken = (payload: object) => {
      const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
      const pl = btoa(JSON.stringify(payload));
      return `${header}.${pl}.signature`;
    };

    it('should parse a valid JWT', () => {
      const payload = { sub: '123', name: 'Test User', exp: 1672531200 };
      const token = createMockToken(payload);
      expect(parseJWT(token)).toEqual(payload);
    });

    it('should return null for an invalid JWT', () => {
      expect(parseJWT('invalid.token')).toBeNull();
    });

    it('should correctly identify an expired token', () => {
      const now = Date.now();
      vi.setSystemTime(now);
      const expiredPayload = { exp: Math.floor(now / 1000) - 1 }; // Expired 1 second ago
      const token = createMockToken(expiredPayload);
      expect(isTokenExpired(token, 0)).toBe(true);
    });

    it('should correctly identify a non-expired token', () => {
        const now = Date.now();
        vi.setSystemTime(now);
        const validPayload = { exp: Math.floor(now / 1000) + 600 }; // Expires in 10 minutes
        const token = createMockToken(validPayload);
        expect(isTokenExpired(token, 0)).toBe(false);
    });

    it('should identify a token as expired if it is within the buffer time', () => {
        const now = Date.now();
        vi.setSystemTime(now);
        const expiringPayload = { exp: Math.floor(now / 1000) + 299 }; // Expires in 299 seconds
        const token = createMockToken(expiringPayload);
        expect(isTokenExpired(token, 300)).toBe(true); // 300s (5min) buffer
    });

    it('should identify a token as expiring soon', () => {
        const now = Date.now();
        vi.setSystemTime(now);
        const expiringPayload = { exp: Math.floor(now / 1000) + 299 }; // Expires in 299 seconds
        const token = createMockToken(expiringPayload);
        expect(isTokenExpiringSoon(token)).toBe(true);
    });

    it('should calculate remaining time correctly', () => {
        const now = Date.now();
        vi.setSystemTime(now);
        const remaining = 123;
        const payload = { exp: Math.floor(now / 1000) + remaining };
        const token = createMockToken(payload);
        expect(getTokenRemainingTime(token)).toBe(remaining);
    });

    it('should return 0 remaining time for expired token', () => {
        const now = Date.now();
        vi.setSystemTime(now);
        const payload = { exp: Math.floor(now / 1000) - 100 };
        const token = createMockToken(payload);
        expect(getTokenRemainingTime(token)).toBe(0);
    });
  });

  describe('hasValidToken', () => {
    it('should return false if no token exists', () => {
        expect(hasValidToken()).toBe(false);
    });

    it('should return false if token is expired', () => {
        const now = Date.now();
        vi.setSystemTime(now);
        const payload = { exp: Math.floor(now / 1000) - 1 };
        const token = createMockToken(payload);
        setToken(token);
        expect(hasValidToken()).toBe(false);
    });

    it('should return true if token exists and is not expired', () => {
        const now = Date.now();
        vi.setSystemTime(now);
        const payload = { exp: Math.floor(now / 1000) + 60 };
        const token = createMockToken(payload);
        setToken(token);
        expect(hasValidToken()).toBe(true);
    });
  });
});
