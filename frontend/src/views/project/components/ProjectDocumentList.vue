<template>
  <div class="project-document-list">
    <!-- 工具栏 -->
    <div class="document-toolbar">
      <div class="toolbar-left">
        <a-button type="primary" @click="handleCreateDocument">
          <template #icon><FileAddOutlined /></template>
          新建文档
        </a-button>
        <a-button @click="handleUploadDocument">
          <template #icon><UploadOutlined /></template>
          上传文档
        </a-button>
        <a-radio-group v-model:value="currentViewMode" button-style="solid">
          <a-radio-button value="list">
            <UnorderedListOutlined /> 列表
          </a-radio-button>
          <a-radio-button value="grid">
            <AppstoreOutlined /> 网格
          </a-radio-button>
        </a-radio-group>
      </div>

      <div class="toolbar-right">
        <a-input-search
          v-model:value="searchKeyword"
          placeholder="搜索文档..."
          style="width: 240px"
          @search="handleSearch"
        />
        <a-select
          v-model:value="filterType"
          placeholder="类型筛选"
          style="width: 120px"
          allow-clear
          @change="handleFilterChange"
        >
          <a-select-option value="">全部</a-select-option>
          <a-select-option value="markdown">Markdown</a-select-option>
          <a-select-option value="richtext">富文本</a-select-option>
          <a-select-option value="file">文件</a-select-option>
        </a-select>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="document-content">
      <!-- 列表视图 -->
      <div v-if="currentViewMode === 'list'" class="list-view">
        <a-table
          :columns="columns"
          :data-source="filteredDocuments"
          :loading="loading"
          :pagination="paginationConfig"
          :row-key="record => record.id"
          @change="handleTableChange"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'title'">
              <div class="title-cell">
                <FileTextOutlined class="file-icon" />
                <a @click="handleDocumentClick(record)">{{ record.name }}</a>
              </div>
            </template>

            <template v-else-if="column.key === 'type'">
              <a-tag :color="getTypeColor(record.fileType)">
                {{ getTypeText(record.fileType) }}
              </a-tag>
            </template>

            <template v-else-if="column.key === 'creator'">
              <div v-if="record.author" class="creator-cell">
                <a-avatar :src="record.author.avatar" :size="24">
                  {{ record.author.name.charAt(0) }}
                </a-avatar>
                <span>{{ record.author.name }}</span>
              </div>
            </template>

            <template v-else-if="column.key === 'updatedAt'">
              {{ formatDate(record.updatedAt) }}
            </template>

            <template v-else-if="column.key === 'size'">
              {{ formatFileSize(record.size) }}
            </template>

            <template v-else-if="column.key === 'actions'">
              <a-space>
                <a-button type="link" size="small" @click="handleViewDocument(record)">
                  查看
                </a-button>
                <a-button type="link" size="small" @click="handleEditDocument(record)">
                  编辑
                </a-button>
                <a-dropdown>
                  <a-button type="link" size="small">
                    更多
                    <DownOutlined />
                  </a-button>
                  <template #overlay>
                    <a-menu>
                      <a-menu-item @click="handleDownloadDocument(record)">
                        <DownloadOutlined /> 下载
                      </a-menu-item>
                      <a-menu-item @click="handleShareDocument(record)">
                        <ShareAltOutlined /> 分享
                      </a-menu-item>
                      <a-menu-divider />
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
      </div>

      <!-- 网格视图 -->
      <div v-else-if="currentViewMode === 'grid'" class="grid-view">
        <a-spin :spinning="loading">
          <a-row :gutter="[16, 16]">
            <a-col
              v-for="doc in filteredDocuments"
              :key="doc.id"
              :xs="24"
              :sm="12"
              :md="8"
              :lg="6"
            >
              <a-card
                hoverable
                class="document-card"
                @click="handleDocumentClick(doc)"
              >
                <template #cover>
                  <div class="document-cover">
                    <FileTextOutlined class="document-icon" />
                  </div>
                </template>
                <a-card-meta :title="doc.name">
                  <template #description>
                    <div class="document-meta">
                      <a-tag :color="getTypeColor(doc.fileType)" size="small">
                        {{ getTypeText(doc.fileType) }}
                      </a-tag>
                      <div class="document-info">
                        <span>{{ formatDate(doc.updatedAt) }}</span>
                        <span>{{ formatFileSize(doc.size) }}</span>
                      </div>
                    </div>
                  </template>
                </a-card-meta>
              </a-card>
            </a-col>
          </a-row>

          <!-- 空状态 -->
          <a-empty
            v-if="filteredDocuments.length === 0 && !loading"
            description="暂无文档"
          >
            <a-button type="primary" @click="handleCreateDocument">
              创建第一个文档
            </a-button>
          </a-empty>
        </a-spin>
      </div>
    </div>

    <!-- 创建文档弹窗 -->
    <CreateDocumentModal
      v-model:visible="createModalVisible"
      :project-id="projectId"
      @success="handleCreateSuccess"
    />

    <!-- 上传文档弹窗 -->
    <UploadModal
      v-model:visible="uploadModalVisible"
      :project-id="projectId"
      @success="handleUploadSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import {
  FileAddOutlined,
  UploadOutlined,
  UnorderedListOutlined,
  AppstoreOutlined,
  FileTextOutlined,
  DownOutlined,
  DownloadOutlined,
  ShareAltOutlined,
  DeleteOutlined
} from '@ant-design/icons-vue'
import { useDocumentStore } from '@/stores/modules/document'
import CreateDocumentModal from '@/components/document/CreateDocumentModal.vue'
import UploadModal from '@/components/document/UploadModal.vue'
import type { Document } from '@/types/document'
import type { TableColumnsType, TableProps } from 'ant-design-vue'
import dayjs from 'dayjs'

