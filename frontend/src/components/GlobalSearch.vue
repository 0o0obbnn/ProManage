<template>
  <!-- 搜索触发按钮 -->
  <div class="global-search-trigger" @click="showModal = true">
    <SearchOutlined class="search-icon" />
    <span class="search-text">搜索</span>
    <a-tag class="shortcut-tag">Ctrl+K</a-tag>
  </div>

  <!-- 搜索模态框 -->
  <a-modal
    v-model:open="showModal"
    title="全局搜索"
    width="800px"
    :footer="null"
    destroy-on-close
  >
    <!-- 搜索输入框 -->
    <div class="search-input-wrapper">
      <a-input-search
        ref="searchInputRef"
        v-model:value="keyword"
        placeholder="搜索项目、文档、任务、变更请求..."
        size="large"
        allow-clear
        @search="handleSearch"
        @focus="showSuggestions = true"
        @input="handleInput"
      >
        <template #prefix>
          <SearchOutlined />
        </template>
      </a-input-search>
    </div>

    <!-- 搜索历史 -->
    <div v-if="showHistory && searchHistory.length > 0" class="search-history">
      <div class="history-header">
        <span>搜索历史</span>
        <a-button type="link" size="small" @click="clearHistory">清空</a-button>
      </div>
      <div class="history-list">
        <a-tag
          v-for="(item, index) in searchHistory"
          :key="index"
          closable
          @click="selectHistory(item)"
          @close="removeHistory(index)"
        >
          {{ item }}
        </a-tag>
      </div>
    </div>

    <!-- 搜索建议 -->
    <div v-if="showSuggestions && suggestions.length > 0" class="suggestions">
      <div
        v-for="item in suggestions"
        :key="item.id"
        class="suggestion-item"
        @click="selectSuggestion(item)"
      >
        <component :is="getTypeIcon(item.type)" class="type-icon" />
        <span class="suggestion-title">{{ item.title }}</span>
        <a-tag size="small">{{ getTypeLabel(item.type) }}</a-tag>
      </div>
    </div>

    <!-- 高级搜索 -->
    <a-collapse v-model:activeKey="advancedSearchKey" ghost>
      <a-collapse-panel key="advanced" header="高级搜索">
        <a-form layout="vertical">
          <a-form-item label="搜索范围">
            <a-checkbox-group v-model:value="searchScope">
              <a-checkbox value="project">项目</a-checkbox>
              <a-checkbox value="document">文档</a-checkbox>
              <a-checkbox value="task">任务</a-checkbox>
              <a-checkbox value="change">变更请求</a-checkbox>
            </a-checkbox-group>
          </a-form-item>

          <a-form-item label="时间范围">
            <a-range-picker v-model:value="dateRange" style="width: 100%" />
          </a-form-item>

          <a-form-item label="创建者">
            <a-select
              v-model:value="creatorId"
              placeholder="选择创建者"
              show-search
              allow-clear
              :filter-option="filterUserOption"
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
        </a-form>
      </a-collapse-panel>
    </a-collapse>

    <!-- 搜索结果 -->
    <div v-if="showResults" class="search-results">
      <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
        <a-tab-pane key="all" :tab="`全部 (${allResults.total})`">
          <SearchResultList
            :results="allResults.list"
            :keyword="keyword"
            :loading="loading"
            @select="handleSelectResult"
          />
        </a-tab-pane>
        <a-tab-pane key="project" :tab="`项目 (${projectResults.total})`">
          <SearchResultList
            :results="projectResults.list"
            :keyword="keyword"
            :loading="loading"
            @select="handleSelectResult"
          />
        </a-tab-pane>
        <a-tab-pane key="document" :tab="`文档 (${documentResults.total})`">
          <SearchResultList
            :results="documentResults.list"
            :keyword="keyword"
            :loading="loading"
            @select="handleSelectResult"
          />
        </a-tab-pane>
        <a-tab-pane key="task" :tab="`任务 (${taskResults.total})`">
          <SearchResultList
            :results="taskResults.list"
            :keyword="keyword"
            :loading="loading"
            @select="handleSelectResult"
          />
        </a-tab-pane>
        <a-tab-pane key="change" :tab="`变更 (${changeResults.total})`">
          <SearchResultList
            :results="changeResults.list"
            :keyword="keyword"
            :loading="loading"
            @select="handleSelectResult"
          />
        </a-tab-pane>
      </a-tabs>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  SearchOutlined,
  ProjectOutlined,
  FileTextOutlined,
  CheckCircleOutlined,
  ExclamationCircleOutlined
} from '@ant-design/icons-vue'
import { searchApi, SearchType } from '@/api/modules/search'
import type { SearchResultItem, SearchSuggestion, SearchResult } from '@/api/modules/search'
import SearchResultList from './SearchResultList.vue'
import type { Dayjs } from 'dayjs'

