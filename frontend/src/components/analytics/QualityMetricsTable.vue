<template>
  <div class="quality-metrics-table">
    <a-table
      :columns="columns"
      :data-source="qualityMetrics"
      :loading="loading"
      :pagination="false"
      :scroll="{ x: 1000 }"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'projectName'">
          <strong>{{ record.projectName }}</strong>
        </template>

        <template v-else-if="column.key === 'testCoverage'">
          <a-progress
            :percent="record.testCoverage"
            :status="getProgressStatus(record.testCoverage, 70)"
            :size="'small'"
          />
        </template>

        <template v-else-if="column.key === 'bugDensity'">
          <span :style="{ color: getBugDensityColor(record.bugDensity) }">
            {{ record.bugDensity.toFixed(2) }}
          </span>
        </template>

        <template v-else-if="column.key === 'codeQuality'">
          <a-progress
            :percent="record.codeQuality"
            :status="getProgressStatus(record.codeQuality, 75)"
            :size="'small'"
          />
        </template>

        <template v-else-if="column.key === 'documentationScore'">
          <a-progress
            :percent="record.documentationScore"
            :status="getProgressStatus(record.documentationScore, 60)"
            :size="'small'"
          />
        </template>

        <template v-else-if="column.key === 'overallScore'">
          <div class="overall-score-cell">
            <a-progress
              type="circle"
              :percent="record.overallScore"
              :width="60"
              :stroke-color="getOverallScoreColor(record.overallScore)"
            />
            <a-tag :color="getStatusTagColor(record.status)" class="status-tag">
              {{ getStatusText(record.status) }}
            </a-tag>
          </div>
        </template>

        <template v-else-if="column.key === 'trend'">
          <div class="trend-cell">
            <span
              class="trend-icon"
              :style="{ color: getTrendColor(record.trend) }"
            >
              {{ getTrendIcon(record.trend) }}
            </span>
            <span class="trend-text">{{ getTrendText(record.trend) }}</span>
          </div>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { computed, watch, onMounted } from 'vue'
import { useAnalyticsStore } from '@/stores/modules/analytics'
import type { TableColumnType } from 'ant-design-vue'
import type { QualityMetrics } from '@/types/analytics'

interface Props {
  projectId?: string
  dateRange?: [string, string]
}

const props = defineProps<Props>()

const analyticsStore = useAnalyticsStore()

const loading = computed(() => analyticsStore.tableLoading)
const qualityMetrics = computed(() => analyticsStore.qualityMetrics)

const columns: TableColumnType<QualityMetrics>[] = [
  {
    title: '项目名称',
    dataIndex: 'projectName',
    key: 'projectName',
    width: 200,
    fixed: 'left'
  },
  {
    title: '测试覆盖率',
    dataIndex: 'testCoverage',
    key: 'testCoverage',
    width: 180,
    sorter: (a, b) => a.testCoverage - b.testCoverage
  },
  {
    title: '缺陷密度',
    dataIndex: 'bugDensity',
    key: 'bugDensity',
    width: 120,
    align: 'center',
    sorter: (a, b) => a.bugDensity - b.bugDensity
  },
  {
    title: '代码质量',
    dataIndex: 'codeQuality',
    key: 'codeQuality',
    width: 180,
    sorter: (a, b) => a.codeQuality - b.codeQuality
  },
  {
    title: '文档评分',
    dataIndex: 'documentationScore',
    key: 'documentationScore',
    width: 180,
    sorter: (a, b) => a.documentationScore - b.documentationScore
  },
  {
    title: '综合得分',
    key: 'overallScore',
    width: 180,
    align: 'center',
    sorter: (a, b) => a.overallScore - b.overallScore,
    defaultSortOrder: 'descend'
  },
  {
    title: '趋势',
    key: 'trend',
    width: 120,
    align: 'center'
  }
]

const getProgressStatus = (value: number, threshold: number) => {
  if (value >= threshold) return 'success'
  if (value >= threshold * 0.7) return 'normal'
  return 'exception'
}

const getBugDensityColor = (density: number) => {
  if (density < 1) return '#52c41a'
  if (density < 3) return '#faad14'
  return '#f5222d'
}

const getOverallScoreColor = (score: number) => {
  if (score >= 80) return '#52c41a'
  if (score >= 60) return '#1890ff'
  if (score >= 40) return '#faad14'
  return '#f5222d'
}

const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    excellent: '优秀',
    good: '良好',
    fair: '一般',
    poor: '较差'
  }
  return textMap[status] || status
}

const getStatusTagColor = (status: string) => {
  const colorMap: Record<string, string> = {
    excellent: 'success',
    good: 'processing',
    fair: 'warning',
    poor: 'error'
  }
  return colorMap[status] || 'default'
}

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
    up: '#52c41a',
    down: '#f5222d',
    stable: '#8c8c8c'
  }
  return colorMap[trend] || '#8c8c8c'
}

const fetchData = async () => {
  await analyticsStore.fetchQualityMetrics()
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
.quality-metrics-table {
  .overall-score-cell {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;

    .status-tag {
      font-size: 12px;
    }
  }

  .trend-cell {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 4px;

    .trend-icon {
      font-size: 18px;
      font-weight: bold;
    }

    .trend-text {
      font-size: 13px;
    }
  }
}
</style>
