<template>
  <div class="document-page">
    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <a-button type="primary" @click="showCreateModal">
          <template #icon><FileAddOutlined /></template>
          新建文档
        </a-button>
        <a-button @click="showUploadModal">
          <template #icon><UploadOutlined /></template>
          上传文档
        </a-button>
        <a-button @click="showCreateFolderModal">
          <template #icon><FolderAddOutlined /></template>
          新建文件夹
        </a-button>
        <a-dropdown v-if="selectedCount > 0">
          <a-button>
            批量操作 ({{ selectedCount }})
            <DownOutlined />
          </a-button>
          <template #overlay>
            <a-menu>
              <a-menu-item key="move" @click="showBatchMoveModal">
                <FolderOutlined /> 移动
              </a-menu-item>
              <a-menu-item key="download" @click="handleBatchDownload">
                <DownloadOutlined /> 下载
              </a-menu-item>
              <a-menu-item key="delete" danger @click="handleBatchDelete">
                <DeleteOutlined /> 删除
              </a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </div>

      <div class="toolbar-right">
        <a-input-search
          v-model:value="searchKeyword"
          placeholder="搜索文档名称、标签..."
          style="width: 280px"
          @search="handleSearch"
        />
        <a-button @click="showFilterDrawer">
          <template #icon><FilterOutlined /></template>
          筛选
        </a-button>
        <a-radio-group v-model:value="currentViewMode" button-style="solid" @change="handleViewModeChange">
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
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="content-wrapper">
      <!-- 左侧文件夹树 -->
      <div v-if="showFolderTree" class="folder-sidebar">
        <div class="sidebar-header">
          <span>文件夹</span>
          <a-button type="text" size="small" @click="showFolderTree = false">
            <template #icon><CloseOutlined /></template>
          </a-button>
        </div>
        <a-tree
          v-model:selectedKeys="selectedFolderKeys"
          :tree-data="folderTreeData"
          :field-names="{ key: 'id', title: 'name', children: 'children' }"
          @select="handleFolderSelect"
        >
          <template #title="{ name, documentCount }">
            <span>{{ name }} <span class="folder-count">({{ documentCount }})</span></span>
          </template>
        </a-tree>
      </div>

      <!-- 主内容区 -->
      <div class="main-content">
        <!-- 面包屑 -->
        <div v-if="currentFolderPath" class="breadcrumb">
          <a-breadcrumb>
            <a-breadcrumb-item>
              <a @click="handleFolderSelect([])">全部文档</a>
            </a-breadcrumb-item>
            <a-breadcrumb-item v-for="folder in currentFolderPath" :key="folder.id">
              <a @click="handleFolderSelect([folder.id])">{{ folder.name }}</a>
            </a-breadcrumb-item>
          </a-breadcrumb>
        </div>

        <!-- 筛选标签 -->
        <div v-if="hasActiveFilters" class="filter-tags">
          <a-tag
            v-for="filter in activeFilters"
            :key="filter.key"
            closable
            @close="removeFilter(filter.key)"
          >
            {{ filter.label }}: {{ filter.value }}
          </a-tag>
          <a-button type="link" size="small" @click="clearAllFilters">
            清空筛选
          </a-button>
        </div>

        <!-- 文档列表 -->
        <a-spin :spinning="loading">
          <!-- 列表视图 -->
          <div v-if="currentViewMode === 'list'" class="list-view">
            <a-table
              :columns="columns"
              :data-source="documentList"
              :pagination="false"
              :row-selection="rowSelection"
              :loading="loading"
              row-key="id"
              @change="handleTableChange"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'name'">
                  <div class="document-name">
                    <component :is="getFileIcon(record.fileType)" class="file-icon" />
                    <a @click="goToDetail(record.id)">{{ record.name }}</a>
                    <a-tag v-if="record.isTemplate" color="blue">模板</a-tag>
                  </div>
                </template>
                <template v-else-if="column.key === 'size'">
                  {{ formatFileSize(record.size) }}
                </template>
                <template v-else-if="column.key === 'priority'">
                  <a-tag v-if="record.priority === 1" color="green">低</a-tag>
                  <a-tag v-else-if="record.priority === 2" color="orange">中</a-tag>
                  <a-tag v-else-if="record.priority === 3" color="red">高</a-tag>
                </template>
                <template v-else-if="column.key === 'tags'">
                  <a-tag v-for="tag in record.tags" :key="tag" color="blue">
                    {{ tag }}
                  </a-tag>
                </template>
                <template v-else-if="column.key === 'status'">
                  <a-tag v-if="record.status === 'DRAFT'" color="orange">草稿</a-tag>
                  <a-tag v-else-if="record.status === 'UNDER_REVIEW'" color="blue">审核中</a-tag>
                  <a-tag v-else-if="record.status === 'APPROVED'" color="green">已批准</a-tag>
                  <a-tag v-else-if="record.status === 'ARCHIVED'" color="gray">已归档</a-tag>
                  <a-tag v-else-if="record.status === 'DEPRECATED'" color="red">已废弃</a-tag>
                </template>
                <template v-else-if="column.key === 'author'">
                  <div class="author-cell">
                    <a-avatar :src="record.author.avatar" :size="24">
                      {{ record.author.name.charAt(0) }}
                    </a-avatar>
                    <span>{{ record.author.name }}</span>
                  </div>
                </template>
                <template v-else-if="column.key === 'createdAt'">
                  {{ formatDate(record.createdAt) }}
                </template>
                <template v-else-if="column.key === 'actions'">
                  <a-space>
                    <a-button type="link" size="small" @click="handleDownload(record)">
                      下载
                    </a-button>
                    <a-dropdown>
                      <a-button type="link" size="small">
                        更多 <DownOutlined />
                      </a-button>
                      <template #overlay>
                        <a-menu>
                          <a-menu-item @click="handleShare(record)">
                            <ShareAltOutlined /> 分享
                          </a-menu-item>
                          <a-menu-item @click="handleMove(record)">
                            <FolderOutlined /> 移动
                          </a-menu-item>
                          <a-menu-item @click="handleCopy(record)">
                            <CopyOutlined /> 复制
                          </a-menu-item>
                          <a-menu-divider />
                          <a-menu-item danger @click="handleDelete(record)">
                            <DeleteOutlined /> 删除
                          </a-menu-item>
                        </a-menu>
                      </template>
                    </a-dropdown>
                  </a-space>
                </template>
              </template>
            </a-table>
          </div>

          <!-- 网格视图 -->
          <div v-else-if="currentViewMode === 'grid'" class="grid-view">
            <div class="document-grid">
              <div
                v-for="doc in documentList"
                :key="doc.id"
                class="document-card"
                :class="{ selected: selectedDocumentIds.includes(doc.id) }"
                @click="handleCardClick(doc)"
              >
                <a-checkbox
                  :checked="selectedDocumentIds.includes(doc.id)"
                  class="card-checkbox"
                  @click.stop
                  @change="() => toggleDocumentSelect(doc.id)"
                />
                <div class="card-preview" @click.stop="goToDetail(doc.id)">
                  <component :is="getFileIcon(doc.fileType)" class="preview-icon" />
                </div>
                <div class="card-info">
                  <div class="card-title" :title="doc.name">
                    {{ doc.name }}
                    <a-tag v-if="doc.isTemplate" color="blue" size="small">模板</a-tag>
                  </div>
                  <div class="card-meta">
                    <span>{{ formatFileSize(doc.size) }}</span>
                    <span>{{ formatDate(doc.createdAt) }}</span>
                  </div>
                  <div class="card-tags">
                    <a-tag v-for="tag in doc.tags.slice(0, 2)" :key="tag" size="small">
                      {{ tag }}
                    </a-tag>
                  </div>
                  <div class="card-status">
                    <a-tag v-if="doc.status === 'DRAFT'" color="orange">草稿</a-tag>
                    <a-tag v-else-if="doc.status === 'UNDER_REVIEW'" color="blue">审核中</a-tag>
                    <a-tag v-else-if="doc.status === 'APPROVED'" color="green">已批准</a-tag>
                    <a-tag v-else-if="doc.status === 'ARCHIVED'" color="gray">已归档</a-tag>
                    <a-tag v-else-if="doc.status === 'DEPRECATED'" color="red">已废弃</a-tag>
                  </div>
                </div>
                <div class="card-actions">
                  <a-button type="text" size="small" @click.stop="handleDownload(doc)">
                    <DownloadOutlined />
                  </a-button>
                  <a-dropdown>
                    <a-button type="text" size="small" @click.stop>
                      <EllipsisOutlined />
                    </a-button>
                    <template #overlay>
                      <a-menu>
                        <a-menu-item @click="handleShare(doc)">
                          <ShareAltOutlined /> 分享
                        </a-menu-item>
                        <a-menu-item @click="handleMove(doc)">
                          <FolderOutlined /> 移动
                        </a-menu-item>
                        <a-menu-divider />
                        <a-menu-item danger @click="handleDelete(doc)">
                          <DeleteOutlined /> 删除
                        </a-menu-item>
                      </a-menu>
                    </template>
                  </a-dropdown>
                </div>
              </div>
            </div>
          </div>

          <!-- 空状态 -->
          <a-empty v-if="!loading && documentList.length === 0" description="暂无文档">
            <a-button type="primary" @click="showCreateModal">
              新建文档
            </a-button>
          </a-empty>
        </a-spin>

        <!-- 分页 -->
        <div v-if="pagination.total > 0" class="pagination-wrapper">
          <a-pagination
            v-model:current="pagination.page"
            v-model:page-size="pagination.pageSize"
            :total="pagination.total"
            :show-size-changer="true"
            :show-total="(total) => `共 ${total} 个文档`"
            @change="handlePageChange"
          />
        </div>
      </div>
    </div>

    <!-- 新建文档模态框 -->
    <CreateDocumentModal
      v-model:visible="createModalVisible"
      @success="handleCreateSuccess"
    />

    <!-- 上传模态框 -->
    <UploadModal
      v-model:visible="uploadModalVisible"
      @success="handleUploadSuccess"
    />

    <!-- 筛选抽屉 -->
    <FilterDrawer
      v-model:visible="filterDrawerVisible"
      :filters="queryParams"
      @apply="handleFilterApply"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Modal, message } from 'ant-design-vue'
