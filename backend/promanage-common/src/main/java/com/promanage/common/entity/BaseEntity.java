package com.promanage.common.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.Version;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;

/**
 * 公共实体基类
 *
 * <p>提供所有持久化实体共享的主键、审计字段和乐观锁版本字段。 统一在此处维护，避免各实体重复声明相同字段。
 */
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class BaseEntity implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  /** 主键ID */
  @TableId(type = IdType.AUTO)
  @Schema(description = "主键ID", example = "1")
  @EqualsAndHashCode.Include
  private Long id;

  /** 创建时间 */
  @TableField(value = "created_at", fill = FieldFill.INSERT)
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  @Schema(description = "创建时间", example = "2025-09-30 10:00:00")
  private LocalDateTime createTime;

  /** 更新时间 */
  @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  @Schema(description = "更新时间", example = "2025-09-30 10:30:00")
  private LocalDateTime updateTime;

  /** 逻辑删除时间 */
  @TableLogic
  @TableField(value = "deleted_at")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  @Schema(description = "删除时间", example = "2025-09-30 11:00:00")
  private LocalDateTime deletedAt;

  /** 删除人ID */
  @TableField(value = "deleted_by")
  @Schema(description = "删除人ID", example = "1")
  private Long deletedBy;

  /** 创建人ID */
  @TableField(value = "creator_id", fill = FieldFill.INSERT)
  @Schema(description = "创建人ID", example = "1")
  private Long creatorId;

  /** 更新人ID */
  @TableField(value = "updater_id", fill = FieldFill.INSERT_UPDATE)
  @Schema(description = "更新人ID", example = "1")
  private Long updaterId;

  /** 乐观锁版本号 */
  @Version
  @TableField(value = "version", fill = FieldFill.INSERT)
  @Schema(description = "版本号", example = "1")
  private Long version;

  /** 兼容别名: createdAt */
  public LocalDateTime getCreatedAt() {
    return createTime;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createTime = createdAt;
  }

  /** 兼容别名: updatedAt */
  public LocalDateTime getUpdatedAt() {
    return updateTime;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updateTime = updatedAt;
  }

  /** 兼容别名: createdBy */
  public Long getCreatedBy() {
    return creatorId;
  }

  public void setCreatedBy(Long createdBy) {
    this.creatorId = createdBy;
  }

  /** 兼容别名: updatedBy */
  public Long getUpdatedBy() {
    return updaterId;
  }

  public void setUpdatedBy(Long updatedBy) {
    this.updaterId = updatedBy;
  }

  /** 兼容旧字段: deleted */
  public Boolean getDeleted() {
    return deletedAt != null;
  }

  public void setDeleted(Boolean deleted) {
    if (Boolean.TRUE.equals(deleted)) {
      if (this.deletedAt == null) {
        this.deletedAt = LocalDateTime.now();
      }
    } else if (Boolean.FALSE.equals(deleted)) {
      this.deletedAt = null;
    }
  }
}
