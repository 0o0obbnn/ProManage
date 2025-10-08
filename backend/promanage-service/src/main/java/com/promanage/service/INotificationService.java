package com.promanage.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.promanage.service.entity.Notification;

import java.util.List;

/**
 * 通知服务接口
 */
public interface INotificationService extends IService<Notification> {

    /**
     * 发送通知
     */
    boolean sendNotification(Long userId, String type, String title, String content);

    /**
     * 发送通知（带相关数据）
     */
    boolean sendNotification(Long userId, String type, String title, String content, 
                           Long relatedId, String relatedType, Long creatorId);

    /**
     * 批量发送通知
     */
    boolean sendNotificationBatch(List<Long> userIds, String type, String title, String content);

    /**
     * 批量发送通知（带相关数据）
     */
    boolean sendNotificationBatch(List<Long> userIds, String type, String title, String content,
                                Long relatedId, String relatedType, Long creatorId);

    /**
     * 获取用户通知列表
     */
    Page<Notification> getUserNotifications(Long userId, int page, int size);

    /**
     * 获取用户未读通知数量
     */
    int getUnreadCount(Long userId);

    /**
     * 标记通知为已读
     */
    boolean markAsRead(Long notificationId, Long userId);

    /**
     * 批量标记通知为已读
     */
    boolean markAsReadBatch(List<Long> notificationIds, Long userId);

    /**
     * 标记所有通知为已读
     */
    boolean markAllAsRead(Long userId);

    /**
     * 删除通知
     */
    boolean deleteNotification(Long notificationId, Long userId);

    /**
     * 批量删除通知
     */
    boolean deleteNotificationBatch(List<Long> notificationIds, Long userId);

    /**
     * 根据类型获取通知
     */
    Page<Notification> getNotificationsByType(Long userId, String type, int page, int size);

    /**
     * 根据相关数据获取通知
     */
    List<Notification> getNotificationsByRelatedData(Long relatedId, String relatedType);
}