/**
 * 组件状态
 */
const router = useRouter()
const showModal = ref(false)
const keyword = ref('')
const searchInputRef = ref()
const loading = ref(false)
const showSuggestions = ref(false)
const showHistory = ref(true)
const showResults = ref(false)
const activeTab = ref('all')
const advancedSearchKey = ref<string[]>([])

// 搜索历史
const searchHistory = ref<string[]>([])

// 搜索建议
const suggestions = ref<SearchSuggestion[]>([])

// 高级搜索
const searchScope = ref<string[]>(['project', 'document', 'task', 'change'])
const dateRange = ref<[Dayjs, Dayjs] | null>(null)
const creatorId = ref<number>()
const users = ref<any[]>([])

// 搜索结果
const allResults = ref<SearchResult>({ total: 0, list: [], page: 1, pageSize: 20 })
const projectResults = ref<SearchResult>({ total: 0, list: [], page: 1, pageSize: 20 })
const documentResults = ref<SearchResult>({ total: 0, list: [], page: 1, pageSize: 20 })
const taskResults = ref<SearchResult>({ total: 0, list: [], page: 1, pageSize: 20 })
const changeResults = ref<SearchResult>({ total: 0, list: [], page: 1, pageSize: 20 })

/**
 * 获取类型图标
 */
const getTypeIcon = (type: SearchType) => {
  const iconMap = {
    [SearchType.PROJECT]: ProjectOutlined,
    [SearchType.DOCUMENT]: FileTextOutlined,
    [SearchType.TASK]: CheckCircleOutlined,
    [SearchType.CHANGE]: ExclamationCircleOutlined,
    [SearchType.ALL]: SearchOutlined
  }
  return iconMap[type] || SearchOutlined
}

/**
 * 获取类型标签
 */
const getTypeLabel = (type: SearchType) => {
  const labelMap = {
    [SearchType.PROJECT]: '项目',
    [SearchType.DOCUMENT]: '文档',
    [SearchType.TASK]: '任务',
    [SearchType.CHANGE]: '变更',
    [SearchType.ALL]: '全部'
  }
  return labelMap[type] || '未知'
}

/**
 * 获取搜索建议 (防抖)
 */
const fetchSuggestions = async (kw: string) => {
  try {
    const res = await searchApi.getSearchSuggestions(kw)
    suggestions.value = res.data
    showSuggestions.value = true
  } catch (error) {
    console.error('获取搜索建议失败:', error)
  }
}

// 防抖处理 (300ms)
let debounceTimer: any = null
const debouncedFetchSuggestions = (kw: string) => {
  if (debounceTimer) {
    clearTimeout(debounceTimer)
  }
  debounceTimer = setTimeout(() => {
    fetchSuggestions(kw)
  }, 300)
}

/**
 * 处理输入
 */
const handleInput = () => {
  if (!keyword.value || keyword.value.length < 2) {
    suggestions.value = []
    showSuggestions.value = false
    showHistory.value = true
    return
  }

  showHistory.value = false

  // 使用防抖获取搜索建议
  debouncedFetchSuggestions(keyword.value)
}

/**
 * 处理搜索
 */
const handleSearch = async () => {
  if (!keyword.value.trim()) {
    message.warning('请输入搜索关键词')
    return
  }

  // 添加到搜索历史
  addToHistory(keyword.value)

  // 隐藏建议和历史
  showSuggestions.value = false
  showHistory.value = false
  showResults.value = true

  // 执行搜索
  await performSearch()
}

/**
 * 执行搜索
 */
