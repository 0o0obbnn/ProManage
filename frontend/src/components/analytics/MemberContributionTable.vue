<template>
  <div class="member-contribution-table">
    <a-table
      :columns="columns"
      :data-source="memberContributions"
      :loading="loading"
      :pagination="false"
      :scroll="{ x: 1000 }"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'member'">
          <div class="member-cell">
            <a-avatar :size="32" :src="record.avatar">
              {{ record.userName.charAt(0) }}
            </a-avatar>
            <span class="member-name">{{ record.userName }}</span>
          </div>
        </template>

        <template v-else-if="column.key === 'contributionScore'">
          <div class="score-cell">
            <a-progress
              type="circle"
              :percent="Math.min(record.contributionScore, 100)"
              :width="50"
              :stroke-color="{
                '0%': '#108ee9',
                '100%': '#87d068'
              }"
            />
            <span class="score-value">{{ record.contributionScore }}</span>
          </div>
        </template>

        <template v-else-if="column.key === 'trend'">
          <a-tag :color="getTrendColor(record.trend)">
            {{ getTrendIcon(record.trend) }}
            {{ getTrendText(record.trend) }}
          </a-tag>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { computed, watch, onMounted } from 'vue'
import { useAnalyticsStore } from '@/stores/modules/analytics'
import type { TableColumnType } from 'ant-design-vue'
import type { MemberContribution } from '@/types/analytics'

interface Props {
  projectId?: string
  dateRange?: [string, string]
}

const props = defineProps<Props>()

const analyticsStore = useAnalyticsStore()

const loading = computed(() => analyticsStore.tableLoading)
const memberContributions = computed(() => analyticsStore.memberContributions)

const columns: TableColumnType<MemberContribution>[] = [
  {
    title: '成员',
    key: 'member',
    width: 180,
    fixed: 'left'
  },
  {
    title: '完成任务',
    dataIndex: 'tasksCompleted',
    key: 'tasksCompleted',
    width: 100,
    align: 'center',
    sorter: (a, b) => a.tasksCompleted - b.tasksCompleted
  },
  {
    title: '代码提交',
    dataIndex: 'codeCommits',
    key: 'codeCommits',
    width: 100,
    align: 'center',
    sorter: (a, b) => a.codeCommits - b.codeCommits
  },
  {
    title: '文档更新',
    dataIndex: 'documentsUpdated',
    key: 'documentsUpdated',
    width: 100,
    align: 'center',
    sorter: (a, b) => a.documentsUpdated - b.documentsUpdated
  },
  {
    title: '评论数',
    dataIndex: 'comments',
    key: 'comments',
    width: 100,
    align: 'center',
    sorter: (a, b) => a.comments - b.comments
  },
  {
    title: '完成评审',
    dataIndex: 'reviewsCompleted',
    key: 'reviewsCompleted',
    width: 100,
    align: 'center',
    sorter: (a, b) => a.reviewsCompleted - b.reviewsCompleted
  },
  {
    title: '贡献度',
    key: 'contributionScore',
    width: 150,
    align: 'center',
    sorter: (a, b) => a.contributionScore - b.contributionScore,
    defaultSortOrder: 'descend'
  },
  {
    title: '趋势',
    key: 'trend',
    width: 100,
    align: 'center'
  }
]

const getTrendIcon = (trend: string) => {
  const iconMap: Record<string, string> = {
    up: '↑',
    down: '↓',
    stable: '→'
  }
  return iconMap[trend] || '-'
}

const getTrendText = (trend: string) => {
  const textMap: Record<string, string> = {
    up: '上升',
    down: '下降',
    stable: '稳定'
  }
  return textMap[trend] || trend
}

const getTrendColor = (trend: string) => {
  const colorMap: Record<string, string> = {
    up: 'success',
    down: 'error',
    stable: 'default'
  }
  return colorMap[trend] || 'default'
}

const fetchData = async () => {
  await analyticsStore.fetchMemberContribution()
}

watch(
  () => [props.projectId, props.dateRange],
  () => {
    if (props.projectId) {
      analyticsStore.setSelectedProject(props.projectId)
    }
    if (props.dateRange) {
      analyticsStore.setDateRange(props.dateRange)
    }
    fetchData()
  },
  { deep: true }
)

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.member-contribution-table {
  .member-cell {
    display: flex;
    align-items: center;
    gap: 12px;

    .member-name {
      font-weight: 500;
      color: #262626;
    }
  }

  .score-cell {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;

    .score-value {
      font-weight: 600;
      color: #1890ff;
      font-size: 14px;
    }
  }
}
</style>
