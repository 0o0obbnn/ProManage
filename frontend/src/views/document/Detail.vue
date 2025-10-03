<template>
  <div class="document-detail-page">
    <a-spin :spinning="loading">
      <div v-if="document" class="detail-container">
        <!-- 顶部操作栏 -->
        <div class="header">
          <div class="header-left">
            <a-button type="text" @click="goBack">
              <template #icon><ArrowLeftOutlined /></template>
              返回
            </a-button>
            <a-divider type="vertical" />
            <h2 class="document-title">{{ document.name }}</h2>
            <a-tag v-if="document.isTemplate" color="blue">模板</a-tag>
          </div>
          <div class="header-right">
            <a-space>
              <a-button @click="handleDownload">
                <template #icon><DownloadOutlined /></template>
                下载
              </a-button>
              <a-button @click="handleShare">
                <template #icon><ShareAltOutlined /></template>
                分享
              </a-button>
              <a-button @click="handleEdit">
                <template #icon><EditOutlined /></template>
                编辑
              </a-button>
              <a-dropdown>
                <a-button>
                  更多 <DownOutlined />
                </a-button>
                <template #overlay>
                  <a-menu>
                    <a-menu-item @click="handleUploadVersion">
                      <UploadOutlined /> 上传新版本
                    </a-menu-item>
                    <a-menu-item @click="handleMove">
                      <FolderOutlined /> 移动
                    </a-menu-item>
                    <a-menu-item @click="handleCopy">
                      <CopyOutlined /> 复制
                    </a-menu-item>
                    <a-menu-divider />
                    <a-menu-item danger @click="handleDelete">
                      <DeleteOutlined /> 删除
                    </a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
            </a-space>
          </div>
        </div>

        <!-- 主内容区 -->
        <div class="content">
          <!-- 左侧预览和信息 -->
          <div class="main-section">
            <!-- 文档预览 -->
            <a-card title="文档预览" class="preview-card">
              <div class="preview-wrapper">
                <component :is="getFileIcon(document.fileType)" class="preview-icon" />
                <p class="preview-hint">{{ getPreviewHint(document.fileType) }}</p>
                <a-button type="primary" @click="handleDownload">
                  下载查看
                </a-button>
              </div>
            </a-card>

            <!-- 文档内容 -->
            <a-card title="文档内容" class="content-card">
              <div class="document-content">
                <div v-if="document.contentType === 'markdown'" class="markdown-content">
                  <!-- 这里应该使用Markdown渲染器 -->
                  <pre>{{ document.description }}</pre>
                </div>
                <div v-else class="rich-content">
                  <!-- 这里应该使用富文本渲染器 -->
                  <div v-html="document.description"></div>
                </div>
              </div>
            </a-card>

            <!-- 文档信息 -->
            <a-card title="文档信息" class="info-card">
              <a-descriptions :column="2" bordered>
                <a-descriptions-item label="文档标题">
                  {{ document.name }}
                </a-descriptions-item>
                <a-descriptions-item label="文档类型">
                  <a-tag color="blue">{{ document.fileType }}</a-tag>
                </a-descriptions-item>
                <a-descriptions-item label="文件大小">
                  {{ formatFileSize(document.size) }}
                </a-descriptions-item>
                <a-descriptions-item label="版本号">
                  <a-tag color="green">{{ document.version }}</a-tag>
                </a-descriptions-item>
                <a-descriptions-item label="所属项目">
                  {{ document.projectName }}
                </a-descriptions-item>
                <a-descriptions-item label="分类">
                  {{ getCategoryName(document.categoryId) }}
                </a-descriptions-item>
                <a-descriptions-item label="优先级">
                  <a-tag v-if="document.priority === 1" color="green">低</a-tag>
                  <a-tag v-else-if="document.priority === 2" color="orange">中</a-tag>
                  <a-tag v-else-if="document.priority === 3" color="red">高</a-tag>
                </a-descriptions-item>
                <a-descriptions-item label="状态">
                  <a-tag :color="getStatusColor(document.status)">
                    {{ getStatusText(document.status) }}
                  </a-tag>
                </a-descriptions-item>
                <a-descriptions-item label="文件夹">
                  {{ getFolderPath(document.folderId) }}
                </a-descriptions-item>
                <a-descriptions-item label="创建者">
                  <div class="author-info">
                    <a-avatar :src="document.author.avatar" :size="24">
                      {{ document.author.name.charAt(0) }}
                    </a-avatar>
                    <span>{{ document.author.name }}</span>
                  </div>
                </a-descriptions-item>
                <a-descriptions-item label="审核人" v-if="document.reviewer">
                  <div class="author-info">
                    <a-avatar :src="document.reviewer.avatar" :size="24">
                      {{ document.reviewer.name.charAt(0) }}
                    </a-avatar>
                    <span>{{ document.reviewer.name }}</span>
                  </div>
                </a-descriptions-item>
                <a-descriptions-item label="创建时间">
                  {{ formatDate(document.createdAt) }}
                </a-descriptions-item>
                <a-descriptions-item label="更新时间">
                  {{ formatDate(document.updatedAt) }}
                </a-descriptions-item>
                <a-descriptions-item label="发布时间" v-if="document.publishedAt">
                  {{ formatDate(document.publishedAt) }}
                </a-descriptions-item>
                <a-descriptions-item label="归档时间" v-if="document.archivedAt">
                  {{ formatDate(document.archivedAt) }}
                </a-descriptions-item>
                <a-descriptions-item label="浏览次数">
                  {{ document.viewCount }}
                </a-descriptions-item>
                <a-descriptions-item label="下载次数">
                  {{ document.downloadCount }}
                </a-descriptions-item>
                <a-descriptions-item label="标签" :span="2">
                  <a-space wrap>
                    <a-tag v-for="tag in document.tags" :key="tag" color="blue">
                      {{ tag }}
                    </a-tag>
                    <a-button type="dashed" size="small" @click="showEditTagsModal">
                      <template #icon><PlusOutlined /></template>
                      添加标签
                    </a-button>
                  </a-space>
                </a-descriptions-item>
                <a-descriptions-item v-if="document.description" label="摘要" :span="2">
                  {{ document.description }}
                </a-descriptions-item>
              </a-descriptions>
            </a-card>

            <!-- 评论区 -->
            <a-card title="评论" class="comment-card">
              <template #extra>
                <span class="comment-count">{{ comments.length }} 条评论</span>
              </template>

              <!-- 评论输入 -->
              <div class="comment-input">
                <a-textarea
                  v-model:value="commentContent"
                  placeholder="添加评论..."
                  :rows="3"
                  :maxlength="500"
                  show-count
                />
                <div class="comment-actions">
                  <a-button type="primary" @click="handleAddComment" :loading="submittingComment">
                    发表评论
                  </a-button>
                </div>
              </div>

              <!-- 评论列表 -->
              <a-list
                v-if="comments.length > 0"
                :data-source="comments"
                item-layout="vertical"
                class="comment-list"
              >
                <template #renderItem="{ item }">
                  <a-list-item>
                    <a-comment>
                      <template #avatar>
                        <a-avatar :src="item.author.avatar">
                          {{ item.author.name.charAt(0) }}
                        </a-avatar>
                      </template>
                      <template #author>
                        <span class="comment-author">{{ item.author.name }}</span>
                      </template>
                      <template #datetime>
                        <span class="comment-time">{{ formatDate(item.createdAt) }}</span>
                      </template>
                      <template #content>
                        <p>{{ item.content }}</p>
                      </template>
                      <template #actions>
                        <span @click="handleReplyComment(item)">回复</span>
                        <span @click="handleDeleteComment(item)" class="delete-action">删除</span>
                      </template>
                    </a-comment>
                  </a-list-item>
                </template>
              </a-list>
              <a-empty v-else description="暂无评论" />
            </a-card>
          </div>

          <!-- 右侧版本历史 -->
          <div class="side-section">
            <a-card title="版本历史" size="small">
              <template #extra>
                <a-button type="link" size="small" @click="handleUploadVersion">
                  上传新版本
                </a-button>
              </template>
              <a-timeline>
                <a-timeline-item v-for="version in versions" :key="version.id" color="blue">
                  <div class="version-item">
                    <div class="version-header">
                      <strong>v{{ version.version }}</strong>
                      <a-tag v-if="version.isCurrent" color="green">当前版本</a-tag>
                      <a-button
                        v-if="!version.isCurrent"
                        type="link"
                        size="small"
                        @click="handleRollback(version)"
                      >
                        回滚
                      </a-button>
                    </div>
                    <div class="version-info">
                      <p class="version-author">{{ version.author.name }}</p>
                      <p class="version-time">{{ formatDate(version.createdAt) }}</p>
                      <p class="version-size">{{ formatFileSize(version.size) }}</p>
                      <p v-if="version.changes" class="version-changes">
                        {{ version.changes }}
                      </p>
                    </div>
                  </div>
                </a-timeline-item>
              </a-timeline>
              <a-empty v-if="versions.length === 0" description="暂无版本历史" />
            </a-card>
          </div>
        </div>
      </div>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Modal, message } from 'ant-design-vue'