interface Props {
  projectId: number
}

const props = defineProps<Props>()

const router = useRouter()
const documentStore = useDocumentStore()

// 响应式数据
const loading = ref(false)
const currentViewMode = ref<'list' | 'grid'>('list')
const searchKeyword = ref('')
const filterType = ref<string>('')
const createModalVisible = ref(false)
const uploadModalVisible = ref(false)
const documents = ref<Document[]>([])
const pagination = ref({
  current: 1,
  pageSize: 20,
  total: 0
})

// 表格列定义
const columns: TableColumnsType = [
  {
    title: '文档名称',
    key: 'title',
    dataIndex: 'title',
    width: 300,
    ellipsis: true
  },
  {
    title: '类型',
    key: 'type',
    dataIndex: 'type',
    width: 100
  },
  {
    title: '创建者',
    key: 'creator',
    dataIndex: 'creator',
    width: 150
  },
  {
    title: '更新时间',
    key: 'updatedAt',
    dataIndex: 'updatedAt',
    width: 180
  },
  {
    title: '大小',
    key: 'size',
    dataIndex: 'size',
    width: 100
  },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    fixed: 'right'
  }
]

// 计算属性
const filteredDocuments = computed(() => {
  let result = documents.value

  // 搜索过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(doc =>
      doc.name.toLowerCase().includes(keyword) ||
      doc.description?.toLowerCase().includes(keyword)
    )
  }

  // 类型过滤
  if (filterType.value) {
    result = result.filter(doc => doc.fileType === filterType.value)
  }

  return result
})

