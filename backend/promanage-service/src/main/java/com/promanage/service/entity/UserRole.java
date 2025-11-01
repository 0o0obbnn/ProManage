package com.promanage.service.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户角色关联实体类
 *
 * <p>映射用户和角色的多对多关系 一个用户可以拥有多个角色,一个角色可以分配给多个用户
 *
 * @author ProManage Team
 * @date 2025-09-30
 */
@Data
@TableName("tb_user_role")
@Schema(description = "用户角色关联实体")
public class UserRole implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 主键ID */
  @TableId(type = IdType.AUTO)
  @Schema(description = "主键ID", example = "1")
  private Long id;

  /** 用户ID (不能为空) */
  @TableField("user_id")
  @Schema(description = "用户ID", example = "1", required = true)
  private Long userId;

  /** 角色ID (不能为空) */
  @TableField("role_id")
  @Schema(description = "角色ID", example = "1", required = true)
  private Long roleId;

  /** 创建时间 */
  @TableField("create_time")
  @Schema(description = "创建时间", example = "2025-09-30 10:00:00")
  private LocalDateTime createTime;
}
