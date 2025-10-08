package com.promanage.api.dto.response;

import com.promanage.service.entity.DocumentFolder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 文档文件夹响应DTO
 *
 * @author ProManage Team
 * @date 2025-10-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "文档文件夹响应")
public class DocumentFolderResponse {

    @Schema(description = "文件夹ID", example = "1")
    private Long id;

    @Schema(description = "文件夹名称", example = "技术文档")
    private String name;

    @Schema(description = "文件夹描述", example = "存放所有技术相关文档")
    private String description;

    @Schema(description = "所属项目ID", example = "1")
    private Long projectId;

    @Schema(description = "父文件夹ID", example = "0")
    private Long parentId;

    @Schema(description = "排序", example = "1")
    private Integer sortOrder;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "创建人ID", example = "1")
    private Long creatorId;

    /**
     * 从实体对象转换为响应对象
     *
     * @param folder 文档文件夹实体
     * @return 文档文件夹响应对象
     */
    public static DocumentFolderResponse fromEntity(DocumentFolder folder) {
        if (folder == null) {
            return null;
        }

        DocumentFolderResponse response = new DocumentFolderResponse();
        response.setId(folder.getId());
        response.setName(folder.getName());
        response.setDescription(folder.getDescription());
        response.setProjectId(folder.getProjectId());
        response.setParentId(folder.getParentId());
        response.setSortOrder(folder.getSortOrder());
        response.setCreateTime(folder.getCreateTime());
        response.setUpdateTime(folder.getUpdateTime());
        response.setCreatorId(folder.getCreatorId());

        return response;
    }
}