import {
  FileAddOutlined,
  UploadOutlined,
  FolderAddOutlined,
  DownOutlined,
  FilterOutlined,
  UnorderedListOutlined,
  AppstoreOutlined,
  PartitionOutlined,
  CloseOutlined,
  DownloadOutlined,
  DeleteOutlined,
  ShareAltOutlined,
  FolderOutlined,
  CopyOutlined,
  EllipsisOutlined,
  FileTextOutlined,
  FilePdfOutlined,
  FileWordOutlined,
  FileExcelOutlined,
  FilePptOutlined,
  FileImageOutlined,
  FileZipOutlined,
  FileOutlined
} from '@ant-design/icons-vue'
import { useDocumentStore } from '@/stores/modules/document'
import { storeToRefs } from 'pinia'
import type { Document, FileType, ViewMode } from '@/types/document'
import CreateDocumentModal from '@/components/document/CreateDocumentModal.vue'
import UploadModal from '@/components/document/UploadModal.vue'
import FilterDrawer from '@/components/document/FilterDrawer.vue'
import dayjs from 'dayjs'

const router = useRouter()
const documentStore = useDocumentStore()

const {
  documentList,
  loading,
  pagination,
  queryParams,
  viewMode,
  selectedDocumentIds,
  selectedCount,
  folders
} = storeToRefs(documentStore)

