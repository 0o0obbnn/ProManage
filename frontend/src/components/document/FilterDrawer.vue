<template>
  <a-drawer
    v-model:open="visible"
    title="筛选文档"
    :width="400"
    placement="right"
    @close="handleClose"
  >
    <a-form :model="localFilters" layout="vertical">
      <!-- 项目筛选 -->
      <a-form-item label="所属项目">
        <a-select
          v-model:value="localFilters.projectId"
          placeholder="选择项目"
          :options="projectOptions"
          allow-clear
          show-search
          :filter-option="filterOption"
        />
      </a-form-item>

      <!-- 文件类型 -->
      <a-form-item label="文件类型">
        <a-select
          v-model:value="localFilters.fileType"
          placeholder="选择文件类型"
          :options="fileTypeOptions"
          allow-clear
        />
      </a-form-item>

      <!-- 标签筛选 -->
      <a-form-item label="标签">
        <a-select
          v-model:value="localFilters.tags"
          mode="multiple"
          placeholder="选择标签"
          :options="tagOptions"
          allow-clear
          show-search
          :max-tag-count="3"
        />
      </a-form-item>

      <!-- 上传者 -->
      <a-form-item label="上传者">
        <a-select
          v-model:value="localFilters.authorId"
          placeholder="选择上传者"
          :options="authorOptions"
          allow-clear
          show-search
          :filter-option="filterOption"
        />
      </a-form-item>

      <!-- 状态 -->
      <a-form-item label="文档状态">
        <a-select
          v-model:value="localFilters.status"
          placeholder="选择状态"
          :options="statusOptions"
          allow-clear
        />
      </a-form-item>

      <!-- 时间范围 -->
      <a-form-item label="上传时间">
        <a-range-picker
          v-model:value="dateRange"
          style="width: 100%"
          :format="dateFormat"
          @change="handleDateChange"
        />
      </a-form-item>

      <!-- 文件大小 -->
      <a-form-item label="文件大小">
        <a-radio-group v-model:value="localFilters.sizeRange">
          <a-radio-button value="">不限</a-radio-button>
          <a-radio-button value="small">小于1MB</a-radio-button>
          <a-radio-button value="medium">1-10MB</a-radio-button>
          <a-radio-button value="large">大于10MB</a-radio-button>
        </a-radio-group>
      </a-form-item>

      <!-- 排序 -->
      <a-form-item label="排序方式">
        <a-select
          v-model:value="localFilters.sort"
          placeholder="选择排序方式"
          :options="sortOptions"
        />
      </a-form-item>

      <a-form-item label="排序顺序">
        <a-radio-group v-model:value="localFilters.order">
          <a-radio-button value="desc">降序</a-radio-button>
          <a-radio-button value="asc">升序</a-radio-button>
        </a-radio-group>
      </a-form-item>
    </a-form>

    <template #footer>
      <div class="drawer-footer">
        <a-space>
          <a-button @click="handleReset">
            重置
          </a-button>
          <a-button type="primary" @click="handleApply">
            应用筛选
          </a-button>
        </a-space>
      </div>
    </template>
  </a-drawer>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import type { DocumentQueryParams, FileType, DocumentStatus } from '@/types/document'
import dayjs, { type Dayjs } from 'dayjs'

interface LocalFilters extends Partial<DocumentQueryParams> {
  sizeRange?: string
}

const props = defineProps<{
  visible: boolean
  filters: Partial<DocumentQueryParams>
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'apply', filters: Partial<DocumentQueryParams>): void
}>()

const visible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

// 本地筛选状态
const localFilters = ref<LocalFilters>({
  projectId: undefined,
  fileType: undefined,
  tags: [],
  authorId: undefined,
  status: undefined,
  startDate: undefined,
  endDate: undefined,
  sizeRange: '',
  sort: 'createdAt',
  order: 'desc'
})

// 日期范围
const dateRange = ref<[Dayjs, Dayjs] | null>(null)
const dateFormat = 'YYYY-MM-DD'

