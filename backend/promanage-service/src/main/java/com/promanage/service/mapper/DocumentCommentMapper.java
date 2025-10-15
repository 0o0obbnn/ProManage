package com.promanage.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.promanage.service.entity.DocumentComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DocumentCommentMapper extends BaseMapper<DocumentComment> {

    @Select("SELECT COUNT(1) FROM tb_document_comment WHERE document_id = #{documentId} AND deleted = FALSE")
    int countByDocumentId(@Param("documentId") Long documentId);
}


