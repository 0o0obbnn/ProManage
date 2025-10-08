package com.promanage.api.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.promanage.common.domain.Result;
import com.promanage.infrastructure.utils.SecurityUtils;
import com.promanage.service.dto.request.NotificationActionRequest;
import com.promanage.service.dto.request.NotificationSendRequest;
import com.promanage.service.entity.Notification;
import com.promanage.service.INotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 通知控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "通知管理", description = "通知相关接口")
public class NotificationController {

    private final INotificationService notificationService;

    @Operation(summary = "发送通知")
    @PostMapping("/send")
    public Result<Boolean> sendNotification(@RequestBody NotificationSendRequest request) {
        try {
            Long currentUserId = SecurityUtils.getCurrentUserId().orElse(null);
            boolean result;
            
            if (request.getUserIds() != null && request.getUserIds().size() > 1) {
                // 批量发送
                result = notificationService.sendNotificationBatch(
                    request.getUserIds(), 
                    request.getType(), 
                    request.getTitle(), 
                    request.getContent(),
                    request.getRelatedId(),
                    request.getRelatedType(),
                    currentUserId
                );
            } else if (request.getUserIds() != null && request.getUserIds().size() == 1) {
                // 单个发送
                result = notificationService.sendNotification(
                    request.getUserIds().get(0), 
                    request.getType(), 
                    request.getTitle(), 
                    request.getContent(),
                    request.getRelatedId(),
                    request.getRelatedType(),
                    currentUserId
                );
            } else {
                return Result.error("接收者不能为空");
            }
            
            return result ? Result.success(true) : Result.error("发送通知失败");
        } catch (Exception e) {
            log.error("发送通知失败", e);
            return Result.error("发送通知失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取用户通知列表")
    @GetMapping("/list")
    public Result<Page<Notification>> getUserNotifications(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        try {
            Long currentUserId = SecurityUtils.getCurrentUserId().orElse(null);
            Page<Notification> notifications = notificationService.getUserNotifications(currentUserId, page, size);
            return Result.success(notifications);
        } catch (Exception e) {
            log.error("获取通知列表失败", e);
            return Result.error("获取通知列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取未读通知数量")
    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount() {
        try {
            Long currentUserId = SecurityUtils.getCurrentUserId().orElse(null);
            int count = notificationService.getUnreadCount(currentUserId);
            return Result.success(count);
        } catch (Exception e) {
            log.error("获取未读通知数量失败", e);
            return Result.error("获取未读通知数量失败: " + e.getMessage());
        }
    }

    @Operation(summary = "根据类型获取通知")
    @GetMapping("/type/{type}")
    public Result<Page<Notification>> getNotificationsByType(
            @Parameter(description = "通知类型") @PathVariable String type,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        try {
            Long currentUserId = SecurityUtils.getCurrentUserId().orElse(null);
            Page<Notification> notifications = notificationService.getNotificationsByType(currentUserId, type, page, size);
            return Result.success(notifications);
        } catch (Exception e) {
            log.error("根据类型获取通知失败", e);
            return Result.error("获取通知失败: " + e.getMessage());
        }
    }

    @Operation(summary = "标记通知为已读")
    @PostMapping("/{id}/read")
    public Result<Boolean> markAsRead(@Parameter(description = "通知ID") @PathVariable Long id) {
        try {
            Long currentUserId = SecurityUtils.getCurrentUserId().orElse(null);
            boolean result = notificationService.markAsRead(id, currentUserId);
            return result ? Result.success(true) : Result.error("标记已读失败");
        } catch (Exception e) {
            log.error("标记通知已读失败", e);
            return Result.error("标记已读失败: " + e.getMessage());
        }
    }

    @Operation(summary = "批量操作通知")
    @PostMapping("/batch")
    public Result<Boolean> batchOperation(@RequestBody NotificationActionRequest request) {
        try {
            Long currentUserId = SecurityUtils.getCurrentUserId().orElse(null);
            boolean result;
            
            if ("read".equals(request.getAction())) {
                // 批量标记已读
                result = notificationService.markAsReadBatch(request.getNotificationIds(), currentUserId);
            } else if ("delete".equals(request.getAction())) {
                // 批量删除
                result = notificationService.deleteNotificationBatch(request.getNotificationIds(), currentUserId);
            } else {
                return Result.error("不支持的操作类型");
            }
            
            return result ? Result.success(true) : Result.error("操作失败");
        } catch (Exception e) {
            log.error("批量操作通知失败", e);
            return Result.error("操作失败: " + e.getMessage());
        }
    }

    @Operation(summary = "标记所有通知为已读")
    @PostMapping("/read-all")
    public Result<Boolean> markAllAsRead() {
        try {
            Long currentUserId = SecurityUtils.getCurrentUserId().orElse(null);
            boolean result = notificationService.markAllAsRead(currentUserId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("标记所有通知已读失败", e);
            return Result.error("标记所有通知已读失败: " + e.getMessage());
        }
    }

    @Operation(summary = "删除通知")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteNotification(@Parameter(description = "通知ID") @PathVariable Long id) {
        try {
            Long currentUserId = SecurityUtils.getCurrentUserId().orElse(null);
            boolean result = notificationService.deleteNotification(id, currentUserId);
            return result ? Result.success(true) : Result.error("删除通知失败");
        } catch (Exception e) {
            log.error("删除通知失败", e);
            return Result.error("删除通知失败: " + e.getMessage());
        }
    }

    @Operation(summary = "根据相关数据获取通知")
    @GetMapping("/related")
    public Result<java.util.List<Notification>> getNotificationsByRelatedData(
            @Parameter(description = "相关数据ID") @RequestParam Long relatedId,
            @Parameter(description = "相关数据类型") @RequestParam String relatedType) {
        try {
            java.util.List<Notification> notifications = notificationService.getNotificationsByRelatedData(relatedId, relatedType);
            return Result.success(notifications);
        } catch (Exception e) {
            log.error("根据相关数据获取通知失败", e);
            return Result.error("获取通知失败: " + e.getMessage());
        }
    }
}