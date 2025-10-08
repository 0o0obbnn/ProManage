package com.promanage.api.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 数据库迁移工具
 * 用于在应用启动时执行必要的数据库Schema更新
 *
 * @author ProManage Team
 * @since 2025-10-04
 */
@Slf4j
@Component
@Order(1) // 确保在应用启动早期执行
public class DatabaseMigrationUtil implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        log.info("开始执行数据库迁移检查...");
        
        try {
            // 迁移1: 添加content_type字段到tb_document表
            addContentTypeColumn();

            // 迁移2: 添加category_id字段
            addColumnIfNotExists("category_id", "BIGINT", "NULL", "Document category ID");

            // 迁移3: 添加summary字段
            addColumnIfNotExists("summary", "TEXT", "NULL", "Document summary");

            // 迁移4: 添加type字段
            addColumnIfNotExists("type", "INTEGER", "1", "Document type (1=文档, 2=附件, 3=链接)");

            // 迁移5: 添加folder_id字段
            addColumnIfNotExists("folder_id", "BIGINT", "NULL", "Document folder ID");

            // 迁移6: 添加file_url字段
            addColumnIfNotExists("file_url", "TEXT", "NULL", "File URL for attachments");

            // 迁移7: 添加file_size字段
            addColumnIfNotExists("file_size", "BIGINT", "NULL", "File size in bytes");

            // 迁移8: 添加current_version字段
            addColumnIfNotExists("current_version", "VARCHAR(20)", "'1.0.0'", "Current version number");

            // 迁移9: 添加view_count字段
            addColumnIfNotExists("view_count", "INTEGER", "0", "View count");

            // 迁移10: 添加is_template字段
            addColumnIfNotExists("is_template", "BOOLEAN", "false", "Is template document");

            // 迁移11: 添加priority字段
            addColumnIfNotExists("priority", "INTEGER", "2", "Priority (1=低, 2=中, 3=高, 4=紧急)");

            // 迁移12: 添加reviewer_id字段
            addColumnIfNotExists("reviewer_id", "BIGINT", "NULL", "Reviewer user ID");

            // 迁移13: 添加published_at字段
            addColumnIfNotExists("published_at", "TIMESTAMP", "NULL", "Published timestamp");

            // 迁移14: 添加archived_at字段
            addColumnIfNotExists("archived_at", "TIMESTAMP", "NULL", "Archived timestamp");

            log.info("数据库迁移检查完成！");
        } catch (Exception e) {
            log.error("数据库迁移失败", e);
            // 不抛出异常，允许应用继续启动
        }
    }

    /**
     * 添加content_type字段到tb_document表
     */
    private void addContentTypeColumn() {
        addColumnIfNotExists("content_type", "VARCHAR(50)", "'text/html'",
                "Document content type (text/html, text/markdown, text/plain, etc.)");
    }

    /**
     * 通用方法：添加字段（如果不存在）
     */
    private void addColumnIfNotExists(String columnName, String columnType, String defaultValue, String comment) {
        try {
            // 检查字段是否存在
            String checkSql = "SELECT COUNT(*) FROM information_schema.columns " +
                    "WHERE table_name = 'tb_document' AND column_name = ?";

            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, columnName);

            if (count == null || count == 0) {
                log.info("字段{}不存在，开始添加...", columnName);

                // 添加字段
                String alterSql = String.format("ALTER TABLE tb_document ADD COLUMN %s %s DEFAULT %s",
                        columnName, columnType, defaultValue);
                jdbcTemplate.execute(alterSql);

                // 添加注释
                if (comment != null && !comment.isEmpty()) {
                    String commentSql = String.format("COMMENT ON COLUMN tb_document.%s IS '%s'",
                            columnName, comment);
                    jdbcTemplate.execute(commentSql);
                }

                log.info("✅ 成功添加{}字段到tb_document表", columnName);
            } else {
                log.info("✅ 字段{}已存在，跳过迁移", columnName);
            }
        } catch (Exception e) {
            log.error("添加{}字段失败", columnName, e);
            // 不抛出异常，允许继续执行其他迁移
        }
    }
}

