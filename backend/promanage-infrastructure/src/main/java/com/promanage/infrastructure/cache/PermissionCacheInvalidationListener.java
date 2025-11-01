package com.promanage.infrastructure.cache;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.promanage.domain.event.RolePermissionChangedEvent;
import com.promanage.domain.event.UserRoleAssignedEvent;
import com.promanage.domain.event.UserRoleRemovedEvent;
import com.promanage.domain.mapper.UserRoleMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 权限缓存失效监听器
 * 
 * <p>监听权限相关事件，自动失效相关用户的权限缓存，确保权限变更后立即生效
 *
 * @author ProManage Team
 * @date 2025-01-06
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionCacheInvalidationListener {

    private final CacheManager cacheManager;
    private final UserRoleMapper userRoleMapper;

    /**
     * 监听角色权限变更事件
     * 
     * <p>当角色权限发生变更时，失效所有拥有该角色的用户权限缓存
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRolePermissionChanged(RolePermissionChangedEvent event) {
        log.info("处理角色权限变更事件, roleId={}", event.getRoleId());
        
        try {
            // 查询拥有该角色的所有用户
            List<Long> userIds = userRoleMapper.findUserIdsByRoleId(event.getRoleId());
            
            if (userIds.isEmpty()) {
                log.debug("角色 {} 没有关联用户，跳过缓存失效", event.getRoleId());
                return;
            }
            
            // 异步失效所有相关用户的权限缓存
            CompletableFuture.runAsync(() -> {
                evictUserPermissionsCache(userIds);
                log.info("角色权限变更缓存失效完成, roleId={}, affectedUsers={}", 
                    event.getRoleId(), userIds.size());
            });
            
        } catch (Exception e) {
            log.error("处理角色权限变更事件失败, roleId={}", event.getRoleId(), e);
        }
    }

    /**
     * 监听用户角色分配事件
     * 
     * <p>当用户被分配新角色时，失效该用户的权限缓存
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserRoleAssigned(UserRoleAssignedEvent event) {
        log.info("处理用户角色分配事件, userId={}, roleId={}", event.getUserId(), event.getRoleId());
        
        try {
            evictUserPermissionsCache(List.of(event.getUserId()));
            log.info("用户角色分配缓存失效完成, userId={}, roleId={}", 
                event.getUserId(), event.getRoleId());
        } catch (Exception e) {
            log.error("处理用户角色分配事件失败, userId={}, roleId={}", 
                event.getUserId(), event.getRoleId(), e);
        }
    }

    /**
     * 监听用户角色移除事件
     * 
     * <p>当用户角色被移除时，失效该用户的权限缓存
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserRoleRemoved(UserRoleRemovedEvent event) {
        log.info("处理用户角色移除事件, userId={}, roleId={}", event.getUserId(), event.getRoleId());
        
        try {
            evictUserPermissionsCache(List.of(event.getUserId()));
            log.info("用户角色移除缓存失效完成, userId={}, roleId={}", 
                event.getUserId(), event.getRoleId());
        } catch (Exception e) {
            log.error("处理用户角色移除事件失败, userId={}, roleId={}", 
                event.getUserId(), event.getRoleId(), e);
        }
    }

    /**
     * 失效指定用户的权限缓存
     * 
     * @param userIds 用户ID列表
     */
    private void evictUserPermissionsCache(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }

        Cache userPermissionsCache = cacheManager.getCache("userPermissions");
        if (userPermissionsCache == null) {
            log.warn("userPermissions缓存不存在，跳过失效操作");
            return;
        }

        int successCount = 0;
        int failCount = 0;

        for (Long userId : userIds) {
            try {
                userPermissionsCache.evict(userId);
                successCount++;
                log.debug("失效用户权限缓存成功, userId={}", userId);
            } catch (Exception e) {
                failCount++;
                log.warn("失效用户权限缓存失败, userId={}", userId, e);
            }
        }

        log.info("权限缓存失效完成, 成功={}, 失败={}", successCount, failCount);
    }
}
