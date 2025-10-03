package com.promanage.infrastructure.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.promanage.infrastructure.security.SecurityUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis Plus Meta Object Handler
 * <p>
 * Automatically fills common audit fields during insert and update operations.
 * Fields handled:
 * - createTime: Set on insert
 * - updateTime: Set on insert and update
 * - creatorId: Set on insert (from SecurityContext)
 * - updaterId: Set on update (from SecurityContext)
 * - deleted: Set on insert (default to false)
 * </p>
 *
 * @author ProManage Team
 * @since 2025-09-30
 */
@Component
public class MyBatisMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        // Set create time
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());

        // Set update time (same as create time on insert)
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());

        // Set creator ID from security context
        try {
            SecurityUtils.getCurrentUserId().ifPresent(userId ->
                this.strictInsertFill(metaObject, "creatorId", Long.class, userId)
            );
        } catch (Exception e) {
            // Ignore if security context is not available
        }

        // Set deleted flag to false (not deleted)
        this.strictInsertFill(metaObject, "deleted", Boolean.class, false);

        // Set version to 0 for optimistic locking
        this.strictInsertFill(metaObject, "version", Long.class, 0L);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // Set update time
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());

        // Set updater ID from security context
        try {
            SecurityUtils.getCurrentUserId().ifPresent(userId ->
                this.strictUpdateFill(metaObject, "updaterId", Long.class, userId)
            );
        } catch (Exception e) {
            // Ignore if security context is not available
        }
    }
}