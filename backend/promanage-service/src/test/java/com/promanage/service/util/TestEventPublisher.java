package com.promanage.service.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

import com.promanage.domain.event.RolePermissionChangedEvent;
import com.promanage.domain.event.UserRoleAssignedEvent;
import com.promanage.domain.event.UserRoleRemovedEvent;

import lombok.extern.slf4j.Slf4j;

/**
 * 测试事件发布器
 * 
 * <p>提供测试环境中的事件发布功能，支持事件监听和验证
 *
 * @author ProManage Team
 * @date 2025-01-06
 */
@Slf4j
public class TestEventPublisher implements ApplicationEventPublisher, ApplicationEventPublisherAware {

    private ApplicationEventPublisher delegate;
    private final List<Object> publishedEvents = new ArrayList<>();
    private CountDownLatch eventLatch;

    @Override
    public void publishEvent(Object event) {
        log.debug("发布事件: {}", event.getClass().getSimpleName());
        
        // 记录发布的事件
        synchronized (publishedEvents) {
            publishedEvents.add(event);
        }
        
        // 如果有等待的latch，则计数减1
        if (eventLatch != null) {
            eventLatch.countDown();
        }
        
        // 委托给实际的发布器
        if (delegate != null) {
            delegate.publishEvent(event);
        }
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.delegate = applicationEventPublisher;
    }

    /**
     * 获取已发布的事件列表
     */
    public List<Object> getPublishedEvents() {
        synchronized (publishedEvents) {
            return new ArrayList<>(publishedEvents);
        }
    }

    /**
     * 清空已发布的事件列表
     */
    public void clearPublishedEvents() {
        synchronized (publishedEvents) {
            publishedEvents.clear();
        }
    }

    /**
     * 获取指定类型的事件数量
     */
    public int getEventCount(Class<?> eventType) {
        synchronized (publishedEvents) {
            return (int) publishedEvents.stream()
                .filter(event -> eventType.isInstance(event))
                .count();
        }
    }

    /**
     * 获取指定类型的事件列表
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getEventsOfType(Class<T> eventType) {
        synchronized (publishedEvents) {
            return publishedEvents.stream()
                .filter(event -> eventType.isInstance(event))
                .map(event -> (T) event)
                .collect(java.util.stream.Collectors.toList());
        }
    }

    /**
     * 等待指定数量的事件发布
     */
    public boolean waitForEvents(int eventCount, long timeout, TimeUnit unit) throws InterruptedException {
        eventLatch = new CountDownLatch(eventCount);
        return eventLatch.await(timeout, unit);
    }

    /**
     * 等待指定类型的事件发布
     */
    public boolean waitForEvent(Class<?> eventType, long timeout, TimeUnit unit) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        long timeoutMs = unit.toMillis(timeout);
        
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            if (getEventCount(eventType) > 0) {
                return true;
            }
            Thread.sleep(10); // 短暂等待
        }
        return false;
    }

    /**
     * 验证是否发布了指定类型的事件
     */
    public boolean hasPublishedEvent(Class<?> eventType) {
        return getEventCount(eventType) > 0;
    }

    /**
     * 验证是否发布了指定的事件
     */
    public boolean hasPublishedEvent(Object expectedEvent) {
        synchronized (publishedEvents) {
            return publishedEvents.contains(expectedEvent);
        }
    }

    /**
     * 获取角色权限变更事件
     */
    public List<RolePermissionChangedEvent> getRolePermissionChangedEvents() {
        return getEventsOfType(RolePermissionChangedEvent.class);
    }

    /**
     * 获取用户角色分配事件
     */
    public List<UserRoleAssignedEvent> getUserRoleAssignedEvents() {
        return getEventsOfType(UserRoleAssignedEvent.class);
    }

    /**
     * 获取用户角色移除事件
     */
    public List<UserRoleRemovedEvent> getUserRoleRemovedEvents() {
        return getEventsOfType(UserRoleRemovedEvent.class);
    }

    /**
     * 验证角色权限变更事件
     */
    public boolean hasRolePermissionChangedEvent(Long roleId) {
        return getRolePermissionChangedEvents().stream()
            .anyMatch(event -> event.getRoleId().equals(roleId));
    }

    /**
     * 验证用户角色分配事件
     */
    public boolean hasUserRoleAssignedEvent(Long userId, Long roleId) {
        return getUserRoleAssignedEvents().stream()
            .anyMatch(event -> event.getUserId().equals(userId) && event.getRoleId().equals(roleId));
    }

    /**
     * 验证用户角色移除事件
     */
    public boolean hasUserRoleRemovedEvent(Long userId, Long roleId) {
        return getUserRoleRemovedEvents().stream()
            .anyMatch(event -> event.getUserId().equals(userId) && event.getRoleId().equals(roleId));
    }

    /**
     * 获取事件发布统计信息
     */
    public EventStats getEventStats() {
        synchronized (publishedEvents) {
            return EventStats.builder()
                .totalEvents(publishedEvents.size())
                .rolePermissionChangedEvents(getEventCount(RolePermissionChangedEvent.class))
                .userRoleAssignedEvents(getEventCount(UserRoleAssignedEvent.class))
                .userRoleRemovedEvents(getEventCount(UserRoleRemovedEvent.class))
                .build();
        }
    }

    /**
     * 事件发布统计信息
     */
    public static class EventStats {
        private int totalEvents;
        private int rolePermissionChangedEvents;
        private int userRoleAssignedEvents;
        private int userRoleRemovedEvents;

        public static EventStatsBuilder builder() {
            return new EventStatsBuilder();
        }

        public static class EventStatsBuilder {
            private EventStats stats = new EventStats();

            public EventStatsBuilder totalEvents(int totalEvents) {
                stats.totalEvents = totalEvents;
                return this;
            }

            public EventStatsBuilder rolePermissionChangedEvents(int rolePermissionChangedEvents) {
                stats.rolePermissionChangedEvents = rolePermissionChangedEvents;
                return this;
            }

            public EventStatsBuilder userRoleAssignedEvents(int userRoleAssignedEvents) {
                stats.userRoleAssignedEvents = userRoleAssignedEvents;
                return this;
            }

            public EventStatsBuilder userRoleRemovedEvents(int userRoleRemovedEvents) {
                stats.userRoleRemovedEvents = userRoleRemovedEvents;
                return this;
            }

            public EventStats build() {
                return stats;
            }
        }

        // Getters
        public int getTotalEvents() { return totalEvents; }
        public int getRolePermissionChangedEvents() { return rolePermissionChangedEvents; }
        public int getUserRoleAssignedEvents() { return userRoleAssignedEvents; }
        public int getUserRoleRemovedEvents() { return userRoleRemovedEvents; }

        @Override
        public String toString() {
            return String.format("EventStats{total=%d, rolePermissionChanged=%d, userRoleAssigned=%d, userRoleRemoved=%d}",
                totalEvents, rolePermissionChangedEvents, userRoleAssignedEvents, userRoleRemovedEvents);
        }
    }
}
