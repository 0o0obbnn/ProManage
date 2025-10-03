# ProManage UI/UX 设计文档 - Part 4: 核心功能UI/UX设计

## 1. 文档管理功能设计

### 1.1 功能概述
```
核心目标:
- 建立单一真实数据源
- 实现文档版本控制
- 支持实时协作编辑
- 提供智能搜索能力
- 确保文档安全性

业务价值:
- 团队协作效率提升 50%
- 减少信息查找时间 60%
- 降低版本混乱问题 80%
```

### 1.2 文档库主界面

```typescript
<template>
  <div class="document-library">
    <!-- 文档库头部 -->
    <div class="document-header">
      <div class="header-left">
        <a-breadcrumb>
          <a-breadcrumb-item>
            <a @click="navigateToRoot">
              <HomeOutlined /> 文档库
            </a>
          </a-breadcrumb-item>
          <a-breadcrumb-item
            v-for="(folder, index) in breadcrumbPath"
            :key="folder.id"
          >
            <a @click="navigateToFolder(folder)">{{ folder.name }}</a>
          </a-breadcrumb-item>
        </a-breadcrumb>
      </div>

      <div class="header-right">
        <a-space>
          <a-button type="primary" @click="handleUploadDocument">
            <template #icon><UploadOutlined /></template>
            上传文档
          </a-button>
          <a-button @click="handleCreateDocument">
            <template #icon><FileAddOutlined /></template>
            新建文档
          </a-button>
          <a-button @click="handleCreateFolder">
            <template #icon><FolderAddOutlined /></template>
            新建文件夹
          </a-button>
        </a-space>
      </div>
    </div>

    <!-- 搜索和筛选工具栏 -->
    <div class="document-toolbar">
      <a-space size="large">
        <!-- 全局搜索 -->
        <a-input-search
          v-model:value="searchQuery"
          placeholder="搜索文档名称、内容、标签..."
          style="width: 400px"
          allow-clear
          @search="handleSearch"
        >
          <template #prefix>
            <SearchOutlined />
          </template>
          <template #suffix>
            <a-tooltip title="高级搜索">
              <FilterOutlined @click="showAdvancedSearch = true" />
            </a-tooltip>
          </template>
        </a-input-search>

        <!-- 筛选器 -->
        <a-select
          v-model:value="filterType"
          placeholder="文档类型"
          style="width: 150px"
          allow-clear
        >
          <a-select-option value="all">全部类型</a-select-option>
          <a-select-option value="word">Word文档</a-select-option>
          <a-select-option value="excel">Excel表格</a-select-option>
          <a-select-option value="pdf">PDF</a-select-option>
          <a-select-option value="markdown">Markdown</a-select-option>
          <a-select-option value="code">代码文件</a-select-option>
        </a-select>

        <a-select
          v-model:value="filterProject"
          placeholder="项目"
          style="width: 150px"
          allow-clear
          show-search
        >
          <a-select-option
            v-for="project in projects"
            :key="project.id"
            :value="project.id"
          >
            {{ project.name }}
          </a-select-option>
        </a-select>

        <a-select
          v-model:value="sortBy"
          style="width: 150px"
        >
          <a-select-option value="updated_desc">最近更新</a-select-option>
          <a-select-option value="created_desc">最新创建</a-select-option>
          <a-select-option value="name_asc">名称A-Z</a-select-option>
          <a-select-option value="name_desc">名称Z-A</a-select-option>
          <a-select-option value="size_desc">大小降序</a-select-option>
        </a-select>

        <!-- 视图切换 -->
        <a-radio-group v-model:value="viewMode" button-style="solid">
          <a-radio-button value="list">
            <UnorderedListOutlined /> 列表
          </a-radio-button>
          <a-radio-button value="grid">
            <AppstoreOutlined /> 网格
          </a-radio-button>
          <a-radio-button value="tree">
            <PartitionOutlined /> 树形
          </a-radio-button>
        </a-radio-group>
      </a-space>
    </div>

    <!-- 文档内容区 -->
    <div class="document-content">
      <a-row :gutter="16">
        <!-- 左侧目录树 -->
        <a-col :xs="0" :lg="6">
          <a-card :bordered="false" class="folder-tree-card">
            <template #title>
              <a-space>
                <FolderOutlined />
                <span>文件夹</span>
              </a-space>
            </template>

            <a-tree
              v-model:selectedKeys="selectedFolderKeys"
              v-model:expandedKeys="expandedFolderKeys"
              :tree-data="folderTree"
              :field-names="{ title: 'name', key: 'id' }"
              show-icon
              @select="handleFolderSelect"
            >
              <template #icon="{ dataRef }">
                <FolderOpenOutlined v-if="expandedFolderKeys.includes(dataRef.id)" />
                <FolderOutlined v-else />
              </template>

              <template #title="{ dataRef }">
                <a-dropdown :trigger="['contextmenu']">
                  <span>{{ dataRef.name }}</span>
                  <template #overlay>
                    <a-menu>
                      <a-menu-item @click="handleRenameFolder(dataRef)">
                        <EditOutlined /> 重命名
                      </a-menu-item>
                      <a-menu-item @click="handleMoveFolder(dataRef)">
                        <DragOutlined /> 移动
                      </a-menu-item>
                      <a-menu-divider />
                      <a-menu-item danger @click="handleDeleteFolder(dataRef)">
                        <DeleteOutlined /> 删除
                      </a-menu-item>
                    </a-menu>
                  </template>
                </a-dropdown>
              </template>
            </a-tree>
          </a-card>
        </a-col>

        <!-- 右侧文档列表 -->
        <a-col :xs="24" :lg="18">
          <!-- 列表视图 -->
          <a-card v-if="viewMode === 'list'" :bordered="false">
            <a-table
              :columns="documentColumns"
              :data-source="documents"
              :loading="loading"
              :pagination="pagination"
              :row-selection="rowSelection"
              :row-key="record => record.id"
              @change="handleTableChange"
            >
              <template #bodyCell="{ column, record }">
                <!-- 文档名称 -->
                <template v-if="column.key === 'name'">
                  <a-space>
                    <component
                      :is="getFileIcon(record.fileType)"
                      :style="{ fontSize: '24px', color: getFileIconColor(record.fileType) }"
                    />
                    <div>
                      <a @click="handleOpenDocument(record)">{{ record.name }}</a>
                      <div class="document-meta">
                        <a-space split="|" size="small">
                          <span>{{ record.projectName }}</span>
                          <span>{{ formatFileSize(record.size) }}</span>
                          <span v-if="record.isLocked">
                            <LockOutlined style="color: #faad14" /> 编辑中
                          </span>
                        </a-space>
                      </div>
                    </div>
                  </a-space>
                </template>

                <!-- 版本号 -->
                <template v-else-if="column.key === 'version'">
                  <a-dropdown>
                    <a>v{{ record.version }} <DownOutlined /></a>
                    <template #overlay>
                      <a-menu>
                        <a-menu-item @click="handleViewVersionHistory(record)">
                          <HistoryOutlined /> 版本历史
                        </a-menu-item>
                        <a-menu-item @click="handleCompareVersions(record)">
                          <DiffOutlined /> 版本对比
                        </a-menu-item>
                      </a-menu>
                    </template>
                  </a-dropdown>
                </template>

                <!-- 最后编辑者 -->
                <template v-else-if="column.key === 'lastEditor'">
                  <a-space>
                    <a-avatar :src="record.lastEditor.avatar" :size="24" />
                    <span>{{ record.lastEditor.name }}</span>
                  </a-space>
                </template>

                <!-- 标签 -->
                <template v-else-if="column.key === 'tags'">
                  <a-space wrap>
                    <a-tag
                      v-for="tag in record.tags.slice(0, 2)"
                      :key="tag"
                      color="blue"
                      size="small"
                    >
                      {{ tag }}
                    </a-tag>
                    <a-tag
                      v-if="record.tags.length > 2"
                      size="small"
                    >
                      +{{ record.tags.length - 2 }}
                    </a-tag>
                  </a-space>
                </template>

                <!-- 操作 -->
                <template v-else-if="column.key === 'actions'">
                  <a-space>
                    <a-button
                      type="link"
                      size="small"
                      @click="handleOpenDocument(record)"
                    >
                      查看
                    </a-button>
                    <a-button
                      type="link"
                      size="small"
                      @click="handleEditDocument(record)"
                    >
                      编辑
                    </a-button>
                    <a-dropdown>
                      <a-button type="link" size="small">
                        更多 <DownOutlined />
                      </a-button>
                      <template #overlay>
                        <a-menu>
                          <a-menu-item @click="handleDownloadDocument(record)">
                            <DownloadOutlined /> 下载
                          </a-menu-item>
                          <a-menu-item @click="handleShareDocument(record)">
                            <ShareAltOutlined /> 分享
                          </a-menu-item>
                          <a-menu-item @click="handleMoveDocument(record)">
                            <DragOutlined /> 移动
                          </a-menu-item>
                          <a-menu-item @click="handleCopyDocument(record)">
                            <CopyOutlined /> 复制
                          </a-menu-item>
                          <a-menu-divider />
                          <a-menu-item @click="handleViewHistory(record)">
                            <HistoryOutlined /> 历史记录
                          </a-menu-item>
                          <a-menu-item danger @click="handleDeleteDocument(record)">
                            <DeleteOutlined /> 删除
                          </a-menu-item>
                        </a-menu>
                      </template>
                    </a-dropdown>
                  </a-space>
                </template>
              </template>
            </a-table>
          </a-card>

          <!-- 网格视图 -->
          <a-card v-else-if="viewMode === 'grid'" :bordered="false">
            <a-row :gutter="[16, 16]">
              <a-col
                v-for="doc in documents"
                :key="doc.id"
                :xs="24"
                :sm="12"
                :md="8"
                :xl="6"
              >
                <a-card
                  hoverable
                  class="document-card"
                  @click="handleOpenDocument(doc)"
                >
                  <!-- 文档预览图 -->
                  <div class="doc-preview">
                    <img
                      v-if="doc.thumbnail"
                      :src="doc.thumbnail"
                      :alt="doc.name"
                    />
                    <div v-else class="doc-icon-placeholder">
                      <component
                        :is="getFileIcon(doc.fileType)"
                        :style="{ fontSize: '48px', color: getFileIconColor(doc.fileType) }"
                      />
                    </div>

                    <!-- 悬浮操作层 -->
                    <div class="doc-overlay">
                      <a-space>
                        <a-button
                          type="primary"
                          shape="circle"
                          @click.stop="handleOpenDocument(doc)"
                        >
                          <EyeOutlined />
                        </a-button>
                        <a-button
                          shape="circle"
                          @click.stop="handleDownloadDocument(doc)"
                        >
                          <DownloadOutlined />
                        </a-button>
                        <a-button
                          shape="circle"
                          @click.stop="handleShareDocument(doc)"
                        >
                          <ShareAltOutlined />
                        </a-button>
                      </a-space>
                    </div>

                    <!-- 文件类型标签 -->
                    <a-tag
                      :color="getFileTypeColor(doc.fileType)"
                      class="file-type-tag"
                    >
                      {{ doc.fileExtension }}
                    </a-tag>
                  </div>

                  <!-- 文档信息 -->
                  <a-card-meta
                    :title="doc.name"
                    :description="doc.projectName"
                  >
                    <template #avatar>
                      <a-avatar :src="doc.lastEditor.avatar" />
                    </template>
                  </a-card-meta>

                  <!-- 底部元信息 -->
                  <div class="doc-footer">
                    <a-space split="|" size="small">
                      <span>
                        <EyeOutlined /> {{ doc.viewCount }}
                      </span>
                      <span>
                        <CommentOutlined /> {{ doc.commentCount }}
                      </span>
                    </a-space>
                    <span class="update-time">
                      {{ formatRelativeTime(doc.updatedAt) }}
                    </span>
                  </div>
                </a-card>
              </a-col>
            </a-row>
          </a-card>

          <!-- 树形视图 -->
          <a-card v-else-if="viewMode === 'tree'" :bordered="false">
            <a-tree
              v-model:expandedKeys="expandedDocKeys"
              :tree-data="documentTree"
              show-icon
              draggable
              @drop="handleDocumentDrop"
            >
              <template #icon="{ dataRef }">
                <FolderOutlined v-if="dataRef.isFolder" />
                <component :is="getFileIcon(dataRef.fileType)" v-else />
              </template>

              <template #title="{ dataRef }">
                <div class="tree-node-title">
                  <span @click="handleOpenDocument(dataRef)">
                    {{ dataRef.name }}
                  </span>
                  <a-space class="tree-node-actions">
                    <a-button
                      type="link"
                      size="small"
                      @click.stop="handleEditDocument(dataRef)"
                    >
                      <EditOutlined />
                    </a-button>
                    <a-dropdown>
                      <a-button type="link" size="small" @click.stop>
                        <MoreOutlined />
                      </a-button>
                      <template #overlay>
                        <a-menu>
                          <a-menu-item @click="handleDownloadDocument(dataRef)">
                            下载
                          </a-menu-item>
                          <a-menu-item @click="handleShareDocument(dataRef)">
                            分享
                          </a-menu-item>
                          <a-menu-item danger @click="handleDeleteDocument(dataRef)">
                            删除
                          </a-menu-item>
                        </a-menu>
                      </template>
                    </a-dropdown>
                  </a-space>
                </div>
              </template>
            </a-tree>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 批量操作栏 -->
    <div v-if="selectedRowKeys.length > 0" class="batch-action-bar">
      <a-space>
        <span>已选择 {{ selectedRowKeys.length }} 项</span>
        <a-button @click="handleBatchDownload">
          <DownloadOutlined /> 批量下载
        </a-button>
        <a-button @click="handleBatchMove">
          <DragOutlined /> 批量移动
        </a-button>
        <a-button @click="handleBatchTag">
          <TagOutlined /> 批量标签
        </a-button>
        <a-button danger @click="handleBatchDelete">
          <DeleteOutlined /> 批量删除
        </a-button>
        <a-button type="link" @click="handleClearSelection">
          清空选择
        </a-button>
      </a-space>
    </div>

    <!-- 高级搜索抽屉 -->
    <a-drawer
      v-model:visible="showAdvancedSearch"
      title="高级搜索"
      :width="480"
    >
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="关键词">
          <a-input v-model:value="advancedSearch.keyword" placeholder="搜索内容" />
        </a-form-item>

        <a-form-item label="文档类型">
          <a-select v-model:value="advancedSearch.fileTypes" mode="multiple">
            <a-select-option value="doc">Word</a-select-option>
            <a-select-option value="xls">Excel</a-select-option>
            <a-select-option value="pdf">PDF</a-select-option>
            <a-select-option value="md">Markdown</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="所属项目">
          <a-select v-model:value="advancedSearch.projects" mode="multiple">
            <a-select-option
              v-for="project in projects"
              :key="project.id"
              :value="project.id"
            >
              {{ project.name }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="创建人">
          <a-select
            v-model:value="advancedSearch.authors"
            mode="multiple"
            show-search
          >
            <a-select-option
              v-for="user in users"
              :key="user.id"
              :value="user.id"
            >
              {{ user.name }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="标签">
          <a-select v-model:value="advancedSearch.tags" mode="tags" />
        </a-form-item>

        <a-form-item label="创建时间">
          <a-range-picker v-model:value="advancedSearch.dateRange" />
        </a-form-item>

        <a-form-item label="文件大小">
          <a-input-group compact>
            <a-input-number
              v-model:value="advancedSearch.minSize"
              placeholder="最小"
              style="width: 45%"
            />
            <a-input style="width: 10%; text-align: center" placeholder="~" disabled />
            <a-input-number
              v-model:value="advancedSearch.maxSize"
              placeholder="最大"
              style="width: 45%"
            />
          </a-input-group>
        </a-form-item>

        <a-form-item :wrapper-col="{ offset: 6, span: 18 }">
          <a-space>
            <a-button type="primary" @click="handleAdvancedSearch">
              搜索
            </a-button>
            <a-button @click="handleResetSearch">
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-drawer>
  </div>
</template>

<style scoped lang="scss">
.document-library {
  .document-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 24px;
    background: #ffffff;
    border-radius: 4px;
    margin-bottom: 16px;
  }

  .document-toolbar {
    padding: 16px 24px;
    background: #ffffff;
    border-radius: 4px;
    margin-bottom: 16px;
  }

  .document-content {
    .folder-tree-card {
      height: calc(100vh - 300px);
      overflow-y: auto;
    }

    .document-meta {
      color: #8c8c8c;
      font-size: 12px;
      margin-top: 4px;
    }

    .document-card {
      transition: all 0.3s;

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);

        .doc-overlay {
          opacity: 1;
        }
      }

      .doc-preview {
        position: relative;
        width: 100%;
        height: 160px;
        background: #f5f5f5;
        border-radius: 4px;
        overflow: hidden;

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }

        .doc-icon-placeholder {
          width: 100%;
          height: 100%;
          display: flex;
          align-items: center;
          justify-content: center;
        }

        .doc-overlay {
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          bottom: 0;
          background: rgba(0, 0, 0, 0.6);
          display: flex;
          align-items: center;
          justify-content: center;
          opacity: 0;
          transition: opacity 0.3s;
        }

        .file-type-tag {
          position: absolute;
          top: 8px;
          right: 8px;
        }
      }

      .doc-footer {
        margin-top: 12px;
        display: flex;
        justify-content: space-between;
        align-items: center;
        color: #8c8c8c;
        font-size: 12px;

        .update-time {
          color: #bfbfbf;
        }
      }
    }

    .tree-node-title {
      display: flex;
      justify-content: space-between;
      align-items: center;
      width: 100%;

      .tree-node-actions {
        opacity: 0;
        transition: opacity 0.3s;
      }

      &:hover .tree-node-actions {
        opacity: 1;
      }
    }
  }

  .batch-action-bar {
    position: fixed;
    bottom: 24px;
    left: 50%;
    transform: translateX(-50%);
    padding: 16px 24px;
    background: #ffffff;
    border-radius: 4px;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
    z-index: 100;
  }
}
</style>
```

