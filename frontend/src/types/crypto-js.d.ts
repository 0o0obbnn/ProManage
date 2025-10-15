declare module 'crypto-js' {
  export namespace AES {
    function encrypt(message: string, key: string): any
    function decrypt(ciphertext: any, key: string): any
  }
  
  export namespace enc {
    const Utf8: any
  }
}