// 本地状态
const searchKeyword = ref('')
const createModalVisible = ref(false)
const uploadModalVisible = ref(false)
const filterDrawerVisible = ref(false)
const showFolderTree = ref(true)
const selectedFolderKeys = ref<number[]>([])
const currentViewMode = ref<ViewMode>(viewMode.value)
const currentFolderPath = ref<any[]>([])

// 表格列配置
const columns = [
  {
    title: '文档名称',
    key: 'name',
    dataIndex: 'name',
    width: 300,
    ellipsis: true,
    sorter: true
  },
  {
    title: '大小',
    key: 'size',
    dataIndex: 'size',
    width: 100,
    sorter: true
  },
  {
    title: '优先级',
    key: 'priority',
    dataIndex: 'priority',
    width: 100
  },
  {
    title: '标签',
    key: 'tags',
    dataIndex: 'tags',
    width: 200
  },
  {
    title: '状态',
    key: 'status',
    dataIndex: 'status',
    width: 120
  },
  {
    title: '上传者',
    key: 'author',
    dataIndex: 'author',
    width: 150
  },
  {
    title: '上传时间',
    key: 'createdAt',
    dataIndex: 'createdAt',
    width: 180,
    sorter: true
  },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    fixed: 'right'
  }
]

// 行选择配置
const rowSelection = {
  selectedRowKeys: selectedDocumentIds,
  onChange: (selectedKeys: number[]) => {
    selectedDocumentIds.value = selectedKeys
  }
}

// 文件夹树数据
const folderTreeData = computed(() => {
  return [
    {
      id: 0,
      name: '全部文档',
      documentCount: pagination.value.total,
      children: folders.value
    }
  ]
})