// 项目选项（模拟数据）
const projectOptions = ref([
  { label: '全部项目', value: undefined },
  { label: '项目A', value: 1 },
  { label: '项目B', value: 2 },
  { label: '项目C', value: 3 }
])

// 文件类型选项
const fileTypeOptions = [
  { label: 'PDF文档', value: 'PDF' },
  { label: 'Word文档', value: 'WORD' },
  { label: 'Excel表格', value: 'EXCEL' },
  { label: 'PPT演示', value: 'PPT' },
  { label: '图片', value: 'IMAGE' },
  { label: '视频', value: 'VIDEO' },
  { label: '音频', value: 'AUDIO' },
  { label: '压缩包', value: 'ZIP' },
  { label: '其他', value: 'OTHER' }
]

// 标签选项（模拟数据）
const tagOptions = ref([
  { label: '需求文档', value: '需求文档' },
  { label: '设计文档', value: '设计文档' },
  { label: '技术文档', value: '技术文档' },
  { label: '测试文档', value: '测试文档' },
  { label: '用户手册', value: '用户手册' },
  { label: '会议纪要', value: '会议纪要' }
])

// 上传者选项（模拟数据）
const authorOptions = ref([
  { label: '张三', value: 1 },
  { label: '李四', value: 2 },
  { label: '王五', value: 3 }
])

// 状态选项
const statusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已发布', value: 'PUBLISHED' },
  { label: '已归档', value: 'ARCHIVED' }
]

// 排序选项
const sortOptions = [
  { label: '上传时间', value: 'createdAt' },
  { label: '更新时间', value: 'updatedAt' },
  { label: '文件名称', value: 'name' },
  { label: '文件大小', value: 'size' },
  { label: '下载次数', value: 'downloadCount' }
]

// 筛选选项
const filterOption = (input: string, option: any) => {
  return option.label.toLowerCase().indexOf(input.toLowerCase()) >= 0
}

// 日期变化
const handleDateChange = (dates: [Dayjs, Dayjs] | null) => {
  if (dates && dates.length === 2) {
    localFilters.value.startDate = dates[0].format(dateFormat)
    localFilters.value.endDate = dates[1].format(dateFormat)
  } else {
    localFilters.value.startDate = undefined
    localFilters.value.endDate = undefined
  }
}

// 应用筛选
const handleApply = () => {
  const filters: Partial<DocumentQueryParams> = {
    projectId: localFilters.value.projectId,
    fileType: localFilters.value.fileType as FileType,
    tags: localFilters.value.tags,
    authorId: localFilters.value.authorId,
    status: localFilters.value.status as DocumentStatus,
    startDate: localFilters.value.startDate,
    endDate: localFilters.value.endDate,
    sort: localFilters.value.sort,
    order: localFilters.value.order
  }

  // 移除 undefined 值
  Object.keys(filters).forEach(key => {
    const k = key as keyof typeof filters
    if (filters[k] === undefined || (Array.isArray(filters[k]) && (filters[k] as any[]).length === 0)) {
      delete filters[k]
    }
  })

  emit('apply', filters)
}

// 重置筛选
const handleReset = () => {
  localFilters.value = {
    projectId: undefined,
    fileType: undefined,
    tags: [],
    authorId: undefined,
    status: undefined,
    startDate: undefined,
    endDate: undefined,
    sizeRange: '',
    sort: 'createdAt',
    order: 'desc'
  }
  dateRange.value = null
}

// 关闭抽屉
const handleClose = () => {
  visible.value = false
}

// 监听props.filters变化，同步到本地状态
watch(
  () => props.filters,
  (newFilters) => {
    localFilters.value = {
      ...localFilters.value,
      ...newFilters
    }

    // 同步日期范围
    if (newFilters.startDate && newFilters.endDate) {
      dateRange.value = [
        dayjs(newFilters.startDate),
        dayjs(newFilters.endDate)
      ]
    } else {
      dateRange.value = null
    }
  },
  { immediate: true, deep: true }
)
</script>

<style scoped lang="scss">
.drawer-footer {
  display: flex;
  justify-content: flex-end;
  padding: 16px 0;
  border-top: 1px solid #e8e8e8;
}
</style>