import {
  ArrowLeftOutlined,
  DownloadOutlined,
  ShareAltOutlined,
  EditOutlined,
  UploadOutlined,
  FolderOutlined,
  CopyOutlined,
  DeleteOutlined,
  DownOutlined,
  PlusOutlined,
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
import type { Document, FileType, DocumentStatus } from '@/types/document'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()
const documentStore = useDocumentStore()

const { currentDocument: document, versions, comments, loading } = storeToRefs(documentStore)

const commentContent = ref('')
const submittingComment = ref(false)

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
  return iconMap[fileType] || FileOutlined
}

// 获取预览提示
const getPreviewHint = (fileType: FileType): string => {
  const hints: Record<FileType, string> = {
    PDF: 'PDF文档预览功能开发中',
    WORD: 'Word文档预览功能开发中',
    EXCEL: 'Excel文档预览功能开发中',
    PPT: 'PPT文档预览功能开发中',
    IMAGE: '图片预览功能开发中',
    VIDEO: '视频预览功能开发中',
    AUDIO: '音频预览功能开发中',
    ZIP: '压缩文件不支持预览',
    OTHER: '该文件类型不支持预览'
  }
  return hints[fileType] || '文件预览功能开发中'
}

// 获取状态颜色
const getStatusColor = (status: DocumentStatus): string => {
  const colorMap: Record<DocumentStatus, string> = {
    DRAFT: 'orange',
    UNDER_REVIEW: 'blue',
    APPROVED: 'green',
    ARCHIVED: 'gray',
    DEPRECATED: 'red'
  }
  return colorMap[status] || 'default'
}

