package com.promanage.common.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * IP地址工具类
 * <p>
 * 提供IP地址获取和处理相关的工具方法
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-04
 */
@Slf4j
public class IpUtils {
    
    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST_IPV4 = "127.0.0.1";
    private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";
    
    /**
     * 私有构造函数，防止实例化
     */
    private IpUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * 获取客户端真实IP地址
     * <p>
     * 支持通过代理、负载均衡等方式获取真实IP
     * 优先级：X-Forwarded-For > X-Real-IP > Proxy-Client-IP > WL-Proxy-Client-IP > RemoteAddr
     * </p>
     *
     * @param request HTTP请求对象
     * @return 客户端IP地址
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            log.warn("HttpServletRequest is null, cannot get IP address");
            return UNKNOWN;
        }
        
        String ip = null;
        
        // 1. X-Forwarded-For: 经过多个代理时，第一个IP为真实IP
        ip = request.getHeader("X-Forwarded-For");
        if (isValidIp(ip)) {
            // 多次反向代理后会有多个IP值，第一个为真实IP
            if (ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }
            return ip;
        }
        
        // 2. X-Real-IP: Nginx等代理服务器设置的真实IP
        ip = request.getHeader("X-Real-IP");
        if (isValidIp(ip)) {
            return ip;
        }
        
        // 3. Proxy-Client-IP: Apache等代理服务器设置
        ip = request.getHeader("Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip;
        }
        
        // 4. WL-Proxy-Client-IP: WebLogic代理服务器设置
        ip = request.getHeader("WL-Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip;
        }
        
        // 5. HTTP_CLIENT_IP
        ip = request.getHeader("HTTP_CLIENT_IP");
        if (isValidIp(ip)) {
            return ip;
        }
        
        // 6. HTTP_X_FORWARDED_FOR
        ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (isValidIp(ip)) {
            return ip;
        }
        
        // 7. 直接从RemoteAddr获取
        ip = request.getRemoteAddr();
        if (isValidIp(ip)) {
            return ip;
        }
        
        log.warn("Cannot get valid IP address from request");
        return UNKNOWN;
    }
    
    /**
     * 检查IP地址是否有效
     *
     * @param ip IP地址
     * @return true表示有效，false表示无效
     */
    private static boolean isValidIp(String ip) {
        return ip != null 
                && !ip.isEmpty() 
                && !UNKNOWN.equalsIgnoreCase(ip);
    }
    
    /**
     * 检查是否是本地IP
     *
     * @param ip IP地址
     * @return true表示是本地IP
     */
    public static boolean isLocalhost(String ip) {
        return LOCALHOST_IPV4.equals(ip) || LOCALHOST_IPV6.equals(ip);
    }
    
    /**
     * 检查是否是内网IP
     *
     * @param ip IP地址
     * @return true表示是内网IP
     */
    public static boolean isInternalIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        
        // 本地回环地址
        if (isLocalhost(ip)) {
            return true;
        }
        
        // IPv4内网地址段
        // 10.0.0.0 - 10.255.255.255
        // 172.16.0.0 - 172.31.255.255
        // 192.168.0.0 - 192.168.255.255
        String[] parts = ip.split("\\.");
        if (parts.length == 4) {
            try {
                int first = Integer.parseInt(parts[0]);
                int second = Integer.parseInt(parts[1]);
                
                // 10.x.x.x
                if (first == 10) {
                    return true;
                }
                
                // 172.16.x.x - 172.31.x.x
                if (first == 172 && second >= 16 && second <= 31) {
                    return true;
                }
                
                // 192.168.x.x
                if (first == 192 && second == 168) {
                    return true;
                }
            } catch (NumberFormatException e) {
                log.warn("Invalid IP format: {}", ip);
            }
        }
        
        return false;
    }
    
    /**
     * IP地址转换为长整型
     *
     * @param ip IP地址
     * @return 长整型表示的IP
     */
    public static Long ipToLong(String ip) {
        if (ip == null || ip.isEmpty()) {
            return null;
        }
        
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return null;
        }
        
        try {
            long result = 0;
            for (int i = 0; i < 4; i++) {
                int part = Integer.parseInt(parts[i]);
                if (part < 0 || part > 255) {
                    return null;
                }
                result = result * 256 + part;
            }
            return result;
        } catch (NumberFormatException e) {
            log.warn("Invalid IP format: {}", ip);
            return null;
        }
    }
    
    /**
     * 长整型转换为IP地址
     *
     * @param ipLong 长整型表示的IP
     * @return IP地址字符串
     */
    public static String longToIp(Long ipLong) {
        if (ipLong == null) {
            return null;
        }
        
        return ((ipLong >> 24) & 0xFF) + "." +
               ((ipLong >> 16) & 0xFF) + "." +
               ((ipLong >> 8) & 0xFF) + "." +
               (ipLong & 0xFF);
    }
}