const performSearch = async () => {
  loading.value = true

  try {
    const params = {
      keyword: keyword.value,
      scope: searchScope.value,
      creatorId: creatorId.value,
      startDate: dateRange.value?.[0]?.format('YYYY-MM-DD'),
      endDate: dateRange.value?.[1]?.format('YYYY-MM-DD')
    }

    // 根据当前Tab搜索
    if (activeTab.value === 'all') {
      const res = await searchApi.search(params)
      allResults.value = res.data
    } else if (activeTab.value === 'project') {
      const res = await searchApi.searchProjects(params)
      projectResults.value = res.data
    } else if (activeTab.value === 'document') {
      const res = await searchApi.searchDocuments(params)
      documentResults.value = res.data
    } else if (activeTab.value === 'task') {
      const res = await searchApi.searchTasks(params)
      taskResults.value = res.data
    } else if (activeTab.value === 'change') {
      const res = await searchApi.searchChanges(params)
      changeResults.value = res.data
    }
  } catch (error) {
    message.error('搜索失败')
  } finally {
    loading.value = false
  }
}

/**
 * 处理Tab切换
 */
const handleTabChange = () => {
  performSearch()
}

/**
 * 选择建议
 */
const selectSuggestion = (item: SearchSuggestion) => {
  showModal.value = false
  router.push(item.url)
}

/**
 * 选择搜索结果
 */
const handleSelectResult = (item: SearchResultItem) => {
  showModal.value = false
  router.push(item.url)
}

/**
 * 添加到搜索历史
 */
const addToHistory = (kw: string) => {
  searchHistory.value = [
    kw,
    ...searchHistory.value.filter(k => k !== kw)
  ].slice(0, 10)
  localStorage.setItem('searchHistory', JSON.stringify(searchHistory.value))
}

/**
 * 选择历史
 */
const selectHistory = (item: string) => {
  keyword.value = item
  handleSearch()
}

/**
 * 移除历史
 */
const removeHistory = (index: number) => {
  searchHistory.value.splice(index, 1)
  localStorage.setItem('searchHistory', JSON.stringify(searchHistory.value))
}

/**
 * 清空历史
 */
const clearHistory = () => {
  searchHistory.value = []
  localStorage.removeItem('searchHistory')
}

/**
 * 加载搜索历史
 */
const loadHistory = () => {
  const history = localStorage.getItem('searchHistory')
  if (history) {
    try {
      searchHistory.value = JSON.parse(history)
    } catch (error) {
      console.error('加载搜索历史失败:', error)
    }
  }
}

/**
 * 过滤用户选项
 */
const filterUserOption = (input: string, option: any) => {
  return option.children[0].children.toLowerCase().includes(input.toLowerCase())
}

/**
 * 快捷键处理
 */
const handleKeydown = (e: KeyboardEvent) => {
  if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
    e.preventDefault()
    showModal.value = true
    nextTick(() => {
      searchInputRef.value?.focus()
    })
  }
}

/**
 * 监听模态框显示
 */
watch(showModal, (visible) => {
  if (visible) {
    nextTick(() => {
      searchInputRef.value?.focus()
    })
    showHistory.value = true
    showResults.value = false
  } else {
    keyword.value = ''
    suggestions.value = []
    showSuggestions.value = false
  }
})

/**
 * 组件挂载
 */
onMounted(() => {
  loadHistory()
  window.addEventListener('keydown', handleKeydown)
})

/**
 * 组件卸载
 */
onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped lang="scss">
.global-search-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    background-color: rgba(0, 0, 0, 0.04);
  }

  .search-icon {
    font-size: 16px;
    color: rgba(0, 0, 0, 0.65);
  }

  .search-text {
    font-size: 14px;
    color: rgba(0, 0, 0, 0.65);
  }

  .shortcut-tag {
    font-size: 12px;
    padding: 0 4px;
    border-radius: 2px;
  }
}

.search-input-wrapper {
  margin-bottom: 16px;
}

.search-history {
  margin-bottom: 16px;

  .history-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
    font-size: 14px;
    color: rgba(0, 0, 0, 0.65);
  }

  .history-list {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;

    .ant-tag {
      cursor: pointer;
      margin: 0;

      &:hover {
        opacity: 0.8;
      }
    }
  }
}

.suggestions {
  max-height: 300px;
  overflow-y: auto;
  margin-bottom: 16px;

  .suggestion-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    border-radius: 4px;
    cursor: pointer;
    transition: background-color 0.3s;

    &:hover {
      background-color: rgba(0, 0, 0, 0.04);
    }

    .type-icon {
      font-size: 16px;
      color: #1890ff;
    }

    .suggestion-title {
      flex: 1;
      font-size: 14px;
    }
  }
}

.search-results {
  margin-top: 16px;
}
</style>