// 获取状态文本
const getStatusText = (status: DocumentStatus): string => {
  const textMap: Record<DocumentStatus, string> = {
    DRAFT: '草稿',
    UNDER_REVIEW: '审核中',
    APPROVED: '已批准',
    ARCHIVED: '已归档',
    DEPRECATED: '已废弃'
  }
  return textMap[status] || status
}

// 获取分类名称
const getCategoryName = (categoryId?: number): string => {
  // TODO: 从分类服务获取分类名称
  return categoryId ? `分类${categoryId}` : '未分类'
}

// 获取文件夹路径
const getFolderPath = (folderId?: number): string => {
  // TODO: 从文件夹服务获取文件夹路径
  return folderId ? `文件夹${folderId}` : '根目录'
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

// 返回
const goBack = () => {
  router.back()
}

// 下载
const handleDownload = async () => {
  if (document.value) {
    await documentStore.downloadDocument(document.value.id, document.value.name)
  }
}

// 分享
const handleShare = () => {
  Modal.info({
    title: '分享文档',
    content: '分享功能待实现'
  })
}

// 编辑
const handleEdit = () => {
  Modal.info({
    title: '编辑文档',
    content: '编辑功能待实现'
  })
}

// 上传新版本
const handleUploadVersion = () => {
  Modal.info({
    title: '上传新版本',
    content: '上传新版本功能待实现'
  })
}

// 移动
const handleMove = () => {
  Modal.info({
    title: '移动文档',
    content: '移动功能待实现'
  })
}

// 复制
const handleCopy = () => {
  Modal.info({
    title: '复制文档',
    content: '复制功能待实现'
  })
}

// 删除
const handleDelete = () => {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除这个文档吗？此操作不可恢复。',
    okText: '确定',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      if (document.value) {
        await documentStore.deleteDocument(document.value.id)
        router.push('/documents')
      }
    }
  })
}

