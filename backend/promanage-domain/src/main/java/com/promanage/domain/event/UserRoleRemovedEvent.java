package com.promanage.domain.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 用户角色移除事件
 * 
 * <p>当用户角色被移除时发布此事件，用于触发该用户权限缓存的失效
 *
 * @author ProManage Team
 * @date 2025-01-06
 */
@Getter
@RequiredArgsConstructor
public class UserRoleRemovedEvent {
    
    /**
     * 用户ID
     */
    private final Long userId;
    
    /**
     * 被移除的角色ID
     */
    private final Long roleId;
    
    /**
     * 事件发生时间戳
     */
    private final long timestamp = System.currentTimeMillis();
}
