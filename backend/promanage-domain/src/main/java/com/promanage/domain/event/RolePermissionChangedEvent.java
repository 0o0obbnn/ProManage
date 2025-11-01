package com.promanage.domain.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 角色权限变更事件
 * 
 * <p>当角色的权限集合发生变化时发布此事件，用于触发相关用户权限缓存的失效
 *
 * @author ProManage Team
 * @date 2025-01-06
 */
@Getter
@RequiredArgsConstructor
public class RolePermissionChangedEvent {
    
    /**
     * 发生变更的角色ID
     */
    private final Long roleId;
    
    /**
     * 事件发生时间戳
     */
    private final long timestamp = System.currentTimeMillis();
}
