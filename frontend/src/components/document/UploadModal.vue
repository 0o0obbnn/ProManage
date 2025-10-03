<template>
  <a-modal
    v-model:open="visible"
    title="上传文档"
    :width="680"
    :footer="null"
    @cancel="handleCancel"
  >
    <a-spin :spinning="uploading">
      <a-form :model="formState" :label-col="{ span: 5 }" :wrapper-col="{ span: 19 }">
        <!-- 文件上传 -->
        <a-form-item label="选择文件" required>
          <a-upload-dragger
            v-model:file-list="fileList"
            name="file"
            :multiple="false"
            :before-upload="beforeUpload"
            :max-count="1"
            @remove="handleRemove"
          >
            <p class="ant-upload-drag-icon">
              <InboxOutlined />
            </p>
            <p class="ant-upload-text">点击或拖拽文件到此区域上传</p>
            <p class="ant-upload-hint">
              支持单个文件上传，文件大小不超过 100MB
            </p>
          </a-upload-dragger>
        </a-form-item>

        <!-- 所属项目 -->
        <a-form-item label="所属项目" required>
          <a-select
            v-model:value="formState.projectId"
            placeholder="请选择项目"
            :options="projectOptions"
            show-search
            :filter-option="filterOption"
          />
        </a-form-item>

        <!-- 文件夹 -->
        <a-form-item label="存储位置">
          <a-tree-select
            v-model:value="formState.folderId"
            placeholder="选择文件夹（可选）"
            :tree-data="folderTreeData"
            :field-names="{ label: 'name', value: 'id', children: 'children' }"
            tree-default-expand-all
            allow-clear
          />
        </a-form-item>

        <!-- 标签 -->
        <a-form-item label="标签">
          <a-select
            v-model:value="formState.tags"
            mode="tags"
            placeholder="添加标签"
            :options="tagOptions"
            :token-separators="[',']"
            allow-clear
          />
        </a-form-item>

        <!-- 描述 -->
        <a-form-item label="描述">
          <a-textarea
            v-model:value="formState.description"
            placeholder="输入文档描述（可选）"
            :rows="4"
            :maxlength="500"
            show-count
          />
        </a-form-item>

        <!-- 状态 -->
        <a-form-item label="状态">
          <a-radio-group v-model:value="formState.status">
            <a-radio value="DRAFT">草稿</a-radio>
            <a-radio value="PUBLISHED">发布</a-radio>
          </a-radio-group>
        </a-form-item>

        <!-- 上传进度 -->
        <a-form-item v-if="uploadProgress > 0 && uploadProgress < 100" :wrapper-col="{ offset: 5, span: 19 }">
          <a-progress :percent="uploadProgress" :status="uploadStatus" />
        </a-form-item>

        <!-- 操作按钮 -->
        <a-form-item :wrapper-col="{ offset: 5, span: 19 }">
          <a-space>
            <a-button type="primary" :loading="uploading" :disabled="!canUpload" @click="handleUpload">
              上传
            </a-button>
            <a-button @click="handleCancel">
              取消
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-spin>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { InboxOutlined } from '@ant-design/icons-vue'
import { useDocumentStore } from '@/stores/modules/document'
import { storeToRefs } from 'pinia'
import type { UploadProps } from 'ant-design-vue'

interface FormState {
  projectId?: number
  folderId?: number
  tags: string[]
  description: string
  status: 'DRAFT' | 'PUBLISHED'
}

const props = defineProps<{
  visible: boolean
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'success'): void
}>()

const documentStore = useDocumentStore()
const { folders, tags: existingTags } = storeToRefs(documentStore)

const visible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

// 表单状态
const formState = ref<FormState>({
  projectId: undefined,
  folderId: undefined,
  tags: [],
  description: '',
  status: 'PUBLISHED'
})

// 文件列表
const fileList = ref<any[]>([])

// 上传状态
const uploading = ref(false)
const uploadProgress = ref(0)
const uploadStatus = ref<'success' | 'exception' | 'normal' | 'active'>('active')

// 项目选项（模拟数据，实际应从store获取）
const projectOptions = ref([
  { label: '项目A', value: 1 },
  { label: '项目B', value: 2 },
  { label: '项目C', value: 3 }
])

// 文件夹树数据
const folderTreeData = computed(() => folders.value)

// 标签选项
const tagOptions = computed(() => {
  return existingTags.value.map(tag => ({
    label: tag,
    value: tag
  }))
})

// 是否可以上传
const canUpload = computed(() => {
  return fileList.value.length > 0 && formState.value.projectId !== undefined
})

// 文件上传前的钩子
const beforeUpload: UploadProps['beforeUpload'] = (file) => {
  // 检查文件大小（100MB）
  const isLt100M = file.size / 1024 / 1024 < 100
  if (!isLt100M) {
    message.error('文件大小不能超过 100MB!')
    return false
  }

  // 不自动上传，手动控制
  return false
}

// 移除文件
const handleRemove = () => {
  fileList.value = []
}

// 项目筛选
const filterOption = (input: string, option: any) => {
  return option.label.toLowerCase().indexOf(input.toLowerCase()) >= 0
}

// 处理上传
const handleUpload = async () => {
  if (!canUpload.value) {
    message.warning('请选择文件和项目')
    return
  }

  try {
    uploading.value = true
    uploadProgress.value = 0
    uploadStatus.value = 'active'

    const file = fileList.value[0].originFileObj

    // 模拟上传进度
    const progressInterval = setInterval(() => {
      if (uploadProgress.value < 90) {
        uploadProgress.value += 10
      }
    }, 200)

    await documentStore.uploadDocument(file, {
      projectId: formState.value.projectId,
      folderId: formState.value.folderId,
      tags: formState.value.tags,
      description: formState.value.description,
      status: formState.value.status
    })

    clearInterval(progressInterval)
    uploadProgress.value = 100
    uploadStatus.value = 'success'

    message.success('文档上传成功')
    emit('success')
    handleCancel()
  } catch (error) {
    uploadStatus.value = 'exception'
    message.error('文档上传失败')
    console.error('Upload error:', error)
  } finally {
    uploading.value = false
  }
}

// 取消上传
const handleCancel = () => {
  // 重置表单
  formState.value = {
    projectId: undefined,
    folderId: undefined,
    tags: [],
    description: '',
    status: 'PUBLISHED'
  }
  fileList.value = []
  uploadProgress.value = 0
  uploadStatus.value = 'active'
  visible.value = false
}

// 监听visible变化
watch(() => props.visible, async (newVal) => {
  if (newVal) {
    // 打开时加载数据
    await documentStore.fetchFolders()
    await documentStore.fetchTags()
  }
})
</script>

<style scoped lang="scss">
:deep(.ant-upload-drag) {
  border-radius: 8px;
}

:deep(.ant-upload-drag-icon) {
  font-size: 48px;
  color: #1890ff;
}
</style>