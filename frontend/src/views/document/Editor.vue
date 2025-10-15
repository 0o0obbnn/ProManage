<template>
  <div class="document-editor-page">
    <a-spin :spinning="loading">
      <div class="editor-container">
        <!-- 顶部工具栏 -->\
        <div class="editor-toolbar">
          <div class="toolbar-left">
            <a-button type="text" @click="goBack">
              <template #icon><ArrowLeftOutlined /></template>
              返回
            </a-button>
            <a-divider type="vertical" />
            <a-input
              v-model:value="documentTitle"
              placeholder="文档标题"
              style="width: 300px"
              @blur="handleTitleBlur"
            />
            <a-tag v-if="autoSaveStatus" color="green">
              <CheckCircleOutlined /> {{ autoSaveStatus }}
            </a-tag>
          </div>

          <div class="toolbar-right">
            <a-radio-group v-model:value="editorMode" button-style="solid" @change="handleEditorModeChange">
              <a-radio-button value="richtext">
                <FileTextOutlined /> 富文本
              </a-radio-button>
              <a-radio-button value="markdown">
                <FileMarkdownOutlined /> Markdown
              </a-radio-button>
            </a-radio-group>
            <a-button @click="handleSave" :loading="saving">
              <template #icon><SaveOutlined /></template>
              保存
            </a-button>
            <a-button type="primary" @click="handlePublish">
              <template #icon><SendOutlined /></template>
              发布
            </a-button>
          </div>
        </div>

        <!-- 编辑器区域 -->
        <div class="editor-content">
          <!-- 富文本编辑器 -->
          <component
            v-if="editorMode === 'richtext' && Editor"
            :is="Editor"
            v-model="content"
            :init="tinymceConfig"
            @input="handleContentChange"
          />

          <!-- Markdown编辑器 -->
          <component
            v-else-if="editorMode === 'markdown' && MdEditor"
            :is="MdEditor"
            v-model="content"
            :toolbars="markdownToolbars"
            @on-save="handleSave"
            @on-upload-img="handleImageUpload"
            @onChange="handleContentChange"
          />
          
          <!-- 加载中状态 -->
          <div v-else class="editor-loading">
            <a-spin size="large" tip="编辑器加载中..." />
          </div>
        </div>
      </div>
    </a-spin>

    <!-- 冲突检测模态框 -->
    <a-modal
      v-model:open="conflictModalVisible"
      title="检测到文档冲突"
      :footer="null"
      width="800px"
    >
      <a-alert
        message="文档已被其他用户修改"
        description="请选择如何处理冲突"
        type="warning"
        show-icon
        style="margin-bottom: 16px"
      />
      <a-space direction="vertical" style="width: 100%">
        <a-button type="primary" block @click="handleUseServerVersion">
          使用服务器版本（放弃本地修改）
        </a-button>
        <a-button block @click="handleUseLocalVersion">
          使用本地版本（覆盖服务器）
        </a-button>
        <a-button block @click="handleMergeVersions">
          手动合并（高级）
        </a-button>
      </a-space>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  ArrowLeftOutlined,
  CheckCircleOutlined,
  FileTextOutlined,
  FileMarkdownOutlined,
  SaveOutlined,
  SendOutlined
} from '@ant-design/icons-vue'
// 动态导入编辑器
const Editor = ref<any>(null)
const MdEditor = ref<any>(null)
const editorLoaded = ref(false)

const loadEditor = async (type: 'richtext' | 'markdown') => {
  if (editorLoaded.value) return
  
  try {
    if (type === 'richtext' && !Editor.value) {
      const editorModule = await import('@tinymce/tinymce-vue')
      Editor.value = editorModule.default
    } else if (type === 'markdown' && !MdEditor.value) {
      const editorModule = await import('md-editor-v3')
      MdEditor.value = editorModule.default
      await import('md-editor-v3/lib/style.css')
    }
    editorLoaded.value = true
  } catch (error) {
    console.error(`Failed to load ${type} editor:`, error)
    message.error(`${type === 'richtext' ? '富文本' : 'Markdown'}编辑器加载失败`)
  }
}
import { debounce } from 'lodash-es'
import { useDocumentStore } from '@/stores/modules/document'

const router = useRouter()
const route = useRoute()
const documentStore = useDocumentStore()

const documentId = ref<number>()
const documentTitle = ref('')
const content = ref('')
const editorMode = ref<'richtext' | 'markdown'>('richtext')
const loading = ref(false)
const saving = ref(false)
const autoSaveStatus = ref('')
const conflictModalVisible = ref(false)
const serverVersion = ref<number>(1)
const localVersion = ref<number>(1)

// TinyMCE配置
const tinymceConfig = {
  height: 'calc(100vh - 200px)',
  menubar: false,
  plugins: [
    'advlist', 'autolink', 'lists', 'link', 'image', 'charmap',
    'preview', 'anchor', 'searchreplace', 'visualblocks', 'code',
    'fullscreen', 'insertdatetime', 'media', 'table', 'code', 'help', 'wordcount'
  ],
  toolbar:
    'undo redo | formatselect | bold italic backcolor | \
    alignleft aligncenter alignright alignjustify | \
    bullist numlist outdent indent | removeformat | table | image | code | help',
  content_style: 'body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif; font-size: 14px; line-height: 1.6; }',
  language: 'zh_CN',
  images_upload_handler: async (blobInfo: any, success: any, failure: any) => {
    try {
      const file = blobInfo.blob()
      const url = await handleImageUpload(file)
      success(url)
    } catch (error) {
      failure('图片上传失败')
    }
  },
  paste_data_images: true,
  automatic_uploads: true
}

