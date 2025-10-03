package com.promanage.api.controller;

import com.promanage.common.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器
 * <p>
 * 提供系统测试和健康检查相关的API端点
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-09-30
 */
@Slf4j
@RestController
@RequestMapping("/api/test")
@Tag(name = "测试接口", description = "系统测试和健康检查相关接口")
public class TestController {

    /**
     * 系统健康检查
     * <p>
     * 检查系统基本状态和配置
     * </p>
     *
     * @return Result<Map<String, Object>> 系统状态信息
     */
    @GetMapping("/health")
    @Operation(summary = "系统健康检查", description = "检查系统基本状态和配置")
    public Result<Map<String, Object>> health() {
        log.info("Health check requested");
        
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("timestamp", LocalDateTime.now());
        healthInfo.put("version", "1.0.0-SNAPSHOT");
        healthInfo.put("springBootVersion", "3.2.5");
        healthInfo.put("javaVersion", System.getProperty("java.version"));
        
        log.info("Health check completed successfully");
        return Result.success("系统运行正常", healthInfo);
    }

    /**
     * 系统信息
     * <p>
     * 获取系统基本信息和配置
     * </p>
     *
     * @return Result<Map<String, Object>> 系统信息
     */
    @GetMapping("/info")
    @Operation(summary = "系统信息", description = "获取系统基本信息和配置")
    public Result<Map<String, Object>> info() {
        log.info("System info requested");
        
        Map<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("applicationName", "ProManage");
        systemInfo.put("version", "1.0.0-SNAPSHOT");
        systemInfo.put("description", "智能项目管理系统");
        systemInfo.put("springBootVersion", "3.2.5");
        systemInfo.put("javaVersion", System.getProperty("java.version"));
        systemInfo.put("osName", System.getProperty("os.name"));
        systemInfo.put("osVersion", System.getProperty("os.version"));
        systemInfo.put("userTimezone", System.getProperty("user.timezone"));
        systemInfo.put("serverTime", LocalDateTime.now());
        
        log.info("System info retrieved successfully");
        return Result.success("系统信息获取成功", systemInfo);
    }

    /**
     * 简单测试接口
     * <p>
     * 用于测试API基本功能
     * </p>
     *
     * @return Result<String> 测试结果
     */
    @GetMapping("/ping")
    @Operation(summary = "Ping测试", description = "简单的连通性测试")
    public Result<String> ping() {
        log.info("Ping test requested");
        return Result.success("Pong! API is working correctly.");
    }
}
