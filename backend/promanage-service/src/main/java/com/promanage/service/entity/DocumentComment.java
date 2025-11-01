package com.promanage.service.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("tb_document_comment")
public class DocumentComment {

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long documentId;

  private Long userId;

  private String content;

  private LocalDateTime createTime;

  private LocalDateTime updateTime;

  @TableLogic private Boolean deleted;
}