// Markdown工具栏配置
const markdownToolbars = [
  'bold', 'italic', 'strikethrough', '-',
  'title', 'sub', 'sup', 'quote', 'unorderedList', 'orderedList', '-',
  'codeRow', 'code', 'link', 'image', 'table', '-',
  'revoke', 'next', 'save', '=', 'preview', 'fullscreen'
]

// 图片上传处理
const handleImageUpload = async (file: File): Promise<string> => {
  const formData = new FormData()
  formData.append('file', file)

  try {
    // TODO: 调用上传API
    // const res = await uploadApi.uploadImage(formData)
    // return res.data.url
    
    // 临时返回本地URL
    return URL.createObjectURL(file)
  } catch (error) {
    message.error('图片上传失败')
    throw error
  }
}

// 编辑器模式切换处理
const handleEditorModeChange = async (e: any) => {
  const mode = e.target.value
  editorMode.value = mode
  await loadEditor(mode)
}

// 内容变化处理
const handleContentChange = () => {
  autoSave()
}

// 自动保存（防抖5秒）
const autoSave = debounce(async () => {
  if (!documentId.value || !content.value) return

  try {
    autoSaveStatus.value = '正在保存...'
    
    // 检查版本冲突
    const hasConflict = await checkVersionConflict()
    if (hasConflict) {
      conflictModalVisible.value = true
      autoSaveStatus.value = ''
      return
    }

    await saveDocument(false)
    autoSaveStatus.value = '已自动保存'
    
    setTimeout(() => {
      autoSaveStatus.value = ''
    }, 3000)
  } catch (error) {
    autoSaveStatus.value = '自动保存失败'
  }
}, 5000)

// 检查版本冲突
const checkVersionConflict = async (): Promise<boolean> => {
  try {
    // TODO: 调用API检查版本
    // const res = await documentApi.getVersion(documentId.value!)
    // return res.data.version > localVersion.value
    return false
  } catch (error) {
    return false
  }
}

// 保存文档
const saveDocument = async (showMessage = true) => {
  if (!documentId.value) return

  try {
    saving.value = true
    
    // TODO: 调用保存API
    // await documentApi.updateDocument(documentId.value, {
    //   title: documentTitle.value,
    //   content: content.value,
    //   contentType: editorMode.value === 'markdown' ? 'markdown' : 'html',
    //   version: localVersion.value
    // })

    localVersion.value++
    
    if (showMessage) {
      message.success('保存成功')
    }
  } catch (error) {
    message.error('保存失败')
    throw error
  } finally {
    saving.value = false
  }
}

// 手动保存
const handleSave = async () => {
  await saveDocument(true)
}

// 发布文档
const handlePublish = async () => {
  try {
    await saveDocument(false)
    
    // TODO: 调用发布API
    // await documentApi.publishDocument(documentId.value!)
    
    message.success('发布成功')
    router.push(`/documents/${documentId.value}`)
  } catch (error) {
    message.error('发布失败')
  }
}

// 标题失焦处理
const handleTitleBlur = () => {
  autoSave()
}

// 编辑器模式切换
const handleEditorModeChange = () => {
  // 可以添加内容格式转换逻辑
  message.info(`已切换到${editorMode.value === 'richtext' ? '富文本' : 'Markdown'}模式`)
}

// 使用服务器版本
const handleUseServerVersion = async () => {
  try {
    // TODO: 重新加载服务器版本
    // const res = await documentApi.getDocument(documentId.value!)
    // content.value = res.data.content
    // localVersion.value = res.data.version
    
    conflictModalVisible.value = false
    message.success('已加载服务器版本')
  } catch (error) {
    message.error('加载失败')
  }
}

// 使用本地版本
const handleUseLocalVersion = async () => {
  try {
    await saveDocument(false)
    conflictModalVisible.value = false
    message.success('已保存本地版本')
  } catch (error) {
    message.error('保存失败')
  }
}

// 手动合并
const handleMergeVersions = () => {
  message.info('手动合并功能待实现')
  conflictModalVisible.value = false
}

// 返回
const goBack = () => {
  if (content.value) {
    // 提示保存
    router.push('/documents')
  } else {
    router.back()
  }
}

// 页面离开前提示
const handleBeforeUnload = (e: BeforeUnloadEvent) => {
  if (content.value) {
    e.preventDefault()
    e.returnValue = ''
  }
}

// 初始化
onMounted(async () => {
  await fetchDocument()
  await loadEditor(editorMode.value)
  initAutoSave()
})

onUnmounted(() => {
  window.removeEventListener('beforeunload', handleBeforeUnload)
})
</script>

<style scoped lang="scss">
.document-editor-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #fff;

  .editor-container {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;

    .editor-toolbar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 24px;
      border-bottom: 1px solid #e8e8e8;

      .toolbar-left,
      .toolbar-right {
        display: flex;
        align-items: center;
        gap: 12px;
      }
    }

    .editor-content {
      flex: 1;
      overflow: hidden;
      padding: 24px;
      
      .editor-loading {
        height: 100%;
        display: flex;
        justify-content: center;
        align-items: center;
      }
    }
  }
}
</style>