// 编辑标签
const showEditTagsModal = () => {
  Modal.info({
    title: '编辑标签',
    content: '编辑标签功能待实现'
  })
}

// 添加评论
const handleAddComment = async () => {
  if (!commentContent.value.trim()) {
    message.warning('请输入评论内容')
    return
  }

  try {
    submittingComment.value = true
    if (document.value) {
      await documentStore.addComment(document.value.id, commentContent.value)
      commentContent.value = ''
    }
  } finally {
    submittingComment.value = false
  }
}

// 回复评论
const handleReplyComment = (comment: any) => {
  Modal.info({
    title: '回复评论',
    content: '回复功能待实现'
  })
}

// 删除评论
const handleDeleteComment = (comment: any) => {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除这条评论吗？',
    okText: '确定',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      if (document.value) {
        await documentStore.deleteComment(document.value.id, comment.id)
      }
    }
  })
}

// 回滚版本
const handleRollback = (version: any) => {
  Modal.confirm({
    title: '确认回滚',
    content: `确定要回滚到版本 ${version.version} 吗？`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        // TODO: 调用回滚API
        message.success('版本回滚成功')
        // 重新加载文档详情
        if (document.value) {
          await documentStore.fetchDocumentDetail(document.value.id)
          await documentStore.fetchVersions(document.value.id)
        }
      } catch (error) {
        message.error('版本回滚失败')
      }
    }
  })
}

// 初始化
onMounted(async () => {
  const documentId = Number(route.params.id)
  if (documentId) {
    await documentStore.fetchDocumentDetail(documentId)
    await documentStore.fetchVersions(documentId)
    await documentStore.fetchComments(documentId)
  }
})
</script>

<style scoped lang="scss">
.document-detail-page {
  height: 100%;
  background: #f5f5f5;
  overflow-y: auto;

  .detail-container {
    max-width: 1400px;
    margin: 0 auto;
    padding: 24px;

    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;
      padding: 16px 24px;
      background: #fff;
      border-radius: 8px;

      .header-left {
        display: flex;
        align-items: center;
        gap: 12px;

        .document-title {
          margin: 0;
          font-size: 20px;
        }
      }
    }

    .content {
      display: flex;
      gap: 24px;

      .main-section {
        flex: 1;
        display: flex;
        flex-direction: column;
        gap: 24px;

        .preview-card {
          .preview-wrapper {
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            min-height: 400px;
            background: #fafafa;
            border-radius: 8px;

            .preview-icon {
              font-size: 120px;
              color: #1890ff;
              margin-bottom: 24px;
            }

            .preview-hint {
              color: #999;
              margin-bottom: 24px;
            }
          }
        }

        .content-card {
          .document-content {
            min-height: 300px;
            
            .markdown-content {
              white-space: pre-wrap;
              line-height: 1.6;
            }
            
            .rich-content {
              line-height: 1.6;
            }
          }
        }

        .info-card {
          .author-info {
            display: flex;
            align-items: center;
            gap: 8px;
          }
        }

        .comment-card {
          .comment-count {
            color: #999;
            font-size: 14px;
          }

          .comment-input {
            margin-bottom: 24px;

            .comment-actions {
              margin-top: 12px;
              display: flex;
              justify-content: flex-end;
            }
          }

          .comment-list {
            .comment-author {
              font-weight: 500;
            }

            .comment-time {
              color: #999;
              font-size: 12px;
            }

            .delete-action {
              color: #ff4d4f;

              &:hover {
                color: #ff7875;
              }
            }
          }
        }
      }

      .side-section {
        width: 320px;
        flex-shrink: 0;

        .version-item {
          .version-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 8px;
            gap: 8px;
            flex-wrap: wrap;
          }

          .version-info {
            font-size: 12px;
            color: #666;

            p {
              margin: 4px 0;
            }

            .version-changes {
              color: #999;
              font-style: italic;
            }
          }
        }
      }
    }
  }
}
</style>