const paginationConfig = computed(() => ({
  current: pagination.value.current,
  pageSize: pagination.value.pageSize,
  total: pagination.value.total,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`
}))

// 方法
const loadDocuments = async () => {
  try {
    loading.value = true
    const result = await documentStore.fetchDocuments({
      projectId: props.projectId,
      page: pagination.value.current,
      pageSize: pagination.value.pageSize
    })
    documents.value = result.list
    pagination.value.total = result.total
  } catch (error) {
    console.error('Load documents failed:', error)
    message.error('加载文档列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  // 搜索已通过 computed 自动过滤
}

const handleFilterChange = () => {
  // 过滤已通过 computed 自动过滤
}

const handleTableChange: TableProps['onChange'] = (pag) => {
  if (pag.current && pag.pageSize) {
    pagination.value.current = pag.current
    pagination.value.pageSize = pag.pageSize
    loadDocuments()
  }
}

const handleCreateDocument = () => {
  createModalVisible.value = true
}

const handleUploadDocument = () => {
  uploadModalVisible.value = true
}

const handleDocumentClick = (doc: Document) => {
  router.push(`/documents/${doc.id}`)
}

const handleViewDocument = (doc: Document) => {
  router.push(`/documents/${doc.id}`)
}

const handleEditDocument = (doc: Document) => {
  router.push(`/documents/${doc.id}/edit`)
}

const handleDownloadDocument = async (doc: Document) => {
  try {
    await documentStore.downloadDocument(doc.id, doc.name)
    message.success('文档下载成功')
  } catch (error) {
    console.error('Download document failed:', error)
    message.error('文档下载失败')
  }
}

const handleShareDocument = (doc: Document) => {
  message.info('分享功能开发中')
}

const handleDeleteDocument = (doc: Document) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除文档"${doc.name}"吗?`,
    okText: '确定',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      try {
        await documentStore.deleteDocument(doc.id)
        message.success('文档删除成功')
        await loadDocuments()
      } catch (error) {
        console.error('Delete document failed:', error)
      }
    }
  })
}

const handleCreateSuccess = () => {
  createModalVisible.value = false
  loadDocuments()
}

const handleUploadSuccess = () => {
  uploadModalVisible.value = false
  loadDocuments()
}

// 辅助函数
const getTypeColor = (type: string) => {
  const colors: Record<string, string> = {
    PDF: 'red',
    WORD: 'blue',
    EXCEL: 'green',
    PPT: 'orange',
    IMAGE: 'purple',
    VIDEO: 'cyan',
    AUDIO: 'magenta',
    ZIP: 'gold',
    OTHER: 'default'
  }
  return colors[type] || 'default'
}

const getTypeText = (type: string) => {
  const texts: Record<string, string> = {
    PDF: 'PDF',
    WORD: 'Word',
    EXCEL: 'Excel',
    PPT: 'PPT',
    IMAGE: '图片',
    VIDEO: '视频',
    AUDIO: '音频',
    ZIP: '压缩包',
    OTHER: '其他'
  }
  return texts[type] || type
}

const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const formatFileSize = (size: number) => {
  if (!size) return '-'
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(2)} KB`
  return `${(size / 1024 / 1024).toFixed(2)} MB`
}

// 生命周期
onMounted(() => {
  loadDocuments()
})

// 监听项目ID变化
watch(() => props.projectId, () => {
  loadDocuments()
})
</script>

<style lang="scss" scoped>
.project-document-list {
  .document-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    padding: 16px;
    background: #fafafa;
    border-radius: 4px;

    .toolbar-left,
    .toolbar-right {
      display: flex;
      gap: 12px;
      align-items: center;
    }
  }

  .document-content {
    min-height: 400px;
  }

  .title-cell {
    display: flex;
    align-items: center;
    gap: 8px;

    .file-icon {
      color: #1890ff;
      font-size: 16px;
    }

    a {
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .creator-cell {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .grid-view {
    .document-card {
      height: 100%;

      .document-cover {
        height: 120px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

        .document-icon {
          font-size: 48px;
          color: white;
        }
      }

      .document-meta {
        .document-info {
          display: flex;
          justify-content: space-between;
          margin-top: 8px;
          font-size: 12px;
          color: #8c8c8c;
        }
      }
    }
  }
}
</style>