### 1.3 文档在线编辑器

```typescript
<template>
  <div class="document-editor">
    <!-- 编辑器头部 -->
    <div class="editor-header">
      <div class="header-left">
        <a-button type="text" @click="handleBack">
          <ArrowLeftOutlined />
        </a-button>
        <a-input
          v-model:value="documentTitle"
          class="doc-title-input"
          placeholder="无标题文档"
          :bordered="false"
          @blur="handleTitleChange"
        />
        <a-tag v-if="isSaving" color="blue">
          <LoadingOutlined /> 保存中...
        </a-tag>
        <a-tag v-else-if="lastSaved" color="green">
          <CheckOutlined /> 已保存于 {{ formatTime(lastSaved) }}
        </a-tag>
      </div>

      <div class="header-right">
        <a-space>
          <!-- 协作者 -->
          <a-avatar-group :max-count="5">
            <a-tooltip
              v-for="collaborator in collaborators"
              :key="collaborator.id"
              :title="`${collaborator.name} (${collaborator.status})`"
            >
              <a-avatar :src="collaborator.avatar">
                <template #icon><UserOutlined /></template>
              </a-avatar>
            </a-tooltip>
          </a-avatar-group>

          <!-- 分享按钮 -->
          <a-button @click="handleShare">
            <ShareAltOutlined /> 分享
          </a-button>

          <!-- 版本历史 -->
          <a-dropdown>
            <a-button>
              <HistoryOutlined /> v{{ currentVersion }}
            </a-button>
            <template #overlay>
              <a-menu @click="handleVersionSelect">
                <a-menu-item
                  v-for="version in versions"
                  :key="version.id"
                >
                  v{{ version.number }} - {{ formatTime(version.createdAt) }}
                  <br />
                  <span style="color: #8c8c8c; font-size: 12px">
                    {{ version.author.name }}
                  </span>
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>

          <!-- 更多操作 -->
          <a-dropdown>
            <a-button>
              <MoreOutlined />
            </a-button>
            <template #overlay>
              <a-menu>
                <a-menu-item @click="handleExportPDF">
                  <FilePdfOutlined /> 导出为PDF
                </a-menu-item>
                <a-menu-item @click="handleExportWord">
                  <FileWordOutlined /> 导出为Word
                </a-menu-item>
                <a-menu-item @click="handlePrint">
                  <PrinterOutlined /> 打印
                </a-menu-item>
                <a-menu-divider />
                <a-menu-item @click="handleDocumentSettings">
                  <SettingOutlined /> 文档设置
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </a-space>
      </div>
    </div>

    <!-- 编辑器工具栏 -->
    <div class="editor-toolbar">
      <a-space>
        <!-- 格式化工具 -->
        <a-button-group>
          <a-button @click="handleFormat('bold')">
            <BoldOutlined />
          </a-button>
          <a-button @click="handleFormat('italic')">
            <ItalicOutlined />
          </a-button>
          <a-button @click="handleFormat('underline')">
            <UnderlineOutlined />
          </a-button>
          <a-button @click="handleFormat('strikethrough')">
            <StrikethroughOutlined />
          </a-button>
        </a-button-group>

        <a-divider type="vertical" />

        <!-- 标题工具 -->
        <a-select
          v-model:value="currentHeading"
          style="width: 120px"
          @change="handleHeadingChange"
        >
          <a-select-option value="p">正文</a-select-option>
          <a-select-option value="h1">标题 1</a-select-option>
          <a-select-option value="h2">标题 2</a-select-option>
          <a-select-option value="h3">标题 3</a-select-option>
          <a-select-option value="h4">标题 4</a-select-option>
        </a-select>

        <a-divider type="vertical" />

        <!-- 列表工具 -->
        <a-button-group>
          <a-button @click="handleFormat('orderedList')">
            <OrderedListOutlined />
          </a-button>
          <a-button @click="handleFormat('unorderedList')">
            <UnorderedListOutlined />
          </a-button>
        </a-button-group>

        <a-divider type="vertical" />

        <!-- 插入工具 -->
        <a-dropdown>
          <a-button>
            插入 <DownOutlined />
          </a-button>
          <template #overlay>
            <a-menu>
              <a-menu-item @click="handleInsert('image')">
                <PictureOutlined /> 图片
              </a-menu-item>
              <a-menu-item @click="handleInsert('link')">
                <LinkOutlined /> 链接
              </a-menu-item>
              <a-menu-item @click="handleInsert('table')">
                <TableOutlined /> 表格
              </a-menu-item>
              <a-menu-item @click="handleInsert('code')">
                <CodeOutlined /> 代码块
              </a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>

        <a-divider type="vertical" />

        <!-- 对齐工具 -->
        <a-button-group>
          <a-button @click="handleAlign('left')">
            <AlignLeftOutlined />
          </a-button>
          <a-button @click="handleAlign('center')">
            <AlignCenterOutlined />
          </a-button>
          <a-button @click="handleAlign('right')">
            <AlignRightOutlined />
          </a-button>
        </a-button-group>
      </a-space>
    </div>

    <!-- 编辑器主体 -->
    <div class="editor-container">
      <a-row :gutter="16">
        <!-- 编辑区 -->
        <a-col :xs="24" :lg="showSidebar ? 18 : 24">
          <div class="editor-content">
            <!-- 使用富文本编辑器组件 -->
            <RichTextEditor
              v-model:value="documentContent"
              :readonly="isReadonly"
              @change="handleContentChange"
              @cursor-position="handleCursorPosition"
            />
          </div>
        </a-col>

        <!-- 侧边栏 -->
        <a-col v-if="showSidebar" :xs="24" :lg="6">
          <a-card :bordered="false" class="editor-sidebar">
            <a-tabs v-model:activeKey="sidebarTab">
              <!-- 评论 -->
              <a-tab-pane key="comments" tab="评论">
                <template #tab>
                  <a-badge :count="commentCount" :offset="[10, 0]">
                    <CommentOutlined /> 评论
                  </a-badge>
                </template>

                <a-list :data-source="comments" size="small">
                  <template #renderItem="{ item }">
                    <a-comment
                      :author="item.author.name"
                      :avatar="item.author.avatar"
                      :content="item.content"
                      :datetime="formatTime(item.createdAt)"
                    >
                      <template #actions>
                        <span @click="handleReplyComment(item)">回复</span>
                        <span
                          v-if="item.resolved"
                          style="color: #52c41a"
                        >
                          <CheckOutlined /> 已解决
                        </span>
                        <span v-else @click="handleResolveComment(item)">
                          解决
                        </span>
                      </template>
                    </a-comment>
                  </template>
                </a-list>

                <a-textarea
                  v-model:value="newComment"
                  :rows="3"
                  placeholder="添加评论..."
                  style="margin-top: 12px"
                />
                <a-button
                  type="primary"
                  block
                  style="margin-top: 8px"
                  :disabled="!newComment"
                  @click="handleAddComment"
                >
                  发表评论
                </a-button>
              </a-tab-pane>

              <!-- 大纲 -->
              <a-tab-pane key="outline" tab="大纲">
                <template #tab>
                  <OrderedListOutlined /> 大纲
                </template>

                <a-tree
                  :tree-data="outlineTree"
                  :selected-keys="selectedOutlineKeys"
                  @select="handleOutlineSelect"
                >
                  <template #title="{ title }">
                    <a>{{ title }}</a>
                  </template>
                </a-tree>
              </a-tab-pane>

              <!-- 活动 -->
              <a-tab-pane key="activity" tab="活动">
                <template #tab>
                  <ClockCircleOutlined /> 活动
                </template>

                <a-timeline>
                  <a-timeline-item
                    v-for="activity in activities"
                    :key="activity.id"
                  >
                    <p>
                      <a-avatar :src="activity.user.avatar" :size="20" />
                      <strong>{{ activity.user.name }}</strong>
                      {{ activity.action }}
                    </p>
                    <p style="color: #8c8c8c; font-size: 12px">
                      {{ formatTime(activity.createdAt) }}
                    </p>
                  </a-timeline-item>
                </a-timeline>
              </a-tab-pane>
            </a-tabs>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 底部状态栏 -->
    <div class="editor-footer">
      <a-space>
        <span>{{ wordCount }} 字</span>
        <a-divider type="vertical" />
        <span>行 {{ cursorLine }}，列 {{ cursorColumn }}</span>
        <a-divider type="vertical" />
        <a-switch
          v-model:checked="showSidebar"
          checked-children="显示侧边栏"
          un-checked-children="隐藏侧边栏"
        />
      </a-space>
    </div>
  </div>
</template>

<style scoped lang="scss">
.document-editor {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;

  .editor-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 24px;
    background: #ffffff;
    border-bottom: 1px solid #f0f0f0;

    .header-left {
      display: flex;
      align-items: center;
      gap: 12px;

      .doc-title-input {
        font-size: 16px;
        font-weight: 600;
        width: 300px;
      }
    }
  }

  .editor-toolbar {
    padding: 12px 24px;
    background: #ffffff;
    border-bottom: 1px solid #f0f0f0;
  }

  .editor-container {
    flex: 1;
    overflow-y: auto;
    padding: 24px;

    .editor-content {
      background: #ffffff;
      min-height: calc(100vh - 250px);
      padding: 40px 60px;
      border-radius: 4px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    }

    .editor-sidebar {
      position: sticky;
      top: 24px;
      max-height: calc(100vh - 250px);
      overflow-y: auto;
    }
  }

  .editor-footer {
    padding: 8px 24px;
    background: #ffffff;
    border-top: 1px solid #f0f0f0;
    color: #8c8c8c;
    font-size: 12px;
  }
}
</style>
```

---

**文档持续...**

**下一部分内容预览**:
- 任务管理功能UI/UX设计
- 变更管理功能UI/UX设计
- 测试管理功能UI/UX设计
- 搜索功能UI/UX设计
- 通知系统UI/UX设计

**文档版本**: v1.0
**创建日期**: 2025-09-30