// 活跃的筛选条件
const activeFilters = computed(() => {
  const filters: { key: string; label: string; value: string }[] = []

  if (queryParams.value.fileType) {
    filters.push({
      key: 'fileType',
      label: '文件类型',
      value: queryParams.value.fileType
    })
  }

  if (queryParams.value.tags && queryParams.value.tags.length > 0) {
    filters.push({
      key: 'tags',
      label: '标签',
      value: queryParams.value.tags.join(', ')
    })
  }

  if (queryParams.value.status) {
    filters.push({
      key: 'status',
      label: '状态',
      value: queryParams.value.status
    })
  }

  if (queryParams.value.priority) {
    const priorityMap = { 1: '低', 2: '中', 3: '高' }
    filters.push({
      key: 'priority',
      label: '优先级',
      value: priorityMap[queryParams.value.priority as number]
    })
  }

  if (queryParams.value.startDate) {
    filters.push({
      key: 'dateRange',
      label: '时间范围',
      value: `${queryParams.value.startDate} ~ ${queryParams.value.endDate}`
    })
  }

  return filters
})

const hasActiveFilters = computed(() => activeFilters.value.length > 0)

// 获取文件图标
const getFileIcon = (fileType: FileType) => {
  const iconMap: Record<FileType, any> = {
    PDF: FilePdfOutlined,
    WORD: FileWordOutlined,
    EXCEL: FileExcelOutlined,
    PPT: FilePptOutlined,
    IMAGE: FileImageOutlined,
    VIDEO: FileOutlined,
    AUDIO: FileOutlined,
    ZIP: FileZipOutlined,
    OTHER: FileOutlined
  }
  return iconMap[fileType] || FileTextOutlined
}

// 格式化文件大小
const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i]
}

// 格式化日期
const formatDate = (date: string): string => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

// 搜索
const handleSearch = () => {
  documentStore.setQueryParams({ keyword: searchKeyword.value })
  documentStore.setPagination(1)
  documentStore.fetchDocuments()
}

// 显示新建文档模态框
const showCreateModal = () => {
  createModalVisible.value = true
}

// 显示上传模态框
const showUploadModal = () => {
  uploadModalVisible.value = true
}

// 显示创建文件夹模态框
const showCreateFolderModal = () => {
  Modal.confirm({
    title: '创建文件夹',
    content: '此功能待实现'
  })
}

// 显示筛选抽屉
const showFilterDrawer = () => {
  filterDrawerVisible.value = true
}

// 应用筛选
const handleFilterApply = (filters: any) => {
  documentStore.setQueryParams(filters)
  documentStore.setPagination(1)
  documentStore.fetchDocuments()
  filterDrawerVisible.value = false
}

// 移除筛选条件
const removeFilter = (key: string) => {
  const updates: any = {}
  if (key === 'dateRange') {
    updates.startDate = undefined
    updates.endDate = undefined
  } else {
    updates[key] = undefined
  }
  documentStore.setQueryParams(updates)
  documentStore.fetchDocuments()
}

// 清空所有筛选
const clearAllFilters = () => {
  documentStore.resetQueryParams()
  searchKeyword.value = ''
  documentStore.fetchDocuments()
}

// 切换视图模式
const handleViewModeChange = () => {
  documentStore.setViewMode(currentViewMode.value)
}

// 文件夹选择
const handleFolderSelect = (keys: number[]) => {
  selectedFolderKeys.value = keys
  const folderId = keys[0] || undefined
  documentStore.setQueryParams({ folderId })
  documentStore.setPagination(1)
  documentStore.fetchDocuments()
}

// 表格变化
const handleTableChange = (pagination: any, filters: any, sorter: any) => {
  if (sorter.field) {
    documentStore.setQueryParams({
      sort: sorter.field,
      order: sorter.order === 'ascend' ? 'asc' : 'desc'
    })
    documentStore.fetchDocuments()
  }
}

// 分页变化
const handlePageChange = (page: number, pageSize: number) => {
  documentStore.setPagination(page, pageSize)
  documentStore.fetchDocuments()
}

// 卡片点击
const handleCardClick = (doc: Document) => {
  documentStore.toggleDocumentSelect(doc.id)
}

// 跳转到详情
const goToDetail = (id: number) => {
  router.push(`/documents/${id}`)
}

// 下载文档
const handleDownload = async (doc: Document) => {
  await documentStore.downloadDocument(doc.id, doc.name)
}

// 批量下载
const handleBatchDownload = () => {
  Modal.info({
    title: '批量下载',
    content: '批量下载功能待实现'
  })
}

// 删除文档
const handleDelete = (doc: Document) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除文档"${doc.name}"吗？此操作不可恢复。`,
    okText: '确定',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      await documentStore.deleteDocument(doc.id)
    }
  })
}

// 批量删除
const handleBatchDelete = () => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除选中的 ${selectedCount.value} 个文档吗？此操作不可恢复。`,
    okText: '确定',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      await documentStore.batchDeleteDocuments(selectedDocumentIds.value)
    }
  })
}

