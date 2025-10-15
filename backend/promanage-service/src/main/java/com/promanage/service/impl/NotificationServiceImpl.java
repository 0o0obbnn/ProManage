package com.promanage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.promanage.common.domain.ResultCode;
import com.promanage.common.exception.BusinessException;
import com.promanage.infrastructure.security.SecurityUtils;
import com.promanage.service.entity.Notification;
import com.promanage.service.INotificationService;
import com.promanage.service.mapper.NotificationMapper;
import com.promanage.service.IWebSocketMessageService;
import com.promanage.service.service.IPermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * 通知服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements INotificationService {

    private final NotificationMapper notificationMapper;
    private final IWebSocketMessageService webSocketMessageService;

    @Override
    @Transactional
    public boolean sendNotification(Long userId, String type, String title, String content) {
        return sendNotification(userId, type, title, content, null, null, null);
    }

    @Override
    @Transactional
    public boolean sendNotification(Long userId, String type, String title, String content, 
                                  Long relatedId, String relatedType, Long creatorId) {
        try {
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setType(type);
            notification.setTitle(title);
            notification.setContent(content);
            notification.setRelatedId(relatedId);
            notification.setRelatedType(relatedType);
            notification.setIsRead(false);
            notification.setCreatorId(creatorId);
            
            int result = notificationMapper.insert(notification);
            if (result > 0) {
                log.info("通知发送成功, 用户ID: {}, 类型: {}, 标题: {}", userId, type, title);
                
                // 同时发送WebSocket消息
                try {
                    webSocketMessageService.sendNotificationToUser(userId, title, content, relatedId, relatedType);
                } catch (Exception e) {
                    log.warn("发送WebSocket消息失败, 用户ID: {}, 错误: {}", userId, e.getMessage());
                }
            }
            return result > 0;
        } catch (Exception e) {
            log.error("通知发送失败, 用户ID: {}, 类型: {}, 错误: {}", userId, type, e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean sendNotificationBatch(List<Long> userIds, String type, String title, String content) {
        return sendNotificationBatch(userIds, type, title, content, null, null, null);
    }

    @Override
    @Transactional
    public boolean sendNotificationBatch(List<Long> userIds, String type, String title, String content,
                                       Long relatedId, String relatedType, Long creatorId) {
        try {
            List<Notification> notifications = userIds.stream().map(userId -> {
                Notification notification = new Notification();
                notification.setUserId(userId);
                notification.setType(type);
                notification.setTitle(title);
                notification.setContent(content);
                notification.setRelatedId(relatedId);
                notification.setRelatedType(relatedType);
                notification.setIsRead(false);
                notification.setCreatorId(creatorId);
                return notification;
            }).toList();
            
            boolean result = saveBatch(notifications);
            if (result) {
                log.info("批量通知发送成功, 用户数量: {}, 类型: {}, 标题: {}", userIds.size(), type, title);
                
                // 同时发送WebSocket消息
                try {
                    webSocketMessageService.sendNotificationToUsers(userIds, title, content, relatedId, relatedType);
                } catch (Exception e) {
                    log.warn("批量发送WebSocket消息失败, 用户数量: {}, 错误: {}", userIds.size(), e.getMessage());
                }
            }
            return result;
        } catch (Exception e) {
            log.error("批量通知发送失败, 用户数量: {}, 类型: {}, 错误: {}", userIds.size(), type, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Page<Notification> getUserNotifications(Long userId, int page, int size) {
        // 获取当前用户
        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录"));

        // 用户只能查看自己的通知
        if (!currentUserId.equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "您只能查看自己的通知");
        }

        Page<Notification> pageParam = new Page<>(page, size);
        QueryWrapper<Notification> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .eq("deleted", false)
                   .orderByDesc("create_time");
        return notificationMapper.selectPage(pageParam, queryWrapper);
    }

    @Override
    public int getUnreadCount(Long userId) {
        // 获取当前用户
        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录"));

        // 用户只能查看自己的未读数量
        if (!currentUserId.equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "您只能查看自己的通知统计");
        }

        return notificationMapper.countUnreadByUserId(userId);
    }

    @Override
    @Transactional
    public boolean markAsRead(Long notificationId, Long userId) {
        try {
            // 获取当前用户
            Long currentUserId = SecurityUtils.getCurrentUserId()
                    .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录"));

            // 用户只能标记自己的通知
            if (!currentUserId.equals(userId)) {
                throw new BusinessException(ResultCode.FORBIDDEN, "您只能标记自己的通知");
            }

            // 验证通知是否属于当前用户
            Notification notification = notificationMapper.selectById(notificationId);
            if (notification == null) {
                throw new BusinessException(ResultCode.NOT_FOUND, "通知不存在");
            }
            if (!userId.equals(notification.getUserId())) {
                throw new BusinessException(ResultCode.FORBIDDEN, "此通知不属于您");
            }

            int result = notificationMapper.markAsRead(notificationId, userId);
            if (result > 0) {
                log.info("通知标记已读成功, 通知ID: {}, 用户ID: {}", notificationId, userId);
                return true;
            }
            return false;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("通知标记已读失败, 通知ID: {}, 用户ID: {}, 错误: {}", notificationId, userId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean markAsReadBatch(List<Long> notificationIds, Long userId) {
        try {
            // 获取当前用户
            Long currentUserId = SecurityUtils.getCurrentUserId()
                    .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录"));

            // 用户只能批量标记自己的通知
            if (!currentUserId.equals(userId)) {
                throw new BusinessException(ResultCode.FORBIDDEN, "您只能标记自己的通知");
            }

            int result = notificationMapper.markAsReadBatch(notificationIds, userId);
            if (result > 0) {
                log.info("批量通知标记已读成功, 通知数量: {}, 用户ID: {}", result, userId);
                return true;
            }
            return false;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("批量通知标记已读失败, 用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean markAllAsRead(Long userId) {
        try {
            // 获取当前用户
            Long currentUserId = SecurityUtils.getCurrentUserId()
                    .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录"));

            // 用户只能标记全部自己的通知
            if (!currentUserId.equals(userId)) {
                throw new BusinessException(ResultCode.FORBIDDEN, "您只能标记自己的全部通知");
            }

            int result = notificationMapper.markAllAsRead(userId);
            log.info("用户所有通知标记已读成功, 影响行数: {}, 用户ID: {}", result, userId);
            return result >= 0;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("用户所有通知标记已读失败, 用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean deleteNotification(Long notificationId, Long userId) {
        try {
            // 获取当前用户
            Long currentUserId = SecurityUtils.getCurrentUserId()
                    .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录"));

            // 用户只能删除自己的通知
            if (!currentUserId.equals(userId)) {
                throw new BusinessException(ResultCode.FORBIDDEN, "您只能删除自己的通知");
            }

            // 验证通知是否属于当前用户
            Notification notification = notificationMapper.selectById(notificationId);
            if (notification == null) {
                throw new BusinessException(ResultCode.NOT_FOUND, "通知不存在");
            }
            if (!userId.equals(notification.getUserId())) {
                throw new BusinessException(ResultCode.FORBIDDEN, "此通知不属于您");
            }

            int result = notificationMapper.deleteNotification(notificationId, userId);
            if (result > 0) {
                log.info("通知删除成功, 通知ID: {}, 用户ID: {}", notificationId, userId);
                return true;
            }
            return false;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("通知删除失败, 通知ID: {}, 用户ID: {}, 错误: {}", notificationId, userId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean deleteNotificationBatch(List<Long> notificationIds, Long userId) {
        try {
            // 获取当前用户
            Long currentUserId = SecurityUtils.getCurrentUserId()
                    .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录"));

            // 用户只能批量删除自己的通知
            if (!currentUserId.equals(userId)) {
                throw new BusinessException(ResultCode.FORBIDDEN, "您只能删除自己的通知");
            }

            int result = notificationMapper.deleteNotificationBatch(notificationIds, userId);
            if (result > 0) {
                log.info("批量通知删除成功, 删除数量: {}, 用户ID: {}", result, userId);
                return true;
            }
            return false;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("批量通知删除失败, 用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Page<Notification> getNotificationsByType(Long userId, String type, int page, int size) {
        // 获取当前用户
        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录"));

        // 用户只能查看自己的通知
        if (!currentUserId.equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "您只能查看自己的通知");
        }

        Page<Notification> pageParam = new Page<>(page, size);
        QueryWrapper<Notification> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .eq("type", type)
                   .eq("deleted", false)
                   .orderByDesc("create_time");
        return notificationMapper.selectPage(pageParam, queryWrapper);
    }

    @Override
    public List<Notification> getNotificationsByRelatedData(Long relatedId, String relatedType) {
        return notificationMapper.findByRelatedData(relatedId, relatedType);
    }
}