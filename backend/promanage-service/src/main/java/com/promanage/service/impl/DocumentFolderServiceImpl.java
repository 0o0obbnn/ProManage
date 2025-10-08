package com.promanage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.promanage.common.domain.PageResult;
import com.promanage.common.domain.ResultCode;
import com.promanage.common.exception.BusinessException;
import com.promanage.service.entity.DocumentFolder;
import com.promanage.service.mapper.DocumentFolderMapper;
import com.promanage.service.service.IDocumentFolderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 文档文件夹服务实现类
 * <p>
 * 实现文档文件夹管理的所有业务逻辑
 * </p>
 *
 * @author ProManage Team
 * @date 2025-10-04
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentFolderServiceImpl implements IDocumentFolderService {

    private final DocumentFolderMapper documentFolderMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFolder(DocumentFolder folder) {
        log.info("创建文档文件夹, name={}, projectId={}", folder.getName(), folder.getProjectId());

        // 参数验证
        validateFolder(folder, true);

        // 设置默认值
        if (folder.getParentId() == null) {
            folder.setParentId(0L); // 默认根目录
        }
        if (folder.getSortOrder() == null) {
            folder.setSortOrder(0);
        }

        // 保存文件夹
        documentFolderMapper.insert(folder);

        log.info("创建文档文件夹成功, id={}, name={}", folder.getId(), folder.getName());
        return folder.getId();
    }

    @Override
    public DocumentFolder getFolderById(Long id) {
        log.info("查询文档文件夹详情, id={}", id);

        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "文件夹ID不能为空");
        }

        DocumentFolder folder = documentFolderMapper.selectById(id);
        if (folder == null || folder.getDeleted()) {
            log.warn("文档文件夹不存在, id={}", id);
            throw new BusinessException(ResultCode.PARAM_ERROR, "文档文件夹不存在");
        }

        return folder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFolder(Long id, DocumentFolder folder) {
        log.info("更新文档文件夹, id={}", id);

        // 检查文件夹是否存在
        DocumentFolder existingFolder = getFolderById(id);

        // 更新字段
        if (StringUtils.isNotBlank(folder.getName())) {
            existingFolder.setName(folder.getName());
        }
        if (StringUtils.isNotBlank(folder.getDescription())) {
            existingFolder.setDescription(folder.getDescription());
        }
        if (folder.getProjectId() != null) {
            existingFolder.setProjectId(folder.getProjectId());
        }
        if (folder.getParentId() != null) {
            existingFolder.setParentId(folder.getParentId());
        }
        if (folder.getSortOrder() != null) {
            existingFolder.setSortOrder(folder.getSortOrder());
        }

        // 保存更新
        documentFolderMapper.updateById(existingFolder);

        log.info("更新文档文件夹成功, id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFolder(Long id) {
        log.info("删除文档文件夹, id={}", id);

        // 检查文件夹是否存在
        getFolderById(id);

        // 检查文件夹下是否有子文件夹
        List<DocumentFolder> childFolders = documentFolderMapper.findByParentId(id);
        if (!childFolders.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "文件夹下存在子文件夹，无法删除");
        }

        // 检查文件夹下是否有文档
        int documentCount = documentFolderMapper.countDocumentsInFolder(id);
        if (documentCount > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "文件夹下存在文档，无法删除");
        }

        // 逻辑删除文件夹
        documentFolderMapper.deleteById(id);

        log.info("删除文档文件夹成功, id={}", id);
    }

    @Override
    public List<DocumentFolder> listByProjectId(Long projectId) {
        log.info("查询项目文件夹, projectId={}", projectId);

        if (projectId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "项目ID不能为空");
        }

        return documentFolderMapper.findByProjectId(projectId);
    }

    @Override
    public List<DocumentFolder> findByParentId(Long parentId) {
        log.info("查询父文件夹下的子文件夹, parentId={}", parentId);

        if (parentId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "父文件夹ID不能为空");
        }

        return documentFolderMapper.findByParentId(parentId);
    }

    @Override
    public List<DocumentFolder> listByProjectIdAndParentId(Long projectId, Long parentId) {
        log.info("查询项目中指定父文件夹下的子文件夹, projectId={}, parentId={}", projectId, parentId);

        if (projectId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "项目ID不能为空");
        }

        if (parentId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "父文件夹ID不能为空");
        }

        return documentFolderMapper.findByProjectIdAndParentId(projectId, parentId);
    }

    @Override
    public List<IDocumentFolderService.DocumentFolderTreeNode> getFolderTree(Long projectId) {
        log.info("获取文件夹树形结构, projectId={}", projectId);

        if (projectId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "项目ID不能为空");
        }

        // 查询项目的所有文件夹
        List<DocumentFolder> allFolders = listByProjectId(projectId);

        // 构建树形结构
        return buildFolderTree(allFolders);
    }

    @Override
    public PageResult<DocumentFolder> listFolders(Integer page, Integer pageSize,
                                                  Long projectId, Long parentId) {
        log.info("分页查询文件夹列表, page={}, pageSize={}, projectId={}, parentId={}",
                page, pageSize, projectId, parentId);

        // 构建分页对象
        Page<DocumentFolder> pageParam = new Page<>(page, pageSize);

        // 构建查询条件
        LambdaQueryWrapper<DocumentFolder> queryWrapper = new LambdaQueryWrapper<>();

        if (projectId != null) {
            queryWrapper.eq(DocumentFolder::getProjectId, projectId);
        }
        if (parentId != null) {
            queryWrapper.eq(DocumentFolder::getParentId, parentId);
        }
        queryWrapper.orderByAsc(DocumentFolder::getSortOrder);

        // 执行查询
        IPage<DocumentFolder> result = documentFolderMapper.selectPage(pageParam, queryWrapper);

        log.info("查询文件夹列表成功, total={}", result.getTotal());
        return PageResult.of(result.getRecords(), result.getTotal(), page, pageSize);
    }

    /**
     * 根据文件夹ID列表获取文件夹名称映射
     *
     * @param folderIds 文件夹ID列表
     * @return 文件夹ID到文件夹名称的映射
     */
    @Override
    public Map<Long, String> getFolderNamesByIds(List<Long> folderIds) {
        log.info("批量获取文件夹名称, folderIds={}", folderIds);

        if (folderIds == null || folderIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // 查询文件夹列表
        LambdaQueryWrapper<DocumentFolder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(DocumentFolder::getId, folderIds);
        List<DocumentFolder> folders = documentFolderMapper.selectList(queryWrapper);
        if (folders == null || folders.isEmpty()) {
            return Collections.emptyMap();
        }

        // 构建ID到名称的映射
        return folders.stream()
                .collect(Collectors.toMap(DocumentFolder::getId, DocumentFolder::getName));
    }

    /**
     * 验证文件夹信息
     *
     * @param folder    文件夹实体
     * @param isCreate  是否为创建操作
     */
    private void validateFolder(DocumentFolder folder, boolean isCreate) {
        if (folder == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "文件夹信息不能为空");
        }

        if (isCreate) {
            if (StringUtils.isBlank(folder.getName())) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "文件夹名称不能为空");
            }
            if (folder.getProjectId() == null) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "项目ID不能为空");
            }
            if (folder.getCreatorId() == null) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "创建人ID不能为空");
            }
        }

        // 验证名称长度
        if (StringUtils.isNotBlank(folder.getName()) && folder.getName().length() > 100) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "文件夹名称长度不能超过100");
        }
    }

    /**
     * 构建文件夹树形结构
     *
     * @param folders 文件夹列表
     * @return 树形结构列表
     */
    private List<IDocumentFolderService.DocumentFolderTreeNode> buildFolderTree(List<DocumentFolder> folders) {
        if (folders == null || folders.isEmpty()) {
            return Collections.emptyList();
        }

        // 转换为TreeNode列表
        List<IDocumentFolderService.DocumentFolderTreeNode> nodes = folders.stream()
                .map(this::convertToTreeNode)
                .collect(Collectors.toList());

        // 构建父子关系
        Map<Long, IDocumentFolderService.DocumentFolderTreeNode> nodeMap = new HashMap<>();
        List<IDocumentFolderService.DocumentFolderTreeNode> rootNodes = new ArrayList<>();

        // 建立ID到节点的映射
        for (IDocumentFolderService.DocumentFolderTreeNode node : nodes) {
            nodeMap.put(node.getId(), node);
        }

        // 构建树形结构
        for (IDocumentFolderService.DocumentFolderTreeNode node : nodes) {
            Long parentId = node.getParentId();
            if (parentId == null || parentId == 0) {
                // 根节点
                rootNodes.add(node);
            } else {
                // 子节点
                IDocumentFolderService.DocumentFolderTreeNode parentNode = nodeMap.get(parentId);
                if (parentNode != null) {
                    if (parentNode.getChildren() == null) {
                        parentNode.setChildren(new ArrayList<>());
                    }
                    parentNode.getChildren().add(node);
                }
            }
        }

        return rootNodes;
    }

    /**
     * 将DocumentFolder转换为DocumentFolderTreeNode
     *
     * @param folder 文件夹实体
     * @return 树节点
     */
    private IDocumentFolderService.DocumentFolderTreeNode convertToTreeNode(DocumentFolder folder) {
        IDocumentFolderService.DocumentFolderTreeNode node = new IDocumentFolderService.DocumentFolderTreeNode();
        node.setId(folder.getId());
        node.setName(folder.getName());
        node.setDescription(folder.getDescription());
        node.setProjectId(folder.getProjectId());
        node.setParentId(folder.getParentId());
        // 获取文件夹下的文档数量
        node.setDocumentCount(documentFolderMapper.countDocumentsInFolder(folder.getId()));
        return node;
    }
}