// 分享文档
const handleShare = (doc: Document) => {
  Modal.info({
    title: '分享文档',
    content: `分享文档"${doc.name}"功能待实现`
  })
}

// 移动文档
const handleMove = (doc: Document) => {
  Modal.info({
    title: '移动文档',
    content: `移动文档"${doc.name}"功能待实现`
  })
}

// 批量移动
const showBatchMoveModal = () => {
  Modal.info({
    title: '批量移动',
    content: '批量移动功能待实现'
  })
}

// 复制文档
const handleCopy = (doc: Document) => {
  Modal.info({
    title: '复制文档',
    content: `复制文档"${doc.name}"功能待实现`
  })
}

// 新建文档成功
const handleCreateSuccess = () => {
  documentStore.fetchDocuments()
}

// 上传成功
const handleUploadSuccess = () => {
  documentStore.fetchDocuments()
}

// 初始化
onMounted(async () => {
  documentStore.restoreViewMode()
  currentViewMode.value = viewMode.value
  await documentStore.fetchDocuments()
  await documentStore.fetchFolders()
})
</script>

<style scoped lang="scss">
.document-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;

  .toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 24px;
    background: #fff;
    border-bottom: 1px solid #e8e8e8;

    .toolbar-left,
    .toolbar-right {
      display: flex;
      align-items: center;
      gap: 12px;
    }
  }

  .content-wrapper {
    flex: 1;
    display: flex;
    overflow: hidden;

    .folder-sidebar {
      width: 260px;
      background: #fff;
      border-right: 1px solid #e8e8e8;
      overflow-y: auto;

      .sidebar-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 16px;
        border-bottom: 1px solid #e8e8e8;
        font-weight: 500;
      }

      .folder-count {
        color: #999;
        font-size: 12px;
      }
    }

    .main-content {
      flex: 1;
      display: flex;
      flex-direction: column;
      overflow: hidden;

      .breadcrumb {
        padding: 12px 24px;
        background: #fff;
        border-bottom: 1px solid #e8e8e8;
      }

      .filter-tags {
        padding: 12px 24px;
        background: #fff;
        border-bottom: 1px solid #e8e8e8;
        display: flex;
        align-items: center;
        gap: 8px;
        flex-wrap: wrap;
      }

      .list-view {
        flex: 1;
        background: #fff;
        padding: 24px;
        overflow-y: auto;

        .document-name {
          display: flex;
          align-items: center;
          gap: 8px;

          .file-icon {
            font-size: 18px;
            color: #1890ff;
          }
        }

        .author-cell {
          display: flex;
          align-items: center;
          gap: 8px;
        }
      }

      .grid-view {
        flex: 1;
        padding: 24px;
        overflow-y: auto;

        .document-grid {
          display: grid;
          grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
          gap: 16px;

          .document-card {
            position: relative;
            background: #fff;
            border: 1px solid #e8e8e8;
            border-radius: 8px;
            padding: 16px;
            cursor: pointer;
            transition: all 0.3s;

            &:hover {
              box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
              transform: translateY(-2px);

              .card-actions {
                opacity: 1;
              }
            }

            &.selected {
              border-color: #1890ff;
              background: #e6f7ff;
            }

            .card-checkbox {
              position: absolute;
              top: 12px;
              left: 12px;
              z-index: 1;
            }

            .card-preview {
              display: flex;
              justify-content: center;
              align-items: center;
              height: 120px;
              margin-bottom: 12px;

              .preview-icon {
                font-size: 64px;
                color: #1890ff;
              }
            }

            .card-info {
              .card-title {
                font-weight: 500;
                margin-bottom: 8px;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
                display: flex;
                align-items: center;
                gap: 4px;
              }

              .card-meta {
                display: flex;
                justify-content: space-between;
                font-size: 12px;
                color: #999;
                margin-bottom: 8px;
              }

              .card-tags {
                display: flex;
                gap: 4px;
                flex-wrap: wrap;
                margin-bottom: 8px;
              }

              .card-status {
                .ant-tag {
                  font-size: 12px;
                  margin-right: 4px;
                }
              }
            }

            .card-actions {
              position: absolute;
              top: 12px;
              right: 12px;
              display: flex;
              gap: 4px;
              opacity: 0;
              transition: opacity 0.3s;
            }
          }
        }
      }

      .pagination-wrapper {
        padding: 16px 24px;
        background: #fff;
        border-top: 1px solid #e8e8e8;
        display: flex;
        justify-content: center;
      }
    }
  }
}
</style>