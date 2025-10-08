
import { setActivePinia, createPinia } from 'pinia';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { useUserStore } from '../user';
import * as authApi from '@/api/modules/auth';
import { setToken, setRefreshToken, clearAuth, getToken, getRefreshToken } from '@/utils/auth';

// Mock the API module
vi.mock('@/api/modules/auth', () => ({
  login: vi.fn(),
  logout: vi.fn(),
  getCurrentUser: vi.fn(),
  getUserPermissions: vi.fn(),
}));

// Mock the auth utils
vi.mock('@/utils/auth', () => ({
  getToken: vi.fn(),
  setToken: vi.fn(),
  getRefreshToken: vi.fn(),
  setRefreshToken: vi.fn(),
  clearAuth: vi.fn(),
}));

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

describe('User Store', () => {
  beforeEach(() => {
    // Create a fresh Pinia instance and make it active
    setActivePinia(createPinia());
    // Clear mocks and localStorage before each test
    vi.clearAllMocks();
    localStorage.clear();
  });

  it('should have correct initial state', () => {
    const store = useUserStore();
    expect(store.userInfo).toBeNull();
    expect(store.token).toBe('');
    expect(store.isLoggedIn).toBe(false);
    expect(store.userPermissions).toEqual([]);
  });

  describe('login action', () => {
    it('should log in a user successfully', async () => {
      const store = useUserStore();
      const mockResponse = {
        token: 'fake-token',
        refreshToken: 'fake-refresh-token',
        userInfo: { id: 1, username: 'testuser', realName: 'Test User', roles: [{ roleCode: 'ROLE_USER' }] },
      };
      const mockPermissions = ['document:view', 'task:create'];

      vi.mocked(authApi.login).mockResolvedValue(mockResponse);
      vi.mocked(authApi.getUserPermissions).mockResolvedValue(mockPermissions);

      await store.login('testuser', 'password');

      // Verify state is updated
      expect(store.token).toBe('fake-token');
      expect(store.userInfo?.username).toBe('testuser');
      expect(store.isLoggedIn).toBe(true);
      expect(store.userPermissions).toEqual(mockPermissions);

      // Verify utils were called
      expect(setToken).toHaveBeenCalledWith('fake-token');
      expect(setRefreshToken).toHaveBeenCalledWith('fake-refresh-token');
      expect(localStorage.setItem).toHaveBeenCalledWith('userInfo', JSON.stringify(mockResponse.userInfo));

      // Verify API calls
      expect(authApi.login).toHaveBeenCalledWith({ username: 'testuser', password: 'password', rememberMe: false });
      expect(authApi.getUserPermissions).toHaveBeenCalled();
    });

    it('should handle login failure', async () => {
      const store = useUserStore();
      const error = new Error('Invalid credentials');
      vi.mocked(authApi.login).mockRejectedValue(error);

      await expect(store.login('testuser', 'wrong-password')).rejects.toThrow('Invalid credentials');

      expect(store.isLoggedIn).toBe(false);
      expect(setToken).not.toHaveBeenCalled();
    });
  });

  describe('logout action', () => {
    it('should log out a user and clear state', async () => {
      const store = useUserStore();
      // Simulate a logged-in state
      store.token = 'fake-token';
      store.userInfo = { id: 1, username: 'testuser' };
      store.userPermissions = ['document:view'];

      vi.mocked(authApi.logout).mockResolvedValue(undefined);

      await store.logout();

      // Verify state is cleared
      expect(store.token).toBe('');
      expect(store.userInfo).toBeNull();
      expect(store.isLoggedIn).toBe(false);
      expect(store.userPermissions).toEqual([]);

      // Verify utils and API were called
      expect(authApi.logout).toHaveBeenCalled();
      expect(clearAuth).toHaveBeenCalled();
    });
  });

  describe('hasPermission getter', () => {
    it('should return true if user has the permission', () => {
      const store = useUserStore();
      store.userPermissions = ['document:view', 'task:edit'];
      expect(store.hasPermission('task:edit')).toBe(true);
    });

    it('should return false if user does not have the permission', () => {
      const store = useUserStore();
      store.userPermissions = ['document:view', 'task:edit'];
      expect(store.hasPermission('project:delete')).toBe(false);
    });

    it('should return true for any permission if user is SUPER_ADMIN', () => {
      const store = useUserStore();
      store.userInfo = { id: 1, username: 'admin', roles: [{ roleCode: 'ROLE_SUPER_ADMIN' }] };
      store.userPermissions = []; // Even with no explicit permissions
      expect(store.hasPermission('any:permission:imaginable')).toBe(true);
    });
  });

  describe('restoreFromLocalStorage action', () => {
    it('should restore state from localStorage', () => {
        const store = useUserStore();
        const mockUserInfo = { id: 1, username: 'testuser' };

        vi.mocked(getToken).mockReturnValue('stored-token');
        vi.mocked(getRefreshToken).mockReturnValue('stored-refresh-token');
        localStorage.setItem('userInfo', JSON.stringify(mockUserInfo));

        store.restoreFromLocalStorage();

        expect(store.token).toBe('stored-token');
        expect(store.refreshToken).toBe('stored-refresh-token');
        expect(store.userInfo).toEqual(mockUserInfo);
        // Ensure permissions are not fetched on restore
        expect(authApi.fetchUserPermissions).not.toHaveBeenCalled();
    });
  });
});
