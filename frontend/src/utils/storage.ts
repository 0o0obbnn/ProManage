/**
 * 安全存储工具
 * 提供加密的localStorage存储
 */

import CryptoJS from 'crypto-js'

// 加密密钥（实际应用中应该从环境变量获取）
const SECRET_KEY = import.meta.env.VITE_STORAGE_SECRET || 'promanage-secret-key-2024'

/**
 * 加密数据
 */
function encrypt(data: string): string {
  try {
    return CryptoJS.AES.encrypt(data, SECRET_KEY).toString()
  } catch (error) {
    console.error('Encryption failed:', error)
    return data
  }
}

/**
 * 解密数据
 */
function decrypt(encryptedData: string): string {
  try {
    const bytes = CryptoJS.AES.decrypt(encryptedData, SECRET_KEY)
    return bytes.toString(CryptoJS.enc.Utf8)
  } catch (error) {
    console.error('Decryption failed:', error)
    return encryptedData
  }
}

/**
 * 安全存储类
 */
export class SecureStorage {
  /**
   * 设置加密数据
   */
  static setItem(key: string, value: string, encrypt: boolean = true): void {
    try {
      const dataToStore = encrypt ? this.encrypt(value) : value
      localStorage.setItem(key, dataToStore)
    } catch (error) {
      console.error('SecureStorage setItem failed:', error)
    }
  }

  /**
   * 获取并解密数据
   */
  static getItem(key: string, encrypted: boolean = true): string | null {
    try {
      const data = localStorage.getItem(key)
      if (!data) return null
      return encrypted ? this.decrypt(data) : data
    } catch (error) {
      console.error('SecureStorage getItem failed:', error)
      return null
    }
  }

  /**
   * 移除数据
   */
  static removeItem(key: string): void {
    try {
      localStorage.removeItem(key)
    } catch (error) {
      console.error('SecureStorage removeItem failed:', error)
    }
  }

  /**
   * 清空所有数据
   */
  static clear(): void {
    try {
      localStorage.clear()
    } catch (error) {
      console.error('SecureStorage clear failed:', error)
    }
  }

  /**
   * 加密方法
   */
  private static encrypt(data: string): string {
    return encrypt(data)
  }

  /**
   * 解密方法
   */
  private static decrypt(encryptedData: string): string {
    return decrypt(encryptedData)
  }
}

/**
 * SessionStorage 安全存储
 */
export class SecureSessionStorage {
  static setItem(key: string, value: string, encrypt: boolean = true): void {
    try {
      const dataToStore = encrypt ? encrypt(value) : value
      sessionStorage.setItem(key, dataToStore)
    } catch (error) {
      console.error('SecureSessionStorage setItem failed:', error)
    }
  }

  static getItem(key: string, encrypted: boolean = true): string | null {
    try {
      const data = sessionStorage.getItem(key)
      if (!data) return null
      return encrypted ? decrypt(data) : data
    } catch (error) {
      console.error('SecureSessionStorage getItem failed:', error)
      return null
    }
  }

  static removeItem(key: string): void {
    try {
      sessionStorage.removeItem(key)
    } catch (error) {
      console.error('SecureSessionStorage removeItem failed:', error)
    }
  }

  static clear(): void {
    try {
      sessionStorage.clear()
    } catch (error) {
      console.error('SecureSessionStorage clear failed:', error)
    }
  }
}
